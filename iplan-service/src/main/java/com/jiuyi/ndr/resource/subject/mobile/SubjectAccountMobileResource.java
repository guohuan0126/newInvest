package com.jiuyi.ndr.resource.subject.mobile;

import com.duanrong.util.InterestUtil;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectAccount;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.dto.config.InvestJumpConfig;
import com.jiuyi.ndr.dto.iplan.mobile.IPlanAppInvestedShareDto;
import com.jiuyi.ndr.dto.subject.mobile.SubjectAppInvestedShareDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.service.config.ConfigService;
import com.jiuyi.ndr.service.drpay.DrpayService;
import com.jiuyi.ndr.service.drpay.MarketingResponse;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.util.DesUtil;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.service.user.ActivityMarkConfigureService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YUMIN on 2017/11/3.
 */
@RestController
public class SubjectAccountMobileResource {

    private final static Logger logger = LoggerFactory.getLogger(SubjectAccountMobileResource.class);

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

    @Value("${duanrong.invest.shareLinkUrl}")
    private String investShareLinkUrl;   //投资加权收益分享链接

    @Value("${duanrong.invest.tyjUrl}")
    private String tyjUrl;//体验金地址

    @Value("${duanrong.invest.successUrl}")
    private String succesUrl;//subject投资完成页面

    private DecimalFormat df = new DecimalFormat("######0.##");

