package com.jiuyi.ndr.resource.credit;

import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectAccount;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.domain.subject.SubjectTransferParam;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.dto.credit.mobile.CreditAppInvestDetailDto;
import com.jiuyi.ndr.dto.credit.mobile.CreditAppInvestedShareDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.resource.subject.mobile.SubjectMobileResource;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.config.ConfigService;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.drpay.DrpayService;
import com.jiuyi.ndr.service.drpay.MarketingResponse;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.subject.SubjectTransferParamService;
import com.jiuyi.ndr.service.user.ActivityMarkConfigureService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.DesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YUMIN on 2017/11/4.
 */
@RestController
public class SubjectCreditMobileResource {

    private final static Logger logger = LoggerFactory.getLogger(SubjectCreditMobileResource.class);

    @Autowired
    private SubjectAccountService subjectAccountService;

    @Autowired
    private ActivityMarkConfigureService activityMarkConfigureService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private UserService userService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private CreditOpeningService creditOpeningService;
    @Autowired
    private CreditService creditService;
    @Autowired
    private SubjectTransferParamService subjectTransferParamService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private CreditOpeningDao creditOpeningDao;

    @Value("${duanrong.invest.shareLinkUrl}")
    private String investShareLinkUrl;   //投资加权收益分享链接

    @Value("${duanrong.invest.tyjUrl}")
    private String tyjUrl;//体验金地址

    @Value("${duanrong.invest.successUrl}")
    private String succesUrl;//subject投资完成页面

    @Value("${duanrong.subject.transferUrl}")
    private String transferUrl;//转让协议
    @Value("${duanrong.subject.riskProtocolUrl}")
    private String riskProtocolUrl;//风险协议

