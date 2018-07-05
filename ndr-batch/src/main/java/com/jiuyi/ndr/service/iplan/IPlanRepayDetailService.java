package com.jiuyi.ndr.service.iplan;

import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.dao.iplan.IPlanRepayDetailDao;
import com.jiuyi.ndr.dao.iplan.IPlanRepayScheduleDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanRepayDetail;
import com.jiuyi.ndr.domain.iplan.IPlanRepaySchedule;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.jiuyi.ndr.batch.iplan.IPlanRaisedFinishWriter.logger;

/**
 * Created by zhangyibo on 2017/6/16.
 */
@Service
public class IPlanRepayDetailService {

    @Autowired
    private IPlanRepayScheduleDao iPlanRepayScheduleDao;

    @Autowired
    private IPlanRepayDetailDao iPlanRepayDetailDao;

    @Autowired
    private IPlanAccountDao iPlanAccountDao;

    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;

    /**
     * 根据定期理财计划的还款计划生成对应的每个人的还款计划并插入数据库中
     *
     * @param iPlan
     */
    public void genRepayDetail(IPlan iPlan) {
        int iplanId = iPlan.getId();
        //实际募集金额;
        Integer realQuota = iPlanTransLogDao.getRealQuota(iplanId);
        List<IPlanRepaySchedule> iPlanRepaySchedules = iPlanRepayScheduleDao.findByIPlanId(iplanId);
        List<IPlanAccount> iPlanAccounts = iPlanAccountDao.findByIPlanId(iplanId);
        for (IPlanRepaySchedule iPlanRepaySchedule : iPlanRepaySchedules) {
            for (IPlanAccount iPlanAccount : iPlanAccounts) {
                if (iPlanAccount.getCurrentPrincipal() == 0) {
                    logger.info("定期理财计划账户{}持有本金{}，不在生成还款计划", iPlanAccount.getId(), iPlanAccount.getCurrentPrincipal());
                    continue;
                }
                IPlanRepayDetail iPlanRepayDetail = new IPlanRepayDetail();
                iPlanRepayDetail.setIplanId(iPlanRepaySchedule.getIplanId());
                iPlanRepayDetail.setUserId(iPlanAccount.getUserId());
                iPlanRepayDetail.setRepayScheduleId(iPlanRepaySchedule.getId());
                iPlanRepayDetail.setTerm(iPlanRepaySchedule.getTerm());
                iPlanRepayDetail.setDueDate(iPlanRepaySchedule.getDueDate());
                Integer currentPrincipal = iPlanAccount.getCurrentPrincipal();
                //应付本金=理财计划应付本金*当前账户投资该理财计划的本金/理财计划额度
                Integer duePrincipal = BigDecimal.valueOf(iPlanRepaySchedule.getDuePrincipal())
                        .multiply(BigDecimal.valueOf(currentPrincipal)).divide(BigDecimal.valueOf(realQuota), 8, BigDecimal.ROUND_DOWN).intValue();
                iPlanRepayDetail.setDuePrincipal(duePrincipal);

                Integer dueInterest = BigDecimal.valueOf(iPlanRepaySchedule.getDueInterest())
                        .multiply(BigDecimal.valueOf(currentPrincipal)).divide(BigDecimal.valueOf(realQuota), 8, BigDecimal.ROUND_DOWN).intValue();
                iPlanRepayDetail.setDueInterest(dueInterest);

                FinanceCalcUtils.CalcResult.Detail detail;
                FinanceCalcUtils.CalcResult.Detail vipDetail;
                if (IPlan.REPAY_TYPE_IFPA.equals(iPlan.getRepayType())) {
                    detail = FinanceCalcUtils.calcIFPA(currentPrincipal, iPlan.getBonusRate(), iPlan.getTerm()).getDetails().get(iPlanRepaySchedule.getTerm());
                    vipDetail = FinanceCalcUtils.calcIFPA(currentPrincipal, iPlanAccount.getVipRate(), iPlan.getTerm()).getDetails().get(iPlanRepaySchedule.getTerm());
                } else if (IPlan.REPAY_TYPE_OTRP.equals(iPlan.getRepayType())) {
                    //区分天标月月盈开始
                    detail = calDetail(iPlan, iPlan.getBonusRate(), currentPrincipal, iPlanRepaySchedule.getTerm());
                    vipDetail = calDetail(iPlan, iPlanAccount.getVipRate(), currentPrincipal, iPlanRepaySchedule.getTerm());
                    //区分天标月月盈结束
                } else {
                    throw new ProcessException(Error.INTERNAL_ERROR);
                }
                Integer dueBonusInterest = detail.getMonthRepayInterest();
                iPlanRepayDetail.setDueBonusInterest(dueBonusInterest);
                Integer dueVipInterest = vipDetail.getMonthRepayInterest();
                iPlanRepayDetail.setDueVipInterest(dueVipInterest);
                iPlanRepayDetail.setStatus(IPlanRepayDetail.STATUS_NOT_REPAY);
                iPlanRepayDetail.setRepayPrincipal(0);
                iPlanRepayDetail.setRepayInterest(0);
                iPlanRepayDetail.setRepayBonusInterest(0);
                iPlanRepayDetail.setRepayVipInterest(0);
                iPlanRepayDetail.setCreateTime(DateUtil.getCurrentDateTime19());
                iPlanRepayDetailDao.insert(iPlanRepayDetail);
            }
        }

        for (IPlanAccount iPlanAccount : iPlanAccounts) {
            List<IPlanRepayDetail> iPlanRepayDetails = iPlanRepayDetailDao.findByUserIdAndIPlanId(iPlanAccount.getUserId(), iplanId);
            iPlanAccount.setExpectedInterest(iPlanRepayDetails.stream().map(IPlanRepayDetail::getDueInterest).reduce(Integer::sum).orElse(0));
            iPlanAccount.setIplanExpectedBonusInterest(iPlanRepayDetails.stream().map(IPlanRepayDetail::getDueBonusInterest).reduce(Integer::sum).orElse(0));
            iPlanAccount.setIplanExpectedVipInterest(iPlanRepayDetails.stream().map(IPlanRepayDetail::getDueVipInterest).reduce(Integer::sum).orElse(0));
            iPlanAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
            iPlanAccountDao.update(iPlanAccount);
        }
    }

    /**
     * 区分天标月月盈和普通月月盈
     *
     * @param iPlan 理财计划
     * @param rate  年利率
     * @param money 金额
     * @param term  期数
     * @return 明细
     */
    private FinanceCalcUtils.CalcResult.Detail calDetail(IPlan iPlan, BigDecimal rate, Integer money, int term) {
        int calcDays;
        if (IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())) {
            calcDays = iPlan.getDay();
        } else {
            calcDays = iPlan.getTerm() * GlobalConfig.ONEMONTH_DAYS;
        }
        return FinanceCalcUtils.calcOTRP(money, rate, calcDays, iPlan.getInterestAccrualType()).getDetails().get(term);
    }

}