    /**
     * App投资
     *
     * @param userId    用户id
     * @param subjectId   id
     * @param params    map参数
     */
    @PostMapping("/authed/subject/{userId}/invest/{subjectId}")
    public RestResponse invest(@PathVariable("userId") String userId,
                               @PathVariable("subjectId") String subjectId,
                               @RequestBody Map<String, String> params) {
        Integer redPackedId = 0;
        if (params.containsKey("redPackedId")) {
            redPackedId = Integer.valueOf(params.get("redPackedId"));
        }
        int amount = 0;
        if (!StringUtils.hasText(params.get("amount"))) {

            throw new IllegalArgumentException("amount不能为空");
        }
        amount =Integer.parseInt(params.get("amount"));
        String device="";
        if (params.containsKey("requestSource")) {
            device = params.get("requestSource");
        }
        String investRequestNo = subjectAccountService.investSubject(userId,subjectId,amount,redPackedId,device, 0);
        SubjectAppInvestedShareDto subjectAppInvestedShareDto = new SubjectAppInvestedShareDto();
        subjectAppInvestedShareDto.setShares(userService.getUserById(userId).getMobileNumber());
        String mobile = "";
        if (org.apache.commons.lang3.StringUtils.isNotBlank(userId)) {
            User user = userService.getUserById(userId);
            mobile = user != null ? user.getMobileNumber() : "";
        }
        SubjectAppInvestedShareDto.Share share = new SubjectAppInvestedShareDto.Share();
        double iPlanInvestAmt = 50000;
        Map<String, Object> map = new HashMap<>();
        map.put("source", MarketingResponse.SOURCE_APP);
        map.put("invest_type", MarketingResponse.INVEST_TYPE_INVEST);
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
            share.setType(SubjectAppInvestedShareDto.Share.TYPE_18);
            share.setTitle(investSuccess.getPicture_title());
            share.setDesc(investSuccess.getPicture_content());
            share.setButtonName(investSuccess.getButton_name());
            share.setPictureUrl(investSuccess.getPicture_url());
            share.setButtonUrl(investSuccess.getButton_url() + "?userId=" + DesUtil.encode(userId));
            share.setSubjectInvestSuccessDesc("");

            share.setShareTitle(investSuccess.getShare_title());
            share.setShareDesc(investSuccess.getShare_content());
            share.setSharePicture(investSuccess.getShare_url());
            share.setShareUrl(investSuccess.getButton_url());
            share.setActivityUrl(investSuccess.getButton_url());

            InvestJumpConfig investJumpConfig = configService.getInvestJumpConfig(amount);
            if (investJumpConfig != null && investJumpConfig.getJumpSwitch() == IPlanAppInvestedShareDto.Share.DIRECT_JUMP_FLAG_Y) {
                share.setDirectJumpFlag(IPlanAppInvestedShareDto.Share.DIRECT_JUMP_FLAG_Y);
                share.setDirectJumpUrl(investJumpConfig.getJumpUrl());
            }

        } else {
            share.setType(SubjectAppInvestedShareDto.Share.TYPE_WEIGHT);
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
            String subjectInvestSuccessDesc = "";
            double money = 0;
            //根据请求流水号查询标的投资账户
            SubjectAccount subjectAccount = subjectAccountService.getSubjectAccountByRequestNo(investRequestNo);

            double vipRate = (subjectAccount != null && subjectAccount.getVipRate() != null) ? subjectAccount.getVipRate().doubleValue() : 0;
            if (vipRate > 0) {
                subjectInvestSuccessDesc += "尊贵的VIP会员，";
            }
            Subject subject = subjectService.findSubjectBySubjectId(subjectId);
            if (subject == null) {
                throw new ProcessException(Error.NDR_0403);
            }

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

            int totalMoney = expectedInterest + expectedBonusInterest;
            subjectInvestSuccessDesc+=subjectAccountService.getRedPacketInterest(subject,totalMoney,redPackedId,amount);

            String jljMsg = "领取奖励金获得更多奖励！";
            if (amount < iPlanInvestAmt) {
                jljMsg = "投资大于500可领取奖励金哦~";
            }
            subjectInvestSuccessDesc += jljMsg;
            share.setSubjectInvestSuccessDesc(subjectInvestSuccessDesc);
        }
        subjectAppInvestedShareDto.setShareMsg(share);
        subjectAppInvestedShareDto.setTyjUrl(tyjUrl);
        // 公式：0.03/365*投资金额*3天=最小5元、最大不超过20
        double tyjAmt = ArithUtil.round(0.03 / 365 * amount / 100.0 * 3, 2);
        String tyjFlag = SubjectAppInvestedShareDto.TYJ_FLAG_N;
        if (tyjAmt < 5) {
            tyjAmt = 5;
        }
        if (tyjAmt > 20) {
            tyjAmt = 20;
        }
        if (amount >= iPlanInvestAmt) {
            tyjFlag = SubjectAppInvestedShareDto.TYJ_FLAG_Y;
        }
        subjectAppInvestedShareDto.setTyjFlag(tyjFlag);
        subjectAppInvestedShareDto.setTyjMsg("恭喜您！获得" + tyjAmt + "元奖励金");
        return new RestResponseBuilder<SubjectAppInvestedShareDto>().success(subjectAppInvestedShareDto);
    }
    /**
     * 散标充值并投资
     * @param params
     * @return
     */
    @PostMapping("/authed/subject/subjectRechargeAndInvest")
    public RestResponse<Object> subjectRechargeAndInvest(@RequestBody Map<String, String> params) {
        Integer redPacketId = 0;
        String device = "";
        String userId = "";
        String subjectId ="";
        int amount = 0;
        int autoInvest = 0;
        int transLogId = 0;
        if(!StringUtils.isEmpty(params.get("transLogId"))){
            transLogId = Integer.valueOf(params.get("transLogId").toString());
        }
        if (transLogId <= 0) {
            device = params.get("device");
            userId = params.get("userId");
            subjectId = params.get("subjectId");
            amount = Integer.valueOf(params.get("amount"));
            autoInvest = Integer.valueOf(params.get("autoInvest"));
            redPacketId = Integer.valueOf(params.get("redPacketId"));
            if (!StringUtils.hasText(device)) {
                throw new IllegalArgumentException("device不能为空");
            }
            if (!StringUtils.hasText(userId)) {
                throw new IllegalArgumentException("userId不能为空");
            }
            if (!StringUtils.hasText(subjectId)) {
                throw new IllegalArgumentException("subjectId不能为空");
            }
            if (amount == 0) {
                throw new IllegalArgumentException("amount不能为空");
            }
        }

        Map<String, Object> map = new HashMap<>();
        Object data =subjectAccountService.subjectRechargeAndInvest(userId, subjectId, amount,
                redPacketId, device, autoInvest, transLogId, "quick");
        map.put("data", data);
        map.put("rechargeFlag", "1");//去充值
        if (data == null) {
            map.put("rechargeFlag", "0");//投资成功，不充值
        }
        return new RestResponseBuilder<>().success(map);
    }

    /**
     *  充值并投资取消接口
     * @param params
     * @return
     */
    @PostMapping("/authed/subjectRechargeAndInvestCancel")
    public RestResponse subjectRechargeAndInvestCancel(@RequestBody Map<String, String> params) {
        int transLogId = Integer.valueOf(params.get("transLogId"));
        if (transLogId == 0) {
            throw new IllegalArgumentException("transLogId can not be null");
        }
        subjectAccountService.subjectRechargeAndInvestCancel(transLogId);
        return new RestResponseBuilder<>().success(null);
    }
}
