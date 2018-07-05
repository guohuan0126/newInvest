package com.jiuyi.ndr.service.xm;

import com.jiuyi.ndr.dao.xm.TransLogDao;
import com.jiuyi.ndr.domain.xm.TransLog;
import com.jiuyi.ndr.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lixiaolei on 2017/4/20.
 */
@Service
public class TransLogService {

    @Autowired
    private TransLogDao transLogDao;

    /**
     * 添加交易记录
     *
     * @param transLog
     * @return
     */
    public TransLog add(TransLog transLog) {
        transLog.setRequestTime(DateUtil.getCurrentDateTime());
        transLog.setCreateTime(DateUtil.getCurrentDateTime19());
        transLogDao.insert(transLog);
        return transLog;
    }

    /**
     * 根据条件更新交易记录
     *
     * @param transLogId
     * @param responsePacket
     * @param respCode
     * @param respMsg
     * @return
     */
    public TransLog update(Integer transLogId, String responsePacket, Integer status, String respCode, String respMsg) {
        TransLog transLog = this.getTransLog(transLogId);
        transLog.setResponsePacket(responsePacket);
        transLog.setStatus(status);
        transLog.setRespCode(respCode);
        transLog.setRespMsg(respMsg);
        return this.update(transLog);
    }

    /**
     * 更新交易记录
     *
     * @param transLog
     * @return
     */
    public TransLog update(TransLog transLog) {
        if (transLog.getId() == null) {
            throw new IllegalArgumentException("更新交易记录id不能为空");
        }
        transLog.setUpdateTime(DateUtil.getCurrentDateTime19());
        transLogDao.update(transLog);
        return transLog;
    }

    /**
     * 根据id查询交易记录
     *
     * @param transLogId
     * @return
     */
    public TransLog getTransLog(Integer transLogId) {
        if (transLogId == null) {
            throw new IllegalArgumentException("查询交易记录id不能为空");
        }
        return transLogDao.findById(transLogId);
    }

}
