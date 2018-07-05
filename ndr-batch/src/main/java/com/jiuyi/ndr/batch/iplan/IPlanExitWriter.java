package com.jiuyi.ndr.batch.iplan;

import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.*;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.*;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zhangyibo on 2017/6/23.
 */
public class IPlanExitWriter implements ItemWriter<IPlanRepaySchedule> {

    private static final Logger logger = LoggerFactory.getLogger(IPlanExitWriter.class);

    @Autowired
    private IPlanAccountDao iPlanAccountDao;

    @Autowired
    private CreditDao creditDao;

    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;

    @Autowired
    private CreditService creditService;

    @Autowired
    private IPlanRepayDetailDao iPlanRepayDetailDao;

    @Autowired
    private IPlanRepayScheduleDao iPlanRepayScheduleDao;

    @Autowired
    private IPlanDao iPlanDao;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private CreditOpeningDao creditOpeningDao;

    @Override
    public void write(List<? extends IPlanRepaySchedule> iPlanRepaySchedules) throws Exception {
        logger.info("开始进行定期计划到期退出");
        exit((List<IPlanRepaySchedule>) iPlanRepaySchedules);
        logger.info("定期计划到期退出完成");
    }

    private void exit(List<IPlanRepaySchedule> iPlanRepaySchedules){
        for(IPlanRepaySchedule iPlanRepaySchedule:iPlanRepaySchedules){
            IPlan iPlan = iPlanDao.findByIdForUpdate(iPlanRepaySchedule.getIplanId());
            if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                logger.info("此项目为省心投项目-{},不进行退出",iPlan.getId());
                continue;
            }
            if(!iPlan.getEndTime().substring(0,10).equals(iPlanRepaySchedule.getDueDate())){
                //防御性判断 防止sql查出的数据不对 导致正常还款变为结清
                logger.info("理财计划到期数据有误！理财计划结束时间为:{},当前还款的还款时间为{}",iPlan.getEndTime(),iPlanRepaySchedule.getDueDate());
                continue;
            }
            List<IPlanRepayDetail> iPlanRepayDetails = iPlanRepayDetailDao.findByRepayScheduleIdNotRepay(iPlanRepaySchedule.getId());
            if(iPlanRepayDetails==null||iPlanRepayDetails.isEmpty()){
                //如果该还款计划下已经没有人在投资了 那就直接将理财计划还款状态置为已还款
                iPlanRepaySchedule.setUpdateTime(DateUtil.getCurrentDateTime19());
                iPlanRepaySchedule.setRepayDate(DateUtil.getCurrentDateShort());
                iPlanRepaySchedule.setRepayTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                iPlanRepayScheduleDao.update(iPlanRepaySchedule);
                continue;
            }
            for(IPlanRepayDetail iPlanRepayDetail:iPlanRepayDetails){
                IPlanAccount iPlanAccount = iPlanAccountDao.findByUserIdAndIPlanIdForUpdate(iPlanRepayDetail.getUserId(),iPlanRepayDetail.getIplanId());

                if(!checkRepayAmount(iPlanAccount,iPlan,iPlanRepayDetail)){
                    logger.error("理财计划退出时金额校验有误,iplanAccount.id={},iplan.id={},iplanRepayDetail.id={}",iPlanAccount.getId(),iPlan.getId(),iPlanRepayDetail.getId());
                    continue;
                }
                if(checkCreditOpening(iPlanAccount)){
                    logger.error("该账户存在未放款的债权,iplanAccount.id={}",iPlanAccount.getId());
                    continue;
                }

                if(iPlanRepayDetail.getCurrentStep()==null) {
                    iPlanRepayDetail.setCurrentStep(IPlanRepayDetail.CURRENT_STEP_PAY_OFF);
                }

                if(IPlanRepayDetail.CURRENT_STEP_PAY_OFF.equals(iPlanRepayDetail.getCurrentStep())) {
                    //理财计划退出
                    iplanExit(iPlanAccount);
                    iPlanRepayDetail.setCurrentStep(IPlanRepayDetail.CURRENT_STEP_REPAY_FINISH);
                    iPlanRepayDetail.setUpdateTime(DateUtil.getCurrentDateTime19());
                    iPlanRepayDetail.setRepayPrincipal(iPlanRepayDetail.getDuePrincipal());
                    iPlanRepayDetail.setRepayInterest(iPlanRepayDetail.getDueInterest());
                    iPlanRepayDetail.setRepayBonusInterest(iPlanRepayDetail.getDueBonusInterest());
                    iPlanRepayDetail.setRepayVipInterest(iPlanRepayDetail.getDueVipInterest());
                    iPlanRepayDetail.setUpdateTime(DateUtil.getCurrentDateTime19());
                    iPlanRepayDetailDao.update(iPlanRepayDetail);
                }
            }
        }
    }

    private boolean checkCreditOpening(IPlanAccount iPlanAccount) {
        List<CreditOpening> creditOpenings = creditOpeningDao.findBySourceAccountIdAndStatusNot(iPlanAccount.getId(), CreditOpening.SOURCE_CHANNEL_IPLAN,CreditOpening.STATUS_LENDED);
        if (creditOpenings.stream().anyMatch(creditOpening -> creditOpening.getAvailablePrincipal()!=0)) {
            logger.info("转出账户 {} 用户Id- {} 还有剩余金额,跳过...", iPlanAccount.getId(), iPlanAccount.getUserId());
            return true;
        }
        if (creditOpenings!=null&&creditOpenings.size()>0){
            for (CreditOpening creditOpening:creditOpenings) {
                List<Credit> credits = creditDao.findByTargetIdAndTarget(creditOpening.getId(),Credit.TARGET_CREDIT);
                return credits.stream().anyMatch(credit -> credit.getCreditStatus().equals(Credit.CREDIT_STATUS_WAIT));
            }

        }
        return false;
    }


    /**
     * 定期计划退出
     * @param iPlanAccount
     */
    private void iplanExit(IPlanAccount iPlanAccount) {
        if(creditDao.findBySourceAccountId(iPlanAccount.getId(),1).stream().anyMatch(credit -> Credit.CREDIT_STATUS_WAIT==credit.getCreditStatus())){
            logger.warn("定期账户{}下还存在未确认的债权，咱不能进行理财计划退出操作");
            return;
        }
        int amtToInvest = 0 ;
        //查询一下该账户下是否还有本金复投的转入记录
        List<IPlanTransLog> iPlanTransLogs = iPlanTransLogDao.findByAccountIdAndTransTypePending(iPlanAccount.getId(),IPlanTransLog.TRANS_TYPE_PRINCIPLE_REINVEST);
        if(iPlanTransLogs!=null&&iPlanTransLogs.size()>0){
            for(IPlanTransLog iPlanTransLog:iPlanTransLogs){
                amtToInvest += (iPlanTransLog.getTransAmt()-iPlanTransLog.getProcessedAmt()) ;
                iPlanTransLog.setProcessedAmt(iPlanTransLog.getTransAmt());
                iPlanTransLog.setTransStatus(IPlanTransLog.TRANS_STATUS_SUCCEED);
                iPlanTransLog.setUpdateTime(DateUtil.getCurrentDateTime19());
                iPlanTransLogDao.update(iPlanTransLog);
            }
        }
        //说明是最后一期 到期退出 需要将理财计划下的债权全部转出
        Set<Integer> creditStatus = new HashSet<>();
        creditStatus.add(Credit.CREDIT_STATUS_HOLDING);
        List<Credit> credits = creditDao.findByUserIdAndCreditStatusAndSourceChannelAndSourceAccountId(iPlanAccount.getUserId(),creditStatus,Credit.SOURCE_CHANNEL_IPLAN,iPlanAccount.getId());
        //Map<Credit,Integer> paramMap = new HashMap<>(credits.size());
        Map<String, List<Credit>> collect = credits.stream().collect(Collectors.groupingBy(Credit::getSubjectId));
        /*for(Credit credit:credits){
            if(credit.getHoldingPrincipal()>0){
                paramMap.put(credit,credit.getHoldingPrincipal());
            }
        }*/
        int principal = iPlanAccount.getCurrentPrincipal();
        List<IPlanRepayDetail> iPlanRepayDetails = iPlanRepayDetailDao.findByUserIdAndIPlanId(iPlanAccount.getUserId(),iPlanAccount.getIplanId());//取最后一期repayDetail计算出利息
        IPlanRepayDetail lastIPlanRepayDetail = iPlanRepayDetails.get(iPlanRepayDetails.size()-1);
        int interest = lastIPlanRepayDetail.getDueInterest();
        int bonusInterest = lastIPlanRepayDetail.getDueBonusInterest();
        int vipInterest = lastIPlanRepayDetail.getDueVipInterest();
        //新增一条转出
        IPlanTransLog iPlanTransLog = saveIPlanTransLog(iPlanAccount,IPlanTransLog.TRANS_TYPE_NORMAL_EXIT,principal+interest+bonusInterest+vipInterest,"到期退出转出",IPlanTransLog.TRANS_STATUS_PROCESSING,0);
        //更新账户中的amtToTransfer金额
        iPlanAccount = iPlanAccountDao.findByAccountIdForUpdate(iPlanAccount.getId());//加锁
        Integer freezeAmtToInvest = iPlanAccount.getFreezeAmtToInvest()==null? 0 : iPlanAccount.getFreezeAmtToInvest();
             amtToInvest += freezeAmtToInvest;
        if (iPlanAccount.getAmtToInvest()!=amtToInvest){
            logger.info("月月盈账户：{},修改前待复投本金：{},本金：{}",iPlanAccount.getId(),iPlanAccount.getAmtToInvest(),amtToInvest);
            iPlanAccount.setAmtToInvest(amtToInvest);
        }
        iPlanAccount.setAmtToTransfer(iPlanAccount.getAmtToTransfer()+principal+interest);
//      iPlanAccount.setStatus(IPlanAccount.STATUS_NORMAL_EXIT);
        iPlanAccount.setCurrentPrincipal(0);
        iPlanAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
        iPlanAccountDao.update(iPlanAccount);
        creditService.creditTransfer(collect, BigDecimal.ONE,iPlanTransLog.getId(),iPlanAccount.getInvestRequestNo(),iPlanAccount.getUserId(),iPlanAccount.getId());
        iPlanRepayDetailDao.update(lastIPlanRepayDetail);
    }


    private IPlanTransLog saveIPlanTransLog(IPlanAccount iPlanAccount,Integer transType,Integer amount,String transDesc,Integer transStatus,Integer processedAmt){
        IPlanTransLog iPlanTransLog = new IPlanTransLog();
        iPlanTransLog.setAccountId(iPlanAccount.getId());
        iPlanTransLog.setUserId(iPlanAccount.getUserId());
        iPlanTransLog.setIplanId(iPlanAccount.getIplanId());
        iPlanTransLog.setTransType(transType);
        iPlanTransLog.setFlag(IPlanTransLog.FLAG_PT);
        iPlanTransLog.setTransAmt(amount);
        iPlanTransLog.setProcessedAmt(processedAmt);
        iPlanTransLog.setTransTime(DateUtil.getCurrentDateTime19()) ;
        iPlanTransLog.setTransDesc(transDesc);
        iPlanTransLog.setTransStatus(transStatus);
        iPlanTransLogDao.insert(iPlanTransLog);
        return iPlanTransLog;
    }

    private boolean checkRepayAmount(IPlanAccount iPlanAccount,IPlan iPlan,IPlanRepayDetail iPlanRepayDetail){
        FinanceCalcUtils.CalcResult calcResult = null;
        if(IPlan.REPAY_TYPE_IFPA.equals(iPlan.getRepayType())){
            calcResult = FinanceCalcUtils.calcIFPA(iPlanAccount.getInitPrincipal(), iPlan.getFixRate(), iPlan.getTerm());
        }else if(IPlan.REPAY_TYPE_OTRP.equals(iPlan.getRepayType())){
            int calcDays = 0;
            if (IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
                calcDays = iPlan.getDay();
            } else {
                calcDays = iPlan.getTerm()*GlobalConfig.ONEMONTH_DAYS;
            }
            calcResult = FinanceCalcUtils.calcOTRP(iPlanAccount.getInitPrincipal(),iPlan.getFixRate(),calcDays,iPlan.getInterestAccrualType());
        }
        if(calcResult!=null){
            FinanceCalcUtils.CalcResult.Detail detail = calcResult.getDetails().get(iPlanRepayDetail.getTerm());
            if(Math.abs(detail.getMonthRepayPrincipal()-iPlanRepayDetail.getDuePrincipal())>2
                    ||Math.abs(detail.getMonthRepayInterest()-iPlanRepayDetail.getDueInterest())>2){
                //如果两边误差大于2 说明有问题
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

}