    private DecimalFormat df = new DecimalFormat("######0.00");
    private DecimalFormat df2 = new DecimalFormat("######0");
    private static DecimalFormat df3 = new DecimalFormat("0.####");
    private DecimalFormat df4 = new DecimalFormat("######0.##");
    /**
     * App购买债权
     *
     * @param userId    用户id
     * @param openingCreditId   开放债权id
     */
    @PostMapping("/authed/app/credit/{userId}/investCredit/{openingCreditId}")
    public RestResponse invest(@PathVariable("userId") String userId,
                               @PathVariable("openingCreditId") int openingCreditId,
                               @RequestBody Map<String, String> params) {
        Integer redPackedId = 0;
        if (params.containsKey("redPackedId") && org.apache.commons.lang3.StringUtils.isNotBlank(params.get("redPackedId"))) {
            redPackedId = Integer.valueOf(params.get("redPackedId"));
        }
        int principal = 0;
        if (!StringUtils.hasText(params.get("principal"))) {
            throw new IllegalArgumentException("principal不能为空");
        }
        principal =Integer.parseInt(params.get("principal"));
        double discount = 0;
        if (!StringUtils.hasText(params.get("discount"))) {
            throw new IllegalArgumentException("discount不能为空");
        }
        discount =Double.valueOf(params.get("discount"));
        String device="";
        if (params.containsKey("requestSource")) {
            device = params.get("requestSource");
        }
        //实际支付金额=购买金额*折扣率
        double actualPrincipal = principal*(creditOpeningDao.findById(openingCreditId).getTransferDiscount().doubleValue());;
        String investRequestNo = subjectAccountService.investSubjectCredit(openingCreditId,principal,
                actualPrincipal,userId,device,redPackedId);
        CreditAppInvestedShareDto creditAppInvestedShareDto = new CreditAppInvestedShareDto();
        creditAppInvestedShareDto.setShares(userService.getUserById(userId).getMobileNumber());
        String mobile = "";
        if (org.apache.commons.lang3.StringUtils.isNotBlank(userId)) {
            User user = userService.getUserById(userId);
            mobile = user != null ? user.getMobileNumber() : "";
        }
        CreditAppInvestedShareDto.Share share = new CreditAppInvestedShareDto.Share();
        double iPlanInvestAmt = 50000;
        Map<String, Object> map = new HashMap<>();
        map.put("source", MarketingResponse.SOURCE_APP);
        map.put("invest_type", MarketingResponse.INVEST_TYPE_CREDIT);
        map.put("userId", org.apache.commons.lang3.StringUtils.isNotBlank(userId) ? userId : "");
        map.put("extSn", investRequestNo);
        MarketingResponse response = null;
        try {
            response = MarketingResponse.toGeneratorJSON(DrpayService.send(succesUrl, map));
        } catch (Exception e) {
            logger.warn("营销系统异常", map.toString());
        }
        if (response != null && MarketingResponse.SUCCESS.equals(response.getErrcode()) && response.getData() != null) {
            MarketingResponse.MarketingInvestSuccess investSuccess = response.getData();
            // 18日加薪
            share.setType(CreditAppInvestedShareDto.Share.TYPE_18);
            share.setTitle(investSuccess.getPicture_title());
            share.setDesc(investSuccess.getPicture_content());
            share.setButtonName(investSuccess.getButton_name());
            share.setPictureUrl(investSuccess.getPicture_url());
            share.setButtonUrl(investSuccess.getButton_url() + "?userId=" + DesUtil.encode(userId));
            share.setCreditInvestSuccessDesc("");

            share.setShareTitle(investSuccess.getShare_title());
            share.setShareDesc(investSuccess.getShare_content());
            share.setSharePicture(investSuccess.getShare_url());
            share.setShareUrl(investSuccess.getButton_url());
            share.setActivityUrl(investSuccess.getButton_url());
        } else {
            share.setType(CreditAppInvestedShareDto.Share.TYPE_WEIGHT);
            share.setTitle("恭喜您，加入成功！");
            share.setDesc("1、点击“收益再加3%”按钮进行分享。\n" +
                    "2、好友通过分享链接完成注册，即可在原有收益增加3天3%收益。\n" +
                    "3、好友注册成功，现金奖励发放至账户余额。");
            share.setButtonName("收益再加3%");
            share.setShareTitle("推荐一个理财神器，笔笔投资可额外增加收益");
            share.setShareDesc("现在加入，立领360元现金红包，专享12%年化收益");
            share.setSharePicture("http://duanrongweb.oss-cn-qingdao.aliyuncs.com/app/share/jxsh.png");
            String shareUrl = investShareLinkUrl + "?recordId=" + investRequestNo + "&recordTable=iplan&referrer=" + mobile;
            share.setShareUrl(shareUrl);
            /**
             a)	在用户使用按天加息券的时候显示：“您本次投资使用X天X%加息券，预计获得xx元收益。您可以邀请好友收益再增加3%收益”；
             b)	在用户使用加息券的时候显示：“您本次投资使用X%加息券。预计获得xx元收益。 您可以邀请好友收益再增加3%收益”；
             c)	在用户使用抵扣券时候显示：“您本次投资使用30元抵扣券，预计获得xx元收益。您可以邀请好友收益再增加3%收益”；
             d)	在用户使用现金券时候显示：“您本次投资使用30元现金券，预计获得xx元收益。您可以邀请好友收益再增加3%收益”；
             e)	在用户没有使用任何券时候显示：“本次投资预计获得xx元收益。您可以邀请好友收益再增加3%收益”；
             备注 VIP投资在每种情况前增加  尊贵的VIP会员，
             */
            String subjectInvestSuccessDesc = "";
            double money = 0;
            //根据请求流水号查询标的投资账户
            SubjectAccount subjectAccount = subjectAccountService.getSubjectAccountByRequestNo(investRequestNo);

            double vipRate = (subjectAccount != null && subjectAccount.getVipRate() != null) ? subjectAccount.getVipRate().doubleValue() : 0;
            if (vipRate > 0) {
                subjectInvestSuccessDesc += "尊贵的VIP会员，";
            }
            Subject subject = subjectService.findSubjectBySubjectId(subjectAccount.getSubjectId());
            if (subject == null) {
                throw new ProcessException(Error.NDR_0403);
            }
            /*double investRate = subject.getInvestRate() != null ? subject.getInvestRate().doubleValue() : 0;
            double bonusRate = subject.getBonusRate() != null ? subject.getBonusRate().doubleValue() : 0;
            double totalRate = investRate + bonusRate + vipRate;
            //期数
            int residualTerm = subject.getTerm()-subject.getCurrentTerm()+1;
            String repayType=subjectService.getSubjectRepayType(subject.getSubjectId());
            money += InterestUtil.getInterestByPeriodMoth(principal/100.0,totalRate,residualTerm,repayType);
            */
             //预期利息（分）
            int expectedInterest = (subjectAccount != null && subjectAccount.getExpectedInterest() != null) ? subjectAccount.getExpectedInterest() : 0;
            //预期加息利息
            int expectedBonusInterest = (subjectAccount != null && subjectAccount.getSubjectExpectedBonusInterest() != null) ? subjectAccount.getSubjectExpectedBonusInterest() : 0;
            //预期vip特权加息
            int expectedVipInterest = (subjectAccount != null && subjectAccount.getSubjectExpectedVipInterest() != null) ? subjectAccount.getSubjectExpectedVipInterest() : 0;
            //预期奖励
            int expectedReward =(subjectAccount != null && subjectAccount.getExpectedReward() != null) ? subjectAccount.getExpectedReward() : 0;

            //得到得到抵扣金额
            int  dedutionAmt =subjectAccount.getDedutionAmt() !=null ?subjectAccount.getDedutionAmt():0;

            int totalMoney = expectedInterest + expectedBonusInterest + expectedReward - dedutionAmt;
            money += totalMoney/100.0;

            subjectInvestSuccessDesc += "本次投资预计赚取" + df.format(money) + "元。";

            subjectInvestSuccessDesc += "您可以前往“我的-债转”中查看购买详情";
            share.setCreditInvestSuccessDesc(subjectInvestSuccessDesc);
        }
        creditAppInvestedShareDto.setShareMsg(share);
        // 公式：0.03/365*投资金额*3天=最小5元、最大不超过20
        double tyjAmt = ArithUtil.round(0.03 / 365 * principal / 100.0 * 3, 2);
        String tyjFlag = CreditAppInvestedShareDto.TYJ_FLAG_N;
        if (tyjAmt < 5) {
            tyjAmt = 5;
        }
        if (tyjAmt > 20) {
            tyjAmt = 20;
        }
        if (principal >= iPlanInvestAmt) {
            tyjFlag = CreditAppInvestedShareDto.TYJ_FLAG_Y;
        }
        creditAppInvestedShareDto.setTyjFlag(tyjFlag);
        creditAppInvestedShareDto.setTyjUrl(tyjUrl);
        creditAppInvestedShareDto.setTyjMsg("恭喜您！获得" + tyjAmt + "元奖励金");
        return new RestResponseBuilder<CreditAppInvestedShareDto>().success(creditAppInvestedShareDto);
    }
    /**
     * App债权抢购页
     *
     * @param userId    用户id
     * @param openingCreditId   开放债权id
     */
    @GetMapping("/authed/app/credit/{userId}/creditDetail/{openingCreditId}")
    public RestResponse invest(@PathVariable("userId") String userId,
                               @PathVariable("openingCreditId") Integer openingCreditId) {
        if (null == openingCreditId || org.apache.commons.lang3.StringUtils.isBlank(userId)) {
            throw new ProcessException(Error.NDR_0101);
        }
        CreditOpening creditOpening = creditOpeningService.getById(openingCreditId);
        if(creditOpening== null){
            logger.warn("不存在该开放中的债权,开放中债权ID为:"+openingCreditId);
            throw new ProcessException(Error.NDR_0202.getCode(),"不存在该开放中的债权,开放中债权ID为:"+openingCreditId);
        }
         Credit credit = creditService.getById(creditOpening.getCreditId());
        Subject subject = subjectService.getBySubjectId(creditOpening.getSubjectId());
        List<RedPacket> redPacketList = redPacketService.getUsablePacketCreditAll(userId, subject, "ios_4.6.0","credit");
        List<CreditAppInvestDetailDto.RedPacketApp> redPacketAppList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(redPacketList)) {
            for (RedPacket redPacket : redPacketList) {
                CreditAppInvestDetailDto.RedPacketApp redPacketApp = new CreditAppInvestDetailDto.RedPacketApp();
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
                redPacketApp.setUseStatus(redPacket.getUseStatus());
                redPacketApp.setInvestMoney(redPacket.getInvestMoney());
                redPacketAppList.add(redPacketApp);
            }
        }

