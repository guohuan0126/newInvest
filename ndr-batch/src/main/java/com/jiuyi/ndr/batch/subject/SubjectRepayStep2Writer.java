package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.subject.SubjectPayoffRegDao;
import com.jiuyi.ndr.dao.user.UserDao;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectPayoffReg;
import com.jiuyi.ndr.domain.subject.SubjectRepayBill;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.account.PlatformAccountService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.subject.SubjectAdvancedPayOffService;
import com.jiuyi.ndr.service.subject.SubjectRepayBillService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by lln on 2017/8/3.
 */
public class SubjectRepayStep2Writer implements ItemWriter<SubjectRepaySchedule> {

    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private PlatformAccountService platformAccountService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private SubjectPayoffRegDao subjectPayoffRegDao;
    @Autowired
    private SubjectAdvancedPayOffService subjectAdvancedPayOffService;
    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private SubjectRepayBillService subjectRepayBillService;


    @Value(value = "${EMAIL.REPAY_EMAIL}")
    private String repayEmail;


    private static final Logger logger = LoggerFactory.getLogger(SubjectRepayStep2Writer.class);

    @Override
    public void write(List<? extends SubjectRepaySchedule> subjectRepaySchedules) throws Exception {
        logger.info("还款第二步处理本地账户【{}】条", subjectRepaySchedules.size());
        //isRepay=2 && currentStep=repay
        for (SubjectRepaySchedule subjectRepaySchedule:subjectRepaySchedules) {
            this.dealLocalAccount(subjectRepaySchedule);
        }
        //subjectRepaySchedules.stream().forEach(this :: dealLocalAccount);
        logger.info("还款第二步本地账户处理完成");
    }


    private void dealLocalAccount(SubjectRepaySchedule subjectRepaySchedule){
        String subjectId= subjectRepaySchedule.getSubjectId();
        logger.info("还款标的-{},开始处理本地账户", subjectId);
        boolean flag = subjectRepayScheduleService.isPossibleForRepay(subjectRepaySchedule);
        if(!flag){
            logger.info("标的不符合还款要求,暂不能还款,subjectId-{}",subjectId);
            return;
        }
        Subject subject = subjectDao.findBySubjectId(subjectId);
        Integer isDirect = subject.getDirectFlag();
        Integer scheduleId = subjectRepaySchedule.getId();
        //查询是否是提前结清的标的
        SubjectPayoffReg subjectPayoffReg = subjectPayoffRegDao.findBySubjectIdAndStatus(subjectId,SubjectPayoffReg.REPAY_STATUS_PROCESSED);
        //若是直贷二期的提前结清查这个
        List<SubjectRepayBill> subjectRepayBills = subjectRepayBillService.getByScheduleIdAndType2(scheduleId,SubjectRepayBill.TYPE_ADVANCED_PAYOFF);
        //若是直贷二期的续贷结清查这个
        List<SubjectRepayBill> subjectRepayXDBills = subjectRepayBillService.getByScheduleIdAndType2(scheduleId,SubjectRepayBill.TYPE_DELAY_PAYOFF);
        //是否卡贷提前结清
        List<SubjectRepayBill> cardRepayBill = null;
        //若是卡贷且不是直贷二
        if((Subject.SUBJECT_TYPE_CARD.equals(subject.getType())||Subject.SUBJECT_TYPE_CASH.equals(subject.getType()))&&!Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag())){
            //查询是否是提前结清
            cardRepayBill = subjectRepayBillService.getByScheduleIdAndType2(subjectRepaySchedule.getId(),SubjectRepayBill.TYPE_ADVANCED_PAYOFF);
        }
        String intermediatorId = subject.getIntermediatorId().trim();
        //是否结清
        boolean isSettle = subjectPayoffReg==null && CollectionUtils.isEmpty(cardRepayBill);
        //查询居间人用户信息
        User user = userDao.getUserById(intermediatorId);
        List<SubjectRepayBill> repayBills = subjectRepayBillService.selectByScheduleId(subjectRepaySchedule.getId());
        String borrowerIdXM="";
        if(isDirect==0){
            borrowerIdXM = intermediatorId;
        }else{
            borrowerIdXM = subject.getBorrowerIdXM();
        }
        //借款信息
        Map<String, Integer> borrowerDetails ;
        if(!Subject.DIRECT_FLAG_YES_01.equals(isDirect)){
            if(isSettle){
                borrowerDetails = subjectRepayScheduleService.getBorrowerDetails(subjectRepaySchedule);
            }else if(cardRepayBill!=null&&cardRepayBill.size()>0){
                borrowerDetails = subjectRepayScheduleService.getBorrowerAdvanceDetails(subject,subjectRepaySchedule,0);
            }else{
                //计算提前结清罚息
                Integer payOffPenalty = subjectAdvancedPayOffService.advancedPayOff(subjectId);
                borrowerDetails = subjectRepayScheduleService.getBorrowerAdvanceDetails(subject,subjectRepaySchedule,payOffPenalty);
            }
        }else{
            if(subjectRepayBills.isEmpty()&&subjectRepayXDBills.isEmpty()){
                borrowerDetails = subjectRepayBillService.getDirect2BorrowerDetails(subjectRepaySchedule,repayBills.get(0));
            }else{
                //直贷二结清
                borrowerDetails = subjectRepayBillService.getDirect2BorrowerDetailsJQ(subjectRepaySchedule,repayBills.get(0),subject);
            }
        }
        double amount =0.0;
        if(isDirect.equals(Subject.DIRECT_FLAG_YES_01)){
            amount=borrowerDetails.get("brwActualOutAmt")/100.0;
        }else{
            amount= (borrowerDetails.get("duePrincipal") + borrowerDetails.get("dueInterest") + borrowerDetails.get("duePenalty") + borrowerDetails.get("dueFee")) / 100.0;
        }
        double borrowerPrincipal = borrowerDetails.get("duePrincipal") / 100.0;
        double borrowerInterest = (borrowerDetails.get("dueInterest") + borrowerDetails.get("duePenalty") + borrowerDetails.get("dueFee")) / 100.0;

