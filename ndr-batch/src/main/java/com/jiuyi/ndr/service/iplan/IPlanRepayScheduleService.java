package com.jiuyi.ndr.service.iplan;

import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.iplan.IPlanRepayScheduleDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanRepaySchedule;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.jiuyi.ndr.util.FinanceCalcUtils.*;

/**
 * Created by zhangyibo on 2017/6/13.
 */
@Service
public class IPlanRepayScheduleService {

    public static final Logger logger = LoggerFactory.getLogger(IPlanRepayScheduleService.class);

    @Autowired
    private IPlanRepayScheduleDao iPlanRepayScheduleDao;

    @Autowired
    private IPlanDao iPlanDao;

    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;

    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private CreditService creditService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private SubjectRepayDetailDao subjectRepayDetailDao;

    /**
     * 生成定期计划还款计划
     */
    public void genIPlanRepaySchedule(IPlan iPlan) {
        int iplanId = iPlan.getId();
        logger.info("开始生成iplanId={}的定期计划的还款计划", iplanId);
        String repayType = iPlan.getRepayType();
        CalcResult calcResult;
        Integer realQuota = iPlanTransLogDao.getRealQuota(iplanId);//实际募集金额;
        if(realQuota==null){
            return;
        }
        if (IPlan.REPAY_TYPE_IFPA.equals(repayType)||IPlan.REPAY_TYPE_MCEI.equals(repayType)) {
            calcResult = calcIFPA(realQuota, iPlan.getFixRate(), iPlan.getTerm());
        } else if (IPlan.REPAY_TYPE_OTRP.equals(repayType)) {
            //天标月月盈新增逻辑开始
            int calcDays;
            if (IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())) {
                calcDays = iPlan.getDay();
            } else {
                calcDays = iPlan.getTerm() * GlobalConfig.ONEMONTH_DAYS;
            }
            calcResult = FinanceCalcUtils.calcOTRP(realQuota, iPlan.getFixRate(), calcDays, iPlan.getInterestAccrualType());
            //天标月月盈新增逻辑结束
        } else {
            logger.error("不存在的还款类型{},iplanId={}", repayType,iplanId);
            throw new ProcessException(Error.NDR_0102);
        }
        for (Map.Entry<Integer, CalcResult.Detail> detailEntry : calcResult.getDetails().entrySet()) {
            CalcResult.Detail detail = detailEntry.getValue();
            Integer term = detailEntry.getKey();
            IPlanRepaySchedule iPlanRepaySchedule = new IPlanRepaySchedule();
            iPlanRepaySchedule.setIplanId(iplanId);
            iPlanRepaySchedule.setTerm(term);
            //天标还款日修改开始
            iPlanRepaySchedule.setDueDate(getDueDateByTerm(iPlan, term));
            //天标还款日修改结束
            iPlanRepaySchedule.setDuePrincipal(detail.getMonthRepayPrincipal());
            iPlanRepaySchedule.setDueInterest(detail.getMonthRepayInterest());
            iPlanRepaySchedule.setStatus(IPlanRepaySchedule.STATUS_NOT_REPAY);
            iPlanRepaySchedule.setCreateTime(DateUtil.getCurrentDateTime19());
            iPlanRepayScheduleDao.insert(iPlanRepaySchedule);
        }

        //计算递增利率省心投每个用户的预期收益
        if (isNewIplan(iPlan)) {
            List<IPlanAccount> iPlanAccounts = iPlanAccountService.getIPlanAccounts(iplanId);
            for (IPlanAccount iPlanAccount : iPlanAccounts) {
                //更新预期收益
                iPlanAccountService.calcInterest(iPlanAccount,iPlan);
                iPlanAccountService.update(iPlanAccount);
            }
        }
    }

    public boolean isNewIplan(IPlan iPlan) {
        return iPlan.getRateType() != null && iPlan.getRateType() == 1 && iPlan.getIncreaseRate() != null && iPlan.getIncreaseRate().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 查询理财计划的剩余期数
     *
     * @param iPlanId   理财计划id
     */
    public Integer getCurrentRepayTerm(Integer iPlanId) {
        List<IPlanRepaySchedule> repayScheduleList = this.getRepaySchedule(iPlanId);
        int totalTerm = repayScheduleList.size();
        for (IPlanRepaySchedule iPlanRepaySchedule : repayScheduleList) {
            Integer term = iPlanRepaySchedule.getTerm();
            String dueDate = iPlanRepaySchedule.getDueDate();//当期还款日
            LocalDate localDate = DateUtil.parseDate(dueDate, DateUtil.DATE_TIME_FORMATTER_10);//2017-06-20
            if (LocalDate.now().isBefore(localDate)) {
                return totalTerm-term;
            }
        }
        return null;
    }

    public List<IPlanRepaySchedule> getRepaySchedule(Integer iPlanId) {
        if (iPlanId == null) {
            throw new IllegalArgumentException("iPlan id is can not null when query iPlan repay schedule");
        }
        return iPlanRepayScheduleDao.findByIPlanId(iPlanId);
    }

    /**
     * 根据期数获取还款日
     *
     * @param iPlan 理财计划
     * @param term  期数
     * @return 还款日
     */
    public String getDueDateByTerm(IPlan iPlan, int term) {
        //开始计息日期
        LocalDate calcInterestDate = DateUtil.parseDateTime(iPlan.getRaiseFinishTime(), DateUtil.DATE_TIME_FORMATTER_19).toLocalDate().plusDays(1);
        if (IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())) {
            return calcInterestDate.plusDays(iPlan.getDay()).format(DateUtil.DATE_TIME_FORMATTER_10);
        } else {
            return calcInterestDate.plusMonths(term).format(DateUtil.DATE_TIME_FORMATTER_10);
        }
    }

    /**
     * 获得当前期数
     * @param iplanId
     * @return
     */
    public Integer getCurrentTermByIplanId(Integer iplanId,String date ){
        List<IPlanRepaySchedule> iplanRepaySchedules = this.getRepaySchedule(iplanId);
        LocalDate now = DateUtil.parseDate(date, DateUtil.DATE_TIME_FORMATTER_8);
        for(IPlanRepaySchedule iplanRepaySchedule : iplanRepaySchedules){
            LocalDate dueDate = DateUtil.parseDate(iplanRepaySchedule.getDueDate(), DateUtil.DATE_TIME_FORMATTER_10);
            if(now.isBefore(dueDate) || now.equals(dueDate)){
                return iplanRepaySchedule.getTerm();
            }else if(now.isAfter(dueDate) && dueDate.getMonthValue()==2 && now.getMonthValue()==3 && now.getDayOfMonth()==1 && dueDate.getDayOfMonth()>27){
                return iplanRepaySchedule.getTerm();
            }
        }
        return null;
    }

}
