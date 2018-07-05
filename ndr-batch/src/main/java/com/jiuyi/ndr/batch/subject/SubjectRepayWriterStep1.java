package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.config.AccountCompensationConfigDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepayDetail;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.service.subject.SubjectRepayBillService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.query.TransactionQueryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * step1
 *
 * 多线程发送还款明细TO厦门银行, 标记处理成功失败
 *
 * Created by lixiaolei on 2017/8/2.
 */
public class SubjectRepayWriterStep1 implements ItemWriter<SubjectRepayDetail> {

    private static final Logger logger = LoggerFactory.getLogger(SubjectRepayWriterStep1.class);

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private SubjectRepayDetailDao subjectRepayDetailDao;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private SubjectRepayBillService subjectRepayBillService;
    @Autowired
    private AccountCompensationConfigDao accountCompensationConfigDao;


    private static final Integer SCOPE_DAYS = 7;//查询天数的范围

    /*private static final ExecutorService threadPool = Executors.newFixedThreadPool(20);*/

    @Override
    public void write(List<? extends SubjectRepayDetail> subjectRepayDetails) throws Exception {

        logger.info("begin multithreading send repay transaction to XM bank!");

        subjectRepayDetails = subjectRepayDetails.stream()
                .filter(subjectRepayDetail ->
                        subjectRepayDetail.getCreateTime().replace("-", "").substring(0, 8).compareTo(getBeforeDateTime(SCOPE_DAYS)) >= 0
                ).collect(Collectors.toList());
        //多线程发送`
        subjectRepayDetails.parallelStream().forEach(this::send);

        logger.info("end mark local repay details");
    }

