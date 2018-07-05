package com.jiuyi.ndr.resource.credit;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiuyi.ndr.constant.CreditConstant;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import com.jiuyi.ndr.domain.subject.*;
import com.jiuyi.ndr.dto.credit.mobile.*;
import com.jiuyi.ndr.dto.iplan.mobile.IPlanAppListDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.rest.page.CreditPageData;
import com.jiuyi.ndr.rest.page.PageData;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.subject.*;
import com.jiuyi.ndr.service.user.ActivityMarkConfigureService;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.PageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mayognbo on 2017/11/2.
 */

@RestController
public class CreditMobileResource {
    @Autowired
    private CreditOpeningService creditOpeningService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private CreditService creditService;

    @Autowired
    private CreditDao creditDao;

    @Autowired
    private SubjectTransferParamService subjectTransferParamService;
    
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;

    @Autowired
    private SubjectAccountService subjectAccountService;

    @Autowired
    private SubjectTransLogService subjectTransLogService;

    @Autowired
    private SubjectRepayDetailService subjectRepayDetailService;

    @Autowired
    private ActivityMarkConfigureService activityMarkConfigureService;

    @Autowired
    private ConfigDao configDao;


    private DecimalFormat df = new DecimalFormat("######0.00");
    private DecimalFormat df2 = new DecimalFormat("######0");
    private static DecimalFormat df3 = new DecimalFormat("0.####");
    private static DecimalFormat df6 = new DecimalFormat("0.###");
    private DecimalFormat df4 = new DecimalFormat("######0.##");
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Value("${duanrong.subject.detailUrl}")
    private String detailUrl;   //债权信息

    @Value("${duanrong.subject.transferUrl}")
    private String transferUrl;//转让协议
    @Value("${duanrong.subject.purchaseRecordUrl}")
    private String purchaseRecordUrl;//加入记录
    @Value("${duanrong.credit.creditIntroduceUrl}")
    private String creditIntroduceUrl;//债转介绍地址

