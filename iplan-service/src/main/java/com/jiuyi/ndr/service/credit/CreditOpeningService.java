package com.jiuyi.ndr.service.credit;

import com.github.pagehelper.PageHelper;
import com.jiuyi.ndr.constant.CreditConstant;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditCondition;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import com.jiuyi.ndr.domain.subject.*;
import com.jiuyi.ndr.domain.transferstation.LoanIntermediaries;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.dto.credit.CreditOpeningDetailDto;
import com.jiuyi.ndr.dto.credit.CreditOpeningDtoPc;
import com.jiuyi.ndr.dto.credit.CreditTransferRecord;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditCancelConfirmDto;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditTransferSuccessDto;
import com.jiuyi.ndr.dto.credit.mobile.CreditOpeningDtoNew;
import com.jiuyi.ndr.dto.subject.SubjectRepayScheduleDto;
import com.jiuyi.ndr.dto.subject.SubjectTransLogDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.subject.*;
import com.jiuyi.ndr.service.user.ActivityMarkConfigureService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.DealUtil;
import com.jiuyi.ndr.util.PageUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lixiaolei on 2017/6/14.
 */
@Service
public class CreditOpeningService {

    private final static Logger logger = LoggerFactory.getLogger(CreditOpeningService.class);

    @Autowired
    private CreditOpeningDao creditOpeningDao;
    @Autowired
    private CreditService creditService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectTransferParamService subjectTransferParamService;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private CreditOpeningService creditOpeningService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private UserService userService;
    @Autowired
    private SubjectTransLogService subjectTransLogService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private ActivityMarkConfigureService activityMarkConfigureService;
    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;

    private DecimalFormat df = new DecimalFormat("######0.00");
    private DecimalFormat df2 = new DecimalFormat("######0");
    private static DecimalFormat df3 = new DecimalFormat("0.####");
    private DecimalFormat df4 = new DecimalFormat("######0.##");
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public CreditOpening getById(Integer id){
        if (id == null){
            throw  new IllegalArgumentException("id can not be null");
        }
        return creditOpeningDao.findByIdForUpdate(id);
    }


    public List<CreditOpening> getByCreditIds(Set<Integer> creditIds) {
        if (creditIds.isEmpty()) {
            throw new IllegalArgumentException("credit ids is can not null or empty when find creditOpening by creditIds");
        }
        return this.getByCreditIdsAndStatuses(creditIds, new HashSet<>(Arrays.asList(CreditOpening.STATUS_OPENING, CreditOpening.STATUS_FINISH)));
    }

    public List<CreditOpening> getByCreditIdsAndStatuses(Set<Integer> creditIds, Set<Integer> statuses) {
        if (creditIds.isEmpty()) {
            throw new IllegalArgumentException("credit ids is can not null or empty when find creditOpening by creditIds and statuses");
        }
        if (statuses.isEmpty()) {
            throw new IllegalArgumentException("credit ids is can not null or empty when find creditOpening by creditIds and statuses");
        }
        return creditOpeningDao.findByCreditIdsAndStatuses(creditIds, statuses);
    }

    public CreditOpening update(CreditOpening creditOpening) {
        if (creditOpening == null || creditOpening.getId() == null) {
            throw new IllegalArgumentException("creditOpening or creditOpening id is can not null when update creditOpening");
        }
        creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
        creditOpeningDao.update(creditOpening);
        return creditOpening;
    }
    @ProductSlave
    public List<CreditOpening> getUserTransferringCreditsInSomeIPlanByPageHelper(String userId, Integer iPlanId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return this.getUserTransferringCreditsInSomeIPlan(userId, iPlanId);
    }

    private List<CreditOpening> getUserTransferringCreditsInSomeIPlan(String userId, Integer iPlanId) {
        Set<Integer> creditIds = creditService.getUserHoldingCreditsInSomeIPlan(userId, iPlanId).stream().map(Credit :: getId).collect(Collectors.toSet());
        return this.getByCreditIds(creditIds);
    }

