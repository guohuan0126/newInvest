package com.jiuyi.ndr.service.subject;

import com.alibaba.fastjson.JSONObject;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.subject.SubjectRepayBillDao;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepayBill;
import com.jiuyi.ndr.domain.subject.SubjectRepayDetail;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestSingleTrans;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.request.RequestUserAutoPreTransaction;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.query.PerTransactionQueryRecord;
import com.jiuyi.ndr.xm.http.response.query.TransactionQueryRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lixiaolei on 2017/9/5.
 */
@Service
public class SubjectRepayBillService {
    private final static Logger logger = LoggerFactory.getLogger(SubjectRepayBillService.class);
    @Autowired
    private SubjectRepayBillDao subjectRepayBillDao;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private TransactionService transactionService;


    public List<SubjectRepayBill> getByScheduleId(Integer scheduleId) {
        return this.getByScheduleIdAndStatus(scheduleId, SubjectRepayBill.STATUS_NOT_REPAY);
    }

    public List<SubjectRepayBill> getBySubjectIdAndType(String subjectId, String type) {
        return subjectRepayBillDao.selectBySubjectIdAndTypeAndStatus(subjectId, type, SubjectRepayBill.STATUS_NOT_REPAY);
    }
    public List<SubjectRepayBill> getBySubjectIdAndTypeAndStatus(String subjectId, String type,Integer status) {
        return subjectRepayBillDao.selectBySubjectIdAndTypeAndStatus(subjectId, type, status);
    }

    public List<SubjectRepayBill> getByScheduleIdAndType(Integer scheduleId, String type) {
        return subjectRepayBillDao.selectByScheduleIdAndTypeAndStatus(scheduleId, type, SubjectRepayBill.STATUS_NOT_REPAY);
    }
    public List<SubjectRepayBill> getByScheduleIdAndType2(Integer scheduleId, String type) {
        return subjectRepayBillDao.getByScheduleIdAndType(scheduleId, type);
    }



    public void batchProcessData() {
        this.batchProcessData(1000);
    }

    /**
     * 批量填充scheduleId，subjectId
     * @param count
     */
    public void batchProcessData(Integer count) {
        List<SubjectRepayBill> subjectRepayBills = subjectRepayBillDao.selectByStatusLimit(SubjectRepayBill.STATUS_CRUDE, count);
        for (SubjectRepayBill subjectRepayBill : subjectRepayBills) {
            String subjectId = subjectService.getByContractNo(subjectRepayBill.getContractId()).getSubjectId();//查询标的号
            Integer scheduleId = subjectRepayScheduleService.findRepaySchedule(subjectId, subjectRepayBill.getTerm()).getId();
            this.update(scheduleId, subjectId, SubjectRepayBill.STATUS_NOT_REPAY, subjectRepayBill.getId());
        }
    }

    public List<SubjectRepayBill> getByScheduleIdAndStatus(Integer scheduleId, Integer status) {
        if (scheduleId == null || status == null) {
            throw new IllegalArgumentException("scheduleId and status can not be null");
        }
        return subjectRepayBillDao.selectByScheduleIdAndStatus(scheduleId, status);
    }

    public int update(Integer scheduleId, String subjectId, Integer status, Integer id) {
        if (scheduleId == null || StringUtils.isBlank(subjectId) || status == null || id == null) {
            throw new IllegalArgumentException("scheduleId and subjectId and status and id can not be null");
        }
        return subjectRepayBillDao.update(scheduleId, subjectId, status, DateUtil.getCurrentDateTime19(), id);
    }

    public int updateType(Integer scheduleId, String subjectId, String type, Integer id) {
        if (scheduleId == null || StringUtils.isBlank(subjectId) || type == null || id == null) {
            throw new IllegalArgumentException("scheduleId and subjectId and type and id can not be null");
        }
        return subjectRepayBillDao.updateType(scheduleId, subjectId, type, DateUtil.getCurrentDateTime19(), id);
    }

    public SubjectRepayBill update(SubjectRepayBill subjectRepayBill) {
        if (subjectRepayBill == null) {
            throw new IllegalArgumentException("subjectRepayBill can not be null");
        }
        if (subjectRepayBill.getId() == null || subjectRepayBill.getId() <= 0) {
            throw new IllegalArgumentException("subjectRepayBill id can not be null");
        }
        subjectRepayBillDao.updateAll(subjectRepayBill);
        return subjectRepayBill;
    }

