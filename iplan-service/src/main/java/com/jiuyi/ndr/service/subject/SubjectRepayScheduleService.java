package com.jiuyi.ndr.service.subject;

import com.alibaba.fastjson.JSONObject;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.redpacket.ActivityMarkConfigureDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayScheduleDao;
import com.jiuyi.ndr.dao.subject.SubjectTransferParamDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.subject.*;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.dto.subject.mobile.SubjectAppRepayDetailDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.account.PlatformTransferService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanRepayScheduleService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.iplan.IPlanTransLogService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 标的还款计划服务
 * Created by lixiaolei on 2017/4/11.
 */
@Service
public class SubjectRepayScheduleService {

    private final static Logger logger = LoggerFactory.getLogger(SubjectRepayScheduleService.class);

    @Autowired
    private SubjectRepayScheduleDao subjectRepayScheduleDao;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectOverdueDefService subjectOverdueDefService;
    @Autowired
    private SubjectAdvancedPayOffService subjectAdvancedPayOffService;

    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private SubjectPayoffRegService payoffRegService;
    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private CreditOpeningDao creditOpeningDao;
    @Autowired
    private CreditService creditService;
    @Autowired
    private PlatformTransferService platformTransferService;
    @Autowired
    private CreditOpeningService creditOpeningService;
    @Autowired
    private CreditDao creditDao;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private ActivityMarkConfigureDao activityMarkConfigureDao;
    @Autowired
    private IPlanRepayScheduleService iPlanRepayScheduleService;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private IPlanTransLogService iPlanTransLogService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private SubjectTransferParamDao subjectTransferParamDao;
    @Autowired
    private SubjectTransLogService subjectTransLogService;

    /**
     * 生成还款计划
     */
    @Transactional
    public void makeUpRepaySchedule(Subject subject) {
        String dateTimeNow = DateUtil.getCurrentDateTime();

        Integer contractAmt = subject.getTotalAmt();
        BigDecimal rate = subject.getRate();//借款年利率
        Integer termMonth = subject.getPeriod() >= GlobalConfig.ONEMONTH_DAYS ? subject.getTerm() : 1;

        FinanceCalcUtils.CalcResult calcResult = null;
        if (Subject.REPAY_TYPE_OTRP.equals(subject.getRepayType()) || termMonth.equals(1)) {//针对一期的标都使用一次还本付息拆标
            calcResult = FinanceCalcUtils.calcOTRP(contractAmt, rate, subject.getPeriod());
        } else if (Subject.REPAY_TYPE_MCEI.equals(subject.getRepayType())) {
            calcResult = FinanceCalcUtils.calcMCEI(contractAmt, rate, termMonth);
        } else if (Subject.REPAY_TYPE_IFPA.equals(subject.getRepayType())) {
            calcResult = FinanceCalcUtils.calcIFPA(contractAmt, rate, termMonth);
        } else if (Subject.REPAY_TYPE_MCEP.equals(subject.getRepayType())) {
            calcResult = FinanceCalcUtils.calcMCEP(contractAmt, rate, termMonth);
        } else {
            throw new IllegalArgumentException("不支持的还款类型！");
        }

        for (int m = 1; m <= termMonth; m++) {
            SubjectRepaySchedule subjectRepaySchedule = new SubjectRepaySchedule();
            FinanceCalcUtils.CalcResult.Detail detail = calcResult.getDetails().get(m);
            subjectRepaySchedule.setSubjectId(subject.getSubjectId());
            subjectRepaySchedule.setTerm(m);
            String yearMonth = plusMonths(m);
            subjectRepaySchedule.setDueDate(yearMonth + day(yearMonth));
            subjectRepaySchedule.setDuePrincipal(detail.getMonthRepayPrincipal());
            subjectRepaySchedule.setDueInterest(detail.getMonthRepayInterest());
            subjectRepaySchedule.setDuePenalty(0);
            subjectRepaySchedule.setDueFee(0);
            subjectRepaySchedule.setStatus(SubjectRepaySchedule.STATUS_NOT_REPAY);
            subjectRepaySchedule.setCreateTime(DateUtil.getCurrentDateTime19());

            subjectRepayScheduleDao.insert(subjectRepaySchedule);
        }

    }

    /**
     * 查询某标的某期还款计划
     *
     * @param subjectId 标的id
     * @param term      期数
     */
    public SubjectRepaySchedule findRepaySchedule(String subjectId, Integer term) {
        if (subjectId == null || term == null) {
            throw new IllegalArgumentException("subjectId or term is empty!");
        }
        return subjectRepayScheduleDao.findBySubjectIdAndTerm(subjectId, term);
    }

    /**
     * 查询某标的的所有还款计划
     *
     * @param subjectId 标的id
     */
    public List<SubjectRepaySchedule> findRepayScheduleBySubjectId(String subjectId) {
        return subjectRepayScheduleDao.findBySubjectIdOrderByTerm(subjectId);
    }

    /**
     * 根据还款状态查询还款计划
     *
     * @param status 还款状态
     */
    public List<SubjectRepaySchedule> findRepayScheduleByStatus(Integer status) {
        return subjectRepayScheduleDao.findByStatusOrderBySubjectId(status);
    }


