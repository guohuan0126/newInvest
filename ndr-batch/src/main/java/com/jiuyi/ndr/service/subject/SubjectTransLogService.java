package com.jiuyi.ndr.service.subject;

import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.subject.*;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.subject.*;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.service.account.PlatformAccountService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.iplan.IPlanTransLogService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.notice.support.TemplateId;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestCancelPreTransactionNew;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.query.TransactionQueryRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by mayongbo on 2017/10/25.
 */

@Service
public class SubjectTransLogService {

    private static final Logger logger = LoggerFactory.getLogger(SubjectTransLogService.class);

    @Autowired
    private SubjectTransLogDao subjectTransLogDao;

    @Autowired
    private CreditOpeningDao creditOpeningDao;

    @Autowired
    private CreditDao creditDao;

    @Autowired
    private SubjectAccountDao subjectAccountDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private PlatformAccountService platformAccountService;

    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private SubjectTransferParamDao subjectTransferParamDao;
    @Autowired
    private UserService userService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private IPlanTransLogService iPlanTransLogService;

    /**
     * 散标债权转让处理
     * @param subjectTransLogId
     */
    public SubjectTransLog exitSubject(Integer subjectTransLogId) {
        SubjectTransLog subjectTransLog = subjectTransLogDao.findByIdForUpdate(subjectTransLogId);
        //查询债权转出记录对应的creditopening
        CreditOpening creditOpening = creditOpeningDao.findByOpenChannelIdAndNotStatus(subjectTransLogId, CreditOpening.OPEN_CHANNEL,CreditOpening.STATUS_LENDED);
        if(creditOpening == null){
            logger.info("转出记录 {} 用户Id- {} 没有要处理的债权,跳过...", subjectTransLog.getId(), subjectTransLog.getUserId());
            return subjectTransLog;
        }
        if ((creditOpening != null && creditOpening.getAvailablePrincipal()!=0)) {
            logger.info("转出记录 {} 用户Id- {} 还有剩余金额,跳过...", subjectTransLog.getId(), subjectTransLog.getUserId());
            return subjectTransLog;
        }
        //查询该creditopening下对应的债权
        Integer totalAmt = 0; //开放债权对应债权放款总金额
        BigDecimal totalBuyAmt = BigDecimal.ZERO;//购买人实际出钱总额
        List<Credit> credits = creditDao.findByTargetIdAndTarget(creditOpening.getId(), Credit.TARGET_CREDIT);
        if (credits != null && credits.size() ==0) {
            logger.info("转出记录 {} 用户Id- {} 被购买的债权还未形成...", subjectTransLog.getId(), subjectTransLog.getUserId());
            return subjectTransLog;
        }
        if (credits.stream().allMatch(credit -> credit.getCreditStatus() == Credit.CREDIT_STATUS_HOLDING)) {
            for (Credit credit : credits) {
                totalAmt += credit.getHoldingPrincipal();
                if(creditOpening.getTransferDiscount().compareTo(BigDecimal.ONE) != 0){
                    totalBuyAmt = totalBuyAmt.add(new BigDecimal(ArithUtil.round((credit.getHoldingPrincipal() /100.0) * (creditOpening.getTransferDiscount().doubleValue()) ,2)+""));
                }
            }
            if (SubjectTransLog.TRANS_STATUS_TO_CANCEL.equals(subjectTransLog.getTransType()) && creditOpening.getTransferPrincipal() != (subjectTransLog.getTransAmt() - subjectTransLog.getProcessedAmt() + totalAmt)) {
                logger.info("债权部分撤销转出记录 {} 用户Id- {} 已放款的债权总额不等于已购买的债权总额", subjectTransLog.getId(), subjectTransLog.getUserId());
                return subjectTransLog;
            }else if(SubjectTransLog.TRANS_TYPE_CREDIT_TRANSFER.equals(subjectTransLog.getTransType()) && !creditOpening.getTransferPrincipal().equals(totalAmt)){
                logger.info("债权全部被购买转出记录 {} 用户Id- {} 已放款的债权总额不等于已购买的债权总额", subjectTransLog.getId(), subjectTransLog.getUserId());
                return subjectTransLog;
            }
        } else {
            logger.warn("转出记录 {},开放中的债权被购买的还没有全部放款", subjectTransLog.getId());
            return subjectTransLog;
        }
        //通过creditOpening中的原credit_id查询出对应的债权,从而查询出散标账户subject_account
        Credit oldCredit = creditDao.findById(creditOpening.getCreditId());

        //转让记录对应的账户
        SubjectAccount subjectAccount = subjectAccountDao.findByIdForUpdate(subjectTransLog.getAccountId());

        //标的
        Subject subject = subjectDao.findBySubjectId(subjectTransLog.getSubjectId());

        //散标交易配置信息
        SubjectTransferParam subjectTransferParam = subjectTransferParamDao.findByTransferParamCode(subject.getTransferParamCode());

        //计算转让服务费
        Double fee = 0.0;
        if(subjectTransLog.getTransFee() == 0){
            Double feeRate = subjectAccountService.calcTransFeeNew(subjectAccount.getTransLogId(), subject, subjectTransferParam);
            fee = (totalAmt /100.0) * (feeRate / 100.0);
        }
        //计算溢价手续费
        Double overPriceFee = 0.0;
        Double transferDiscount = creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue();
        if (creditOpening.getTransferDiscount().compareTo(new BigDecimal(1)) == 1){
            overPriceFee = (totalAmt /100.0) * (transferDiscount - 100) / 100.0 * 0.2;
        }

        Double returnReward = 0.0;//要回收的红包奖励
        if(!iPlanTransLogService.isNewFixIplan(subjectTransferParam)){
            if (subjectAccount.getTotalReward() > 0){
                //转让记录对应的标的
                 if (Credit.TARGET_SUBJECT == oldCredit.getTarget()){ //只有投资标的,才有红包奖励和抵扣券
                     Subject oldSubject = subjectDao.findById(oldCredit.getTargetId());
                     returnReward = subjectAccount.getTotalReward() *(oldSubject.getTerm() - oldSubject.getCurrentTerm() + 1) / oldSubject.getTerm() * (totalAmt /100.0) / (oldCredit.getInitPrincipal());
                }else{
                     Subject oldSubject = subjectDao.findBySubjectId(oldCredit.getSubjectId());
                     //购买时间
                     String startTime = oldCredit.getCreateTime().substring(0, 10).replace("-", "");
                     //购买时所在期数
                     Integer startTerm = subjectRepayScheduleService.getTermRepaySchedule(subject.getSubjectId(), startTime);
                     returnReward = subjectAccount.getTotalReward() * (oldSubject.getTerm() - oldSubject.getCurrentTerm() + 1) / (oldSubject.getTerm() - startTerm + 1) * (totalAmt /100.0) / (oldCredit.getInitPrincipal() / 1.0);
                 }
            }
        }
        //转让人账户上的冻结金额
        Double totalFreeze = 0.0;

        //要扣除的费用总和
        Double totalCommission = 0.0;

        if (BaseResponse.STATUS_PENDING.equals(subjectTransLog.getExtStatus())) {
            RequestSingleTransQuery request = new RequestSingleTransQuery();
            request.setRequestNo(subjectTransLog.getExtSn());
            request.setTransactionType(TransactionType.TRANSACTION);
            ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
            if (!"0".equals(responseQuery.getCode())) {
                if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(responseQuery.getCode())) {
                    //订单不存在，设置交易失败，重新发起交易
                    subjectTransLog.setExtStatus(BaseResponse.STATUS_FAILED);
                    subjectTransLogDao.update(subjectTransLog);
                }
                return subjectTransLog;
            } else {
                TransactionQueryRecord transactionQueryRecord = (TransactionQueryRecord) responseQuery.getRecords().get(0);
                if ("SUCCESS".equals(transactionQueryRecord.getStatus())) {
                    subjectTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                } else if ("PROCESSING".equals(transactionQueryRecord.getStatus())) {
                    subjectTransLog.setExtStatus(BaseResponse.STATUS_PENDING);
                    subjectTransLogDao.update(subjectTransLog);
                    return subjectTransLog;
                }else {
                    subjectTransLog.setExtSn(subjectTransLog.getExtSn());
                    subjectTransLog.setExtStatus(BaseResponse.STATUS_FAILED);
                    subjectTransLogDao.update(subjectTransLog);
                    return subjectTransLog;
                }
            }
        } else {
            totalFreeze = ArithUtil.round((totalAmt /100.0) * (creditOpening.getTransferDiscount().doubleValue()) ,2);
            if(creditOpening.getTransferDiscount().compareTo(BigDecimal.ONE) != 0){
                if(totalFreeze > totalBuyAmt.doubleValue()){
                    totalFreeze = totalBuyAmt.doubleValue();
                }
            }

            totalCommission = ArithUtil.round(returnReward,2) + ArithUtil.round(fee,2) + ArithUtil.round(overPriceFee,2);
            totalCommission = ArithUtil.round(totalCommission,2);
            if("jMVfayj22m22oqah".equals(subjectTransLog.getUserId())){
                totalCommission = 0.0;
                returnReward = 0.0;
                fee = 0.0;
                overPriceFee = 0.0;
            }
            BaseResponse response = this.cancelPreTransaction(creditOpening.getTransferorId(),GlobalConfig.MARKETING_SYS_DR,subject.getSubjectId(),subjectAccount.getInvestRequestNo(),totalFreeze,totalCommission);
            if (BaseResponse.STATUS_FAILED.equals(response.getStatus())) {//交易失败
                subjectTransLog.setExtSn(response.getRequestNo());
                subjectTransLog.setExtStatus(response.getStatus());
                subjectTransLogDao.update(subjectTransLog);
                return subjectTransLog;
            }
            if (BaseResponse.STATUS_PENDING.equals(response.getStatus())) {//处理中
                subjectTransLog.setExtSn(response.getRequestNo());
                subjectTransLog.setExtStatus(response.getStatus());
                subjectTransLogDao.update(subjectTransLog);
                return subjectTransLog;
            }
            subjectTransLog.setExtSn(response.getRequestNo());
            subjectTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
        }
            if(SubjectTransLog.TRANS_TYPE_CREDIT_TRANSFER.equals(subjectTransLog.getTransType())){
                subjectTransLog.setProcessedAmt(subjectTransLog.getProcessedAmt() + totalAmt);
            }
            subjectTransLog.setActualPrincipal((int) ArithUtil.round((totalFreeze * 100 - totalCommission * 100),2));

