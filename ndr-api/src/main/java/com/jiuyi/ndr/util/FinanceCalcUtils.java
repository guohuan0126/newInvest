package com.jiuyi.ndr.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

/**
 * 各种还款方式的还款金额的计算工具
 * Created by zhangyibo on 2016/11/7.
 */
public class FinanceCalcUtils {


    private static final BigDecimal ONE = BigDecimal.ONE;

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private static final BigDecimal ONEYEAR_MONTHS = new BigDecimal(12);

    private static final BigDecimal ONEYEAR_DAYS = new BigDecimal(360);

    private static final BigDecimal ONEYEAR_DAYS_356 = new BigDecimal(365);

    public static final Integer ONEMONTH_DAYS = 30;

    //0 为按月计息
    public static final Integer INTEREST_ACCRUAL_TYPE_MONTH = 0;
    //1 为按天计息
    public static final Integer INTEREST_ACCRUAL_TYPE_DAY = 1;

    /**
     * 等额本息计算
     * @param principal
     * @param yearRate
     * @param terms
     * @return
     */
    public static CalcResult calcMCEI(Integer principal, BigDecimal yearRate, Integer terms){
        BigDecimal monthRate = yearRate.divide(ONEYEAR_MONTHS,8,BigDecimal.ROUND_HALF_UP);

        CalcResult calcResult = new CalcResult();
        //以元来进行计算
        BigDecimal principalBig = new BigDecimal(principal);
        //等额本息=借款本金*月利率*(1+月利率)^还款月数/(1+月利率)^还款月数-1
        BigDecimal top = principalBig.multiply(monthRate).multiply((monthRate.add(ONE)).pow(terms));//分子
        BigDecimal bottom = ((ONE.add(monthRate)).pow(terms)).subtract(ONE);
        BigDecimal monthRepay = top.divide(bottom,8,BigDecimal.ROUND_HALF_UP);

        calcResult.setTerms(terms);

        TreeMap<Integer,CalcResult.Detail> details = new TreeMap<>();

        //剩余未还本金
        BigDecimal principalRemain = principalBig;
        int totalRepayPrincipal = 0;
        for(int i=1;i<=terms;i++){
            CalcResult.Detail detail = new CalcResult.Detail();
            //月还利息
            BigDecimal monthRepayInterest = principalRemain.multiply(monthRate);

            detail.setTerm(i);
            if(i == terms){
                //最后一期月还本金=总借款本金-前面N期已还本金
                detail.setMonthRepayPrincipal(principal-totalRepayPrincipal);
            }else{
                //月还本金
                BigDecimal monthRepayPrincipal = monthRepay.subtract(monthRepayInterest);
                detail.setMonthRepayPrincipal(monthRepayPrincipal.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
            }
            totalRepayPrincipal+=detail.getMonthRepayPrincipal();
            principalRemain = principalRemain.subtract(BigDecimal.valueOf(detail.getMonthRepayPrincipal()));
            detail.setMonthRepayInterest(monthRepayInterest.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
            detail.setMonthRepay(detail.getMonthRepayPrincipal()+detail.getMonthRepayInterest());
            detail.setRemainPrincipal(principalRemain.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());

            details.put(i,detail);
        }
        calcResult.setDetails(details);

        int totalRepay = details.entrySet().stream().map(Map.Entry::getValue).map(CalcResult.Detail::getMonthRepay).reduce((a, b)->a+b).orElse(0);
        calcResult.setTotalRepay(totalRepay);
        calcResult.setTotalRepayPrincipal(totalRepayPrincipal);
        calcResult.setTotalRepayInterest(totalRepay-totalRepayPrincipal);

        return calcResult;
    }
    /**
     * 卡贷等额本息计算
     * @param principal 本金
     * @param yearRate 年化利率
     * @param terms 期数
     * @param loanDate 借款日
     * @param firstRepayDate 第一个账单日
     * @return
     */
    public static CalcResult calcMCEICreditCard(Integer principal, BigDecimal yearRate, Integer terms, LocalDate loanDate,LocalDate firstRepayDate){
        BigDecimal monthRate = yearRate.divide(ONEYEAR_MONTHS,6,BigDecimal.ROUND_HALF_UP);
        BigDecimal dayRate = yearRate.divide(ONEYEAR_DAYS,6,BigDecimal.ROUND_HALF_UP);
        CalcResult calcResult = new CalcResult();
        //以元来进行计算
        BigDecimal principalBig = new BigDecimal(principal);
        //等额本息=借款本金*月利率*(1+月利率)^还款月数/(1+月利率)^还款月数-1
        BigDecimal monthRepay = calcMonthRepay(principalBig,terms,dayRate,loanDate,firstRepayDate);
        calcResult.setTerms(terms);

        TreeMap<Integer,CalcResult.Detail> details = new TreeMap<>();

        //剩余未还本金
        BigDecimal principalRemain = principalBig;
        int totalRepayPrincipal = 0;
        for(int i=1;i<=terms;i++){

            CalcResult.Detail detail = new CalcResult.Detail();
            //当期还款日
            LocalDate currentDueDate = getCurrentDueDate(firstRepayDate, i - 1);
            detail.setDueDate(currentDueDate.format(DateUtil.DATE_TIME_FORMATTER_8));
            //当期天数
            Integer daysInTerm = getDaysInTerm(loanDate, i - 1, firstRepayDate);
            //月还利息
            BigDecimal monthRepayInterest = principalRemain.multiply(dayRate).multiply(new BigDecimal(daysInTerm)).setScale(0, RoundingMode.HALF_UP);;
            //月还本金
            BigDecimal monthRepayPrincipal = BigDecimal.ZERO;
            detail.setTerm(i);
            if(i == terms){
                //最后一期月还本金=总借款本金-前面N期已还本金
                detail.setMonthRepayPrincipal(principal-totalRepayPrincipal);
            }else{
                //月还本金
                monthRepayPrincipal = monthRepay.subtract(monthRepayInterest).setScale(0, RoundingMode.HALF_UP);
                detail.setMonthRepayPrincipal(monthRepayPrincipal.setScale(0,RoundingMode.HALF_UP).intValue());
            }
            totalRepayPrincipal+=detail.getMonthRepayPrincipal();
            principalRemain = principalRemain.subtract(monthRepayPrincipal);
            detail.setMonthRepayInterest(monthRepayInterest.setScale(0,RoundingMode.HALF_UP).intValue());
            detail.setMonthRepay(detail.getMonthRepayPrincipal()+detail.getMonthRepayInterest());
            detail.setRemainPrincipal(principalRemain.setScale(0,RoundingMode.HALF_UP).intValue());
            details.put(i,detail);
        }
        calcResult.setDetails(details);

        int totalRepay = details.entrySet().stream().map(Map.Entry::getValue).map(CalcResult.Detail::getMonthRepay).reduce((a, b)->a+b).orElse(0);
        calcResult.setTotalRepay(totalRepay);
        calcResult.setTotalRepayPrincipal(totalRepayPrincipal);
        calcResult.setTotalRepayInterest(totalRepay-totalRepayPrincipal);

        return calcResult;
    }

    /**
     *
     * @param principalBig 本金
     * @param terms 总期数
     * @param dayRate 日利率
     * @param loanDate  借款日
     * @param firstRepayDate 第一个账单日
     * @return
     */
    private static BigDecimal calcMonthRepay(BigDecimal principalBig, Integer terms, BigDecimal dayRate, LocalDate loanDate, LocalDate firstRepayDate) {
        BigDecimal[] monthRates = new BigDecimal[terms];
        BigDecimal[] y = new BigDecimal[terms];

        for (int i = 0; i < terms; i++) {
            monthRates[i] = dayRate.multiply(new BigDecimal(getDaysInTerm(loanDate, i,firstRepayDate))).setScale(6, RoundingMode.HALF_UP);
            if (i == 0) {
                y[i] = (BigDecimal.ONE.add(monthRates[i])).multiply(BigDecimal.ONE).setScale(6, RoundingMode.HALF_UP);
            } else {
                y[i] = (BigDecimal.ONE.add(monthRates[i])).multiply(y[i - 1]).setScale(6, RoundingMode.HALF_UP);
            }
        }
        for (int i = 0; i < terms; i++) {
            if (i == 0) {
                y[i] = (BigDecimal.ONE.add(monthRates[terms - 1 - i])).multiply(BigDecimal.ONE).setScale(6, RoundingMode.HALF_UP);
            } else {
                y[i] = (BigDecimal.ONE.add(monthRates[terms - 1 - i])).multiply(y[i - 1]).setScale(6, RoundingMode.HALF_UP);
            }
        }
        //分子
        BigDecimal top = principalBig.multiply((y[terms - 1]));
        //分母
        BigDecimal bottom = BigDecimal.ONE;
        for (int i = 0; i < terms - 1; i++) {
            bottom = bottom.add(y[i]);
        }
        return  top.divide(bottom,0,RoundingMode.HALF_UP);
    }

    /**
     *
     * @param loanDate 借款日
     * @param currentTerm  当前期数-1
     * @param firstRepayDate 第一个账单日
     * @return 当期天数
     */
    private static Integer getDaysInTerm(LocalDate loanDate, int currentTerm,LocalDate firstRepayDate) {
        LocalDate lastDueDate = loanDate;
        LocalDate currentDueDate = firstRepayDate;
        if(currentTerm > 0){
            lastDueDate = getCurrentDueDate(firstRepayDate,currentTerm - 1);
            currentDueDate = getCurrentDueDate(firstRepayDate,currentTerm);
        }
        return  (int) DateUtil.betweenDays(lastDueDate,currentDueDate);
    }

    /**
     * 根据期数获取当期的还款日
     * @param firstRepayDate 第一个账单日
     * @param i              所在期数 - 1
     * @return
     */
    private static LocalDate getCurrentDueDate(LocalDate firstRepayDate, int i) {
        Integer year = firstRepayDate.getYear();
        Integer Month = firstRepayDate.getMonthValue();
        Integer dayOfMonth = firstRepayDate.getDayOfMonth();
        YearMonth yearMonth = YearMonth.of(year, Month);
        String repayDay = plusMonthsNew(yearMonth,i) + (dayOfMonth < 10 ? "0" + dayOfMonth:dayOfMonth);
        if(yearMonth.plusMonths(i).lengthOfMonth() < dayOfMonth){
            repayDay =plusMonthsNew(yearMonth,i + 1) + "01";
        }
        return DateUtil.parseDate(repayDay, DateUtil.DATE_TIME_FORMATTER_8);
    }

    /**
     * 散标等额本息计算
     * @param principal
     * @param yearRate
     * @param terms
     * @return
     */
    public static CalcResult calcMCEISubject(Integer principal, BigDecimal yearRate, Integer terms){
        BigDecimal monthRate = yearRate.divide(ONEYEAR_MONTHS,8,BigDecimal.ROUND_HALF_DOWN);
//       BigDecimal monthRate = yearRate.multiply(BigDecimal.valueOf(30)).divide(BigDecimal.valueOf(365),6,BigDecimal.ROUND_HALF_DOWN);
        CalcResult calcResult = new CalcResult();
        //以元来进行计算
        BigDecimal principalBig = new BigDecimal(principal);
        //等额本息=借款本金*月利率*(1+月利率)^还款月数/(1+月利率)^还款月数-1
        BigDecimal top = principalBig.multiply(monthRate).multiply((monthRate.add(ONE)).pow(terms));//分子
        BigDecimal bottom = ((ONE.add(monthRate)).pow(terms)).subtract(ONE);
        BigDecimal monthRepay = top.divide(bottom,8,BigDecimal.ROUND_HALF_DOWN);

        calcResult.setTerms(terms);

        TreeMap<Integer,CalcResult.Detail> details = new TreeMap<>();

        //剩余未还本金
        BigDecimal principalRemain = principalBig;
        int totalRepayPrincipal = 0;
        for(int i=1;i<=terms;i++){
            CalcResult.Detail detail = new CalcResult.Detail();
            //月还利息
            BigDecimal monthRepayInterest = principalRemain.multiply(monthRate);

            detail.setTerm(i);
            if(i == terms){
                //最后一期月还本金=总借款本金-前面N期已还本金
                detail.setMonthRepayPrincipal(principal-totalRepayPrincipal);
            }else{
                //月还本金
                BigDecimal monthRepayPrincipal = monthRepay.subtract(monthRepayInterest);
                detail.setMonthRepayPrincipal(monthRepayPrincipal.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
            }
            totalRepayPrincipal+=detail.getMonthRepayPrincipal();
            principalRemain = principalRemain.subtract(BigDecimal.valueOf(detail.getMonthRepayPrincipal()));
            detail.setMonthRepayInterest(monthRepayInterest.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
            detail.setMonthRepay(detail.getMonthRepayPrincipal()+detail.getMonthRepayInterest());
            detail.setRemainPrincipal(principalRemain.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());

            details.put(i,detail);
        }
        calcResult.setDetails(details);

        int totalRepay = details.entrySet().stream().map(Map.Entry::getValue).map(CalcResult.Detail::getMonthRepay).reduce((a, b)->a+b).orElse(0);
        calcResult.setTotalRepay(totalRepay);
        calcResult.setTotalRepayPrincipal(totalRepayPrincipal);
        calcResult.setTotalRepayInterest(totalRepay-totalRepayPrincipal);

        return calcResult;
    }
    /**
     * 等额本金计算
     * @param principal
     * @param yearRate
     * @param terms
     * @return
     */
    public static CalcResult calcMCEP(Integer principal, BigDecimal yearRate, Integer terms){
        BigDecimal monthRate = yearRate.divide(ONEYEAR_MONTHS,8,BigDecimal.ROUND_HALF_UP);
        CalcResult calcResult = new CalcResult();
        TreeMap<Integer,CalcResult.Detail> details = new TreeMap<Integer,CalcResult.Detail>();
        //以元为单位做计算
        BigDecimal principalBig = new BigDecimal(principal);
        BigDecimal remainPrincipel = principalBig;//已归还本金

        BigDecimal totalRepayPrincipal = BigDecimal.ZERO;//总还本金
        BigDecimal totalRepayInterest = BigDecimal.ZERO;//总还利息

        for(int i=1;i<=terms;i++){
            CalcResult.Detail detail = new CalcResult.Detail();

            //月还本金=本金/期数
            BigDecimal monthRepayPrinciple = principalBig.divide(new BigDecimal(terms),8,BigDecimal.ROUND_HALF_UP);
            totalRepayPrincipal = totalRepayPrincipal.add(monthRepayPrinciple);

            //利息=剩余本金*月利率
            BigDecimal monthRepayInterest = remainPrincipel.multiply(monthRate);
            totalRepayInterest = totalRepayInterest.add(monthRepayInterest);

            //月还款额=月还本金+月还利息
            BigDecimal monthRepay = monthRepayPrinciple.add(monthRepayInterest);

            remainPrincipel = remainPrincipel.subtract(monthRepayPrinciple);

            detail.setTerm(i);
            detail.setMonthRepay(monthRepay.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
            detail.setMonthRepayPrincipal(monthRepayPrinciple.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
            detail.setMonthRepayInterest(monthRepayInterest.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
            detail.setRemainPrincipal(remainPrincipel.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
            details.put(i,detail);
        }

        calcResult.setTerms(terms);
        BigDecimal totalRepay = totalRepayPrincipal.add(totalRepayInterest);
        calcResult.setTotalRepay(totalRepay.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
        calcResult.setTotalRepayInterest(totalRepayPrincipal.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
        calcResult.setTotalRepayPrincipal(totalRepayInterest.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
        calcResult.setDetails(details);


        return calcResult;
    }
    /**
     * 散标等额本金计算
     * @param principal
     * @param yearRate
     * @param terms
     * @return
     */
    public static CalcResult calcMCEPSubject(Integer principal, BigDecimal yearRate, Integer terms){
        BigDecimal monthRate = yearRate.divide(ONEYEAR_MONTHS,8,BigDecimal.ROUND_HALF_DOWN);
        CalcResult calcResult = new CalcResult();
        TreeMap<Integer,CalcResult.Detail> details = new TreeMap<Integer,CalcResult.Detail>();
        //以元为单位做计算
        BigDecimal principalBig = new BigDecimal(principal);
        BigDecimal remainPrincipel = principalBig;//已归还本金

        BigDecimal totalRepayPrincipal = BigDecimal.ZERO;//总还本金
        BigDecimal totalRepayInterest = BigDecimal.ZERO;//总还利息

        for(int i=1;i<=terms;i++){
            CalcResult.Detail detail = new CalcResult.Detail();

            //月还本金=本金/期数
            BigDecimal monthRepayPrinciple = principalBig.divide(new BigDecimal(terms),8,BigDecimal.ROUND_HALF_DOWN);
            totalRepayPrincipal = totalRepayPrincipal.add(monthRepayPrinciple);

            //利息=剩余本金*月利率
            BigDecimal monthRepayInterest = remainPrincipel.multiply(monthRate);
            totalRepayInterest = totalRepayInterest.add(monthRepayInterest);

            //月还款额=月还本金+月还利息
            BigDecimal monthRepay = monthRepayPrinciple.add(monthRepayInterest);

            remainPrincipel = remainPrincipel.subtract(monthRepayPrinciple);

            detail.setTerm(i);
            detail.setMonthRepay(monthRepay.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
            detail.setMonthRepayPrincipal(monthRepayPrinciple.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
            detail.setMonthRepayInterest(monthRepayInterest.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
            detail.setRemainPrincipal(remainPrincipel.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
            details.put(i,detail);
        }

        calcResult.setTerms(terms);
        BigDecimal totalRepay = totalRepayPrincipal.add(totalRepayInterest);
        calcResult.setTotalRepay(totalRepay.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
        calcResult.setTotalRepayInterest(totalRepayPrincipal.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
        calcResult.setTotalRepayPrincipal(totalRepayInterest.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
        calcResult.setDetails(details);


        return calcResult;
    }
    /**
     * 按月付息到期还本计算
     * @param principal
     * @param yearRate
     * @param terms
     * @return
     */
    public static CalcResult calcIFPA(Integer principal, BigDecimal yearRate, Integer terms){
        BigDecimal monthRate = yearRate.divide(ONEYEAR_MONTHS,8,BigDecimal.ROUND_HALF_UP);
        CalcResult calcResult = new CalcResult();
        TreeMap<Integer,CalcResult.Detail> details = new TreeMap<>();

        //以元为单位做计算
        BigDecimal principalBig = new BigDecimal(principal);

        BigDecimal monthRepayInterest = principalBig.multiply(monthRate);

        BigDecimal totalInterest = monthRepayInterest.multiply(new BigDecimal(terms));

        for(int i=1;i<=terms;i++){
            CalcResult.Detail detail = new CalcResult.Detail();
            detail.setTerm(i);
            //最后一期要还本金
            if(i==terms){
                detail.setMonthRepay(principalBig.add(monthRepayInterest).setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
                detail.setMonthRepayInterest(monthRepayInterest.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
                detail.setMonthRepayPrincipal(principal);
                detail.setRemainPrincipal(0);
            }else{
                detail.setMonthRepay(monthRepayInterest.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
                detail.setMonthRepayInterest(monthRepayInterest.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
                detail.setMonthRepayPrincipal(0);
                detail.setRemainPrincipal(principal);
            }
            details.put(i,detail);

        }

        calcResult.setDetails(details);
        calcResult.setTotalRepay(totalInterest.add(principalBig).setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
        calcResult.setTerms(terms);
        calcResult.setTotalRepayPrincipal(principal);
        calcResult.setTotalRepayInterest(totalInterest.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());

        return calcResult;
    }

    /**
     * 散标按月付息到期还本计算
     * @param principal
     * @param yearRate
     * @param terms
     * @return
     */
    public static CalcResult calcIFPASubject(Integer principal, BigDecimal yearRate, Integer terms){
        BigDecimal monthRate = yearRate.divide(ONEYEAR_MONTHS,8,BigDecimal.ROUND_HALF_DOWN);
        CalcResult calcResult = new CalcResult();
        TreeMap<Integer,CalcResult.Detail> details = new TreeMap<>();

        //以元为单位做计算
        BigDecimal principalBig = new BigDecimal(principal);

        BigDecimal monthRepayInterest = principalBig.multiply(monthRate);

        BigDecimal totalInterest = monthRepayInterest.multiply(new BigDecimal(terms));

        for(int i=1;i<=terms;i++){
            CalcResult.Detail detail = new CalcResult.Detail();
            detail.setTerm(i);
            //最后一期要还本金
            if(i==terms){
                detail.setMonthRepay(principalBig.add(monthRepayInterest).setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
                detail.setMonthRepayInterest(monthRepayInterest.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
                detail.setMonthRepayPrincipal(principal);
                detail.setRemainPrincipal(0);
            }else{
                detail.setMonthRepay(monthRepayInterest.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
                detail.setMonthRepayInterest(monthRepayInterest.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
                detail.setMonthRepayPrincipal(0);
                detail.setRemainPrincipal(principal);
            }
            details.put(i,detail);

        }

        calcResult.setDetails(details);
        calcResult.setTotalRepay(totalInterest.add(principalBig).setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
        calcResult.setTerms(terms);
        calcResult.setTotalRepayPrincipal(principal);
        calcResult.setTotalRepayInterest(totalInterest.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());

        return calcResult;
    }

    /**
     * 一次性还款
     * @param principal
     * @param yearRate
     * @param calcDays
     * @return
     */
    public static CalcResult calcOTRP(Integer principal, BigDecimal yearRate, Integer calcDays){
        BigDecimal dayRate;
        if(calcDays<ONEMONTH_DAYS||calcDays>ONEMONTH_DAYS){
            dayRate = yearRate.divide(ONEYEAR_DAYS_356,8,BigDecimal.ROUND_HALF_UP);
        } else {
            dayRate = yearRate.divide(ONEYEAR_DAYS,8,BigDecimal.ROUND_HALF_UP);
        }

        CalcResult result = new CalcResult();

        TreeMap<Integer,CalcResult.Detail> details = new TreeMap<>();

        BigDecimal principalBig = new BigDecimal(principal);

        BigDecimal totalRepayInterest = principalBig.multiply(dayRate).multiply(new BigDecimal(calcDays));

        BigDecimal totalRepay = principalBig.add(totalRepayInterest);

        /*for(int i=1;i<terms;i++){
            CalcResult.Detail detail = new CalcResult.Detail();

            detail.setTerm(i);
            detail.setMonthRepayInterest(0);
            detail.setMonthRepay(0);
            detail.setMonthRepayPrincipal(0);
            detail.setRemainPrincipal(principal);
            details.put(i,detail);
        }*/

        CalcResult.Detail detail = new CalcResult.Detail();
        detail.setTerm(1);
        detail.setRemainPrincipal(0);
        detail.setMonthRepayPrincipal(principal);
        detail.setMonthRepayInterest(totalRepayInterest.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
        detail.setMonthRepay(totalRepay.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
        details.put(1,detail);

        result.setTerms(1);
        result.setTotalRepayInterest(totalRepayInterest.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
        result.setTotalRepayPrincipal(principal);
        result.setTotalRepay(totalRepay.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
        result.setDetails(details);

        return result;
    }

    /**
     * 一次性还款
     * @param principal
     * @param yearRate
     * @param calcDays
     * @param interestAccrualType 计息方式
     * @return
     */
    public static CalcResult calcOTRP(Integer principal, BigDecimal yearRate, Integer calcDays, Integer interestAccrualType){
        BigDecimal dayRate;
        if(INTEREST_ACCRUAL_TYPE_DAY.equals(interestAccrualType)){
            dayRate = yearRate.divide(ONEYEAR_DAYS_356,8,BigDecimal.ROUND_HALF_UP);
        } else {
            dayRate = yearRate.divide(ONEYEAR_DAYS,8,BigDecimal.ROUND_HALF_UP);
        }

        CalcResult result = new CalcResult();

        TreeMap<Integer,CalcResult.Detail> details = new TreeMap<>();

        BigDecimal principalBig = new BigDecimal(principal);

        BigDecimal totalRepayInterest = principalBig.multiply(dayRate).multiply(new BigDecimal(calcDays));

        BigDecimal totalRepay = principalBig.add(totalRepayInterest);

        /*for(int i=1;i<terms;i++){
            CalcResult.Detail detail = new CalcResult.Detail();

            detail.setTerm(i);
            detail.setMonthRepayInterest(0);
            detail.setMonthRepay(0);
            detail.setMonthRepayPrincipal(0);
            detail.setRemainPrincipal(principal);
            details.put(i,detail);
        }*/

        CalcResult.Detail detail = new CalcResult.Detail();
        detail.setTerm(1);
        detail.setRemainPrincipal(0);
        detail.setMonthRepayPrincipal(principal);
        detail.setMonthRepayInterest(totalRepayInterest.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
        detail.setMonthRepay(totalRepay.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
        details.put(1,detail);

        result.setTerms(1);
        result.setTotalRepayInterest(totalRepayInterest.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
        result.setTotalRepayPrincipal(principal);
        result.setTotalRepay(totalRepay.setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
        result.setDetails(details);

        return result;
    }
    /**
     * 散标一次性还款
     * @param principal
     * @param yearRate
     * @param calcDays
     * @return
     */
    public static CalcResult calcOTRPSubject(Integer principal, BigDecimal yearRate, Integer calcDays){
        BigDecimal dayRate;
        if(calcDays<ONEMONTH_DAYS){
            dayRate = yearRate.divide(ONEYEAR_DAYS_356,8,BigDecimal.ROUND_HALF_DOWN);
        } else {
            dayRate = yearRate.divide(ONEYEAR_DAYS,8,BigDecimal.ROUND_HALF_DOWN);
        }

        CalcResult result = new CalcResult();

        TreeMap<Integer,CalcResult.Detail> details = new TreeMap<>();

        BigDecimal principalBig = new BigDecimal(principal);

        BigDecimal totalRepayInterest = principalBig.multiply(dayRate).multiply(new BigDecimal(calcDays));

        BigDecimal totalRepay = principalBig.add(totalRepayInterest);

        /*for(int i=1;i<terms;i++){
            CalcResult.Detail detail = new CalcResult.Detail();

            detail.setTerm(i);
            detail.setMonthRepayInterest(0);
            detail.setMonthRepay(0);
            detail.setMonthRepayPrincipal(0);
            detail.setRemainPrincipal(principal);
            details.put(i,detail);
        }*/

        CalcResult.Detail detail = new CalcResult.Detail();
        detail.setTerm(1);
        detail.setRemainPrincipal(0);
        detail.setMonthRepayPrincipal(principal);
        detail.setMonthRepayInterest(totalRepayInterest.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
        detail.setMonthRepay(totalRepay.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
        details.put(1,detail);

        result.setTerms(1);
        result.setTotalRepayInterest(totalRepayInterest.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
        result.setTotalRepayPrincipal(principal);
        result.setTotalRepay(totalRepay.setScale(0,BigDecimal.ROUND_HALF_DOWN).intValue());
        result.setDetails(details);

        return result;
    }

    public static class CalcResult{

        private Integer terms;//期数

        private Integer totalRepay;//总还款额

        private Integer totalRepayPrincipal;//总还款本金

        private Integer totalRepayInterest;//总还款利息

        //能够根据期数取出需要的数据
        private TreeMap<Integer,Detail> details;

        @Override
        public String toString() {
            StringBuilder calResult = new StringBuilder("计算结果{总期数=").append(terms)
                    .append(",总还款额=").append(totalRepay)
                    .append(",总还本金=").append(totalRepayPrincipal)
                    .append(",总还利息=").append(totalRepayInterest).append("}").append("\n");


            for(Map.Entry<Integer,Detail> detail:details.entrySet()){
                calResult.append(detail.getValue().toString()).append("\n");
            }
            return calResult.toString();
        }

        public TreeMap<Integer, Detail> getDetails() {
            return details;
        }

        public void setDetails(TreeMap<Integer, Detail> details) {
            this.details = details;
        }

        public Integer getTerms() {
            return terms;
        }

        public void setTerms(Integer terms) {
            this.terms = terms;
        }

        public Integer getTotalRepay() {
            return totalRepay;
        }

        public void setTotalRepay(Integer totalRepay) {
            this.totalRepay = totalRepay;
        }

        public Integer getTotalRepayPrincipal() {
            return totalRepayPrincipal;
        }

        public void setTotalRepayPrincipal(Integer totalRepayPrincipal) {
            this.totalRepayPrincipal = totalRepayPrincipal;
        }

        public Integer getTotalRepayInterest() {
            return totalRepayInterest;
        }

        public void setTotalRepayInterest(Integer totalRepayInterest) {
            this.totalRepayInterest = totalRepayInterest;
        }

        public static class Detail implements Comparable<Detail>{

            private Integer term;//第几期

            private Integer monthRepay;//月还本息

            private Integer monthRepayPrincipal;//月还本金

            private Integer monthRepayInterest;//月还利息

            private Integer remainPrincipal;//剩余本金

            private String dueDate;//应还日期

            public String getDueDate() {
                return dueDate;
            }

            public void setDueDate(String dueDate) {
                this.dueDate = dueDate;
            }

            public Integer getTerm() {
                return term;
            }

            public void setTerm(Integer term) {
                this.term = term;
            }

            public Integer getMonthRepay() {
                return monthRepay;
            }

            public void setMonthRepay(Integer monthRepay) {
                this.monthRepay = monthRepay;
            }

            public Integer getMonthRepayPrincipal() {
                return monthRepayPrincipal;
            }

            public void setMonthRepayPrincipal(Integer monthRepayPrincipal) {
                this.monthRepayPrincipal = monthRepayPrincipal;
            }

            public Integer getMonthRepayInterest() {
                return monthRepayInterest;
            }

            public void setMonthRepayInterest(Integer monthRepayInterest) {
                this.monthRepayInterest = monthRepayInterest;
            }

            public Integer getRemainPrincipal() {
                return remainPrincipal;
            }

            public void setRemainPrincipal(Integer remainPrincipal) {
                this.remainPrincipal = remainPrincipal;
            }

            @Override
            public String toString() {
                return  "{期数=" + term +
                        ", 月还款额=" + monthRepay +
                        ", 月还本金=" + monthRepayPrincipal +
                        ", 月还利息=" + monthRepayInterest +
                        ", 剩余本金=" + remainPrincipal +
                        '}';
            }

            @Override
            public int compareTo(Detail detail) {
                return this.term.compareTo(detail.term);
            }
        }
    }

    /**
     * 计算本利
     * @param principal 本金
     * @param rate 利率
     * @param days 天数
     * @return
     */
    public static Integer calcPrincipalInterest(Integer principal, BigDecimal rate, Integer days) {
        return principal + calcInterest(principal, rate, days);
    }

    /**
     * 利息计算
     * 算法 ： 本金 * 利率（年）/ 一年的天数（365） * 天数
     *
     * @param principal   本金
     * @param rate      利率
     * @param days      天数
     */
    public static Integer calcInterest(Integer principal, BigDecimal rate, Integer days) {
        Integer interest = new BigDecimal(principal).multiply(rate).multiply(new BigDecimal(days))
                .divide(new BigDecimal(365), 0, BigDecimal.ROUND_DOWN).intValue();
        return interest;
    }
    /**
     * 利息计算 按月
     * 算法 ： 本金 * 利率（年）* 月份/ 12 月
     *
     * @param principal   本金
     * @param rate      利率
     * @param months      天数
     */
    public static Integer calcInterestByMonth(Integer principal, BigDecimal rate, Integer months) {
        Integer interest = new BigDecimal(principal).multiply(rate).multiply(new BigDecimal(months))
                .divide(new BigDecimal(12), 0, BigDecimal.ROUND_DOWN).intValue();
        return interest;
    }

    private static String plusMonthsNew(YearMonth yearMonth,int monthsToAdd) {
        return yearMonth.plusMonths(monthsToAdd).format(DateTimeFormatter.ofPattern("yyyyMM"));
    }
}
