package com.jiuyi.ndr.rest.interceptor;

import com.duanrong.util.json.FastJsonUtil;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.service.config.ConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class MaintainInterceptor extends HandlerInterceptorAdapter
{
    private static final String ENCODING = "UTF-8";
    private static final String DRPAY_STOP_SWITCH = "drpay_stop_switch";
    private static final String DRPAY_STOP_START_TIME = "drpay_stop_start_time";
    private static final String DRPAY_STOP_END_TIME = "drpay_stop_end_time";
    private static final String DRPAY_STOP_SWITCH_ON = "1";

    @Autowired
    private ConfigService configService;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object obj)
            throws Exception
    {
        resp.setCharacterEncoding("UTF-8");

        String url = req.getRequestURL().toString();

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        Config drpaySwitch = this.configService.getConfigById("drpay_stop_switch");
        Config drpayStopStartTime = this.configService.getConfigById("drpay_stop_start_time");
        Config drpayStopEndTime = this.configService.getConfigById("drpay_stop_end_time");
        if (drpaySwitch != null && StringUtils.equals(drpaySwitch.getValue(), DRPAY_STOP_SWITCH_ON)) {
            if (drpayStopStartTime != null && StringUtils.isNotBlank(drpayStopStartTime.getValue())
                    && drpayStopEndTime != null && StringUtils.isNotBlank(drpayStopEndTime.getValue())) {
                Date start = sdf.parse(drpayStopStartTime.getValue());
                Date end = sdf.parse(drpayStopEndTime.getValue());
                if (date.getTime() > start.getTime() && date.getTime() < end.getTime()) {
                    if((url.contains("/advanceExit")) || (url.contains("/creditTransfer")) || (url.contains("/investCredit/")) || (url.contains("/creditCancel/"))||(url.contains("/invest/"))
                            && (!url.contains("manage"))
                            && (!url.contains("detail"))
                            && (!url.contains("toConfirm"))){
                        Map<String, Object> map = new HashMap<>();
                        Map<String, String> map1 = new HashMap<>();
                        map1.put("desc","因厦门银行存管系统维护，暂停所有交易");
                        map.put("status", "FALSE");
                        map.put("errorCode", "45003");
                        map.put("errorMsg", "因厦门银行存管系统维护，暂停所有交易");
                        map.put("response",map1);
                        Writer writer = resp.getWriter();
                        writer.write(FastJsonUtil.objToJson(map));
                        writer.flush();
                        writer.close();
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
