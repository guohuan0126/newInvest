package com.jiuyi.ndr.service.lplan;

import com.github.pagehelper.PageHelper;
import com.jiuyi.ndr.dao.lplan.LPlanTransLogDao;
import com.jiuyi.ndr.domain.lplan.LPlanTransLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by WangGang on 2017/4/11.
 */
@Service
public class LPlanTransLogService {

    @Autowired
    private LPlanTransLogDao transLogDao;

    //保存活期交易流水
    public LPlanTransLog saveTransLog(LPlanTransLog log) {
        transLogDao.insert(log);
        return log;
    }

    //查询用户活期交易流水
    public List<LPlanTransLog> findByUserId(String userId, int page, int size) {
        PageHelper.startPage(page,size);
        return transLogDao.findByUserId(userId);
    }

    //根据用户和交易类型查询活期交易流水
    public List<LPlanTransLog> findByUserIdAndTransType(String userId, int transType, int page, int size) {
        PageHelper.startPage(page,size);
        return transLogDao.findByUserIdAndTransType(userId, transType);
    }

    //根据用户和交易类型查询活期交易流水
    public List<LPlanTransLog> findByUserIdAndTransTypes(String userId, int[] transTypes, int page, int size) {
        PageHelper.startPage(page,size);
        return transLogDao.findByUserIdAndTransTypeIn(userId, transTypes);
    }

    //加锁
    public LPlanTransLog findByIdForUpdate(Integer id) {
        return transLogDao.findByIdForUpdate(id);
    }


    public List<LPlanTransLog> findUserPendingTransByType(String userId, Integer investType) {
        return transLogDao.findByUserIdAndTransStatusAndTransTypeOrderByTransTimeAsc(userId, LPlanTransLog.TRANS_STATUS_PENDING, investType);
    }

    public LPlanTransLog update(LPlanTransLog lPlanTransLog) {
        if (lPlanTransLog.getId() == null) {
            throw new IllegalArgumentException("更新活期交易记录id不能为空");
        }
        transLogDao.update(lPlanTransLog);
        return lPlanTransLog;
    }

    public List<LPlanTransLog> findByUserIdAndTransTypeAndTransDate(String userId, Integer transType, String transDate) {
        return transLogDao.findByUserIdAndTransTypeAndTransDate(userId, transType, transDate);
    }
}