            //更新账户信息
            subjectAccount.setAmtToTransfer(subjectAccount.getAmtToTransfer() - totalAmt);
            subjectAccount.setPaidReward(subjectAccount.getPaidReward() - BigDecimal.valueOf(returnReward * 100).intValue());
            subjectAccountDao.update(subjectAccount);


        //更新本地translog和creditOpening
        subjectTransLog.setTransStatus(SubjectTransLog.TRANS_STATUS_SUCCEED);
        subjectTransLogDao.update(subjectTransLog);
        userAccountService.unfreezeForShow(creditOpening.getTransferorId(),totalFreeze,BusinessEnum.ndr_credit_return,subject.getName()+"债权转让到账",subject.getName()+"债权转让到账"+totalFreeze+"元"+"本金"+totalAmt/100.0+"元",subjectTransLog.getExtSn());
        //用户流水出账
        //扣除奖励
        if (ArithUtil.round(returnReward,2) > 0){
            userAccountService.transferOut(creditOpening.getTransferorId(),ArithUtil.round(returnReward,2),BusinessEnum.ndr_credit_reward,subject.getName()+"债权转让扣除奖励",subject.getName()+"债权转让扣除奖励"+ArithUtil.round(returnReward,2)+"元",subjectTransLog.getExtSn(),1);
        }
        //扣除手续费
        if(fee > 0.0){
            userAccountService.transferOut(creditOpening.getTransferorId(),totalCommission - ArithUtil.round(overPriceFee,2) - ArithUtil.round(returnReward,2),BusinessEnum.ndr_credit_fee,subject.getName()+"债权转让扣除手续费",subject.getName()+"债权转让扣除手续费"+ArithUtil.round(fee,2)+"元",subjectTransLog.getExtSn(),1);
        }

