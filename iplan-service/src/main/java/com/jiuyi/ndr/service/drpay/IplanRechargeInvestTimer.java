package com.jiuyi.ndr.service.drpay;

import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.iplan.IPlanTransLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class IplanRechargeInvestTimer {

    private IPlanAccountService iPlanAccountService;

    private IPlanTransLogService iPlanTransLogService;

    private IPlanService iPlanService;


    private static final Logger logger = LoggerFactory.getLogger(IplanRechargeInvestTimer.class);

    Timer timer;

    public IplanRechargeInvestTimer(int iplanTransLogId, int seconds){
        logger.info("++++++++++++进入充值并投资调度+++++++++++++，iplanTransLogId=[{}], seconds=[{}]", iplanTransLogId, seconds);
        timer = new Timer();
        timer.schedule(new IplanRechargeInvestCancelTask(iplanTransLogId), seconds * 1000);
    }

    class IplanRechargeInvestCancelTask extends TimerTask {

        private int iplanTransLogId;

        public IplanRechargeInvestCancelTask(int iplanTransLogId) {
            this.iplanTransLogId = iplanTransLogId;
        }
        @Override
        public void run() {
            //调用iplan-service中充值并投资取消
            if (iplanTransLogId > 0) {
                try {
                    iPlanAccountService = SpringUtils.getBean(IPlanAccountService.class);
                    logger.info("++++++++++++++开始执行充值并投资调度+++++++++++++，iplanTransLogId=[{}]", iplanTransLogId);
                    iPlanAccountService.rechargeAndInvestCancel(iplanTransLogId);
                    iPlanTransLogService = SpringUtils.getBean(IPlanTransLogService.class);
                    IPlanTransLog iPlanTransLog = iPlanTransLogService.getById(iplanTransLogId);
                    Map<String,String> maps = new HashMap<>();
                    maps.put("userId", iPlanTransLog.getUserId());
                    iPlanService = SpringUtils.getBean(IPlanService.class);
                    IPlan iplan = iPlanService.getIPlanById(iPlanTransLog.getIplanId());
                    maps.put("alert", "小主，您未成功投资"+iplan.getName()+"项目，资金已返还到短融网账户中。");
                    JiGuangUtil.sendHttpPost(maps);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warn("充值并投资transLogId=[{}]取消异常，[{}]", iplanTransLogId, e.getMessage());
                }
            } else {
                logger.warn("充值并投资取消失败，iplanTransLogId 不能为0");
            }
            timer.cancel();
            logger.info("+++++++++++++充值并投资调度执行结束，取消timer完成++++++++++++，iplanTransLogId=[{}]", iplanTransLogId);
        }
    }

    public static void main(String[] args) {
//        new IplanRechargeInvestTimer(248, 30);
        Map<String,String> maps = new HashMap<>();
        maps.put("userId", "YBZBvmQbQfQbsmvj");
        maps.put("alert", "小主，您未成功投资"+"项目，资金已返还到短融网账户中。");
        JiGuangUtil.sendHttpPost(maps);
    }


}