    public List<SubjectRepayBill> selectByScheduleId(Integer scheduleId){
        if (scheduleId == null ) {
            throw new IllegalArgumentException("scheduleId  can not be null");
        }
        return subjectRepayBillDao.selectByScheduleId(scheduleId);
    }
    /**
     * 直贷二借款金额计算
     */
    public Map<String, Integer> getDirect2BorrowerDetails(SubjectRepaySchedule subjectRepaySchedule,SubjectRepayBill subjectRepayBill){
        Map<String, Integer> borrowerDetails = new HashMap<>();
        Integer duePrincipal = subjectRepaySchedule.getDuePrincipal();
        Integer dueInterest = subjectRepaySchedule.getDueInterest();
        Integer duePenalty = subjectRepaySchedule.getDuePenalty();
        Integer dueFee = subjectRepaySchedule.getDueFee();

        Integer billDuePrincipal = subjectRepayBill.getDuePrincipal();
        Integer billDueInterest = subjectRepayBill.getDueInterest();
        Integer billDuePenalty = subjectRepayBill.getDuePenalty();
        Integer billDueFee = subjectRepayBill.getDueFee();

        Integer repayPrincipal = subjectRepayBill.getRepayPrincipal();
        Integer repayInterest = subjectRepayBill.getRepayInterest();
        Integer repayPenalty = subjectRepayBill.getRepayPenalty();
        Integer repayFee = subjectRepayBill.getRepayFee();
        Integer offlineAmt = subjectRepayBill.getOfflineAmt();
        Integer deratePrincipal = subjectRepayBill.getDeratePrincipal();
        Integer derateInterest = subjectRepayBill.getDerateInterest();
        Integer deratePenalty = subjectRepayBill.getDeratePenalty();
        Integer derateFee = subjectRepayBill.getDerateFee();
        Integer returnPremiumFee = subjectRepayBill.getReturnPremiumFee();
        Integer returnFee = subjectRepayBill.getReturnFee();

        Integer dueTotalAmt = billDuePrincipal + billDueInterest + billDuePenalty + billDueFee;

        Integer repayTotalAmt = repayPrincipal + repayInterest + repayPenalty + repayFee;//实还总金额（不包含减免）

        Integer derateAmt = deratePrincipal + derateInterest + deratePenalty + derateFee;//减免总金额
        Integer returnAmt = returnPremiumFee + returnFee;//退还总金额
        Integer brwActualOutAmt = repayTotalAmt - offlineAmt - returnAmt;//借款人账户实际出款金额
        Integer needCpsAmt = dueTotalAmt  - brwActualOutAmt;//需要从代偿账户出账的金额（包括不足还款额、退还、减免、线下打款）
        needCpsAmt = needCpsAmt<0 ? 0 : needCpsAmt;
        borrowerDetails.put("brwActualOutAmt", brwActualOutAmt);//借款人实还
        borrowerDetails.put("dueTotalAmt", dueTotalAmt);//当期应还
        borrowerDetails.put("derateAmt", derateAmt);//减免费用
        borrowerDetails.put("repayTotalAmt", repayTotalAmt);//实还金额
        borrowerDetails.put("needCpsAmt", needCpsAmt);//代偿出
        //当期应还本金(之前线上线下本金一致,利息有误差;之后本金分布按线上还款本金来算)
        borrowerDetails.put("duePrincipal", duePrincipal);
        borrowerDetails.put("dueInterest", billDueInterest);//当期应还利息
        borrowerDetails.put("duePenalty", duePenalty);//当期应还罚息
        borrowerDetails.put("dueFee", dueFee);//当期应还费用
        borrowerDetails.put("repayPenalty", repayPenalty);//当期实还罚息
        borrowerDetails.put("repayFee", repayFee);//当期实还罚息
        borrowerDetails.put("billDuePenalty", billDuePenalty);//当期实还罚息
        borrowerDetails.put("billDueFee", billDueFee);//当期实还罚息
        return borrowerDetails;
    }

