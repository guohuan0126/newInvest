package com.jiuyi.ndr.service.account;

import com.jiuyi.ndr.dao.account.BrwForCpsLogDao;
import com.jiuyi.ndr.domain.account.BrwForCpsLog;
import com.jiuyi.ndr.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 借款人还代偿流水服务
 * Created by lixiaolei on 2017/9/7.
 */
@Service
public class BrwForCpsLogService {

    private final static Logger logger = LoggerFactory.getLogger(BrwForCpsLogService.class);

    @Autowired
    private BrwForCpsLogDao brwForCpsLogDao;

    public List<BrwForCpsLog> getByRepayBillIdAndExtStatusesAndStatus(Integer repayBillId, Set<Integer> extStatuses, Integer status) {
        if (repayBillId == null || CollectionUtils.isEmpty(extStatuses) || status == null) {
            throw new IllegalArgumentException("repayBillId or extStatuses or status can't be null");
        }
        return brwForCpsLogDao.findByRepayBillIdAndExtStatusesAndStatus(repayBillId, extStatuses, status);
    }

    public List<BrwForCpsLog> getByStatusAndExtStatus(Integer status, Integer extStatus) {
        if (extStatus == null || status == null) {
            throw new IllegalArgumentException("extStatus or status can't be null");
        }
        return brwForCpsLogDao.findByStatusAndExtStatus(status, extStatus);
    }

    public BrwForCpsLog getByRepayBillIdAndExtStatusAndStatus(Integer repayBillId, Integer extStatus, Integer status) {
        if (repayBillId == null || extStatus == null || status == null) {
            throw new IllegalArgumentException("repayBillId or extStatus or status can't be null");
        }
        return brwForCpsLogDao.findByRepayBillIdAndExtStatusAndStatus(repayBillId, extStatus, status);
    }

    public BrwForCpsLog insert(Integer scheduleId, String subjectId, Integer term, String borrowerId, Integer repayBillId, String account,
                               String extSn, Integer extStatus, Integer status, Integer repayAmt, Integer derateReturnAmt, Integer offlineAmt) {
        BrwForCpsLog brwForCpsLog = new BrwForCpsLog();
        brwForCpsLog.setScheduleId(scheduleId);
        brwForCpsLog.setSubjectId(subjectId);
        brwForCpsLog.setTerm(term);
        brwForCpsLog.setBorrowerId(borrowerId);
        brwForCpsLog.setRepayBillId(repayBillId);
        brwForCpsLog.setAccount(account);
        brwForCpsLog.setExtSn(extSn);
        brwForCpsLog.setExtStatus(extStatus);
        brwForCpsLog.setStatus(status);
        brwForCpsLog.setRepayAmt(repayAmt);
        brwForCpsLog.setDerateReturnAmt(derateReturnAmt);
        brwForCpsLog.setOfflineAmt(offlineAmt);
        brwForCpsLogDao.insert(brwForCpsLog);
        return brwForCpsLog;
    }

    public int update(String extSn, Integer extStatus, Integer status, Integer id) {
        return brwForCpsLogDao.update(extSn, extStatus, status, DateUtil.getCurrentDateTime19(), id);
    }

    public int updateForStatus(Integer status, Integer id) {
        return brwForCpsLogDao.updateForStatus(status, DateUtil.getCurrentDateTime19(), id);
    }
    public BrwForCpsLog  findByscheduleIdAndBillIdAndStatus(Integer scheduleId,Integer billId,Integer status){
        return brwForCpsLogDao.findByscheduleIdAndBillIdAndStatus(scheduleId,billId,status);
    }

    public List<BrwForCpsLog> findByscheduleIdAndSubjectId(Integer scheduleId,String subjectId,Integer status){
        return brwForCpsLogDao.findByscheduleIdAndSubjectId(scheduleId,subjectId,status);
    };
}
