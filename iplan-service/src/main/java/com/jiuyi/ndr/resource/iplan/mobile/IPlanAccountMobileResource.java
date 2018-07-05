package com.jiuyi.ndr.resource.iplan.mobile;

import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanRepayDetail;
import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import com.jiuyi.ndr.domain.redpacket.InvestRedpacket;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.dto.config.InvestJumpConfig;
import com.jiuyi.ndr.dto.iplan.mobile.BaseIPlanDto;
import com.jiuyi.ndr.dto.iplan.mobile.IPlanAppInvestedShareDto;
import com.jiuyi.ndr.dto.iplan.mobile.IPlanAppRepayDetailDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.service.config.ConfigService;
import com.jiuyi.ndr.service.drpay.DrpayService;
import com.jiuyi.ndr.service.drpay.MarketingResponse;
import com.jiuyi.ndr.service.iplan.*;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.user.ActivityMarkConfigureService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.DesUtil;
import com.jiuyi.ndr.util.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lixiaolei       on 2017/6/15.
 */
@RestController
public class IPlanAccountMobileResource {

    private final static Logger logger = LoggerFactory.getLogger(IPlanAccountMobileResource.class);

    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private IPlanRepayDetailService iPlanRepayDetailService;
    @Autowired
    private ActivityMarkConfigureService activityMarkConfigureService;
    @Autowired
    private IPlanTransLogService iPlanTransLogService;
    @Autowired
    private IPlanParamService iPlanParamService;
    @Autowired
    private UserService userService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private SubjectService subjectService;

    @Value("${duanrong.invest.shareLinkUrl}")
    private String investShareLinkUrl;   //投资加权收益分享链接

    @Value("${duanrong.invest.tyjUrl}")
    private String tyjUrl;//体验金地址

    @Value("${duanrong.invest.successUrl}")
    private String succesUrl;//iplan投资完成页面

    private DecimalFormat df = new DecimalFormat("######0.##");