        if(ArithUtil.round(overPriceFee,2) > 0){
            userAccountService.transferOut(creditOpening.getTransferorId(),ArithUtil.round(overPriceFee,2),BusinessEnum.ndr_credit_over_price,subject.getName()+"债权转让扣除溢价手续费",subject.getName()+"债权转让扣除溢价手续费"+ArithUtil.round(overPriceFee.doubleValue(),2)+"元",subjectTransLog.getExtSn(),1);
        }
        //平台自有账户佣金流水
        if(totalCommission >= 0.01){
            platformAccountService.transferIn(GlobalConfig.MARKETING_SYS_DR, totalCommission, BusinessEnum.ndr_commission, oldCredit.getUserId()+"债权转让,佣金收取："+totalCommission+"元",subjectTransLog.getExtSn(),subject.getSubjectId(),null);
        }
        User user = userService.getUserById(creditOpening.getTransferorId());
        if(CreditOpening.STATUS_CANCEL_PENDING.equals(creditOpening.getStatus())){
            creditOpening.setStatus(CreditOpening.STATUS_CANCEL);
            creditOpeningDao.update(creditOpening);
            //发送短信
            Integer amount = creditOpening.getTransferPrincipal();
            Double transAmt = ArithUtil.round((amount /100.0) * (creditOpening.getTransferDiscount().doubleValue()) ,2);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = now.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = now.withHour(7).withMinute(0).withSecond(0);
            if(now.isAfter(start) && now.isBefore(end)){
                String msg = subject.getName()+","+String.valueOf(creditOpening.getTransferPrincipal()/100.0)+","
                            +String.valueOf(transAmt)+","+String.valueOf(totalAmt/100.0)+","
                            +String.valueOf(totalCommission)+","+String.valueOf(ArithUtil.round(totalFreeze -totalCommission,2))+","
                            +String.valueOf((amount - totalAmt)/100.0);
                subjectService.insertMsg(creditOpening.getTransferorId(),msg,user.getMobileNumber(),TemplateId.CREDIT_TRANSFER_PART);
            }else{
                try {
                    String smsTemplate = TemplateId.CREDIT_TRANSFER_PART;
                    noticeService.send(user.getMobileNumber(), subject.getName()+","
                            + String.valueOf(amount/100.0)+","+String.valueOf(transAmt)+","+String.valueOf(totalAmt/100.0)+","+String.valueOf(totalCommission)+","+String.valueOf(ArithUtil.round(totalFreeze -totalCommission,2))+","+String.valueOf((amount - totalAmt)/100.0), smsTemplate);
                } catch (Exception e) {
                    logger.error("债权部分短信发送失败",user.getMobileNumber()+"开放债权id："+creditOpening.getId()+"成交金额："+String.valueOf(totalAmt/100.0));
                }
                return subjectTransLog;
            }
        }else {
            creditOpening.setStatus(CreditOpening.STATUS_LENDED);
        }
        creditOpeningDao.update(creditOpening);
        try {
            String smsTemplate = TemplateId.CREDIT_TRANSFER_DEAL_OUT;
            noticeService.send(user.getMobileNumber(), subject.getName()+","
                    + String.valueOf(totalAmt/100.0)+","+String.valueOf(totalFreeze)+","+String.valueOf(totalCommission)+","+String.valueOf(ArithUtil.round(totalFreeze -totalCommission,2)), smsTemplate);
        } catch (Exception e) {
            logger.error("债权全部成交短信发送失败",user.getMobileNumber()+"开放债权id："+creditOpening.getId()+"成交金额："+String.valueOf(totalAmt/100.0));
        }

        return subjectTransLog;
    }

    //请求厦门银行,解冻金额
    public BaseResponse cancelPreTransaction(String sourcePlatformUserNo,String targetPlatformUserNo,String subjectId,String requestNo,Double amount,Double totalCommission){
        RequestCancelPreTransactionNew request = new RequestCancelPreTransactionNew();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setPreTransactionNo(requestNo);
        request.setAmount(amount);
        if (totalCommission >= 0.01){
        request.setCommission(totalCommission);
        }
        //调用厦门预处理取消接口
        BaseResponse baseResponse = null;
        try {
            logger.info("开始调用厦门银行预处理取消接口->{}", JSON.toJSONString(request));
            baseResponse = transactionService.cancelPreTransaction(sourcePlatformUserNo,targetPlatformUserNo,subjectId,request);
            logger.info("预处理取消接口返回->{}", JSON.toJSONString(baseResponse));
        } catch (Exception e) {
            if (baseResponse == null) {
                baseResponse = new BaseResponse();
            }
            baseResponse.setStatus(BaseResponse.STATUS_PENDING);
        }
        return baseResponse;
    }

}
