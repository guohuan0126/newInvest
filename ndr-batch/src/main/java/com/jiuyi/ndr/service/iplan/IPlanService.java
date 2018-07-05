package com.jiuyi.ndr.service.iplan;

import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.service.invest.InvestService;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.redis.RedisClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by drw on 2017/6/14.
 */
@Service
public class IPlanService {

    private final static Logger logger = LoggerFactory.getLogger(IPlanService.class);

    @Autowired
    private IPlanDao iPlanDao;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private InvestService investService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private IPlanTransLogService iPlanTransLogService;

    public IPlan findOneById(Integer id) {
        return iPlanDao.findById(id);
    }
    public IPlan getIPlanById(int iPlanId) {
        if (iPlanId == 0) {
            throw new IllegalArgumentException("iPlanId can not be null");
        }
        return iPlanDao.findById(iPlanId);
    }

    public IPlan getIPlanByIdForUpdate(int iPlanId) {
        if (iPlanId == 0) {
            throw new IllegalArgumentException("iPlanId can not be null");
        }
        return iPlanDao.findByIdForUpdate(iPlanId);
    }

    public IPlan update(IPlan iPlan) {
        if (iPlan == null) {
            throw new IllegalArgumentException("iPlan is can not null");
        }
        iPlan.setUpdateTime(DateUtil.getCurrentDateTime19());
        iPlanDao.update(iPlan);
        return iPlan;
    }

    /**
     * iPlan是否可投
     */
    public Boolean iPlanInvestable(Integer id, int autoInvest) {
        if (id == null) {
            throw new IllegalArgumentException("iPlanCode is can not null");
        }
        IPlan iPlan = null;
        List<String> DOUBLE_11_IPLAN = redisClient.getVByList(GlobalConfig.DOUBLE_11_IPLAN,0,-1);
        if (DOUBLE_11_IPLAN.contains(String.valueOf(id))){
            iPlan = redisClient.get(GlobalConfig.IPLAN_REDIS+id,IPlan.class);
            if (iPlan==null){
                iPlan = iPlanDao.findById(id);
                String iplan = JSON.toJSONString(iPlan);
                try {
                    redisClient.set(GlobalConfig.IPLAN_REDIS+id,iplan);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            logger.info("理财计划详情：{}",iPlan.toString());
            if (IPlan.STATUS_ANNOUNCING.equals(iPlan.getStatus())){
                if(DateUtil.compareDateTime(DateUtil.getCurrentDateTime19(),iPlan.getRaiseOpenTime())){
                    iPlan.setStatus(IPlan.STATUS_RAISING);
                    String iplan = JSON.toJSONString(iPlan);
                    try {
                        redisClient.set(GlobalConfig.IPLAN_REDIS+id,iplan);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            iPlan = iPlanDao.findById(id);
        }

        if (iPlan != null) {
            if (!iPlan.getPushStatus().equals(IPlan.PUSH_STATUS_Y)) {
                return false;
            }
            if (autoInvest == 0 ) {
                if (iPlan.getStatus().equals(IPlan.STATUS_RAISING)) {
                    return true;
                }
            }
            if (autoInvest == 1) {
                if (iPlan.getStatus().equals(IPlan.STATUS_ANNOUNCING) || iPlan.getStatus().equals(IPlan.STATUS_RAISING)) {
                    return true;
                }
            }
        }
        return false;
    }

    //查询用户已投金额
    public Double findInvestedAmtByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId不能为空");
        }
        Double investTotalMoney = investService.getInvestTotalMoney(userId);
        Long iPlanTotalMoney = iPlanAccountService.getIPlanTotalMoney(userId);
        //新增散标的投资额度
        Long subjectTotalMoney =subjectAccountService.getSubjectTotalMoney(userId);
        return iPlanTotalMoney + subjectTotalMoney + investTotalMoney * 100;

    }

    /**
     * 强制满标服务
     *
     * @param iPlanId
     * @return
     */
    public IPlan mandatoryIPlan(Integer iPlanId) {
        if (iPlanId == null) {
            throw new IllegalArgumentException("iPlanId is can not null");
        }
        IPlan iPlan = iPlanDao.findById(iPlanId);
        if (iPlan != null) {
            // 判断改计划下的投资记录是不是有待处理和待待等待的记录如果有就不能满标
            //查询状态只有0处理中(购买成功)，3超时，4待确认（只有充值并投资有这个状态，在iplan中查询并流标）
            List<IPlanTransLog> iPlanTransLogs = iPlanTransLogService.getByIPlanIdAndTransStatusAndTransTypeIns(iPlanId, "0,6", "0,3");
            List<IPlanTransLog> iPlanTransLogsConfirm = iPlanTransLogService.getByIPlanIdAndTransStatusAndTransTypeIns(iPlanId, "0,6", "4");
            if (iPlanTransLogs != null && iPlanTransLogs.size() == 0) {
                for (IPlanTransLog iPlanTransLog : iPlanTransLogsConfirm) {
                    //有充值投资待确认的记录就流标
                    iPlanAccountService.rechargeAndInvestCancel(iPlanTransLog.getId());
                }
                IPlan iPlanNew = new IPlan();
                // Integer quota = iPlan.getQuota() - iPlan.getAvailableQuota();
                iPlanNew.setId(iPlan.getId());
                iPlanNew.setAvailableQuota(0);
                iPlanNew.setStatus(IPlan.STATUS_RAISING_FINISH);// 由募集中改为募集完成
                iPlanNew.setUpdateTime(DateUtil.getCurrentDateTime19());
                iPlanNew.setRaiseCloseTime(DateUtil.getCurrentDateTime19());// 结束募集时间
                iPlan = this.update(iPlanNew);
            } else {
                logger.warn("定期理财计划：" + iPlan.getCode() + "有正在处理中的投资记录，请稍等");
//                throw new ProcessException(Error.NDR_0464);
                return null;
            }
        }
        return iPlan;
    }
}