    public List<CreditOpening> findBySubjectId(String subjectId){
        return creditOpeningDao.findBySubjectId(subjectId);
    }

    @ProductSlave
    public List<CreditOpeningDtoNew> getAllCreditOpening(String type,String rate,String discount,String term,List<String> rates,List<String> discounts,List<String> terms, Integer pageNo, Integer pageSize) {
        StringBuilder sql = new StringBuilder();
        if(StringUtils.isNotBlank(rate) && !"0".equals(rate)){
            switch (rate){
                case "1": sql.append(" AND (ns.invest_rate + IFNULL(ns.bonus_rate,0)) <= " + Double.parseDouble(rates.get(0))/100.0); break;
                case "2": sql.append(" AND (ns.invest_rate + IFNULL(ns.bonus_rate,0)) > " + Double.parseDouble(rates.get(0))/100.0 +"  AND (ns.invest_rate + IFNULL(ns.bonus_rate,0)) <= " + Double.parseDouble(rates.get(1))/100.0); break;
                case "3": sql.append(" AND (ns.invest_rate + IFNULL(ns.bonus_rate,0)) > " + Double.parseDouble(rates.get(1))/100.0); break;
            }
        }
        if(StringUtils.isNotBlank(discount) && !"0".equals(discount)){
            switch (discount){
                case "1": sql.append(" AND nco.transfer_discount = " + Double.parseDouble(discounts.get(0))/100.0); break;
                case "2": sql.append(" AND nco.transfer_discount > " + Double.parseDouble(discounts.get(1))/100.0 +"  AND nco.transfer_discount > " + Double.parseDouble(discounts.get(0))/100.0); break;
                case "3": sql.append(" AND nco.transfer_discount <= " + Double.parseDouble(discounts.get(1))/100.0); break;
            }
        }
        if(StringUtils.isNotBlank(term) && !"0".equals(term)){
            switch (term){
                case "1": sql.append(" AND (ns.term - ns.current_term + 1) >= " + Double.parseDouble(terms.get(0))/100.0 + " AND (ns.term - ns.current_term + 1) <= " + Double.parseDouble(terms.get(1))/100.0); break;
                case "2": sql.append(" AND (ns.term - ns.current_term + 1) >= " + Double.parseDouble(terms.get(2))/100.0 + " AND (ns.term - ns.current_term + 1) <= " + Double.parseDouble(terms.get(3))/100.0); break;
                case "3": sql.append(" AND (ns.term - ns.current_term + 1) >= " + Double.parseDouble(terms.get(4))/100.0); break;
            }
        }
        PageHelper.startPage(pageNo, pageSize);
        return creditOpeningDao.findCreditOpeningAllSql(type,sql.append(" ").toString());
    }

    //查询债权列表
    public List<CreditOpeningDtoPc> getAllCreditOpeningPc(String type, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return creditOpeningDao.findCreditOpeningPcAllSql(type);
    }
    public List<CreditOpeningDtoPc> getAllCreditOpeningSort(CreditCondition creditCondition, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return creditOpeningDao.findCreditOpeningSortSql(creditCondition);
    }

    //年化利率
    public BigDecimal calcTotalRate(String subjectId){
        Subject subject = subjectService.getBySubjectId(subjectId);

        //原标的利率
        BigDecimal totalRate = BigDecimal.ZERO;
        if (subject.getBonusRate() != null){
            totalRate = subject.getInvestRate().add(subject.getBonusRate());
        }else{
            totalRate = subject.getInvestRate();
        }
        return totalRate;
    }

    //新年化利率
    public Double calcNewRate(String subjectId,CreditOpening creditOpening){
        BigDecimal totalRate = this.calcTotalRate(subjectId);
        Double newRate = totalRate.divide(creditOpening.getTransferDiscount(), 4, RoundingMode.HALF_UP).doubleValue();
        return newRate;
    }



