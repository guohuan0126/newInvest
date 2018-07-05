package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.subject.SubjectPayoffRegDao;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectPayoffReg;
import com.jiuyi.ndr.domain.subject.SubjectRepayBill;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.account.PlatformTransferService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.subject.SubjectAdvancedPayOffService;
import com.jiuyi.ndr.service.subject.SubjectRepayBillService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.query.PerTransactionQueryRecord;
import com.jiuyi.ndr.xm.http.response.query.TransactionQueryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by lln on 2017/8/2.
 */
public class SubjectRepayStep1Writer implements ItemWriter<SubjectRepaySchedule> {
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private SubjectPayoffRegDao subjectPayoffRegDao;
    @Autowired
    private SubjectAdvancedPayOffService subjectAdvancedPayOffService;
    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private PlatformTransferService platformTransferService;
    @Autowired
    private SubjectRepayBillService subjectRepayBillService;

    @Value(value = "${EMAIL.REPAY_EMAIL}")
    private String repayEmail;

    private static final Logger logger = LoggerFactory.getLogger(SubjectRepayStep1Writer.class);

    //冻结借款人账户资金
    @Override
    public void write(List<? extends SubjectRepaySchedule> subjectRepaySchedules) throws Exception {
        logger.info("未还款任务【{}】条", subjectRepaySchedules.size());
        //isRepay = 1
        //subjectRepaySchedules.parallelStream().forEach(this::freezeAmount);
        for(SubjectRepaySchedule subjectRepaySchedule:subjectRepaySchedules){
            this.freezeAmount(subjectRepaySchedule);
        }
        logger.info("冻结借款人账户处理完成");
    }


    /**
     * 冻结资金
     */
    private void freezeAmount(SubjectRepaySchedule subjectRepaySchedule){
        String subjectId = subjectRepaySchedule.getSubjectId();
        boolean flag = subjectRepayScheduleService.isPossibleForRepay(subjectRepaySchedule);
        if(!flag){
            logger.info("标的不符合还款要求,暂不能还款,subjectId-{}",subjectId);
            return;
        }
//        Integer term = subjectRepaySchedule.getTerm();
        Subject subject = subjectDao.findBySubjectId(subjectId);
        //查询是否是提前结清的标的
        SubjectPayoffReg subjectPayoffReg = subjectPayoffRegDao.findBySubjectIdAndStatus(subjectId,SubjectPayoffReg.REPAY_STATUS_PROCESSED);
        //查询信贷文件表
        List<SubjectRepayBill> repayBills = subjectRepayBillService.selectByScheduleId(subjectRepaySchedule.getId());
        //是否卡贷提前结清
        List<SubjectRepayBill> cardRepayBill = null;
        //若是卡贷且不是直贷二
        if((Subject.SUBJECT_TYPE_CARD.equals(subject.getType())||Subject.SUBJECT_TYPE_CASH.equals(subject.getType())) &&!Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag())){
            //查询是否是提前结清
            cardRepayBill = subjectRepayBillService.getByScheduleIdAndType2(subjectRepaySchedule.getId(),SubjectRepayBill.TYPE_ADVANCED_PAYOFF);
        }
        //是否直贷
        Integer isDirect = subject.getDirectFlag();
        //是否结清
        boolean isSettle = subjectPayoffReg==null && CollectionUtils.isEmpty(cardRepayBill);