    /**
     * 直贷二借款金额计算(结清的)
     */
    public Map<String, Integer> getDirect2BorrowerDetailsJQ(SubjectRepaySchedule subjectRepaySchedule,SubjectRepayBill subjectRepayBill,Subject subject){
        Map<String, Integer> borrowerDetails = new HashMap<>();
        Integer duePrincipal = subject.getTotalAmt()-subject.getPaidPrincipal();
        Integer dueInterest = subjectRepaySchedule.getDueInterest();
        Integer duePenalty = subjectRepaySchedule.getDuePenalty();
        Integer dueFee = subjectRepaySchedule.getDueFee();

        Integer billDuePrincipal = subjectRepayBill.getDuePrincipal();
        Integer billDueInterest = subjectRepayBill.getDueInterest();
        Integer billDuePenalty = subjectRepayBill.getDuePenalty();
        Integer billDueFee = subjectRepayBill.getDueFee();

        Integer repayPrincipal = subjectRepayBill.getRepayPrincipal();
        Integer repayInterest = subjectRepayBill.getRepayInterest();
        Integer repayPenalty = subjectRepayBill.getRepayPenalty();
        Integer repayFee = subjectRepayBill.getRepayFee();
        Integer offlineAmt = subjectRepayBill.getOfflineAmt();
        Integer deratePrincipal = subjectRepayBill.getDeratePrincipal();
        Integer derateInterest = subjectRepayBill.getDerateInterest();
        Integer deratePenalty = subjectRepayBill.getDeratePenalty();
        Integer derateFee = subjectRepayBill.getDerateFee();
        Integer returnPremiumFee = subjectRepayBill.getReturnPremiumFee();
        Integer returnFee = subjectRepayBill.getReturnFee();

        Integer dueTotalAmt = billDuePrincipal + billDueInterest + billDuePenalty + billDueFee;

        Integer repayTotalAmt = repayPrincipal + repayInterest + repayPenalty + repayFee;//实还总金额（不包含减免）

        Integer derateAmt = deratePrincipal + derateInterest + deratePenalty + derateFee;//减免总金额
        Integer returnAmt = returnPremiumFee + returnFee;//退还总金额
        Integer brwActualOutAmt = repayTotalAmt - offlineAmt - returnAmt;//借款人账户实际出款金额
        Integer needCpsAmt = dueTotalAmt  - brwActualOutAmt;//需要从代偿账户出账的金额（包括不足还款额、退还、减免、线下打款）
        needCpsAmt = needCpsAmt<0 ? 0 : needCpsAmt;
        borrowerDetails.put("brwActualOutAmt", brwActualOutAmt);//借款人实还
        borrowerDetails.put("dueTotalAmt", dueTotalAmt);//当期应还
        borrowerDetails.put("derateAmt", derateAmt);//减免费用
        borrowerDetails.put("repayTotalAmt", repayTotalAmt);//实还金额
        borrowerDetails.put("needCpsAmt", needCpsAmt);//代偿出
        borrowerDetails.put("duePrincipal", duePrincipal);//结清本金
        borrowerDetails.put("dueInterest", billDueInterest);//当期应还利息
        borrowerDetails.put("duePenalty", duePenalty);//当期应还罚息
        borrowerDetails.put("dueFee", dueFee);//当期应还费用
        borrowerDetails.put("repayPenalty", repayPenalty);//当期实还罚息
        borrowerDetails.put("repayFee", repayFee);//当期实还罚息
        borrowerDetails.put("billDuePenalty", billDuePenalty);//当期实还罚息
        borrowerDetails.put("billDueFee", billDueFee);//当期实还罚息
        return borrowerDetails;
    }

    /**
     * 冻结借款人账户用于还代偿账户交易
     * @param subjectId
     * @param userId
     * @param amount
     * @return
     */
    public final BaseResponse freezeBrwAcctForCpsTrans(String subjectId, String userId, Integer amount) {
        RequestUserAutoPreTransaction request = new RequestUserAutoPreTransaction();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setPlatformUserNo(userId);
        //预处理业务类型:还代偿款
        request.setBizType(BizType.COMPENSATORY_REPAYMENT);
        request.setAmount(amount/100.0);
        request.setProjectNo(subjectId);
        return transactionService.userAutoPreTransaction(request);
    }