    /*public List<CreditOpening> getByCreditId(Integer creditId) {
        if (creditId == null){
            throw new IllegalArgumentException("id can not be null");
        }
        return  creditOpeningDao.findByCreditId(creditId);
    }*/
    @ProductSlave
    public List<CreditOpening> getByUserIdAndStatusAndOpenChannel(String userId,Integer openChannel) {
        if (StringUtils.isBlank(userId)){
            throw new IllegalArgumentException("userId can not be null");
        }
        if (openChannel == null){
            throw new IllegalArgumentException("sourceChannel can not be null");
        }
        return  creditOpeningDao.findByUserIdAndStatusAndOpenChannel(userId,openChannel);
    }

    public List<CreditOpening> getBySubjectId(String subjectId){
        return creditOpeningDao.findBySubjectId(subjectId);
    }

    public CreditOpening getBySourceChannelId(Integer id,Integer sourceChannel) {
        if (id == null){
            throw new IllegalArgumentException("id can not be null");
        }
        return creditOpeningDao.findBySourceChannelIdNew(id,sourceChannel);
    }

    public List<CreditOpening> sortByCondition(List<CreditOpening> creditOpenings, Integer target, Integer pageNo, Integer pageSize) {
        if(target == null){
            throw new IllegalArgumentException("targetCredit can not be null");
        }
        List<CreditOpening> lists = new ArrayList<>();
        for (CreditOpening creditOpening : creditOpenings) {
            Credit credit = creditService.getById(creditOpening.getCreditId());
            if (credit.getTarget().equals(target)){
                lists.add(creditOpening);
            }
        }
        List newList = new PageUtil().ListSplit(lists, pageNo, pageSize);
        return newList;
    }

    public List<CreditOpening> sortByConditionNoPage(List<CreditOpening> creditOpenings, Integer target) {
        if(target == null){
            throw new IllegalArgumentException("targetCredit can not be null");
        }
        List<CreditOpening> lists = new ArrayList<>();
        for (CreditOpening creditOpening : creditOpenings) {
            Credit credit = creditService.getById(creditOpening.getCreditId());
            if (credit.getTarget().equals(target)){
                lists.add(creditOpening);
            }
        }
        return lists;
    }

    public CreditOpening getBySourceChannelIdAndOpenChannel(Integer sourceChannelId, Integer openChannel) {
        if(sourceChannelId == null){
            throw new IllegalArgumentException("sourceChannelId can not be null");
        }
        if(openChannel == null){
            throw new IllegalArgumentException("openChannel can not be null");
        }
        return creditOpeningDao.findBySourceChannelIdAndSourceChannel(sourceChannelId,openChannel);
    }

