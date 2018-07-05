package com.jiuyi.ndr.resource.credit;

import com.jiuyi.ndr.constant.CreditConstant;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectAccount;
import com.jiuyi.ndr.domain.subject.SubjectTransLog;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.dto.credit.CreditManageHoldDto;
import com.jiuyi.ndr.dto.credit.CreditSubjectDto;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditManageHoldDto;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditSubjectDto;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.subject.SubjectTransLogService;
import com.jiuyi.ndr.service.user.ActivityMarkConfigureService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@RestController
public class CreditManageResource {

    @Autowired
    private CreditService creditService;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private CreditOpeningService creditOpeningService;
    @Autowired
    private SubjectTransLogService subjectTransLogService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private ActivityMarkConfigureService activityMarkConfigureService;



    private DecimalFormat df4 = new DecimalFormat("######0.##");

    //app 可转让列表
    @GetMapping("app/credit/transfer/list")
    public RestResponse creditInvestManage(@RequestParam("userId") String userId,
                                           @RequestParam("type") int type,
                                           @RequestParam(value = "pageNo", defaultValue = "1") int pageNum,
                                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        AppCreditManageHoldDto appCreditManageHoldDto = new AppCreditManageHoldDto();
        List<AppCreditManageHoldDto.Detail> details = new ArrayList<>();

        AppCreditSubjectDto appCreditSubjectDto = new AppCreditSubjectDto();
        List<AppCreditSubjectDto.Detail> lists = new ArrayList<>();
        List<Credit> credits = creditService.getByUserIdAndStatusAndSourceChannel(userId,Credit.SOURCE_CHANNEL_SUBJECT,Credit.CREDIT_STATUS_HOLDING,type);
        if (credits != null){
            List<Credit> creditList = creditService.sortByCondition(credits, pageNum, pageSize);
            if (!creditList.isEmpty()){
                if(Credit.TARGET_CREDIT == type){//债权
                    //总持有金额
                    Double amount = 0.0;
                    //预期收益
                    Double profit = 0.0;
                    for (Credit credit : creditList) {
                        AppCreditManageHoldDto.Detail detail = new AppCreditManageHoldDto.Detail();
                        SubjectAccount subjectAccount = subjectAccountService.findAccountById(credit.getSourceAccountId());
                        detail.setId(subjectAccount.getId());
                        Subject subject = subjectService.getBySubjectId(credit.getSubjectId());
                        detail.setName(subject.getName());
                        detail.setStatus(CreditConstant.REPAYING);
                        detail.setRepayType(subjectService.getRepayType(credit.getSubjectId()));
                        detail.setBuyTime(DateUtil.parseDate(credit.getCreateTime(), DateUtil.DATE_TIME_FORMATTER_19).toString());
                        detail.setEndTime(DateUtil.parseDate(credit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());
                        detail.setHoldingAmt(credit.getHoldingPrincipal() / 100.0);
                        detail.setHoldingAmtStr(df4.format(credit.getHoldingPrincipal() / 100.0));
                        amount += detail.getHoldingAmt();
                        detail.setExpectAmt((subjectAccount.getExpectedInterest() +subjectAccount.getSubjectExpectedBonusInterest())/100.0);
                        detail.setExpectAmtStr(df4.format(detail.getExpectAmt()));
                        profit += detail.getExpectAmt();
                        details.add(detail);
                    }
                    appCreditManageHoldDto.setDetails(details);
                    appCreditManageHoldDto.setPageType(AppCreditManageHoldDto.TARGET_CREDIT);
                    appCreditManageHoldDto.setAmount(amount);
                    appCreditManageHoldDto.setAmountStr(df4.format(amount));
                    appCreditManageHoldDto.setProfit(profit);
                    appCreditManageHoldDto.setProfitStr(df4.format(profit));
                    return new RestResponseBuilder<>().success(appCreditManageHoldDto);
                }else {//散标
                    for (Credit credit : creditList) {
                        AppCreditSubjectDto.Detail detail = new AppCreditSubjectDto.Detail();
                        //账户id
                        detail.setId(credit.getSourceAccountId());
                        //项目名称
                        Subject subject = subjectService.getBySubjectId(credit.getSubjectId());
                        detail.setName(subject.getName());
                        //回款方式
                        detail.setRepayType(subjectService.getRepayType(credit.getSubjectId()));
                        //投资金额
                        detail.setInvestAmt(credit.getHoldingPrincipal() / 100.0);
                        detail.setInvestAmtStr(df4.format(detail.getInvestAmt()));
                        //年化利率
                        BigDecimal totalRate = creditOpeningService.calcTotalRate(credit.getSubjectId());
                        detail.setRate(totalRate.multiply(new BigDecimal(100)).doubleValue());
                        detail.setRateStr(df4.format(detail.getRate())+"%");
                        //待收本息
                        SubjectAccount subjectAccount = subjectAccountService.findAccountById(credit.getSourceAccountId());
                        detail.setExpectAmt((subjectAccount.getCurrentPrincipal() + subjectAccount.getAmtToTransfer() + subjectAccount.getExpectedInterest()+subjectAccount.getSubjectExpectedBonusInterest()) / 100.0);
                        detail.setExpectAmtStr(df4.format(detail.getExpectAmt()));
                        //红包相关
                        detail.setRedPacket("");
                        SubjectTransLog log = subjectTransLogService.getById(subjectAccount.getTransLogId());
                        if (log != null && log.getRedPacketId() != null && log.getRedPacketId() > 0) {
                            RedPacket packet = redPacketService.getRedPacketById(log.getRedPacketId());
                            if ("money".equals(packet.getType())) {
                                detail.setRedPacket(df4.format(packet.getMoney())
                                        + "元现金券");
                            } else if ("rate".equals(packet.getType())) {
                                detail.setRedPacket(df4.format(packet.getRate() * 100)
                                        + "%加息券");
                            } else if ("deduct".equals(packet.getType())) {
                                detail.setRedPacket(df4.format(packet.getMoney())
                                        + "元抵扣券");
                            } else if ("rateByDay".equals(packet.getType())) {
                                detail.setRedPacket(df4.format(packet.getRate() * 100)
                                        + "%"+packet.getRateDay()+"天加息券");
                            }
                        }
                        //活动相关
                        detail.setActivityName("");
                        detail.setIncreaseInterest(0.0);
                        detail.setFontColor("");
                        detail.setBackground("");
                        Integer activityId = subject.getActivityId();
                        if (activityId != null) {
                            ActivityMarkConfigure amc = activityMarkConfigureService.findById(activityId);
                            detail.setActivityName(amc.getActivityName());
                            detail.setIncreaseInterest(amc.getIncreaseInterest());
                            detail.setFontColor(amc.getFontColorPc());
                            detail.setBackground(amc.getBackgroundPc());
                            if(amc.getIncreaseTerm() != null){
                                detail.setAddTerm(amc.getIncreaseTerm());
                            }else{
                                detail.setAddTerm(0);
                            }
                        }
                        lists.add(detail);
                    }
                    appCreditSubjectDto.setDetials(lists);
                    appCreditSubjectDto.setPageType(AppCreditManageHoldDto.TARGET_SUBJECT);
                    return new RestResponseBuilder<>().success(appCreditSubjectDto);
                }
            }else{//没有满足条件的数据
                if(Credit.TARGET_CREDIT == type){
                    appCreditManageHoldDto.setPageType(AppCreditManageHoldDto.TARGET_CREDIT);
                    return new RestResponseBuilder<>().success(appCreditManageHoldDto);
                }else {
                    appCreditSubjectDto.setPageType(AppCreditManageHoldDto.TARGET_SUBJECT);
                    return new RestResponseBuilder<>().success(appCreditSubjectDto);
                }
            }
        } else {
            if(Credit.TARGET_CREDIT == type){
                appCreditManageHoldDto.setPageType(AppCreditManageHoldDto.TARGET_CREDIT);
                return new RestResponseBuilder<>().success(appCreditManageHoldDto);
            }else {
                appCreditSubjectDto.setPageType(AppCreditManageHoldDto.TARGET_SUBJECT);
                return new RestResponseBuilder<>().success(appCreditSubjectDto);
            }
        }
    }

    //pc 可转让列表
    @GetMapping("pc/credit/transfer/list")
    public RestResponse creditInvestManagePc(@RequestParam("userId") String userId,
                                           @RequestParam("type") int type,
                                           @RequestParam(value = "pageNo", defaultValue = "1") int pageNum,
                                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        CreditManageHoldDto creditManageHoldDto = new CreditManageHoldDto();
        List<CreditManageHoldDto.Detail> details = new ArrayList<>();

        CreditSubjectDto creditSubjectDto = new CreditSubjectDto();
        List<CreditSubjectDto.Detail> lists = new ArrayList<>();
        List<Credit> credits = creditService.getByUserIdAndStatusAndSourceChannel(userId,Credit.SOURCE_CHANNEL_SUBJECT,Credit.CREDIT_STATUS_HOLDING,type);
        if (credits != null){
            List<Credit> creditList = creditService.sortByCondition(credits, pageNum, pageSize);
            if (!creditList.isEmpty()){
                if(Credit.TARGET_CREDIT == type){//债权
                    for (Credit credit : creditList) {
                        CreditManageHoldDto.Detail detail = new CreditManageHoldDto.Detail();
                        SubjectAccount subjectAccount = subjectAccountService.findAccountById(credit.getSourceAccountId());
                        detail.setId(subjectAccount.getId());
                        Subject subject = subjectService.getBySubjectId(credit.getSubjectId());
                        detail.setName(subject.getName());
                        BigDecimal rate = creditOpeningService.calcTotalRate(credit.getSubjectId());
                        detail.setRate(rate.doubleValue());
                        detail.setRateStr(df4.format(rate.doubleValue() * 100));
                        detail.setRepayType(subjectService.getRepayType(credit.getSubjectId()));
                        detail.setStatus(CreditConstant.REPAYING);
                        detail.setHoldingAmt(credit.getHoldingPrincipal()/100.0);
                        detail.setHoldingAmtStr(df4.format(credit.getHoldingPrincipal()/ 100.0));
                        detail.setEndTime(DateUtil.parseDate(credit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());
                        String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
                        String endDate = credit.getEndTime().substring(0, 8);
                        long days = DateUtil.betweenDays(currentDate, endDate);
                        detail.setResidualDay((int) days);
                        //已到账收益
                        detail.setReceivedAmt((subjectAccount.getPaidInterest() +subjectAccount.getSubjectPaidBonusInterest())/100.0);
                        detail.setReceivedAmtStr(df4.format(detail.getReceivedAmt()));

                        //红包信息
                        RedPacket redPacket =  null;
                        SubjectTransLog subjectTransLog = subjectTransLogService.getById(credit.getSourceChannelId());
                        if (subjectTransLog.getRedPacketId() != null && subjectTransLog.getRedPacketId() > 0){
                            redPacket = redPacketService.getRedPacketById(subjectTransLog.getRedPacketId());
                        }
                        detail.setRedPacket(redPacket);
                        details.add(detail);
                    }
                    creditManageHoldDto.setPageType(CreditManageHoldDto.TARGET_CREDIT);
                    creditManageHoldDto.setDetails(details);

                    return new RestResponseBuilder<>().success(creditManageHoldDto);
                }else {//散标
                    for (Credit credit : creditList) {
                        CreditSubjectDto.Detail detail = new CreditSubjectDto.Detail();
                        SubjectAccount subjectAccount = subjectAccountService.findAccountById(credit.getSourceAccountId());
                        detail.setId(subjectAccount.getId());
                        Subject subject = subjectService.getBySubjectId(credit.getSubjectId());
                        detail.setName(subject.getName());
                        detail.setRepayType(subjectService.getRepayType(credit.getSubjectId()));
                        BigDecimal rate = creditOpeningService.calcTotalRate(credit.getSubjectId());
                        detail.setRate(rate.doubleValue());
                        detail.setRateStr(df4.format(rate.doubleValue() * 100));
                        detail.setHoldAmt(credit.getHoldingPrincipal()/100.0);
                        detail.setHoldAmtStr(df4.format(credit.getHoldingPrincipal()/ 100.0));
                        detail.setEndTime(DateUtil.parseDate(credit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());
                        //预期收益
                        detail.setExpectAmt((subjectAccount.getExpectedInterest() + subjectAccount.getSubjectExpectedBonusInterest())/100.0);
                        detail.setExpectAmtStr(df4.format(detail.getExpectAmt()));
                        //已到账收益
                        detail.setReceivedAmt((subjectAccount.getPaidInterest() +subjectAccount.getSubjectPaidBonusInterest())/100.0);
                        detail.setReceivedAmtStr(df4.format(detail.getReceivedAmt()));

                        //红包相关
                        detail.setRedPacket("");
                        SubjectTransLog log = subjectTransLogService.getById(subjectAccount.getTransLogId());
                        if (log != null && log.getRedPacketId() != null && log.getRedPacketId() > 0) {
                            RedPacket packet = redPacketService.getRedPacketById(log.getRedPacketId());
                            if ("money".equals(packet.getType())) {
                                detail.setRedPacket(df4.format(packet.getMoney())
                                        + "元现金券");
                            } else if ("rate".equals(packet.getType())) {
                                detail.setRedPacket(df4.format(packet.getRate() * 100)
                                        + "%加息券");
                            } else if ("deduct".equals(packet.getType())) {
                                detail.setRedPacket(df4.format(packet.getMoney())
                                        + "元抵扣券");
                            } else if ("rateByDay".equals(packet.getType())) {
                                detail.setRedPacket(df4.format(packet.getRate() * 100)
                                        + "%"+packet.getRateDay()+"天加息券");
                            }
                        }
                        //活动相关
                        detail.setActivityName("");
                        detail.setIncreaseInterest(0.0);
                        detail.setFontColor("");
                        detail.setBackground("");
                        Integer activityId = subject.getActivityId();
                        if (activityId != null) {
                            ActivityMarkConfigure amc = activityMarkConfigureService.findById(activityId);
                            detail.setActivityName(amc.getActivityName());
                            detail.setIncreaseInterest(amc.getIncreaseInterest());
                            detail.setFontColor(amc.getFontColorPc());
                            detail.setBackground(amc.getBackgroundPc());
                            if(amc.getIncreaseTerm() != null){
                                detail.setAddTerm(amc.getIncreaseTerm());
                            }else{
                                detail.setAddTerm(0);
                            }
                        }
                        lists.add(detail);
                    }
                    creditSubjectDto.setPageType(CreditManageHoldDto.TARGET_SUBJECT);
                    creditSubjectDto.setDetials(lists);
                    return new RestResponseBuilder<>().success(creditSubjectDto);
                }
            }else{//没有满足条件的数据
                if(Credit.TARGET_CREDIT == type){//债权
                    creditManageHoldDto.setPageType(CreditManageHoldDto.TARGET_CREDIT);
                    return new RestResponseBuilder<>().success(creditManageHoldDto);
                }else {//散标
                    creditSubjectDto.setPageType(CreditManageHoldDto.TARGET_SUBJECT);
                    return new RestResponseBuilder<>().success(creditSubjectDto);
                }
            }
        } else {
            if(Credit.TARGET_CREDIT == type){//债权
                creditManageHoldDto.setPageType(CreditManageHoldDto.TARGET_CREDIT);
                return new RestResponseBuilder<>().success(creditManageHoldDto);
            }else {//散标
                creditSubjectDto.setPageType(CreditManageHoldDto.TARGET_SUBJECT);
                return new RestResponseBuilder<>().success(creditSubjectDto);
            }
        }
    }
}
