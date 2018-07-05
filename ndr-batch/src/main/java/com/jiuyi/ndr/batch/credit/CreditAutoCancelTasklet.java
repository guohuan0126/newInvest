package com.jiuyi.ndr.batch.credit;

import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.iplan.IPlanRepayScheduleDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.dao.subject.*;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanRepaySchedule;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.subject.*;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.notice.support.TemplateId;
import com.jiuyi.ndr.service.subject.SubjectRepayBillService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class CreditAutoCancelTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(CreditCancelTasklet.class);

    @Autowired
    private CreditOpeningDao creditOpeningDao;

    @Autowired
    private SubjectTransferParamDao subjectTransferParamDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private SubjectRepayScheduleDao subjectRepayScheduleDao;

    @Autowired
    private CreditOpeningService creditOpeningService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UserService userService;

    @Autowired
    private SubjectPayoffRegDao subjectPayoffRegDao;
    
    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;

    @Autowired
    private IPlanDao iPlanDao;

    @Autowired
    private SubjectRepayBillService subjectRepayBillService;


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("正在进行自动撤消{}");
        creditAutoCancel();
        return RepeatStatus.FINISHED;
    }

    public void creditAutoCancel(){
        //散标债转自动撤消
        List<CreditOpening> creditOpenings = creditOpeningDao.findByStatusAndOpenChannel(CreditOpening.STATUS_OPENING, 1);
        for (CreditOpening creditOpening : creditOpenings) {
            logger.info("正在进行自动撤消,creditOpeningId{}",creditOpening.getId());
            String createTime = creditOpening.getCreateTime().substring(0,10);
            LocalDate dateStart = DateUtil.parseDate(createTime, DateUtil.DATE_TIME_FORMATTER_10);
            Subject subject = subjectDao.findBySubjectId(creditOpening.getSubjectId());
            SubjectTransferParam transferParamCode = subjectTransferParamDao.findByTransferParamCode(subject.getTransferParamCode());
            LocalDate currentDate = LocalDate.now();
            boolean before = dateStart.plusDays(transferParamCode.getAutoRevokeTime() - 1).isBefore(currentDate);
            User user = userService.getUserById(creditOpening.getTransferorId());
            //插入一条短信记录
            String msg = "";
            String type = "";
            if(before){
                creditOpeningService.cancleCredit(creditOpening.getId());
                if(CreditOpening.SOURCE_CHANNEL_SUBJECT== creditOpening.getSourceChannel() && CreditOpening.STATUS_CANCEL_ALL.equals(creditOpening.getStatus())){
                    msg = subject.getName()+","+String.valueOf(creditOpening.getTransferPrincipal()/100.0)+","
                          +String.valueOf(ArithUtil.round((creditOpening.getTransferPrincipal()/100.0)*(creditOpening.getTransferDiscount().doubleValue()),2));
                    type = TemplateId.CREDIT_AUTO_CANCLE;
                    subjectService.insertMsg(creditOpening.getTransferorId(),msg,user.getMobileNumber(),type);
                }
            }
            String dueDate = DateUtil.getCurrentDateShort();
            SubjectRepaySchedule subjectRepaySchedule = subjectRepayScheduleDao.findBySubjectIdAndDate(creditOpening.getSubjectId(), dueDate);
            SubjectPayoffReg subjectPayoffReg = subjectPayoffRegDao.findBySubjectId(creditOpening.getSubjectId());
            List<SubjectRepaySchedule> schdules = subjectRepayScheduleDao.findSubjectRepayScheduleBySubjectIdNotRepay(subject.getSubjectId());
            List<SubjectRepayBill> repayBills = this.getRepayBill(creditOpening.getSubjectId());
            if((subjectRepaySchedule != null && !before) || (subjectPayoffReg != null && (schdules != null && schdules.size() > 0)&& !before) || (repayBills != null && !before && repayBills.size() > 0)){
                logger.info("正在进行还款日自动撤消,creditOpeningId{}",creditOpening.getId());
                creditOpeningService.cancleCredit(creditOpening.getId());
                if(CreditOpening.SOURCE_CHANNEL_SUBJECT== creditOpening.getSourceChannel() && CreditOpening.STATUS_CANCEL_ALL.equals(creditOpening.getStatus())){
                    msg = subject.getName()+","+String.valueOf(creditOpening.getTransferPrincipal()/100.0)+","
                            +String.valueOf(ArithUtil.round((creditOpening.getTransferPrincipal()/100.0)*(creditOpening.getTransferDiscount().doubleValue()),2));
                    type = TemplateId.CREDIT_REPAY_CANCLE;
                    subjectService.insertMsg(creditOpening.getTransferorId(),msg,user.getMobileNumber(),type);
                }
            }
        }

        //一键投债转撤消
        List<IPlanTransLog> iPlanTransLogs = iPlanTransLogDao.findNeedCancel();
        for (IPlanTransLog iPlanTransLog : iPlanTransLogs) {
            logger.info("正在进行一键投自动撤消,transLogId{}",iPlanTransLog.getId());
            //查询折让率
            CreditOpening opening = creditOpeningDao.findByTransLogIdAllNoConditon(iPlanTransLog.getId());
            if(opening!= null){
                String createTime = iPlanTransLog.getCreateTime().substring(0,10);
                LocalDate dateStart = DateUtil.parseDate(createTime, DateUtil.DATE_TIME_FORMATTER_10);

                BigDecimal transferDiscount = opening.getTransferDiscount();
                IPlan iPlan = iPlanDao.findById(iPlanTransLog.getIplanId());
                //一键投交易配置信息
                SubjectTransferParam transferParamCode = subjectTransferParamDao.findByTransferParamCode(iPlan.getTransferParamCode());
                LocalDate currentDate = LocalDate.now();
                boolean before = dateStart.plusDays(transferParamCode.getAutoRevokeTime() - 1).isBefore(currentDate);
                User user = userService.getUserById(iPlanTransLog.getUserId());
                //插入一条短信记录
                String msg = "";
                if(before){
                    creditOpeningService.cancelCreditTransferNew(iPlanTransLog.getId());
                    if(iPlanTransLog.getTransType().equals(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL) && iPlanTransLog.getActualAmt() == 0){
                        msg = iPlan.getName()+","+String.valueOf(iPlanTransLog.getTransAmt()/100.0)+","
                                +String.valueOf(ArithUtil.round((iPlanTransLog.getTransAmt()/100.0)*(transferDiscount.doubleValue()),2));
                        subjectService.insertMsg(iPlanTransLog.getUserId(),msg,user.getMobileNumber(),TemplateId.CREDIT_AUTO_CANCLE);
                    }
                }
                String dueDate = DateUtil.getCurrentDateShort();
                List<Subject> subjects =new ArrayList<>();
                if(IPlan.PACKAGING_TYPE_SUBJECT.equals(iPlan.getPackagingType())){
                    subjects = subjectDao.getSubjectByIplanId(iPlanTransLog.getIplanId());
                }else{
                    List<String> subjectIds = creditOpeningDao.findByIplanId(iPlanTransLog.getIplanId());
                    subjects = subjectDao.findBySubjectIds(subjectIds);
                }
                SubjectPayoffReg subjectPayoffReg = null;
                SubjectRepaySchedule subjectRepaySchedule = null;
                List<SubjectRepayBill> bills = null;
                for (Subject subject : subjects) {
                    subjectRepaySchedule = subjectRepayScheduleDao.findBySubjectIdAndDate(subject.getSubjectId(), dueDate);
                    if(subjectRepaySchedule != null){
                        break;
                    }
                    SubjectPayoffReg  payoffReg = subjectPayoffRegDao.findBySubjectId(subject.getSubjectId());
                    List<SubjectRepaySchedule> schdules = subjectRepayScheduleDao.findSubjectRepayScheduleBySubjectIdNotRepay(subject.getSubjectId());
                    if(payoffReg != null && (schdules != null && schdules.size() > 0)){
                        subjectPayoffReg = payoffReg;
                        break;
                    }
                    List<SubjectRepayBill> repayBills = this.getRepayBill(subject.getSubjectId());
                    if(repayBills != null && repayBills.size() > 0){
                        bills = repayBills;
                        break;
                    }
                }
                if((subjectRepaySchedule != null && !before) || (subjectPayoffReg != null && !before) || (bills != null && !before && bills.size() >0)){
                    logger.info("正在进行一键投还款日自动撤消,transLogId{}",iPlanTransLog.getId());
                    creditOpeningService.cancelCreditTransferNew(iPlanTransLog.getId());
                    if(iPlanTransLog.getTransType().equals(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL) && iPlanTransLog.getActualAmt() == 0){
                        msg =iPlan.getName()+","+String.valueOf(iPlanTransLog.getTransAmt()/100.0)+","
                             +String.valueOf(ArithUtil.round((iPlanTransLog.getTransAmt()/100.0)*(transferDiscount.doubleValue()),2));
                        subjectService.insertMsg(iPlanTransLog.getUserId(),msg,user.getMobileNumber(),TemplateId.CREDIT_REPAY_CANCLE);
                    }
                }
            }

        }
    }

    public List<SubjectRepayBill> getRepayBill(String subjectId){
        List<SubjectRepayBill> list = new ArrayList<>();
        List<SubjectRepayBill> subjectRepayBills = subjectRepayBillService.getBySubjectIdAndType(subjectId, SubjectRepayBill.TYPE_ADVANCED_PAYOFF);
        List<SubjectRepayBill> repayBills = subjectRepayBillService.getBySubjectIdAndType(subjectId, SubjectRepayBill.TYPE_DELAY_PAYOFF);
        if(subjectRepayBills != null && subjectRepayBills.size()>0){
            list.addAll(subjectRepayBills);
        }
        if(repayBills != null && repayBills.size()>0){
            list.addAll(repayBills);
        }
        return list;
    }
}
