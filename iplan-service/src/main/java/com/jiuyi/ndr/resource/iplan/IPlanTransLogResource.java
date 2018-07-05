package com.jiuyi.ndr.resource.iplan;

import com.github.pagehelper.PageInfo;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.dto.iplan.IPlanTransLogDto;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.rest.page.PageData;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.iplan.IPlanTransLogService;
import com.jiuyi.ndr.service.user.RedPacketService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by lixiaolei on 2017/6/15.
 */
@RestController
public class IPlanTransLogResource {

    @Autowired
    private IPlanTransLogService iPlanTransLogService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private IPlanService iPlanService;

    private DecimalFormat df = new DecimalFormat("######0.0");

    /**
     * 购买记录
     */
    @GetMapping("/{userId}/{iPlanId}/trans")
    public RestResponse getTransLog(@PathVariable("userId") String userId,
                                    @PathVariable("iPlanId") Integer iPlanId,
                                    @RequestParam("transTypes") String transTypes,
                                    @RequestParam("transStatus") String transStatus,
                                    @RequestParam("pageNo") Integer pageNo,
                                    @RequestParam("pageSize") Integer pageSize) {
        List<IPlanTransLogDto> transLogDtos = new ArrayList<>();
        List<IPlanTransLog> iPlanTransLogs = iPlanTransLogService.getByPageHelper(userId, iPlanId, transTypes, transStatus, pageNo, pageSize);
        for (IPlanTransLog transLog : iPlanTransLogs) {
            IPlanTransLogDto transLogDto = new IPlanTransLogDto();
            BeanUtils.copyProperties(transLog, transLogDto);

            Integer redPacketId = transLog.getRedPacketId();
            if (redPacketId != 0) {
                RedPacket redPacket = redPacketService.getRedPacketById(redPacketId);
                if (null != redPacket) {

                    String redDes="";
                    if(Objects.equals(redPacket.getType(), "rate")){
                        redDes = df.format(redPacket.getRate()*100)+"%加息劵";
                    }else if(Objects.equals(redPacket.getType(), "money")){
                        redDes = redPacket.getMoney()+"元现金券";
                    }else if(Objects.equals(redPacket.getType(), "deduct")){
                        redDes = redPacket.getMoney()+"元抵扣券";
                    }else if(Objects.equals(redPacket.getType(), "rateByDay")){
                        redDes = df.format(redPacket.getRate()*100)+"%"+redPacket.getRateDay()+"天加息劵";
                    }
                    transLogDto.setRedPacketName(redDes);
                }
            }
            transLogDtos.add(transLogDto);
        }

        PageData<IPlanTransLogDto> pageData = new PageData<>();
        PageInfo<IPlanTransLog> pageInfo = new PageInfo<>(iPlanTransLogs);
        pageData.setList(transLogDtos);
        pageData.setPage(pageInfo.getPageNum());
        pageData.setSize(pageInfo.getSize());
        pageData.setTotalPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        return new RestResponseBuilder<>().success(pageData);
    }

}
