package com.jiuyi.ndr.service.drpay;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectTransLog;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.subject.SubjectTransLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SubjectRechargeInvestTimer {

    private SubjectAccountService  subjectAccountService;

    private SubjectTransLogService subjectTransLogService;

    private SubjectService subjectService;


    private static final Logger logger = LoggerFactory.getLogger(SubjectRechargeInvestTimer.class);
    Timer timer;

    public SubjectRechargeInvestTimer(int subjectTransLogId, int seconds){
        logger.info("++++++++++++进入充值并投资调度+++++++++++++，subjectTransLogId=[{}], seconds=[{}]", subjectTransLogId, seconds);
        timer = new Timer();
        timer.schedule(new SubjectRechargeInvestCancelTask(subjectTransLogId), seconds * 1000);
    }

    class SubjectRechargeInvestCancelTask extends TimerTask {

        private int subjectTransLogId;

        public SubjectRechargeInvestCancelTask(int subjectTransLogId) {
            this.subjectTransLogId = subjectTransLogId;
        }
        @Override
        public void run() {
            //调用subject-service中充值并投资取消
            if (subjectTransLogId > 0) {
                try {
                    subjectAccountService = SpringUtils.getBean(SubjectAccountService.class);
                    logger.info("++++++++++++++开始执行充值并投资调度+++++++++++++，subjectTransLogId=[{}]", subjectTransLogId);
                    subjectAccountService.subjectRechargeAndInvestCancel(subjectTransLogId);
                    subjectTransLogService = SpringUtils.getBean(SubjectTransLogService.class);
                    SubjectTransLog subjectTransLog = subjectTransLogService.getById(subjectTransLogId);
                    Map<String,String> maps = new HashMap<>();
                    maps.put("userId", subjectTransLog.getUserId());
                    subjectService = SpringUtils.getBean(SubjectService.class);
                    Subject subject =subjectService.findSubjectBySubjectId(subjectTransLog.getSubjectId());
                    maps.put("alert", "小主，您未成功投资"+subject.getName()+"项目，资金已返还到短融网账户中。");
                    JiGuangUtil.sendHttpPost(maps);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warn("散标充值并投资transLogId=[{}]取消异常，[{}]", subjectTransLogId, e.getMessage());
                }
            } else {
                logger.warn("散标充值并投资取消失败，subjectTransLogId 不能为0");
            }
            timer.cancel();
            logger.info("+++++++++++++散标充值并投资调度执行结束，取消timer完成++++++++++++，subjectTransLogId=[{}]", subjectTransLogId);
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