    /**
     * 投资
     *
     * @param userId    用户id
     * @param iPlanId   理财计划id
     * @param amount    投资金额（分）
     * @param device    设备
     * @param params    map参数
     */
    @PostMapping("/authed/{userId}/invest/{iPlanId}/{amount}")
    public RestResponse invest(@PathVariable("userId") String userId,
                               @PathVariable("iPlanId") int iPlanId,
                               @PathVariable("amount") int amount,
                               @RequestParam(value = "requestSource", required = false) String device,
                               @RequestBody Map<String, String> params) {
        IPlan iPlan = iPlanService.getIPlanById(iPlanId);
        Integer redPackedId = 0;
        if (params.containsKey("redPackedId")) {
            redPackedId = Integer.valueOf(params.get("redPackedId"));
        }
        String investRequestNo =null;
        List<String> DOUBLE_11_IPLAN = redisClient.getVByList(GlobalConfig.DOUBLE_11_IPLAN,0,-1);
        if (DOUBLE_11_IPLAN.contains(String.valueOf(iPlanId))){
            investRequestNo = iPlanAccountService.snathInvest(userId,iPlanId,amount,redPackedId,device);
        } else {
            investRequestNo = iPlanAccountService.invest(userId, iPlanId, amount, redPackedId, device, 0);
        }
        IPlanAppInvestedShareDto iPlanAppInvestedShareDto = new IPlanAppInvestedShareDto();
        iPlanAppInvestedShareDto.setShares(userService.getUserById(userId).getMobileNumber());
        String mobile = "";
        if (org.apache.commons.lang3.StringUtils.isNotBlank(userId)) {
            User user = userService.getUserById(userId);
            mobile = user != null ? user.getMobileNumber() : "";
        }
        IPlanAppInvestedShareDto.Share share = new IPlanAppInvestedShareDto.Share();

        Map<String, Object> map = new HashMap<>();
        map.put("source", MarketingResponse.SOURCE_APP);
        map.put("invest_type", MarketingResponse.INVEST_TYPE_IPLAN);
        map.put("userId", org.apache.commons.lang3.StringUtils.isNotBlank(userId) ? userId : "");
        map.put("extSn", investRequestNo);
        MarketingResponse response = null;
        try {
            response =MarketingResponse.toGeneratorJSON(DrpayService.send(succesUrl, map));
        } catch (Exception e) {
            logger.warn("营销系统异常", map.toString());
        }
        if (response != null && MarketingResponse.SUCCESS.equals(response.getErrcode()) && response.getData() != null) {
            MarketingResponse.MarketingInvestSuccess investSuccess = response.getData();
            // 18日加薪
            share.setType(IPlanAppInvestedShareDto.Share.TYPE_18);
            share.setTitle(investSuccess.getPicture_title());
            share.setDesc(investSuccess.getPicture_content());
            share.setButtonName(investSuccess.getButton_name());
            share.setPictureUrl(investSuccess.getPicture_url());
            share.setButtonUrl(investSuccess.getButton_url() + "?userId=" + DesUtil.encode(userId));
            share.setIplanInvestSuccessDesc("");

            share.setShareTitle(investSuccess.getShare_title());
            share.setShareDesc(investSuccess.getShare_content());
            share.setSharePicture(investSuccess.getShare_url());
            share.setShareUrl(investSuccess.getButton_url());

            InvestJumpConfig investJumpConfig = configService.getInvestJumpConfig(amount);
            if (investJumpConfig != null && investJumpConfig.getJumpSwitch() == IPlanAppInvestedShareDto.Share.DIRECT_JUMP_FLAG_Y) {
                share.setDirectJumpFlag(IPlanAppInvestedShareDto.Share.DIRECT_JUMP_FLAG_Y);
                share.setDirectJumpUrl(investJumpConfig.getJumpUrl());
            }

        } else {
        /*}
        Config dayOfAct = configService.getConfigById("invest_activity_day");
        Integer day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        if (org.apache.commons.lang3.StringUtils.contains(dayOfAct.getValue(), day.toString())) {
            // 18日加薪
            share.setType(IPlanAppInvestedShareDto.Share.TYPE_18);
            share.setTitle(configService.getConfigById("invest_activity_title") != null ?
                    configService.getConfigById("invest_activity_title").getValue() : "");
            share.setDesc(configService.getConfigById("invest_activity_msg") != null ?
                    configService.getConfigById("invest_activity_msg").getValue() : "");
            share.setButtonName("立即参与");
            share.setPictureUrl("活动图片");
            share.setButtonUrl(configService.getConfigById("invest_activity_url") != null ?
                    configService.getConfigById("invest_activity_url").getValue() : "");
            share.setIplanInvestSuccessDesc("");
        } else {*/
            // 加权收益
            share.setType(IPlanAppInvestedShareDto.Share.TYPE_WEIGHT);
            share.setTitle("恭喜您，投资成功！");
            share.setDesc("1、点击“收益再加3%”按钮进行分享。\n" +
                    "2、好友通过分享链接完成注册，即可在原有收益增加3天3%收益。\n" +
                    "3、好友注册成功，现金奖励发放至账户余额。");
            share.setButtonName("收益再加3%");
            share.setShareTitle("推荐一个理财神器，笔笔投资可额外增加收益");
            share.setShareDesc("现在加入，立领360元现金红包，专享12%年化收益");
            share.setSharePicture("http://duanrongweb.oss-cn-qingdao.aliyuncs.com/app/share/jxsh.png");
            String shareUrl = investShareLinkUrl + "?recordId=" + investRequestNo + "&recordTable=iplan&referrer=" + mobile;
            share.setShareUrl(shareUrl);

            InvestJumpConfig investJumpConfig = configService.getInvestJumpConfig(amount);
            if (investJumpConfig != null && investJumpConfig.getJumpSwitch() == IPlanAppInvestedShareDto.Share.DIRECT_JUMP_FLAG_Y) {
                share.setDirectJumpFlag(IPlanAppInvestedShareDto.Share.DIRECT_JUMP_FLAG_Y);
                share.setDirectJumpUrl(investJumpConfig.getJumpUrl());
            }

            /**
             a)	在用户使用按天加息券的时候显示：“您本次投资使用X天X%加息券，预计获得xx元收益。您可以邀请好友收益再增加3%收益”；
             b)	在用户使用加息券的时候显示：“您本次投资使用X%加息券。预计获得xx元收益。 您可以邀请好友收益再增加3%收益”；
             c)	在用户使用抵扣券时候显示：“您本次投资使用30元抵扣券，预计获得xx元收益。您可以邀请好友收益再增加3%收益”；
             d)	在用户使用现金券时候显示：“您本次投资使用30元现金券，预计获得xx元收益。您可以邀请好友收益再增加3%收益”；
             e)	在用户没有使用任何券时候显示：“本次投资预计获得xx元收益。您可以邀请好友收益再增加3%收益”；
             备注 VIP投资在每种情况前增加  尊贵的VIP会员，
             */
            String iplanInvestSuccessDesc = "";
            double money = 0;
            IPlanAccount iPlanAccount = iPlanAccountService.getIPlanAccount(userId, iPlanId);
            double vipRate = (iPlanAccount != null && iPlanAccount.getVipRate() != null) ? iPlanAccount.getVipRate().doubleValue() : 0;
            if (vipRate > 0) {
                iplanInvestSuccessDesc += "尊贵的VIP会员，";
            }
            double fixRate = (iPlan != null && iPlan.getFixRate() != null) ? iPlan.getFixRate().doubleValue() : 0;
            double bonusRate = (iPlan != null && iPlan.getBonusRate() != null) ? iPlan.getBonusRate().doubleValue() : 0;
            double totalRate = fixRate + bonusRate + vipRate;
            int term = iPlan != null ? iPlan.getTerm() : 0;
            double profit = 0.0;
            int interestAccrualType = iPlan.getInterestAccrualType();
            //todo 取省心投标的最短期限(随心投修改-jgx-5.16)
            int minTerm = iPlanAccountService.getYjtMinTerm(iPlan);
            if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                profit = subjectService.getInterestByRepayType(amount, BigDecimal.valueOf(totalRate), iPlanAccountService.getRate(iPlan),
                        minTerm, minTerm * 30, iPlan.getRepayType());
                ActivityMarkConfigure amc = activityMarkConfigureService.findById(iPlan.getActivityId());
                if(amc.getIncreaseTerm() != null){
                    profit = (iPlanAccount.getExpectedInterest() + iPlanAccount.getIplanExpectedBonusInterest())/100.0;
                }
            }else {
                profit = iPlanAccountService.calInterest(interestAccrualType, amount / 100, totalRate, iPlan);
            }
            iplanInvestSuccessDesc = iPlanAccountService.getInvestInfer(amount, redPackedId, iplanInvestSuccessDesc, money, iPlan,profit, minTerm);
            if(!IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                String jljMsg = "领取奖励金获得更多奖励！";
                if (amount < IPlanAppInvestedShareDto.IPLAN_INVEST_AMT) {
                    jljMsg = "投资大于500可领取奖励金哦~";
                }
                iplanInvestSuccessDesc += jljMsg;
            }
            share.setIplanInvestSuccessDesc(iplanInvestSuccessDesc);
        }
        iPlanAppInvestedShareDto.setShareMsg(share);
        iPlanAppInvestedShareDto.setTyjUrl(tyjUrl);
        // 公式：0.03/365*投资金额*3天=最小5元、最大不超过20
        double tyjAmt = ArithUtil.round(0.03 / 365 * amount / 100.0 * 3, 2);
        String tyjFlag = IPlanAppInvestedShareDto.TYJ_FLAG_N;
        if (tyjAmt < 5) {
            tyjAmt = 5;
        }
        if (tyjAmt > 20) {
            tyjAmt = 20;
        }
        if (amount >= IPlanAppInvestedShareDto.IPLAN_INVEST_AMT) {
            tyjFlag = IPlanAppInvestedShareDto.TYJ_FLAG_Y;
            if(IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                tyjFlag = "0";
            }
        }
        iPlanAppInvestedShareDto.setTyjFlag(tyjFlag);
        iPlanAppInvestedShareDto.setTyjMsg("恭喜您！获得" + tyjAmt + "元奖励金");
        return new RestResponseBuilder<IPlanAppInvestedShareDto>().success(iPlanAppInvestedShareDto);
    }


    @PostMapping("/authed/iplan/rechargeAndinvest")
    public RestResponse<Object> rechargeAndinvest(@RequestBody Map<String, String> params) {
        Integer redPacketId = 0;
        String device = "";
        String userId = "";
        int iPlanId = 0;
        int amount = 0;
        int autoInvest = 0;
        int transLogId = Integer.valueOf(params.get("transLogId"));
        if (transLogId <= 0) {
            device = params.get("device");
            userId = params.get("userId");
            iPlanId = Integer.valueOf(params.get("iPlanId"));
            List<String> DOUBLE_11_IPLAN = redisClient.getVByList(GlobalConfig.DOUBLE_11_IPLAN,0,-1);
            if (DOUBLE_11_IPLAN.contains(String.valueOf(iPlanId))){
                throw new ProcessException(Error.NDR_0463);
            }
            amount = Integer.valueOf(params.get("amount"));
            autoInvest = Integer.valueOf(params.get("autoInvest"));
            redPacketId = Integer.valueOf(params.get("redPacketId"));
            if (!StringUtils.hasText(device)) {
                throw new IllegalArgumentException("device不能为空");
            }
            if (!StringUtils.hasText(userId)) {
                throw new IllegalArgumentException("userId不能为空");
            }
            if (iPlanId == 0) {
                throw new IllegalArgumentException("iPlanId不能为空");
            }
            if (amount == 0) {
                throw new IllegalArgumentException("amount不能为空");
            }
        }

        Map<String, Object> map = new HashMap<>();
        Object data = iPlanAccountService.rechargeAndInvest(userId, iPlanId, amount, redPacketId, device, autoInvest, transLogId, "quick");
        map.put("data", data);
        map.put("rechargeFlag", "1");//去充值
        if (data == null) {
            map.put("rechargeFlag", "0");//投资成功，不充值
        }
        return new RestResponseBuilder<>().success(map);
    }

    @PostMapping("/authed/rechargeAndInvestCancel")
    public RestResponse rechargeAndInvestCancel(@RequestBody Map<String, String> params) {
        int transLogId = Integer.valueOf(params.get("transLogId"));
        if (transLogId == 0) {
            throw new IllegalArgumentException("transLogId can not be null");
        }
        iPlanAccountService.rechargeAndInvestCancel(transLogId);
        return new RestResponseBuilder<>().success(null);
    }

    /**
     * 提前退出
     */
    @PostMapping("/authed/{userId}/advanceExit/{iPlanId}")
    public RestResponse<IPlanAppInvestedShareDto> advanceExit(@PathVariable("userId") String userId,
                                                              @PathVariable("iPlanId") int iPlanId,
                                                              @RequestParam(value = "requestSource", required = false) String device) {
        iPlanAccountService.advanceExit(userId, iPlanId, device);
        IPlanAppInvestedShareDto iPlanAppInvestedShareDto = new IPlanAppInvestedShareDto();
        iPlanAppInvestedShareDto.setShares(userService.getUserById(userId).getMobileNumber());
        return new RestResponseBuilder<IPlanAppInvestedShareDto>().success(iPlanAppInvestedShareDto);
    }

    @GetMapping("authed/{userId}/{iPlanId}/repaySchedule")
    public RestResponse<IPlanAppRepayDetailDto> getPlanRePaySchedule(@PathVariable("userId") String userId,
                                                                     @PathVariable("iPlanId") Integer iPlanId) {
        IPlanAppRepayDetailDto iPlanRepayScheduleDto = new IPlanAppRepayDetailDto();
        List<IPlanAppRepayDetailDto.AppRepaySchedule> appRepaySchedules = new ArrayList<>();
        IPlan iPlan = iPlanService.getIPlanById(iPlanId);
        IPlanAccount iPlanAccount = iPlanAccountService.getIPlanAccount(userId, iPlanId);
        if (iPlanAccount == null) {
            logger.warn("用户：" + userId + "在" + iPlanId + "不存在");
            throw new ProcessException(Error.NDR_0452.getCode(), Error.NDR_0452.getMessage() + " userId:" + userId + ", iPlanId" + iPlanId);
        }
        InvestRedpacket investRedpacket = redPacketService.getReceivedRedPacketAmt(userId, String.valueOf(iPlanId), RedPacket.INVEST_REDPACKET_TYPE);
        if (investRedpacket != null && investRedpacket.getRewardMoney() != 0 && investRedpacket.getSendRedpacketTime() != null) {
            iPlanRepayScheduleDto.setRedPacketDesc("红包券收益:" + ArithUtil.round(investRedpacket.getRewardMoney(), 2) + "元");
            iPlanRepayScheduleDto.setRedPacketDate(DateUtil.SDF_10.format(investRedpacket.getSendRedpacketTime()));
        }
        iPlanRepayScheduleDto.setRepayType(iPlan.getRepayType());
        //还款方式新增一次性还本付息
        String calcInterestDate = DateUtil.getDateStr(DateUtil.parseDate(iPlanAccount.getCreateTime().substring(0, 10), DateUtil.DATE_TIME_FORMATTER_10).plusDays(1), DateUtil.DATE_TIME_FORMATTER_10);
        iPlanRepayScheduleDto.setCalcInterestDate(calcInterestDate);
        //List<IPlanRepaySchedule> iPlanRepaySchedules = iPlanRepayScheduleService.getRepaySchedule(iPlanId);
        List<IPlanRepayDetail> iPlanRepayDetails = iPlanRepayDetailService.getByUserIdAndIPlanId(userId, iPlanId);
        for (IPlanRepayDetail repayDetail : iPlanRepayDetails) {
            IPlanAppRepayDetailDto.AppRepaySchedule appRepaySchedule = new IPlanAppRepayDetailDto.AppRepaySchedule();
            String repayStatusStr = "未回收";
            String repayTypeStr = "利息";
            appRepaySchedule.setDueDate(repayDetail.getDueDate());
            appRepaySchedule.setStatus(repayDetail.getStatus());
            appRepaySchedule.setInterest(String.valueOf((repayDetail.getDueInterest() + repayDetail.getDueBonusInterest() + repayDetail.getDueVipInterest()) / 100.0));
            appRepaySchedule.setVipInterest(df.format(repayDetail.getDueVipInterest()/100.0));
            appRepaySchedule.setTerm(repayDetail.getTerm());
            if (repayDetail.getTerm().equals(iPlanRepayDetails.size())) {
                repayTypeStr = "本息";
                appRepaySchedule.setInterest(String.valueOf((repayDetail.getDuePrincipal() + repayDetail.getDueInterest() + repayDetail.getDueBonusInterest() + repayDetail.getDueVipInterest()) / 100.0));
            }
            if (!IPlanRepayDetail.STATUS_NOT_REPAY.equals(repayDetail.getStatus())) {
                repayStatusStr = "回收";
                appRepaySchedule.setInterest(String.valueOf((repayDetail.getRepayInterest() + repayDetail.getRepayBonusInterest()) / 100.0));
                if (repayDetail.getTerm().equals(iPlanRepayDetails.size())) {
                    repayTypeStr = "本息";
                    appRepaySchedule.setInterest(String.valueOf((repayDetail.getRepayPrincipal() + repayDetail.getRepayInterest() + repayDetail.getRepayBonusInterest()) / 100.0));
                }
            }

            // 月月盈提前还款
            if (IPlanRepayDetail.STATUS_REPAY_CLEAN.equals(repayDetail.getStatus())) {
                double beforeRepayPrincipal = repayDetail.getRepayPrincipal() != null ? repayDetail.getRepayPrincipal().doubleValue() : 0;
                double beforeRepayInterest = repayDetail.getRepayInterest() != null ? repayDetail.getRepayInterest().doubleValue() : 0;
                double beforeRepayBonusInterest = repayDetail.getRepayBonusInterest() != null ? repayDetail.getRepayBonusInterest().doubleValue() : 0;
                appRepaySchedule.setBeforeRepayFlag(BaseIPlanDto.BEFORE_REPAY_FLAG_Y);
                appRepaySchedule.setBeforeRepayContent("提前回款金额" + df.format(beforeRepayPrincipal+beforeRepayInterest+beforeRepayBonusInterest)
                        + "，本金" + df.format(beforeRepayPrincipal) + "，利息" + df.format(beforeRepayInterest) + "，项目加息" + df.format(beforeRepayBonusInterest));
            }

            appRepaySchedule.setRepayContent("第"+appRepaySchedule.getTerm()+"期"+repayStatusStr+repayTypeStr+appRepaySchedule.getInterest()+"元");
            appRepaySchedules.add(appRepaySchedule);
        }
        iPlanRepayScheduleDto.setAppRepaySchedules(appRepaySchedules);
        return new RestResponseBuilder<IPlanAppRepayDetailDto>().success(iPlanRepayScheduleDto);
    }

}
