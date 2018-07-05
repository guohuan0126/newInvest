package com.jiuyi.test;

import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.BatchApplication;
import com.jiuyi.ndr.batch.credit.CreditAutoCancelTasklet;
import com.jiuyi.ndr.batch.credit.CreditCancelTasklet;
import com.jiuyi.ndr.batch.iplan.IPlanTradeCompensateTasklet;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.subject.*;
import com.jiuyi.ndr.dao.xm.TransLogDao;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.subject.*;
import com.jiuyi.ndr.domain.xm.TransLog;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanRepayDetailService;
import com.jiuyi.ndr.service.iplan.IPlanRepayScheduleService;
import com.jiuyi.ndr.service.iplan.IPlanTransLogService;
import com.jiuyi.ndr.service.lplan.LPlanAccountService;
import com.jiuyi.ndr.service.notice.support.TemplateId;
import com.jiuyi.ndr.service.redpacket.RedpacketService;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.subject.SubjectTransLogService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.HttpClient;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestCancelDebentureSale;
import com.jiuyi.ndr.xm.http.request.RequestIntelligentProjectDebentureSale;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by drw on 2017/8/7.
 */
@SpringBootTest(classes = BatchApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dem03")
public class CreditTest {

    @Autowired
    CreditService creditService;

    @Autowired
    CreditDao creditDao;

    @Autowired
    SubjectService subjectService;

    @Autowired
    SubjectTransLogService subjectTransLogService;

    @Autowired
    SubjectTransLogDao subjectTransLogDao;

    @Autowired
    CreditCancelTasklet creditCancelTasklet;
    @Autowired
    TransactionService transactionService;
    @Autowired
    SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    CreditAutoCancelTasklet creditAutoCancelTasklet;
    @Autowired
    IPlanRepayScheduleService iPlanRepayScheduleService;
    @Autowired
    IPlanRepayDetailService iPlanRepayDetailService;
    @Autowired
    RedpacketService redpacketService;
    @Autowired
    IPlanDao iPlanDao;
    @Autowired
    SubjectSendSmsDao subjectSendSmsDao;
    @Autowired
    CreditOpeningDao creditOpeningDao;
    @Autowired
    SubjectDao subjectDao;
    @Autowired
    SubjectTransferParamDao subjectTransferParamDao;
    @Autowired
    CreditOpeningService creditOpeningService;
    @Autowired
    SubjectAccountDao subjectAccountDao;
    @Autowired
    SubjectAccountService subjectAccountService;
    @Autowired
    IPlanAccountService iPlanAccountService;
    @Autowired
    private LPlanAccountService lPlanAccountService;
    @Autowired
    TransLogDao transLogDao;
    @Autowired
    IPlanTradeCompensateTasklet iPlanTradeCompensateTasklet;
    @Autowired
    IPlanTransLogService iPlanTransLogService;

    private DecimalFormat df4 = new DecimalFormat("######0.##");

    @Test
    public  void testForce(){
        LocalDate calcInterestDate = DateUtil.parseDateTime("2018-05-30 18:00:00", DateUtil.DATE_TIME_FORMATTER_19).toLocalDate().plusDays(1);
        System.out.println("+++++++++++++++++++++"+calcInterestDate);
        String date =  calcInterestDate.plusMonths(1).format(DateUtil.DATE_TIME_FORMATTER_10);
        System.out.println("+++++++++++++++++++++"+date);
    }

    @Test
    public void testSchedule(){
        Subject subject = subjectDao.findBySubjectId("NDR171208152140000001");
        subjectRepayScheduleService.makeUpCreditCardRepaySchedule(subject);
    }

    @Test
    public void testTransLog(){
        IPlan iPlan = iPlanDao.findById(4576);
        IPlanAccount iPlanAccount = iPlanAccountService.findById(23);
        iPlanAccountService.calcInterest(iPlanAccount,iPlan);
    }

    @Test
    public void testRedis() {
        IPlanAccount account = iPlanAccountService.findById(67);
        IPlan iplan = iPlanDao.findById(70);
        iPlanAccountService.calcInterest(account,iplan);
    }

    @Test
    public void testMan(){
        Integer dayDifference = DateUtil.dayDifference("2018-05-01", "2018-05-01");
        System.out.println(dayDifference);
    }

    @Test
    public void creditLoanTest(){
        //List<TransLog> transLogListCredit = transLogDao.findByTransCodeAndStatus(TransCode.CREDIT_TRANSFER.getCode(),TransLog.STATUS_PENDING);
        TransLog transLog = transLogDao.findById(4636);
        //for (TransLog t:transLogListCredit) {
            String json = transLog.getRequestPacket();
            RequestIntelligentProjectDebentureSale request = JSON.parseObject(json, RequestIntelligentProjectDebentureSale.class);
            List<RequestIntelligentProjectDebentureSale.Detail> details = request.getDetails();
            for (RequestIntelligentProjectDebentureSale.Detail d:details) {
                RequestCancelDebentureSale req = new RequestCancelDebentureSale();
                req.setRequestNo(IdUtil.getRequestNo());
                req.setCreditsaleRequestNo(d.getSaleRequestNo());
                req.setTransCode(TransCode.CREDIT_CANCEL.getCode());

                BaseResponse baseResponse = null;
                try {
                    //logger.info("开始调用厦门银行取消债权出让接口->{}", JSON.toJSONString(request));
                    baseResponse = transactionService.cancelDebentureSale(req);
                    //logger.info("取消债权出让接口返回->{}", JSON.toJSONString(baseResponse));
                } catch (Exception e) {
                    if (baseResponse == null) {
                        baseResponse = new BaseResponse();
                    }
                    baseResponse.setStatus(BaseResponse.STATUS_PENDING);
                }
            }

       // }
    }

    @Test
    public void testInvest(){
        //iPlanAccountService.investIPlan(1211);
    }



    @Test
    public void test1(){
        LocalDate localDate = DateUtil.parseDate("20171219", DateUtil.DATE_TIME_FORMATTER_8);
        String dateStr = DateUtil.getDateStr(localDate, DateUtil.DATE_TIME_FORMATTER_10)+" 00:00:00";
        System.out.println(dateStr);
    }
    @Test
    public void creditLoanLocalTest(){
        List<CreditOpening> creditOpenings = creditOpeningDao.findIPlanInvestableCreditOpening();
        //根据原标的结束日期正序
        Comparator<CreditOpening> creditOpeningComparator = Comparator.comparing(CreditOpening::getEndTime).thenComparing(CreditOpening::getOpenTime);
        Collections.sort(creditOpenings,creditOpeningComparator);
        for (CreditOpening c:creditOpenings) {
            System.out.println("id:"+c.getId()+",end_time:"+c.getEndTime()+"open_time:"+c.getOpenTime());
        }
    }
    @Test
    public void test(){
        List<Credit> credits = creditDao.findByUserIdAndCreditStatusAndSourceChannel("EVn2Ez2ueYVboide",
                Arrays.asList(Credit.CREDIT_STATUS_WAIT, Credit.CREDIT_STATUS_HOLDING), Credit.SOURCE_CHANNEL_LPLAN);//持有中或等待放款的债权的活期债权
    }

    private void system(Integer m){
        List<Credit> credits = creditDao.findByUserIdAndCreditStatusAndSourceChannel("EVn2Ez2ueYVboide",
                Arrays.asList(Credit.CREDIT_STATUS_WAIT, Credit.CREDIT_STATUS_HOLDING), Credit.SOURCE_CHANNEL_LPLAN);//持有中或等待放款的债权的活期债权
    }
    @Test
    public void testLend(){
        subjectService.lend("NDR171113114831000011");
    }

    //债权放款
    @Test
    public void testCreditLoan(){
        List<Credit> credits = creditDao.findLoanCredit();
        for (Credit credit : credits) {
            creditService.creditLoan(credit);
        }
    }
    //债权放款本地处理
    @Test
    public void testCreditLoanLocal(){
        List<Credit> loanLocalCredit = creditDao.findLoanLocalCredit();
        creditService.creditLocalHandle(loanLocalCredit);
    }

    @Test
    public void testTranslog(){
        subjectTransLogService.exitSubject(548);
    }


    @Test
    public void testCreate(){
        List<SubjectTransLog> transLogs = subjectTransLogDao.findByTypAndExtStatus();
        for (SubjectTransLog transLog : transLogs) {
            creditService.creditCreate(transLog);
        }
    }
    @Test
    public void exitLPlanTest5() throws InterruptedException {
        List<Thread> list = new ArrayList<>();
        for(int i=0;i<100;i++){
            list.add(new Thread(()->{
                //单笔查询
                RequestSingleTransQuery request = new RequestSingleTransQuery();
                request.setRequestNo("NDR20171114114639fbd302");
                request.setTransactionType(TransactionType.DEBENTURE_SALE);
                ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
                System.out.println("查询变更标的状态:"+responseQuery);
            }));
        }
        for (Thread thread:list){
            thread.start();
        }

        byte[]s = new byte[0];
        synchronized (s){
            s.wait();
        }

    }

    public static void main(String[] args) {
        List<Thread> list = new ArrayList<>();
        String url = "http://localhost:8292/client/test";
        List<NameValuePair> params = new ArrayList<>();
        for(int i=0;i<5;i++){
            list.add(new Thread(()->{
                //https调用
                String httpResp;
                try {
                    httpResp = HttpClient.post(url, new Header[0], params);
                } catch (Exception e) {
                    System.out.println("HTTP请求异常 \n {}"+e.getMessage());
                    throw new ProcessException(Error.NDR_0801);
                }
            }));
        }
        for (Thread thread:list){
            thread.start();
        }
    }
    @Test
    public void testHttpClient() throws InterruptedException {
        List<Thread> list = new ArrayList<>();
        String url = "http://localhost:8292/client/test";
        List<NameValuePair> params = new ArrayList<>();
        for(int i=0;i<100;i++){
            list.add(new Thread(()->{
                //https调用
                String httpResp;
                try {
                    httpResp = HttpClient.post(url, new Header[0], params);
                } catch (Exception e) {
                    System.out.println("HTTP请求异常 \n {}"+e.getMessage());
                    throw new ProcessException(Error.NDR_0801);
                }
            }));
        }
        for (Thread thread:list){
            thread.start();
        }

        byte[]s = new byte[0];
        synchronized (s){
            s.wait();
        }

    }
    @Test
    public void cancel(){

        System.out.println(new BigDecimal(0.995).doubleValue()+"++++++++++++++");
        Double expectRate = new BigDecimal(0.12).divide(new BigDecimal(0.95),4, RoundingMode.HALF_UP).doubleValue();
        System.out.println(expectRate+"++++++++++++++");
    }
    @Test
    public void cancelFor(){
        List<String> list = new ArrayList<>();
        for (String s : list) {
            System.out.println("+++++++++");
            System.out.println(s);
        }
    }
    @Test
    public void cancelForE(){
        Subject subject = subjectService.findBySubjectId("NDR171118143807000028");
        subjectRepayScheduleService.makeUpRepaySchedule(subject);
    }
    @Test
    public void cancelForAuto(){
        creditAutoCancelTasklet.creditAutoCancel();
    }

    @Test
    public void iplan(){
        IPlan iPlan = iPlanDao.findById(3);
        /*iPlanRepayScheduleService.genIPlanRepaySchedule(3);
        iPlanRepayDetailService.genRepayDetail(3);*/
        //奖励发放
        redpacketService.createPacketInvest(iPlan);
        iPlan.setStatus(IPlan.STATUS_EARNING);
        iPlan.setRaiseFinishTime(DateUtil.getCurrentDateTime19());
        //开始计息日期
        LocalDate calcInterestDate = DateUtil.parseDateTime(iPlan.getRaiseFinishTime(), DateUtil.DATE_TIME_FORMATTER_19).plusDays(1).toLocalDate();
        iPlan.setEndTime(calcInterestDate.plusMonths(iPlan.getTerm()).format(DateUtil.DATE_TIME_FORMATTER_10));
        iPlan.setUpdateTime(DateUtil.getCurrentDateTime19());
        iPlanDao.update(iPlan);
    }

    @Test
    public void time(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = now.withHour(16).withMinute(0).withSecond(0);
        System.out.println(now);
        System.out.println(start);
        System.out.println(end);
        if(now.isAfter(start) && now.isBefore(end)){
            System.out.println("=======================");
        }
    }
    @Test
    public void calc(){
        double round = ArithUtil.round(0.005, 2);
        System.out.println(round);
        Integer totalAmt = 500;
        BigDecimal bigDecimal = new BigDecimal(1.005);
        System.out.println("Bigdecimal"+bigDecimal.doubleValue());
        double over = bigDecimal.doubleValue() - 1;
        System.out.println("over"+over);
        Double v = (totalAmt / 100.0) * (bigDecimal.doubleValue() - 1) * 0.2;
        Double neVal = Double.valueOf(df4.format(v));
        System.out.println(v);
        System.out.println(neVal);
    }
    @Test
    public void calc1(){
        double round = ArithUtil.round(0.005, 2);
        System.out.println(round);
        Integer totalAmt = 500;
        BigDecimal bigDecimal = new BigDecimal(1.005);
        Double transferDiscount = bigDecimal.multiply(new BigDecimal(100)).doubleValue();
        System.out.println("Bigdecimal"+transferDiscount);
        Double over = bigDecimal.doubleValue() - 1;
        System.out.println("over"+over);
        Double newValue = Double.valueOf(df4.format(transferDiscount));
        System.out.println("newValue"+newValue);
        System.out.println(df4.format(transferDiscount));
        Double v = (totalAmt / 100.0) * (transferDiscount - 100) / 100.0 * 0.2;
        System.out.println(v);
        Double overFee = (totalAmt /100.0) * (transferDiscount - 100) / 100.0 * 0.2;
        System.out.println(overFee);
    }
    @Test
    public void calc2(){
        Integer totalAmt = 500;
        CreditOpening creditOpening = creditOpeningDao.findById(3);
        Double transferDiscount = creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue();
        System.out.println("Bigdecimal"+transferDiscount);
        Double v = (totalAmt / 100.0) * (transferDiscount - 100) / 100.0 * 0.2;
        System.out.println((transferDiscount - 100) / 100.0);
        System.out.println(v);
        BigDecimal newVal = creditOpening.getTransferDiscount();
        Double overFee = (totalAmt /100.0) * (newVal.doubleValue() - 1)  * 0.2;
        System.out.println((newVal.doubleValue() - 1));
        System.out.println(overFee);
    }

    @Test
    public void sms(){
        //插入一条短信记录
        SubjectSendSms sms = new SubjectSendSms();
        sms.setUserId("fasghj");
        sms.setStatus(SubjectSendSms.HAS_NOT_SEND_MSG);
        String msg = "";
        msg = subjectSendSmsDao.getFromUserMessageTemplateById(TemplateId.CREDIT_AUTO_CANCLE);
        msg = StringUtils.replace(msg,
                "#{creditId}", "金农宝");
        msg = StringUtils.replace(msg,
                "#{principal}", String.valueOf(200000/100.0));
        msg = StringUtils.replace(msg,
                "#{money}", String.valueOf(1000.0));
        sms.setType(TemplateId.CREDIT_AUTO_CANCLE);
        sms.setMsg(msg);
        sms.setContent(msg);
        sms.setMobileNumber("15136456879");
        subjectSendSmsDao.insert(sms);
    }


    @Test
    public void testCancel(){
        CreditOpening creditOpening = creditOpeningDao.findById(166);
        String createTime = creditOpening.getCreateTime().substring(0,10);
        LocalDate dateStart = DateUtil.parseDate(createTime, DateUtil.DATE_TIME_FORMATTER_10);
        Subject subject = subjectDao.findBySubjectId(creditOpening.getSubjectId());
        SubjectTransferParam transferParamCode = subjectTransferParamDao.findByTransferParamCode(subject.getTransferParamCode());
        LocalDate currentDate = LocalDate.now();
        System.out.println(dateStart.plusDays(transferParamCode.getAutoRevokeTime()));
        boolean before = dateStart.plusDays(transferParamCode.getAutoRevokeTime()).isBefore(currentDate);
        System.out.println(before);
        if(before){
            creditOpeningService.cancleCredit(creditOpening.getId());
        }
    }

    @Test
    public  void testIn(){
        SubjectAccount subjectAccount = subjectAccountDao.findById(21);
        Subject subject = subjectDao.findBySubjectId("NDR171129155752000005");
        Credit credit = creditDao.findById(58);
        Double ex = subjectService.getInterestByRepayType(subjectAccount.getCurrentPrincipal(),subject.getInvestRate(),subject.getRate(),3,subject.getPeriod(),subject.getRepayType())*100;
        Double exinterest = subjectService.getInterestByRepayType(subjectAccount.getCurrentPrincipal(),subject.getBonusRate(),subject.getRate(),3,subject.getPeriod(),subject.getRepayType())*100;
        Integer expectInterest =(int)(subjectService.getInterestByRepayType(subjectAccount.getCurrentPrincipal(),subject.getInvestRate(),subject.getRate(),3,subject.getPeriod(),subject.getRepayType())*100);
        Integer expectBonusInterest = (int)(subjectService.getInterestByRepayType(subjectAccount.getCurrentPrincipal(),subject.getBonusRate(),subject.getRate(),3,subject.getPeriod(),subject.getRepayType())*100);
        System.out.println("doubel"+ex);
        System.out.println("doubel"+exinterest);
        System.out.println("int"+expectInterest);
        System.out.println("int"+expectBonusInterest);
    }

    @Test
    public  void testAc(){
        Integer totalAmt= 1000;
        CreditOpening creditOpening = creditOpeningDao.findById(28);
        SubjectAccount subjectAccount = subjectAccountService.findAccountById(43);
        Subject subject = subjectDao.findBySubjectId(creditOpening.getSubjectId());
        SubjectTransferParam subjectTransferParam = subjectTransferParamDao.findByTransferParamCode(subject.getTransferParamCode());
        Double totalFreeze = ArithUtil.round((totalAmt /100.0) * (creditOpening.getTransferDiscount().doubleValue()) ,2);
        System.out.println(totalFreeze);
        Double feeRate = subjectAccountService.calcTransFeeNew(subjectAccount.getTransLogId(), subject, subjectTransferParam);
        Double fee = (totalAmt /100.0) * (feeRate / 100.0);
        Double totalCommission = ArithUtil.round(fee,2);
        System.out.println(totalCommission);
        totalCommission = ArithUtil.round(totalCommission,2);
        System.out.println(totalCommission);
        Double val = (totalFreeze * 100 - totalCommission * 100);
        System.out.println(val);
        Integer act = (int)(totalFreeze * 100 - totalCommission * 100);
        System.out.println(act);
        Double val2 = ArithUtil.round((totalFreeze * 100 - totalCommission * 100),2);
        System.out.println(val2);
        System.out.println((int) ArithUtil.round((totalFreeze * 100 - totalCommission * 100),2));
    }

    @Test
    public void testDay(){
        String yearMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String loanDay = yearMonth + day(yearMonth, 31);
        System.out.println(loanDay);
        //借款日
        LocalDate loanDate = DateUtil.parseDate(loanDay, DateUtil.DATE_TIME_FORMATTER_8).plusDays(1);
        System.out.println(loanDate);
        //第一个账单日
        String firstRepayDay =plusMonths(1) + day( plusMonths(1), 31);
        LocalDate firstRepayDate = DateUtil.parseDate(firstRepayDay, DateUtil.DATE_TIME_FORMATTER_8).plusDays(1);
        long days = DateUtil.betweenDays(loanDate, firstRepayDate);
        System.out.println(days);
        System.out.println(firstRepayDate);
    }
    @Test
    public  void testDayNow(){
        //第一个账单日
        String firstRepayDay =plusMonths(2) + "09";
        //String firstRepayDay =YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "31";
        System.out.println(firstRepayDay);
        LocalDate firstRepayDate = DateUtil.parseDate(firstRepayDay, DateUtil.DATE_TIME_FORMATTER_8);
        Integer year = firstRepayDate.getYear();
        Integer Month = firstRepayDate.getMonthValue();
        Integer dayOfMonth = firstRepayDate.getDayOfMonth();
        YearMonth yearMonth = YearMonth.of(year, Month);

       /* System.out.println(firstRepayDate);
        System.out.println(dayOfMonth);
        System.out.println("++++++++++++++++++");
        for (int i = 1 ; i < 6;i++) {
            String repayDay = plusMonthsNew(yearMonth,i) + (dayOfMonth < 10 ? "0" + dayOfMonth:dayOfMonth);
            if(yearMonth.plusMonths(i).lengthOfMonth() < dayOfMonth){
                repayDay =plusMonthsNew(yearMonth,i + 1) + "01";
            }
            System.out.println(plusMonthsNew(yearMonth,i));
            LocalDate dueDate = DateUtil.parseDate(repayDay, DateUtil.DATE_TIME_FORMATTER_8);
            System.out.println(dueDate);
        }*/
        System.out.println(firstRepayDate);
        System.out.println("++++++++++++++");
        for (int i = 0 ; i < 6;i++){
            System.out.println(getCurrentDueDate(firstRepayDate,i));
        }


    }

    private String day(String yearMonthStr, Integer repayDate) {
        YearMonth yearMonth = YearMonth.parse(yearMonthStr, DateTimeFormatter.ofPattern("yyyyMM"));
        int currentDay = repayDate;
        int day = yearMonth.lengthOfMonth() > currentDay ? currentDay : yearMonth.lengthOfMonth();
        return String.valueOf(day < 10 ? "0" + day : day);
    }

    private String plusMonths(int monthsToAdd) {
        return YearMonth.now().plusMonths(monthsToAdd).format(DateTimeFormatter.ofPattern("yyyyMM"));
    }
    private String plusMonthsNew(YearMonth yearMonth,int monthsToAdd) {
        return yearMonth.plusMonths(monthsToAdd).format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    private  LocalDate getCurrentDueDate(LocalDate firstRepayDate, int i) {
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

    @Test
    public void testRepay(){
        BigDecimal principalBig = new BigDecimal("1020000");
        Integer terms = 6;
        BigDecimal yearRate = new BigDecimal("0.144");
        BigDecimal dayRate = yearRate.divide(new BigDecimal("360"),6,BigDecimal.ROUND_HALF_UP);
        int year = LocalDate.now().getYear();
        LocalDate loanDate = LocalDate.of(year+1, 2, 24);
        System.out.println(loanDate);
        LocalDate firstRepayDate = LocalDate.of(year+1, 3, 15);
        System.out.println(firstRepayDate);
        System.out.println(calcMonthRepay(principalBig,6,dayRate,loanDate,firstRepayDate));
    }
    @Test
    public void testTime(){
        String createTime = "2017-02-22 11:39:25".substring(0, 10).replace("-", "");
        System.out.println(createTime);
        LocalDate createDate = DateUtil.parseDate(createTime, DateUtil.DATE_TIME_FORMATTER_8);
        System.out.println(createDate);
        String currentRepayDay = "2017-02-26 11:39:25".substring(0, 10).replace("-", "");
        System.out.println(currentRepayDay);
        LocalDate currentDate = DateUtil.parseDate(currentRepayDay, DateUtil.DATE_TIME_FORMATTER_8).plusDays(1);
        System.out.println(currentDate);
        if(createDate.isBefore(currentDate)){
            if(DateUtil.betweenDays(createDate,currentDate) > 5){
                System.out.println("++++++++++++++");
            }
        }
    }

    private  BigDecimal calcMonthRepay(BigDecimal principalBig, Integer terms, BigDecimal dayRate, LocalDate loanDate, LocalDate firstRepayDate) {
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
        return  top.divide(bottom,2,RoundingMode.HALF_UP);
    }

    private  Integer getDaysInTerm(LocalDate loanDate, int currentTerm,LocalDate firstRepayDate) {
        LocalDate lastDueDate = loanDate;
        LocalDate currentDueDate = firstRepayDate;
        if(currentTerm > 0){
            lastDueDate = getCurrentDueDate(firstRepayDate,currentTerm - 1);
            currentDueDate = getCurrentDueDate(firstRepayDate,currentTerm);
        }
        return  (int) DateUtil.betweenDays(lastDueDate,currentDueDate);
    }

}
