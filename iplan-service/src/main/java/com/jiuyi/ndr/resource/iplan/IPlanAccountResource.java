package com.jiuyi.ndr.resource.iplan;

import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import com.jiuyi.ndr.dto.iplan.mobile.IPlanAppInvestedShareDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.rest.customannotation.AutoLogger;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.user.ActivityMarkConfigureService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.redis.RedisClient;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lixiaolei on 2017/6/8.
 */
@RestController
public class IPlanAccountResource {

    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private UserService userService;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private ActivityMarkConfigureService activityMarkConfigureService;

    private DecimalFormat df = new DecimalFormat("######0.##");

    private final static Logger logger = LoggerFactory.getLogger(IPlanAccountResource.class);


    @PostMapping("{userId}/invest/{iPlanId}/{amount}")
    @AutoLogger
    public RestResponse<IPlanAppInvestedShareDto> invest(@PathVariable("userId") String userId, @PathVariable("iPlanId") int iPlanId,
                                                         @PathVariable("amount") int amount, @RequestBody Map<String, String> params) {
        Integer redPackedId = 0;
        if (params.containsKey("redPackedId")) {
            redPackedId = Integer.valueOf(params.get("redPackedId"));
        }
        String device = params.get("device");
        if (!StringUtils.hasText(device)) {
            throw new IllegalArgumentException("device不能为空");
        }
        logger.info("理财计划投资userId：{}，理财计划Id：{}，金额：{}", userId, iPlanId, amount);
        List<String> DOUBLE_11_IPLAN = redisClient.getVByList(GlobalConfig.DOUBLE_11_IPLAN, 0, -1);
        if (DOUBLE_11_IPLAN.contains(String.valueOf(iPlanId))) {
            iPlanAccountService.snathInvest(userId, iPlanId, amount, redPackedId, device);
        } else {
            iPlanAccountService.invest(userId, iPlanId, amount, redPackedId, device, 0);
        }
        IPlanAppInvestedShareDto iPlanAppInvestedShareDto = new IPlanAppInvestedShareDto();
        iPlanAppInvestedShareDto.setShares(userService.getUserById(userId).getMobileNumber());
        String iplanInvestSuccessDesc = "";
        double money = 0;
        IPlanAccount iPlanAccount = iPlanAccountService.getIPlanAccount(userId, iPlanId);
        double vipRate = (iPlanAccount != null && iPlanAccount.getVipRate() != null) ? iPlanAccount.getVipRate().doubleValue() : 0;
        if (vipRate > 0) {
            iplanInvestSuccessDesc += "尊贵的VIP会员，";
        }
        IPlan iPlan = iPlanService.getIPlanById(iPlanId);
        double fixRate = (iPlan != null && iPlan.getFixRate() != null) ? iPlan.getFixRate().doubleValue() : 0;
        double bonusRate = (iPlan != null && iPlan.getBonusRate() != null) ? iPlan.getBonusRate().doubleValue() : 0;
        double totalRate = fixRate + bonusRate + vipRate;
        int term = iPlan != null ? iPlan.getTerm() : 0;
        double profit = 0.0;
        int interestAccrualType = iPlan.getInterestAccrualType();
        //todo 取省心投标的最短期限(随心投修改-jgx-5.16)
        int minTerm = iPlanAccountService.getYjtMinTerm(iPlan);
        if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())) {
            double expectedInterest = subjectService.getInterestByRepayType(amount, BigDecimal.valueOf(fixRate), iPlanAccountService.getRate(iPlan),
                    minTerm, minTerm * 30, iPlan.getRepayType());
            double expectedBonusInterest = 0.0;
            if(bonusRate>0) {
                expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),iPlanAccountService.getRate(iPlan),
                        minTerm,minTerm*30,iPlan.getRepayType());
                ActivityMarkConfigure amc = activityMarkConfigureService.findById(iPlan.getActivityId());
                if(amc != null && amc.getIncreaseTerm() != null){
                    expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),iPlanAccountService.getRate(iPlan),
                            amc.getIncreaseTerm(),minTerm*30,iPlan.getRepayType());
                }
            }
            profit = expectedInterest + expectedBonusInterest;
        }else {
            profit = iPlanAccountService.calInterest(interestAccrualType, amount / 100, totalRate, iPlan);
        }
        iplanInvestSuccessDesc = iPlanAccountService.getInvestInfer(amount, redPackedId, iplanInvestSuccessDesc, money, iPlan, profit, minTerm);
        IPlanAppInvestedShareDto.Share share = new IPlanAppInvestedShareDto.Share();
        share.setIplanInvestSuccessDesc(iplanInvestSuccessDesc);
        iPlanAppInvestedShareDto.setShareMsg(share);

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
            if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())) {
                tyjFlag = "0";
            }
        }
        iPlanAppInvestedShareDto.setTyjFlag(tyjFlag);
        iPlanAppInvestedShareDto.setTyjMsg("恭喜您！获得" + tyjAmt + "元奖励金");

        return new RestResponseBuilder<IPlanAppInvestedShareDto>().success(iPlanAppInvestedShareDto);
    }



    @PostMapping("iplan/rechargeAndinvest")
    public RestResponse<Object> rechargeAndinvest(@RequestBody Map<String, String> params) {
        int transLogId = Integer.valueOf(params.get("transLogId"));
        String device = "";
        String userId = "";
        int iPlanId = 0;
        int amount = 0;
        int redPacketId = 0;
        int autoInvest = 0;
        if (transLogId <= 0) {
            device = params.get("device");
            userId = params.get("userId");
            iPlanId = Integer.valueOf(params.get("iPlanId"));
            List<String> DOUBLE_11_IPLAN = redisClient.getVByList(GlobalConfig.DOUBLE_11_IPLAN, 0, -1);
            if (DOUBLE_11_IPLAN.contains(String.valueOf(iPlanId))) {
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

        Object data = iPlanAccountService.rechargeAndInvest(userId, iPlanId, amount, redPacketId, device, autoInvest, transLogId, "quick");
        Map<String, Object> map = new HashMap<>();
        map.put("rechargeFlag", "1");//去充值
        if (data == null) {
            map.put("rechargeFlag", "0");//投资成功，不充值
        }
        map.put("data", data);
        return new RestResponseBuilder<>().success(map);
    }

    @PostMapping("iplan/rechargeSuccessInvest")
    public RestResponse<Object> rechargeSuccessInvest(@RequestBody Map<String, String> params) {
        logger.info("月月盈省心投充值并投资drpay回调请求参数{}", params);
        int transLogId = Integer.valueOf(params.get("transLogId"));
        String device = params.get("device");
        String userId = params.get("userId");
        int iPlanId = Integer.valueOf(params.get("iPlanId"));
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
        if (iPlanId == 0) {
            throw new IllegalArgumentException("iPlanId不能为空");
        }
        if (amount == 0) {
            throw new IllegalArgumentException("amount不能为空");
        }
        if (actualAmt == 0) {
            throw new IllegalArgumentException("actualAmt不能为空");
        }
        iPlanAccountService.iPlanInvest(userId, iPlanId, amount, actualAmt, redPacketId, device, autoInvest, transLogId);
        logger.info("月月盈省心投充值并投资drpay回调投资处理成功{}", params);
        return new RestResponseBuilder<>().success(null);
    }


    @PostMapping("/rechargeAndInvestCancel")
    public RestResponse rechargeAndInvestCancel(@RequestBody Map<String, String> params) {
        int transLogId = Integer.valueOf(params.get("transLogId"));
        if (transLogId == 0) {
            throw new IllegalArgumentException("transLogId can not be null");
        }
        iPlanAccountService.rechargeAndInvestCancel(transLogId);
        return new RestResponseBuilder<>().success(null);
    }

    @PostMapping("{userId}/{iPlanId}/advanceExit")
    public RestResponse<Void> advanceExit(@PathVariable("userId") String userId, @PathVariable("iPlanId") int iPlanId,
                                          @RequestBody Map<String, Object> params) {
        /*String userId = (String) params.get("userId");
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId不能为空");
        }
        Integer iPlanId = (Integer) params.get("iPlanId");
        if (iPlanId == null) {
            throw new IllegalArgumentException("iPlanId不能为空");
        }*/
        String device = (String) params.get("device");
        if (!StringUtils.hasText(device)) {
            throw new IllegalArgumentException("device不能为空");
        }
        iPlanAccountService.advanceExit(userId, iPlanId, device);
        return new RestResponseBuilder<Void>().success(null);
    }


    @RequestMapping(value = "/iPlanInvestCancel", method = RequestMethod.POST)
    public RestResponse iPlanInvestCancel(@RequestParam("iPlanTransLogId") int iPlanTransLogId) {
        iPlanAccountService.iPlanInvestCancel(iPlanTransLogId);
        return new RestResponseBuilder<>().success(null);
    }

    @RequestMapping(value = "/freeze", method = RequestMethod.POST)
    public RestResponse newIplanFreeze(@RequestParam("userId") String userId, @RequestParam("amount") int amount) {
        BaseResponse baseResponse = iPlanAccountService.newIplanFreeze(userId, amount);
        return new RestResponseBuilder<>().success(baseResponse);
    }

    //@PostMapping("/iplanToYjt")
   /* public RestResponse<IPlan> iplanToYjt(@RequestParam("ids") String[] ids,@RequestParam("term") Integer term) {
        if(ids.length == 0){
            throw new IllegalArgumentException("ids不能为空");
        }
        if(term == null){
            throw new IllegalArgumentException("term不能为空");
        }
        IPlan iPlan = iPlanAccountService.iplanToYjt(Arrays.asList(ids), term);
        return new RestResponseBuilder<IPlan>().success(iPlan);
    }*/
    @PostMapping("/iplanToYjt")
    public RestResponse<IPlan> iplanToYjt(@RequestBody Map<String,Object> map) {
        List<String> ids = new ArrayList<>();
        Integer term = 0;
        Integer rateType = 0;
        if (map.containsKey("ids") && map.get("ids") != null){
            ids = (List<String>) map.get("ids");
        }
        if (map.containsKey("term") && map.get("term") != null ){
            term = (Integer) map.get("term");
        }
        if (map.containsKey("rateType") && map.get("rateType") != null ){
            rateType = Integer.parseInt((String) map.get("rateType"));
        }

        if(ids.size() == 0){
            throw new IllegalArgumentException("ids不能为空");
        }

        if(rateType == null){
            throw new IllegalArgumentException("rateType不能为空");
        }else if(rateType == 0){
            if(term == 0){
                throw new IllegalArgumentException("term不能为空");
            }
        }

        IPlan iPlan = iPlanAccountService.iplanToYjt(ids, term,rateType);
        return new RestResponseBuilder<IPlan>().success(iPlan);
    }

    @PostMapping("/market/invest/record")
    public RestResponse<Map<String,Double>> marketInvest(@RequestBody Map<String,String> map){
        Map<String, Double> investResult = iPlanAccountService.getInvestResult(map);
        return new RestResponseBuilder<Map<String,Double>>().success(investResult);
    }


}