    //app-获取债转列表
    @GetMapping("/app/credit/list")
    public RestResponse getCreditOpeningLists(@RequestParam("pageNo") int pageNo,
                                 @RequestParam("pageSize") int pageSize,
                                 @RequestParam(value = "requestSource", required = false) String requestSource,
                                 @RequestParam(value = "userId", required = false) String userId,
                                 @RequestParam(value = "type", required = false) String type,
                                 @RequestParam(value = "rate", required = false) String rate,
                                 @RequestParam(value = "discount", required = false) String discount,
                                 @RequestParam(value = "term", required = false) String term) {

        if (pageNo <= 0) {
            pageNo = 1;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }
        List<String> rates = Arrays.asList(configDao.getConfigById("credit_rate").getValue().split(","));
        List<String> terms = Arrays.asList(configDao.getConfigById("credit_term").getValue().split(","));
        List<String> discounts = Arrays.asList(configDao.getConfigById("credit_discount").getValue().split(","));
        PageHelper.startPage(pageNo, pageSize);
        List<CreditOpeningDtoNew> allCreditOpeningDto = creditOpeningService.getAllCreditOpening(type, rate,discount,term,rates,discounts,terms,pageNo, pageSize);
        if (allCreditOpeningDto == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        for (CreditOpeningDtoNew creditOpeningDtoNew : allCreditOpeningDto) {

            // 对债转项目名称进行处理
            creditOpeningDtoNew.setName(IPlanAppListDto.dealName(creditOpeningDtoNew.getName()));

            String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
            String endDate = creditOpeningDtoNew.getEndTime().substring(0, 8);
            long days = DateUtil.betweenDays(currentDate, endDate);
            Credit credit = creditDao.findById(creditOpeningDtoNew.getCreditId());
            creditOpeningDtoNew.setTerm(credit.getResidualTerm());
            creditOpeningDtoNew.setResidualDay((int) days);
            creditOpeningDtoNew.setAvailablePrincipal(creditOpeningDtoNew.getAvailablePrincipal() / 100.0);
            if(creditOpeningDtoNew.getAvailablePrincipal() >= 10000.0){
                creditOpeningDtoNew.setAvailablePrincipalStr(df4.format(creditOpeningDtoNew.getAvailablePrincipal() / 10000.0)+"万");
            }else{
                creditOpeningDtoNew.setAvailablePrincipalStr(df4.format(creditOpeningDtoNew.getAvailablePrincipal())+"元");
            }
            creditOpeningDtoNew.setTotalRateStr(df4.format(creditOpeningDtoNew.getTotalRate() * 100)+"%");
            if(StringUtils.isNotBlank(requestSource) && requestSource.contains("ios")){
                int vsersion = Integer.parseInt(requestSource.substring(4).replace(".", ""));
                if(vsersion >= 520){
                    if(creditOpeningDtoNew.getBonusRate() != null && creditOpeningDtoNew.getBonusRate() != 0.0){
                        creditOpeningDtoNew.setTotalRateStr(df4.format(creditOpeningDtoNew.getInvestRate() * 100)+"+"+df4.format((creditOpeningDtoNew.getBonusRate()) * 100)+"%");
                        Subject subject = subjectService.findSubjectBySubjectId(credit.getSubjectId());
                        ActivityMarkConfigure amc = activityMarkConfigureService.findById(subject.getActivityId());
                        if(amc.getIncreaseTerm()!=null){
                            creditOpeningDtoNew.setTotalRateStr(df4.format(creditOpeningDtoNew.getInvestRate() * 100)+"%");
                        }

                    }else{
                        creditOpeningDtoNew.setTotalRateStr(df4.format(creditOpeningDtoNew.getInvestRate() * 100)+"%");
                    }
                }
            }
            if(!StringUtils.isNotBlank(requestSource)){
                if(creditOpeningDtoNew.getBonusRate() != null && creditOpeningDtoNew.getBonusRate() != 0.0){
                    creditOpeningDtoNew.setTotalRateStr(df4.format(creditOpeningDtoNew.getInvestRate() * 100)+"+"+df4.format((creditOpeningDtoNew.getBonusRate()) * 100)+"%");
                }else{
                    creditOpeningDtoNew.setTotalRateStr(df4.format(creditOpeningDtoNew.getInvestRate() * 100)+"%");
                }
            }
            creditOpeningDtoNew.setTransferDiscountStr(df4.format(creditOpeningDtoNew.getTransferDiscount()*100)+"折");
            creditOpeningDtoNew.setEndTime( DateUtil.parseDate(creditOpeningDtoNew.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());
        }

        CreditPageData<CreditOpeningDtoNew> pageData = new CreditPageData<>();
        PageInfo<CreditOpeningDtoNew> pageInfo2 = new PageInfo<>(allCreditOpeningDto);
        pageData.setList(allCreditOpeningDto);
        pageData.setPage(pageInfo2.getPageNum());
        pageData.setSize(pageInfo2.getSize());
        pageData.setTotalPages(pageInfo2.getPages());
        pageData.setTotal(pageInfo2.getTotal());
        pageData.setRates(Arrays.asList("全部",rates.get(0)+"%以下",rates.get(0)+"-"+rates.get(1)+"%",rates.get(1)+"%以上"));
        pageData.setDiscounts(Arrays.asList("全部",discounts.get(0)+"%",discounts.get(0)+"-"+discounts.get(1)+"%",discounts.get(1)+"%以下"));
        pageData.setTerms(Arrays.asList("全部",terms.get(0)+"-"+terms.get(1)+"期",terms.get(2)+"-"+terms.get(3)+"期",terms.get(4)+"期以上"));
        pageData.setIntroduceUrl(creditIntroduceUrl);
        return new RestResponseBuilder<>().success(pageData);
    }

    //app-获取项目详情
    @GetMapping("/app/creditDetail")
    public RestResponse getAppCreditOpeningDetailDto(@RequestParam("id") int id,
                                 @RequestParam(value = "requestSource", required = false) String requestSource,
                                 @RequestParam(value = "userId", required = false) String userId) {

        CreditOpening creditOpening = creditOpeningService.getById(id);
        if (creditOpening == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        Subject subject = subjectService.getBySubjectId(creditOpening.getSubjectId());
        Credit credit = creditService.getById(creditOpening.getCreditId());

        AppCreditOpeningDetailDto appCreditOpeningDetailDto = new AppCreditOpeningDetailDto();

        appCreditOpeningDetailDto.setId(id);
        appCreditOpeningDetailDto.setName(subject.getName());

        //新的年化利率
        //原标的利率
        BigDecimal totalRate = creditOpeningService.calcTotalRate(creditOpening.getSubjectId());
        Double expectRate = creditOpeningService.calcNewRate(creditOpening.getSubjectId(),creditOpening);
        appCreditOpeningDetailDto.setExpectRate(totalRate.doubleValue());
        appCreditOpeningDetailDto.setExpectRateStr(df4.format(appCreditOpeningDetailDto.getExpectRate() * 100)+"%");
        appCreditOpeningDetailDto.setExpectRate(expectRate);
        appCreditOpeningDetailDto.setExpectRateStr(df4.format(expectRate * 100)+"%");

        if(subject.getBonusRate() != null && subject.getBonusRate().doubleValue()!=0.0){
            appCreditOpeningDetailDto.setExpectRateStr(df4.format(subject.getInvestRate().doubleValue() * 100)+"+"+df4.format((subject.getBonusRate().doubleValue()) * 100)+"%");
            ActivityMarkConfigure amc = activityMarkConfigureService.findById(subject.getActivityId());
            if(amc.getIncreaseTerm()!=null){
                appCreditOpeningDetailDto.setExpectRateStr(df4.format(subject.getInvestRate().doubleValue() * 100)+"%");
            }
        }else{
            appCreditOpeningDetailDto.setExpectRateStr(df4.format(subject.getInvestRate().doubleValue() * 100)+"%");
        }

        //剩余时间
        String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
        String endDate = credit.getEndTime().substring(0, 8);
        long days = DateUtil.betweenDays(currentDate, endDate);
        appCreditOpeningDetailDto.setResidualDay((int) days);
        appCreditOpeningDetailDto.setTerm(credit.getResidualTerm());
        appCreditOpeningDetailDto.setAvailablePrincipal(creditOpening.getAvailablePrincipal()/100.0);

        //预期收益
        Double expectInterest = subjectService.getInterestByRepayType(creditOpening.getAvailablePrincipal(),subject.getInvestRate(),subject.getRate(),credit.getResidualTerm(),subject.getPeriod(),subject.getRepayType());
        Double expectBonusInterest = subjectService.getInterestByRepayType(creditOpening.getAvailablePrincipal(),subject.getBonusRate(),subject.getRate(),credit.getResidualTerm(),subject.getPeriod(),subject.getRepayType());
        appCreditOpeningDetailDto.setExpectProfit(expectInterest + expectBonusInterest);
        appCreditOpeningDetailDto.setExpectProfitStr(df4.format(appCreditOpeningDetailDto.getExpectProfit()));

        //原债权价值
        appCreditOpeningDetailDto.setOldValue(subjectRepayScheduleService.calcTotalInterest(creditOpening,subject)+(creditOpening.getAvailablePrincipal()/100.0));
        appCreditOpeningDetailDto.setOldValueStr(df4.format(appCreditOpeningDetailDto.getOldValue()));

        //承接价格
        Double buyPrice = (creditOpening.getAvailablePrincipal()/100.0) * (creditOpening.getTransferDiscount().doubleValue());
        appCreditOpeningDetailDto.setBuyPrice(buyPrice);
        appCreditOpeningDetailDto.setBuyPriceStr(df4.format(buyPrice));

        //折让比例
        appCreditOpeningDetailDto.setDiscount(creditOpening.getTransferDiscount().doubleValue());
        appCreditOpeningDetailDto.setDiscountStr(df4.format(appCreditOpeningDetailDto.getDiscount() * 100)+"折");

        //原年利化率
        appCreditOpeningDetailDto.setOldRate(totalRate.doubleValue());
        appCreditOpeningDetailDto.setOldRateStr(df4.format(totalRate.doubleValue() * 100) + "%");

        //回款方式
        appCreditOpeningDetailDto.setRepayType(subjectService.getRepayType(creditOpening.getSubjectId()));
        //起投金额
        SubjectTransferParam transferParam = subjectTransferParamService.getByTransferParamCode(subject.getTransferParamCode());
        appCreditOpeningDetailDto.setInvestMoney(transferParam.getPurchasingPriceMin()/100.0);
        appCreditOpeningDetailDto.setInvestMoneyStr(df4.format(transferParam.getPurchasingPriceMin()/100.0));

        //项目结束时间
        appCreditOpeningDetailDto.setEndTime(DateUtil.parseDate(credit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());

        //下次回款时间
        SubjectRepaySchedule currentRepaySchedule = subjectRepayScheduleService.getCurrentRepaySchedule(subject.getSubjectId());
        appCreditOpeningDetailDto.setNextRepayTime(DateUtil.parseDate(currentRepaySchedule.getDueDate(), DateUtil.DATE_TIME_FORMATTER_8).toString());

        //购买记录
        appCreditOpeningDetailDto.setTransLogId(credit.getSourceChannelId());

        //债权信息
        appCreditOpeningDetailDto.setCreditId(creditOpening.getCreditId());
        appCreditOpeningDetailDto.setRemark("当日计息");
        appCreditOpeningDetailDto.setWeight(df6.format(creditOpening.getAvailablePrincipal()/(creditOpening.getTransferPrincipal()*1.0)));
        //债转信息
        appCreditOpeningDetailDto.setCreditDetailUrl(detailUrl+"?subjectId="+subject.getSubjectId());
        //加入记录
        appCreditOpeningDetailDto.setPurchaseRecordUrl(purchaseRecordUrl+"?id="+creditOpening.getId());
        appCreditOpeningDetailDto.setInstructions(GlobalConfig.CREDIT_INSTRUCTIONS);
        appCreditOpeningDetailDto.setSubjectId(subject.getSubjectId());
        return new RestResponseBuilder<>().success(appCreditOpeningDetailDto);
    }

    //app 持有中 转让中 已完成
    @GetMapping("app/credit/manage")
    public RestResponse creditInvestManage(@RequestParam("userId") String userId,
                                          @RequestParam("type") int type,
                                          @RequestParam(value = "pageNo", defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        if (type == AppCreditManageHoldDto.PAGE_TYPE_HOLDING) {

            AppCreditManageHoldDto appCreditManageHoldDto = new AppCreditManageHoldDto();
            List<AppCreditManageHoldDto.Detail> details = new ArrayList<>();

            //总持有金额
            Double amount = 0.0;
            //预期收益
            Double profit = 0.0;

            List<SubjectAccount> subjectAccounts = new ArrayList<>();
            List<SubjectAccount> allSubjectAccounts =  subjectAccountService.getByUserIdAndStatusCreditNoPage(userId, SubjectAccount.STATUS_PROCEEDS,SubjectAccount.SOURCE_CREDIT);
            if (allSubjectAccounts != null && allSubjectAccounts.size() > 0){
                for (SubjectAccount subjectAccount : allSubjectAccounts) {
                    amount += (subjectAccount.getCurrentPrincipal()) / 100.0;
                    profit += (subjectAccount.getExpectedInterest() + subjectAccount.getSubjectExpectedBonusInterest()) / 100.0;
                }
                subjectAccounts =  subjectAccountService.getByUserIdAndStatusCredit(userId, SubjectAccount.STATUS_PROCEEDS,SubjectAccount.SOURCE_CREDIT, pageNum, pageSize);
            }
            //持有中
            if(subjectAccounts.size() > 0){
                for (SubjectAccount subjectAccount : subjectAccounts) {
                    AppCreditManageHoldDto.Detail detail = new AppCreditManageHoldDto.Detail();
                    detail.setId(subjectAccount.getId());
                    Subject subject = subjectService.getBySubjectId(subjectAccount.getSubjectId());
                    detail.setName(subject.getName());
                    detail.setRepayType(subjectService.getRepayType(subjectAccount.getSubjectId()));
                    SubjectTransLog subjectTransLog = subjectTransLogService.getById(subjectAccount.getTransLogId());
                    detail.setBuyTime(subjectTransLog.getCreateTime().substring(0,10));
                    detail.setStatus(CreditConstant.JOINING);
                    detail.setEndTime("生成中");
                    detail.setHoldingAmt((subjectAccount.getCurrentPrincipal()) / 100.0);
                    detail.setHoldingAmtStr(df4.format(detail.getHoldingAmt()));
                    detail.setExpectAmt((subjectAccount.getExpectedInterest() +subjectAccount.getSubjectExpectedBonusInterest())/100.0);
                    detail.setExpectAmtStr(df4.format(detail.getExpectAmt()));
                    Credit credit = creditService.getBySubjectAccountId(subjectAccount.getId());
                    if(credit != null && (Credit.CREDIT_STATUS_HOLDING == credit.getCreditStatus())){
                        detail.setStatus(CreditConstant.REPAYING);
                        detail.setEndTime(DateUtil.parseDate(credit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());
                    }
                    details.add(detail);
                }
            }
                appCreditManageHoldDto.setAmount(amount);
                appCreditManageHoldDto.setAmountStr(df4.format(amount));
                appCreditManageHoldDto.setProfit(profit);
                appCreditManageHoldDto.setProfitStr(df4.format(profit));
                appCreditManageHoldDto.setPageType(AppCreditManageHoldDto.PAGE_TYPE_HOLDING);
                appCreditManageHoldDto.setDetails(details);
            return new RestResponseBuilder<>().success(appCreditManageHoldDto);
        }else if(type == AppCreditManageHoldDto.PAGE_TYPE_TRANSFERRING){
            AppCreditManageTransferDto appCreditManageTransferDto = new AppCreditManageTransferDto();
            List<AppCreditManageTransferDto.Detail> details = new ArrayList<>();

            //总转让金额
            Double amount = 0.0;
            //已成交金额
            Double finishAmt = 0.0;

            //单个转让金额
            Double holdingAmt = 0.0;

            //单个成交金额
            Double transferAmt = 0.0;

            //转让中
            List<CreditOpening> creditOpenings = creditOpeningService.getByUserIdAndStatusAndOpenChannel(userId,CreditOpening.OPEN_CHANNEL);
            List<CreditOpening> newCreditOpenings = new ArrayList<>();
            List<CreditOpening> allCreditOpenings = new ArrayList<>();
            if(creditOpenings != null && creditOpenings.size() > 0){
                allCreditOpenings = creditOpeningService.sortByConditionNoPage(creditOpenings,Credit.TARGET_CREDIT);
                if(allCreditOpenings.size() > 0){
                    for (CreditOpening creditOpening : allCreditOpenings) {
                        amount += creditOpening.getTransferPrincipal() / 100.0;
                        transferAmt = (creditOpening.getTransferPrincipal() - creditOpening.getAvailablePrincipal()) / 100.0;
                        if(CreditOpening.STATUS_CANCEL_PENDING.equals(creditOpening.getStatus())){
                            SubjectTransLog subjectTransLog = subjectTransLogService.getById(creditOpening.getSourceChannelId());
                            transferAmt= subjectTransLog.getProcessedAmt() / 100.0;
                        }
                        finishAmt += transferAmt;
                    }
                }
                newCreditOpenings = creditOpeningService.sortByCondition(creditOpenings,Credit.TARGET_CREDIT,pageNum,pageSize);
            }
            if (newCreditOpenings.size() > 0) {
                for (CreditOpening creditOpening : newCreditOpenings) {
                    Credit credit = creditService.getById(creditOpening.getCreditId());
                    Subject subject = subjectService.getBySubjectId(creditOpening.getSubjectId());
                    AppCreditManageTransferDto.Detail detail = new AppCreditManageTransferDto.Detail();
                    detail.setId(creditOpening.getId());
                    detail.setName(subject.getName());
                    detail.setStatus(CreditConstant.TRANSFERING);
                    detail.setRepayType(subjectService.getRepayType(credit.getSubjectId()));
                    detail.setBuyTime(DateUtil.parseDate(credit.getCreateTime(), DateUtil.DATE_TIME_FORMATTER_19).toString());
                    //持有金额
                    holdingAmt = creditOpening.getTransferPrincipal() / 100.0;
                    detail.setHoldingAmt(holdingAmt);
                    detail.setHoldingAmtStr(df4.format(detail.getHoldingAmt()));
                    //成交金额
                    transferAmt = (creditOpening.getTransferPrincipal() - creditOpening.getAvailablePrincipal()) / 100.0;
                    if(CreditOpening.STATUS_CANCEL_PENDING.equals(creditOpening.getStatus())){
                        SubjectTransLog subjectTransLog = subjectTransLogService.getById(creditOpening.getSourceChannelId());
                        transferAmt= subjectTransLog.getProcessedAmt() / 100.0;
                    }
                    detail.setTransferAmt(transferAmt);
                    detail.setTransferAmtStr(df4.format(detail.getTransferAmt()));

                    //剩余时间
                    String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
                    String endDate = credit.getEndTime().substring(0, 8);
                    long days = DateUtil.betweenDays(currentDate, endDate);
                    detail.setResidualDay((int) days);
                    details.add(detail);
                }
            }
            appCreditManageTransferDto.setAmount(amount);
            appCreditManageTransferDto.setAmountStr(df4.format(amount));
            appCreditManageTransferDto.setFinishAmt(finishAmt);
            appCreditManageTransferDto.setFinishAmtStr(df4.format(finishAmt));
            appCreditManageTransferDto.setPageType(AppCreditManageHoldDto.PAGE_TYPE_TRANSFERRING);
            appCreditManageTransferDto.setDetails(details);
            return new RestResponseBuilder<>().success(appCreditManageTransferDto);
        } if(type == AppCreditManageHoldDto.PAGE_TYPE_FINISH){
            AppCreditManageFinishDto appCreditManageFinishDto = new AppCreditManageFinishDto();
            List<AppCreditManageFinishDto.Detail> details = new ArrayList<>();

            //总转让金额
            Double amount = 0.0;
            //实际到账金额
            Double totalActualAmt = 0.0;

            //正常还款已完成
            List<Credit> credits = creditService.getCreditFinishByUserId(userId,Credit.TARGET_CREDIT,Credit.CREDIT_STATUS_FINISH);
            if(credits != null && credits.size() > 0){
                for (Credit credit : credits) {
                    AppCreditManageFinishDto.Detail detail = new AppCreditManageFinishDto.Detail();
                    //账户
                    SubjectAccount subjectAccount = subjectAccountService.findAccountById(credit.getSourceAccountId());
                    detail.setId(subjectAccount.getId());
                    //标的
                    Subject subject = subjectService.getBySubjectId(credit.getSubjectId());
                    detail.setName(subject.getName());
                    //利率
                    BigDecimal totalRate = creditOpeningService.calcTotalRate(credit.getSubjectId());
                    //CreditOpening creditOpening = creditOpeningService.getById(credit.getTargetId());
                    //Double newRate = totalRate.divide(creditOpening.getTransferDiscount(), 2, RoundingMode.HALF_UP).doubleValue();
                    detail.setRate(totalRate.doubleValue());
                    detail.setRateStr(df4.format(detail.getRate() * 100));
                    //还款类型
                    detail.setRepayType(subjectService.getRepayType(credit.getSubjectId()));
                    //完成方式
                    detail.setType(CreditConstant.REPAY_FINISH);
                    //本金
                    Integer principal = subjectRepayDetailService.getPrincipal(credit.getSubjectId(), credit.getUserId(), credit.getSourceAccountId());
                    //利息
                    Integer interest = subjectRepayDetailService.getInterest(credit.getSubjectId(), credit.getUserId(), credit.getSourceAccountId());
                    //持有金额
                    detail.setHoldingAmt(principal/100.0);
                    detail.setHoldingAmtStr(df4.format(detail.getHoldingAmt()));
                    //回款金额
                    detail.setTransferAmt((principal + interest) / 100.0);
                    detail.setTransferAmtStr(df4.format(detail.getTransferAmt()));
                    amount += detail.getHoldingAmt();
                    //实际到账金额
                    detail.setActualAmt(detail.getTransferAmt());
                    detail.setActualAmtStr(df4.format(detail.getTransferAmt()));
                    totalActualAmt += detail.getActualAmt();
                    if((principal+interest)>0){
                        details.add(detail);
                    }
                }
            }
            //转让完成
            List<SubjectTransLog> subjectTransLogs = subjectTransLogService.getSubjectTransLogByUserIdAndStatus(userId);
            List<SubjectTransLog> logs = new ArrayList<>();
            if(subjectTransLogs != null && subjectTransLogs.size() > 0){
                logs = subjectTransLogService.sortBySource(subjectTransLogs,SubjectAccount.SOURCE_CREDIT);
            }
            if(logs != null && logs.size() > 0){
                for (SubjectTransLog subjectTransLog : logs) {
                    AppCreditManageFinishDto.Detail detail = new AppCreditManageFinishDto.Detail();
                    SubjectAccount subjectAccount = subjectAccountService.findAccountById(subjectTransLog.getAccountId());
                    //transLogId
                    detail.setId(subjectTransLog.getId());
                    //利率
                    Credit credit = creditService.getBySubjectAccountId(subjectAccount.getId());
                    CreditOpening creditOpening = creditOpeningService.getById(credit.getTargetId());
                    //Double newRate = creditOpeningService.calcNewRate(credit.getSubjectId(),creditOpening);
                    BigDecimal totalRate = creditOpeningService.calcTotalRate(creditOpening.getSubjectId());
                    detail.setRate(totalRate.doubleValue());
                    detail.setRateStr(df4.format(detail.getRate() * 100));
                    //项目名称
                    Subject subject = subjectService.getBySubjectId(subjectTransLog.getSubjectId());
                    detail.setName(subject.getName());
                    //还款类型
                    detail.setRepayType(subjectService.getRepayType(subjectTransLog.getSubjectId()));
                    //完成方式
                    detail.setType(CreditConstant.TRANSFER_FINISH);
                    //购买金额
                    detail.setHoldingAmt(subjectAccount.getInitPrincipal() / 100.0);
                    detail.setHoldingAmtStr(df4.format(detail.getHoldingAmt()));
                    //转让金额
                    detail.setTransferAmt(subjectTransLog.getProcessedAmt()/ 100.0);
                    detail.setTransferAmtStr(df4.format(detail.getTransferAmt()));
                    amount += detail.getTransferAmt();
                    //实际到账金额
                    detail.setActualAmt(subjectTransLog.getActualPrincipal() / 100.0);
                    detail.setActualAmtStr(df4.format(detail.getActualAmt()));
                    totalActualAmt += detail.getActualAmt();
                    details.add(detail);
                }
            }
            List<AppCreditManageFinishDto.Detail> list = new PageUtil().ListSplit(details, pageNum, pageSize);
            appCreditManageFinishDto.setAmount(amount);
            appCreditManageFinishDto.setAmountStr(df4.format(amount));
            appCreditManageFinishDto.setTotalActualAmt(totalActualAmt);
            appCreditManageFinishDto.setTotalActualAmtStr(df4.format(totalActualAmt));
            appCreditManageFinishDto.setPageType(AppCreditManageHoldDto.PAGE_TYPE_FINISH);
            appCreditManageFinishDto.setDetails(list);
            return new RestResponseBuilder<>().success(appCreditManageFinishDto);
        } else {
            throw new IllegalArgumentException("参数type不支持！");
        }
    }

    @GetMapping("/app/credit/manage/creditHoldDetail")
    public RestResponse getAppCreditHoldDetailDto(@RequestParam("id") int subjectAccountId,
                                                     @RequestParam(value = "userId") String userId) {

        SubjectAccount subjectAccount = subjectAccountService.findAccountById(subjectAccountId);
        if (subjectAccount == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        Credit credit = creditService.getBySubjectAccountId(subjectAccount.getId());
        Subject subject = subjectService.getBySubjectId(subjectAccount.getSubjectId());
        SubjectTransLog subjectTransLog = subjectTransLogService.getTransLogByAccountId(subjectAccountId);
        AppCreditHoldDetailDto appCreditHoldDetailDto = new AppCreditHoldDetailDto();
        appCreditHoldDetailDto.setId(subjectAccount.getId());
        appCreditHoldDetailDto.setName(subject.getName());
        //原标的利率
        BigDecimal totalRate = creditOpeningService.calcTotalRate(subjectAccount.getSubjectId());
        ActivityMarkConfigure activityMarkConfigure = null;
        if (subject.getActivityId() != null && subject.getActivityId() > 0) {
            activityMarkConfigure = activityMarkConfigureService.findById(subject.getActivityId());
            if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null) {
                totalRate = subject.getInvestRate();
            }
        }
        CreditOpening creditOpening = creditOpeningService.getById(subjectTransLog.getTargetId());
        //Double newRate = creditOpeningService.calcNewRate(subjectAccount.getSubjectId(),creditOpening);
        appCreditHoldDetailDto.setRate(totalRate.doubleValue());
        appCreditHoldDetailDto.setRateStr(df4.format(appCreditHoldDetailDto.getRate() * 100));
        Credit oldCredit = creditService.getById(creditOpening.getCreditId());

        //还款方式
        appCreditHoldDetailDto.setRepayType(subjectService.getRepayType(subjectAccount.getSubjectId()));

        //剩余时间
        String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
        String endDate = oldCredit.getEndTime().substring(0, 8);
        long days = DateUtil.betweenDays(currentDate, endDate);
        appCreditHoldDetailDto.setResidualDay((int) days);

        //预期收益
        Double expectProfit = (subjectAccount.getExpectedInterest() +subjectAccount.getSubjectExpectedBonusInterest())/100.0;
        appCreditHoldDetailDto.setExpectProfit(expectProfit);
        appCreditHoldDetailDto.setExpectProfitStr(df4.format(appCreditHoldDetailDto.getExpectProfit()));

        //投资金额
        appCreditHoldDetailDto.setHoldingPrincipal(subjectAccount.getCurrentPrincipal() / 100.0);
        appCreditHoldDetailDto.setHoldingPrincipalStr(df4.format(subjectAccount.getCurrentPrincipal() / 100.0));

        //投资时间
        appCreditHoldDetailDto.setBuyTime(DateUtil.parseDate(subjectTransLog.getCreateTime(), DateUtil.DATE_TIME_FORMATTER_19).toString());
        appCreditHoldDetailDto.setEndTime(DateUtil.parseDate(oldCredit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());

        appCreditHoldDetailDto.setCreditDetailUrl(detailUrl+"?subjectId="+subject.getSubjectId());
        appCreditHoldDetailDto.setCreditTransferUrl(transferUrl);
        appCreditHoldDetailDto.setSubjectId(subjectAccount.getSubjectId());
        if(credit != null){
            String contractId = credit.getContractId();
            if(StringUtils.isNotBlank(contractId)){
                appCreditHoldDetailDto.setCreditTransferUrl(creditService.getContractViewPdfUrlByContractId(contractId));
            }
        }

        //是否可转让
        appCreditHoldDetailDto.setStatus(subjectService.checkCondition(subject,subjectAccount));

        appCreditHoldDetailDto.setMessage(subjectService.checkConditionStr(subject,subjectAccount));

        return new RestResponseBuilder<>().success(appCreditHoldDetailDto);
    }


    @GetMapping("/app/credit/manage/creditTransferDetail")
    public RestResponse getAppCreditTransferDetailDto(@RequestParam("id") int creditOpeningId,
                                                  @RequestParam(value = "userId") String userId) {

        CreditOpening creditOpening = creditOpeningService.getById(creditOpeningId);
        if (creditOpening == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        AppCreditTransferDetailDto appCreditTransferDetailDto = new AppCreditTransferDetailDto();
        appCreditTransferDetailDto.setId(creditOpening.getId());
        Subject subject = subjectService.getBySubjectId(creditOpening.getSubjectId());
        appCreditTransferDetailDto.setName(subject.getName());
        //还款方式
        appCreditTransferDetailDto.setRepayType(subjectService.getRepayType(creditOpening.getSubjectId()));
        //出售金额
        appCreditTransferDetailDto.setSaleAmt(creditOpening.getTransferPrincipal() / 100.0);
        appCreditTransferDetailDto.setSaleAmtStr(df4.format(appCreditTransferDetailDto.getSaleAmt()));

        //折让率
        appCreditTransferDetailDto.setTransDiscount(creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue());
        appCreditTransferDetailDto.setTransDiscountStr(df4.format(appCreditTransferDetailDto.getTransDiscount()));

        //投资金额
        Credit credit = creditService.getById(creditOpening.getCreditId());
        appCreditTransferDetailDto.setInvestAmt(credit.getInitPrincipal() / 100.0);
        appCreditTransferDetailDto.setInvestAmtStr(df4.format(appCreditTransferDetailDto.getInvestAmt()));

        //投资日期
        appCreditTransferDetailDto.setInvestTime(DateUtil.parseDate(credit.getCreateTime(), DateUtil.DATE_TIME_FORMATTER_19).toString());
        appCreditTransferDetailDto.setEndTime(DateUtil.parseDate(credit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());

        //持有天数
        String startDate = credit.getCreateTime().substring(0, 10).replace("-","");
        String currentDate = DateUtil.getCurrentDate().substring(0, 10).replace("-","");
        long days = DateUtil.betweenDays(startDate,currentDate);
        appCreditTransferDetailDto.setHoldDay((int) days);

       //已到账收益
        SubjectAccount subjectAccount = subjectAccountService.findAccountById(credit.getSourceAccountId());
        appCreditTransferDetailDto.setReceivedAmt(subjectAccount.getPaidInterest() / 100.0);
        appCreditTransferDetailDto.setReceivedAmtStr(df4.format(appCreditTransferDetailDto.getReceivedAmt()));

        //转让时间
        appCreditTransferDetailDto.setTransferTime(creditOpening.getCreateTime().substring(0,10));

        //散标交易配置信息
        SubjectTransferParam subjectTransferParam = subjectTransferParamService.getByTransferParamCode(subject.getTransferParamCode());

        //转让服务费
        SubjectTransLog subjectTransLog = subjectTransLogService.getById(creditOpening.getSourceChannelId());
        if(subjectTransLog.getTransFee() == 0){
            Double feeRate = subjectAccountService.calcTransFeeNew(subjectAccount.getTransLogId(), subject, subjectTransferParam);
            appCreditTransferDetailDto.setFee((creditOpening.getTransferPrincipal()/100.0) * feeRate / 100.0);
        }else{
            appCreditTransferDetailDto.setFee(0.0);
        }
            appCreditTransferDetailDto.setFeeStr(df4.format(appCreditTransferDetailDto.getFee()));


        //扣除红包奖励
        Double redFee = subjectAccountService.calcRedFee(subjectAccount, credit);
        appCreditTransferDetailDto.setRedFee((creditOpening.getTransferPrincipal() /100.0)   * redFee);
        appCreditTransferDetailDto.setRedFeeStr(df4.format(appCreditTransferDetailDto.getRedFee() ));

        //溢价手续费
        Double overFee = 0.0;
        Double transferDiscount = creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue();
        if(transferDiscount > 100){
            overFee = (creditOpening.getTransferPrincipal() /100.0) * (transferDiscount - 100) / 100.0 * 0.2;
        }
        appCreditTransferDetailDto.setOverFee(overFee);
        appCreditTransferDetailDto.setOverFeeStr(df4.format(overFee));

        //预计到账金额
        Double expectAmt = ArithUtil.calcExp((creditOpening.getTransferPrincipal() /100.0) * (transferDiscount / 100.0),appCreditTransferDetailDto.getRedFee(),appCreditTransferDetailDto.getOverFee(),appCreditTransferDetailDto.getFee());
        appCreditTransferDetailDto.setExpectAmt(expectAmt);
        appCreditTransferDetailDto.setExpectAmtStr(df4.format(appCreditTransferDetailDto.getExpectAmt()));


        //是否可撤销
        Integer status = 1;
        if(creditOpening.getAvailablePrincipal() == 0 || CreditOpening.STATUS_CANCEL_PENDING.equals(creditOpening.getStatus())){
            status = 0;
        }
        appCreditTransferDetailDto.setStatus(status);

        appCreditTransferDetailDto.setCreditTransferUrl(transferUrl);
        appCreditTransferDetailDto.setCreditDetailUrl(detailUrl+"?subjectId="+subject.getSubjectId());

        return new RestResponseBuilder<>().success(appCreditTransferDetailDto);
    }

    @GetMapping("/app/credit/manage/creditFinishDetail")
    public RestResponse getAppCreditFinishDetailDto(@RequestParam("id") int id,
                                                    @RequestParam("type") String  type,
                                                  @RequestParam(value = "userId") String userId) {
        if (CreditConstant.REPAY_FINISH.equals(type)){
                AppCreditFinishDetailDto appCreditFinishDetailDto = new AppCreditFinishDetailDto();
                SubjectAccount subjectAccount = subjectAccountService.findAccountById(id);
                if (subjectAccount == null) {
                    throw new ProcessException(Error.NDR_0202);
                }
                Credit credit = creditService.getBySubjectAccountId(subjectAccount.getId());
                Subject subject = subjectService.getBySubjectId(credit.getSubjectId());
                appCreditFinishDetailDto.setId(subjectAccount.getId());
                appCreditFinishDetailDto.setName(subject.getName());
                //原标的利率
                BigDecimal totalRate = creditOpeningService.calcTotalRate(credit.getSubjectId());
                //CreditOpening creditOpening = creditOpeningService.getById(credit.getTargetId());
                //Double newRate = totalRate.divide(creditOpening.getTransferDiscount(), 4, RoundingMode.HALF_UP).doubleValue();
                ActivityMarkConfigure activityMarkConfigure = null;
                if (subject.getActivityId() != null && subject.getActivityId() > 0) {
                    activityMarkConfigure = activityMarkConfigureService.findById(subject.getActivityId());
                    if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null) {
                        totalRate = subject.getInvestRate();
                    }
                }
                appCreditFinishDetailDto.setRate(totalRate.doubleValue());
                appCreditFinishDetailDto.setRateStr(df4.format(appCreditFinishDetailDto.getRate() * 100));

                //还款方式
                appCreditFinishDetailDto.setRepayType(subjectService.getRepayType(credit.getSubjectId()));

                //本金
                Integer principal = subjectRepayDetailService.getPrincipal(credit.getSubjectId(), credit.getUserId(), credit.getSourceAccountId());
                //利息
                Integer interest = subjectRepayDetailService.getInterest(credit.getSubjectId(), credit.getUserId(), credit.getSourceAccountId());
                //持有金额
                appCreditFinishDetailDto.setHoldingPrincipal(principal / 100.0);
                appCreditFinishDetailDto.setHoldingPrincipalStr(df4.format(appCreditFinishDetailDto.getHoldingPrincipal()));


                //已到账金额
                appCreditFinishDetailDto.setReceivedAmt((principal + interest) / 100.0);
                appCreditFinishDetailDto.setReceivedAmtStr(df4.format(appCreditFinishDetailDto.getReceivedAmt()));


                //投资时间
                appCreditFinishDetailDto.setBuyTime(DateUtil.parseDate(credit.getCreateTime(), DateUtil.DATE_TIME_FORMATTER_19).toString());
                appCreditFinishDetailDto.setEndTime(DateUtil.parseDate(credit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());

                //持有天数
                String startDate = credit.getCreateTime().substring(0, 10).replace("-","");
                String currentDate = DateUtil.getCurrentDate().substring(0, 10).replace("-","");
                long days = DateUtil.betweenDays(startDate,currentDate);
                appCreditFinishDetailDto.setHoldDay((int) days);

                //到账利息
                appCreditFinishDetailDto.setInterest(interest /100.0);
                appCreditFinishDetailDto.setInterestStr(df4.format(appCreditFinishDetailDto.getInterest()));

                appCreditFinishDetailDto.setCreditTransferUrl(transferUrl);
                String contractId = credit.getContractId();
                if(StringUtils.isNotBlank(contractId)){
                    appCreditFinishDetailDto.setCreditTransferUrl(creditService.getContractViewPdfUrlByContractId(contractId));
                }
                appCreditFinishDetailDto.setPageType(AppCreditFinishDetailDto.PAGE_TYPE_REPAYFINISH);

                appCreditFinishDetailDto.setStatus(CreditConstant.REPAYFINSH);
                return new RestResponseBuilder<>().success(appCreditFinishDetailDto);
            }else {
            AppCreditTransferFinishDetailDto appCreditTransferFinishDetailDto = new AppCreditTransferFinishDetailDto();
            SubjectTransLog subjectTransLog = subjectTransLogService.getById(id);
            if (subjectTransLog == null) {
                throw new ProcessException(Error.NDR_0202);
            }
            CreditOpening creditOpening = creditOpeningService.getBySourceChannelId(subjectTransLog.getId(), CreditOpening.SOURCE_CHANNEL_SUBJECT);
            appCreditTransferFinishDetailDto.setId(creditOpening.getId());
            Subject subject = subjectService.getBySubjectId(subjectTransLog.getSubjectId());
            appCreditTransferFinishDetailDto.setName(subject.getName());
            appCreditTransferFinishDetailDto.setRepayType(subjectService.getRepayType(subjectTransLog.getSubjectId()));

            //年化利率
            BigDecimal totalRate = creditOpeningService.calcTotalRate(creditOpening.getSubjectId());
            ActivityMarkConfigure activityMarkConfigure = null;
            if (subject.getActivityId() != null && subject.getActivityId() > 0) {
                activityMarkConfigure = activityMarkConfigureService.findById(subject.getActivityId());
                if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null) {
                    totalRate = subject.getInvestRate();
                }
            }
            //Double newRate = creditOpeningService.calcNewRate(creditOpening.getSubjectId(),creditOpening);
            appCreditTransferFinishDetailDto.setRate(totalRate.doubleValue());
            appCreditTransferFinishDetailDto.setRateStr(df4.format(appCreditTransferFinishDetailDto.getRate() * 100));

            //持有金额
            SubjectAccount subjectAccount = subjectAccountService.findAccountById(subjectTransLog.getAccountId());
            appCreditTransferFinishDetailDto.setHoldingAmt(subjectAccount.getInitPrincipal() / 100.0);
            appCreditTransferFinishDetailDto.setHoldingAmtStr(df4.format(appCreditTransferFinishDetailDto.getHoldingAmt()));

            //已到账金额
            appCreditTransferFinishDetailDto.setReceivedAmt(subjectTransLog.getActualPrincipal() / 100.0);
            appCreditTransferFinishDetailDto.setReceivedAmtStr(df4.format(appCreditTransferFinishDetailDto.getReceivedAmt()));
            //转让发起时间
            appCreditTransferFinishDetailDto.setTransferTime(subjectTransLog.getCreateTime().substring(0,10));
            //转让金额
            appCreditTransferFinishDetailDto.setTransferAmt(subjectTransLog.getTransAmt() / 100.0);
            appCreditTransferFinishDetailDto.setTransferAmtStr(df4.format(appCreditTransferFinishDetailDto.getTransferAmt()));
            //已成交金额
            appCreditTransferFinishDetailDto.setProcessedAmt(subjectTransLog.getProcessedAmt() / 100.0);
            appCreditTransferFinishDetailDto.setProcessedAmtStr(df4.format(appCreditTransferFinishDetailDto.getProcessedAmt()));
            //债权取消金额
            appCreditTransferFinishDetailDto.setCancelAmt(appCreditTransferFinishDetailDto.getTransferAmt() - appCreditTransferFinishDetailDto.getProcessedAmt());
            appCreditTransferFinishDetailDto.setCancelAmtStr(df4.format(appCreditTransferFinishDetailDto.getCancelAmt()));

            //折让率
            appCreditTransferFinishDetailDto.setTransDiscount(creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue());
            appCreditTransferFinishDetailDto.setTransDiscountStr(df4.format(appCreditTransferFinishDetailDto.getTransDiscount()));

            //散标交易配置信息
            SubjectTransferParam subjectTransferParam = subjectTransferParamService.getByTransferParamCode(subject.getTransferParamCode());

            //转让服务费
            if(subjectTransLog.getTransFee() == 0){
                Double feeRate = subjectAccountService.calcTransFeeFinish(subjectAccount.getTransLogId(), subject, subjectTransferParam,id);
                appCreditTransferFinishDetailDto.setFee((subjectTransLog.getProcessedAmt()/100.0) * feeRate / 100.0);
            }else{
                appCreditTransferFinishDetailDto.setFee(0.0);
            }
            appCreditTransferFinishDetailDto.setFeeStr(df4.format(appCreditTransferFinishDetailDto.getFee()));

            //扣除红包奖励
            Credit credit = creditService.getById(creditOpening.getCreditId());
            Double redFee = subjectAccountService.calcRedFeeFinish(subjectAccount, credit,id);
            appCreditTransferFinishDetailDto.setRedFee((subjectTransLog.getProcessedAmt() /100.0)   * redFee);
            appCreditTransferFinishDetailDto.setRedFeeStr(df4.format(appCreditTransferFinishDetailDto.getRedFee() ));

            //溢价手续费
            Double overFee = 0.0;
            Double transferDiscount = creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue();
            if(transferDiscount > 100){
                overFee = (subjectTransLog.getProcessedAmt() /100.0) * (transferDiscount - 100) / 100.0 * 0.2;
            }
            appCreditTransferFinishDetailDto.setOverFee(overFee);
            appCreditTransferFinishDetailDto.setOverFeeStr(df4.format(overFee));

            appCreditTransferFinishDetailDto.setCreditTransferUrl(transferUrl);
            String contractId = credit.getContractId();
            if(StringUtils.isNotBlank(contractId)){
                appCreditTransferFinishDetailDto.setCreditTransferUrl(creditService.getContractViewPdfUrlByContractId(contractId));
            }
            appCreditTransferFinishDetailDto.setPageType(AppCreditFinishDetailDto.PAGE_TYPE_TRANSFERRFINISH);
            return new RestResponseBuilder<>().success(appCreditTransferFinishDetailDto);
        }
    }
}
