package com.jiuyi.ndr.service.redpacket;

import com.duanrong.util.InterestUtil;
import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.dao.redpacket.InvestRedPacketDao;
import com.jiuyi.ndr.dao.redpacket.RedPacketDetailDao;
import com.jiuyi.ndr.dao.subject.SubjectAccountDao;
import com.jiuyi.ndr.dao.subject.SubjectTransLogDao;
import com.jiuyi.ndr.dao.subject.SubjectTransferParamDao;
import com.jiuyi.ndr.dao.user.RedPacketDao;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.redpacket.InvestRedpacket;
import com.jiuyi.ndr.domain.redpacket.RedPacketDetail;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectAccount;
import com.jiuyi.ndr.domain.subject.SubjectTransLog;
import com.jiuyi.ndr.domain.subject.SubjectTransferParam;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.invest.InvestService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanTransLogService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Created by drw on 2017/6/13.
 */
@Service
public class RedpacketService {

    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;

    @Autowired
    private RedPacketDetailDao redPacketDetailDao;

    @Autowired
    private InvestRedPacketDao investRedPacketDao;

    @Autowired
    private SubjectTransLogDao subjectTransLogDao;

    @Autowired
    private SubjectAccountDao subjectAccountDao;

    @Autowired
    private CreditDao creditDao;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private RedPacketDao redPacketDao;

    @Autowired
    private IPlanDao iPlanDao;

    @Autowired
    private CreditOpeningDao creditOpeningDao;

    @Autowired
    private UserService userService;

    @Autowired
    private InvestService investService;
    @Autowired
    private ConfigDao configDao;
    @Autowired
    private SubjectTransferParamDao subjectTransferParamDao;
    @Autowired
    private IPlanTransLogService iPlanTransLogService;
    @Autowired
    private IPlanAccountService iPlanAccountService;

    private static final Logger logger = LoggerFactory.getLogger(RedpacketService.class);
    /**
     * 最大补息利率
     */
    private static final LocalDateTime CHANGE_RATE_TIME =  DateUtil.parseDateTime("2018-05-07 12:00:00",DateUtil.DATE_TIME_FORMATTER_19);

    public RedPacketDetail save(RedPacketDetail redPacketDetail) {
        redPacketDetailDao.insert(redPacketDetail);
        return redPacketDetail;
    }

