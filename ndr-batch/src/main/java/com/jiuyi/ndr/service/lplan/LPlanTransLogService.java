package com.jiuyi.ndr.service.lplan;

import com.jiuyi.ndr.dao.lplan.LPlanTransLogDao;
import com.jiuyi.ndr.domain.lplan.LPlanTransLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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

    //加锁
    public LPlanTransLog findByIdForUpdate(Integer id) {
        return transLogDao.findByIdForUpdate(id);
    }


    public LPlanTransLog update(LPlanTransLog lPlanTransLog) {
        if (lPlanTransLog.getId() == null) {
            throw new IllegalArgumentException("更新活期交易记录id不能为空");
        }
        transLogDao.update(lPlanTransLog);
        return lPlanTransLog;
    }
}