        try{
            if ("repay".equals(subjectRepaySchedule.getCurrentStep())
                    && BaseResponse.STATUS_SUCCEED.equals(subjectRepaySchedule.getExtStatus())
                    && SubjectRepaySchedule.SIGN_FOR_FROZEN.equals(subjectRepaySchedule.getIsRepay())) {

                if(isDirect.equals(Subject.DIRECT_FLAG_YES)){

                    if(isSettle){
                        //营销款短融本地账户扣款
                        platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_01_DR, amount, BusinessEnum.ndr_repay, "借款人还款代充，标的名称：" + subject.getName() + "，本金：" + borrowerPrincipal + "，利息：" + borrowerInterest, subjectRepaySchedule.getMarketSn(),subjectId,subjectRepaySchedule.getId());
                        //借款人入账-本地账户更新
                        userAccountService.transferIn(subject.getBorrowerIdXM(), amount, BusinessEnum.ndr_pt_transfer, "借款人还款代充:" + subject.getName(), "借款人还款代充，标的名称：" + subject.getName() + "，本金：" + borrowerPrincipal + "，利息：" + borrowerInterest, subjectRepaySchedule.getMarketSn(),subjectId,subjectRepaySchedule.getId());
                    }else{
                        //营销款短融本地账户扣款
                        platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_01_DR, amount, BusinessEnum.ndr_repay, "借款人提前结清代充-" + subject.getName(), subjectRepaySchedule.getMarketSn(),subjectId,subjectRepaySchedule.getId());
                        userAccountService.transferIn(subject.getBorrowerIdXM(), amount, BusinessEnum.ndr_pt_transfer, "借款人结清代充-" + subject.getName(), "借款人结清代充，标的ID:" + subjectId + "，本金：" + borrowerPrincipal + "，利息：" + borrowerInterest, subjectRepaySchedule.getMarketSn(),subjectId,subjectRepaySchedule.getId());
                    }
                }

                if (isSettle&&subjectRepayBills.isEmpty()&&subjectRepayXDBills.isEmpty()){
                    userAccountService.freeze(borrowerIdXM, amount, BusinessEnum.ndr_repay, "标的还款冻结-" + subject.getName(), "标的还款冻结,标的ID:" + subjectId + ",本金:" + borrowerPrincipal + ",利息:" + borrowerInterest, subjectRepaySchedule.getExtSn(),subjectId,subjectRepaySchedule.getId());
                }else{
                    userAccountService.freeze(borrowerIdXM, amount, BusinessEnum.ndr_repay, "标的结清冻结-" + subject.getName(), "标的结清冻结标的名称：" + subject.getName(), subjectRepaySchedule.getExtSn(),subjectId,subjectRepaySchedule.getId());
                }
                subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_FOR_LOCALACCOUNT);//表示处理完第二步
                subjectRepayScheduleService.update(subjectRepaySchedule);
                logger.info("还款标的-{},处理本地账户结束", subjectId);
            }else{
                logger.warn("标的{}还款失败，未处理本地账户", subjectId);
            }
        }catch (ProcessException pe){
            logger.warn("标的{}第{}期还款失败，标的还款第二步处理本地账户异常",subjectRepaySchedule.getSubjectId(),subjectRepaySchedule.getTerm());
        }
    }
}