    /**
     * 逾期更新还款计划，累加罚息
     *
     * @param scheduleId    还款计划id
     */
    @Transactional
    public SubjectRepaySchedule updateRepayScheduleCauseOverdue(Integer scheduleId) {
        SubjectRepaySchedule subjectRepaySchedule = subjectRepayScheduleDao.findById(scheduleId);
        Subject subject = subjectService.getBySubjectId(subjectRepaySchedule.getSubjectId());
        SubjectOverduePenaltyDef subjectOverduePenaltyDef = subjectOverdueDefService.findOverdueDefById(subject.getOverduePenalty());
        List<SubjectOverduePenaltyDef.OverdueDefDetail> overdueDefDetails = JSONObject.parseArray(subjectOverduePenaltyDef.getOverduePenaltyDef(),
                SubjectOverduePenaltyDef.OverdueDefDetail.class);
        //拿罚息定义表的相关字段，累加还款计划表中的罚息
        long days = DateUtil.betweenDays(subjectRepaySchedule.getDueDate(), DateUtil.getCurrentDateShort());//逾期天数
        for (SubjectOverduePenaltyDef.OverdueDefDetail overdueDefDetail : overdueDefDetails) {
            if (days >= overdueDefDetail.getOverdueDayStart() && (days <= overdueDefDetail.getOverdueDayEnd() || days == Integer.MAX_VALUE)) {//如果在当期内
                Integer duePenalty = 0;
                if (SubjectOverduePenaltyDef.OverdueDefDetail.PENALTY_BASE_LOAN_AMT
                        .equalsIgnoreCase(overdueDefDetail.getPenaltyBase())) {//借款金额为罚息本金
                    duePenalty = subjectRepaySchedule.getDuePenalty()
                            + this.calcOverduePenaltyDaily(subject.getTotalAmt(), overdueDefDetail.getPenaltyRate());
                }
                if (SubjectOverduePenaltyDef.OverdueDefDetail.PENALTY_BASE_OVERDUE_PRINCIPAL
                        .equalsIgnoreCase(overdueDefDetail.getPenaltyBase())) {//逾期本金为罚息本金
                    duePenalty = subjectRepaySchedule.getDuePenalty()
                            + this.calcOverduePenaltyDaily(subjectRepaySchedule.getDuePrincipal(), overdueDefDetail.getPenaltyRate());
                }
                if (SubjectOverduePenaltyDef.OverdueDefDetail.PENALTY_BASE_OVERDUE_PRINCIPAL_INTEREST
                        .equalsIgnoreCase(overdueDefDetail.getPenaltyBase())) {//逾期本息为罚息本金
                    duePenalty = subjectRepaySchedule.getDuePenalty()
                            + this.calcOverduePenaltyDaily(subjectRepaySchedule.getDuePrincipal() + subjectRepaySchedule.getDueInterest()
                            , overdueDefDetail.getPenaltyRate());
                }
                subjectRepaySchedule.setDuePenalty(duePenalty);
                break;
            }
        }
        subjectRepaySchedule = this.update(subjectRepaySchedule);
        return subjectRepaySchedule;
    }

    /**
     * 获得当前期数
     */
    public Integer getCurrentTermRepaySchedule(List<SubjectRepaySchedule> subjectRepaySchedules){
        LocalDate now = LocalDate.now();
        for(SubjectRepaySchedule subjectRepaySchedule:subjectRepaySchedules){
            LocalDate dueDate = DateUtil.parseDate(subjectRepaySchedule.getDueDate(),DateUtil.DATE_TIME_FORMATTER_8);
            if(now.isBefore(dueDate)||now.equals(dueDate)){
                return subjectRepaySchedule.getTerm();
            }
        }
        return null;
    }


    /**
     * 获得当前期数
     */
    public Integer getCurrentTermRepaySchedule(String subjectId){
        List<SubjectRepaySchedule> subjectRepaySchedules = this.findRepayScheduleBySubjectId(subjectId);
        LocalDate now = LocalDate.now();
        for(SubjectRepaySchedule subjectRepaySchedule : subjectRepaySchedules){
            LocalDate dueDate = DateUtil.parseDate(subjectRepaySchedule.getDueDate(), DateUtil.DATE_TIME_FORMATTER_8);
            if(now.isBefore(dueDate) || now.equals(dueDate)){
                return subjectRepaySchedule.getTerm();
            }
        }
        return null;
    }

    /**
     * 根据传入时间获取期数
     */
    public Integer getTermRepaySchedule(String subjectId,String date){
        List<SubjectRepaySchedule> subjectRepaySchedules = this.findRepayScheduleBySubjectId(subjectId);
        for(SubjectRepaySchedule subjectRepaySchedule : subjectRepaySchedules){
            String dueDate = subjectRepaySchedule.getDueDate();
            if(date.compareTo(dueDate) <= 0){
                return subjectRepaySchedule.getTerm();
            }
        }
        return null;
    }

    /**
     * 根据传入时间获取期数不包括当天
     */
    public Integer getTermRepayScheduleNotToday(String subjectId,String date){
        List<SubjectRepaySchedule> subjectRepaySchedules = this.findRepayScheduleBySubjectId(subjectId);
        for(SubjectRepaySchedule subjectRepaySchedule : subjectRepaySchedules){
            String dueDate = subjectRepaySchedule.getDueDate();
            if(date.compareTo(dueDate) < 0){
                return subjectRepaySchedule.getTerm();
            }
        }
        return null;
    }
    /**
     * 获得当前还款schedule
     * @param subjectId
     * @return
     */
    public SubjectRepaySchedule getCurrentRepaySchedule(String subjectId){
        List<SubjectRepaySchedule> subjectRepaySchedules = subjectRepayScheduleDao.findBySubjectIdAndStatusOrderByTerm(subjectId);
        LocalDate now = LocalDate.now();
        for(SubjectRepaySchedule subjectRepaySchedule : subjectRepaySchedules){
            LocalDate dueDate = DateUtil.parseDate(subjectRepaySchedule.getDueDate(), DateUtil.DATE_TIME_FORMATTER_8);
            if(now.isBefore(dueDate) || now.equals(dueDate)){
                return subjectRepaySchedule;
            }
        }
        return null;
    }

    /**
     * 获得当前还款schedule的期数
     * @param subjectId
     * @return
     */
    public Integer getCurrentRepayScheduleTerm(String subjectId,String date){
        List<SubjectRepaySchedule> subjectRepaySchedules = subjectRepayScheduleDao.findBySubjectIdOrderByTerm(subjectId);
        LocalDate now = DateUtil.parseDate(date,DateUtil.DATE_TIME_FORMATTER_8);
        for(SubjectRepaySchedule subjectRepaySchedule : subjectRepaySchedules){
            LocalDate dueDate = DateUtil.parseDate(subjectRepaySchedule.getDueDate(), DateUtil.DATE_TIME_FORMATTER_8);
            if(now.isBefore(dueDate) || now.equals(dueDate)){
                return subjectRepaySchedule.getTerm();
            }
        }
        return null;
    }

    public List<SubjectRepaySchedule> findBySubjectIds(Set<String> subjectIds){
        return subjectRepayScheduleDao.findBySubjectIdIn(subjectIds);
    }

