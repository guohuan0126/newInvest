package com.jiuyi.ndr.resource.subject;

import com.jiuyi.ndr.domain.subject.SubjectAccount;
import com.jiuyi.ndr.dto.iplan.mobile.IPlanAppInvestedShareDto;
import com.jiuyi.ndr.dto.subject.SubjectInvestDetailDto;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.util.ArithUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by YUMIN on 2017/11/3.
 */
@RestController
@RequestMapping(value = "/subject")
public class SubjectAccountResource {

    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private UserService userService;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private  SubjectService subjectService;

    private DecimalFormat df = new DecimalFormat("######0.##");

    private final static Logger logger = LoggerFactory.getLogger(SubjectAccountResource.class);


    /**
     * PC 投资散标
     * @param userId
     * @param subjectId
     * @param amount
     * @param params
     * @return
     */
    @PostMapping("/{userId}/invest/{subjectId}/{amount}")
    public RestResponse<SubjectInvestDetailDto> invest(
            @PathVariable("userId") String userId,
            @PathVariable("subjectId") String subjectId,
            @PathVariable("amount") int amount,
            @RequestBody Map<String, String> params){
        Integer redPackedId = 0;
        if (params.containsKey("redPackedId")) {
            redPackedId = Integer.valueOf(params.get("redPackedId"));
        }
        String device = params.get("device");
        if (!StringUtils.hasText(device)) {
            throw new IllegalArgumentException("device不能为空");
        }
        String  investRequestNo =subjectAccountService.investSubject(userId,subjectId,amount,redPackedId,device, 0);

        SubjectInvestDetailDto subjectInvestDetailDto = new SubjectInvestDetailDto();
        String iplanInvestSuccessDesc = "";
        double money = 0;
        //根据请求流水号查询标的投资账户
        SubjectAccount subjectAccount = subjectAccountService.getSubjectAccountByRequestNo(investRequestNo);
     /*
        double vipRate = (subjectAccount != null && subjectAccount.getVipRate() != null) ? subjectAccount.getVipRate().doubleValue() : 0;
        if (vipRate > 0) {
            iplanInvestSuccessDesc += "尊贵的VIP会员，";
        }
        Subject subject = subjectService.findSubjectBySubjectId(subjectId);*/
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
        int totalMoney = expectedInterest + expectedBonusInterest +expectedReward-dedutionAmt;
        money += totalMoney/100.0;
        iplanInvestSuccessDesc += "投资预计赚取" + df.format(money) + "元。";
        iplanInvestSuccessDesc += "您可以使用App邀请好友继续赚取更多奖励。";
        subjectInvestDetailDto.setTitle("加入成功");
        subjectInvestDetailDto.setInvestSuccessDesc(iplanInvestSuccessDesc);

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
        }
        subjectInvestDetailDto.setTyjFlag(tyjFlag);
        subjectInvestDetailDto.setTyjMsg("恭喜您！获得" + tyjAmt + "元奖励金");

        return new RestResponseBuilder<SubjectInvestDetailDto>().success(subjectInvestDetailDto);
    }

    /**
     * PC充值并投资
     * @param params
     * @return
     */
    @PostMapping("/toRechargeAndinvest")
    public RestResponse<Object> toRechargeAndinvest(@RequestBody Map<String, String> params) {
        String device = "";
        String userId = "";
        String subjectId= "";
        int amount = 0;
        int redPacketId = 0;
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
        Object data = subjectAccountService.subjectRechargeAndInvest(userId, subjectId, amount,
                redPacketId, device, autoInvest, transLogId, "quick");
        map.put("data", data);
        map.put("rechargeFlag", "1");//去充值
        if (data == null) {
            map.put("rechargeFlag", "0");//投资成功，不充值
        }
        return new RestResponseBuilder<>().success(map);
    }

    /**
     * 散标充值成功回调
     * @param params
     * @return
     */
    @PostMapping("/rechargeSuccessInvest")
    public RestResponse<Object> rechargeSuccessInvest(@RequestBody Map<String, String> params) {
        logger.info("散标充值并投资drpay回调请求参数{}",params);
        int transLogId = Integer.valueOf(params.get("transLogId"));
        String device = params.get("device");
        String userId = params.get("userId");
        String  subjectId = params.get("subjectId");
        int amount = Integer.valueOf(params.get("amount"));
        double actualAmt = Double.valueOf(params.get("actualAmt"));
        int redPacketId = Integer.valueOf(params.get("redPacketId"));
        int autoInvest = Integer.valueOf(params.get("autoInvest"));

        if (org.apache.commons.lang3.StringUtils.isBlank(device)) {
            throw new IllegalArgumentException("device不能为空");
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId不能为空");
        }
        if (!StringUtils.hasText(subjectId)) {
            throw new IllegalArgumentException("subjectId不能为空");
        }
        if (amount == 0) {
            throw new IllegalArgumentException("amount不能为空");
        }
        if (actualAmt == 0) {
            throw new IllegalArgumentException("actualAmt不能为空");
        }
        subjectAccountService.subjectInvest(userId,subjectId,amount, actualAmt, redPacketId,device,autoInvest, transLogId);
        logger.info("散标充值并投资drpay回调投资处理成功{}",params);
        return new RestResponseBuilder<>().success(null);
    }

    /**
     *  充值并投资取消接口
     * @param params
     * @return
     */
    @PostMapping("/subjectRechargeAndInvestCancel")
    public RestResponse subjectRechargeAndInvestCancel(@RequestBody Map<String, String> params) {
        int transLogId = Integer.valueOf(params.get("transLogId"));
        if (transLogId == 0) {
            throw new IllegalArgumentException("transLogId can not be null");
        }
        subjectAccountService.subjectRechargeAndInvestCancel(transLogId);
        return new RestResponseBuilder<>().success(null);
    }

}
