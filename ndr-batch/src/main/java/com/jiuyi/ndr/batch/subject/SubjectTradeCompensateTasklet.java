package com.jiuyi.ndr.batch.subject;
import com.alibaba.fastjson.JSON;
import com.duanrong.util.InterestUtil;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.subject.SubjectAccountDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.subject.SubjectTransLogDao;
import com.jiuyi.ndr.dao.user.RedPacketDao;
import com.jiuyi.ndr.dao.xm.TransLogDao;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectAccount;
import com.jiuyi.ndr.domain.subject.SubjectTransLog;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.domain.xm.TransLog;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.redpacket.RedpacketService;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.request.RequestUserAutoPreTransaction;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.query.TransactionQueryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Yu on 2017/10/31.
 */
@Service
public class SubjectTradeCompensateTasklet implements Tasklet {
    private static final Logger logger = LoggerFactory.getLogger(SubjectTradeCompensateTasklet.class);

    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private TransLogDao transLogDao;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private SubjectAccountDao subjectAccountDao;
    @Autowired
    private SubjectTransLogDao subjectTransLogDao;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private RedPacketDao redPacketDao;
    @Autowired
    private RedpacketService redpacketService;
    @Autowired
    private SubjectService subjectService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        //购买交易补偿
        transferInCompensate();
        return RepeatStatus.FINISHED;
    }

    /**
     * 购买补偿(散标+债权)
     */
    public void transferInCompensate(){
        //查询所有交易待处理中的记录
        List<SubjectTransLog> subjectTransLogs = subjectTransLogDao.findPendingTransLog();
        logger.info("共有{}条转入记录状态未知 需要进行补偿:", subjectTransLogs.size());
        for (SubjectTransLog subjectTransLog : subjectTransLogs) {
            {
                logger.info("交易记录{}正在进行散标转入补偿",subjectTransLog.getExtSn());
                List<TransLog> transLogs = transLogDao.findByTxnSnAndTransCode(subjectTransLog.getExtSn(), TransCode.SUBJECT_INVEST_FREEZE.getCode());
                //判断购买的记录是散标还是债权
                if(subjectTransLog.getTarget().equals(SubjectTransLog.TARGET_CREDIT)) {
                    transLogs =transLogDao.findByTxnSnAndTransCode(subjectTransLog.getExtSn(), TransCode.CREDIT_INVEST_FREEZE.getCode());
                }
                //获取最新的一条投资记录
                TransLog transLog = transLogs.get(transLogs.size()-1);
                 //根据活期交易记录查询到对应的活期账户
                SubjectAccount subjectAccount= subjectAccountService.findAccountById(subjectTransLog.getAccountId());
                int amtToInvest = subjectTransLog.getTransAmt();
                Subject subject = subjectDao.findBySubjectIdForUpdate(subjectTransLog.getSubjectId());
                RequestSingleTransQuery queryRequest = new RequestSingleTransQuery();
                queryRequest.setRequestNo(transLog.getTxnSn());
                queryRequest.setTransactionType(TransactionType.TRANSACTION);
                ResponseSingleTransQuery queryResponse = transactionService.singleTransQuery(queryRequest);
                if (BaseResponse.STATUS_SUCCEED.equals(queryResponse.getStatus())) {
                    TransactionQueryRecord record = (TransactionQueryRecord) queryResponse.getRecords().get(0);
                    if ("SUCCESS".equals(record.getStatus())){
                        transLog.setStatus(BaseResponse.STATUS_SUCCEED);
                        transLogDao.update(transLog);
                        this.updateSubjectAccount(subjectAccount,subject,subjectTransLog,amtToInvest,subjectTransLog.getTarget());
                        subjectTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                        subjectTransLogDao.update(subjectTransLog);
                    }else if("FAIL".equals(record.getStatus())){
                        transLog.setStatus(BaseResponse.STATUS_FAILED);
                        transLogDao.update(transLog);
                        //用户预处理接口
                        RequestUserAutoPreTransaction lastRequest = JSON.parseObject(transLog.getRequestPacket(), RequestUserAutoPreTransaction.class);
                        lastRequest.setRequestNo(IdUtil.getRequestNo());
                        subjectTransLog.setExtSn(lastRequest.getRequestNo());
                        subjectTransLogDao.update(subjectTransLog);
                        subjectAccount.setInvestRequestNo(lastRequest.getRequestNo());
                        logger.info("{}正在发起定期普通转入重试,重试报文为:{}",lastRequest.getRequestNo(),JSON.toJSONString(lastRequest));
                        BaseResponse retryResponse = transactionService.userAutoPreTransaction(lastRequest);
                        logger.info("{}的发起的定期普通转入重试结果为：{}",lastRequest.getRequestNo(),JSON.toJSONString(retryResponse));
                        if (BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())) {
                            this.updateSubjectAccount(subjectAccount,subject,subjectTransLog,amtToInvest,subjectTransLog.getTarget());
                            subjectTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                            subjectTransLogDao.update(subjectTransLog);
                        }
                    }else{
                        //处理中的情况暂时不做处理
                    }
                }else if(BaseResponse.STATUS_FAILED.equals(queryResponse.getStatus())){
                    //如果是因为没有请求到厦门银行而导致的失败
                    if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(queryResponse.getCode())) {
                        transLog.setStatus(BaseResponse.STATUS_FAILED);
                        transLogDao.update(transLog);
                        //用户预处理接口
                        RequestUserAutoPreTransaction lastRequest = JSON.parseObject(transLog.getRequestPacket(), RequestUserAutoPreTransaction.class);
                        logger.info("{}正在发起定期普通转入重试,重试报文为:{}",transLog.getTxnSn(),JSON.toJSONString(lastRequest));
                        BaseResponse retryResponse = transactionService.userAutoPreTransaction(lastRequest);
                        logger.info("{}的发起的定期普通转入重试结果为：{}",transLog.getTxnSn(),JSON.toJSONString(retryResponse));
                        if (BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())) {
                            this.updateSubjectAccount(subjectAccount,subject,subjectTransLog,amtToInvest,subjectTransLog.getTarget());
                            subjectTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                            subjectTransLogDao.update(subjectTransLog);
                        }
                    } else {
                        logger.error("转入失败->厦门银行返回:{},散标账户(subjectAccount)信息:{},转入交易(subjectTransLog)信息:{},TransLog信息:{}",
                                JSON.toJSONString(queryResponse), JSON.toJSONString(subjectAccount), JSON.toJSONString(subjectTransLog), JSON.toJSONString(transLog));
                    }
                }
                }

        }

    }

    /**
     * 更新账户
     * @param subjectAccount
     * @param subject
     * @param subjectTransLog
     * @param amtToInvest
     */
    private void updateSubjectAccount(SubjectAccount subjectAccount,Subject subject,SubjectTransLog subjectTransLog,int amtToInvest,int target){
        subjectAccount.setInitPrincipal(amtToInvest);
        subjectAccount.setCurrentPrincipal(amtToInvest);
        String repayType=subjectService.getRepayType(subject);
        subjectAccount.setSubjectExpectedBonusInterest(0);
        if(target==SubjectTransLog.TARGET_SUBJECT) {
            double investRate = subject.getInvestRate() != null ? subject.getInvestRate().doubleValue() : 0;
            double bonusRate = subject.getBonusRate() != null ? subject.getBonusRate().doubleValue() : 0;
            double expectedInterest=subjectService.getInterestByRepayType(amtToInvest, BigDecimal.valueOf(investRate),subject.getRate(),
                    subject.getTerm(),subject.getPeriod(),subject.getRepayType());
            subjectAccount.setExpectedInterest((int)(expectedInterest*100));
            if(bonusRate>0) {
                double expectedBonusInterest=subjectService.getInterestByRepayType(amtToInvest,BigDecimal.valueOf(bonusRate),subject.getRate(),
                        subject.getTerm(),subject.getPeriod(),subject.getRepayType());
                subjectAccount.setSubjectExpectedBonusInterest((int)(expectedBonusInterest*100));

            }
            RedPacket redPacket = null;
            int redPacketId = subjectTransLog.getRedPacketId() != null ? subjectTransLog.getRedPacketId() : 0;
            if (redPacketId > 0) {
                redPacket = redPacketDao.getRedPacketById(redPacketId);
            }
            if (redPacketId > 0 && redPacket != null) {
                if (org.apache.commons.lang3.StringUtils.equals(redPacket.getType(), RedPacket.TYPE_DEDUCT)) {
                    subjectAccount.setDedutionAmt((int) (redPacket.getMoney() * 100));
                    subjectAccount.setExpectedReward((int) (redPacket.getMoney() * 100));
                    subjectAccount.setTotalReward((int) (redPacket.getMoney() * 100));
                } else {
                    double redPacketMoney = redpacketService.getRedpacketMoneyCommon(redPacket,subject,amtToInvest / 100.0);
                    subjectAccount.setExpectedReward((int) (redPacketMoney * 100));
                    subjectAccount.setTotalReward((int) (redPacketMoney * 100));
                }
            }
        }else if(target==SubjectTransLog.TARGET_CREDIT){
            int residualTerm = subject.getTerm() - subject.getCurrentTerm() + 1;
            double investRate = subject.getInvestRate() != null ? subject.getInvestRate().doubleValue() : 0;
            double bonusRate = subject.getBonusRate() != null ? subject.getBonusRate().doubleValue() : 0;
            double expectedInterest=subjectService.getInterestByRepayType(amtToInvest, BigDecimal.valueOf(investRate),subject.getRate(),
                    residualTerm,subject.getPeriod(),subject.getRepayType());
            subjectAccount.setExpectedInterest((int)(expectedInterest*100));
            if(bonusRate>0) {
                double expectedBonusInterest=subjectService.getInterestByRepayType(amtToInvest,BigDecimal.valueOf(bonusRate),subject.getRate(),
                        residualTerm,subject.getPeriod(),subject.getRepayType());
                subjectAccount.setSubjectExpectedBonusInterest((int)(expectedBonusInterest*100));
            }
        }
        subjectAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectAccountDao.update(subjectAccount);

    }
}
