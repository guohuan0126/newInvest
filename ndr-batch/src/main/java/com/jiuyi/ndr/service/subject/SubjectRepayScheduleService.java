package com.jiuyi.ndr.service.subject;

import com.alibaba.fastjson.JSONObject;
import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.agricultureloaninfo.AgricultureLoaninfoDao;
import com.jiuyi.ndr.dao.config.AccountCompensationConfigDao;
import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.redpacket.ActivityMarkConfigureDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayScheduleDao;
import com.jiuyi.ndr.dao.user.UserDao;
import com.jiuyi.ndr.domain.account.CompensatoryAcctLog;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.agricultureloaninfo.AgricultureLoaninfo;
import com.jiuyi.ndr.domain.config.AccountCompensationConfig;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.subject.*;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.service.account.CompensatoryAcctLogService;
import com.jiuyi.ndr.service.account.PlatformAccountService;
import com.jiuyi.ndr.service.account.PlatformTransferService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanRepayScheduleService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestSingleTrans;
import com.jiuyi.ndr.xm.http.request.RequestUserAutoPreTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 标的还款计划服务
 * Created by lixiaolei on 2017/4/11.
 */
@Service
public class SubjectRepayScheduleService {

    private final static Logger logger = LoggerFactory.getLogger(SubjectRepayScheduleService.class);

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectOverdueDefService subjectOverdueDefService;
    @Autowired
    private SubjectRepayScheduleDao subjectRepayScheduleDao;
    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private SubjectRepayDetailDao subjectRepayDetailDao;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private SubjectAdvancedPayOffService subjectAdvancedPayOffService;
    @Autowired
    private SubjectPayoffRegService payoffRegService;
    @Autowired
    private CreditOpeningDao creditOpeningDao;
    @Autowired
    private CreditService creditService;
    @Autowired
    private PlatformTransferService platformTransferService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ConfigDao configDao;
    @Autowired
    private AgricultureLoaninfoDao agricultureLoaninfoDao;
    @Autowired
    private AccountCompensationConfigDao accountCompensationConfigDao;
    @Autowired
    private SubjectRepayBillService subjectRepayBillService;
    @Autowired
    private CompensatoryAcctLogService cpsAcctLogService;
    @Autowired
    private IPlanRepayScheduleService iPlanRepayScheduleService;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private ActivityMarkConfigureDao activityMarkConfigureDao;


