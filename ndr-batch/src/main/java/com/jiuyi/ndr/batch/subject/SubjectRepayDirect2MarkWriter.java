package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.domain.account.BrwForCpsLog;
import com.jiuyi.ndr.domain.account.CompensatoryAcctLog;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepayBill;
import com.jiuyi.ndr.domain.subject.SubjectRepayEmail;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.service.account.BrwForCpsLogService;
import com.jiuyi.ndr.service.account.CompensatoryAcctLogService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.subject.SubjectRepayBillService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

public class SubjectRepayDirect2MarkWriter implements ItemWriter<SubjectRepayBill> {

    private static final Logger logger = LoggerFactory.getLogger(SubjectRepayDirect2MarkWriter.class);

    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private CompensatoryAcctLogService cpsAcctLogService;
    @Autowired
    private SubjectRepayBillService subjectRepayBillService;
    @Autowired
    private BrwForCpsLogService brwForCpsLogService;
    @Autowired
    private SubjectRepayDetailDao subjectRepayDetailDao;

    @Override
    public void write(List<? extends SubjectRepayBill> subjectRepayBills) throws Exception {
        logger.info("开始处理直贷二期还款,条数:{}",subjectRepayBills.size());
        for (SubjectRepayBill subjectRepayBill : subjectRepayBills) {
            this.handleTrans(subjectRepayBill);
        }
        logger.info("直贷二期还款第一步完成");
    }

    //处理直贷二还款
    public void handleTrans(SubjectRepayBill subjectRepayBill) {
        SubjectRepaySchedule subjectRepaySchedule = subjectRepayScheduleService.getById(subjectRepayBill.getScheduleId());
        Integer scheduleId = subjectRepaySchedule.getId();
        Subject subject = subjectService.findBySubjectId(subjectRepaySchedule.getSubjectId());
        String subjectId = subject.getSubjectId();
        if (!subject.getDirectFlag().equals(Subject.DIRECT_FLAG_YES_01)) {
            logger.warn("非直贷二期标的还款！subjectId = {}", subjectId);
            //将is_repay更新成1
            if(!"O".equals(subjectRepayBill.getType())&&SubjectRepaySchedule.SIGN_NOT_REPAY.equals(subjectRepaySchedule.getIsRepay())){
                subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_WAIT_REPAY);
                subjectRepayScheduleService.update(subjectRepaySchedule);
            }
            //卡贷类型
            subjectRepayBillService.update(scheduleId, subjectId, SubjectRepayBill.STATUS_REPAY, subjectRepayBill.getId());
            return;
        }
        //若是卡贷结清,且上一期是提前还当期
        if(Subject.SUBJECT_TYPE_CARD.equals(subject.getType())&&SubjectRepayBill.TYPE_ADVANCED_PAYOFF.equals(subjectRepayBill.getType()) && SubjectRepaySchedule.STATUS_NORMAL_REPAID.equals(subjectRepaySchedule.getStatus())){
            List<SubjectRepayBill> billList = subjectRepayBillService.getByScheduleIdAndType2(scheduleId,SubjectRepayBill.TYPE_SETTLE_CURR_REPAY);
            if(!CollectionUtils.isEmpty(billList)){
                subjectRepayBill.setTerm(subjectRepaySchedule.getTerm()+1);
                subjectRepayBill.setStatus(SubjectRepayBill.STATUS_CRUDE);
                subjectRepayBillService.update(subjectRepayBill);
                return;
            }
        }

        //若状态为续贷结清或是未代偿
        if(subjectRepayBill.getType().equals(SubjectRepayBill.TYPE_DELAY_PAYOFF)|| SubjectRepaySchedule.CPS_STATUS_NOT_YET.equals(subjectRepaySchedule.getCpsStatus())){
            boolean flag = subjectRepayScheduleService.isPossibleForRepay(subjectRepaySchedule);
            if(!flag){
                logger.info("标的不符合还款要求,暂不能还款,subjectId-{}",subjectId);
                return;
            }
        }
        //代偿账户名称
        String cpsAccountName = subject.getCompensationAccount().trim();

        UserAccount userAccount = userAccountService.findUserAccount(subject.getBorrowerId());
        UserAccount cpsLocalAcct = userAccountService.findUserAccount(cpsAccountName);

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

        Integer availableBalance = BigDecimal.valueOf(userAccount.getAvailableBalance()).multiply(BigDecimal.valueOf(100)).intValue();

        Integer dueTotalAmt = billDuePrincipal + billDueInterest + billDuePenalty + billDueFee;

        Integer repayTotalAmt = repayPrincipal + repayInterest + repayPenalty + repayFee;//实还总金额（不包含减免）