        CreditAppInvestDetailDto creditAppInvestDetailDto = new CreditAppInvestDetailDto();
        creditAppInvestDetailDto.setRedPacketAppList(redPacketAppList);
        creditAppInvestDetailDto.setId(openingCreditId);
        creditAppInvestDetailDto.setName(subject.getName());
        BigDecimal totalRate = creditOpeningService.calcTotalRate(credit.getSubjectId());
        //原年利化率
        creditAppInvestDetailDto.setOldRate(totalRate.doubleValue());
        creditAppInvestDetailDto.setOldRateStr(df4.format(totalRate.doubleValue() * 100) + "%");

        //用户风险测评
        String whereAnswer = userService.getComplianceAnswer(userId);
        String setUpDesc = "";
        if(null == whereAnswer || ("").equals(whereAnswer)){
            creditAppInvestDetailDto.setWhereAnswer("false");

        }else{
            creditAppInvestDetailDto.setWhereAnswer("true");
            if("A".equals(whereAnswer)){
                setUpDesc = "积极型";
            }else if("B".equals(whereAnswer)){
                setUpDesc = "稳健型";
            }else {
                setUpDesc = "保守型";
            }
        }
        creditAppInvestDetailDto.setSetUpDesc(setUpDesc);

        //起投金额
        SubjectTransferParam transferParam = subjectTransferParamService.getByTransferParamCode(subject.getTransferParamCode());