    public String setToString(Set<String> strings){

        StringBuffer sb = new StringBuffer();
        sb.append("(");
        for (String s : strings) {
            sb.append(s);
            sb.append(", ");
        }
        return sb.toString();
    }
    @ProductSlave
    public List<SubjectRepaySchedule> findByConditions(Map<String, String> conditions) {
        String subjectName = StringUtils.hasText(conditions.get("subjectName")) ? "%" + conditions.get("subjectName") + "%" : "%%";
        Integer isDirect = StringUtils.hasText(conditions.get("isDirect")) ? Integer.valueOf(conditions.get("isDirect")) : 1;
        String intermediatorId = conditions.get("intermediatorId");
        int openChannel = Integer.valueOf(conditions.get("openChannel"));
        Integer[] openChannels = null;
        switch (openChannel) {
            case 2 : openChannels = new Integer[] {4, 5, 6, 7}; break;
            case 1 : openChannels = new Integer[] {2, 3, 6, 7}; break;
            case 0 : openChannels = new Integer[] {1, 3, 5, 7}; break;
            default : throw new IllegalArgumentException("不存在的开放渠道！");
        }
        String startDate = StringUtils.hasText(conditions.get("startDate")) ? conditions.get("startDate") : "20170401";
        String endDate = StringUtils.hasText(conditions.get("endDate")) ? conditions.get("endDate") : "20291231";
        return subjectRepayScheduleDao.findByConditions(subjectName, isDirect, intermediatorId, openChannels, startDate, endDate, SubjectRepaySchedule.STATUS_NOT_REPAY, SubjectRepaySchedule.SIGN_NOT_REPAY);
    }