    private void send(SubjectRepayDetail subjectRepayDetail) {
        //System.out.println(Thread.currentThread().getName());
        Subject subject = subjectService.findBySubjectId(subjectRepayDetail.getSubjectId());
        Integer isDirect = subject.getDirectFlag();
        String borrowerIdXM, profitAcctXM = subject.getProfitAccount();
        if (SubjectRepayDetail.SOURCE_CPS.equals(subjectRepayDetail.getSourceType())) {
            String cpsLocalName = "";
            if(Subject.DIRECT_FLAG_YES.equals(subject.getDirectFlag())){
                cpsLocalName = accountCompensationConfigDao.findByDepartmentAndType(subject.getAccountingDepartment(), subject.getType()).getCompensationAccount();
            }else{
                cpsLocalName = subject.getCompensationAccount().trim();
            }
            borrowerIdXM = cpsLocalName;
        } else if (isDirect.equals(Subject.DIRECT_FLAG_NO)) {
            borrowerIdXM = subject.getIntermediatorIdXM();
        } else {
            borrowerIdXM = subject.getBorrowerIdXM();
        }

        SubjectRepaySchedule subjectRepaySchedule = subjectRepayScheduleService.getById(subjectRepayDetail.getScheduleId());
        String borrowerFreezeExtSn = SubjectRepayDetail.SOURCE_CPS.equals(subjectRepayDetail.getSourceType()) ? subjectRepaySchedule.getExtSnCps() : subjectRepaySchedule.getExtSn();

        Integer principalInterestPenaltyFee = subjectRepayDetail.getPrincipal() + subjectRepayDetail.getInterest() + subjectRepayDetail.getPenalty() + subjectRepayDetail.getFee();
        Integer interest = subjectRepayDetail.getInterest() + subjectRepayDetail.getPenalty() + subjectRepayDetail.getFee();
        double freezeAmount = (subjectRepayDetail.getFreezePrincipal() + subjectRepayDetail.getFreezeInterest() + subjectRepayDetail.getFreezePenalty() + subjectRepayDetail.getFreezeFee()) / 100.0;
        String requestNo = subjectRepayDetail.getExtSn();
        int transStatus = BaseResponse.STATUS_FAILED;
        if (StringUtils.hasText(requestNo) && BaseResponse.STATUS_PENDING.equals(subjectRepayDetail.getExtStatus())) {
            //上次交易状态未知，查询
            transStatus = BaseResponse.STATUS_PENDING;
            logger.info("上次还款交易状态未知，单笔查询，请求流水号{}", requestNo);
            RequestSingleTransQuery request = new RequestSingleTransQuery();
            request.setRequestNo(requestNo);
            request.setTransactionType(TransactionType.TRANSACTION);
            ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
            if (!"0".equals(responseQuery.getCode())) {
                //查询交易失败
                logger.info("上次还款交易状态未知，单笔查询，请求流水号{},查询失败，返回码{}", requestNo, responseQuery.getCode());
                if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(responseQuery.getCode())) {
                    //订单不存在，设置交易失败，重新发起交易
                    subjectRepayDetail.setExtStatus(BaseResponse.STATUS_FAILED);
                    transStatus = BaseResponse.STATUS_FAILED;
                }
            } else {
                TransactionQueryRecord transactionQueryRecord = (TransactionQueryRecord) responseQuery.getRecords().get(0);
                if ("SUCCESS".equals(transactionQueryRecord.getStatus())) {
                    //交易成功
                    logger.info("上次还款交易状态未知，单笔查询，请求流水号{},交易查询成功", requestNo);
                    transStatus = BaseResponse.STATUS_SUCCEED;
                } else if ("FAIL".equals(transactionQueryRecord.getStatus())) {
                    //查询结果，交易失败，重新发起交易
                    logger.info("上次还款交易状态未知，单笔查询，请求流水号{},交易查询失败，重新发起交易", requestNo);
                    subjectRepayDetail.setExtStatus(BaseResponse.STATUS_FAILED);
                    transStatus = BaseResponse.STATUS_FAILED;
                } else {
                    //查询结果：交易处理中
                    logger.info("上次还款交易状态未知，单笔查询，请求流水号{},交易查询处理中", requestNo);
                }
            }
        }
        //如果上次交易失败,重新发起交易
        if (transStatus == BaseResponse.STATUS_FAILED) {
            BaseResponse response = null;
            //若直贷二
            if (SubjectRepayDetail.SOURCE_CPS.equals(subjectRepayDetail.getSourceType())) {
                if(principalInterestPenaltyFee<=0){
                    response = subjectRepayBillService.compensatorySingleTransNew(borrowerFreezeExtSn, subject.getSubjectId(), subjectRepayDetail, principalInterestPenaltyFee, interest, freezeAmount, borrowerIdXM, profitAcctXM);
                }else{
                    response = subjectRepayBillService.compensatorySingleTrans(borrowerFreezeExtSn, subject.getSubjectId(), subjectRepayDetail, principalInterestPenaltyFee, interest, freezeAmount, borrowerIdXM, profitAcctXM);
                }
            } else {
                if(principalInterestPenaltyFee<=0){
                    response = subjectRepayScheduleService.repayForRepay(subject.getSubjectId(), borrowerFreezeExtSn, borrowerIdXM, subjectRepayDetail, principalInterestPenaltyFee, interest, freezeAmount, profitAcctXM);
                }else{
                    response = subjectRepayScheduleService.repayForRepayNew(subject.getSubjectId(), borrowerFreezeExtSn, borrowerIdXM, subjectRepayDetail, principalInterestPenaltyFee, interest, freezeAmount, profitAcctXM);
                }
            }
            requestNo = response.getRequestNo();//请求流水号
            if (response.getStatus().equals(BaseResponse.STATUS_SUCCEED)) {
                transStatus = BaseResponse.STATUS_SUCCEED;
                subjectRepayDetail.setStatus(SubjectRepayDetail.STATUS_REPAID);
                subjectRepayDetail.setExtSn(requestNo);
                subjectRepayDetail.setExtStatus(transStatus);
                subjectRepayDetail.setCurrentStep(SubjectRepayDetail.STEP_HAS_TRANS);
            } else {
                transStatus = BaseResponse.STATUS_PENDING;
                subjectRepayDetail.setStatus(SubjectRepayDetail.STATUS_PENDING);
                subjectRepayDetail.setExtSn(requestNo);
                subjectRepayDetail.setExtStatus(transStatus);
                subjectRepayDetail.setCurrentStep(SubjectRepayDetail.STEP_HAS_TRANS);
            }
        } else if (transStatus == BaseResponse.STATUS_SUCCEED) {
            subjectRepayDetail.setStatus(SubjectRepayDetail.STATUS_REPAID);
            subjectRepayDetail.setExtSn(requestNo);
            subjectRepayDetail.setExtStatus(transStatus);
            subjectRepayDetail.setCurrentStep(SubjectRepayDetail.STEP_HAS_TRANS);
        } else {
            subjectRepayDetail.setStatus(SubjectRepayDetail.STATUS_PENDING);
            subjectRepayDetail.setExtSn(requestNo);
            subjectRepayDetail.setExtStatus(transStatus);
            subjectRepayDetail.setCurrentStep(SubjectRepayDetail.STEP_HAS_TRANS);
        }
        subjectRepayDetailDao.update(subjectRepayDetail);

    }

    /**
     * 获取前几天的日期
     *
     * @param days
     * @return
     */
    private String getBeforeDateTime(Integer days) {
        LocalDate timeNow = LocalDate.now();
        String beforeTime = DateUtil.getDateStr(timeNow.plusDays(-days), DateUtil.DATE_TIME_FORMATTER_8);
        return beforeTime;
    }

}
