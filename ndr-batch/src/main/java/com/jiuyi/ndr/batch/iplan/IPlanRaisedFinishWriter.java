package com.jiuyi.ndr.batch.iplan;

import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.iplan.IPlanSettleDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanSettle;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanRepayDetailService;
import com.jiuyi.ndr.service.iplan.IPlanRepayScheduleService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.redpacket.RedpacketService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.redis.RedisClient;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zhangyibo on 2017/6/13.
 */
public class IPlanRaisedFinishWriter implements ItemWriter<IPlan>{

    public static final Logger logger = LoggerFactory.getLogger(IPlanRaisedFinishWriter.class);

    @Autowired
    private IPlanRepayScheduleService iPlanRepayScheduleService;

    @Autowired
    private IPlanDao iPlanDao;

    @Autowired
    private RedpacketService redpacketService;

    @Autowired
    private IPlanRepayDetailService iPlanRepayDetailService;

    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private IPlanAccountDao iPlanAccountDao;

    @Autowired
    private CreditDao creditDao;

    @Autowired
    private IPlanAccountService iPlanAccountService;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private IPlanSettleDao iPlanSettleDao;

    @Override
    public void write(List<? extends IPlan> iplans) throws Exception {
        for(IPlan iPlan:iplans){
            logger.info("正在对理财计划{}进行理财计划满标操作",iPlan.getId());
            List<IPlanTransLog> iPlanTransLogs = iPlanTransLogDao.findByIPlanId(iPlan.getId());
            //判断省心投及月月盈是否符合放款条件 符合返回true 否则 false
            boolean isRaiseFinish = checkCanRaisFinish(iPlanTransLogs,iPlan);

            if (!isRaiseFinish){
                logger.info("理财计划【{}】,不符合放款条件跳过",iPlan.getId());
                continue;
            }

            iPlan.setRaiseFinishTime(DateUtil.getCurrentDateTime19());
            //生成还款计划
            iPlanRepayScheduleService.genIPlanRepaySchedule(iPlan);
            if (!IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                //生成每个人的理财计划还款计划
                iPlanRepayDetailService.genRepayDetail(iPlan);
                //奖励发放
            }
            redpacketService.createPacketInvest(iPlan);
            iPlan.setStatus(IPlan.STATUS_EARNING);
            //天标项目结束日期修改开始
            iPlan.setEndTime(iPlanRepayScheduleService.getDueDateByTerm(iPlan, iPlan.getTerm()));
            //天标项目结束日期修改结束
            iPlan.setUpdateTime(DateUtil.getCurrentDateTime19());
            iPlanDao.update(iPlan);
            delRedisForIPlan(iPlan.getId());
            //添加到提前结清表
            if (IPlan.IPLAN_TYPE_TTZ.equals(iPlan.getIplanType())){
                insertIPlanSettle(iPlan);
            }
        }
    }

    private boolean checkCanRaisFinish(List<IPlanTransLog> iPlanTransLogs,IPlan iPlan){
        //要判断该理财计划下面是否还有调用厦门银行不成功的转入交易 如果存在 就不生成还款计划
        if(iPlanTransLogs.stream().filter(iPlanTransLog -> !(iPlanTransLog.getTransStatus().equals(IPlanTransLog.TRANS_STATUS_FAILED) &&iPlanTransLog.getExtStatus()==null)).filter(iPlanTransLog-> !iPlanTransLog.getTransStatus().equals(IPlanTransLog.TRANS_STATUS_TO_CANCEL))
                .anyMatch(iPlanTransLog -> !BaseResponse.STATUS_SUCCEED.equals(iPlanTransLog.getExtStatus()))) {
            //要先过滤掉本地失败 没有调用厦门银行的交易 比如说充值并投资失败的交易
            List<IPlanTransLog> confirmIPlanTransLogs = iPlanTransLogs.stream().filter(iPlanTransLog -> iPlanTransLog.getTransStatus().equals(IPlanTransLog.TRANS_STATUS_TO_CONFIRM)).collect(Collectors.toList());
            if (!confirmIPlanTransLogs.isEmpty()){
                for (IPlanTransLog i:confirmIPlanTransLogs) {
                    iPlanAccountService.rechargeAndInvestCancel(i.getId());
                }
            }
            logger.warn("理财计划{}下存在不成功的转入交易，暂不能进行理财计划募集完成操作",iPlan.getId());
            noticeService.sendEmail("理财计划放款","理财计划"+iPlan.getId()+"下存在不成功的转入交易，暂不能进行理财计划募集完成操作","guohuan@duanrong.com");
            return false;
        }
        List<IPlanTransLog> transLogs = iPlanTransLogs.stream().filter(iPlanTransLog -> (iPlanTransLog.getTransType().equals(IPlanTransLog.TRANS_TYPE_INIT_IN)||iPlanTransLog.getTransType().equals(IPlanTransLog.TRANS_TYPE_NORMAL_IN))).filter(iPlanTransLog -> iPlanTransLog.getTransStatus().equals(IPlanTransLog.TRANS_STATUS_PROCESSING)).collect(Collectors.toList());
        if(transLogs!=null&&transLogs.size()>0){
            logger.warn("理财计划{}下存在还未匹配的转入记录{}，暂不能进行理财计划募集完成操作",iPlan.getId(),transLogs.stream().map(IPlanTransLog::getId).collect(Collectors.toList()));
            return false;
        }
        //只有理财计划下的标的都放款完成了 才能进行募集完成操作
        List<IPlanAccount> iPlanAccounts = iPlanAccountDao.findByIPlanId(iPlan.getId());
        if (iPlanAccounts==null||iPlanAccounts.size()==0){
            logger.warn("理财计划{}没有用户投资，不能进行理财计划募集完成操作",iPlan.getId());
            iPlan.setStatus(IPlan.STATUS_CANCEL);
            iPlanDao.update(iPlan);
            noticeService.sendEmail("理财计划放款","理财计划"+iPlan.getId()+"下没有用户投资，不能进行理财计划募集完成操作","guohuan@duanrong.com");
            return false;
        }
        List<Credit> credits = creditDao.findWaitCredits(iPlanAccounts.stream().map(IPlanAccount::getId).collect(Collectors.toList()));
        if(credits!=null&&credits.size()>0){
            logger.warn("理财计划{}下存在还未放款的标的{}，暂不能进行理财计划募集完成操作",iPlan.getId(),credits.stream().map(Credit::getSubjectId).collect(Collectors.toList()));
            return false;
        }
        return true;
    }

    private void insertIPlanSettle(IPlan iPlan) {
        try {
            IPlanSettle iPlanSettle = new IPlanSettle();
            iPlanSettle.setIplanId(iPlan.getId());
            iPlanSettle.setStatus(IPlanSettle.STATUS_PENDING);
            iPlanSettle.setSettleDay(DateUtil.getCurrentDate());
            iPlanSettleDao.insert(iPlanSettle);
        } catch (Exception e){
            logger.error("月月盈-{}放入提前结清表异常",iPlan.getId());
            e.printStackTrace();
        }


    }

    private void delRedisForIPlan(int iPlanId){
        try {
            List<String> DOUBLE_11_IPLAN = redisClient.getVByList(GlobalConfig.DOUBLE_11_IPLAN,0,-1);
            if (DOUBLE_11_IPLAN.contains(String.valueOf(iPlanId))){
                redisClient.del(GlobalConfig.IPLAN_REDIS+iPlanId);
            }
        }catch (Exception e){
            logger.error("理财计划募满更新redis[理财计划Id：{}]",iPlanId);
        }
    }

}