    /**
     * 冻结代偿账户交易(平台预处理冻结)
     * @param subjectId
     * @param acctXM    代偿金账户编号
     * @param amount
     * @return
     */
    public final BaseResponse freezeCpsAcctTrans(String subjectId, String acctXM, Integer amount) {
        RequestUserAutoPreTransaction request = new RequestUserAutoPreTransaction();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setPlatformUserNo(acctXM);
        //预处理业务类型:代偿
        request.setBizType(BizType.COMPENSATORY);
        request.setAmount(amount/100.0);
        request.setProjectNo(subjectId);
        return transactionService.platformPreTransaction(request);
    }

    /**
     * 冻结代偿账户交易(用户预处理冻结,担保账户)
     * @param subjectId
     * @param acctXM    代偿金账户编号
     * @param amount
     * @return
     */
    public final BaseResponse freezeCpsAcctTrans2(String subjectId, String acctXM, Integer amount) {
        RequestUserAutoPreTransaction request = new RequestUserAutoPreTransaction();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setPlatformUserNo(acctXM);
        //预处理业务类型:代偿
        request.setBizType(BizType.COMPENSATORY);
        request.setAmount(amount/100.0);
        request.setProjectNo(subjectId);
        return transactionService.userAutoPreTransaction(request);
    }

    /**
     * 还代偿账户交易
     * @param subjectId 标的号
     * @param amount    还款总额
     * @param freezeRequestNo 预处理冻结流水号
     * @param borrowId  借款人id
     * @param comAccount   代偿账户
     * @param profitAccount 代偿账户
     * @param profitCps     分润给代偿账户金额(新直贷二模式会有)
     * @return
     */
    public final BaseResponse repayCpsAcctTrans(String subjectId, Integer amount, String freezeRequestNo, String borrowId,
                                                String comAccount,Integer profit,Integer profitCps,String profitAccount) {
        RequestSingleTrans compenTransRepay = new RequestSingleTrans();
        compenTransRepay.setTradeType(TradeType.COMPENSATORY_REPAYMENT);
        compenTransRepay.setRequestNo(IdUtil.getRequestNo());
        compenTransRepay.setProjectNo(subjectId);
        List<RequestSingleTrans.Detail> details = new ArrayList<>();
        RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
        detail.setBizType(BizType.COMPENSATORY_REPAYMENT);
        detail.setFreezeRequestNo(freezeRequestNo);
        //借款人
        detail.setSourcePlatformUserNo(borrowId);
        //代偿账户
        detail.setTargetPlatformUserNo(comAccount);
        detail.setAmount((amount-profit-profitCps) / 100.0);
        details.add(detail);
        if(profit>0) {
            RequestSingleTrans.Detail detail1 = new RequestSingleTrans.Detail();
            detail1.setBizType(BizType.PROFIT);
            detail1.setFreezeRequestNo(freezeRequestNo);
            detail1.setSourcePlatformUserNo(borrowId);//借款人
            detail1.setTargetPlatformUserNo(profitAccount);//分润账户
            detail1.setAmount(profit / 100.0);
            details.add(detail1);
        }
        if(profitCps>0) {
            RequestSingleTrans.Detail detail2 = new RequestSingleTrans.Detail();
            detail2.setBizType(BizType.PROFIT);
            detail2.setFreezeRequestNo(freezeRequestNo);
            detail2.setSourcePlatformUserNo(borrowId);
            detail2.setTargetPlatformUserNo(comAccount);
            detail2.setAmount(profitCps / 100.0);
            details.add(detail2);
        }

        compenTransRepay.setDetails(details);
        return transactionService.singleTrans(compenTransRepay);
    }