        Integer derateAmt = deratePrincipal + derateInterest + deratePenalty + derateFee;//减免总金额
        Integer returnAmt = returnPremiumFee + returnFee;//退还总金额
        Integer brwActualOutAmt = repayTotalAmt - offlineAmt - returnAmt;//借款人账户实际出款金额
        Integer needCpsAmt = dueTotalAmt - brwActualOutAmt;//需要从代偿账户出账的金额（包括不足还款额、退还、减免、线下打款）
        needCpsAmt = needCpsAmt<0 ? 0 : needCpsAmt;
        logger.info("直贷二还款,标的:{},schedule:{},借款人实还:{},代偿出款:{}",subject.getId(),scheduleId,brwActualOutAmt,needCpsAmt);
        if(brwActualOutAmt<0){
            logger.warn("直贷二还款借款人实出金额小于0,异常，scheduleId={}, borrowerId={},repayBillId={}", scheduleId, subject.getBorrowerId(),subjectRepayBill.getId());
            return;
        }
        //是否新的直贷二模式
        boolean newFlag = Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag())&&subject.getInvestRate()!=null && subject.getRate().equals(subject.getInvestRate()) && subject.getRate().compareTo(BigDecimal.valueOf(0.144))<0 && subject.getRate().compareTo(BigDecimal.valueOf(0.068))>0;
        if(!newFlag && SubjectRepaySchedule.STATUS_NOT_REPAY.equals(subjectRepaySchedule.getStatus())) {
            if (SubjectRepayBill.TYPE_NORMAL_REPAY.equals(subjectRepayBill.getType())) {
                //应还本金小于本地应还且差值大于等于1块(因为线上线下会有0.01的误差 单位是分)
                if (billDuePrincipal < subjectRepaySchedule.getDuePrincipal() && subjectRepaySchedule.getDuePrincipal()-billDuePrincipal>=100) {
                    logger.warn("直贷二还款文件本金小于应还,异常，scheduleId={}, borrowerId={},repayBillId={}", scheduleId, subject.getBorrowerId(), subjectRepayBill.getId());
                    return;
                }
            } else if (SubjectRepayBill.TYPE_ADVANCED_PAYOFF.equals(subjectRepayBill.getType()) || SubjectRepayBill.TYPE_DELAY_PAYOFF.equals(subjectRepayBill.getType())) {
                if (billDuePrincipal < (subject.getTotalAmt() - subject.getPaidPrincipal()) &&  (subject.getTotalAmt() - subject.getPaidPrincipal())-billDuePrincipal >=100) {
                    logger.warn("直贷二还款文件本金小于应还,异常，scheduleId={}, borrowerId={},repayBillId={}", scheduleId, subject.getBorrowerId(), subjectRepayBill.getId());
                    return;
                }
            }
        }else if(newFlag && SubjectRepaySchedule.STATUS_NOT_REPAY.equals(subjectRepaySchedule.getStatus())){
            if (SubjectRepayBill.TYPE_NORMAL_REPAY.equals(subjectRepayBill.getType())) {
                if ((billDuePrincipal+billDueInterest) < (subjectRepaySchedule.getDuePrincipal()+subjectRepaySchedule.getDueInterest())) {
                    logger.warn("直贷二还款文件本金小于应还,异常，scheduleId={}, borrowerId={},repayBillId={}", scheduleId, subject.getBorrowerId(), subjectRepayBill.getId());
                    return;
                }
            } else if (SubjectRepayBill.TYPE_ADVANCED_PAYOFF.equals(subjectRepayBill.getType()) || SubjectRepayBill.TYPE_DELAY_PAYOFF.equals(subjectRepayBill.getType())) {
                if (billDuePrincipal < (subject.getTotalAmt() - subject.getPaidPrincipal())) {
                    logger.warn("直贷二还款文件本金小于应还,异常，scheduleId={}, borrowerId={},repayBillId={}", scheduleId, subject.getBorrowerId(), subjectRepayBill.getId());
                    return;
                }
            }
        }
        //代偿余额
        Integer cpsAccoutBalance = BigDecimal.valueOf(cpsLocalAcct.getAvailableBalance()).multiply(BigDecimal.valueOf(100)).intValue();
        //若是续贷结清
        if(subjectRepayBill.getType().equals(SubjectRepayBill.TYPE_DELAY_PAYOFF)){
            needCpsAmt = repayTotalAmt+derateAmt;

            BaseResponse baseResponse = null;
            Integer status = subjectRepaySchedule.getExtStatus();
            //未代偿
            if (SubjectRepaySchedule.CPS_STATUS_NOT_YET.equals(subjectRepaySchedule.getCpsStatus())&&SubjectRepaySchedule.STATUS_NOT_REPAY.equals(subjectRepaySchedule.getStatus())) {
                //失败或是未处理
                if(status==null || BaseResponse.STATUS_FAILED.equals(status)){
                    if (cpsLocalAcct!=null&&needCpsAmt > cpsAccoutBalance) {
                        noticeService.sendRepayEmail("续贷结清还款代偿账户余额不足","代偿账户:"+cpsAccountName+",标的:"+subjectId+",scheduleId:"+scheduleId,"liulina@duanrong.com",cpsAccountName,Subject.DIRECT_FLAG_YES_01, SubjectRepayEmail.STATUS_ALL);
                        logger.warn("代偿账户余额不足，account={}，scheduleId={}, borrowerId={}", cpsAccountName, subjectRepaySchedule.getId(), subject.getBorrowerId());
                        return;
                    }

                    baseResponse = subjectRepayBillService.freezeCpsAcctTrans2(subjectId,cpsAccountName,needCpsAmt);
                    status = baseResponse.getStatus();
                }else{
                    baseResponse = subjectRepayBillService.preSingleTransQuery(subjectRepaySchedule.getExtSnCps());
                    if (BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                        status = BaseResponse.STATUS_SUCCEED;
                        logger.info("代偿账户平台预处理交易上次处理中，查询结果是上次交易成功，scheduleId={}", subjectRepaySchedule.getId());
                    } else if (BaseResponse.STATUS_FAILED.equals(baseResponse.getStatus())) {
                        status = BaseResponse.STATUS_FAILED;
                        logger.info("代偿账户平台预处理交易上次处理中，查询结果是上次交易失败，continue ！scheduleId={}", subjectRepaySchedule.getId());
                    } else {
                        status = BaseResponse.STATUS_PENDING;
                        logger.info("代偿账户平台预处理交易上次处理中，查询仍处理中，continue ！scheduleId={}", subjectRepaySchedule.getId());
                    }
                }
                if (BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                    subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_FOR_LOCALACCOUNT);
                    subjectRepaySchedule.setCpsStatus(SubjectRepaySchedule.CPS_STATUS_HAS_BEEN);
                    subjectRepaySchedule.setCurrentStep("repay");
                    //插入代偿账户平台流水
                    logger.info("本地开始处理代偿账户冻结");
                    userAccountService.freeze(cpsAccountName, needCpsAmt/ 100.0, BusinessEnum.ndr_subject_repay_cps_out, "标的还款续贷结清代偿冻结-" + subject.getName(), "标的还款续贷结清冻结,标的ID:" + subject.getSubjectId() + ",金额:" + needCpsAmt/ 100.0, baseResponse.getRequestNo(),subject.getSubjectId(),scheduleId);
                    //成功,插入流水
                    cpsAcctLogService.log(subjectRepaySchedule.getId(), subjectRepaySchedule.getSubjectId(),
                            subjectRepaySchedule.getTerm(), subjectRepayBill.getId(), cpsAccountName, needCpsAmt, baseResponse.getRequestNo(), baseResponse.getStatus(), CompensatoryAcctLog.TYPE_CONTINUE_OUT);
                    //成功,更新还款文件状态
                    subjectRepayBillService.update(scheduleId, subjectId, SubjectRepayBill.STATUS_REPAY, subjectRepayBill.getId());
                }
                subjectRepaySchedule.setExtStatus(status);
                subjectRepaySchedule.setExtSnCps(baseResponse.getRequestNo());
                subjectRepaySchedule.setInterimRepayAmt(0);
                subjectRepaySchedule.setInterimCpsAmt(needCpsAmt);
                subjectRepaySchedule.setRepayPrincipal(subjectRepaySchedule.getRepayPrincipal() + repayPrincipal + deratePrincipal);
                subjectRepaySchedule.setRepayInterest(subjectRepaySchedule.getRepayInterest() + repayInterest + derateInterest);
                subjectRepaySchedule.setRepayPenalty(subjectRepaySchedule.getRepayPenalty() + repayPenalty + deratePenalty);
                subjectRepaySchedule.setRepayFee(subjectRepaySchedule.getRepayFee() + repayFee + derateFee);
                //记录代偿账户初始出的金额
                subjectRepaySchedule.setInitCpsAmt(needCpsAmt);
                subjectRepayScheduleService.update(subjectRepaySchedule);
            }else{
                //逾期续贷,这条还款文件不处理
                subjectRepayBillService.update(scheduleId, subjectId, SubjectRepayBill.STATUS_REPAY, subjectRepayBill.getId());
            }
            return;
        }else {
            BaseResponse baseResponse = null;
            //未代偿
            if (SubjectRepaySchedule.CPS_STATUS_NOT_YET.equals(subjectRepaySchedule.getCpsStatus()) && SubjectRepaySchedule.STATUS_NOT_REPAY.equals(subjectRepaySchedule.getStatus()) && SubjectRepaySchedule.SIGN_NOT_REPAY.equals(subjectRepaySchedule.getIsRepay())) {
                if (availableBalance < brwActualOutAmt) {
                    logger.warn("借款人账户余额不足，scheduleId={}, borrowerId={}", scheduleId, subject.getBorrowerId());
                    return;
                }
                if (cpsLocalAcct != null && needCpsAmt > cpsAccoutBalance) {
                    logger.warn("代偿账户余额不足，account={}，scheduleId={}, borrowerId={}", cpsAccountName, scheduleId, subject.getBorrowerId());
                    return;
                }
                CompensatoryAcctLog cpsAcctLog;
                if (offlineAmt > 0) {
                    cpsAcctLog = cpsAcctLogService.getCpsAcctLog(subjectRepayBill.getId(), CompensatoryAcctLog.TYPE_OFFLINE_OUT, BaseResponse.STATUS_PENDING, CompensatoryAcctLog.STATUS_NOT_HANDLED);
                } else if (derateAmt + returnAmt > 0) {
                    cpsAcctLog = cpsAcctLogService.getCpsAcctLog(subjectRepayBill.getId(), CompensatoryAcctLog.TYPE_DERATE_RETURN_OUT, BaseResponse.STATUS_PENDING, CompensatoryAcctLog.STATUS_NOT_HANDLED);
                } else if (repayTotalAmt + derateAmt < dueTotalAmt) {
                    cpsAcctLog = cpsAcctLogService.getCpsAcctLog(subjectRepayBill.getId(), CompensatoryAcctLog.TYPE_CPS_OUT, BaseResponse.STATUS_PENDING, CompensatoryAcctLog.STATUS_NOT_HANDLED);
                } else {
                    cpsAcctLog = null;
                }

                String freezeNo;
                if (cpsAcctLog == null) {
                    if (needCpsAmt > 0) {
                        //代偿账户实际出的钱 = 应还总额 - 借款人在平台实际出的钱
//                        baseResponse = subjectRepayBillService.freezeCpsAcctTrans(subjectId, cpsAccountName, needCpsAmt);
                        baseResponse = subjectRepayBillService.freezeCpsAcctTrans2(subject.getSubjectId(),cpsAccountName,needCpsAmt);
                    }

                } else {
                    logger.info("代偿账户平台预处理交易上次处理中，进行单笔交易查询！scheduleId={}，repayBillId={}", scheduleId, subjectRepayBill.getId());
                    freezeNo = cpsAcctLog.getExtSn();
                    baseResponse = subjectRepayBillService.preSingleTransQuery(freezeNo);
                    if (BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                        cpsAcctLogService.update(baseResponse.getStatus(), BrwForCpsLog.STATUS_FREEZE, cpsAcctLog.getId());
                        logger.info("代偿账户平台预处理交易上次处理中，查询结果是上次交易成功，scheduleId={}，repayBillId={}", scheduleId, subjectRepayBill.getId());
                    } else if (BaseResponse.STATUS_FAILED.equals(baseResponse.getStatus())) {
                        cpsAcctLogService.update(baseResponse.getStatus(), BrwForCpsLog.STATUS_FREEZE, cpsAcctLog.getId());
                        logger.info("代偿账户平台预处理交易上次处理中，查询结果是上次交易失败，continue ！scheduleId={}，repayBillId={}", scheduleId, subjectRepayBill.getId());
                        return;
                    } else {
                        logger.info("代偿账户平台预处理交易上次处理中，查询仍处理中，continue ！scheduleId={}，repayBillId={}", scheduleId, subjectRepayBill.getId());
                        return;
                    }
                }
                if (baseResponse != null && BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                    subjectRepaySchedule.setExtSnCps(baseResponse.getRequestNo());
                    //正常还款
                    userAccountService.freeze(cpsAccountName, needCpsAmt/ 100.0, BusinessEnum.ndr_subject_repay_cps_out, "标的还款代偿冻结-" + subject.getName(), "标的还款代偿冻结,标的ID:" + subject.getSubjectId() + ",金额:" + needCpsAmt/ 100.0, baseResponse.getRequestNo(),subject.getSubjectId(),scheduleId);
                }

                //1.线下打款代偿
                if (offlineAmt > 0) {
                    cpsAcctLogService.log(subjectRepaySchedule.getId(), subjectRepaySchedule.getSubjectId(),
                            subjectRepaySchedule.getTerm(), subjectRepayBill.getId(), cpsAccountName, offlineAmt, baseResponse.getRequestNo(), baseResponse.getStatus(), CompensatoryAcctLog.TYPE_OFFLINE_OUT);
                }
                //2.减免、退还代偿
                if (derateAmt + returnAmt > 0) {
                    cpsAcctLogService.log(subjectRepaySchedule.getId(), subjectRepaySchedule.getSubjectId(),
                            subjectRepaySchedule.getTerm(), subjectRepayBill.getId(), cpsAccountName, derateAmt + returnAmt, baseResponse.getRequestNo(), baseResponse.getStatus(), CompensatoryAcctLog.TYPE_DERATE_RETURN_OUT);
                }

                if (needCpsAmt<=0) {
                    subjectRepaySchedule.setCpsStatus(SubjectRepaySchedule.CPS_STATUS_NOT_YET);
                } else {//还款额不足代偿（部分还款），存在减免、退还和线下打款等情况
                    Integer cpsAmt = dueTotalAmt - brwActualOutAmt - (offlineAmt + derateAmt + returnAmt);//代偿账户部分还款代偿额
                    if (cpsAmt > 0) {
                        cpsAcctLogService.log(subjectRepaySchedule.getId(), subjectRepaySchedule.getSubjectId(),
                                subjectRepaySchedule.getTerm(), subjectRepayBill.getId(), cpsAccountName, cpsAmt, baseResponse.getRequestNo(), baseResponse.getStatus(), CompensatoryAcctLog.TYPE_CPS_OUT);
                    }
                    subjectRepaySchedule.setCpsStatus(SubjectRepaySchedule.CPS_STATUS_HAS_BEEN);
                }
                //若交易没成功不执行下面的更新操作
                if (baseResponse != null && !BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                    return;
                }

                if (brwActualOutAmt > 0 && needCpsAmt == 0 && SubjectRepaySchedule.STATUS_NOT_REPAY.equals(subjectRepaySchedule.getStatus())) {//repay_schedule，不在这里冻结
                    //未代偿 冻结借款人
                    subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_WAIT_REPAY);
                    subjectRepaySchedule.setCurrentStep("freeze");
                } else if (brwActualOutAmt > 0 && needCpsAmt > 0 && SubjectRepaySchedule.STATUS_NOT_REPAY.equals(subjectRepaySchedule.getStatus())) {
                    //部分还款
                    if (BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                        subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_WAIT_REPAY);
                        subjectRepaySchedule.setCurrentStep("freeze");
                    }
                } else if (brwActualOutAmt == 0) {
                    //全部代偿
                    //若代偿账户冻结成功,将该条还款计划is_repay设为3,直接去处理本地
                    if (BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus()) && SubjectRepaySchedule.STATUS_NOT_REPAY.equals(subjectRepaySchedule.getStatus())) {
                        subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_FOR_LOCALACCOUNT);
                        subjectRepaySchedule.setCurrentStep("repay");
                        subjectRepaySchedule.setExtStatus(baseResponse.getStatus());
                    }
                }
                subjectRepaySchedule.setRepayPrincipal(repayPrincipal + deratePrincipal);
                subjectRepaySchedule.setRepayInterest(repayInterest + derateInterest);
                subjectRepaySchedule.setRepayPenalty(repayPenalty + deratePenalty);
                subjectRepaySchedule.setRepayFee(repayFee + derateFee);
                subjectRepaySchedule.setInterimRepayAmt(brwActualOutAmt);
                subjectRepaySchedule.setInterimCpsAmt(needCpsAmt);
                subjectRepaySchedule.setInitCpsAmt(needCpsAmt);
                //逾期结清还逾期那期,将type更新成逾期还款
                if(SubjectRepayBill.TYPE_ADVANCED_PAYOFF.equals(subjectRepayBill.getType())&&(SubjectRepaySchedule.STATUS_OVERDUE.equals(subjectRepaySchedule.getStatus())||SubjectRepaySchedule.STATUS_OVERDUE_REPAID.equals(subjectRepaySchedule.getStatus()))){
                    subjectRepayBillService.updateType(scheduleId,subject.getSubjectId(),SubjectRepayBill.TYPE_OVERDUE_REPAY,subjectRepayBill.getId());
                }
            } else if(!SubjectRepaySchedule.CPS_STATUS_NOT_YET.equals(subjectRepaySchedule.getCpsStatus()) && !SubjectRepaySchedule.STATUS_NOT_REPAY.equals(subjectRepaySchedule.getStatus())){//已代偿并已发生还款

                needCpsAmt = offlineAmt + returnAmt + derateAmt;
                //若借款人实出金额为0,则不进行还代偿账户操作
                if (brwActualOutAmt == 0 || brwActualOutAmt<=derateAmt) {
                    subjectRepayBillService.update(scheduleId, subjectId, SubjectRepayBill.STATUS_REPAY, subjectRepayBill.getId());
                    //插入一条借款人还代偿账户流水
                    brwForCpsLogService.insert(subjectRepaySchedule.getId(), subjectId, subjectRepaySchedule.getTerm(), subject.getBorrowerId(), subjectRepayBill.getId(),
                            cpsAccountName, null, BaseResponse.STATUS_SUCCEED, BrwForCpsLog.STATUS_LOCAL, brwActualOutAmt, needCpsAmt - offlineAmt, offlineAmt);
                    return;
                }
                String freezeNo;
                //先通过bill_id查询冻结交易存不存在处理中的交易，若不存在，直接发交易；若存在，判断交易状态
                BrwForCpsLog brwForCpsLog = brwForCpsLogService.getByRepayBillIdAndExtStatusAndStatus(subjectRepayBill.getId(), BaseResponse.STATUS_PENDING, BrwForCpsLog.STATUS_FREEZE);
                if (brwForCpsLog == null) {
                    brwForCpsLog = brwForCpsLogService.getByRepayBillIdAndExtStatusAndStatus(subjectRepayBill.getId(), BaseResponse.STATUS_SUCCEED, BrwForCpsLog.STATUS_FREEZE);
                    if (brwForCpsLog != null) {
                        freezeNo = brwForCpsLog.getExtSn();
                    } else {
                        if (availableBalance < brwActualOutAmt) {
                            logger.warn("借款人账户余额不足，scheduleId={}, borrowerId={}", scheduleId, subject.getBorrowerId());
                            return;
                        }
                        baseResponse = subjectRepayBillService.freezeBrwAcctForCpsTrans(subjectId, subject.getBorrowerId(), brwActualOutAmt);
                        //记录借款人还代偿账户流水
                        brwForCpsLogService.insert(subjectRepaySchedule.getId(), subjectId, subjectRepaySchedule.getTerm(), subject.getBorrowerId(), subjectRepayBill.getId(),
                                cpsAccountName, baseResponse.getRequestNo(), baseResponse.getStatus(), BrwForCpsLog.STATUS_FREEZE, brwActualOutAmt, needCpsAmt - offlineAmt, offlineAmt);
                        if (!BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                            return;
                        }
                        freezeNo = baseResponse.getRequestNo();
                    }
                } else {//处理中，进行单笔业务查询
                    logger.info("借款人还代偿账户授权预处理交易上次处理中，进行单笔交易查询！scheduleId={}，repayBillId={}", scheduleId, subjectRepayBill.getId());
                    freezeNo = brwForCpsLog.getExtSn();
                    baseResponse = subjectRepayBillService.preSingleTransQuery(freezeNo);
                    if (BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                        brwForCpsLogService.update(freezeNo, baseResponse.getStatus(), BrwForCpsLog.STATUS_FREEZE, brwForCpsLog.getId());
                        logger.info("借款人还代偿账户授权预处理交易上次处理中，查询结果是上次交易成功，scheduleId={}，repayBillId={}", scheduleId, subjectRepayBill.getId());
                    } else if (BaseResponse.STATUS_FAILED.equals(baseResponse.getStatus())) {
                        brwForCpsLogService.update(freezeNo, baseResponse.getStatus(), BrwForCpsLog.STATUS_FREEZE, brwForCpsLog.getId());
                        logger.info("借款人还代偿账户授权预处理交易上次处理中，查询结果是上次交易失败，continue ！scheduleId={}，repayBillId={}", scheduleId, subjectRepayBill.getId());
                        return;
                    } else {
                        logger.info("借款人还代偿账户授权预处理交易上次处理中，查询仍处理中，continue ！scheduleId={}，repayBillId={}", scheduleId, subjectRepayBill.getId());
                        return;
                    }
                }

                if (baseResponse != null && BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                    //借款人本地账户冻结
                    userAccountService.freeze(subject.getBorrowerIdXM(), brwActualOutAmt / 100.0, BusinessEnum.ndr_subject_repay_cps_in, "借款人还代偿账户冻结", "借款人还代偿账户冻结,标的ID:" + subject.getSubjectId() + ",金额:" + brwActualOutAmt / 100.0, baseResponse.getRequestNo(), subject.getSubjectId(), subjectRepaySchedule.getId());
                }

                if (brwForCpsLog == null) {
                    brwForCpsLog = brwForCpsLogService.findByscheduleIdAndBillIdAndStatus(subjectRepaySchedule.getId(), subjectRepayBill.getId(),BaseResponse.STATUS_SUCCEED);
                    if(brwForCpsLog==null){
                        return;
                    }
                }

                //查询借款人已经还代偿的
                List<BrwForCpsLog> repayBrwFor = brwForCpsLogService.findByscheduleIdAndSubjectId(scheduleId, subject.getSubjectId(), BrwForCpsLog.STATUS_LOCAL);
                Integer totalRepayAmt = repayBrwFor.stream().map(BrwForCpsLog::getRepayAmt).reduce((a, b) -> a + b).orElse(0);
                //查询代偿替借款人代偿的(不包含线下打款和减免)
//                CompensatoryAcctLog repayCpsLog = cpsAcctLogService.getCpsAcctLogsByScheduleIdAndType(scheduleId, CompensatoryAcctLog.TYPE_CPS_OUT);
                //由于出账记为-
//                Integer initAmt = Integer.valueOf(repayCpsLog.getAmount().toString().replace("-", "")) - repayCpsLog.getProfit();
                //剩余还代偿的金额
//                Integer surplusRepayAmt = initAmt - totalRepayAmt;
                Integer amt = 0;
                //借款人还得本息
                Integer cpsInt = brwActualOutAmt-subjectRepayBill.getRepayPenalty()-subjectRepayBill.getRepayFee();
                //分润给事业部的钱
                Integer profit = subjectRepayBill.getRepayPenalty()+subjectRepayBill.getRepayFee();
                Integer profitCps = 0;//分润给代偿账户的钱
                if(cpsInt>0) {
                    //代偿给投资人本息和,佣金分润给代偿,其余的走分润
                    Integer totalAmtByScheduleId = subjectRepayDetailDao.getTotalAmtByScheduleId(scheduleId);
                    if (cpsInt > (totalAmtByScheduleId - totalRepayAmt)) { //若代偿还款本息大于代偿的本息
                        profitCps = cpsInt - (totalAmtByScheduleId - totalRepayAmt);
                        profitCps = profitCps > 0 ? profitCps : 0;
                    } else if (totalRepayAmt > totalAmtByScheduleId) {//若已还的>代偿的本息佣金,则除去罚费其他的分润给代偿账户
                        profitCps = cpsInt;
                    }
                }
                //代偿入账
                CompensatoryAcctLog cpsAcctLog = cpsAcctLogService.getCpsAcctLog(subjectRepayBill.getId(), CompensatoryAcctLog.TYPE_CPS_IN, BaseResponse.STATUS_PENDING, CompensatoryAcctLog.STATUS_NOT_HANDLED);
                if (cpsAcctLog == null) {
                    cpsAcctLog = cpsAcctLogService.getCpsAcctLog(subjectRepayBill.getId(), CompensatoryAcctLog.TYPE_CPS_IN, BaseResponse.STATUS_SUCCEED, CompensatoryAcctLog.STATUS_NOT_HANDLED);
                    if (cpsAcctLog == null) {
                        //若已还清代偿,则此次还代偿账户的钱入分润账户
                        if (SubjectRepaySchedule.CPS_STATUS_PAYOFF.equals(subjectRepaySchedule.getCpsStatus())) {
                            baseResponse = subjectRepayBillService.repayCpsAcctTrans(subjectId, brwActualOutAmt, freezeNo, subject.getBorrowerId(), cpsAccountName, brwActualOutAmt,0, subject.getProfitAccount());
                            cpsAcctLogService.log(subjectRepaySchedule.getId(), subjectRepaySchedule.getSubjectId(),
                                    subjectRepaySchedule.getTerm(), subjectRepayBill.getId(), cpsAccountName, brwActualOutAmt, baseResponse.getRequestNo(), baseResponse.getStatus(), CompensatoryAcctLog.TYPE_CPS_RECORD_IN);
                            profit = brwActualOutAmt;
                        } else {
                            baseResponse = subjectRepayBillService.repayCpsAcctTrans(subjectId, brwActualOutAmt, freezeNo, subject.getBorrowerId(), cpsAccountName, profit, profitCps,subject.getProfitAccount());
                            cpsAcctLogService.log(subjectRepaySchedule.getId(), subjectRepaySchedule.getSubjectId(),
                                    subjectRepaySchedule.getTerm(), subjectRepayBill.getId(), cpsAccountName, brwActualOutAmt-profit, baseResponse.getRequestNo(), baseResponse.getStatus(), CompensatoryAcctLog.TYPE_CPS_IN);
                        }
                        //若成功,增加一条分润流水
                        if (profit > 0 && BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                            userAccountService.transferIn(subject.getProfitAccount(), profit / 100.0, BusinessEnum.ndr_repay_cps_profit,
                                    "借款人还代偿账户", "借款人还代偿账户:标的ID-" + subject.getSubjectId() + ",分润收取:金额" + profit / 100.0, baseResponse.getRequestNo(), subject.getSubjectId(), subjectRepaySchedule.getId());
                        }
                        if (!BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                            return;
                        }
                        //还代偿账户成功,更新流水状态
                        brwForCpsLogService.updateForStatus(BrwForCpsLog.STATUS_REPAY, brwForCpsLog.getId());
                    } else {
                        baseResponse.setStatus(BaseResponse.STATUS_SUCCEED);
                    }
                } else {//单笔交易查询
                    logger.info("借款人还代偿账户上次交易处理中，进行单笔交易查询！scheduleId={}，repayBillId={}", scheduleId, subjectRepayBill.getId());
                    baseResponse = subjectRepayBillService.conSingleTransQuery(cpsAcctLog.getExtSn());
                    if (BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                        //更新借款人还代偿账户流水状态
                        brwForCpsLogService.updateForStatus(BrwForCpsLog.STATUS_REPAY, brwForCpsLog.getId());
                        cpsAcctLogService.update(baseResponse.getStatus(), cpsAcctLog.getStatus(), cpsAcctLog.getId());
                        logger.info("借款人还代偿账户上次交易处理中，查询结果是上次交易成功，continue ！scheduleId={}，repayBillId={}", scheduleId, subjectRepayBill.getId());
                    } else if (BaseResponse.STATUS_FAILED.equals(baseResponse.getStatus())) {
                        cpsAcctLogService.update(baseResponse.getStatus(), cpsAcctLog.getStatus(), cpsAcctLog.getId());
                        logger.info("借款人还代偿账户上次交易处理中，查询结果是上次交易失败，continue ！scheduleId={}，repayBillId={}", scheduleId, subjectRepayBill.getId());
                        return;
                    } else {
                        logger.info("借款人还代偿账户上次交易处理中，查询仍处理中，continue ！scheduleId={}，repayBillId={}", scheduleId, subjectRepayBill.getId());
                        return;
                    }
                }

                //还代偿账户交易成功,借款人账户从冻结中转出
                if(baseResponse !=null && baseResponse.getStatus().equals(BaseResponse.STATUS_SUCCEED)){
                    //代偿账户入账
                    Integer moneyIn = brwActualOutAmt-profit;
                    if(moneyIn>0){
                        userAccountService.transferIn(cpsAccountName, moneyIn/100.0, BusinessEnum.ndr_subject_repay_cps_in, "借款人还代偿账户,标的:" + subject.getName(), "借款人还代偿账户，标的名称：" + subject.getName() + "，金额：" + moneyIn/100.0 , baseResponse.getRequestNo(),subject.getSubjectId(),scheduleId);
                    }
                    //借款人本地账户处理
                    userAccountService.tofreeze(brwForCpsLog.getBorrowerId(), brwActualOutAmt / 100.0, BusinessEnum.ndr_subject_repay_cps_in,
                            "借款人还代偿账户" , "借款人还代偿账户，标的：" + subject.getName() + "，金额：" + brwForCpsLog.getRepayAmt() / 100.0 , brwForCpsLog.getExtSn(),brwForCpsLog.getSubjectId(),brwForCpsLog.getScheduleId());
                    brwForCpsLogService.update(brwForCpsLog.getExtSn(), brwForCpsLog.getExtStatus(), BrwForCpsLog.STATUS_LOCAL, brwForCpsLog.getId());
                }
                //若已还完代偿,且交易成功
                if (SubjectRepaySchedule.CPS_STATUS_PAYOFF.equals(subjectRepaySchedule.getCpsStatus()) && BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                    cpsAcctLogService.log(subjectRepaySchedule.getId(), subjectRepaySchedule.getSubjectId(),
                            subjectRepaySchedule.getTerm(), subjectRepayBill.getId(), cpsAccountName, brwActualOutAmt, baseResponse.getRequestNo(), baseResponse.getStatus(), CompensatoryAcctLog.TYPE_CPS_RECORD_OUT);
                }
                //若借款人实还大于等于代偿金额
                if (amt >= 0 && SubjectRepaySchedule.CPS_STATUS_HAS_BEEN.equals(subjectRepaySchedule.getCpsStatus())) {
                    subjectRepaySchedule.setCpsStatus(SubjectRepaySchedule.CPS_STATUS_PAYOFF);
                }
                //借款人---->代偿账户
                subjectRepaySchedule.setRepayPrincipal(subjectRepaySchedule.getRepayPrincipal() + repayPrincipal + deratePrincipal);
                subjectRepaySchedule.setRepayInterest(subjectRepaySchedule.getRepayInterest() + repayInterest + derateInterest);
                subjectRepaySchedule.setRepayPenalty(subjectRepaySchedule.getRepayPenalty() + repayPenalty + deratePenalty);
                subjectRepaySchedule.setRepayFee(subjectRepaySchedule.getRepayFee() + repayFee + derateFee);
                //更新代偿流水终态
                List<CompensatoryAcctLog> logs = cpsAcctLogService.getCpsAcctLogsByStatusAndScheduleId(scheduleId,CompensatoryAcctLog.STATUS_HANDLED_LOCAL_FREEZE, BaseResponse.STATUS_SUCCEED);
                if(logs!=null && logs.size()>0){
                    for (CompensatoryAcctLog log:logs) {
                        cpsAcctLogService.updateByStatusAndId(CompensatoryAcctLog.STATUS_HANDLED_LOCAL_TOFREEZE,log.getId());
                    }
                }
            }else{
                //未代偿
                logger.info("还款文件数据异常,repayBillId-{}",subjectRepayBill.getId());
                return;
            }
        }
        subjectRepayScheduleService.update(subjectRepaySchedule);
        subjectRepayBillService.update(scheduleId, subjectId, SubjectRepayBill.STATUS_REPAY, subjectRepayBill.getId());
    }

}