        String currentStep;
        if (Subject.DIRECT_FLAG_YES.equals(isDirect)) {
            currentStep = subjectRepaySchedule.getCurrentStep() == null ? "market" : subjectRepaySchedule.getCurrentStep().toLowerCase();
        } else {
            //若是债转或是直贷二,直接冻结借款人账户
            currentStep = subjectRepaySchedule.getCurrentStep() == null ? "freeze" : subjectRepaySchedule.getCurrentStep().toLowerCase();
        }
        String extSn = subjectRepaySchedule.getExtSn();
        Integer extStatus = subjectRepaySchedule.getExtStatus();
        //借款信息
        Map<String, Integer> borrowerDetails ;
        if(isSettle){
            borrowerDetails = subjectRepayScheduleService.getBorrowerDetails(subjectRepaySchedule);
        }else if(cardRepayBill!=null&&cardRepayBill.size()>0){
            borrowerDetails = subjectRepayScheduleService.getBorrowerAdvanceDetails(subject,subjectRepaySchedule,0);;
        }else{
            //计算提前结清罚息
            Integer payOffPenalty = subjectAdvancedPayOffService.advancedPayOff(subjectId);
            borrowerDetails = subjectRepayScheduleService.getBorrowerAdvanceDetails(subject,subjectRepaySchedule,payOffPenalty);
        }
        double amount=0.0;
        //直贷二
        if(isDirect.equals(Subject.DIRECT_FLAG_YES_01)){
            //直贷二计算需冻结的金额
            if(!repayBills.isEmpty()){
                amount = subjectRepayBillService.getDirect2BorrowerDetails(subjectRepaySchedule,repayBills.get(0)).get("brwActualOutAmt")/100.0;//借款人账户实际出款金额
            }
        }else{
            amount = (borrowerDetails.get("duePrincipal") + borrowerDetails.get("dueInterest") + borrowerDetails.get("duePenalty") + borrowerDetails.get("dueFee")) / 100.0;
        }

