package com.jiuyi.ndr.batch.iplan;

import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.dao.xm.TransLogDao;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.xm.TransLog;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.*;
import com.jiuyi.ndr.xm.http.response.ResponseQueryProjectInformation;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.query.CancelPreTransactionQueryRecord;
import com.jiuyi.ndr.xm.http.response.query.PerTransactionQueryRecord;
import com.jiuyi.ndr.xm.http.response.query.TransactionQueryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by zhangyibo on 2017/6/15.
 */
public class IPlanTradeCompensateTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(IPlanTradeCompensateTasklet.class);

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private IPlanAccountDao iPlanAccountDao;

    @Autowired
    private IPlanAccountService iPlanAccountService;

    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;

    @Autowired
    private TransLogDao transLogDao;
    @Autowired
    private IPlanDao iPlanDao;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private CreditDao creditDao;


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        //转入补偿
        transferInCompensate();
        //流标补偿
        transferInCancleCompensate();

        //mergeCredit();
        changeSubjectXM();
        return RepeatStatus.FINISHED;
    }

    private void changeSubjectXM() {
        List<TransLog> transLogList = transLogDao.findByTransCodeAndStatus(TransCode.MODIFY_PROJECT.getCode(), BaseResponse.STATUS_PENDING);
        for (TransLog transLog:transLogList){
            RequestModifyProject modifyProject = JSON.parseObject(transLog.getRequestPacket(), RequestModifyProject.class);
            ResponseQueryProjectInformation response = transactionService.queryProjectInformation(modifyProject.getProjectNo());
            if (modifyProject!=null){
                if (!response.getStatus().equals(Subject.SUBJECT_STATUS_REPAY_NORMAL_XM)&&!response.getStatus().equals(Subject.SUBJECT_STATUS_FINISH_XM)){
                    if (modifyProject.getStatus().equals(Subject.SUBJECT_STATUS_REPAY_NORMAL_XM)){
                        BaseResponse baseResponse = transactionService.modifyProject(modifyProject);
                        if (baseResponse.getStatus().equals(Subject.SUBJECT_STATUS_REPAY_NORMAL_XM)){
                            transLog.setStatus(BaseResponse.STATUS_SUCCEED);
                            transLogDao.update(transLog);
                        }
                    }

                } else {
                    transLog.setStatus(BaseResponse.STATUS_SUCCEED);
                    transLogDao.update(transLog);
                }
            }

        }
    }

    public void mergeCredit() {
        Set<Integer> creditStatus = new HashSet<>();
        creditStatus.add(Credit.CREDIT_STATUS_HOLDING);
        List<Credit> credits = creditDao.findByUserIdAndCreditStatusAndSourceChannelAndSourceAccountId("i6rABfMz2Qnmielx", creditStatus, Credit.SOURCE_CHANNEL_IPLAN, 110803);

        Map<String, List<Credit>> collect = credits.stream().collect(Collectors.groupingBy(Credit::getSubjectId));

        for (Map.Entry<String, List<Credit>> entry : collect.entrySet()) {
            List<Credit> credits1 = entry.getValue();
            Integer transferPrincipal = credits1.stream().map(credit -> credit.getHoldingPrincipal()).reduce(Integer::sum).orElse(0);
            for (int i = 0; i < credits1.size(); i++) {
                if (i == 0) {
                    Credit credit = credits1.get(i);
                    credit.setInitPrincipal(transferPrincipal);
                    credit.setHoldingPrincipal(transferPrincipal);
                    creditDao.update(credit);
                } else {
                    Credit credit = credits1.get(i);
                    credit.setHoldingPrincipal(0);
                    credit.setCreditStatus(Credit.CREDIT_STATUS_FINISH);
                    creditDao.update(credit);
                }
            }
        }
    }


    private void transferInCancleCompensate() {
        List<TransLog> transLogListFreeze = transLogDao.findByTransCodeAndStatus(TransCode.IPLAN_INVEST_UNFREEZE.getCode(), BaseResponse.STATUS_PENDING);
        for (TransLog transLog : transLogListFreeze) {
            IPlanTransLog iPlanTransLog = iPlanTransLogDao.findByExtSnAndExtStatus(transLog.getTxnSn(), BaseResponse.STATUS_PENDING);
            //只对流标的进行补偿
            if (IPlanTransLog.TRANS_STATUS_TO_CANCEL.equals(iPlanTransLog.getTransType())) {
                RequestSingleTransQuery queryRequest = new RequestSingleTransQuery();
                queryRequest.setTransactionType(TransactionType.CANCEL_PRETRANSACTION);
                queryRequest.setRequestNo(transLog.getTxnSn());
                ResponseSingleTransQuery queryResponse = transactionService.singleTransQuery(queryRequest);
                if (BaseResponse.STATUS_SUCCEED.equals(queryResponse.getStatus())) {
                    CancelPreTransactionQueryRecord record = (CancelPreTransactionQueryRecord) queryResponse.getRecords().get(0);
                    if ("SUCCESS".equals(record.getStatus())) {
                        transLog.setStatus(BaseResponse.STATUS_SUCCEED);
                        transLogDao.update(transLog);
                        iPlanTransLog.setTransStatus(TransLog.STATUS_SUCCEED);
                        iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                        iPlanTransLog.setUpdateTime(DateUtil.getCurrentDateTime19());
                        iPlanTransLogDao.update(iPlanTransLog);
                    } else if ("FAIL".equals(record.getStatus())) {
                        transLog.setStatus(BaseResponse.STATUS_FAILED);
                        transLog.setUpdateTime(DateUtil.getCurrentDateTime19());
                        transLogDao.update(transLog);
                    } else {
                        //处理中的情况暂时不做处理
                    }
                } else if (BaseResponse.STATUS_FAILED.equals(queryResponse.getStatus())) {
                    if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(queryResponse.getCode())) {
                        transLog.setStatus(BaseResponse.STATUS_FAILED);
                        transLogDao.update(transLog);
                        RequestIntelligentProjectUnfreeze lastRequest = JSON.parseObject(transLog.getRequestPacket(), RequestIntelligentProjectUnfreeze.class);
                        logger.info("{}重试流标解冻操作发送到厦门银行的报文->{}", transLog.getTxnSn(), JSON.toJSONString(lastRequest));
                        BaseResponse retryResponse = transactionService.intelligentProjectUnfreeze(lastRequest);
                        logger.info("{}流标解冻重试厦门银行返回报文->{}", transLog.getTxnSn(), JSON.toJSON(retryResponse));
                        if (BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())) {
                            iPlanTransLog.setTransStatus(TransLog.STATUS_SUCCEED);
                            iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                            iPlanTransLog.setUpdateTime(DateUtil.getCurrentDateTime19());
                            iPlanTransLogDao.update(iPlanTransLog);
                        }
                    }
                } else {
                    //处理中的情况暂时不做处理
                }
            }
        }
    }

    /**
     * 转入补偿
     */
    private void transferInCompensate() {
        //查询所有处理中的转入记录
        List<IPlanTransLog> iPlanTransLogs = iPlanTransLogDao.findPendingTransLog();
        logger.info("共有{}条转入记录状态未知 需要进行补偿:", iPlanTransLogs.size());
        for (IPlanTransLog iPlanTransLog : iPlanTransLogs) {
            logger.info("交易记录{}正在进行定期转入补偿", iPlanTransLog.getExtSn());
            List<TransLog> transLogs = transLogDao.findByTxnSnAndTransCode(iPlanTransLog.getExtSn(), TransCode.IPLAN_INVEST_FREEZE.getCode());
            if (transLogs == null || transLogs.size() == 0) {
                return;
            }
            TransLog transLog = transLogs.get(transLogs.size() - 1);
            //查询这笔请求对应的活期交易记录
            //根据活期交易记录查询到对应的活期账户
            IPlanAccount iPlanAccount = iPlanAccountDao.findByAccountIdForUpdate(iPlanTransLog.getAccountId());

            //首次投资该理财计划
            if (IPlanTransLog.TRANS_TYPE_INIT_IN.equals(iPlanTransLog.getTransType())) {
                //调用预处理交易查询
                ResponseSingleTransQuery response = transactionService.singleTransQuery(constructRequest(transLog.getTxnSn(), TransactionType.PRETRANSACTION));
                if (BaseResponse.STATUS_SUCCEED.equals(response.getStatus())) {
                    PerTransactionQueryRecord record = ((PerTransactionQueryRecord) response.getRecords().get(0));
                    if ("FREEZED".equals(record.getStatus())) {
                        //更新上一次请求的交易结果
                        transLog.setStatus(BaseResponse.STATUS_SUCCEED);
                        transLogDao.update(transLog);
                        int amtToInvest = iPlanTransLog.getTransAmt();
                        iPlanAccount.setCurrentPrincipal(iPlanAccount.getCurrentPrincipal() + amtToInvest);
                        iPlanAccount.setInitPrincipal(iPlanAccount.getInitPrincipal() + amtToInvest);
                        iPlanAccount.setAmtToInvest(iPlanAccount.getAmtToInvest() + amtToInvest);
                        iPlanAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
                        this.calcInterest(iPlanAccount);
                        iPlanAccountDao.update(iPlanAccount);
                        iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                        iPlanTransLog.setUpdateTime(DateUtil.getCurrentDateTime19());
                        iPlanTransLogDao.update(iPlanTransLog);
                    } else if ("FAIL".equals(record.getStatus())) {
                        //更新上一次请求的交易结果
                        transLog.setStatus(BaseResponse.STATUS_FAILED);
                        transLogDao.update(transLog);
                        RequestPurchaseIntelligentProject request = JSON.parseObject(transLog.getRequestPacket(), RequestPurchaseIntelligentProject.class);
                        request.setRequestNo(IdUtil.getRequestNo());
                        iPlanTransLog.setExtSn(request.getRequestNo());
                        iPlanTransLogDao.update(iPlanTransLog);
                        logger.info("{}正在发起定期新手转入重试:{}", request.getRequestNo(), JSON.toJSONString(request));
                        BaseResponse retryResponse = transactionService.purchaseIntelligentProject(request);
                        logger.info("{}重试后的结果为:{}", request.getRequestNo(), JSON.toJSONString(retryResponse));
                        if (BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())) {
                            int amtToInvest = iPlanTransLog.getTransAmt();
                            iPlanAccount.setCurrentPrincipal(iPlanAccount.getCurrentPrincipal() + amtToInvest);
                            iPlanAccount.setInitPrincipal(iPlanAccount.getInitPrincipal() + amtToInvest);
                            iPlanAccount.setAmtToInvest(iPlanAccount.getAmtToInvest() + amtToInvest);
                            iPlanAccount.setStatus(IPlanAccount.STATUS_PROCEEDS);
                            iPlanAccount.setInvestRequestNo(request.getRequestNo());
                            this.calcInterest(iPlanAccount);
                            iPlanAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
                            iPlanAccountDao.update(iPlanAccount);
                            iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                            iPlanTransLogDao.update(iPlanTransLog);
                        }
                    } else {
                        //处理中的情况暂时不做处理
                    }
                } else if (BaseResponse.STATUS_FAILED.equals(response.getStatus())) {
                    //如果是因为没有请求到厦门银行而导致的失败
                    if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(response.getCode())) {
                        //更新上一次请求的交易结果
                        transLog.setStatus(BaseResponse.STATUS_FAILED);
                        transLogDao.update(transLog);
                        RequestPurchaseIntelligentProject request = JSON.parseObject(transLog.getRequestPacket(), RequestPurchaseIntelligentProject.class);
                        logger.info("{}正在发起定期新手转入重试:{}", transLog.getTxnSn(), JSON.toJSONString(request));
                        BaseResponse retryResponse = transactionService.purchaseIntelligentProject(request);
                        logger.info("{}的定期新手转入重试结果为{}", transLog.getTxnSn(), JSON.toJSONString(retryResponse));
                        if (BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())) {
                            int amtToInvest = iPlanTransLog.getTransAmt();
                            iPlanAccount.setCurrentPrincipal(iPlanAccount.getCurrentPrincipal() + amtToInvest);
                            iPlanAccount.setInitPrincipal(iPlanAccount.getInitPrincipal() + amtToInvest);
                            iPlanAccount.setAmtToInvest(iPlanAccount.getAmtToInvest() + amtToInvest);
                            iPlanAccount.setStatus(IPlanAccount.STATUS_PROCEEDS);
                            this.calcInterest(iPlanAccount);
                            iPlanAccount.setInvestRequestNo(transLog.getTxnSn());
                            iPlanAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
                            iPlanAccountDao.update(iPlanAccount);
                            iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                            iPlanTransLogDao.update(iPlanTransLog);
                        }
                    } else {
                        logger.error("转入失败->厦门银行返回:{},活期账户(iPlanAccount)信息:{},转入交易(iPlanTransLog)信息:{},TransLog信息:{}",
                                JSON.toJSONString(response), JSON.toJSONString(iPlanAccount), JSON.toJSONString(iPlanTransLog), JSON.toJSONString(transLog));
                    }
                }
            } else if (IPlanTransLog.TRANS_TYPE_NORMAL_IN.equals(iPlanTransLog.getTransType())) {
                //普通投资
                int amtToInvest = iPlanTransLog.getTransAmt();
                RequestSingleTransQuery queryRequest = new RequestSingleTransQuery();
                queryRequest.setRequestNo(transLog.getTxnSn());
                queryRequest.setTransactionType(TransactionType.TRANSACTION);
                ResponseSingleTransQuery queryResponse = transactionService.singleTransQuery(queryRequest);
                if (BaseResponse.STATUS_SUCCEED.equals(queryResponse.getStatus())) {
                    TransactionQueryRecord record = (TransactionQueryRecord) queryResponse.getRecords().get(0);
                    if ("SUCCESS".equals(record.getStatus())) {
                        transLog.setStatus(BaseResponse.STATUS_SUCCEED);
                        transLogDao.update(transLog);
                        iPlanAccount.setCurrentPrincipal(iPlanAccount.getCurrentPrincipal() + amtToInvest);
                        iPlanAccount.setInitPrincipal(iPlanAccount.getInitPrincipal() + amtToInvest);
                        iPlanAccount.setAmtToInvest(iPlanAccount.getAmtToInvest() + amtToInvest);
                        iPlanAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
                        this.calcInterest(iPlanAccount);
                        iPlanAccountDao.update(iPlanAccount);
                        iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                        iPlanTransLogDao.update(iPlanTransLog);
                    } else if ("FAIL".equals(record.getStatus())) {
                        transLog.setStatus(BaseResponse.STATUS_FAILED);
                        transLogDao.update(transLog);
                        RequestSingleTrans lastRequest = JSON.parseObject(transLog.getRequestPacket(), RequestSingleTrans.class);
                        lastRequest.setRequestNo(IdUtil.getRequestNo());
                        iPlanTransLog.setExtSn(lastRequest.getRequestNo());
                        iPlanTransLogDao.update(iPlanTransLog);
                        logger.info("{}正在发起定期普通转入重试,重试报文为:{}", lastRequest.getRequestNo(), JSON.toJSONString(lastRequest));
                        BaseResponse retryResponse = transactionService.singleTrans(lastRequest);
                        logger.info("{}的发起的定期普通转入重试结果为：{}", lastRequest.getRequestNo(), JSON.toJSONString(retryResponse));
                        if (BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())) {
                            iPlanAccount.setCurrentPrincipal(iPlanAccount.getCurrentPrincipal() + amtToInvest);
                            iPlanAccount.setInitPrincipal(iPlanAccount.getInitPrincipal() + amtToInvest);
                            iPlanAccount.setAmtToInvest(iPlanAccount.getAmtToInvest() + amtToInvest);
                            iPlanAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
                            iPlanAccountDao.update(iPlanAccount);
                            iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                            iPlanTransLogDao.update(iPlanTransLog);
                        }
                    } else {
                        //处理中的情况暂时不做处理
                    }
                } else if (BaseResponse.STATUS_FAILED.equals(queryResponse.getStatus())) {
                    //如果是因为没有请求到厦门银行而导致的失败
                    if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(queryResponse.getCode())) {
                        transLog.setStatus(BaseResponse.STATUS_FAILED);
                        transLogDao.update(transLog);
                        RequestSingleTrans lastRequest = JSON.parseObject(transLog.getRequestPacket(), RequestSingleTrans.class);
                        logger.info("{}正在发起定期普通转入重试,重试报文为:{}", transLog.getTxnSn(), JSON.toJSONString(lastRequest));
                        BaseResponse retryResponse = transactionService.singleTrans(lastRequest);
                        logger.info("{}的发起的定期普通转入重试结果为：{}", transLog.getTxnSn(), JSON.toJSONString(retryResponse));
                        if (BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())) {
                            iPlanAccount.setCurrentPrincipal(iPlanAccount.getCurrentPrincipal() + amtToInvest);
                            iPlanAccount.setInitPrincipal(iPlanAccount.getInitPrincipal() + amtToInvest);
                            iPlanAccount.setAmtToInvest(iPlanAccount.getAmtToInvest() + amtToInvest);
                            iPlanAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
                            this.calcInterest(iPlanAccount);
                            iPlanAccountDao.update(iPlanAccount);
                            iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                            iPlanTransLogDao.update(iPlanTransLog);
                        }
                    } else {
                        logger.error("转入失败->厦门银行返回:{},活期账户(iPlanAccount)信息:{},转入交易(iPlanTransLog)信息:{},TransLog信息:{}",
                                JSON.toJSONString(queryResponse), JSON.toJSONString(iPlanAccount), JSON.toJSONString(iPlanTransLog), JSON.toJSONString(transLog));
                    }
                }
            }
        }
    }

    private RequestSingleTransQuery constructRequest(String requestNo, TransactionType transactionType) {
        RequestSingleTransQuery request = new RequestSingleTransQuery();
        request.setTransactionType(transactionType);
        request.setRequestNo(requestNo);
        return request;
    }

    private void calcInterest(IPlanAccount iPlanAccount) {
        IPlan iPlan = iPlanDao.findById(iPlanAccount.getIplanId());
        Integer amount = iPlanAccount.getCurrentPrincipal();
        double fixRate = iPlan.getFixRate() != null ? iPlan.getFixRate().doubleValue() : 0;
        double bonusRate = iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue() : 0;
        if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())) {
            double expectedInterest = subjectService.getInterestByRepayType(amount, BigDecimal.valueOf(fixRate), new BigDecimal("0.144"),
                    iPlan.getTerm(), iPlan.getTerm() * 30, iPlan.getRepayType());
            iPlanAccount.setExpectedInterest((int) (expectedInterest * 100));
            iPlanAccount.setIplanExpectedBonusInterest(0);
            if (bonusRate > 0) {
                double expectedBonusInterest = subjectService.getInterestByRepayType(amount, BigDecimal.valueOf(bonusRate), new BigDecimal("0.144"),
                        iPlan.getTerm(), iPlan.getTerm() * 30, iPlan.getRepayType());
                iPlanAccount.setIplanExpectedBonusInterest((int) (expectedBonusInterest * 100));
            }
        } else {
            int interestAccrualType = iPlan.getInterestAccrualType();
            iPlanAccount.setExpectedInterest((int)iPlanAccountService.calInterest(interestAccrualType, amount, fixRate, iPlan));
            iPlanAccount.setIplanExpectedBonusInterest((int)iPlanAccountService.calInterest(interestAccrualType, amount, bonusRate, iPlan));
        }
    }
}
