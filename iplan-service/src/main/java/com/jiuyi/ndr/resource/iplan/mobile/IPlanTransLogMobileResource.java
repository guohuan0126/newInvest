package com.jiuyi.ndr.resource.iplan.mobile;

import com.github.pagehelper.PageInfo;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.dto.iplan.mobile.IPlanAppTransLogDto;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.rest.page.PageData;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanTransLogService;
import com.jiuyi.ndr.service.user.RedPacketService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lixiaolei on 2017/6/15.
 */
@RestController
public class IPlanTransLogMobileResource {

    @Autowired
    private IPlanTransLogService iPlanTransLogService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private IPlanAccountService iPlanAccountService;

    private DecimalFormat df = new DecimalFormat("######0.##");

    /**
     * 购买记录
     *
     * @param userId        用户id
     * @param iPlanId       计划id
     * @param pageNo        页码
     * @param pageSize      页面大小
     */
    @GetMapping("authed/{userId}/{iPlanId}/invest/trans")
    public RestResponse<PageData> getTransLog(@PathVariable("userId") String userId, @PathVariable("iPlanId") Integer iPlanId,
                                              @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
        List<IPlanAppTransLogDto> transLogDtos = new ArrayList<>();
        List<IPlanTransLog> iPlanTransLogs = iPlanTransLogService.getByPageHelper(userId, iPlanId,
                IPlanTransLog.TRANS_TYPE_NORMAL_IN+","+IPlanTransLog.TRANS_TYPE_INIT_IN,
                IPlanTransLog.TRANS_STATUS_SUCCEED+","+IPlanTransLog.TRANS_STATUS_PROCESSING,
                pageNo, pageSize);
        for (IPlanTransLog transLog : iPlanTransLogs) {
            IPlanAccount iPlanAccount = iPlanAccountService.getIPlanAccount(userId, iPlanId);
            IPlanAppTransLogDto transLogDto = new IPlanAppTransLogDto();
            transLogDto.setId(transLog.getId());
            transLogDto.setTransDesc("加入金额");
            transLogDto.setTransAmt(String.valueOf(transLog.getTransAmt() / 100.0));
            transLogDto.setStatus(transLog.getTransStatus());
            transLogDto.setTransTime(transLog.getTransTime());
            String redPacketDesc = "";
            String redMsg = "";
            String vipMsg = "";
            if (transLog.getRedPacketId() != null && transLog.getRedPacketId() > 0) {
                RedPacket redPacket = redPacketService.getRedPacketById(transLog.getRedPacketId());
                if (RedPacket.TYPE_DEDUCT.equals(redPacket.getType())) {
                    redMsg = redPacket.getMoney() + "元抵扣券；";
                } else if (RedPacket.TYPE_MONEY.equals(redPacket.getType())) {
                    redMsg = redPacket.getMoney() + "元现金券；";
                } else if (RedPacket.TYPE_RATE.equals(redPacket.getType())) {
                    redMsg = this.getRateStr(redPacket.getRate()) + "加息券；";
                } else if (RedPacket.TYPE_RATE_BY_DAY.equals(redPacket.getType())) {
                    redMsg = this.getRateStr(redPacket.getRate()) + redPacket.getRateDay() + "天加息券；";
                }
            }
            double vipRate = 0;
            if (iPlanAccount != null) {
                if (iPlanAccount.getVipRate() != null) {
                    vipRate = iPlanAccount.getVipRate().doubleValue();
                }
            }
            /*if (vipRate > 0) {
                vipMsg = "会员额外加息" + getRateStr(vipRate) + "奖励；";
            }*/
            if (StringUtils.isNotBlank(redMsg) || StringUtils.isNotBlank(vipMsg)) {
                redPacketDesc = "使用奖励：" + redMsg + vipMsg;
            }
            transLogDto.setUsedRedPactDesc(redPacketDesc);

            transLogDtos.add(transLogDto);
        }

        PageData<IPlanAppTransLogDto> pageData = new PageData<>();
        PageInfo<IPlanTransLog> pageInfo = new PageInfo<>(iPlanTransLogs);
        pageData.setList(transLogDtos);
        pageData.setPage(pageInfo.getPageNum());
        pageData.setSize(pageInfo.getSize());
        pageData.setTotalPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        return new RestResponseBuilder<PageData>().success(pageData);
    }

    private String getRateStr(double rate) {
        /*BigDecimal rateBD = BigDecimal.valueOf(rate);
        String rateStr = String.valueOf(BigDecimal.valueOf(100).multiply(rateBD));
        rateStr = rateStr.replaceAll("0*$", "");
        rateStr = (rateStr.lastIndexOf(".") == rateStr.length() - 1 ? rateStr.substring(0, rateStr.length() - 1) : rateStr) + "%";
        return rateStr;*/
        return df.format(rate * 100) + "%";
    }

}