        logger.info("标的-{}-还款金额-{}-",subjectId,amount);
        if ("market".equals(currentStep)) {
            //若需给营销款打款,更新还款计划
            this.dealMarket(subjectRepaySchedule,extStatus,extSn,subject,amount);
            subjectRepaySchedule = subjectRepayScheduleService.getById(subjectRepaySchedule.getId());
            currentStep = subjectRepaySchedule.getCurrentStep();
        }
        if ("freeze".equals(currentStep)) {
            //冻结资金,更新还款计划
            this.dealFreeze(subjectRepaySchedule,subjectRepaySchedule.getExtStatus(),subjectRepaySchedule.getExtSn(),subject,amount);
            subjectRepaySchedule = subjectRepayScheduleService.getById(subjectRepaySchedule.getId());
            currentStep = subjectRepaySchedule.getCurrentStep();
        }
        if ("repay".equals(currentStep)
                && BaseResponse.STATUS_SUCCEED.equals(subjectRepaySchedule.getExtStatus())){
            subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_FOR_FROZEN);
        }
        subjectRepayScheduleService.update(subjectRepaySchedule);
    }

    /**
     * 处理营销款打款交易
     */
    private void dealMarket(SubjectRepaySchedule subjectRepaySchedule,Integer extStatus,String extSn,Subject subject,Double amount) {
        String subjectId = subjectRepaySchedule.getSubjectId();
        Integer term = subjectRepaySchedule.getTerm();
        logger.info("标的-{},第{}期开始处理营销款打款", subjectId,term);
        String intermediatorId=subject.getIntermediatorId().trim();
        String requestNo = null;
        if (extStatus != null && BaseResponse.STATUS_PENDING.equals(extStatus)) {
            //上次发放营销款状态未知，同样的流水号再发送一次
            requestNo = extSn;
            //进行单笔业务查询 是否需要给营销款打钱
            RequestSingleTransQuery request = new RequestSingleTransQuery();
            request.setRequestNo(extSn);
            request.setTransactionType(TransactionType.TRANSACTION);
            ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
            if (!"0".equals(responseQuery.getCode())) {
                //查询交易失败
                logger.info("上次发放营销款交易状态未知，单笔查询，请求流水号{},查询失败，返回码{}", requestNo, responseQuery.getCode());
                if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(responseQuery.getCode())) {
                    subjectRepaySchedule.setExtStatus(BaseResponse.STATUS_FAILED);
                    //生成新的流水号
                    String newRequestNo = IdUtil.getRequestNo();
                    //营销款打款这步同步把两个字段都设置,为了不修改其他地方
                    subjectRepaySchedule.setExtSn(newRequestNo);
                    subjectRepaySchedule.setMarketSn(newRequestNo);
                    extStatus = BaseResponse.STATUS_FAILED;
                    requestNo=newRequestNo;
                }
            } else {
                TransactionQueryRecord transactionQueryRecord = (TransactionQueryRecord) responseQuery.getRecords().get(0);
                if ("SUCCESS".equals(transactionQueryRecord.getStatus())) {
                    //交易成功
                    subjectRepaySchedule.setExtStatus(BaseResponse.STATUS_SUCCEED);
                    extStatus = BaseResponse.STATUS_SUCCEED;
                } else if ("FAIL".equals(transactionQueryRecord.getStatus())) {
                    //查询结果，交易失败，重新发起交易
                    logger.info("发放营销款交易状态失败，单笔查询，请求流水号{},交易查询失败，重新发起交易", requestNo);
                    subjectRepaySchedule.setExtStatus(BaseResponse.STATUS_FAILED);
                    String newRequestNo = IdUtil.getRequestNo();
                    subjectRepaySchedule.setExtSn(newRequestNo);
                    subjectRepaySchedule.setMarketSn(newRequestNo);
                    extStatus = BaseResponse.STATUS_FAILED;
                    requestNo=newRequestNo;
                } else {
                    //查询结果：交易处理中
                    logger.info("发放营销款交易状态未知，单笔查询，请求流水号{},交易查询处理中", requestNo);
                    subjectRepaySchedule.setExtStatus(BaseResponse.STATUS_PENDING);
                    extStatus = BaseResponse.STATUS_PENDING;
                }
            }
        }
        if (BaseResponse.STATUS_FAILED .equals(extStatus) || extStatus==null) {
            double totalActualMoney = ArithUtil.round(platformTransferService.selectTotalSctualMoneyByInterviewerId(intermediatorId),2);
            if(totalActualMoney<amount&&Subject.DIRECT_FLAG_YES.equals(subject.getDirectFlag())){
                logger.warn("标的{}还款营销款打款失败，居间人{}在营销款账户余额不足", subjectId,intermediatorId);
                return;
            }
            //上次交易失败 重新发起交易
            BaseResponse marketingResponse;
            try {
                marketingResponse = subjectRepayScheduleService.marketingForRepay(subject.getBorrowerIdXM(), amount, requestNo);
                subjectRepaySchedule.setExtSn(marketingResponse.getRequestNo());
                subjectRepaySchedule.setExtStatus(marketingResponse.getStatus());
                subjectRepaySchedule.setMarketSn(marketingResponse.getRequestNo());
                if (marketingResponse.getStatus().equals(BaseResponse.STATUS_FAILED)) {
                    extStatus = BaseResponse.STATUS_FAILED;
                    String newRequestNo = IdUtil.getRequestNo();
                    subjectRepaySchedule.setExtSn(newRequestNo);
                    subjectRepaySchedule.setMarketSn(newRequestNo);
                    logger.warn("标的{}第{}期还款失败，发放营销款失败", subjectId, term);
                    noticeService.sendEmail("标的还款营销款账户异常", "标的"+subjectId+"营销款到借款人充值失败", repayEmail);
                }else if (marketingResponse.getStatus().equals(BaseResponse.STATUS_PENDING)) {
                    extStatus = BaseResponse.STATUS_PENDING;
                    noticeService.sendEmail("标的还款营销款账户异常", "标的"+subjectId+"营销款到借款人充值状态未知", repayEmail);
                    logger.warn("标的{}第{}期还款失败，发放营销款状态未知", subjectId, term);
                }else{
                    extStatus = BaseResponse.STATUS_SUCCEED;
                }
            } catch (ProcessException pe) {
                logger.warn("标的{}第{}期还款失败，发放营销款异常", subjectId, term);
                subjectRepayScheduleService.update(subjectRepaySchedule);
                return;
            }
        }
        if(BaseResponse.STATUS_SUCCEED.equals(extStatus)){
            //营销款出账-插入表platform_transfer
            platformTransferService.out(subject.getBorrowerId(), amount, String.valueOf(subjectRepaySchedule.getId()), subjectRepaySchedule.getMarketSn(), intermediatorId,subjectId);
            subjectRepaySchedule.setCurrentStep("freeze");
        }
        subjectRepayScheduleService.update(subjectRepaySchedule);
    }


    /**
     * 处理冻结账户
     */
    private void dealFreeze(SubjectRepaySchedule subjectRepaySchedule,Integer extStatus,String extSn,Subject subject,Double amount){
        String requestNo = null;
        String subjectId = subjectRepaySchedule.getSubjectId();
        Integer term = subjectRepaySchedule.getTerm();
        logger.info("标的-{},第{}期开始处理冻结账户", subjectId,term);
        Integer isDirect = subject.getDirectFlag();
        String borrowerIdXM="";
        if(isDirect.equals(Subject.DIRECT_FLAG_NO)){
            borrowerIdXM = subject.getIntermediatorIdXM();
        }else{
            borrowerIdXM = subject.getBorrowerIdXM();
        }
        //当前还款明细的交易状态是处理中或是直贷项目
        if (extStatus != null && BaseResponse.STATUS_PENDING.equals(extStatus)) {
            //上次状态未知，同样的流水号再发送一次
            requestNo = extSn;
            //进行单笔业务查询
            RequestSingleTransQuery request = new RequestSingleTransQuery();
            request.setRequestNo(requestNo);
            request.setTransactionType(TransactionType.PRETRANSACTION);
            ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
            if(!"0".equals(responseQuery.getCode())){
                if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(responseQuery.getCode())) {
                    //订单不存在,重新发起交易
                    subjectRepaySchedule.setExtStatus(BaseResponse.STATUS_FAILED);
                    extStatus = BaseResponse.STATUS_FAILED;
                }
                if("1".equals(responseQuery.getCode())){
                    logger.info("标的{}冻结借款人资金交易状态失败，单笔查询，请求流水号{},交易查询失败,{}",subjectId, requestNo,responseQuery.getDescription());
                    subjectRepaySchedule.setExtStatus(BaseResponse.STATUS_FAILED);
                    extStatus = BaseResponse.STATUS_FAILED;
                }
                subjectRepaySchedule.setExtSn(IdUtil.getRequestNo());
            } else{
                PerTransactionQueryRecord transactionQueryRecord = (PerTransactionQueryRecord) responseQuery.getRecords().get(0);
                if("FREEZED".equals(transactionQueryRecord.getStatus())){
                    //交易成功
                    subjectRepaySchedule.setExtStatus(BaseResponse.STATUS_SUCCEED);
                    extStatus = BaseResponse.STATUS_SUCCEED;
                }else  if("FAIL".equals(transactionQueryRecord.getStatus())){
                    //查询结果，交易失败，重新发起交易
                    logger.info("标的{}冻结借款人资金交易状态失败，单笔查询，请求流水号{},交易查询失败",subjectId, requestNo);
                    subjectRepaySchedule.setExtStatus(BaseResponse.STATUS_FAILED);
                    subjectRepaySchedule.setExtSn(IdUtil.getRequestNo());
                    extStatus = BaseResponse.STATUS_FAILED;
                }else{
                    logger.info("标的{}冻结借款人资金交易状态未知，单笔查询，请求流水号{},交易查询失败",subjectId, requestNo);
                }
            }
        }else{
            BaseResponse freezeResponse;
            try {
                logger.info("标的{}第{}期,冻结金额{}",subjectId,term,amount);
                freezeResponse = subjectRepayScheduleService.freezeForRepay(subjectId,borrowerIdXM, amount, requestNo);
                subjectRepaySchedule.setExtSn(freezeResponse.getRequestNo());
                subjectRepaySchedule.setExtStatus(freezeResponse.getStatus());
                if (freezeResponse.getStatus().equals(BaseResponse.STATUS_FAILED)) {
                    extStatus = BaseResponse.STATUS_FAILED;
                    logger.warn("标的{}第{}期还款失败，冻结借款人资金失败", subjectId, term);
                    subjectRepaySchedule.setExtSn(IdUtil.getRequestNo());
                }else if (freezeResponse.getStatus().equals(BaseResponse.STATUS_PENDING)) {
                    extStatus = BaseResponse.STATUS_PENDING;
                    logger.warn("标的{}第{}期还款失败，冻结借款人资金状态未知", subjectId, term);
                }else{

                    extStatus = BaseResponse.STATUS_SUCCEED;
                }
            } catch (ProcessException pe) {
                logger.warn("标的{}第{}期还款失败，冻结账户资金异常", subjectId, term);
                subjectRepayScheduleService.update(subjectRepaySchedule);
                return;
            }
        }
        if(BaseResponse.STATUS_SUCCEED.equals(extStatus)){
            subjectRepaySchedule.setCurrentStep("repay");
        }

         subjectRepayScheduleService.update(subjectRepaySchedule);
    }
}