    //债权取消
    public AppCreditCancelConfirmDto creditTransferConfirm(Map<String,String> map){
        Integer creditOpeningId = 0;
        String userId = null;
        if (map.containsKey("id") && StringUtils.isNotBlank(map.get("id"))){
            creditOpeningId = Integer.valueOf(map.get("id"));
        }
        if (map.containsKey("userId") && StringUtils.isNotBlank(map.get("userId"))){
            userId = map.get("userId");
        }
        logger.info("开始调用债权取消确认接口->输入参数:开放中债权ID={}",
                creditOpeningId);

        CreditOpening creditOpening = creditOpeningService.getById(creditOpeningId);
        if (creditOpening == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        if(creditOpening.getAvailablePrincipal() == 0){
            throw new ProcessException(Error.NDR_0707);
        }
        AppCreditCancelConfirmDto appCreditCancelConfirmDto = new AppCreditCancelConfirmDto();

        appCreditCancelConfirmDto.setId(creditOpening.getId());
        Subject subject = subjectService.getBySubjectId(creditOpening.getSubjectId());

        //转出金额
        appCreditCancelConfirmDto.setTransAmt(creditOpening.getTransferPrincipal() / 100.0);
        appCreditCancelConfirmDto.setTransAmtStr(df4.format(appCreditCancelConfirmDto.getTransAmt()));

        //已成交金额
        appCreditCancelConfirmDto.setFinishAmt((creditOpening.getTransferPrincipal() - creditOpening.getAvailablePrincipal()) / 100.0);
        appCreditCancelConfirmDto.setFinishAmtStr(df4.format(appCreditCancelConfirmDto.getFinishAmt()));

        //撤销金额
        appCreditCancelConfirmDto.setCancelAmt(creditOpening.getAvailablePrincipal() / 100.0);
        appCreditCancelConfirmDto.setCancelAmtStr(df4.format(appCreditCancelConfirmDto.getCancelAmt()));


        //散标交易配置信息
        SubjectTransferParam subjectTransferParam = subjectTransferParamService.getByTransferParamCode(subject.getTransferParamCode());

        Credit credit = creditService.getById(creditOpening.getCreditId());
        SubjectAccount subjectAccount = subjectAccountService.findAccountById(credit.getSourceAccountId());

        //转让服务费
        Double feeRate = subjectAccountService.calcTransFeeNew(subjectAccount.getTransLogId(), subject, subjectTransferParam);
        appCreditCancelConfirmDto.setFee((appCreditCancelConfirmDto.getFinishAmt()) * feeRate / 100.0);
        appCreditCancelConfirmDto.setFeeStr(df4.format(appCreditCancelConfirmDto.getFee()));

        //扣除红包奖励
        Double redFee = subjectAccountService.calcRedFee(subjectAccount, credit);
        appCreditCancelConfirmDto.setRedFee((appCreditCancelConfirmDto.getFinishAmt())  * redFee);
        appCreditCancelConfirmDto.setRedFeeStr(df4.format(appCreditCancelConfirmDto.getRedFee() ));

        //溢价手续费
        Double overFee = 0.0;
        Double transferDiscount = creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue();
        if(transferDiscount > 100){
            overFee = (appCreditCancelConfirmDto.getFinishAmt()) * (transferDiscount - 100) / 100.0 * 0.2;
        }
        appCreditCancelConfirmDto.setOverFee(overFee);
        appCreditCancelConfirmDto.setOverFeeStr(df4.format(overFee));

        return appCreditCancelConfirmDto;
    }

    //债权撤消实际调用接口
    public AppCreditTransferSuccessDto creditTransferCancelFinsh(Map<String,String> map){
        Integer id = 0;
        String userId = null;
        Integer flag = 0;
        if (map.containsKey("id") && StringUtils.isNotBlank(map.get("id"))){
            id = Integer.valueOf(map.get("id"));
        }
        if (map.containsKey("flag") && StringUtils.isNotBlank(map.get("flag"))){
            flag = Integer.valueOf(map.get("flag"));
        }
        if (map.containsKey("userId") && StringUtils.isNotBlank(map.get("userId"))){
            userId = map.get("userId");
        }
        logger.info("开始调用债权取消确认接口->输入参数:开放中债权ID={}",
                id);
        if(flag == 0){
            CreditOpening creditOpening = creditOpeningService.getById(id);
            if (creditOpening == null) {
                throw new ProcessException(Error.NDR_0202);
            }
            if(!creditOpening.getTransferorId().equals(userId)){
                throw new ProcessException(Error.NDR_0202);
            }
            subjectAccountService.cancelCreditTransferNew(id);
        }else{
            IPlanTransLog iPlanTransLog = iPlanTransLogDao.findByIdAndStatus(id);
            if (iPlanTransLog == null) {//查询不到转让交易记录,不能撤消
                throw new ProcessException(Error.NDR_0706.getCode(), Error.NDR_0706.getMessage());
            }
            if(!iPlanTransLog.getUserId().equals(userId)){
                throw new ProcessException(Error.NDR_0202);
            }
            iPlanAccountService.cancelCreditTransferNew(id);
        }
        return new AppCreditTransferSuccessDto("申请撤销成功!","您可以再次发起债权转让");
    }

    public List<CreditOpeningDtoPc> getList(CreditCondition creditCondition,Integer pageNo, Integer pageSize){
        List<CreditOpeningDtoPc> allCreditOpeningDto = this.getAllCreditOpeningSort(creditCondition, pageNo, pageSize);
        if (allCreditOpeningDto == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        for (CreditOpeningDtoPc creditOpeningDtoPc : allCreditOpeningDto) {
            CreditOpening creditOpening = creditOpeningDao.findById(creditOpeningDtoPc.getId());
            Subject subject = subjectService.findSubjectBySubjectId(creditOpening.getSubjectId());
            creditOpeningDtoPc.setCreditValue(creditOpeningDtoPc.getAvailablePrincipal() / 100.0 + subjectRepayScheduleService.calcTotalInterest(creditOpening,subject));
            if("MCEI".equals(creditOpeningDtoPc.getRepayType())){
                creditOpeningDtoPc.setRepayType("等额本息");
            }else if("IFPA".equals(creditOpeningDtoPc.getRepayType())){
                creditOpeningDtoPc.setRepayType("按月付息到期还本");
            }else if("OTRP".equals(creditOpeningDtoPc.getRepayType())){
                creditOpeningDtoPc.setRepayType("一次性到期还本付息");
            }
            creditOpeningDtoPc.setTotalRateStr(df4.format(creditOpeningDtoPc.getTotalRate()*100)+"%");
            if(creditOpeningDtoPc.getBonusRate() != null && creditOpeningDtoPc.getBonusRate() != 0.0){
                creditOpeningDtoPc.setTotalRateStr(df4.format(creditOpeningDtoPc.getInvestRate() * 100)+"%"+"+"+df4.format((creditOpeningDtoPc.getBonusRate()) * 100)+"%");
                ActivityMarkConfigure amc = activityMarkConfigureService.findById(subject.getActivityId());
                if(amc.getIncreaseTerm()!=null){
                    creditOpeningDtoPc.setTotalRateStr(df4.format(creditOpeningDtoPc.getInvestRate() * 100)+"%");
                }
            }else{
                creditOpeningDtoPc.setTotalRateStr(df4.format(creditOpeningDtoPc.getInvestRate() * 100)+"%");
            }
            creditOpeningDtoPc.setEndTime(DateUtil.parseDate(creditOpeningDtoPc.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());
            creditOpeningDtoPc.setPayMoney((creditOpeningDtoPc.getAvailablePrincipal() / 100.0)*(creditOpeningDtoPc.getTransferDiscount()));
            SubjectRepaySchedule schedule = subjectRepayScheduleService.getCurrentRepaySchedule(subject.getSubjectId());
            String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
            String endDate = schedule.getDueDate();
            long days = DateUtil.betweenDays(currentDate, endDate);
            creditOpeningDtoPc.setResidualDay((int) days);
            creditOpeningDtoPc.setResidualTerm(creditOpeningDtoPc.getTerm() - creditOpeningDtoPc.getCurrentTerm() + 1);
            creditOpeningDtoPc.setAvailablePrincipal(creditOpeningDtoPc.getAvailablePrincipal()/100.0);
            creditOpeningDtoPc.setTransferPrincipal(creditOpeningDtoPc.getTransferPrincipal()/100.0);
        }
        return allCreditOpeningDto;
    }

    //pc 项目详情页
    public CreditOpeningDetailDto getCreditOpeningDetail(Integer id,String userId){
        CreditOpening creditOpening = creditOpeningService.getById(id);
        if (creditOpening == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        Subject subject = subjectService.getBySubjectId(creditOpening.getSubjectId());
        Credit credit = creditService.getById(creditOpening.getCreditId());
        List<RedPacket> redPacketList = redPacketService.getUsablePacketCreditAll(userId, subject, "pc","credit");
        List<CreditOpeningDetailDto.RedPacketApp> redPacketAppList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(redPacketList)) {
            for (RedPacket redPacket : redPacketList) {
                CreditOpeningDetailDto.RedPacketApp redPacketApp = new CreditOpeningDetailDto.RedPacketApp();
                redPacketApp.setId(redPacket.getId());
                redPacketApp.setAmt(redPacket.getMoney());
                redPacketApp.setAmt2(df.format(redPacket.getMoney()));
                redPacketApp.setRate(redPacket.getRate());
                redPacketApp.setRate2(df.format(redPacket.getRate() * 100) + "%");
                redPacketApp.setRateDay(redPacket.getRateDay());
                redPacketApp.setName(redPacket.getName());
                redPacketApp.setDeadLine(redPacket.getDeadLine().substring(0,10));
                redPacketApp.setIntroduction(redPacketService.getRedPacketInvestMoneyStr(redPacket));
                redPacketApp.setType(redPacket.getType());
                redPacketApp.setInvestMoney(redPacket.getInvestMoney());
                redPacketApp.setUseStatus(redPacket.getUseStatus());

                redPacketAppList.add(redPacketApp);
            }
        }
        CreditOpeningDetailDto creditOpeningDetailDto = new CreditOpeningDetailDto();
        creditOpeningDetailDto.setRedPacketAppList(redPacketAppList);
        creditOpeningDetailDto.setId(id);
        creditOpeningDetailDto.setSubjectId(subject.getSubjectId());
        creditOpeningDetailDto.setName(subject.getName());
        //折让率
        BigDecimal transferDiscount = creditOpening.getTransferDiscount();

        //原标的利率
        BigDecimal totalRate = BigDecimal.ZERO;
        if (subject.getBonusRate() != null){
            totalRate = subject.getInvestRate().add(subject.getBonusRate());
        }else{
            totalRate = subject.getInvestRate();
        }
        Double expectRate = calcNewRate(creditOpening.getSubjectId(), creditOpening);
        creditOpeningDetailDto.setExpectRate(totalRate.doubleValue());
        if(subject.getBonusRate() != null && subject.getBonusRate().doubleValue()!=0.0){
            creditOpeningDetailDto.setExpectRateStr(df4.format(subject.getInvestRate().doubleValue() * 100)+"+"+df4.format((subject.getBonusRate().doubleValue()) * 100)+"%");
            ActivityMarkConfigure amc = activityMarkConfigureService.findById(subject.getActivityId());
            if(amc.getIncreaseTerm()!=null){
                creditOpeningDetailDto.setExpectRateStr(df4.format(subject.getInvestRate().doubleValue() * 100)+"%");
            }
        }else{
            creditOpeningDetailDto.setExpectRateStr(df4.format(subject.getInvestRate().doubleValue() * 100)+"%");
        }
        creditOpeningDetailDto.setRate(subject.getRate().doubleValue());
        //剩余时间
        String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
        String endDate = credit.getEndTime().substring(0, 8);
        long days = DateUtil.betweenDays(currentDate, endDate);
        creditOpeningDetailDto.setResidualDay((int) days);
        creditOpeningDetailDto.setPrincipalStr(df4.format(creditOpening.getTransferPrincipal()/100.0));
        creditOpeningDetailDto.setAvailablePrincipal(creditOpening.getAvailablePrincipal()/100.0);

        //预期收益
        Double expectInterest = subjectService.getInterestByRepayType(creditOpening.getAvailablePrincipal(),subject.getInvestRate(),subject.getRate(),credit.getResidualTerm(),subject.getPeriod(),subject.getRepayType());
        Double expectBonusInterest = subjectService.getInterestByRepayType(creditOpening.getAvailablePrincipal(),subject.getBonusRate(),subject.getRate(),credit.getResidualTerm(),subject.getPeriod(),subject.getRepayType());
        creditOpeningDetailDto.setExpectProfit(expectInterest + expectBonusInterest);
        creditOpeningDetailDto.setExpectProfitStr(df4.format(creditOpeningDetailDto.getExpectProfit()));

        //原债权价值
        creditOpeningDetailDto.setOldValue(subjectRepayScheduleService.calcTotalInterest(creditOpening,subject)+(creditOpening.getAvailablePrincipal()/100.0));
        creditOpeningDetailDto.setOldValueStr(df4.format(creditOpeningDetailDto.getOldValue())+"");

        //承接价格
        Double buyPrice = (creditOpening.getAvailablePrincipal()/100.0) * (transferDiscount.doubleValue());
        creditOpeningDetailDto.setBuyPrice(buyPrice);
        creditOpeningDetailDto.setBuyPriceStr(df4.format(buyPrice)+"");

        //折让比例
        creditOpeningDetailDto.setDiscount(transferDiscount.doubleValue());
        creditOpeningDetailDto.setDiscountStr(df4.format(creditOpeningDetailDto.getDiscount() * 100)+"折");

        //原年利化率
        creditOpeningDetailDto.setOldRate(totalRate.doubleValue());
        creditOpeningDetailDto.setOldRateStr(df4.format(totalRate.doubleValue() * 100) + "%");

        //差异利率
        creditOpeningDetailDto.setDifferRate(expectRate - totalRate.doubleValue());
        if (expectRate - totalRate.doubleValue() > 0){
            creditOpeningDetailDto.setDifferRateStr("+"+df4.format(creditOpeningDetailDto.getDifferRate() * 100) + "%");
        }else{
            creditOpeningDetailDto.setDifferRateStr("-"+df4.format((totalRate.doubleValue() - expectRate) * 100) + "%");
        }
        //回款方式
        creditOpeningDetailDto.setRepayType(subjectService.getRepayType(creditOpening.getSubjectId()));

        //起投金额
        SubjectTransferParam transferParam = subjectTransferParamService.getByTransferParamCode(subject.getTransferParamCode());
        creditOpeningDetailDto.setInvestMoney(transferParam.getPurchasingPriceMin()/100.0);
        creditOpeningDetailDto.setInvestMoneyStr(df4.format(transferParam.getPurchasingPriceMin()/100.0));

        //项目结束时间
        creditOpeningDetailDto.setEndTime(DateUtil.parseDate(credit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());

        //下次回款时间
        SubjectRepaySchedule currentRepaySchedule = subjectRepayScheduleService.getCurrentRepaySchedule(subject.getSubjectId());
        creditOpeningDetailDto.setNextRepayTime(DateUtil.parseDate(currentRepaySchedule.getDueDate(), DateUtil.DATE_TIME_FORMATTER_8).toString());

        //购买记录
        creditOpeningDetailDto.setTransLogId(credit.getSourceChannelId());

        //债权信息
        creditOpeningDetailDto.setCreditId(creditOpening.getCreditId());
        //剩余期限
        creditOpeningDetailDto.setResidualTerm(credit.getResidualTerm());

        List<SubjectRepayScheduleDto> subjectRepayScheduleDtos = new ArrayList<>();
        //未还计划
        List<SubjectRepaySchedule> schedules = subjectRepayScheduleService.getSubjectRepayScheduleBySubjectIdNotRepay(subject.getSubjectId());
        Integer principal = creditOpening.getTransferPrincipal();
        if(schedules != null && schedules.size()>0){
            Integer totalPrincipal=0;
            for (SubjectRepaySchedule schedule : schedules) {
                totalPrincipal+=schedule.getDuePrincipal();
            }
            for (SubjectRepaySchedule schedule : schedules) {
                SubjectRepayScheduleDto subjectRepayScheduleDto = new SubjectRepayScheduleDto();
                SubjectRepayDetail subjectRepayDetail = subjectRepayScheduleService.repayDetailCredtiBySchedule(schedule, credit, principal, totalPrincipal);
                BeanUtils.copyProperties(schedule, subjectRepayScheduleDto);
                subjectRepayScheduleDto.setDueDate(DateUtil.parseDate(subjectRepayScheduleDto.getDueDate(), DateUtil.DATE_TIME_FORMATTER_8).toString());
                subjectRepayScheduleDto.setDuePrincipal(subjectRepayDetail.getPrincipal());
                subjectRepayScheduleDto.setDueInterest(subjectRepayDetail.getInterest() + subjectRepayDetail.getBonusInterest());
                subjectRepayScheduleDtos.add(subjectRepayScheduleDto);
                principal -=subjectRepayDetail.getPrincipal();
                totalPrincipal -= schedule.getDuePrincipal();
            }
        }
        creditOpeningDetailDto.setSubjectRepayScheduleDtos(subjectRepayScheduleDtos);

        //购买记录
        List<SubjectTransLogDto> subjectTransLogDtos = new ArrayList<>();
        List<SubjectTransLog> subjectTransLogs = subjectTransLogService.getAllSubjectTransLogByTargetId(creditOpening.getId());

        Integer i = 1;
        if (subjectTransLogs != null){
            for (SubjectTransLog subjectTransLog : subjectTransLogs) {
                SubjectTransLogDto subjectTransLogDto = new SubjectTransLogDto();
                subjectTransLogDto.setId(i);
                i++;
                subjectTransLogDto.setDevice(subjectTransLog.getTransDevice());
                subjectTransLogDto.setTime(subjectTransLog.getCreateTime());
                User user = userService.getUserById(subjectTransLog.getUserId());
                subjectTransLogDto.setName(user.getRealname().substring(0,1)+"**");
                subjectTransLogDto.setAmount(subjectTransLog.getTransAmt() / 100.0);
                subjectTransLogDtos.add(subjectTransLogDto);
            }
        }
        //查询车贷借款信息
        if(StringUtils.isNotBlank(subject.getContractNo())){
            List<LoanIntermediaries> loanIntermediaries = subjectService.getVehicleLoanInformation(subject.getContractNo());
            String  guaranteeType ="";
            if(!loanIntermediaries.isEmpty()){
                guaranteeType =loanIntermediaries.get(0).getGuaranteeType();
            }
            if(StringUtils.isNotBlank(guaranteeType) && "A".equals(guaranteeType)){
                creditOpeningDetailDto.setGuaranteeType("质押");
            }else{
                creditOpeningDetailDto.setGuaranteeType("抵押");
            }
        }
        creditOpeningDetailDto.setSubjectTransLogDtos(subjectTransLogDtos);
        creditOpeningDetailDto.setInstructions(GlobalConfig.CREDIT_INSTRUCTIONS);
        creditOpeningDetailDto.setTerm(subject.getTerm());
        return creditOpeningDetailDto;
    }

    //pc 购买页
    public List<SubjectTransLogDto> getSubjectTransLogDtos(Integer id){
        List<SubjectTransLogDto> subjectTransLogDtos = new ArrayList<>();
        List<SubjectTransLog> subjectTransLogs = subjectTransLogService.getAllSubjectTransLogByTargetId(id);
        if (subjectTransLogs == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        Integer i = 1;
        if (subjectTransLogs != null){
            for (SubjectTransLog subjectTransLog : subjectTransLogs) {
                SubjectTransLogDto subjectTransLogDto = new SubjectTransLogDto();
                subjectTransLogDto.setId(i);
                i++;
                subjectTransLogDto.setDevice(subjectTransLog.getTransDevice());
                subjectTransLogDto.setTime(subjectTransLog.getCreateTime());
                User user = userService.getUserById(subjectTransLog.getUserId());
                subjectTransLogDto.setName(user.getRealname().substring(0,1)+"**");
                subjectTransLogDto.setAmount(subjectTransLog.getTransAmt() / 100.0);
                subjectTransLogDtos.add(subjectTransLogDto);
            }
        }
        return subjectTransLogDtos;
    }

    public List<Map<String, Object>> getCreditTransferRecord(int creditOpeningId) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<CreditTransferRecord> creditTransferRecords = new ArrayList();
        CreditOpening creditOpening = creditOpeningService.getById(creditOpeningId);
        CreditTransferRecord creditTransferRecord = new CreditTransferRecord();
        User user = userService.getUserById(creditOpening.getTransferorId());
        creditTransferRecord.setTransferUserName(user != null ? DealUtil.dealRealname(user.getRealname()) : "");
        creditTransferRecord.setTransferPrincipal(df4.format(creditOpening.getTransferPrincipal()/100.0));
        creditTransferRecord.setTransferUserId(DealUtil.dealUserId(creditOpening.getTransferorId()));
        creditTransferRecords.add(creditTransferRecord);
        Map<String, Object> map = new HashMap<>();
        map.put("date", creditOpening.getCreateTime());
        map.put("transferList", creditTransferRecords);
        result.add(map);
        return result;
    }

    //债权自动撤消

}
