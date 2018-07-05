package com.jiuyi.ndr.resource.credit.pc;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiuyi.ndr.constant.CreditConstant;
import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditCondition;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectAccount;
import com.jiuyi.ndr.domain.subject.SubjectTransLog;
import com.jiuyi.ndr.domain.subject.SubjectTransferParam;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.dto.credit.*;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditFinishDetailDto;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditManageFinishDto;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditTransferFinishDetailDto;
import com.jiuyi.ndr.dto.subject.SubjectTransLogDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.rest.page.CreditPageData;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.subject.*;
import com.jiuyi.ndr.service.user.ActivityMarkConfigureService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.service.user.UserService;
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
import java.util.*;

/**
 * Created by mayognbo on 2017/11/2.
 */

@RestController
public class CreditPcResource {

    @Autowired
    private CreditOpeningService creditOpeningService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private CreditService creditService;

    @Autowired
    private SubjectTransferParamService subjectTransferParamService;

    @Autowired
    private UserService userService;

    @Autowired
    private SubjectTransLogService subjectTransLogService;

    @Autowired
    private SubjectAccountService subjectAccountService;

    @Autowired
    private RedPacketService redPacketService;

    @Autowired
    private SubjectRepayDetailService subjectRepayDetailService;

    @Autowired
    private ActivityMarkConfigureService activityMarkConfigureService;

    @Autowired
    private ConfigDao configDao;


    private DecimalFormat df = new DecimalFormat("######0.00");
    private DecimalFormat df2 = new DecimalFormat("######0");
    private static DecimalFormat df3 = new DecimalFormat("0.####");
    private DecimalFormat df4 = new DecimalFormat("######0.##");
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Value("${duanrong.subject.detailUrl}")
    private String detailUrl;   //债权信息
    @Value("${duanrong.subject.transferUrl}")
    private String transferUrl;//转让协议

    //pc-获取债转列表
    @GetMapping("/pc/credit/list")
    public RestResponse getCreditOpeningLists(CreditCondition creditCondition) {

        if (creditCondition.getPageNo() <= 0) {
            creditCondition.setPageNo(1);
        }
        if (creditCondition.getPageSize() <= 0) {
            creditCondition.setPageSize(10);
        }
        PageHelper.startPage(creditCondition.getPageNo(), creditCondition.getPageSize());
        List<CreditOpeningDtoPc> allCreditOpeningDto = creditOpeningService.getList(creditCondition, creditCondition.getPageNo(), creditCondition.getPageSize());
        CreditPageData<CreditOpeningDtoPc> pageData = new CreditPageData<>();
        PageInfo<CreditOpeningDtoPc> pageInfo2 = new PageInfo<>(allCreditOpeningDto);
        pageData.setList(allCreditOpeningDto);
        pageData.setPage(pageInfo2.getPageNum());
        pageData.setSize(pageInfo2.getSize());
        pageData.setTotalPages(pageInfo2.getPages());
        pageData.setTotal(pageInfo2.getTotal());
        pageData.setRates(Arrays.asList(configDao.getConfigById("credit_rate").getValue().split(",")));
        pageData.setTerms(Arrays.asList(configDao.getConfigById("credit_term").getValue().split(",")));
        return new RestResponseBuilder<>().success(pageData);
    }

    //pc-获取项目详情
    @GetMapping("/pc/creditDetail")
    public RestResponse getCreditOpeningDetailDto(@RequestParam("id") int id,
                                                  @RequestParam(value = "userId", required = false) String userId) {

        CreditOpeningDetailDto creditOpeningDetailDto = creditOpeningService.getCreditOpeningDetail(id, userId);
        return new RestResponseBuilder<>().success(creditOpeningDetailDto);
    }

    //pc wap 购买页面
    @GetMapping("/pc/buyPage")
    public RestResponse getSubjectTransLogDtosById(@RequestParam("id") Integer id){
        List<SubjectTransLogDto> subjectTransLogDtos = creditOpeningService.getSubjectTransLogDtos(id);
        return new RestResponseBuilder<>().success(subjectTransLogDtos);
    }

