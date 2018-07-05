package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.dao.user.UserDao;
import com.jiuyi.ndr.domain.account.CompensatoryAcctLog;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepayDetail;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.service.account.CompensatoryAcctLogService;
import com.jiuyi.ndr.service.account.PlatformAccountService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.notice.support.TemplateId;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.util.ArithUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

public class SubjectRepayWriterStep2 implements ItemWriter<SubjectRepayDetail> {

    private static final Logger logger = LoggerFactory.getLogger(SubjectRepayWriterStep2.class);

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private PlatformAccountService platformAccountService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private SubjectRepayDetailDao subjectRepayDetailDao;
    @Autowired
    private CompensatoryAcctLogService compensatoryAcctLogService;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;


    @Override
    public void write(List<? extends SubjectRepayDetail> subjectRepayDetails) throws Exception {
        logger.info("begin update investor local data for repay");
        for (SubjectRepayDetail subjectRepayDetail : subjectRepayDetails) {
            this.updateLocalData(subjectRepayDetail);
        }
        logger.info("end update investor local data for repay");
    }

    private void updateLocalData(SubjectRepayDetail subjectRepayDetail) {

        Subject subject = subjectService.findBySubjectId(subjectRepayDetail.getSubjectId());
        double investorPrincipal = subjectRepayDetail.getPrincipal() / 100.0;
        double principalInterestPenaltyFee = (subjectRepayDetail.getPrincipal() + subjectRepayDetail.getInterest() + subjectRepayDetail.getPenalty() + subjectRepayDetail.getFee()) / 100.0;
        double interest = (subjectRepayDetail.getInterest() + subjectRepayDetail.getPenalty() + subjectRepayDetail.getFee()) / 100.0;
        double freezeAmount = (subjectRepayDetail.getFreezePrincipal() + subjectRepayDetail.getFreezeInterest() + subjectRepayDetail.getFreezePenalty() + subjectRepayDetail.getFreezeFee()) / 100.0;
        double commission = subjectRepayDetail.getCommission() / 100.0;
        double profit = (subjectRepayDetail.getProfit() + subjectRepayDetail.getDeptPenalty()) / 100.0;
        String investorIdXM = subjectRepayDetail.getUserId();
        String requestNo = subjectRepayDetail.getExtSn();

        if(subjectRepayDetail.getChannel().equals(Credit.SOURCE_CHANNEL_LPLAN)){
            commission +=investorPrincipal + interest;
        }

        if (commission > 0) {
            //收益账户短融本地收取佣金
            platformAccountService.transferIn(GlobalConfig.MARKETING_SYS_DR, commission,
                    SubjectRepayDetail.SOURCE_BRW.equals(subjectRepayDetail.getSourceType())?BusinessEnum.ndr_commission : BusinessEnum.ndr_cps_commission,
                    "标的还款佣金收取：标的名称：" + subject.getName(), requestNo,subject.getSubjectId(),subjectRepayDetail.getScheduleId());
            logger.info("local collect commission [{}] because subject repay", commission);
        }

        if (profit > 0) {
            userAccountService.transferIn(subject.getProfitAccount(), profit, BusinessEnum.ndr_subject_repay_profit, "标的还款分润收取：标的名称：" + subject.getName(), "标的还款分润收取，金额：" + profit , requestNo,subject.getSubjectId(),subjectRepayDetail.getScheduleId());
            if(SubjectRepayDetail.SOURCE_CPS.equals(subjectRepayDetail.getSourceType())){
                compensatoryAcctLogService.updateProfit(subjectRepayDetail.getProfit() + subjectRepayDetail.getDeptPenalty(),subjectRepayDetail.getScheduleId(), CompensatoryAcctLog.TYPE_CPS_OUT);
            }
            logger.info("local account : [{}] collect profit : [{}] because subject repay", subject.getProfitAccount(), profit);
        }
        //是否新的直贷二模式
        boolean newFlag = Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag())&& subject.getInvestRate()!=null && subject.getRate().equals(subject.getInvestRate()) && subject.getRate().compareTo(BigDecimal.valueOf(0.144))<0 && subject.getRate().compareTo(BigDecimal.valueOf(0.068))>0;
        //直贷一或是直贷二旧模式,投资人收取佣金,入一笔,出一笔
        if(!newFlag && commission>0){
            userAccountService.transferIn(investorIdXM, commission, BusinessEnum.ndr_commission, "标的还款超出发行利率部分的利息：标的编号:" + subject.getSubjectId(), "标的还款超出发行利率部分的利息：标的ID：" + subject.getSubjectId() + "，金额：" + commission, requestNo,subject.getSubjectId(),subjectRepayDetail.getScheduleId(),0);
            userAccountService.transferOutForNotShow(investorIdXM,commission,BusinessEnum.ndr_commission,"标的还款向出借人收取居间服务费：标的编号:"+subject.getSubjectId(),"标的还款向出借人收取居间服务费：标的ID:"+subject.getSubjectId()+",金额"+commission,requestNo);
        }
        if (subjectRepayDetail.getChannel().equals(Credit.SOURCE_CHANNEL_SUBJECT)) {//散标或债权
            if (investorPrincipal + interest > 0) {
                //投资人短融本地账户收益
                subjectAccountService.updateAccountForRepay(subjectRepayDetail);
                userAccountService.transferIn(investorIdXM, principalInterestPenaltyFee, BusinessEnum.ndr_repay, "项目还款：" + subject.getName(), "标的回款，标的ID：" + subject.getSubjectId() + "，本金：" + investorPrincipal + "，利息："+interest, requestNo,subject.getSubjectId(),subjectRepayDetail.getScheduleId());
                logger.info("subject [{}] local take back principal [{}], interest [{}] because subject repay", investorIdXM, investorPrincipal, interest);
                try{
                    User user = userDao.getUserById(subjectRepayDetail.getUserId());
                    UserAccount account = userAccountService.findUserAccount(subjectRepayDetail.getUserId());
                    String msg = subject.getName()+","+String.valueOf(ArithUtil.round(investorPrincipal + interest,2))+","+String.valueOf(account.getAvailableBalance());
                    subjectService.insertMsg(subjectRepayDetail.getUserId(),msg,user.getMobileNumber(),TemplateId.SUBJECT_REPAY_SMS);
                }catch(Exception e){
                    logger.error("插入短信记录异常,detailID-{}",subjectRepayDetail.getId());
                }
            }
        } else if (subjectRepayDetail.getChannel().equals(Credit.SOURCE_CHANNEL_IPLAN)) {//定期
            if (investorPrincipal + interest > 0) {
                //投资人短融本地账户收益
                iPlanAccountService.updateAccountForRepay(subjectRepayDetail);
                userAccountService.transferInForNotShow(investorIdXM, principalInterestPenaltyFee, BusinessEnum.ndr_repay, "理财计划标的本息回款：" + subject.getName(), "标的回款，标的ID：" + subject.getSubjectId() + "，本金：" + investorPrincipal + "，利息："+interest, requestNo);
                logger.info("iplan [{}] local take back principal [{}], interest [{}] because subject repay", investorIdXM, investorPrincipal, interest);
            }

            //投资人短融本地账户冻结
            if (freezeAmount > 0) {
                userAccountService.freezeForNotShow(investorIdXM, freezeAmount, BusinessEnum.ndr_iplan_invest, "理财计划标的回款冻结：" + subject.getName(), "回款复投冻结，标的ID：" + subject.getSubjectId(), requestNo);
                logger.info("iplan [{}] local freeze amount [{}] because subject repay ", investorIdXM, freezeAmount);
            }
        }/* else if(subjectRepayDetail.getChannel().equals(Credit.SOURCE_CHANNEL_LPLAN)){//活期
            if (investorPrincipal + interest > 0) {
                //投资人短融本地账户收益
                userAccountService.transferInForNotShow(investorIdXM, principalInterestPenaltyFee, BusinessEnum.ndr_repay, "天天赚标的本息回款：" + subject.getName(), "标的回款，标的ID：" + subject.getSubjectId() + "，本金：" + investorPrincipal + "，利息：" + interest, requestNo);
                lPlanAccountService.updateAccountForRepay(subjectRepayDetail);
                logger.info("lplan [{}] local take back principal [{}], interest [{}] because subject repay", investorIdXM, investorPrincipal, interest);
            }

            //投资人短融本地账户冻结
            if (freezeAmount > 0) {//冻结本地金额
                userAccountService.freezeForNotShow(investorIdXM, freezeAmount, BusinessEnum.ndr_ttz_invest, "天天赚标的回款冻结："+ subject.getName(), "回款复投冻结，标的ID：" + subject.getSubjectId(), requestNo);
                logger.info("lplan [{}] local freeze amount [{}] because subject repay ", investorIdXM, freezeAmount);
            }
        }*/else if(subjectRepayDetail.getChannel().equals(Credit.SOURCE_CHANNEL_YJT)){//一键投
            if (investorPrincipal + interest > 0) {
                iPlanAccountService.updateAccountForNewRepay(subjectRepayDetail);
                IPlanAccount iPlanAccount = iPlanAccountService.findById(subjectRepayDetail.getSourceAccountId());
                IPlan iplan = iPlanService.getIPlanById(iPlanAccount.getIplanId());
                //投资人短融本地账户收益
                userAccountService.transferIn(investorIdXM, principalInterestPenaltyFee, BusinessEnum.ndr_repay, "项目还款：" + iplan.getName(), "标的回款，标的ID：" + subject.getSubjectId() + "，本金：" + investorPrincipal + "，利息："+interest, requestNo,subject.getSubjectId(),subjectRepayDetail.getScheduleId());
                logger.info("newIPlan [{}] local take back principal [{}], interest [{}] because subject repay", investorIdXM, investorPrincipal, interest);
                try{
                    SubjectRepaySchedule schedule = subjectRepayScheduleService.getById(subjectRepayDetail.getScheduleId());
                    User user = userDao.getUserById(subjectRepayDetail.getUserId());
                    UserAccount account = userAccountService.findUserAccount(subjectRepayDetail.getUserId());
                    //插入一条短信记录
                    String msg = "";
                    String type = "";
                    if(SubjectRepaySchedule.STATUS_NORMAL_REPAID.equals(schedule.getStatus())){
                       msg =  iplan.getName()+","+String.valueOf(ArithUtil.round(principalInterestPenaltyFee,2))+","+String.valueOf(account.getAvailableBalance());
                       type=TemplateId.SUBJECT_REPAY_SMS;
                    }else{
                        msg =  iplan.getName()+","+String.valueOf(ArithUtil.round(investorPrincipal,2))+","+String.valueOf(iPlanAccount.getCurrentPrincipal()/100.0)+","+String.valueOf(ArithUtil.round(principalInterestPenaltyFee,2));
                        type=TemplateId.REPAY_SMS_ADVANCE;
                    }
                    subjectService.insertMsg(subjectRepayDetail.getUserId(),msg,user.getMobileNumber(),type);
                }catch(Exception e){
                    logger.error("插入短信记录异常,detailID-{}",subjectRepayDetail.getId());
                }
            }
        }
        subjectRepayDetail.setCurrentStep(SubjectRepayDetail.STEP_HAS_DEAL_INVESTOR);
        subjectRepayDetailDao.update(subjectRepayDetail);
    }

}