    public RedPacket getRedPacketById(int redPacketId) {
        if (redPacketId == 0) {
            throw new IllegalArgumentException("redPacketId不能为空");
        }
        return redPacketDao.getRedPacketById(redPacketId);
    }
    /**
     * 月月盈生成补息和加息券奖励金额
     *
     * @param iPlan
     */
    public void createPacketInvest(IPlan iPlan) {

        List<IPlanTransLog> list = iPlanTransLogDao.findByIPlanId(iPlan.getId());
        if (list==null||list.size()<=0){
            logger.info("此理财计划没有投资记录:{}",iPlan.getId());
            return;
        }

        BigDecimal rate = iPlan.getFixRate();
        String date = DateUtil.getCurrentDateTime(DateUtil.DATE_10);
        if(iPlan.getBonusRate().compareTo(BigDecimal.ZERO)==1){
            rate = rate.add(iPlan.getBonusRate());
        }
        for (IPlanTransLog iPlanTransLog:list) {
            if (IPlanTransLog.TRANS_TYPE_PRINCIPLE_REINVEST.equals(iPlanTransLog.getTransType())
                    ||IPlanTransLog.TRANS_TYPE_PROFIT_INCOME.equals(iPlanTransLog.getTransType())
                    ||IPlanTransLog.TRANS_TYPE_NORMAL_INCOME.equals(iPlanTransLog.getTransType())
                    ||IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT.equals(iPlanTransLog.getTransType())
                    ||IPlanTransLog.TRANS_TYPE_NORMAL_EXIT.equals(iPlanTransLog.getTransType())){
                logger.info("该笔投资{}交易交易类型{}，跳过",iPlanTransLog.getId(),iPlanTransLog.getTransType());
                continue;
            }
            if (IPlanTransLog.TRANS_STATUS_TO_CANCEL.equals(iPlanTransLog.getTransStatus())){
                logger.info("该笔理财计划交易已流标:{}",iPlanTransLog.getId());
                continue;
            }
            if (BaseResponse.STATUS_SUCCEED.equals(iPlanTransLog.getExtStatus())){
                Integer money = iPlanTransLog.getTransAmt();
                Integer investAllowanceInterest = 0;
                if (IPlanTransLog.FLAG_YJT.equals(iPlanTransLog.getFlag())){
                    InvestRedpacket ir = new InvestRedpacket();
                    ir.setRepackedOrder( "YJT"
                            + iPlanTransLog.getExtSn());
                    int size = redPacketDetailDao.findCountByRepackedOrderOnly(ir.getRepackedOrder());
                    if (size > 0) {
                        return;
                    }
                    double d = 0;
                    // 大于0表示该笔投资使用加息券
                    if (iPlanTransLog.getRedPacketId()!=null &&
                            iPlanTransLog.getRedPacketId() > 0) {
                        RedPacket packet = redPacketDetailDao.findRedPacketById(iPlanTransLog.getRedPacketId());
                        //todo 月月盈放款省心投不再发放全程加息券(随心投修改-jgx-5.16)
                        SubjectTransferParam subjectTransferParam = subjectTransferParamDao.findByTransferParamCode(iPlan.getTransferParamCode());
                        if(iPlanTransLogService.isNewFixIplan(subjectTransferParam)){
                            if (packet != null && !packet.getType().equals(RedPacket.TYPE_DEDUCT) && !packet.getType().equals(RedPacket.TYPE_RATE)) {
                                d = getRedpacketMoney(packet,money/100.0,iPlan);
                            }
                        }else{
                            if (packet != null && !packet.getType().equals(RedPacket.TYPE_DEDUCT)) {
                                d = getRedpacketMoney(packet,money/100.0,iPlan);
                            }
                        }
                        logger.info("使用红包卷的理财计划交易：{},奖励金额：{}",iPlanTransLog.getId(),d);
                    }
                    // 如果补息或者奖励金额大于0,则 创建补息和奖励记录
                    if (d > 0) {
                        ir.setId(IdUtil.randomUUID());
                        ir.setInvestId(iPlanTransLog.getId().toString());
                        ir.setLoanId(iPlanTransLog.getIplanId().toString());
                        ir.setRewardMoney(d);

                        ir.setRepackedOrder("YJT"
                                + iPlanTransLog.getExtSn());
                        ir.setType(InvestRedpacket.RED_PACKET_TYPE_IPLN);
                        ir.setSendRedpacketStatus(0);
                        ir.setSendAllowanceStatus(0);
                        ir.setRepackedId(iPlanTransLog.getRedPacketId());
                        ir.setCreateTime(new Date());
                        ir.setUserId(iPlanTransLog.getUserId());
                        investRedPacketDao.insert(ir);
                    }
                } else {
                    String transTime = DateUtil.parseDateTime(iPlanTransLog.getTransTime(),DateUtil.DATE_TIME_FORMATTER_19).format(DateUtil.DATE_TIME_FORMATTER_10);
                    Integer dayDifference = DateUtil.dayDifference(transTime,date )-1;// 补多少天的利息
                    if (dayDifference > 0) {
                        rate = getRealRate(rate, DateUtil.parseDateTime(iPlan.getRaiseOpenTime(), DateUtil.DATE_TIME_FORMATTER_19));
                        investAllowanceInterest = FinanceCalcUtils.calcInterest(money, rate, dayDifference);
                    }
                    InvestRedpacket ir = new InvestRedpacket();
                    ir.setAllowanceOrder( "TZLCJHBX"
                            + iPlanTransLog.getExtSn());
                    ir.setRepackedOrder( "LCJH"
                            + iPlanTransLog.getExtSn());
                    int size = redPacketDetailDao.findCountByRepackedOrder(ir.getRepackedOrder(),ir.getAllowanceOrder());
                    if (size > 0) {
                        return;
                    }
                    double d = 0;
                    // 大于0表示该笔投资使用加息券
                    if (iPlanTransLog.getRedPacketId()!=null &&
                            iPlanTransLog.getRedPacketId() > 0) {
                        RedPacket packet = redPacketDetailDao.findRedPacketById(iPlanTransLog.getRedPacketId());
                        if (packet != null && !packet.getType().equals(RedPacket.TYPE_DEDUCT)) {
                            d = getRedpacketMoney(packet,money/100.0,iPlan);
                        }
                        logger.info("使用红包卷的理财计划交易：{},奖励金额：{}",iPlanTransLog.getId(),d);
                    }
                    // 如果补息或者奖励金额大于0,则 创建补息和奖励记录
                    if ( investAllowanceInterest > 0 || d > 0) {
                        ir.setId(IdUtil.randomUUID());
                        ir.setInvestId(iPlanTransLog.getId().toString());
                        ir.setLoanId(iPlanTransLog.getIplanId().toString());
                        ir.setRewardMoney(d);
                        ir.setInvestAllowanceInterest(investAllowanceInterest/100.0);
                        ir.setAllowanceOrder("TZLCJHBX"
                                + iPlanTransLog.getExtSn());
                        ir.setRepackedOrder("LCJH"
                                + iPlanTransLog.getExtSn());
                        ir.setType(InvestRedpacket.RED_PACKET_TYPE_IPLN);
                        ir.setSendRedpacketStatus(0);
                        ir.setSendAllowanceStatus(0);
                        ir.setRepackedId(iPlanTransLog.getRedPacketId());
                        ir.setCreateTime(new Date());
                        ir.setUserId(iPlanTransLog.getUserId());
                        investRedPacketDao.insert(ir);
                    }
                }

            }
        }
    }
    /**
     * 一键投标的补息
     *@param subject
     */
    public void createSubjectPacketInvestByYjt(Subject subject){
        List<Credit> credits = creditDao.findAllCreditBySubjectId(subject.getSubjectId());
        for (Credit credit:credits) {
            logger.info("一键投标的补息,债权id：{}",credit.getId());
            if (!credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_YJT)){
                logger.info("此债权来源不是一键投无法补息:{}",credit.getId());
                continue;
            }

            String date = DateUtil.getCurrentDateTime(DateUtil.DATE_10);
            IPlanTransLog iPlanTransLog = iPlanTransLogDao.findById(credit.getSourceChannelId());
            IPlan iPlan = iPlanDao.findById(iPlanTransLog.getIplanId());
            BigDecimal rate = iPlan.getFixRate();
            if(iPlan.getBonusRate().compareTo(BigDecimal.ZERO)==1){
                rate = rate.add(iPlan.getBonusRate());
            }
            if (BaseResponse.STATUS_SUCCEED.equals(iPlanTransLog.getExtStatus())){
                Integer money = credit.getHoldingPrincipal();
                //获取散标投资时间
                String transTime = DateUtil.parseDateTime(iPlanTransLog.getTransTime(),DateUtil.DATE_TIME_FORMATTER_19).format(DateUtil.DATE_TIME_FORMATTER_10);
                //计算补息天数
                Integer dayDifference = DateUtil.dayDifference(transTime,date )-1;// 补多少天的利息
                Integer investAllowanceInterest = 0;
                if (dayDifference > 0) {
                    rate = getRealRate(rate, DateUtil.parseDateTime(subject.getOpenTime(), DateUtil.DATE_TIME_FORMATTER_17));
                    investAllowanceInterest = FinanceCalcUtils.calcInterest(money, rate, dayDifference);
                }
                InvestRedpacket ir = new InvestRedpacket();
                ir.setAllowanceOrder( "YJTBX"
                        + iPlanTransLog.getExtSn());
                int size = redPacketDetailDao.findCountByAllowanceOrder(ir.getAllowanceOrder());
                logger.info("省心投标的放款补息，补息金额-{}，补息天数-{}",investAllowanceInterest,dayDifference);
                if (size > 0) {
                    ir.setAllowanceOrder("YJTBX"
                            + iPlanTransLog.getExtSn()+size);
                }
                // 如果补息或者奖励金额大于0,则 创建补息和奖励记录
                if ( investAllowanceInterest > 0) {
                    ir.setId(IdUtil.randomUUID());
                    ir.setInvestId(iPlanTransLog.getId().toString());
                    ir.setLoanId(iPlanTransLog.getIplanId().toString());
                    ir.setRewardMoney(0);
                    ir.setInvestAllowanceInterest(investAllowanceInterest/100.0);
                    ir.setAllowanceOrder(ir.getAllowanceOrder());
                    ir.setRepackedOrder(null);
                    ir.setType(InvestRedpacket.RED_PACKET_TYPE_IPLN);
                    ir.setSendRedpacketStatus(0);
                    ir.setSendAllowanceStatus(0);
                    ir.setRepackedId(0);
                    ir.setCreateTime(new Date());
                    ir.setUserId(iPlanTransLog.getUserId());
                    investRedPacketDao.insert(ir);
                }
            }
        }
    }
    /**
     * 散标
     * 生成补息和加息券奖励金额
     *
     * @param subject
     */
    public void createSubjectPacketInvest(Subject subject) {

        List<SubjectTransLog> list = subjectTransLogDao.findBySubjectIdAndStatus(subject.getSubjectId(),SubjectTransLog.TRANS_TYPE_NORMAL_IN);
        if (list==null||list.size()<=0){
            logger.info("此散标没有投资记录:{}");
            return;
        }

        BigDecimal rate = subject.getInvestRate();
        String date = DateUtil.getCurrentDateTime(DateUtil.DATE_10);
        if(subject.getBonusRate().compareTo(BigDecimal.ZERO)==1){
            rate = rate.add(subject.getBonusRate());
        }
        for (SubjectTransLog subjectTransLog:list) {
            try {
                if (!SubjectTransLog.TRANS_STATUS_SUCCEED.equals(subjectTransLog.getTransStatus())){
                    logger.info("该笔散标交易未成功:{}",subjectTransLog.getId());
                    continue;
                }
                if (BaseResponse.STATUS_SUCCEED.equals(subjectTransLog.getExtStatus())){
                    //获取散标投资金额
                    Integer money = subjectTransLog.getTransAmt();
                    //获取散标投资时间
                    String transTime = DateUtil.parseDateTime(subjectTransLog.getTransTime(),DateUtil.DATE_TIME_FORMATTER_19).format(DateUtil.DATE_TIME_FORMATTER_10);
                    //计算补息天数
                    Integer dayDifference = DateUtil.dayDifference(transTime,date )-1;// 补多少天的利息
                    Integer investAllowanceInterest = 0;
                    if (dayDifference > 0) {
                        rate = getRealRate(rate, DateUtil.parseDateTime(subject.getOpenTime(), DateUtil.DATE_TIME_FORMATTER_17));
                        investAllowanceInterest = FinanceCalcUtils.calcInterest(money, rate, dayDifference);
                    }
                    InvestRedpacket ir = new InvestRedpacket();
                    ir.setAllowanceOrder( "TZSBBX"
                            + subjectTransLog.getExtSn());
                    ir.setRepackedOrder( "SUBJECT"
                            + subjectTransLog.getExtSn());
                    int size = redPacketDetailDao.findCountByRepackedOrder(ir.getRepackedOrder(),ir.getAllowanceOrder());
                    if (size > 0) {
                        return;
                    }
                    double d = 0;
                    // 大于0表示该笔投资使用加息券
                    if (subjectTransLog.getRedPacketId()!=null &&
                            subjectTransLog.getRedPacketId() > 0) {
                        RedPacket packet = redPacketDetailDao.findRedPacketById(subjectTransLog.getRedPacketId());
                        //todo 散标放款不再发放全程加息券(随心投修改-jgx-5.16)
                        SubjectTransferParam subjectTransferParam = subjectTransferParamDao.findByTransferParamCode(subject.getTransferParamCode());
                        if(iPlanTransLogService.isNewFixIplan(subjectTransferParam)){
                            if (packet != null && !packet.getType().equals(RedPacket.TYPE_DEDUCT) && !packet.getType().equals(RedPacket.TYPE_RATE)) {
                                d = getRedpacketMoneyCommon(packet, subject, money / 100.0);
                            }
                        }else{
                            if (packet != null && !packet.getType().equals(RedPacket.TYPE_DEDUCT)) {
                                d = getRedpacketMoneyCommon(packet,subject,money/100.0);
                            }
                        }
                        logger.info("使用红包卷的散标交易：{},奖励金额：{}",subjectTransLog.getId(),d);
                    }
                    // 如果补息或者奖励金额大于0,则 创建补息和奖励记录
                    if ( investAllowanceInterest > 0 || d > 0) {
                        ir.setId(IdUtil.randomUUID());
                        ir.setInvestId(subjectTransLog.getId().toString());
                        ir.setLoanId(subjectTransLog.getSubjectId());
                        ir.setRewardMoney(d);
                        ir.setInvestAllowanceInterest(investAllowanceInterest/100.0);
                        ir.setAllowanceOrder("TZSBBX"
                                + subjectTransLog.getExtSn());
                        ir.setRepackedOrder("SUBJECT"
                                + subjectTransLog.getExtSn());
                        ir.setType(InvestRedpacket.RED_PACKET_TYPE_SUBJECT);
                        ir.setSendRedpacketStatus(0);
                        ir.setSendAllowanceStatus(0);
                        ir.setRepackedId(subjectTransLog.getRedPacketId());
                        ir.setCreateTime(new Date());
                        ir.setUserId(subjectTransLog.getUserId());
                        investRedPacketDao.insert(ir);
                    }
                    //更新账户奖励情况
                    SubjectAccount subjectAccount = subjectAccountDao.findByTransLogId(subjectTransLog.getId());
                    subjectAccount.setPaidReward(subjectAccount.getExpectedReward());
                    subjectAccount.setExpectedReward(0);
                    subjectAccountDao.update(subjectAccount);
                }
            } catch (Exception e){
                logger.error("散标生产加息及补息奖励异常userId-{},subjectId-{}",subjectTransLog.getUserId(),subjectTransLog.getId(),e);
            }
        }
    }
    //获取红包卷加息金额(调整)
    public double getRedpacketMoney(RedPacket redPacket,double money,IPlan iPlan) {

        String type = redPacket.getType();
        String repayType = iPlan.getRepayType();
        int interestAccrualType = iPlan.getInterestAccrualType();
        int periods = 0 ;
        if (IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(interestAccrualType)){
            periods = iPlan.getDay();
        } else {
            periods = iPlan.getTerm();
        }
        String status = redPacket.getSendStatus();
        double rate = redPacket.getRate();
        double d = 0;
        if (23 == redPacket.getRuleId()) {// 如果红包规则是23则金额最高计算上限是5万
            money = money > 50000 ? 50000 : money;
        }
        //普通加息券就是加到锁定期为止
        if (RedPacket.SEND_STATUS_USED.equals(status)){
            if (RedPacket.TYPE_RATE.equals(type)) {
                if (IPlan.REPAY_TYPE_MCEI.equals(repayType)){
                    repayType = "等额本息";
                    d = InterestUtil.getInterestByPeriodMoth(money,rate,periods,repayType);
                } else {
                    if (IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(interestAccrualType)){
                        d = InterestUtil.getInterestByPeriodDay(money, rate, periods);
                    } else {
                        d = InterestUtil.getRFCLInterestByPeriodMoth(money, rate, periods);
                    }
                }
            } else if (RedPacket.TYPE_RATE_BY_DAY.equals(type)) {
                //按天加息券，如果天数小于锁定期，按加息劵天数加息
                if (IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(interestAccrualType)){
                    periods = periods < redPacket.getRateDay() ? periods : redPacket.getRateDay();
                } else {
                    periods = redPacket.getRateDay();
                }
                d = InterestUtil.getInterestByPeriodDay(money, rate, periods);

            } else if (RedPacket.TYPE_MONEY.equals(type)) {
                // 固定金额奖励的情况下
                d = redPacket.getMoney();
            }
        }
        return d;
    }

    //获取红包卷加息金额(调整)
    public double getRedpacketMoney(RedPacket redPacket,Integer periods,double money,IPlan iPlan) {

        String type = redPacket.getType();
        String status = redPacket.getSendStatus();
        double rate = redPacket.getRate();
        String repayType = iPlan.getRepayType();
        int interestAccrualType = iPlan.getInterestAccrualType();
        if (IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(interestAccrualType)){
            periods = iPlan.getDay();
        } else {
            periods = iPlan.getTerm();
            if(iPlanAccountService.isNewIplan(iPlan)){
                periods = iPlan.getExitLockDays()/31==0?1:iPlan.getExitLockDays()/31;
            }
        }
        double d = 0;
        if (23 == redPacket.getRuleId()) {// 如果红包规则是23则金额最高计算上限是5万
            money = money > 50000 ? 50000 : money;
        }
        //普通加息券就是加到锁定期为止
        if (RedPacket.SEND_STATUS_USED.equals(status)){
            if (RedPacket.TYPE_RATE.equals(type)) {
                if (IPlan.REPAY_TYPE_MCEI.equals(repayType)){
                    repayType = "等额本息";
                    d= InterestUtil.getInterestByPeriodMoth(money,rate,periods,repayType);
                }else {
                    if (IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(interestAccrualType)){
                        d = InterestUtil.getInterestByPeriodDay(money, rate, periods);
                    } else {
                        d = InterestUtil.getRFCLInterestByPeriodMoth(money, rate, periods);
                    }
                }
            } else if (RedPacket.TYPE_RATE_BY_DAY.equals(type)) {
                //按天加息券，如果天数小于锁定期，按加息劵天数加息
                if (IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(interestAccrualType)){
                    periods = periods < redPacket.getRateDay() ? periods : redPacket.getRateDay();
                } else {
                    periods = redPacket.getRateDay();
                }
                d = InterestUtil.getInterestByPeriodDay(money, rate, periods);

            } else if (RedPacket.TYPE_MONEY.equals(type)) {
                // 固定金额奖励的情况下
                d = redPacket.getMoney();
            }
        }
        return d;
    }

    /**
     * 散标获取红包卷加息金额(调整)
     * @param redPacket
     * @param subject
     * @param money
     * @return
     */
    public double getRedpacketMoneyCommon(RedPacket redPacket,Subject subject, double money){
        String type = redPacket.getType();
        String status = redPacket.getSendStatus();
        double rate = redPacket.getRate();
        double d = 0.0;
        if (23 == redPacket.getRuleId()) {// 如果红包规则是23则金额最高计算上限是5万
            money = money > 50000 ? 50000 : money;
        }
        //普通加息券就是加到锁定期为止

            String repayType=subjectService.getRepayType(subject);
            //期数
            int term = subject != null ? subject.getTerm() : 0;
            //天数
            int period = subject != null ? subject.getPeriod() : 0;
            if (type.equals(RedPacket.TYPE_RATE)) {
                if(period<30){
                    d= InterestUtil.getInterestByPeriodDay(money,rate,period);
                }
                else{
                    d= InterestUtil.getInterestByPeriodMoth(money,rate,term,repayType);
                }
            } else if (type.equals(RedPacket.TYPE_RATE_BY_DAY)) {
                //按天加息券，如果天数小于期限数，按加息劵天数加息
                int rateDay = period <= redPacket.getRateDay() ? period : redPacket.getRateDay();
                d = InterestUtil.getInterestByPeriodDay(money, rate, rateDay);
            } else if (RedPacket.TYPE_MONEY.equals(type)) {
                // 固定金额奖励的情况下
                d = redPacket.getMoney();
            }
        return d;
    }

    public RedPacket getRedPacketByIdLocked(int redPacketId) {
        if (redPacketId == 0) {
            throw new IllegalArgumentException("redPacketId不能为空");
        }
        return redPacketDao.getRedPacketByIdLocked(redPacketId);
    }
    public void update(RedPacket redPacket) {
        if (redPacket == null) {
            throw new IllegalArgumentException("redPacket can not be null");
        }
        redPacketDao.update(redPacket);
    }

    /**
     * 校验红包是否可用
     * @param userId
     * @param redPacketId
     * @param iPlan
     * @param userSource
     * @param money
     */
    public void verifyRedPacket(String userId, int redPacketId, IPlan iPlan, String userSource, double money) {
        logger.warn("校验红包是否可用，userId:" + userId + ",redPacketId:" + redPacketId + ",iPlan:" + iPlan.toString() + ",userSource:"
                + ",money:" + money);
        if (StringUtils.isBlank(userId) || redPacketId == 0 || iPlan == null || StringUtils.isBlank(userSource)) {
            logger.warn("用户: " + userId + "使用红包id为0");
            throw new ProcessException(Error.NDR_0101);
        }
        User user = userService.getUserById(userId);
        if (user == null) {
            logger.warn("用户: " + userId +"不存在");
            throw new ProcessException(Error.NDR_0419);
        }
        String mobileNumber = user.getMobileNumber();
        if (mobileNumber == null){
            logger.warn("用户：" + userId + "手机号不存在");
            throw new ProcessException(Error.NDR_0431);
        }
        RedPacket redPacket = new RedPacket(redPacketId, mobileNumber, null, null, null,
                null, 0);
        List<RedPacket> redPackets = redPacketDao.getRedPacketsByCondition(redPacket);
        if (redPackets != null && redPackets.size() == 1) {
            redPacket = redPackets.get(0);
        } else {
            logger.warn("用户：" + userId + "使用的红包：" + redPacketId + "不存在");
            throw new ProcessException(Error.NDR_0432);
        }
        if (redPacket != null) {
            //排除天天赚红包
            if (StringUtils.equals(RedPacket.TYPE_RATELPLAN, redPacket.getType())) {
                throw new ProcessException(Error.NDR_04391);
            }
            //天天赚转投月月盈专属红包
            if(redPacket.getSpecificType() != null && redPacket.getSpecificType() != 1){
                throw new ProcessException(Error.NDR_04392);
            }
            if(redPacket.getSpecificType() == null ){
                throw new ProcessException(Error.NDR_04392);
            }
            /*
             *   useLoanType：1,新手标不可使用，2，APP专享，3，APP专享且新手标不可用
             */
            if (iPlan.getNewbieOnly().equals(IPlan.NEWBIE_ONLY_Y) && redPacket.getUseLoanType() == 1) {
                logger.warn("用户ID：" + userId + ",项目ID：" + iPlan.getId()
                        + "redpacketId:" + redPacketId + "此加息券新手标不可使用");
                throw new ProcessException(Error.NDR_0433);
            }
            if (iPlan.getNewbieOnly().equals(IPlan.NEWBIE_ONLY_Y) && redPacket.getUseLoanType() == 3) {
                logger.warn("用户ID：" + userId + ",项目ID：" + iPlan.getId()
                        + "redpacketId:" + redPacketId + "此红包券APP投资专享且新手标不可用");
                throw new ProcessException(Error.NDR_0434);
            }
            if (redPacket.getUseLoanType() == 2 && !userSource.contains("ios") && !userSource.contains("android")) {
                logger.warn("用户ID：" + userId + ",项目ID：" + iPlan.getId()
                        + "redpacketId:" + redPacketId + "此加息券APP专享");
                throw new ProcessException(Error.NDR_0435);
            }
            if (redPacket.getUseLoanType() == 3 && !userSource.contains("ios") && !userSource.contains("android")) {
                logger.warn("用户ID：" + userId + ",项目ID：" + iPlan.getId()
                        + "redpacketId:" + redPacketId + "此红包券APP投资专享且新手标不可用");
                throw new ProcessException(Error.NDR_0434);
            }
            // 投资周期限制
            if (redPacket.getInvestCycle() > 0 && redPacket.getInvestCycle() > iPlan.getTerm()) {
                logger.warn("用户ID：" + userId + "，项目ID：" + iPlan.getId()
                        + "redPacketId:" + redPacketId + "此红包券仅限" + redPacket.getInvestCycle() + "个月及以上标的使用");
                throw new ProcessException(Error.NDR_0436);
            }
            if (!"unused".equals(redPacket.getSendStatus())) {
                logger.warn("用户ID：" + userId + "，项目ID：" + iPlan.getId()
                        + "redPacketId:" + redPacketId + "状态不是未使用" + redPacket.toString());
                String status = "被使用";
                if ("expired".equals(redPacket.getSendStatus())) {
                    status = "过期";
                    throw new ProcessException(Error.NDR_0438);
                }
                throw new ProcessException(Error.NDR_0437);
            }
            if (!StringUtils.equals(RedPacket.USAGEDETAIL_INVEST, redPacket.getUsageDetail())) {
                logger.warn("投资不可使用" + redPacketId);
                throw new ProcessException(Error.NDR_0439);
            }
            if (money < redPacket.getInvestMoney()) {
                logger.warn("投资金额小于限制金额;投资金额：" + money + ";限制金额：" + redPacket.getInvestMoney());
                throw new ProcessException(Error.NDR_0440);
            }
            if (iPlan.getRateType().equals(IPlan.RATE_TYPE_FIX) && redPacket.getInvestRate() > 0
                    && iPlan.getFixRate() != null && iPlan.getFixRate().doubleValue()> redPacket.getInvestRate()) {
                logger.warn("投资利率大于限制利率;投资利率：" + iPlan.getFixRate() + ";限制利率："
                        + redPacket.getInvestRate());
                throw new ProcessException(Error.NDR_0441);
            }
            if (redPacket.getRuleId() == 31) {
                logger.warn("userId:" + userId + ",红包ID：" + redPacket.getId());
                double newbieUsable = investService.getNewbieUsable(userId,iPlan.getIplanType());
                if (newbieUsable <= 0) {
                    logger.warn("老用户不能使用此类红包券userId:" + userId + "red" + redPacket.toString());
                    throw new ProcessException(Error.NDR_0442);
                }
            }
            /*if ("rateByDay".equals(redPacket.getType())) {
                int rateDay = redPacket.getRateDay();
                int periods = iPlan.getExitLockDays();
                if (rateDay > periods) {
                    logger.warn("userId:" + userId + "使用按天加息红包券，红包加息时间：" + rateDay + "大于项目锁定时间：" + periods);
                    throw new ProcessException(Error.NDR_0443);
                }
            }*/
        } else {
            logger.warn("用户：" + userId + "使用的红包：" + redPacketId + "不存在");
            throw new ProcessException(Error.NDR_0432);
        }
    }

    //购买债权,发送红包奖励
    public void createCreditPacketInvest(Credit credit) {
        //查询出交易记录
        SubjectTransLog subjectTransLog = subjectTransLogDao.findById(credit.getSourceChannelId());
        Integer money = credit.getInitPrincipal();
        Integer investAllowanceInterest = 0;

        InvestRedpacket ir = new InvestRedpacket();
        ir.setRepackedOrder("CREDIT"
                + subjectTransLog.getExtSn());
        int size = redPacketDetailDao.findCountByRepackedOrderOnly(ir.getRepackedOrder());
        if (size > 0) {
            return;
        }
        double d = 0;
        // 大于0表示该笔投资使用加息券
        if (subjectTransLog.getRedPacketId() != null &&
                subjectTransLog.getRedPacketId() > 0) {
            RedPacket packet = redPacketDetailDao.findRedPacketById(subjectTransLog.getRedPacketId());
            if (packet != null && !packet.getType().equals(RedPacket.TYPE_DEDUCT)) {
                d = getCreditRedpacketMoney(packet, credit.getTargetId(), money / 100.0);
            }
            logger.info("使用红包卷的债权购买交易：{},奖励金额：{}", subjectTransLog.getId(), d);
        }
        // 如果补息或者奖励金额大于0,则 创建补息和奖励记录
        if (d > 0) {
            ir.setId(IdUtil.randomUUID());
            ir.setInvestId(subjectTransLog.getId().toString());
            ir.setLoanId(subjectTransLog.getSubjectId().toString());
            ir.setRewardMoney(d);
            ir.setRepackedOrder("CREDIT"
                    + subjectTransLog.getExtSn());
            ir.setType(InvestRedpacket.RED_PACKET_TYPE_SUBJECT);
            ir.setSendRedpacketStatus(0);
            ir.setSendAllowanceStatus(0);
            ir.setRepackedId(subjectTransLog.getRedPacketId());
            ir.setCreateTime(new Date());
            ir.setUserId(subjectTransLog.getUserId());
            investRedPacketDao.insert(ir);
        }
    }

    /**
     * 债转获取红包卷加息金额
     * @param redPacket
     * @param id
     * @param money
     * @return
     */
    public double getCreditRedpacketMoney(RedPacket redPacket,Integer id, double money){
        String type = redPacket.getType();
        String status = redPacket.getSendStatus();
        CreditOpening creditOpening = creditOpeningDao.findById(id);
        Credit credit = creditDao.findById(creditOpening.getCreditId());

        double rate = redPacket.getRate();
        double d = 0.0;
        if (23 == redPacket.getRuleId()) {// 如果红包规则是23则金额最高计算上限是5万
            money = money > 50000 ? 50000 : money;
        }
        String repayType=subjectService.getSubjectRepayType(creditOpening.getSubjectId());
        //期数
        int term = credit != null ? credit.getResidualTerm() : 0;
        //天数
        String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
        String endDate = creditOpening.getEndTime().substring(0, 8);
        int period = (int) DateUtil.betweenDays(currentDate, endDate);
        if (type.equals(RedPacket.TYPE_RATE)) {
            if(period<30){
                d= InterestUtil.getInterestByPeriodDay(money,rate,period);
            }
            else{
                d= InterestUtil.getInterestByPeriodMoth(money,rate,term,repayType);
            }
        } else if (type.equals(RedPacket.TYPE_RATE_BY_DAY)) {
            //按天加息券，如果天数小于期限数，按加息劵天数加息
            int rateDay = period <= redPacket.getRateDay() ? period : redPacket.getRateDay();
            d = InterestUtil.getInterestByPeriodDay(money, rate, rateDay);
        } else if (RedPacket.TYPE_MONEY.equals(type)) {
            // 固定金额奖励的情况下
            d = redPacket.getMoney();
        }
        return d;
    }

    public BigDecimal getRealRate(BigDecimal rate, LocalDateTime openTime) {
        logger.info("原项目利率：[{}],开始时间：[{}]", rate, openTime);
        //如果项目发布时间在上线时间之前，返回原利率
        if (openTime == null || openTime.isBefore(CHANGE_RATE_TIME)) {
            return rate;
        }
        Config config = configDao.getConfigById(Config.PACKET_INVEST_RATE);
        if (config != null) {
            String value = config.getValue();
            if (StringUtils.isBlank(value)) {
                return rate;
            }
            BigDecimal valueB = null;
            try {
                valueB = new BigDecimal(value);
            } catch (Exception e) {
                logger.warn("补息利率解析失败,value=[{}]", value, e);
                return rate;
            }
            //如果value在[0,rate)区间，返回value值
            if (valueB.compareTo(BigDecimal.ZERO) >= 0 && valueB.compareTo(rate) < 0) {
                return valueB;
            }
        }
        return rate;
    }
}