    //pc 持有中 转让中 已完成
    @GetMapping("pc/credit/manage")
    public RestResponse creditInvestManage(@RequestParam("userId") String userId,
                                          @RequestParam("type") int type,
                                          @RequestParam(value = "pageNo", defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        if (type == CreditManageHoldDto.PAGE_TYPE_HOLDING) {

            CreditManageHoldDto creditManageHoldDto = new CreditManageHoldDto();
            List<CreditManageHoldDto.Detail> details = new ArrayList<>();

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
                if (subjectAccounts.size() > 0){
                    //持有中
                    for (SubjectAccount subjectAccount : subjectAccounts) {
                        CreditManageHoldDto.Detail detail = new CreditManageHoldDto.Detail();
                        detail.setId(subjectAccount.getId());
                        Subject subject = subjectService.getBySubjectId(subjectAccount.getSubjectId());
                        detail.setName(subject.getName());
                        detail.setRepayType(subjectService.getRepayType(subjectAccount.getSubjectId()));
                        SubjectTransLog subjectTransLog = subjectTransLogService.getById(subjectAccount.getTransLogId());
                        //原标的利率
                        BigDecimal totalRate = creditOpeningService.calcTotalRate(subjectAccount.getSubjectId());
                        CreditOpening creditOpening = creditOpeningService.getById(subjectTransLog.getTargetId());
                        //Double newRate = creditOpeningService.calcNewRate(creditOpening.getSubjectId(), creditOpening);
                        detail.setRate(totalRate.doubleValue());
                        detail.setRateStr(df4.format(detail.getRate() * 100));
                        detail.setStatus(CreditConstant.JOINING);
                        detail.setEndTime("生成中");
                        detail.setHoldingAmt((subjectAccount.getCurrentPrincipal()) / 100.0);
                        detail.setHoldingAmtStr(df4.format(detail.getHoldingAmt()));
                        detail.setReceivedAmt((subjectAccount.getPaidInterest() +subjectAccount.getSubjectPaidBonusInterest())/100.0);
                        detail.setReceivedAmtStr(df4.format(detail.getReceivedAmt()));
                        detail.setExpectAmt((subjectAccount.getExpectedInterest() + subjectAccount.getSubjectExpectedBonusInterest())/100.0);
                        detail.setExpectAmtStr(df4.format(detail.getExpectAmt()));
                        //红包信息
                        RedPacket redPacket =  null;
                        if (subjectTransLog.getRedPacketId() != null && subjectTransLog.getRedPacketId() > 0){
                            redPacket = redPacketService.getRedPacketById(subjectTransLog.getRedPacketId());
                        }
                        detail.setRedPacket(redPacket);
                        Credit credit = creditService.getBySubjectAccountId(subjectAccount.getId());
                        if(credit != null && (Credit.CREDIT_STATUS_HOLDING == credit.getCreditStatus())){
                            detail.setStatus(CreditConstant.REPAYING);
                            detail.setEndTime(DateUtil.parseDate(credit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());
                            String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
                            String endDate = credit.getEndTime().substring(0, 8);
                            long days = DateUtil.betweenDays(currentDate, endDate);
                            detail.setResidualDay((int) days);
                        }
                        //是否可转让
                        detail.setTransfer(subjectService.checkCondition(subject,subjectAccount));
                        details.add(detail);
                    }
                }
            }
                creditManageHoldDto.setAmount(amount);
                creditManageHoldDto.setAmountStr(df4.format(amount));
                creditManageHoldDto.setProfit(profit);
                creditManageHoldDto.setProfitStr(df4.format(profit));
                creditManageHoldDto.setPageType(CreditManageHoldDto.PAGE_TYPE_HOLDING);
                creditManageHoldDto.setDetails(details);

                //分页相关
                PageInfo<SubjectAccount> pageInfo = new PageInfo<>(subjectAccounts);
                creditManageHoldDto.setPage(pageInfo.getPageNum());
                creditManageHoldDto.setSize(pageInfo.getSize());
                creditManageHoldDto.setTotalPages(pageInfo.getPages());
                creditManageHoldDto.setTotal(pageInfo.getTotal());
            return new RestResponseBuilder<>().success(creditManageHoldDto);
        }else  if (type == CreditManageHoldDto.PAGE_TYPE_TRANSFERRING){
            CreditManageTransferDto creditManageTransferDto = new CreditManageTransferDto();
            List<CreditManageTransferDto.Detail> details = new ArrayList<>();

            //总转让金额
            Double amount = 0.0;
            //已成交金额
            Double finishAmt = 0.0;

            //转让中
            List<CreditOpening> newCreditOpenings = new ArrayList<>();
            List<CreditOpening> creditOpenings = creditOpeningService.getByUserIdAndStatusAndOpenChannel(userId, CreditOpening.OPEN_CHANNEL);
            if(creditOpenings != null && creditOpenings.size() > 0){
                List<CreditOpening> allCreditOpenings = creditOpeningService.sortByConditionNoPage(creditOpenings,Credit.TARGET_CREDIT);
                if(allCreditOpenings != null && allCreditOpenings.size() > 0){
                    for (CreditOpening creditOpening : allCreditOpenings) {
                        amount += creditOpening.getTransferPrincipal() / 100.0;
                        Double transferAmt = (creditOpening.getTransferPrincipal() - creditOpening.getAvailablePrincipal()) / 100.0;
                        if(CreditOpening.STATUS_CANCEL_PENDING.equals(creditOpening.getStatus())){
                            SubjectTransLog subjectTransLog = subjectTransLogService.getById(creditOpening.getSourceChannelId());
                            transferAmt= subjectTransLog.getProcessedAmt() / 100.0;
                        }
                        finishAmt += transferAmt;
                    }
                }
                newCreditOpenings= creditOpeningService.sortByCondition(creditOpenings,Credit.TARGET_CREDIT,pageNum,pageSize);
                if (newCreditOpenings.size() > 0) {
                    //持有金额
                    for (CreditOpening creditOpening : newCreditOpenings) {
                        Credit credit = creditService.getById(creditOpening.getCreditId());
                        Subject subject = subjectService.getBySubjectId(creditOpening.getSubjectId());
                        CreditManageTransferDto.Detail detail = new CreditManageTransferDto.Detail();
                        detail.setId(creditOpening.getId());
                        detail.setName(subject.getName());
                        BigDecimal totalRate = creditOpeningService.calcTotalRate(credit.getSubjectId());
                        //Double newRate = creditOpeningService.calcNewRate(credit.getSubjectId(),creditOpening);
                        detail.setRate(totalRate.doubleValue());
                        detail.setRateStr(df4.format(detail.getRate() * 100));
                        //红包信息
                        RedPacket redPacket =  new RedPacket();
                        SubjectTransLog subjectTransLog = subjectTransLogService.getById(credit.getSourceChannelId());
                        if (subjectTransLog.getRedPacketId() != null && subjectTransLog.getRedPacketId() > 0){
                            redPacket = redPacketService.getRedPacketById(subjectTransLog.getRedPacketId());
                        }
                        detail.setRedPacket(redPacket);
                        //回款方式
                        detail.setRepayType(subjectService.getRepayType(credit.getSubjectId()));
                        //持有金额
                        detail.setHoldingAmt(credit.getInitPrincipal() / 100.0);
                        detail.setHoldingAmtStr(df4.format(detail.getHoldingAmt()));
                        //转让中金额
                        Double transferAmt = (creditOpening.getAvailablePrincipal()) / 100.0;
                        detail.setTransferAmt(transferAmt);
                        detail.setTransferAmtStr(df4.format(detail.getTransferAmt()));

                        //已成交金额
                        Double finAmt = (creditOpening.getTransferPrincipal() - creditOpening.getAvailablePrincipal()) / 100.0;
                        if(CreditOpening.STATUS_CANCEL_PENDING.equals(creditOpening.getStatus())){
                            subjectTransLog = subjectTransLogService.getById(creditOpening.getSourceChannelId());
                            finAmt= subjectTransLog.getProcessedAmt() / 100.0;
                        }
                        detail.setHasFinishAmt(finAmt);
                        detail.setHasFinishAmtStr(df4.format(detail.getHasFinishAmt()));


                        //剩余时间
                        String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
                        String endDate = credit.getEndTime().substring(0, 8);
                        long days = DateUtil.betweenDays(currentDate, endDate);
                        detail.setResidualDay((int) days);

                        //是否可撤销
                        Integer status = 1;
                        if(creditOpening.getAvailablePrincipal() == 0 || CreditOpening.STATUS_CANCEL_PENDING.equals(creditOpening.getStatus())){
                            status = 0;
                        }
                        detail.setStatus(status);
                        details.add(detail);
                    }
                }
            }
            creditManageTransferDto.setAmount(amount);
            creditManageTransferDto.setAmountStr(df4.format(amount));
            creditManageTransferDto.setFinishAmt(finishAmt);
            creditManageTransferDto.setFinishAmtStr(df4.format(finishAmt));
            creditManageTransferDto.setPageType(CreditManageHoldDto.PAGE_TYPE_TRANSFERRING);
            creditManageTransferDto.setDetails(details);

            //分页相关
            creditManageTransferDto.setPage(pageNum);
            creditManageTransferDto.setSize(newCreditOpenings.size());
            creditManageTransferDto.setTotalPages(newCreditOpenings.size() % pageSize != 0 ? newCreditOpenings.size() / pageSize + 1:newCreditOpenings.size() / pageSize);
            creditManageTransferDto.setTotal(newCreditOpenings.size());
            return new RestResponseBuilder<>().success(creditManageTransferDto);
        }if(type == CreditManageHoldDto.PAGE_TYPE_FINISH){
            AppCreditManageFinishDto appCreditManageFinishDto = new AppCreditManageFinishDto();
            List<AppCreditManageFinishDto.Detail> details = new ArrayList<>();

            //总转让金额
            Double amount = 0.0;
            //实际到账金额
            Double totalActualAmt = 0.0;

            //正常还款已完成
            List<Credit> credits = creditService.getCreditFinishByUserId(userId,Credit.TARGET_CREDIT, Credit.CREDIT_STATUS_FINISH);
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
                    CreditOpening creditOpening = creditOpeningService.getById(credit.getTargetId());
                    //Double newRate = creditOpeningService.calcNewRate(creditOpening.getSubjectId(), creditOpening);
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
                    amount += detail.getHoldingAmt();
                    //回款金额
                    detail.setTransferAmt(0.0);
                    detail.setTransferAmtStr(df4.format(detail.getTransferAmt()));
                    //实际到账金额
                    detail.setActualAmt((principal + interest) / 100.0);
                    detail.setActualAmtStr(df4.format(detail.getActualAmt()));
                    totalActualAmt += detail.getActualAmt();
                    if((principal+interest)>0){
                        details.add(detail);
                    }
                }
            }
            //转让完成
            List<SubjectTransLog> subjectTransLogs = subjectTransLogService.getSubjectTransLogByUserIdAndStatus(userId);
            if(subjectTransLogs != null && subjectTransLogs.size() > 0){
                List<SubjectTransLog> logs = subjectTransLogService.sortBySource(subjectTransLogs,SubjectAccount.SOURCE_CREDIT);
                if(logs != null && logs.size() > 0){
                    for (SubjectTransLog subjectTransLog : logs) {
                        AppCreditManageFinishDto.Detail detail = new AppCreditManageFinishDto.Detail();
                        SubjectAccount subjectAccount = subjectAccountService.findAccountById(subjectTransLog.getAccountId());
                        //transLogId
                        detail.setId(subjectTransLog.getId());
                        //利率
                        Credit credit = creditService.getBySubjectAccountId(subjectAccount.getId());
                        CreditOpening creditOpening = creditOpeningService.getById(credit.getTargetId());
                        BigDecimal totalRate = creditOpeningService.calcTotalRate(creditOpening.getSubjectId());
                        //Double newRate = creditOpeningService.calcNewRate(creditOpening.getSubjectId(), creditOpening);
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
            }
            List<AppCreditManageFinishDto.Detail> list = new PageUtil().ListSplit(details, pageNum, pageSize);
            appCreditManageFinishDto.setPage(pageNum);
            appCreditManageFinishDto.setSize(pageSize);
            appCreditManageFinishDto.setTotal(details.size());
            appCreditManageFinishDto.setTotalPages(details.size() % pageSize != 0 ? details.size() / pageSize + 1:details.size() / pageSize);
            appCreditManageFinishDto.setAmount(amount);
            appCreditManageFinishDto.setAmountStr(df4.format(amount));
            appCreditManageFinishDto.setTotalActualAmt(totalActualAmt);
            appCreditManageFinishDto.setTotalActualAmtStr(df4.format(totalActualAmt));
            appCreditManageFinishDto.setPageType(CreditManageHoldDto.PAGE_TYPE_FINISH);
            appCreditManageFinishDto.setDetails(list);
            return new RestResponseBuilder<>().success(appCreditManageFinishDto);
        } else{
            throw new IllegalArgumentException("参数type不支持！");
        }
    }

    @GetMapping("/pc/credit/manage/creditHoldDetail")
    public RestResponse getCreditHoldDetailDto(@RequestParam("id") int subjectAccountId,
                                                  @RequestParam(value = "userId") String userId) {

        SubjectAccount subjectAccount = subjectAccountService.findAccountById(subjectAccountId);
        if (subjectAccount == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        Credit credit = creditService.getBySubjectAccountId(subjectAccount.getId());
        Subject subject = subjectService.getBySubjectId(subjectAccount.getSubjectId());
        SubjectTransLog subjectTransLog = subjectTransLogService.getTransLogByAccountId(subjectAccountId);
        CreditOpening creditOpening = creditOpeningService.getById(subjectTransLog.getTargetId());
        Credit oldCredit = creditService.getById(creditOpening.getCreditId());
        CreditHoldDetailDto creditHoldDetailDto = new CreditHoldDetailDto();
        creditHoldDetailDto.setId(subjectAccount.getId());
        creditHoldDetailDto.setName(subject.getName());

        //还款状态
        creditHoldDetailDto.setReturnStatus(CreditConstant.REPAYING);

        //剩余时间
        creditHoldDetailDto.setResidualDay(creditService.getDays(oldCredit));


        //原标的利率
        BigDecimal totalRate = creditOpeningService.calcTotalRate(subjectAccount.getSubjectId());
        ActivityMarkConfigure activityMarkConfigure = null;
        if (subject.getActivityId() != null && subject.getActivityId() > 0) {
            activityMarkConfigure = activityMarkConfigureService.findById(subject.getActivityId());
            if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null) {
                totalRate = subject.getInvestRate();
            }
        }
        //Double newRate = creditOpeningService.calcNewRate(creditOpening.getSubjectId(), creditOpening);
        creditHoldDetailDto.setRate(totalRate.doubleValue());
        creditHoldDetailDto.setRateStr(df4.format(creditHoldDetailDto.getRate() * 100));


        //预期收益
        Double expectProfit = (subjectAccount.getExpectedInterest() +subjectAccount.getSubjectExpectedBonusInterest())/100.0;
        creditHoldDetailDto.setExpectProfit(expectProfit);
        creditHoldDetailDto.setExpectProfitStr(df4.format(creditHoldDetailDto.getExpectProfit()));

        //是否可转
        creditHoldDetailDto.setStatus(subjectService.checkCondition(subject,subjectAccount));
        creditHoldDetailDto.setMessage(subjectService.checkConditionStr(subject,subjectAccount));

        //还款方式
        creditHoldDetailDto.setRepayType(subjectService.getRepayType(subjectAccount.getSubjectId()));

        //投资时间
        creditHoldDetailDto.setBuyTime(subjectTransLog.getCreateTime());
        creditHoldDetailDto.setEndTime(DateUtil.parseDate(oldCredit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());

        //投资金额
        creditHoldDetailDto.setHoldingPrincipal(subjectAccount.getCurrentPrincipal() / 100.0);
        creditHoldDetailDto.setHoldingPrincipalStr(df4.format(creditHoldDetailDto.getHoldingPrincipal()));

        //红包信息
        RedPacket redPacket =  null;
        if (subjectTransLog.getRedPacketId() != null && subjectTransLog.getRedPacketId() > 0){
            redPacket = redPacketService.getRedPacketById(subjectTransLog.getRedPacketId());
        }
        creditHoldDetailDto.setRedPacket(redPacket);

        //url
        creditHoldDetailDto.setCreditDetailUrl(detailUrl+"?subjectId="+subject.getSubjectId());
        creditHoldDetailDto.setCreditTransferUrl(transferUrl);
        if(credit != null){
            String contractId = credit.getContractId();
            if(StringUtils.isNotBlank(contractId)){
                creditHoldDetailDto.setCreditTransferUrl(creditService.getContractViewPdfUrlByContractId(contractId));
            }
        }

        //购买状态
        creditHoldDetailDto.setBuyStatus(CreditConstant.BUY_STATUS_SUCCESS);

        //借款人信息
        User user = userService.getUserById(subject.getBorrowerId());
        creditHoldDetailDto.setUserName(user.getRealname().substring(0,1)+"**");

        creditHoldDetailDto.setCardId(user.getIdCard().substring(0,6)+"********"+user.getIdCard().substring(14));
        creditHoldDetailDto.setCreditId("生成中");
        if(credit != null ){
            creditHoldDetailDto.setCreditId(credit.getId().toString());
        }
        creditHoldDetailDto.setSubjectId(subjectAccount.getSubjectId());
        return new RestResponseBuilder<>().success(creditHoldDetailDto);

    }

    @GetMapping("/pc/credit/manage/creditTransferDetail")
    public RestResponse getCreditTransferDetailDto(@RequestParam("id") int creditOpeningId,
                                                      @RequestParam(value = "userId") String userId) {

        CreditOpening creditOpening = creditOpeningService.getById(creditOpeningId);
        if (creditOpening == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        CreditTransferDetailDto creditTransferDetailDto = new CreditTransferDetailDto();
        creditTransferDetailDto.setId(creditOpening.getId());
        Subject subject = subjectService.getBySubjectId(creditOpening.getSubjectId());
        creditTransferDetailDto.setName(subject.getName());

        creditTransferDetailDto.setReturnStatus(CreditConstant.TRANSFERING);

        //剩余时间
        Credit credit = creditService.getById(creditOpening.getCreditId());
        creditTransferDetailDto.setResidualDay(creditService.getDays(credit));

        //已到账收益
        SubjectAccount subjectAccount = subjectAccountService.findAccountById(credit.getSourceAccountId());
        creditTransferDetailDto.setReceivedAmt(subjectAccount.getPaidInterest() / 100.0);
        creditTransferDetailDto.setReceivedAmtStr(df4.format(creditTransferDetailDto.getReceivedAmt()));

        //年利化率
        BigDecimal totalRate = creditOpeningService.calcTotalRate(credit.getSubjectId());
        //Double newRate = creditOpeningService.calcNewRate(creditOpening.getSubjectId(), creditOpening);
        ActivityMarkConfigure activityMarkConfigure = null;
        if (subject.getActivityId() != null && subject.getActivityId() > 0) {
            activityMarkConfigure = activityMarkConfigureService.findById(subject.getActivityId());
            if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null) {
                totalRate = subject.getInvestRate();
            }
        }
        creditTransferDetailDto.setRate(totalRate.doubleValue());
        creditTransferDetailDto.setRateStr(df4.format(creditTransferDetailDto.getRate() * 100));

        //还款方式
        creditTransferDetailDto.setRepayType(subjectService.getRepayType(creditOpening.getSubjectId()));

        //项目结束时间
        creditTransferDetailDto.setEndTime(DateUtil.parseDate(credit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());

        //购买时间
        creditTransferDetailDto.setBuyTime(DateUtil.parseDate(credit.getCreateTime(), DateUtil.DATE_TIME_FORMATTER_19).toString());

        //购买金额
        creditTransferDetailDto.setHoldingPrincipal(credit.getInitPrincipal() / 100.0);
        creditTransferDetailDto.setHoldingPrincipalStr(df4.format(creditTransferDetailDto.getHoldingPrincipal()));

        //购买状态
        creditTransferDetailDto.setBuyStatus(CreditConstant.BUY_STATUS_SUCCESS);

        //出售金额
        creditTransferDetailDto.setSaleAmt(creditOpening.getTransferPrincipal() / 100.0);
        creditTransferDetailDto.setSaleAmtStr(df4.format(creditTransferDetailDto.getSaleAmt()));

        //折让率
        creditTransferDetailDto.setTransDiscount(creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue());
        creditTransferDetailDto.setTransDiscountStr(df4.format(creditTransferDetailDto.getTransDiscount()));

        //持有天数
        String startDate = credit.getCreateTime().substring(0, 10).replace("-","");
        String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
        long holdDays = DateUtil.betweenDays(startDate,currentDate);
        creditTransferDetailDto.setHoldDay((int) holdDays);

        //转让时间
        creditTransferDetailDto.setTransferTime(creditOpening.getCreateTime().substring(0,10));

        //散标交易配置信息
        SubjectTransferParam subjectTransferParam = subjectTransferParamService.getByTransferParamCode(subject.getTransferParamCode());

        //转让服务费
        SubjectTransLog subjectTransLog = subjectTransLogService.getById(creditOpening.getSourceChannelId());
        if(subjectTransLog.getTransFee() == 0){
            Double feeRate = subjectAccountService.calcTransFeeNew(subjectAccount.getTransLogId(), subject, subjectTransferParam);
            creditTransferDetailDto.setFee((creditOpening.getTransferPrincipal()/100.0) * feeRate / 100.0);
        }else{
            creditTransferDetailDto.setFee(0.0);
        }
        creditTransferDetailDto.setFeeStr(df4.format(creditTransferDetailDto.getFee()));
        //扣除红包奖励
        Double redFee = subjectAccountService.calcRedFee(subjectAccount, credit);
        creditTransferDetailDto.setRedFee((creditOpening.getTransferPrincipal() /100.0)   * redFee);
        creditTransferDetailDto.setRedFeeStr(df4.format(creditTransferDetailDto.getRedFee() ));

        //溢价手续费
        Double overFee = 0.0;
        Double transferDiscount = creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue();
        if(transferDiscount > 100){
            overFee = (creditOpening.getTransferPrincipal() /100.0) * (transferDiscount - 100) / 100.0 * 0.2;
        }
        creditTransferDetailDto.setOverFee(overFee);
        creditTransferDetailDto.setOverFeeStr(df4.format(overFee));

        //预计到账金额
        Double expectAmt = ArithUtil.calcExp((creditOpening.getTransferPrincipal() /100.0) * (transferDiscount / 100.0), creditTransferDetailDto.getRedFee(), creditTransferDetailDto.getOverFee(), creditTransferDetailDto.getFee());
        creditTransferDetailDto.setExpectAmt(expectAmt);
        creditTransferDetailDto.setExpectAmtStr(df4.format(creditTransferDetailDto.getExpectAmt()));

        //是否可撤销
        Integer status = 1;
        if(creditOpening.getAvailablePrincipal() == 0 || CreditOpening.STATUS_CANCEL_PENDING.equals(creditOpening.getStatus())){
            status = 0;
        }
        creditTransferDetailDto.setStatus(status);

        creditTransferDetailDto.setCreditTransferUrl(transferUrl);
        creditTransferDetailDto.setCreditDetailUrl(detailUrl+"?subjectId="+subject.getSubjectId());

        return new RestResponseBuilder<>().success(creditTransferDetailDto);
    }

    @GetMapping("/pc/credit/manage/creditFinishDetail")
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
            CreditOpening creditOpening = creditOpeningService.getById(credit.getTargetId());
            BigDecimal totalRate = creditOpeningService.calcTotalRate(credit.getSubjectId());
            ActivityMarkConfigure activityMarkConfigure = null;
            if (subject.getActivityId() != null && subject.getActivityId() > 0) {
                activityMarkConfigure = activityMarkConfigureService.findById(subject.getActivityId());
                if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null) {
                    totalRate = subject.getInvestRate();
                }
            }
            //Double newRate = creditOpeningService.calcNewRate(creditOpening.getSubjectId(),creditOpening);
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
            appCreditFinishDetailDto.setStatus(CreditConstant.REPAYFINSH);
            appCreditFinishDetailDto.setPageType(AppCreditFinishDetailDto.PAGE_TYPE_REPAYFINISH);
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
            BigDecimal totalRate = creditOpeningService.calcTotalRate(subjectTransLog.getSubjectId());
            //Double newRate = creditOpeningService.calcNewRate(creditOpening.getSubjectId(),creditOpening);
            ActivityMarkConfigure activityMarkConfigure = null;
            if (subject.getActivityId() != null && subject.getActivityId() > 0) {
                activityMarkConfigure = activityMarkConfigureService.findById(subject.getActivityId());
                if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null) {
                    totalRate = subject.getInvestRate();
                }
            }
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
            appCreditTransferFinishDetailDto.setRedFee((subjectTransLog.getProcessedAmt() /100.0)  * redFee);
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

    @GetMapping("/credit/transfer/record")
    public RestResponse getCreditTransferRecord(@RequestParam("creditOpeningId") int creditOpeningId) {

        List<Map<String, Object>> result = creditOpeningService.getCreditTransferRecord(creditOpeningId);

        return new RestResponseBuilder<>().success(result);
    }

}