    /**
     * 授权预处理单笔交易查询
     * @param requestNo
     * @return
     */
    public final BaseResponse preSingleTransQuery(String requestNo) {
        if (!org.springframework.util.StringUtils.hasText(requestNo)) {
            throw new IllegalArgumentException("单笔交易查询requestNo不能为空");
        }
        BaseResponse baseResponse = null;
        logger.info("上次交易状态未知，单笔查询，请求流水号{}", requestNo);
        RequestSingleTransQuery request = new RequestSingleTransQuery();
        request.setRequestNo(requestNo);
        request.setTransactionType(TransactionType.PRETRANSACTION);
        ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
        if (!"0".equals(responseQuery.getCode())) {
            //查询交易失败
            logger.info("预处理冻结上次交易状态未知，单笔查询，请求流水号{}, 查询失败，返回码{}", requestNo, responseQuery.getCode());
            if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(responseQuery.getCode())) {
                baseResponse = new BaseResponse("交易不存在", BaseResponse.STATUS_FAILED, requestNo);
                logger.info("预处理冻结资金交易不存在，单笔查询，请求流水号{},交易查询失败,{}", requestNo,responseQuery.getDescription());
            }
            if("1".equals(responseQuery.getCode())){
                baseResponse = new BaseResponse("交易失败", BaseResponse.STATUS_FAILED, requestNo);
                logger.info("预处理冻结资金交易状态失败，单笔查询，请求流水号{},交易查询失败,{}", requestNo,responseQuery.getDescription());
            }
        } else {
            PerTransactionQueryRecord transactionQueryRecord = (PerTransactionQueryRecord) responseQuery.getRecords().get(0);
            if("FREEZED".equals(transactionQueryRecord.getStatus())){
                //交易成功
                baseResponse = new BaseResponse("交易成功", BaseResponse.STATUS_SUCCEED, requestNo);
            }else  if("FAIL".equals(transactionQueryRecord.getStatus())){
                //查询结果，交易失败，重新发起交易
                baseResponse = new BaseResponse("交易失败", BaseResponse.STATUS_FAILED, requestNo);
                logger.info("预处理冻结资金交易状态失败，单笔查询，请求流水号{},交易查询失败", requestNo);
            }else{
                baseResponse = new BaseResponse("交易处理中", BaseResponse.STATUS_PENDING, requestNo);
            }
        }
        return baseResponse;
    }

    /**
     * 交易确认单笔交易查询
     * @param requestNo
     * @return
     */
    public final BaseResponse conSingleTransQuery(String requestNo) {
        if (!org.springframework.util.StringUtils.hasText(requestNo)) {
            throw new IllegalArgumentException("单笔交易查询requestNo不能为空");
        }
        BaseResponse baseResponse = null;
        //上次交易状态未知，查询
        logger.info("上次交易状态未知，单笔查询，请求流水号{}", requestNo);
        RequestSingleTransQuery request = new RequestSingleTransQuery();
        request.setRequestNo(requestNo);
        request.setTransactionType(TransactionType.TRANSACTION);
        try {
            ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
            if (!"0".equals(responseQuery.getCode())) {
                //查询交易失败
                logger.info("上次交易状态未知，单笔查询，请求流水号{},查询失败，返回码{}", requestNo, responseQuery.getCode());
                if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(responseQuery.getCode())) {
                    baseResponse = new BaseResponse("交易不存在", BaseResponse.STATUS_FAILED, requestNo);
                }
                if ("1".equals(responseQuery.getCode())) {
                    baseResponse = new BaseResponse("交易失败", BaseResponse.STATUS_FAILED, requestNo);
                }
            } else {
                TransactionQueryRecord transactionQueryRecord = (TransactionQueryRecord) responseQuery.getRecords().get(0);
                if ("SUCCESS".equals(transactionQueryRecord.getStatus())) {
                    baseResponse = new BaseResponse("交易成功", BaseResponse.STATUS_SUCCEED, requestNo);
                    logger.info("上次交易状态未知，单笔查询，请求流水号{},交易查询成功", requestNo);
                } else if ("FAIL".equals(transactionQueryRecord.getStatus())) {
                    baseResponse = new BaseResponse("交易失败", BaseResponse.STATUS_FAILED, requestNo);
                    logger.info("上次交易状态未知，单笔查询，请求流水号{},交易查询失败，重新发起交易", requestNo);
                } else {
                    baseResponse = new BaseResponse("交易处理中", BaseResponse.STATUS_PENDING, requestNo);
                    logger.info("上次交易状态未知，单笔查询，请求流水号{},交易查询处理中", requestNo);
                }
            }
        }catch (ProcessException e){
            baseResponse = new BaseResponse("交易处理中", BaseResponse.STATUS_PENDING, requestNo);
            logger.info("上次交易状态未知，单笔查询，请求流水号{},交易查询处理中", requestNo);
        }
        return baseResponse;
    }

    /**
     * 发厦门代偿交易
     * @param borrowerFreezeExtSn
     * @param subjectId
     * @param subjectRepayDetail
     * @param principalInterestPenaltyFee
     * @param interest
     * @param freezeAmount
     * @return
     */
    public  BaseResponse compensatorySingleTrans(String borrowerFreezeExtSn,String subjectId,
                                                 SubjectRepayDetail subjectRepayDetail,
                                                 Integer principalInterestPenaltyFee,
                                                 Integer interest,
                                                 Double freezeAmount,
                                                 String account, String profitAcctXM){
        logger.info("开始拼接代偿交易报文");
        RequestSingleTrans compenTrans = new RequestSingleTrans();
        compenTrans.setTradeType(TradeType.COMPENSATORY);
        compenTrans.setRequestNo(IdUtil.getRequestNo());
        compenTrans.setProjectNo(subjectId);
        compenTrans.setTransCode(TransCode.SUBJECT_REPAY.getCode());
        List<RequestSingleTrans.Detail> details = new ArrayList<>();
        RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
        detail.setBizType(BizType.COMPENSATORY);
        detail.setFreezeRequestNo(borrowerFreezeExtSn);
        //代偿账户
        detail.setSourcePlatformUserNo(account);
        //投资人
        detail.setTargetPlatformUserNo(subjectRepayDetail.getUserId());
        //若由代偿账户还款,则将佣金先给投资人,再从投资人那收回
        detail.setAmount(BigDecimal.valueOf(principalInterestPenaltyFee + subjectRepayDetail.getCommission()).divide(BigDecimal.valueOf(100)).doubleValue());
        detail.setIncome(BigDecimal.valueOf(interest+subjectRepayDetail.getCommission()).divide(BigDecimal.valueOf(100)).doubleValue());
        details.add(detail);

        if (subjectRepayDetail.getProfit() + subjectRepayDetail.getDeptPenalty() > 0) {
            RequestSingleTrans.Detail detail4 = new RequestSingleTrans.Detail();
            detail4.setBizType(BizType.PROFIT);
            detail4.setFreezeRequestNo(borrowerFreezeExtSn);
            detail4.setSourcePlatformUserNo(account);//代偿账户出分润金额
            detail4.setTargetPlatformUserNo(profitAcctXM);//分润给各事业部
            detail4.setAmount((subjectRepayDetail.getProfit() + subjectRepayDetail.getDeptPenalty()) / 100.0);//分润给各事业部
            details.add(detail4);
        }
        if (subjectRepayDetail.getCommission() > 0 || (subjectRepayDetail.getChannel()==2&&principalInterestPenaltyFee>0)) {
            RequestSingleTrans.Detail detail3 = new RequestSingleTrans.Detail();
            detail3.setBizType(BizType.COMMISSION);
            //从投资人收佣金不需要预处理冻结流水号
            //detail3.setFreezeRequestNo(borrowerFreezeExtSn);
            //投资人出佣金
            detail3.setSourcePlatformUserNo(subjectRepayDetail.getUserIdXm());
            if(subjectRepayDetail.getChannel()==2){
                detail3.setAmount(BigDecimal.valueOf(principalInterestPenaltyFee + subjectRepayDetail.getCommission()).divide(BigDecimal.valueOf(100)).doubleValue());
                freezeAmount=0.0;
            }else{
                detail3.setAmount(subjectRepayDetail.getCommission()/100.0);
            }
            details.add(detail3);
        }
        //若是散标,则不追加冻结
        if(subjectRepayDetail.getChannel()!=0){
            RequestSingleTrans.Detail detail2 = new RequestSingleTrans.Detail();
            detail2.setBizType(BizType.APPEND_FREEZE);
            detail2.setFreezeRequestNo(subjectRepayDetail.getFreezeRequestNo());
            detail2.setSourcePlatformUserNo(subjectRepayDetail.getUserId());//冻结投资人
            detail2.setAmount(subjectRepayDetail.getChannel()==3?0.0:freezeAmount);
            details.add(detail2);
        }

        compenTrans.setDetails(details);
//        logger.info("发代偿还款交易{}", JSONObject.toJSONString(compenTrans));
        return transactionService.singleTrans(compenTrans);
    }

    /**
     * 发厦门代偿交易
     * @param borrowerFreezeExtSn
     * @param subjectId
     * @param subjectRepayDetail
     * @param principalInterestPenaltyFee
     * @param interest
     * @param freezeAmount
     * @return
     */
    public  BaseResponse compensatorySingleTransNew(String borrowerFreezeExtSn,String subjectId,
                                                 SubjectRepayDetail subjectRepayDetail,
                                                 Integer principalInterestPenaltyFee,
                                                 Integer interest,
                                                 Double freezeAmount,
                                                 String account, String profitAcctXM){
        logger.info("开始拼接代偿交易报文");
        RequestSingleTrans compenTrans = new RequestSingleTrans();
        compenTrans.setTradeType(TradeType.COMPENSATORY);
        compenTrans.setRequestNo(IdUtil.getRequestNo());
        compenTrans.setProjectNo(subjectId);
        compenTrans.setTransCode(TransCode.SUBJECT_REPAY.getCode());
        List<RequestSingleTrans.Detail> details = new ArrayList<>();
        RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
        detail.setBizType(BizType.COMPENSATORY);
        detail.setFreezeRequestNo(borrowerFreezeExtSn);
        //代偿账户
        detail.setSourcePlatformUserNo(account);
        //投资人
        detail.setTargetPlatformUserNo(subjectRepayDetail.getUserId());
        //若由代偿账户还款,则将佣金先给投资人,再从投资人那收回
        detail.setAmount(BigDecimal.valueOf(principalInterestPenaltyFee ).divide(BigDecimal.valueOf(100)).doubleValue());
        detail.setIncome(BigDecimal.valueOf(interest).divide(BigDecimal.valueOf(100)).doubleValue());
        details.add(detail);

        if (subjectRepayDetail.getProfit() + subjectRepayDetail.getDeptPenalty() > 0) {
            RequestSingleTrans.Detail detail4 = new RequestSingleTrans.Detail();
            detail4.setBizType(BizType.PROFIT);
            detail4.setFreezeRequestNo(borrowerFreezeExtSn);
            detail4.setSourcePlatformUserNo(account);//代偿账户出分润金额
            detail4.setTargetPlatformUserNo(profitAcctXM);//分润给各事业部
            detail4.setAmount((subjectRepayDetail.getProfit() + subjectRepayDetail.getDeptPenalty()) / 100.0);//分润给各事业部
            details.add(detail4);
        }
        if (subjectRepayDetail.getCommission() > 0 ) {
            RequestSingleTrans.Detail detail3 = new RequestSingleTrans.Detail();
            detail3.setBizType(BizType.COMMISSION);
            //从投资人收佣金不需要预处理冻结流水号
            detail3.setFreezeRequestNo(borrowerFreezeExtSn);
            //投资人出佣金
            detail3.setSourcePlatformUserNo(account);
            detail3.setAmount(subjectRepayDetail.getCommission()/100.0);
            details.add(detail3);
        }
        if (subjectRepayDetail.getChannel()==2&&principalInterestPenaltyFee>0) {
            RequestSingleTrans.Detail detail5 = new RequestSingleTrans.Detail();
            detail5.setBizType(BizType.COMMISSION);
            //从投资人收佣金不需要预处理冻结流水号
            detail5.setSourcePlatformUserNo(subjectRepayDetail.getUserIdXm());
            detail5.setAmount(BigDecimal.valueOf(principalInterestPenaltyFee ).divide(BigDecimal.valueOf(100)).doubleValue());
            freezeAmount=0.0;
            details.add(detail5);
        }
        //若是散标,则不追加冻结
        if(subjectRepayDetail.getChannel()!=0){
            RequestSingleTrans.Detail detail2 = new RequestSingleTrans.Detail();
            detail2.setBizType(BizType.APPEND_FREEZE);
            detail2.setFreezeRequestNo(subjectRepayDetail.getFreezeRequestNo());
            detail2.setSourcePlatformUserNo(subjectRepayDetail.getUserId());//冻结投资人
            detail2.setAmount(subjectRepayDetail.getChannel()==3?0.0:freezeAmount);
            details.add(detail2);
        }

        compenTrans.setDetails(details);
//        logger.info("发代偿还款交易{}", JSONObject.toJSONString(compenTrans));
        return transactionService.singleTrans(compenTrans);
    }

    public int getByScheduleIdAndTypeCount(Integer scheduleId,String type){
        return this.subjectRepayBillDao.getByScheduleIdAndTypeCount(scheduleId,type);
    }

    public int insert(SubjectRepayBill subjectRepayBill){
        return this.subjectRepayBillDao.insert(subjectRepayBill);
    }
}