    //提前结清打标记
    public void markAdvanceSubject(String subjectId){
        Subject subject = subjectDao.findBySubjectId(subjectId);
        boolean isDirect = Subject.DIRECT_FLAG_YES.equals(subject.getDirectFlag())?true:false;
        String intermediatorId = subject.getIntermediatorId().trim();
        //罚息
        int payOffPenalty = subjectAdvancedPayOffService.advancedPayOff(subjectId);
        SubjectRepaySchedule subjectRepaySchedule = this.findRepaySchedule(subjectId, subject.getCurrentTerm());
        if (Subject.REPAY_PAYOFF.equals(subject.getRepayStatus().trim()) || Subject.REPAY_ADVANCED_PAYOFF.equals(subject.getRepayStatus().trim())) {
            //已还款完成，无需再还
            logger.warn("标的{}已到期结束或已提前结清，请勿重复还款", subjectId);
            return;
        }
        if (Subject.REPAY_OVERDUE.equals(subject.getRepayStatus().trim())) {
            //标的有逾期，需先还清逾期
            logger.warn("标的{}有逾期未还清，请先还清逾期", subjectId);
            return;
        }
        //未完成的转让中债权
        List<CreditOpening> unlendedCreditOpenings = creditOpeningDao.findNotLendedBySubjectId(subjectId);
        if (unlendedCreditOpenings != null && unlendedCreditOpenings.size() > 0) {
            //标的有未完成的转让中债权，不能还款
            logger.warn("标的{}有未完成的转让中债权，暂不能还款", subjectId);
            return;
        }

        //查询债权关系
        List<Credit> credits = creditService.findAllCreditBySubjectId(subjectId);
        if (credits.stream().anyMatch(credit -> credit.getCreditStatus().equals(Credit.CREDIT_STATUS_WAIT))) {
            //标的有未确认债权，不能还款
            logger.warn("标的{}有未确认债权，暂不能还款", subjectId);
            return;
        }
        //还款金额
        double amount = (subject.getTotalAmt()-subject.getPaidPrincipal()+subjectRepaySchedule.getDueInterest()+subjectRepaySchedule.getDuePenalty()+payOffPenalty+subjectRepaySchedule.getDueFee())/100.0;
        //若是直贷
        if(isDirect){
            //查看营销款账户资金是否足够
            double totalActualMoney = platformTransferService.selectTotalSctualMoneyByInterviewerId(intermediatorId);
            //PlatformAccount repayer = platformAccountService.getPlatformAccount(GlobalConfig.MARKETING_ACCOUNT_01_DR);
            if (amount > totalActualMoney) {
                throw new ProcessException(Error.NDR_0424.getCode(), Error.NDR_0424.getMessage() + ", 营销款账户" + GlobalConfig.MARKETING_ACCOUNT_01_DR);
            }
        }else{
            //查询居间人账户是否充足
            UserAccount repayer = userAccountService.findUserAccount(intermediatorId);
            if (amount > repayer.getAvailableBalance()) {
                throw new ProcessException(Error.NDR_0425.getCode(), Error.NDR_0425.getMessage() + ", 居间人" + intermediatorId);
            }
        }
        //更新payoff
        payoffRegService.onLinePayOff(subjectId);
        //打标记,将isRepay设为1
        subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_WAIT_REPAY);
        this.update(subjectRepaySchedule);
    }

    /**
     * 更新还款计划
     */
    public SubjectRepaySchedule update(SubjectRepaySchedule subjectRepaySchedule) {
        if (subjectRepaySchedule.getId() == null) {
            throw new IllegalArgumentException("更新还款计划，计划id不能为空");
        }
        subjectRepaySchedule.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectRepayScheduleDao.update(subjectRepaySchedule);
        return subjectRepaySchedule;
    }

    public SubjectRepaySchedule delete(SubjectRepaySchedule subjectRepaySchedule) {
        if (subjectRepaySchedule.getId() == null) {
            throw new IllegalArgumentException("删除还款计划，计划id不能为空");
        }
        subjectRepayScheduleDao.delete(subjectRepaySchedule);
        return subjectRepaySchedule;
    }

    /**
     * 每天罚息计算
     * 算法 ：基数 * 利率（年）/ 一年的天数（360）
     *
     * @param baseAmt 基数
     * @param rate 利率
     */
    private Integer calcOverduePenaltyDaily(Integer baseAmt, BigDecimal rate) {
        Integer penaltyDaily = new BigDecimal(baseAmt).multiply(rate)
                .divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS), 6, BigDecimal.ROUND_HALF_UP).intValue();
        return penaltyDaily;
    }

    /**
     * 返回还款日中的日
     *
     * @param yearMonthStr 还款日年月
     */
    private String day(String yearMonthStr) {
        YearMonth yearMonth = YearMonth.parse(yearMonthStr, DateTimeFormatter.ofPattern("yyyyMM"));
        int currentDay = Integer.valueOf(MonthDay.now().format(DateTimeFormatter.ofPattern("dd")));
        int day = yearMonth.lengthOfMonth() > currentDay ? currentDay : yearMonth.lengthOfMonth();
        return String.valueOf(day < 10 ? "0" + day : day);
    }

    /**
     * 当月累加monthsToAdd后是哪一年的哪一月
     *
     * 例：现在是2017/04月，monthsToAdd=3，返回2017/07月，monthsToAdd=12，返回2018/04月
     * @param monthsToAdd   累加数
     */
    private String plusMonths(int monthsToAdd) {
        return YearMonth.now().plusMonths(monthsToAdd).format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    //正常还款打标记
    public void markRepaySubject(String subjectId,Integer term){
        Subject subject = subjectDao.findBySubjectId(subjectId);
        SubjectRepaySchedule subjectRepaySchedule = this.findRepaySchedule(subjectId, term);
        boolean isDirect = Subject.DIRECT_FLAG_YES.equals(subject.getDirectFlag())?true:false;
        String intermediatorId = subject.getIntermediatorId().trim();
        //还款金额
        double amount = (subjectRepaySchedule.getDuePrincipal()+subjectRepaySchedule.getDueInterest()+subjectRepaySchedule.getDuePenalty()+subjectRepaySchedule.getDueFee())/100.0;
        if (SubjectRepaySchedule.STATUS_NORMAL_REPAID.equals(subjectRepaySchedule.getStatus())
                || SubjectRepaySchedule.STATUS_OVERDUE_REPAID.equals(subjectRepaySchedule.getStatus())
                || SubjectRepaySchedule.STATUS_ADVANCE_PAYOFF.equals(subjectRepaySchedule.getStatus())) {
            //已还款完成，无需再还
            logger.warn("标的{}第{}期已还款完成，请勿重复还款", subjectId, subjectRepaySchedule.getTerm());
            return;
        }
        //未完成的转让中债权
        List<CreditOpening> unlendedCreditOpenings = creditOpeningDao.findNotLendedBySubjectId(subjectId);
        if (unlendedCreditOpenings != null && unlendedCreditOpenings.size() > 0) {
            //标的有未完成的转让中债权，不能还款
            logger.warn("标的{}有未完成的转让中债权，暂不能还款", subjectId);
            return;
        }
        //查询债权关系
        List<Credit> credits = creditService.findAllCreditBySubjectId(subjectId);
        if (credits.stream().anyMatch(credit -> credit.getCreditStatus().equals(Credit.CREDIT_STATUS_WAIT))) {
            //标的有未确认债权，不能还款
            logger.warn("标的{}有未确认债权，暂不能还款", subjectId);
            return;
        }
        //若是直贷
        if(isDirect){
            //查看营销款账户资金是否足够
            double totalActualMoney = platformTransferService.selectTotalSctualMoneyByInterviewerId(intermediatorId);
            //PlatformAccount repayer = platformAccountService.getPlatformAccount(GlobalConfig.MARKETING_ACCOUNT_01_DR);
            if (amount > totalActualMoney) {
                throw new ProcessException(Error.NDR_0424.getCode(), Error.NDR_0424.getMessage() + ", 营销款账户" + GlobalConfig.MARKETING_ACCOUNT_01_DR);
            }
        }else{
            //查询居间人账户是否充足
            UserAccount repayer = userAccountService.findUserAccount(intermediatorId);
            if (amount > repayer.getAvailableBalance()) {
                throw new ProcessException(Error.NDR_0425.getCode(), Error.NDR_0425.getMessage() + ", 居间人" + intermediatorId);
            }
        }
        //打标记,将isRepay设为1
        subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_WAIT_REPAY);
        this.update(subjectRepaySchedule);
    }

    public List<SubjectRepaySchedule> getSubjectRepayScheduleBySubjectIdNotRepay(String subjectId){
        return subjectRepayScheduleDao.findSubjectRepayScheduleBySubjectIdNotRepay(subjectId);
    }

    public List<SubjectRepaySchedule> findSubjectRepayScheduleBySubjectIdAndTermNotRepay(String subjectId, int term){
        return subjectRepayScheduleDao.findSubjectRepayScheduleBySubjectIdAndTermNotRepay(subjectId, term);
    }

    /**
     * 计算当期应还本金利息
     * @param subjectRepaySchedule
     * @param userId
     * @param credit
     * @return
     */
    public SubjectRepayDetail repayDetailBySchedule(SubjectRepaySchedule subjectRepaySchedule,String userId,Credit credit,Integer principal,Integer totalPrincipal){
        String subjectId = subjectRepaySchedule.getSubjectId();
        Subject subject = subjectService.findSubjectBySubjectId(subjectRepaySchedule.getSubjectId());
        Integer term = subjectRepaySchedule.getTerm();
        SubjectRepaySchedule previousSchedule = null;
        if (term > 1) {
            previousSchedule = this.findRepaySchedule(subjectId, term - 1);
        }
        String holdDate = credit.getStartTime().substring(0, 8);
        //起始日期=持有日期
        String startDate = holdDate;
        //最后日期=还款日
        String endDate = subjectRepaySchedule.getDueDate();
        if(subject.getLendTime()!=null){
            String lendTime = subject.getLendTime().substring(0,8);
            startDate = startDate.compareTo(lendTime) < 0 ? lendTime : startDate;
        }
        if (term > 1) {
            if (holdDate.compareTo(previousSchedule.getDueDate()) < 0) {
                startDate = previousSchedule.getDueDate();
            }
        }
        //是否新模式的
        boolean isNewFixIplan = false;
        if(credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_SUBJECT)){
            isNewFixIplan = iPlanAccountService.isNewFixIplan(subjectTransferParamDao.findByTransferParamCode(subject.getTransferParamCode()));
        }
        BigDecimal rate = subject.getInvestRate();
        BigDecimal bonusRate = subject.getBonusRate();
        Integer activityId = subject.getActivityId();
        Integer creditPack = 0;
        if(credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_YJT)){
            Integer sourceAccountId = credit.getSourceAccountId();
            IPlanAccount account = iPlanAccountService.findById(sourceAccountId);
            IPlan iPlan = iPlanService.getIPlanById(account.getIplanId());
            if(iPlan.getRaiseFinishTime()==null){
                return null;
            }
            isNewFixIplan = iPlanAccountService.isNewFixIplan(subjectTransferParamDao.findByTransferParamCode(iPlan.getTransferParamCode()));
            rate = iPlan.getFixRate();
            bonusRate = iPlan.getBonusRate();
            activityId = iPlan.getActivityId();
            if(IPlan.PACKAGING_TYPE_CREDIT.equals(iPlan.getPackagingType())){
                term = iPlanRepayScheduleService.getCurrentTermByIplanId(iPlan.getId(),subjectRepaySchedule.getDueDate());
            }
            if(iPlan.getIncreaseRate()!=null && iPlan.getIncreaseRate().compareTo(BigDecimal.ZERO)>0&&iPlanAccountService.isNewIplan(iPlan)){
                //查询是否过了锁定期
                long days = DateUtil.betweenDays(credit.getStartTime().substring(0,8), endDate);
                Integer lockTerm = iPlan.getExitLockDays()/31>0 ? iPlan.getExitLockDays()/31 : 1;
                if(days> iPlan.getExitLockDays() && term > lockTerm){
                    rate = rate.add(iPlan.getIncreaseRate().multiply(BigDecimal.valueOf(term-lockTerm)));
                    rate = rate.compareTo(subject.getInvestRate())>0 ? subject.getInvestRate() : rate;
                }
            }
            creditPack = iPlan.getPackagingType();
        }
        //债权利息
        BigDecimal creditInterest =new BigDecimal(0);
        BigDecimal bonusInterest = new BigDecimal(0);
        BigDecimal bonusReward = new BigDecimal(0);
        if (subject.getLendTime() != null) {
            creditInterest = this.calculateInterest(startDate, endDate, new BigDecimal(principal), rate, subject.getPeriod(),creditPack,term);
            //若加息利率大于0,则计算加息利息
            if (bonusRate != null && bonusRate.compareTo(BigDecimal.valueOf(0)) > 0) {
                Integer addTerm = activityMarkConfigureDao.findTermById(activityId);
                //若使用的按月加息,则判断期数是否已用完
                if (addTerm != null && addTerm < term) {
                    bonusInterest = BigDecimal.ZERO;
                } else {
                    bonusInterest = this.calculateInterest(startDate, endDate, new BigDecimal(principal), bonusRate, subject.getPeriod(),creditPack,term);
                }
            }
            if(isNewFixIplan) {
                //查询对应trans_log,得到红包相关数据
                Integer redId = 0;
                if(credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_YJT)){
                    IPlanTransLog transLog = iPlanTransLogService.getById(credit.getSourceChannelId());
                    if(transLog != null){
                        redId = transLog.getRedPacketId();
                    }
                }else if (credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_SUBJECT)){
                    SubjectTransLog transLog = subjectTransLogService.getById(credit.getSourceChannelId());
                    if(transLog != null){
                        redId = transLog.getRedPacketId();
                    }
                }
                if (redId > 0) {
                    RedPacket red = redPacketService.getRedPacketById(redId);
                    if (red != null && RedPacket.TYPE_RATE.equals(red.getType()) && RedPacket.SEND_STATUS_USED.equals(red.getSendStatus())) {
                        bonusReward = this.calculateInterest(startDate, endDate, new BigDecimal(credit.getHoldingPrincipal()), BigDecimal.valueOf(red.getRate()), subject.getPeriod(),creditPack,term);
                    }
                }
            }
        }

        BigDecimal principalPaid = new BigDecimal(0);
        if (!totalPrincipal.equals(0)) {
            //该债权持有本金=（该债权本金/债权总本金）*标的应还本金
            principalPaid = new BigDecimal(subjectRepaySchedule.getDuePrincipal()).multiply(new BigDecimal(principal)).divide(BigDecimal.valueOf(totalPrincipal), 0, BigDecimal.ROUND_DOWN);
        }

        Integer interestBd = creditInterest.intValue();
        Integer principalBd = principalPaid.intValue();
        SubjectRepayDetail detail = new SubjectRepayDetail();
        detail.setInterest(interestBd);
        detail.setPrincipal(principalBd);
        detail.setBonusInterest(bonusInterest.intValue());
        detail.setCreateTime(subjectRepaySchedule.getDueDate());
        detail.setScheduleId(subjectRepaySchedule.getId());
        detail.setSubjectId(subjectId);
        detail.setUserId(userId);
        detail.setBonusReward(bonusReward.intValue());
        detail.setSourceAccountId(credit.getSourceAccountId());
        detail.setTerm(term);
        return detail;
    }

    /**
     * 计算当期应还本金利息
     * @param subjectRepaySchedule
     * @return
     */
    public SubjectRepayDetail repayDetailCredtiBySchedule(SubjectRepaySchedule subjectRepaySchedule,Credit credit,Integer principal,Integer totalPrincipal){
        String subjectId = subjectRepaySchedule.getSubjectId();
        //根据accountId查询对应债权
        Subject subject = subjectService.findSubjectBySubjectId(subjectRepaySchedule.getSubjectId());
        Integer term = subjectRepaySchedule.getTerm();
        SubjectRepaySchedule previousSchedule = null;
        if (term > 1) {
            previousSchedule = this.findRepaySchedule(subjectId, term - 1);
        }
        String holdDate = credit.getStartTime().substring(0, 8);
        //起始日期=持有日期
        String startDate = holdDate;
        //最后日期=还款日
        String endDate = subjectRepaySchedule.getDueDate();
        if(subject.getLendTime()!=null){
            String lendTime = subject.getLendTime().substring(0,8);
            startDate = startDate.compareTo(lendTime) < 0 ? lendTime : startDate;
        }
        if (term > 1) {
            if (holdDate.compareTo(previousSchedule.getDueDate()) < 0) {
                startDate = previousSchedule.getDueDate();
            }
        }
        BigDecimal rate = subject.getInvestRate();
        BigDecimal bonusRate = subject.getBonusRate();
        Integer activityId = subject.getActivityId();
        Integer currentTerm = term;
        Integer creditPack = 0;
        if(credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_YJT)){
            Integer sourceAccountId = credit.getSourceAccountId();
            IPlanAccount account = iPlanAccountService.findById(sourceAccountId);
            IPlan iPlan = iPlanService.getIPlanById(account.getIplanId());
            rate = iPlan.getFixRate();
            bonusRate = iPlan.getBonusRate();
            activityId = iPlan.getActivityId();
            if(IPlan.PACKAGING_TYPE_CREDIT.equals(iPlan.getPackagingType())){
                currentTerm = currentTerm -(subject.getTerm()-iPlan.getTerm());
            }
            creditPack = iPlan.getPackagingType();
        }
        //债权利息
        BigDecimal creditInterest =null;
        BigDecimal bonusInterest = new BigDecimal(0);
        if (subject.getLendTime() != null) {
            creditInterest = this.calculateInterest(startDate, endDate, new BigDecimal(principal), rate, subject.getPeriod(),creditPack,currentTerm);
            //若加息利率大于0,则计算加息利息
            if (bonusRate != null && bonusRate.compareTo(BigDecimal.valueOf(0)) > 0) {
                Integer addTerm = activityMarkConfigureDao.findTermById(activityId);
                if (addTerm != null && addTerm < currentTerm) {//若使用的按月加息,则判断期数是否已用完
                    bonusInterest = BigDecimal.ZERO;
                } else {
                    bonusInterest = this.calculateInterest(startDate, endDate, new BigDecimal(principal), bonusRate, subject.getPeriod(),creditPack,currentTerm);
                }
            }
        } else {
            creditInterest = new BigDecimal(0);
        }

        BigDecimal principalPaid = null;
        if (!totalPrincipal.equals(0)) {
            //该债权持有本金=（该债权本金/债权总本金）*标的应还本金
            principalPaid = new BigDecimal(subjectRepaySchedule.getDuePrincipal()).multiply(new BigDecimal(principal)).divide(BigDecimal.valueOf(totalPrincipal), 0, BigDecimal.ROUND_DOWN);
        }

        Integer interestBd = creditInterest.intValue();
        Integer principalBd = principalPaid.intValue();
        SubjectRepayDetail detail = new SubjectRepayDetail();
        detail.setInterest(interestBd);
        detail.setPrincipal(principalBd);
        detail.setBonusInterest(bonusInterest.intValue());
        detail.setCreateTime(subjectRepaySchedule.getDueDate());
        detail.setScheduleId(subjectRepaySchedule.getId());
        detail.setSubjectId(subjectId);
        return detail;
    }

    /**
     * 计算指定时间段下指定本金的应付利息
     *
     * @param startDate     起始时间
     * @param endDate       截止时间
     * @param principal     本金
     * @param rate          利率
     */
    public BigDecimal calculateInterest(String startDate, String endDate, BigDecimal principal, BigDecimal rate,Integer period,Integer creditPack,Integer term) {
        long days = DateUtil.betweenDays(startDate, endDate);
        //若持有时间>30天则按30天算
        if (days > 30 || (days < 30 && days >= 28)) {
            days=30;
        }

        //利息=本金*利息*持有天数
        if(period<30 || (IPlan.PACKAGING_TYPE_CREDIT.equals(creditPack)&&term==1&&days<30)){
            return principal.multiply(new BigDecimal(days)).multiply(rate).divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS), 6, BigDecimal.ROUND_DOWN);
        }else{
            return principal.multiply(new BigDecimal(days)).multiply(rate).divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS2), 6, BigDecimal.ROUND_DOWN);
        }
    }

    public SubjectRepaySchedule findById(Integer scheduleId){
        return subjectRepayScheduleDao.findById(scheduleId);
    }

    public Double calcInterest(Integer id){
        CreditOpening creditOpening = creditOpeningDao.findById(id);

        //标的
        Subject subject = subjectDao.findBySubjectId(creditOpening.getSubjectId());

        //原债权
        Credit credit = creditService.getById(creditOpening.getCreditId());

        //原标的年化
        BigDecimal totalRate = creditOpeningService.calcTotalRate(creditOpening.getSubjectId());
        System.out.println(totalRate);
        //转让金额
        Integer principal = creditOpening.getTransferPrincipal();

        //折让率
        BigDecimal transferDiscount = creditOpening.getTransferDiscount();
        //预期收益
        Integer expectInterest =(int)(subjectService.getInterestByRepayType(principal, totalRate, subject.getRate(), credit.getResidualTerm(), subject.getPeriod(), subject.getRepayType()) * 100);

        //标的未还总本金
        Integer totalPrincipal = 0;
        List<SubjectRepaySchedule> schedules = getSubjectRepayScheduleBySubjectIdNotRepay(creditOpening.getSubjectId());
        for (SubjectRepaySchedule schedule1 : schedules) {
            totalPrincipal += schedule1.getDuePrincipal();
        }
        //剩余天数
        String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
        String endDate = creditOpening.getEndTime().substring(0, 8);
        Integer days = (int)(DateUtil.betweenDays(currentDate, endDate));

        //新的年化利率
        BigDecimal interest  = BigDecimal.ZERO;
        if(!Subject.REPAY_TYPE_MCEI.equals(subject.getRepayType())){
            interest = new BigDecimal(expectInterest).multiply(new BigDecimal(365)).divide(new BigDecimal(principal).multiply(transferDiscount).multiply(new BigDecimal(days)),6,BigDecimal.ROUND_DOWN);
        }else {
            //计算中间值
            Integer principalPaid = 0;
            //存储每期持有本金
            List<Integer> list = new ArrayList<>();
            for (SubjectRepaySchedule schedule : schedules) {
                if(principal>0){
                    if (!totalPrincipal.equals(0)) {
                        //该债权持有本金=（该债权本金/债权总本金）*标的应还本金
                        principalPaid = new BigDecimal(schedule.getDuePrincipal()).multiply(new BigDecimal(principal)).divide(BigDecimal.valueOf(totalPrincipal), 0, BigDecimal.ROUND_DOWN).intValue();
                        list.add(principal);
                        principal -= principalPaid;
                    }
                }
                totalPrincipal -= schedule.getDuePrincipal();
            }
            for (Integer i : list) {
                System.out.println("i="+i);
            }
            String dueDate = schedules.get(0).getDueDate();
            days = (int)(DateUtil.betweenDays(currentDate, dueDate));
            //分母
            Double totalAmt = new BigDecimal(list.get(0)).multiply(transferDiscount).multiply(new BigDecimal(days)).divide(new BigDecimal(365),6,BigDecimal.ROUND_DOWN).doubleValue();
            List<Integer> subList = list.subList(1, list.size());
            for (Integer amt : subList) {
                totalAmt += new BigDecimal(amt).multiply(transferDiscount).multiply(new BigDecimal(30)).divide(new BigDecimal(365),6,BigDecimal.ROUND_DOWN).doubleValue();
            }
            interest = new BigDecimal(expectInterest).divide(new BigDecimal(totalAmt),6,BigDecimal.ROUND_DOWN);
        }
        return interest.doubleValue();
    }

    public Double calcTotalInterest(CreditOpening creditOpening,Subject subject){
        SubjectRepaySchedule previousSchedule = subjectRepayScheduleDao.findBySubjectIdAnsStatus(creditOpening.getSubjectId(), 1);
        String dueDate = "";
        if(previousSchedule != null){
             dueDate = previousSchedule.getDueDate();
        }else{
            Credit credit = creditDao.findById(creditOpening.getCreditId());
            dueDate = credit.getStartTime().substring(0, 8);
        }
        String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
        long days = DateUtil.betweenDays(dueDate, currentDate);
        double interest = (creditOpening.getAvailablePrincipal() / 100.0) * (days / 1.0) * (subject.getInvestRate().add(subject.getBonusRate()).doubleValue()) / 360.0;
        return interest;
    }

    public SubjectRepaySchedule findRepayScheduleNotRepayOnlyOne(String subjectId){

        return subjectRepayScheduleDao.findRepayScheduleNotRepayOnlyOne(subjectId);
    }

    public List<SubjectRepaySchedule> getFinishedBySubjectId (String subjectId,int term){
        return subjectRepayScheduleDao.getFinishedBySubjectId(subjectId, term);
    }

    /**
     * 计算应还本金,利息
     * @param schedule
     * @param subject
     * @param credit
     * @param principal
     * @param totalPrincipal
     * @param iPlan
     * @return
     */
    public SubjectRepayDetail repayDetailByScheduleAndRate(SubjectRepaySchedule schedule, Subject subject, Credit credit, Integer principal, Integer totalPrincipal, IPlan iPlan) {
        //债权持有开始时间
        String holdDate = credit.getStartTime().substring(0, 8);

        //查询当前期的前一期
        Integer term = schedule.getTerm();
        SubjectRepaySchedule previousSchedule = null;
        if (term > 1) {
            previousSchedule = this.findRepaySchedule(subject.getSubjectId(), term - 1);
        }

        //起始日期=持有日期
        String startDate = holdDate;
        //最后日期=还款日
        String endDate = schedule.getDueDate();
        //若
        if(subject.getLendTime()!=null){
            String lendTime = subject.getLendTime().substring(0,8);
            startDate = startDate.compareTo(lendTime) < 0 ? lendTime : startDate;
        }

        if (previousSchedule !=null) {
            if (holdDate.compareTo(previousSchedule.getDueDate()) < 0) {
                startDate = previousSchedule.getDueDate();
                if(startDate.compareTo(DateUtil.getCurrentDateShort()) > 0){
                    startDate = DateUtil.getCurrentDateShort();
                }
            }
        }
        BigDecimal rate = iPlan.getFixRate();
        BigDecimal bonusRate = iPlan.getBonusRate();
        Integer currentTerm = schedule.getTerm();
        //查询当前是省心投的第几期
        if(IPlan.PACKAGING_TYPE_CREDIT.equals(iPlan.getPackagingType())){
            currentTerm = iPlanRepayScheduleService.getCurrentTermByIplanId(iPlan.getId(),schedule.getDueDate());
        }

        //是否过了锁定期
        boolean isPassedLockDay = false;
        //从初始到现在持有时间
        long days = DateUtil.betweenDays(IPlan.PACKAGING_TYPE_CREDIT.equals(iPlan.getPackagingType()) ? credit.getStartTime().substring(0,8): subject.getLendTime().substring(0,8), endDate);
        //锁定期数
        Integer lockTerm = iPlan.getExitLockDays()/31 >0 ? iPlan.getExitLockDays()/31 : 1;
        //若持有天数大于锁定期,并且当前期大于锁定期数
        if(days> iPlan.getExitLockDays() && currentTerm> lockTerm ){
            isPassedLockDay = true;
        }
        if(isPassedLockDay && (iPlan.getIncreaseRate()!=null && iPlan.getIncreaseRate().compareTo(BigDecimal.ZERO)>0)){
            rate = rate.add(iPlan.getIncreaseRate().multiply(BigDecimal.valueOf(currentTerm-lockTerm)));
            rate = rate.compareTo(subject.getInvestRate())>0 ? subject.getInvestRate() : rate;
        }

        //利息
        BigDecimal creditInterest =BigDecimal.ZERO;
        //活动加息
        BigDecimal bonusInterest = BigDecimal.ZERO;

        if(subject.getLendTime()!=null || iPlan.getRaiseFinishTime()!=null){
            creditInterest = subjectAccountService.calculateInterest(startDate, endDate, BigDecimal.valueOf(principal), rate,subject.getPeriod(),true,true,iPlan.getPackagingType());
            //若加息利率大于0,则计算加息利息
            if (bonusRate != null && bonusRate.compareTo(BigDecimal.valueOf(0)) > 0) {
                Integer addTerm = activityMarkConfigureDao.findTermById(iPlan.getActivityId());
                if (addTerm != null && addTerm < currentTerm) {//若使用的按月加息,则判断期数是否已用完
                    bonusInterest = BigDecimal.ZERO;
                } else {
                    bonusInterest = subjectAccountService.calculateInterest(startDate, endDate, BigDecimal.valueOf(principal), bonusRate, subject.getPeriod(), true,true,iPlan.getPackagingType());
                }
            }
        }

        BigDecimal principalPaid = new BigDecimal(0);
        if (!totalPrincipal.equals(0)) {
            //该债权持有本金=（该债权本金/债权总本金）*标的应还本金
            principalPaid = new BigDecimal(schedule.getDuePrincipal()).multiply(new BigDecimal(principal)).divide(BigDecimal.valueOf(totalPrincipal), 0, BigDecimal.ROUND_DOWN);
        }

        Integer interestBd = creditInterest.intValue();
        Integer principalBd = principalPaid.intValue();
        SubjectRepayDetail detail = new SubjectRepayDetail();
        detail.setInterest(interestBd);
        detail.setPrincipal(principalBd);
        detail.setBonusInterest(bonusInterest.intValue());
        detail.setCreateTime(schedule.getDueDate());
        detail.setScheduleId(schedule.getId());
        detail.setSubjectId(subject.getSubjectId());
        detail.setUserId(credit.getUserId());
        detail.setSourceAccountId(credit.getSourceAccountId());
        return detail;
    }


    /**
     * 公共计算回款方法
     * @param credit  债权
     * @param schedules 未还还款计划
     * @param details   还款明细
     * @return
     */
    public List<SubjectAppRepayDetailDto.RepayDetail> commonRepaymentMethod(Credit credit,List<SubjectRepaySchedule> schedules,List<SubjectRepayDetail> details){
        List<SubjectAppRepayDetailDto.RepayDetail>  detailsForRepay = new ArrayList<>();
        if (!details.isEmpty()) {
            for (SubjectRepayDetail detail : details) {
                SubjectAppRepayDetailDto.RepayDetail detailForRepay = new SubjectAppRepayDetailDto.RepayDetail();
                SubjectRepaySchedule subjectRepaySchedule = this.findById(detail.getScheduleId());
                detailForRepay.setInterest(String.valueOf(detail.getInterest() / 100.0));//利息
                detailForRepay.setPrincipal(String.valueOf(detail.getPrincipal() / 100.0));//本金
                detailForRepay.setRepayDate(detail.getCreateTime().substring(0, 10));
                detailForRepay.setTerm(subjectRepaySchedule.getTerm());
                //加息奖励
                detailForRepay.setBonus(String.valueOf((detail.getBonusInterest()+detail.getBonusReward())/100.0));
                //总金额
                String money = String.valueOf((detail.getPrincipal()+detail.getInterest()+detail.getBonusInterest()+detail.getBonusReward())/100.0);
                detailForRepay.setMoney(money);
                //明细还未处理完
                if(detail.getStatus()==0 || (detail.getStatus()==1 && detail.getCurrentStep()!=3)){
                    detailForRepay.setStatus(0);
                    if(detail.getPrincipal()>0){
                        detailForRepay.setDescribe("第"+subjectRepaySchedule.getTerm()+"期未收回本息"+money+"元");
                    }else{
                        detailForRepay.setDescribe("第"+subjectRepaySchedule.getTerm()+"期未收回利息"+money+"元");
                    }
                }else{
                    detailForRepay.setStatus(1);
                    if(detail.getPrincipal()>0){
                        detailForRepay.setDescribe("第"+subjectRepaySchedule.getTerm()+"期已收回本息"+money+"元");
                    }else{
                        detailForRepay.setDescribe("第"+subjectRepaySchedule.getTerm()+"期已收回利息"+money+"元");
                    }
                }
                if (subjectRepaySchedule != null && subjectRepaySchedule.getStatus() >= 4) {
                    detailForRepay.setBeforeRepayFlag(SubjectAppRepayDetailDto.BEFORE_REPAY_FLAG_Y);
                }
                if(detail.getBonusInterest()+detail.getBonusReward() >0){
                    detailForRepay.setRedFlag(1);
                }
                detailsForRepay.add(detailForRepay);
            }
        }
        Integer principal = credit.getHoldingPrincipal();
        if (!schedules.isEmpty()) {
            Integer totalPrincipal = schedules.stream().map(SubjectRepaySchedule::getDuePrincipal).reduce(Integer::sum).orElse(0);
            for (SubjectRepaySchedule scedule : schedules) {
                SubjectRepayDetail repayDetail = this.repayDetailBySchedule(scedule, credit.getUserId(), credit, principal, totalPrincipal);
                if(repayDetail==null){
                    break;
                }
                SubjectAppRepayDetailDto.RepayDetail detailForRepay = new SubjectAppRepayDetailDto.RepayDetail();
                detailForRepay.setInterest(String.valueOf(repayDetail.getInterest() / 100.0));
                detailForRepay.setPrincipal(String.valueOf(repayDetail.getPrincipal() / 100.0));
                detailForRepay.setRepayDate(scedule.getDueDate().substring(0, 4) + "-" + scedule.getDueDate().substring(4, 6) + "-" + scedule.getDueDate().substring(6, 8));
                detailForRepay.setTerm(scedule.getTerm());
                //加息奖励
                detailForRepay.setBonus(String.valueOf((repayDetail.getBonusInterest()+repayDetail.getBonusReward())/100.0));
                //总金额
                String money = String.valueOf((repayDetail.getPrincipal()+repayDetail.getInterest()+repayDetail.getBonusInterest()+repayDetail.getBonusReward())/100.0);
                detailForRepay.setMoney(money);
                detailForRepay.setStatus(0);
                if(repayDetail.getPrincipal()>0){
                    detailForRepay.setDescribe("第"+scedule.getTerm()+"期未收回本息"+money+"元");
                }else{
                    detailForRepay.setDescribe("第"+scedule.getTerm()+"期未收回利息"+money+"元");
                }
                if(repayDetail.getBonusInterest()+repayDetail.getBonusReward() >0){
                    detailForRepay.setRedFlag(1);
                }
                detailsForRepay.add(detailForRepay);
                principal -= repayDetail.getPrincipal();
                totalPrincipal -= scedule.getDuePrincipal();
            }
        }
        return detailsForRepay;
    }
}