        creditAppInvestDetailDto.setInvestMoney(transferParam.getPurchasingPriceMin());
        creditAppInvestDetailDto.setInvestMoneyStr(df4.format(transferParam.getPurchasingPriceMin()/100.0)+"元起投");
        //若剩余转让本金低于最低购买金额，需要剩余金额为最低起投金额
        if(creditOpening.getAvailablePrincipal()<transferParam.getPurchasingPriceMin()){
            creditAppInvestDetailDto.setInvestMoney(creditOpening.getAvailablePrincipal());
            creditAppInvestDetailDto.setInvestMoneyStr(df4.format(creditOpening.getAvailablePrincipal()/100.0)+"元需全部购买");
        }
         //剩余时间
        String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
        String endDate = credit.getEndTime().substring(0, 8);
        long days = DateUtil.betweenDays(currentDate, endDate);
        creditAppInvestDetailDto.setResidualDay((int) days);
        //剩余本金
        creditAppInvestDetailDto.setAvailablePrincipal(creditOpening.getAvailablePrincipal()/100.0);
        //折让比例
        creditAppInvestDetailDto.setDiscount(creditOpening.getTransferDiscount().doubleValue());
        creditAppInvestDetailDto.setDiscountStr(df4.format(creditAppInvestDetailDto.getDiscount() * 100)+"%");

        //下次回款时间
        SubjectRepaySchedule currentRepaySchedule = subjectRepayScheduleService.getCurrentRepaySchedule(subject.getSubjectId());
        creditAppInvestDetailDto.setNextRepayTime(DateUtil.parseDate(currentRepaySchedule.getDueDate(), DateUtil.DATE_TIME_FORMATTER_8).toString());
        creditAppInvestDetailDto.setIsOpenAccount(userAccountService.checkIfOpenAccount(userId) ? 1 : 0);
        //账户余额
        UserAccount userAccount = userAccountService.getUserAccountForUpdate(userId);
        creditAppInvestDetailDto.setAvailMoney(userAccount!=null?userAccount.getAvailableBalance():0.0);
        //债权转让协议
        creditAppInvestDetailDto.setCreditProtocolUrl(transferUrl);
        //网贷协议
        creditAppInvestDetailDto.setNetProtocolUrl(riskProtocolUrl);
        //是否开户
        creditAppInvestDetailDto.setRate(subject.getRate());
        creditAppInvestDetailDto.setRepayType(SubjectMobileResource.repayType(subject.getRepayType()));
        //新债权利率
        Double expectRate = creditOpeningService.calcNewRate(creditOpening.getSubjectId(), creditOpening);
        creditAppInvestDetailDto.setExpectRate(expectRate);
        creditAppInvestDetailDto.setExpectRateStr(df4.format(creditAppInvestDetailDto.getExpectRate() * 100)+"%");
        //剩余期限月
        int residualTerm = subject.getTerm()-subject.getCurrentTerm()+1;
        creditAppInvestDetailDto.setResidualTerm(residualTerm);

        return new RestResponseBuilder<CreditAppInvestDetailDto>().success(creditAppInvestDetailDto);
    }
}