    @Value(value = "${EMAIL.REPAY_EMAIL}")
    private String repayEmail;
    /**
     * 逾期更新还款计划，累加罚息
     * @param scheduleId
     * @return
     */
    @Transactional
    public SubjectRepaySchedule updateRepayScheduleCauseOverdue(Integer scheduleId) {
        SubjectRepaySchedule subjectRepaySchedule = subjectRepayScheduleDao.findById(scheduleId);
        Subject subject = subjectService.findSubjectBySubjectId(subjectRepaySchedule.getSubjectId());
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
     * 查询某标的某期还款计划
     * @param subjectId
     * @param term
     * @return
     */
    public SubjectRepaySchedule findRepaySchedule(String subjectId, Integer term) {
        if (subjectId == null || term == null) {
            throw new IllegalArgumentException("subjectId or term is empty!");
        }
        SubjectRepaySchedule subjectRepaySchedule = subjectRepayScheduleDao.findBySubjectIdAndTerm(subjectId, term);
        return subjectRepaySchedule;
    }

    /**
     * 查询某标的的所有还款计划
     * @param subjectId
     * @return
     */
    public List<SubjectRepaySchedule> findRepayScheduleBySubjectId(String subjectId) {
        List<SubjectRepaySchedule> subjectRepaySchedules = subjectRepayScheduleDao.findBySubjectIdOrderByTerm(subjectId);
        return subjectRepaySchedules;
    }

    /**
     * 根据还款状态查询还款计划
     * @param status
     * @return
     */
    public List<SubjectRepaySchedule> findRepayScheduleByStatus(Integer status) {
        List<SubjectRepaySchedule> subjectRepaySchedules = subjectRepayScheduleDao.findByStatusOrderBySubjectId(status);
        return subjectRepaySchedules;
    }

    /**
     * 获得当前期数
     * @return
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
     * @param subjectId
     * @return
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

    public List<SubjectRepaySchedule> findBySubjectIds(Set<String> subjectIds){
        return subjectRepayScheduleDao.findBySubjectIdIn(subjectIds);
    }

    /**
     * 每天罚息计算
     * 算法 ：基数 * 利率（年）/ 一年的天数（360）
     * @param baseAmt 基数
     * @param rate 利率
     * @return
     */
    private Integer calcOverduePenaltyDaily(Integer baseAmt, BigDecimal rate) {
        Integer penaltyDaily = new BigDecimal(baseAmt).multiply(rate)
                .divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS), 6, BigDecimal.ROUND_HALF_UP).intValue();
        return penaltyDaily;
    }

    /**
     * 生成还款计划
     */
    @Transactional
    public List<SubjectRepaySchedule> makeUpRepaySchedule(Subject subject) {
        Integer contractAmt = subject.getTotalAmt();
        BigDecimal rate = subject.getRate();//借款年利率
        Integer termMonth = subject.getPeriod() >= GlobalConfig.ONEMONTH_DAYS ? subject.getTerm() : 1;
        Integer period = subject.getPeriod();
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
        List<SubjectRepaySchedule> schedules = new ArrayList<>(termMonth);
        for (int m = 1; m <= termMonth; m++) {
            SubjectRepaySchedule subjectRepaySchedule = new SubjectRepaySchedule();
            if (period>=30){
                FinanceCalcUtils.CalcResult.Detail detail = calcResult.getDetails().get(m);
                subjectRepaySchedule.setSubjectId(subject.getSubjectId());
                subjectRepaySchedule.setTerm(m);
                String yearMonth = plusMonths(m);
                if (!Subject.DIRECT_FLAG_NO.equals(subject.getDirectFlag())){
                    String day = yearMonth + day(yearMonth);
                    LocalDate nextDay = DateUtil.parseDate(day, DateUtil.DATE_TIME_FORMATTER_8).plusDays(1);
                    String dueDay = nextDay.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                    subjectRepaySchedule.setDueDate(dueDay);
                }else {
                    subjectRepaySchedule.setDueDate(yearMonth + day(yearMonth));
                }
                subjectRepaySchedule.setDuePrincipal(detail.getMonthRepayPrincipal());
                subjectRepaySchedule.setDueInterest(detail.getMonthRepayInterest());
                subjectRepaySchedule.setDuePenalty(0);
                subjectRepaySchedule.setDueFee(0);
                subjectRepaySchedule.setStatus(SubjectRepaySchedule.STATUS_NOT_REPAY);
                subjectRepaySchedule.setIsRepay(0);
                subjectRepaySchedule.setCreateTime(DateUtil.getCurrentDateTime19());
                //直贷二期 新增字段初始化
                subjectRepaySchedule.setRepayPrincipal(0);
                subjectRepaySchedule.setRepayInterest(0);
                subjectRepaySchedule.setRepayPenalty(0);
                subjectRepaySchedule.setRepayFee(0);
                subjectRepaySchedule.setInterimRepayAmt(0);
                subjectRepaySchedule.setInterimCpsAmt(0);
                subjectRepaySchedule.setRepayFee(0);
                subjectRepaySchedule.setCpsStatus(SubjectRepaySchedule.CPS_STATUS_NOT_YET);
            } else {
                FinanceCalcUtils.CalcResult.Detail detail = calcResult.getDetails().get(m);
                subjectRepaySchedule.setSubjectId(subject.getSubjectId());
                subjectRepaySchedule.setTerm(m);
                String date = DateUtil.parseDate(DateUtil.getCurrentDateShort(),DateUtil.DATE_TIME_FORMATTER_8).plusDays(period).format(DateUtil.DATE_TIME_FORMATTER_8);
                subjectRepaySchedule.setDueDate(date);
                subjectRepaySchedule.setDuePrincipal(detail.getMonthRepayPrincipal());
                subjectRepaySchedule.setDueInterest(detail.getMonthRepayInterest());
                subjectRepaySchedule.setDuePenalty(0);
                subjectRepaySchedule.setDueFee(0);
                subjectRepaySchedule.setStatus(SubjectRepaySchedule.STATUS_NOT_REPAY);
                subjectRepaySchedule.setIsRepay(0);
                subjectRepaySchedule.setCreateTime(DateUtil.getCurrentDateTime19());
                //直贷二期 新增字段初始化
                subjectRepaySchedule.setRepayPrincipal(0);
                subjectRepaySchedule.setRepayInterest(0);
                subjectRepaySchedule.setRepayPenalty(0);
                subjectRepaySchedule.setRepayFee(0);
                subjectRepaySchedule.setInterimRepayAmt(0);
                subjectRepaySchedule.setInterimCpsAmt(0);
                subjectRepaySchedule.setRepayFee(0);
                subjectRepaySchedule.setCpsStatus(SubjectRepaySchedule.CPS_STATUS_NOT_YET);
            }
            schedules.add(subjectRepaySchedule);
            subjectRepayScheduleDao.insert(subjectRepaySchedule);
        }
        return schedules;
    }
    /**
     * 返回还款日中的日
     * @param yearMonthStr 还款日年月
     * @return
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
     * @param monthsToAdd
     * @return
     */
    private String plusMonths(int monthsToAdd) {
        return YearMonth.now().plusMonths(monthsToAdd).format(DateTimeFormatter.ofPattern("yyyyMM"));
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


    public SubjectRepaySchedule getById(Integer id) {
        return subjectRepayScheduleDao.findById(id);
    }

    @Transactional
    public Subject findBySubjectIdForUpdate(String subjectId) {
        if (!StringUtils.hasText(subjectId)) {
            throw new IllegalArgumentException("subjectId不能为空");
        }
        return subjectDao.findBySubjectIdForUpdate(subjectId);
    }

    /**
     * 是否直贷
     * @param flag
     * @return
     */
    public boolean isDirect(Integer flag){
        return (!Subject.DIRECT_FLAG_NO.equals(flag))?true:false;
    }

    /**
     * 借款信息
     */
    public Map<String, Integer> getBorrowerDetails(SubjectRepaySchedule subjectRepaySchedule){
        Map<String, Integer> borrowerDetails = new HashMap<>();
        borrowerDetails.put("duePrincipal", subjectRepaySchedule.getDuePrincipal());//当期应还本金
        borrowerDetails.put("dueInterest", subjectRepaySchedule.getDueInterest());//当期应还利息
        borrowerDetails.put("duePenalty", subjectRepaySchedule.getDuePenalty());//当期应还罚息
        borrowerDetails.put("dueFee", subjectRepaySchedule.getDueFee());//当期应还费用
        borrowerDetails.put("repayPenalty", subjectRepaySchedule.getRepayPenalty());
        borrowerDetails.put("repayFee", subjectRepaySchedule.getRepayFee());
        borrowerDetails.put("dueTotalAmt", subjectRepaySchedule.getDuePrincipal()+subjectRepaySchedule.getDueInterest()+subjectRepaySchedule.getDuePenalty()+subjectRepaySchedule.getDueFee());
        borrowerDetails.put("billDuePenalty", subjectRepaySchedule.getDuePenalty());//当期应还罚息
        borrowerDetails.put("billDueFee", subjectRepaySchedule.getDueFee());//当期应还罚息
        return borrowerDetails;
    }
    /**
     * 提前结清借款信息
     */
    public Map<String, Integer> getBorrowerAdvanceDetails(Subject subject,SubjectRepaySchedule subjectRepaySchedule,Integer payOffPenalty){
        Map<String, Integer> borrowerDetails = new HashMap<>();
        borrowerDetails.put("duePrincipal", subject.getTotalAmt() - subject.getPaidPrincipal());
        borrowerDetails.put("dueInterest", subjectRepaySchedule.getDueInterest());
        borrowerDetails.put("duePenalty", subjectRepaySchedule.getDuePenalty() + payOffPenalty);
        borrowerDetails.put("dueFee", subjectRepaySchedule.getDueFee());
        borrowerDetails.put("repayPenalty", subjectRepaySchedule.getRepayPenalty());
        borrowerDetails.put("repayFee", subjectRepaySchedule.getRepayFee());
        borrowerDetails.put("dueTotalAmt", subject.getTotalAmt() - subject.getPaidPrincipal()+subjectRepaySchedule.getDueInterest()+subjectRepaySchedule.getDuePenalty()+subjectRepaySchedule.getDueFee());
        borrowerDetails.put("billDuePenalty", subjectRepaySchedule.getDuePenalty());//当期应还罚息
        borrowerDetails.put("billDueFee", subjectRepaySchedule.getDueFee());//当期应还罚息
        return borrowerDetails;
    }

    /**
     * 保存还款明细
     * @param scheduleId
     * @param subjectId
     * @param borrowerDetails
     * @param subjectDetails 散标列表
     * @param iplanDetails   定期
     * @param lplanDetails  活期
     * @param newIplanDetails  一键投
     */
    public void saveRepayDetails(Integer scheduleId, String subjectId, Map<String, Integer> borrowerDetails,
                                 Map<String, Map<String, Object>> subjectDetails,
                                 Map<String, Map<String, Object>> iplanDetails,
                                 Map<String, Map<String, Object>> lplanDetails,
                                 Map<String, Map<String, Object>> newIplanDetails) {
        List<SubjectRepayDetail> list = new ArrayList<>();
        Subject subject = subjectDao.findBySubjectId(subjectId);
        SubjectRepaySchedule subjectRepaySchedule = subjectRepayScheduleDao.findById(scheduleId);
        Integer repayPrincipal = subjectRepaySchedule.getInterimRepayAmt();//借款人实还
        Integer repayCps = subjectRepaySchedule.getInterimCpsAmt();//代偿实还
        //代偿初始记录的金额
        Integer initCps = subjectRepaySchedule.getInitCpsAmt();
        Integer totalPenalty = 0;
        int totalPaidAmt = 0;
        Integer totalBrwCom = 0;
        if (subjectDetails != null) {
            for (Map.Entry<String, Map<String, Object>> entry : subjectDetails.entrySet()) {
                String userIdAndAccountId = entry.getKey();
                String userId = userIdAndAccountId.split("_")[0];
                Map<String, Object> result = entry.getValue();
                String userIdXm = (String) result.get("userIdXm");
                int interest = (int) (result.get("interest") == null ? 0 : result.get("interest"));
                int principal = (int) (result.get("principal") == null ? 0 : result.get("principal"));
                int penalty = (int) (result.get("penalty") == null ? 0 : result.get("penalty"));
                int fee = (int) (result.get("fee") == null ? 0 : result.get("fee"));
                int interestFreeze = (int) (result.get("interestFreeze") == null ? 0 : result.get("interestFreeze"));
                int principalFreeze = (int) (result.get("principalFreeze") == null ? 0 : result.get("principalFreeze"));
                int penaltyFreeze = (int) (result.get("penaltyFreeze") == null ? 0 : result.get("penaltyFreeze"));
                int feeFreeze = (int) (result.get("feeFreeze") == null ? 0 : result.get("feeFreeze"));
                int bonusInterest = (int)(result.get("bonusInterest") == null ? 0 : result.get("bonusInterest"));
                int commission = (int)(result.get("commission") == null ? 0 : result.get("commission"));
                int bonusReward = (int)(result.get("bonusReward") == null ? 0 : result.get("bonusReward"));
                totalPaidAmt += (interest + principal + penalty + fee + commission);
                totalPenalty += penalty;
                logger.info("散标开始计算明细,借款实还金额{},应还{}",repayPrincipal,interest + principal + penalty);
                //若是代偿出
                if((initCps!=null && initCps>0) || Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag())){
                    //代偿出
                    if((interest + principal + penalty + commission)<=repayCps){
                        SubjectRepayDetail detail = this.saveDetail(subjectId,scheduleId,userId,principal,interest,penalty,fee,commission,0,0,
                                (String) result.get("investRequestNo"),(Integer) result.get("sourceAccountId"),Credit.SOURCE_CHANNEL_SUBJECT,SubjectRepayDetail.SOURCE_CPS,bonusInterest,bonusReward);
                        list.add(detail);
                        bonusInterest=0;
                        bonusReward=0;
                        repayCps -=(interest + principal + penalty+commission);
                        logger.info("代偿出金额还剩{}",repayCps);
                        continue;
                    }
                    //借款人出一部分,代偿出一部分
                    if(repayCps>0&&repayCps<interest + principal + penalty + commission){
                        SubjectRepayDetail detail = new SubjectRepayDetail();
                        detail.setChannel(Credit.SOURCE_CHANNEL_SUBJECT);
                        detail.setScheduleId(scheduleId);
                        detail.setSubjectId(subjectId);
                        detail.setUserId(userId);
                        detail.setUserIdXm(userIdXm);
                        if(principal<=repayCps){
                            //借款人余额不足,全部给这条明细,剩下的代偿出
                            detail.setPrincipal(principal);
                            detail.setFreezePrincipal(principalFreeze);
                            repayCps -= principal;
                            principal=0;
                        }else{
                            detail.setPrincipal(repayCps);
                            detail.setFreezePrincipal(repayCps);
                            principal -= repayCps;
                            repayCps =0;
                        }
                        logger.info("部分还款借款人出一部分,出完本金还剩{}",repayCps);
                        if(interest<=repayCps){
                            detail.setInterest(interest);
                            detail.setFreezeInterest(interestFreeze);
                            repayCps -= interest;
                            interest=0;
                        }else{
                            detail.setInterest(repayCps);
                            detail.setFreezeInterest(repayCps);
                            interest -= repayCps;
                            repayCps =0;
                        }
                        if(penalty<=repayCps){
                            detail.setPenalty(penalty);
                            detail.setFreezePenalty(penaltyFreeze);
                            repayCps -= penalty;
                            penalty=0;
                        }else{
                            detail.setPenalty(repayCps);
                            detail.setFreezePenalty(repayCps);
                            penalty -= repayCps;
                            repayCps =0;
                        }
                        if(commission <= repayCps){
                            detail.setCommission(commission);
                            repayCps -= commission;
                            commission = 0;
                        }else{
                            detail.setCommission(repayCps);
                            commission -= repayCps;
                            repayCps=0;
                        }
                        detail.setFee(fee);
                        detail.setFreezeFee(feeFreeze);
                        detail.setFreezeRequestNo((String) result.get("investRequestNo"));
                        detail.setStatus(SubjectRepayDetail.STATUS_PENDING);
                        detail.setSourceAccountId((Integer) result.get("sourceAccountId"));
                        detail.setProfit(0);
                        detail.setDeptPenalty(0);
                        if(bonusInterest>0){
                            detail.setBonusInterest(bonusInterest);
                            bonusInterest=0;
                        }else{
                            detail.setBonusInterest(0);
                        }
                        if(bonusReward>0){
                            detail.setBonusReward(bonusReward);
                            bonusReward=0;
                        }else{
                            detail.setBonusReward(0);
                        }
                        detail.setCurrentStep(SubjectRepayDetail.STEP_NOT_TRANS);
                        //还款明细新增字段
                        detail.setSourceType(SubjectRepayDetail.SOURCE_CPS);
                        list.add(detail);
                        logger.info("代偿实还金额还剩{}",repayCps);
                    }
                    //借款人出
                    if(repayCps==0&&repayPrincipal>=interest + principal + penalty + commission){
                        SubjectRepayDetail detail = this.saveDetail(subjectId,scheduleId,userId,principal,interest,penalty,fee,commission,0,0,
                                (String) result.get("investRequestNo"),(Integer) result.get("sourceAccountId"),Credit.SOURCE_CHANNEL_SUBJECT,SubjectRepayDetail.SOURCE_BRW,bonusInterest,bonusReward);
                        list.add(detail);
                        repayPrincipal -=(interest + principal + penalty + commission);
                        logger.info("借款人实还金额还剩{},应还{}",repayPrincipal,interest + principal + penalty + commission);
                    }
                }else{
                    SubjectRepayDetail detail = this.saveDetail(subjectId,scheduleId,userId,principal,interest,penalty,fee,commission,0,0,
                            (String) result.get("investRequestNo"),(Integer) result.get("sourceAccountId"),Credit.SOURCE_CHANNEL_SUBJECT,SubjectRepayDetail.SOURCE_BRW,bonusInterest,bonusReward);
                    list.add(detail);
                }
            }
        }
        if (iplanDetails != null) {
            for (Map.Entry<String, Map<String, Object>> entry : iplanDetails.entrySet()) {
                String userIdAndIPlanId = entry.getKey();
                String userId = userIdAndIPlanId.split("_")[0];
                Map<String, Object> result = entry.getValue();
                String userIdXm = (String) result.get("userIdXm");
                int interest = (int) (result.get("interest") == null ? 0 : result.get("interest"));
                int principal = (int) (result.get("principal") == null ? 0 : result.get("principal"));
                int penalty = (int) (result.get("penalty") == null ? 0 : result.get("penalty"));
                int fee = (int) (result.get("fee") == null ? 0 : result.get("fee"));
                int interestFreeze = (int) (result.get("interestFreeze") == null ? 0 : result.get("interestFreeze"));
                int principalFreeze = (int) (result.get("principalFreeze") == null ? 0 : result.get("principalFreeze"));
                int penaltyFreeze = (int) (result.get("penaltyFreeze") == null ? 0 : result.get("penaltyFreeze"));
                int feeFreeze = (int) (result.get("feeFreeze") == null ? 0 : result.get("feeFreeze"));
                int commission = (int)(result.get("commission") == null ? 0 : result.get("commission"));
                int brwCommission = (int)(result.get("brwCommission") == null ? 0 : result.get("brwCommission"));
                totalPaidAmt += (interest + principal + penalty + fee + commission);
                totalPenalty += penalty;
                totalBrwCom += brwCommission;
                logger.info("月月盈开始计算明细,借款实还金额{},应还{}",repayPrincipal,interest + principal + penalty);
                //若是有代偿出
                if((initCps!=null && initCps>0) || Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag())){
                    //代偿出
                    if(interest + principal + penalty + commission<=repayCps){
                        SubjectRepayDetail detail = this.saveDetail(subjectId,scheduleId,userId,principal,interest,penalty,fee,commission,0,0,
                                (String) result.get("investRequestNo"),(Integer) result.get("sourceAccountId"),Credit.SOURCE_CHANNEL_IPLAN,SubjectRepayDetail.SOURCE_CPS,0,0);
                        list.add(detail);
                        repayCps -=(interest + principal + penalty + commission);
                        logger.info("代偿出金额还剩{}",repayCps);
                        continue;
                    }
                    //借款人出一部分,代偿出一部分
                    if(repayCps>0&&repayCps<interest + principal + penalty + commission){
                        SubjectRepayDetail detail = new SubjectRepayDetail();
                        detail.setChannel(Credit.SOURCE_CHANNEL_IPLAN);
                        detail.setScheduleId(scheduleId);
                        detail.setSubjectId(subjectId);
                        detail.setUserId(userId);
                        detail.setUserIdXm(userIdXm);
                        if(principal<=repayCps){
                            detail.setPrincipal(principal);//借款人余额不足,全部给这条明细,剩下的代偿出
                            detail.setFreezePrincipal(principalFreeze);
                            repayCps -= principal;
                            principal=0;
                        }else{
                            detail.setPrincipal(repayCps);
                            detail.setFreezePrincipal(repayCps);
                            principal -= repayCps;
                            repayCps =0;
                        }
                        logger.info("部分还款代偿出一部分,还剩{}",repayCps);
                        if(interest<=repayCps){
                            detail.setInterest(interest);
                            detail.setFreezeInterest(interestFreeze);
                            repayCps -= interest;
                            interest=0;
                        }else{
                            detail.setInterest(repayCps);
                            detail.setFreezeInterest(repayCps);
                            interest -= repayCps;
                            repayCps =0;
                        }
                        if(penalty<=repayCps){
                            detail.setPenalty(penalty);
                            detail.setFreezePenalty(penaltyFreeze);
                            repayCps -= penalty;
                            penalty=0;
                        }else{
                            detail.setPenalty(repayCps);
                            detail.setFreezePenalty(repayCps);
                            penalty -= repayCps;
                            repayCps =0;
                        }
                        if(commission <= repayCps){
                            detail.setCommission(commission);
                            repayCps -= commission;
                            commission = 0;
                        }else{
                            detail.setCommission(repayCps);
                            commission -= repayCps;
                            repayCps=0;
                        }
                        detail.setFee(fee);
                        detail.setFreezeFee(feeFreeze);
                        detail.setFreezeRequestNo((String) result.get("investRequestNo"));
                        detail.setStatus(SubjectRepayDetail.STATUS_PENDING);
                        detail.setSourceAccountId((Integer) result.get("sourceAccountId"));
                        detail.setProfit(0);
                        detail.setDeptPenalty(0);
                        detail.setCurrentStep(SubjectRepayDetail.STEP_NOT_TRANS);
                        detail.setSourceType(SubjectRepayDetail.SOURCE_CPS);
                        list.add(detail);
                        logger.info("代偿账户实还金额还剩{}",repayCps);
                    }
                    //借款人出
                    if(repayCps==0&&repayPrincipal>=interest + principal + penalty + commission){
                        SubjectRepayDetail detail = this.saveDetail(subjectId,scheduleId,userId,principal,interest,penalty,fee,commission,0,0,
                                (String) result.get("investRequestNo"),(Integer) result.get("sourceAccountId"),Credit.SOURCE_CHANNEL_IPLAN,SubjectRepayDetail.SOURCE_BRW,0,0);
                        list.add(detail);
                        repayPrincipal -=(interest + principal + penalty + commission);
                        logger.info("借款人实还金额还剩{},应还{}",repayPrincipal,interest + principal + penalty + commission);
                    }
                }else{
                    SubjectRepayDetail detail = this.saveDetail(subjectId,scheduleId,userId,principal,interest,penalty,fee,commission,0,0,
                            (String) result.get("investRequestNo"),(Integer) result.get("sourceAccountId"),Credit.SOURCE_CHANNEL_IPLAN,SubjectRepayDetail.SOURCE_BRW,0,0);
                    list.add(detail);
                }
            }
        }
        if (newIplanDetails != null) {
            for (Map.Entry<String, Map<String, Object>> entry : newIplanDetails.entrySet()) {
                String userIdAndIPlanId = entry.getKey();
                String userId = userIdAndIPlanId.split("_")[0];
                Map<String, Object> result = entry.getValue();
                String userIdXm = (String) result.get("userIdXm");
                int interest = (int) (result.get("interest") == null ? 0 : result.get("interest"));
                int principal = (int) (result.get("principal") == null ? 0 : result.get("principal"));
                int penalty = (int) (result.get("penalty") == null ? 0 : result.get("penalty"));
                int fee = (int) (result.get("fee") == null ? 0 : result.get("fee"));
                int interestFreeze = (int) (result.get("interestFreeze") == null ? 0 : result.get("interestFreeze"));
                int principalFreeze = (int) (result.get("principalFreeze") == null ? 0 : result.get("principalFreeze"));
                int penaltyFreeze = (int) (result.get("penaltyFreeze") == null ? 0 : result.get("penaltyFreeze"));
                int feeFreeze = (int) (result.get("feeFreeze") == null ? 0 : result.get("feeFreeze"));
                int bonusInterest = (int)(result.get("bonusInterest") == null ? 0 : result.get("bonusInterest"));
                int commission = (int)(result.get("commission") == null ? 0 : result.get("commission"));
                int bonusReward = (int)(result.get("bonusReward") == null ? 0 : result.get("bonusReward"));
                int brwCommission = (int)(result.get("brwCommission") == null ? 0 : result.get("brwCommission"));

                totalPaidAmt += (interest + principal + penalty + fee +commission);
                totalPenalty += penalty;
                totalBrwCom += brwCommission;
                //若是有代偿出的金额
                if((initCps!=null && initCps>0) || Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag())){
                    //代偿出
                    if(interest + principal + penalty + commission<=repayCps){
                        SubjectRepayDetail detail = this.saveDetail(subjectId,scheduleId,userId,principal,interest,penalty,fee,commission,0,0,
                                (String) result.get("investRequestNo"),(Integer) result.get("sourceAccountId"),Credit.SOURCE_CHANNEL_YJT,SubjectRepayDetail.SOURCE_CPS,bonusInterest,bonusReward);
                        list.add(detail);
                        bonusInterest=0;
                        bonusReward=0;
                        repayCps -=(interest + principal + penalty + commission);
                        logger.info("代偿出金额还剩{}",repayCps);
                        continue;
                    }
                    //借款人出一部分,代偿出一部分
                    if(repayCps>0&&repayCps<interest + principal + penalty + commission){
                        SubjectRepayDetail detail = new SubjectRepayDetail();
                        detail.setChannel(Credit.SOURCE_CHANNEL_YJT);
                        detail.setScheduleId(scheduleId);
                        detail.setSubjectId(subjectId);
                        detail.setUserId(userId);
                        detail.setUserIdXm(userIdXm);
                        if(principal<=repayCps){
                            //借款人余额不足,全部给这条明细,剩下的代偿出
                            detail.setPrincipal(principal);
                            detail.setFreezePrincipal(principalFreeze);
                            repayCps -= principal;
                            principal=0;
                        }else{
                            detail.setPrincipal(repayCps);
                            detail.setFreezePrincipal(repayCps);
                            principal -= repayCps;
                            repayCps =0;
                        }
                        logger.info("部分还款代偿出一部分,出完本金还剩{}",repayCps);
                        if(interest<=repayCps){
                            detail.setInterest(interest);
                            detail.setFreezeInterest(interestFreeze);
                            repayCps -= interest;
                            interest=0;
                        }else{
                            detail.setInterest(repayCps);
                            detail.setFreezeInterest(repayCps);
                            interest -= repayCps;
                            repayCps =0;
                        }
                        if(penalty<=repayCps){
                            detail.setPenalty(penalty);
                            detail.setFreezePenalty(penaltyFreeze);
                            repayCps -= penalty;
                            penalty=0;
                        }else{
                            detail.setPenalty(repayCps);
                            detail.setFreezePenalty(repayCps);
                            penalty -= repayCps;
                            repayCps =0;
                        }
                        if(commission <= repayCps){
                            detail.setCommission(commission);
                            repayCps -= commission;
                            commission = 0;
                        }else{
                            detail.setCommission(repayCps);
                            commission -= repayCps;
                            repayCps = 0;
                        }
                        detail.setFee(fee);
                        detail.setFreezeFee(feeFreeze);
                        detail.setFreezeRequestNo((String) result.get("investRequestNo"));
                        detail.setStatus(SubjectRepayDetail.STATUS_PENDING);
                        detail.setSourceAccountId((Integer) result.get("sourceAccountId"));
                        detail.setProfit(0);
                        detail.setDeptPenalty(0);
                        if(bonusInterest>0){
                            detail.setBonusInterest(bonusInterest);
                            bonusInterest=0;
                        }else{
                            detail.setBonusInterest(0);
                        }
                        if(bonusReward>0){
                            detail.setBonusReward(bonusReward);
                            bonusReward=0;
                        }else{
                            detail.setBonusReward(0);
                        }
                        detail.setCurrentStep(SubjectRepayDetail.STEP_NOT_TRANS);
                        //还款明细新增字段
                        detail.setSourceType(SubjectRepayDetail.SOURCE_CPS);
                        list.add(detail);
                        logger.info("代偿实还金额还剩{}",repayCps);
                    }
                    //借款人出
                    if(repayCps==0&&repayPrincipal>=interest + principal + penalty + commission){
                        SubjectRepayDetail detail = this.saveDetail(subjectId,scheduleId,userId,principal,interest,penalty,fee,commission,0,0,
                                (String) result.get("investRequestNo"),(Integer) result.get("sourceAccountId"),Credit.SOURCE_CHANNEL_YJT,SubjectRepayDetail.SOURCE_BRW,bonusInterest,bonusReward);
                        list.add(detail);
                        repayPrincipal -=(interest + principal + penalty + commission);
                        logger.info("借款人实还金额还剩{},应还{}",repayPrincipal,interest + principal + penalty + commission);
                    }
                }else{
                    SubjectRepayDetail detail = this.saveDetail(subjectId,scheduleId,userId,principal,interest,penalty,fee,commission,0,0,
                            (String) result.get("investRequestNo"),(Integer) result.get("sourceAccountId"),Credit.SOURCE_CHANNEL_YJT,SubjectRepayDetail.SOURCE_BRW,bonusInterest,bonusReward);
                    list.add(detail);
                }
            }
        }
        if (list.size() > 0) {
            int duePrincipal = borrowerDetails.get("duePrincipal");
            int dueInterest = borrowerDetails.get("dueInterest");
            int duePenalty = borrowerDetails.get("duePenalty");
            int dueFee = borrowerDetails.get("dueFee");
            int dueTotalAmt = borrowerDetails.get("dueTotalAmt");//应还总金额
            int commission = totalBrwCom;
            SubjectRepayDetail detail = list.get(list.size()-1);
            if((initCps!=null && initCps>0) || Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag())){
                Integer frProfit = borrowerDetails.get("billDueFee");//分润给事业部的费用
                int frPenalty = borrowerDetails.get("billDuePenalty") - totalPenalty;
                frPenalty = frPenalty<0?0:frPenalty;
                //佣金=应还本息罚费-给投资人的本息罚-分润给事业部的罚和费
                Integer amt = dueTotalAmt- totalPaidAmt-frPenalty-frProfit - commission;
                commission +=  amt;
                logger.info("借款人应出佣金{}",commission);
                if(SubjectRepayDetail.SOURCE_CPS.equals(detail.getSourceType())){
                    if(repayCps==0&&repayPrincipal>0){
                        SubjectRepayDetail detail1 = this.saveDetail(subjectId,scheduleId,detail.getUserId(),0,0,0,0,commission,
                                frProfit,frPenalty,detail.getFreezeRequestNo(),detail.getSourceAccountId(),detail.getChannel(),SubjectRepayDetail.SOURCE_BRW,0,0);
                        list.add(detail1);
                        repayPrincipal -= (commission+frPenalty+frProfit);
                    }else if(repayCps>0&&repayCps<=commission+frPenalty+frProfit){
                        if(commission<=repayCps){
                            detail.setCommission(detail.getCommission()+commission);
                            repayCps -= commission;
                            commission=0;
                        }else{
                            detail.setCommission(detail.getCommission()+repayCps);
                            commission -= repayCps;
                            repayCps =0;
                        }
                        if(frPenalty<=repayCps){
                            detail.setDeptPenalty(detail.getDeptPenalty()+frPenalty);
                            repayCps -= frPenalty;
                            frPenalty=0;
                        }else{
                            detail.setDeptPenalty(detail.getDeptPenalty()+repayCps);
                            frPenalty -= repayCps;
                            repayCps =0;
                        }
                        if(frProfit<=repayCps){
                            detail.setProfit(detail.getProfit()+frProfit);
                            repayCps -= frProfit;
                            frProfit=0;
                        }else{
                            detail.setProfit(detail.getProfit()+repayCps);
                            frProfit -= repayCps;
                            repayCps =0;
                        }
                        logger.info("佣金{},分润罚息{},分润费用{}",commission,frPenalty,frProfit);
                        //若代偿没出完,继续生成一条新的明细,由借款人出
                        if(commission+frPenalty+frProfit>0){
                            SubjectRepayDetail detail1 = this.saveDetail(subjectId,scheduleId,detail.getUserId(),0,0,0,0,commission,frProfit,frPenalty,
                                    detail.getFreezeRequestNo(),detail.getSourceAccountId(),detail.getChannel(),SubjectRepayDetail.SOURCE_BRW,0,0);
                            list.add(detail1);
                            repayPrincipal -= (commission+frPenalty+frProfit);
                        }
                    }
                }else{
                    SubjectRepayDetail detail2 = this.saveDetail(subjectId,scheduleId,detail.getUserId(),0,0,0,0,commission,
                    frProfit,frPenalty,detail.getFreezeRequestNo(),detail.getSourceAccountId(),detail.getChannel(),detail.getSourceType(),0,0);
                    list.add(detail2);
                    repayPrincipal -= (commission+frPenalty+frProfit);
                }
            }else{
                commission += duePrincipal + dueInterest + duePenalty + dueFee - totalPaidAmt;
                SubjectRepayDetail detail2 = this.saveDetail(subjectId,scheduleId,detail.getUserId(),0,0,0,0,commission,
                        0,0,detail.getFreezeRequestNo(),detail.getSourceAccountId(),detail.getChannel(),detail.getSourceType(),0,0);
                list.add(detail2);
            }
            logger.info("应还投资人总金额{}",commission,totalPaidAmt);
            logger.info("借款人实际金额{},代偿实际金额{}",repayPrincipal,repayCps);
            logger.info("subject {} schedule {} repay detail: borrower paid {}, investor paid {}, commission {}",
                    subjectId, scheduleId, duePrincipal + dueInterest + duePenalty + dueFee, totalPaidAmt, commission);
            List<SubjectRepayDetail> listToSave = new ArrayList<>();
            for (SubjectRepayDetail repayDetail : list) {
                int interest = repayDetail.getInterest() == null ? 0 : repayDetail.getInterest();
                int principal = repayDetail.getPrincipal() == null ? 0 : repayDetail.getPrincipal();
                int penalty = repayDetail.getPenalty() == null ? 0 : repayDetail.getPenalty();
                int fee = repayDetail.getFee() == null ? 0 : repayDetail.getFee();
                int cm = repayDetail.getCommission() == null ? 0 : repayDetail.getCommission();
                int frPenalty = repayDetail.getDeptPenalty() == null ? 0 : repayDetail.getDeptPenalty();
                int frProfit = repayDetail.getProfit() == null ? 0 : repayDetail.getProfit();
                if (interest + principal + penalty + fee + cm + frPenalty + frProfit > 0) {
                    listToSave.add(repayDetail);
                }
            }
            for (SubjectRepayDetail subjectRepayDetail : listToSave) {
                subjectRepayDetail.setCreateTime(DateUtil.getCurrentDateTime19());
                subjectRepayDetailDao.insert(subjectRepayDetail);
            }
        }
        //更新schedule表里的借款人实际出与代偿账户实际出字段,应该都是0
        subjectRepaySchedule.setInterimCpsAmt(repayCps);
        subjectRepaySchedule.setInterimRepayAmt(repayPrincipal);
        this.update(subjectRepaySchedule);
    }


    //给账户打营销款，为还款做准备
    public BaseResponse marketingForRepay(String dest, Double amount, String requestNo) {
        RequestSingleTrans marketingTrans = new RequestSingleTrans();
        if (requestNo == null) {
            requestNo = IdUtil.getRequestNo();
        }
        marketingTrans.setTradeType(TradeType.MARKETING);
        marketingTrans.setRequestNo(requestNo);
        marketingTrans.setTransCode(TransCode.MARKET002_01_TRANSFER.getCode());
        List<RequestSingleTrans.Detail> details = new ArrayList<>();
        RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
        detail.setBizType(BizType.MARKETING);
        detail.setSourcePlatformUserNo(GlobalConfig.MARKETING_ACCOUNT_01_XM);
        detail.setTargetPlatformUserNo(dest);
        detail.setAmount(amount);
        details.add(detail);
        marketingTrans.setDetails(details);

        return transactionService.singleTrans(marketingTrans);
    }

    //冻结账户金额，为还款做准备
    public BaseResponse freezeForRepay(String subjectId, String dest, Double amount, String requestNo) {
        RequestUserAutoPreTransaction request = new RequestUserAutoPreTransaction();
        if (requestNo == null) {
            requestNo = IdUtil.getRequestNo();
        }
        request.setRequestNo(requestNo);
        request.setPlatformUserNo(dest);
        request.setBizType(BizType.REPAYMENT);
        request.setAmount(amount);
        request.setProjectNo(subjectId);
        return transactionService.userAutoPreTransaction(request);
    }


    //提前结清打标记
    public void markAdvanceSubject(SubjectPayoffReg subjectPayoffReg){
        String subjectId = subjectPayoffReg.getSubjectId();
        logger.info("标的-{}提前结清还款打标记开始",subjectId);
        Subject subject = subjectDao.findBySubjectId(subjectId);
        boolean isDirect = this.isDirect(subject.getDirectFlag());
        String intermediatorId = subject.getIntermediatorId().trim();
        //查询居间人用户信息
        User user = userDao.getUserById(intermediatorId);
        //罚息
        int payOffPenalty = subjectAdvancedPayOffService.advancedPayOff(subjectId);
        SubjectRepaySchedule subjectRepaySchedule = this.findRepaySchedule(subjectId, subject.getCurrentTerm());
        if (Subject.REPAY_PAYOFF.equals(subject.getRepayStatus().trim()) || Subject.REPAY_ADVANCED_PAYOFF.equals(subject.getRepayStatus().trim())) {
            //已还款完成，无需再还
            logger.warn("标的{}已到期结束或已提前结清，请勿重复还款", subjectId);
            payoffRegService.onLinePayOff(subjectId);
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
            //若是续贷,或者逾期结清
            /*if(SubjectPayoffReg.SUBJECT_IS_DELAY.equals(subjectPayoffReg.getIsDelay())|| SubjectPayoffReg.STATUS_ADVANCE_PAYOFF_OVERDUE.equals(subjectPayoffReg.getSettlementType())){
                //处理代偿账户冻结
                if(SubjectRepaySchedule.STATUS_NOT_REPAY.equals(subjectRepaySchedule.getStatus())&&SubjectRepaySchedule.SIGN_NOT_REPAY.equals(subjectRepaySchedule.getIsRepay())){
                    subjectRepaySchedule = this.dealCpsAmt(subject,subjectRepaySchedule);
                    if(subjectRepaySchedule!=null){
                        payoffRegService.onLinePayOff(subjectId);
                    }
                }
                return;
            }else{*/
            //查看营销款账户资金是否足够
            double totalActualMoney = ArithUtil.round(platformTransferService.selectTotalSctualMoneyByInterviewerId(intermediatorId),2);
            if (amount > totalActualMoney) {
                logger.warn("标的{}还款提前结清打标记失败，居间人{}在营销款账户余额不足", subjectId,intermediatorId);
                return;
            }
           /* }*/
        }else{
            //查询居间人账户是否充足
            UserAccount repayer = userAccountService.findUserAccount(intermediatorId);
            if (amount > repayer.getAvailableBalance()) {
                logger.warn("标的{}还款提前结清打标记失败，居间人{}账户余额不足", subjectId,intermediatorId);
                return;
            }
        }
        if(SubjectRepaySchedule.STATUS_NOT_REPAY.equals(subjectRepaySchedule.getStatus())&&SubjectRepaySchedule.SIGN_NOT_REPAY.equals(subjectRepaySchedule.getIsRepay())){
            //更新payoff
            payoffRegService.onLinePayOff(subjectId);
            //打标记,将isRepay设为1
            subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_WAIT_REPAY);
        }
        this.update(subjectRepaySchedule);
    }

    /**
     * 处理代偿金额
     * @param subject
     * @param subjectRepaySchedule
     */
    @Transactional
    public SubjectRepaySchedule dealCpsAmt(Subject subject, SubjectRepaySchedule subjectRepaySchedule) {
        logger.info("处理冻结代偿账户,subjectId-{},scheduleId-{}",subject.getSubjectId(),subjectRepaySchedule.getId());
        Integer status = subjectRepaySchedule.getExtStatus();
        String cpsEctSn = subjectRepaySchedule.getExtSnCps();
        BaseResponse baseResponse = null;
        SubjectPayoffReg payOffReg = payoffRegService.getSubjectPayoffReg(subject.getSubjectId());
        Integer amount = 0;
        if(payOffReg!=null && (SubjectPayoffReg.SUBJECT_IS_DELAY.equals(payOffReg.getIsDelay())|| SubjectPayoffReg.STATUS_ADVANCE_PAYOFF_OVERDUE.equals(payOffReg.getSettlementType()))){
            amount = subject.getTotalAmt()-subject.getPaidPrincipal()+subjectRepaySchedule.getDueInterest()+subjectRepaySchedule.getDuePenalty()+subjectRepaySchedule.getDueFee();
        }else{
            Map<String,Integer> borrowerDetails = this.getBorrowerDetails(subjectRepaySchedule);
            amount = borrowerDetails.get("duePrincipal") + borrowerDetails.get("dueInterest") + borrowerDetails.get("duePenalty") + borrowerDetails.get("dueFee");
        }
        AccountCompensationConfig config =  accountCompensationConfigDao.findByDepartmentAndType(subject.getAccountingDepartment(), subject.getType());
        if (config != null) {
            String cpsAccount = config.getCompensationAccount().trim();
            //若之前状态处理中
            if(status!=null && BaseResponse.STATUS_PENDING.equals(status)){
                baseResponse = subjectRepayBillService.preSingleTransQuery(subjectRepaySchedule.getExtSnCps());
                if (BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                    logger.info("代偿账户预处理交易上次处理中，查询结果是上次交易成功，scheduleId={}", subjectRepaySchedule.getId());
                } else if (BaseResponse.STATUS_FAILED.equals(baseResponse.getStatus())) {
                    logger.info("代偿账户预处理交易上次处理中，查询结果是上次交易失败，continue ！scheduleId={}", subjectRepaySchedule.getId());
                } else {
                    logger.info("代偿账户预处理交易上次处理中，查询仍处理中，continue ！scheduleId={}", subjectRepaySchedule.getId());
                }
                status = baseResponse.getStatus();
                cpsEctSn = baseResponse.getRequestNo();
            }

            if(status==null || BaseResponse.STATUS_FAILED.equals(status)){
                UserAccount account = userAccountService.findUserAccount(cpsAccount);
                Integer cpsAccoutBalance = BigDecimal.valueOf(account.getAvailableBalance()).multiply(BigDecimal.valueOf(100)).intValue();
                if(account!=null&&cpsAccoutBalance<amount){
                    logger.info("代偿账户余额不足,代偿账户{},余额{},应还金额{}",cpsAccount,cpsAccoutBalance,amount);
                    return null;
                }
                //若未处理或是失败
                baseResponse = subjectRepayBillService.freezeCpsAcctTrans2(subject.getSubjectId(), cpsAccount, amount);
                status = baseResponse.getStatus();
                cpsEctSn = baseResponse.getRequestNo();
            }
            //若成功
            if(baseResponse!=null && BaseResponse.STATUS_SUCCEED.equals(status)){
                cpsAcctLogService.log(subjectRepaySchedule.getId(), subjectRepaySchedule.getSubjectId(),
                        subjectRepaySchedule.getTerm(), 0, cpsAccount, amount, cpsEctSn, status, CompensatoryAcctLog.TYPE_CPS_OUT);
                userAccountService.freeze(cpsAccount, amount/ 100.0, BusinessEnum.ndr_subject_repay_cps_out, "标的还款代偿冻结-" + subject.getName(), "标的还款代偿冻结,标的ID:" + subject.getSubjectId() + ",金额:" + amount/ 100.0, cpsEctSn,subject.getSubjectId(),subjectRepaySchedule.getId());
                subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_FOR_LOCALACCOUNT);
                subjectRepaySchedule.setCurrentStep("repay");
                subjectRepaySchedule.setCpsStatus(SubjectRepaySchedule.CPS_STATUS_HAS_BEEN);
            }else{
                //若未冻结成功,则将is_repay置为1
                subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_WAIT_REPAY);
                subjectRepaySchedule.setCurrentStep("freeze");
            }
            subjectRepaySchedule.setExtStatus(status);
            subjectRepaySchedule.setExtSnCps(cpsEctSn);
            subjectRepaySchedule.setInitCpsAmt(amount);
            subjectRepaySchedule.setInterimCpsAmt(amount);
            this.update(subjectRepaySchedule);
            return subjectRepaySchedule;
        }else{
            logger.info("未找到对应代偿账户,subjectId-{}",subject.getSubjectId());
            return null;
        }
    }


    //正常还款打标记
    public void markRepaySubject(String subjectId,Integer term){
        logger.info("标的-{}还款打标记,当前期数-{}",subjectId,term);
        Subject subject = subjectDao.findBySubjectId(subjectId);
        SubjectRepaySchedule subjectRepaySchedule = this.findRepaySchedule(subjectId, term);
        boolean isDirect = this.isDirect(subject.getDirectFlag());
        String intermediatorId = subject.getIntermediatorId().trim();
        //还款金额
        double amount = (subjectRepaySchedule.getDuePrincipal()+subjectRepaySchedule.getDueInterest()+subjectRepaySchedule.getDuePenalty()+subjectRepaySchedule.getDueFee())/100.0;
        boolean flag = this.isPossibleForRepay(subjectRepaySchedule);
        if(!flag){
            logger.info("标的不符合还款要求,暂不能还款,subjectId-{}",subjectId);
            return;
        }
        //若是直贷
        if(isDirect){
            //查看营销款账户资金是否足够
            /*double totalActualMoney = ArithUtil.round(platformTransferService.selectTotalSctualMoneyByInterviewerId(intermediatorId),2);
            if (amount > totalActualMoney) {
                logger.warn("标的{}还款打标记失败，居间人{}在营销款账户余额不足", subjectId,intermediatorId);
                return;
            }*/
        }else{
            //查询居间人账户是否充足
            UserAccount repayer = userAccountService.findUserAccount(intermediatorId);
            if (amount > repayer.getAvailableBalance()) {
                logger.warn("标的{}还款打标记失败，居间人{}账户余额不足", subjectId,intermediatorId);
                return;
            }
        }
        //打标记,将isRepay设为1
        if(SubjectRepaySchedule.STATUS_NOT_REPAY.equals(subjectRepaySchedule.getStatus())&&SubjectRepaySchedule.SIGN_NOT_REPAY.equals(subjectRepaySchedule.getIsRepay())){
            subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_WAIT_REPAY);
        }
        this.update(subjectRepaySchedule);
    }


    /**
     * 发厦门还款交易
     */
    public BaseResponse repayForRepay(String subjectId,String borrowerFreezeExtSn,
                                      String borrowerIdXM,SubjectRepayDetail subjectRepayDetail,
                                      Integer principalInterestPenaltyFee,
                                      Integer interest,
                                      Double freezeAmount, String profitAcctXM){
        RequestSingleTrans request = new RequestSingleTrans();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setTradeType(TradeType.REPAYMENT);
        request.setProjectNo(subjectId);
        request.setTransCode(TransCode.SUBJECT_REPAY.getCode());
        List<RequestSingleTrans.Detail> details = new ArrayList<>();
        RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
        detail.setBizType(BizType.REPAYMENT);
        detail.setFreezeRequestNo(borrowerFreezeExtSn);
        detail.setSourcePlatformUserNo(borrowerIdXM);//借款人出款
        detail.setTargetPlatformUserNo(subjectRepayDetail.getUserId());//投资人收款
        detail.setAmount(principalInterestPenaltyFee/100.0);
        detail.setIncome(interest/100.0);
        details.add(detail);

        if ((subjectRepayDetail.getProfit() + subjectRepayDetail.getDeptPenalty()) > 0) {
            RequestSingleTrans.Detail detail3 = new RequestSingleTrans.Detail();
            detail3.setBizType(BizType.PROFIT);
            detail3.setFreezeRequestNo(borrowerFreezeExtSn);
            detail3.setSourcePlatformUserNo(borrowerIdXM);//借款人出分润金额
            detail3.setTargetPlatformUserNo(profitAcctXM);//分润给各事业部
            detail3.setAmount((subjectRepayDetail.getProfit() + subjectRepayDetail.getDeptPenalty()) / 100.0);//分润给各事业部
            details.add(detail3);
        }
        if (subjectRepayDetail.getCommission() > 0) {
            RequestSingleTrans.Detail detail2 = new RequestSingleTrans.Detail();
            detail2.setBizType(BizType.COMMISSION);
            detail2.setFreezeRequestNo(borrowerFreezeExtSn);
            detail2.setSourcePlatformUserNo(borrowerIdXM);//借款人出佣金
            detail2.setAmount(subjectRepayDetail.getCommission()/100.0);
            details.add(detail2);
        }
        if(subjectRepayDetail.getChannel()==2 && principalInterestPenaltyFee>0){
            RequestSingleTrans.Detail detail4 = new RequestSingleTrans.Detail();
            detail4.setBizType(BizType.COMMISSION);
            detail4.setSourcePlatformUserNo(subjectRepayDetail.getUserId());//投资人出佣金
            detail4.setAmount(principalInterestPenaltyFee/100.0);
            details.add(detail4);
            freezeAmount=0.0;
        }
        //若是散标且不是一键投,则不追加冻结
        if(subjectRepayDetail.getChannel()!=0) {
            RequestSingleTrans.Detail detail3 = new RequestSingleTrans.Detail();
            detail3.setBizType(BizType.APPEND_FREEZE);
            detail3.setFreezeRequestNo(subjectRepayDetail.getFreezeRequestNo());
            detail3.setSourcePlatformUserNo(subjectRepayDetail.getUserId());//冻结投资人
            detail3.setAmount(subjectRepayDetail.getChannel()==3?0.0:freezeAmount);
            details.add(detail3);
        }
        request.setDetails(details);
//        logger.info("发厦门还款交易报文{}", JSONObject.toJSONString(request));
        return transactionService.singleTrans(request);
    }

    /**
     * 发厦门还款交易
     */
    public BaseResponse repayForRepayNew(String subjectId,String borrowerFreezeExtSn,
                                      String borrowerIdXM,SubjectRepayDetail subjectRepayDetail,
                                      Integer principalInterestPenaltyFee,
                                      Integer interest,
                                      Double freezeAmount, String profitAcctXM){
        RequestSingleTrans request = new RequestSingleTrans();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setTradeType(TradeType.REPAYMENT);
        request.setProjectNo(subjectId);
        request.setTransCode(TransCode.SUBJECT_REPAY.getCode());
        List<RequestSingleTrans.Detail> details = new ArrayList<>();
        RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
        detail.setBizType(BizType.REPAYMENT);
        detail.setFreezeRequestNo(borrowerFreezeExtSn);
        detail.setSourcePlatformUserNo(borrowerIdXM);//借款人出款
        detail.setTargetPlatformUserNo(subjectRepayDetail.getUserId());//投资人收款
        detail.setAmount((principalInterestPenaltyFee+subjectRepayDetail.getCommission())/100.0);
        detail.setIncome((interest+subjectRepayDetail.getCommission())/100.0);
        details.add(detail);

        if ((subjectRepayDetail.getProfit() + subjectRepayDetail.getDeptPenalty()) > 0) {
            RequestSingleTrans.Detail detail3 = new RequestSingleTrans.Detail();
            detail3.setBizType(BizType.PROFIT);
            detail3.setFreezeRequestNo(borrowerFreezeExtSn);
            detail3.setSourcePlatformUserNo(borrowerIdXM);//借款人出分润金额
            detail3.setTargetPlatformUserNo(profitAcctXM);//分润给各事业部
            detail3.setAmount((subjectRepayDetail.getProfit() + subjectRepayDetail.getDeptPenalty()) / 100.0);//分润给各事业部
            details.add(detail3);
        }
        if (subjectRepayDetail.getCommission() > 0) {
            RequestSingleTrans.Detail detail2 = new RequestSingleTrans.Detail();
            detail2.setBizType(BizType.COMMISSION);
//            detail2.setFreezeRequestNo(borrowerFreezeExtSn);
            detail2.setSourcePlatformUserNo(subjectRepayDetail.getUserIdXm());//投资人出佣金
            detail2.setAmount(subjectRepayDetail.getCommission()/100.0);
            details.add(detail2);
        }
        if(subjectRepayDetail.getChannel()==2 && principalInterestPenaltyFee>0){
            RequestSingleTrans.Detail detail4 = new RequestSingleTrans.Detail();
            detail4.setBizType(BizType.COMMISSION);
            detail4.setSourcePlatformUserNo(subjectRepayDetail.getUserId());//投资人出佣金
            detail4.setAmount(principalInterestPenaltyFee/100.0);
            details.add(detail4);
            freezeAmount=0.0;
        }
        //若是散标且不是一键投,则不追加冻结
        if(subjectRepayDetail.getChannel()!=0) {
            RequestSingleTrans.Detail detail3 = new RequestSingleTrans.Detail();
            detail3.setBizType(BizType.APPEND_FREEZE);
            detail3.setFreezeRequestNo(subjectRepayDetail.getFreezeRequestNo());
            detail3.setSourcePlatformUserNo(subjectRepayDetail.getUserId());//冻结投资人
            detail3.setAmount(subjectRepayDetail.getChannel()==3?0.0:freezeAmount);
            details.add(detail3);
        }
        request.setDetails(details);
//        logger.info("发厦门还款交易报文{}", JSONObject.toJSONString(request));
        return transactionService.singleTrans(request);
    }

    //设置明细内容
    //设置明细内容
    public SubjectRepayDetail saveDetail(String subjectId, Integer scheduleId, String userId,
                                         Integer principal, Integer interest, Integer penalty, Integer fee,
                                         Integer commission,Integer profit,Integer deptPenalty,
                                         String investRequestNo, Integer sourceAccountId, Integer creditChannel, Integer source,Integer bonusInterest,Integer bonusReward){
        SubjectRepayDetail detail = new SubjectRepayDetail();
        detail.setChannel(creditChannel);
        detail.setScheduleId(scheduleId);
        detail.setSubjectId(subjectId);
        detail.setUserId(userId);
        detail.setUserIdXm(userId);
        detail.setPrincipal(principal);
        detail.setInterest(interest);
        detail.setPenalty(penalty);
        detail.setFee(fee);
        detail.setFreezePrincipal(principal);
        detail.setFreezeInterest(interest);
        detail.setFreezePenalty(penalty);
        detail.setFreezeFee(fee);
        detail.setFreezeRequestNo(investRequestNo);
        detail.setStatus(SubjectRepayDetail.STATUS_PENDING);
        detail.setSourceAccountId(sourceAccountId);
        detail.setCommission(commission);
        detail.setProfit(profit);
        detail.setDeptPenalty(deptPenalty);
        detail.setCurrentStep(SubjectRepayDetail.STEP_NOT_TRANS);
        //还款明细新增字段
        detail.setSourceType(source);
        detail.setBonusInterest(bonusInterest);
        detail.setBonusReward(bonusReward);
        return detail;
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
     * 卡贷还款计划
     * @param subject
     */
    public List<SubjectRepaySchedule> makeUpCreditCardRepaySchedule(Subject subject) {
        Config creditCard = configDao.getConfigById("credit_card");
        Integer cardDays = Integer.valueOf(creditCard.getValue());

        Integer contractAmt = subject.getTotalAmt();
        BigDecimal rate = subject.getRate();//借款年利率
        Integer termMonth = subject.getPeriod() > GlobalConfig.ONEMONTH_DAYS ? subject.getTerm() : 1;
        Integer period = subject.getPeriod();
        FinanceCalcUtils.CalcResult calcResult = null;
        if (Subject.REPAY_TYPE_OTRP.equals(subject.getRepayType()) || termMonth.equals(1)) {//针对一期的标都使用一次还本付息拆标
            calcResult = FinanceCalcUtils.calcOTRP(contractAmt, rate, subject.getPeriod());
        } else if (Subject.REPAY_TYPE_MCEI.equals(subject.getRepayType())) {
            String yearMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            String loanDay = yearMonth + day(yearMonth);
            //计息日
            LocalDate loanDate = DateUtil.parseDate(loanDay, DateUtil.DATE_TIME_FORMATTER_8).plusDays(1);

            //资产端推标时间
            AgricultureLoaninfo agricultureLoaninfo = agricultureLoaninfoDao.findByContractId(subject.getContractNo());
            LocalDate createDate = DateUtil.parseDate(agricultureLoaninfo.getCreateTime(), DateUtil.DATE_TIME_FORMATTER_19);

            //本月的账单日
            String currentRepayDay = plusMonths(0) + day(plusMonths(0), agricultureLoaninfo.getRepayDate());
            LocalDate currentDate = DateUtil.parseDate(currentRepayDay, DateUtil.DATE_TIME_FORMATTER_8).plusDays(1);
            //第一个账单日
            String firstRepayDay = plusMonths(1) + day(plusMonths(1), agricultureLoaninfo.getRepayDate());
            LocalDate firstRepayDate = DateUtil.parseDate(firstRepayDay, DateUtil.DATE_TIME_FORMATTER_8).plusDays(1);


            if(createDate.isBefore(currentDate)){
                if(DateUtil.betweenDays(createDate,currentDate) > cardDays + 1){
                    firstRepayDate = currentDate;
                }
            }
            calcResult = FinanceCalcUtils.calcMCEICreditCard(contractAmt, rate, termMonth,loanDate,firstRepayDate);
        } else {
            throw new IllegalArgumentException("不支持的还款类型！");
        }
        List<SubjectRepaySchedule> schedules = new ArrayList<>(termMonth);
        for (int m = 1; m <= termMonth; m++) {
            SubjectRepaySchedule subjectRepaySchedule = new SubjectRepaySchedule();
            if (period>30){
                FinanceCalcUtils.CalcResult.Detail detail = calcResult.getDetails().get(m);
                subjectRepaySchedule.setSubjectId(subject.getSubjectId());
                subjectRepaySchedule.setTerm(m);
                subjectRepaySchedule.setDueDate(detail.getDueDate());
                subjectRepaySchedule.setDuePrincipal(detail.getMonthRepayPrincipal());
                subjectRepaySchedule.setDueInterest(detail.getMonthRepayInterest());
                subjectRepaySchedule.setDuePenalty(0);
                subjectRepaySchedule.setDueFee(0);
                subjectRepaySchedule.setStatus(SubjectRepaySchedule.STATUS_NOT_REPAY);
                subjectRepaySchedule.setIsRepay(0);
                subjectRepaySchedule.setCreateTime(DateUtil.getCurrentDateTime19());
                //直贷二期 新增字段初始化
                subjectRepaySchedule.setRepayPrincipal(0);
                subjectRepaySchedule.setRepayInterest(0);
                subjectRepaySchedule.setRepayPenalty(0);
                subjectRepaySchedule.setRepayFee(0);
                subjectRepaySchedule.setInterimRepayAmt(0);
                subjectRepaySchedule.setInterimCpsAmt(0);
                subjectRepaySchedule.setRepayFee(0);
                subjectRepaySchedule.setCpsStatus(SubjectRepaySchedule.CPS_STATUS_NOT_YET);
            } else {
                FinanceCalcUtils.CalcResult.Detail detail = calcResult.getDetails().get(m);
                subjectRepaySchedule.setSubjectId(subject.getSubjectId());
                subjectRepaySchedule.setTerm(m);
                String date = DateUtil.parseDate(DateUtil.getCurrentDateShort(),DateUtil.DATE_TIME_FORMATTER_8).plusDays(period).format(DateUtil.DATE_TIME_FORMATTER_8);
                subjectRepaySchedule.setDueDate(date);
                subjectRepaySchedule.setDuePrincipal(detail.getMonthRepayPrincipal());
                subjectRepaySchedule.setDueInterest(detail.getMonthRepayInterest());
                subjectRepaySchedule.setDuePenalty(0);
                subjectRepaySchedule.setDueFee(0);
                subjectRepaySchedule.setStatus(SubjectRepaySchedule.STATUS_NOT_REPAY);
                subjectRepaySchedule.setIsRepay(0);
                subjectRepaySchedule.setCreateTime(DateUtil.getCurrentDateTime19());
                //直贷二期 新增字段初始化
                subjectRepaySchedule.setRepayPrincipal(0);
                subjectRepaySchedule.setRepayInterest(0);
                subjectRepaySchedule.setRepayPenalty(0);
                subjectRepaySchedule.setRepayFee(0);
                subjectRepaySchedule.setInterimRepayAmt(0);
                subjectRepaySchedule.setInterimCpsAmt(0);
                subjectRepaySchedule.setRepayFee(0);
                subjectRepaySchedule.setCpsStatus(SubjectRepaySchedule.CPS_STATUS_NOT_YET);
            }
            schedules.add(subjectRepaySchedule);
            subjectRepayScheduleDao.insert(subjectRepaySchedule);
        }
        return schedules;
    }

    private String day(String yearMonthStr, Integer repayDate) {
        YearMonth yearMonth = YearMonth.parse(yearMonthStr, DateTimeFormatter.ofPattern("yyyyMM"));
        int currentDay = repayDate;
        int day = yearMonth.lengthOfMonth() > currentDay ? currentDay : yearMonth.lengthOfMonth();
        return String.valueOf(day < 10 ? "0" + day : day);
    }


    /**
     * 查询未还款计划
     * @param subjectId
     * @return
     */
    public List<SubjectRepaySchedule> getSubjectRepayScheduleBySubjectIdNotRepay(String subjectId){
        return subjectRepayScheduleDao.findSubjectRepayScheduleBySubjectIdNotRepay(subjectId);
    }

    public int insertSchedule(SubjectRepaySchedule schedule){
        return subjectRepayScheduleDao.insert(schedule);
    }

    /**
     * 按subjectId和status查询一条
     * @param subjectId
     * @param status
     * @return
     */
    public SubjectRepaySchedule findBySubjectIdAnsStatus(String subjectId,Integer status){
        return subjectRepayScheduleDao.findBySubjectIdAnsStatus(subjectId,status);
    }


    /**
     * 按期数查询 应还日期倒叙排,取第一个的还款日
     * @param subjectId
     * @param term
     * @return
     */
    public String findBySubjectIdAndTermOrderByDuedate(String subjectId,Integer term){
        return subjectRepayScheduleDao.findBySubjectIdAndTermOrderByDuedate(subjectId,term);
    }

    /**
     * 查询已还最近的schedule
     * @param subjectIds
     * @param status
     * @return
     */
    public List<SubjectRepaySchedule> findBySubjectIdInAndStatus(Set<String> subjectIds,Integer[] status){
        return subjectRepayScheduleDao.findBySubjectIdInAndStatus(subjectIds,status);
    }

    public List<SubjectRepaySchedule> getByStatusAndSubjectIdIn(Set<String> subjectIds,Set<Integer> status){

        return subjectRepayScheduleDao.findByStatusAndSubjectIdIn(subjectIds,status);
    }

    /**
     * 生成还款计划
     */
    @Transactional
    public FinanceCalcUtils.CalcResult calculationOfRepaymentPlan(Subject subject) {
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

        return calcResult;
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
        //
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
        Integer currentTerm = term;
        Integer creditPack = iPlan.getPackagingType();
        //查询当前是省心投的第几期
        if(IPlan.PACKAGING_TYPE_CREDIT.equals(creditPack)){
            currentTerm = iPlanRepayScheduleService.getCurrentTermByIplanId(iPlan.getId(),schedule.getDueDate());
        }

        //是否过了锁定期
        boolean isPassedLockDay = false;
        //从初始到现在持有时间
        long days = DateUtil.betweenDays(IPlan.PACKAGING_TYPE_CREDIT.equals(creditPack) ? credit.getStartTime().substring(0,8): subject.getLendTime().substring(0,8), endDate);
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
            creditInterest = subjectAccountService.calculateInterest(startDate, endDate, new BigDecimal(principal), rate,subject.getPeriod(),true,true,creditPack,currentTerm);
            //若加息利率大于0,则计算加息利息
            if (bonusRate != null && bonusRate.compareTo(BigDecimal.valueOf(0)) > 0) {
                Integer addTerm = activityMarkConfigureDao.findTermById(iPlan.getActivityId());
                if (addTerm != null && addTerm < currentTerm) {//若使用的按月加息,则判断期数是否已用完
                    bonusInterest = BigDecimal.ZERO;
                } else {
                    bonusInterest = subjectAccountService.calculateInterest(startDate, endDate, new BigDecimal(principal), bonusRate, subject.getPeriod(), true,true,creditPack,currentTerm);
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

    public SubjectRepaySchedule findRepayScheduleNotRepayOnlyOne(String subjectId){

        return subjectRepayScheduleDao.findRepayScheduleNotRepayOnlyOne(subjectId);
    }

    /**
     * 校验是否可以还款
     * @param subjectRepaySchedule
     * @return
     */
    public boolean isPossibleForRepay(SubjectRepaySchedule subjectRepaySchedule){
        String subjectId = subjectRepaySchedule.getSubjectId();
        if (!SubjectRepaySchedule.STATUS_NOT_REPAY.equals(subjectRepaySchedule.getStatus())
                && !SubjectRepaySchedule.STATUS_OVERDUE.equals(subjectRepaySchedule.getStatus())
                ) {
            //已还款完成，无需再还
            logger.warn("标的{}第{}期已还款完成，请勿重复还款", subjectId, subjectRepaySchedule.getTerm());
            return false;
        }
        //未完成的转让中债权
        List<CreditOpening> unlendedCreditOpenings = creditOpeningDao.findNotLendedBySubjectId(subjectId);
        if (unlendedCreditOpenings != null && unlendedCreditOpenings.size() > 0) {
            //标的有未完成的转让中债权，不能还款
            logger.warn("标的{}有未完成的转让中债权，暂不能还款", subjectId);
            return false;
        }
        //查询债权关系
        List<Credit> credits = creditService.findAllCreditBySubjectId(subjectId);
        if (credits.stream().anyMatch(credit -> credit.getCreditStatus().equals(Credit.CREDIT_STATUS_WAIT))) {
            //标的有未确认债权，不能还款
            logger.warn("标的{}有未确认债权，暂不能还款", subjectId);
            return false;
        }
        return true;
    }

    /**
     * 计算集合本金总和
     * @param details
     * @return
     */
    public Integer getTotalAmt(Map<String, Map<String, Object>> details){
        Integer totalMoney = 0;
        if(details!=null){
            for (Map.Entry<String, Map<String, Object>> entry : details.entrySet()) {
                Map<String, Object> result = entry.getValue();
                if(!result.isEmpty()){
                    totalMoney += (Integer)result.get("principal");
                }
            }
        }
        return totalMoney;
    }

    /**
     * 查询标的原始利率
     * @param subject
     * @return
     */
    public BigDecimal getOriginalRate(Subject subject){
        BigDecimal originalRate = subject.getRate();
        if(Subject.SUBJECT_TYPE_CAR.equals(subject.getType())){
            originalRate = subjectService.findRateByContractNoFromLoanIntermediaries(subject.getContractNo());
        }else {
            originalRate = subjectService.findRateByContractNoFromAgricultureLoaninfo(subject.getContractNo());
        }
        return originalRate;

    }
}
