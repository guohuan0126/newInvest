package com.jiuyi.ndr.service.account;

import com.jiuyi.ndr.dao.account.CompensatoryAcctLogDao;
import com.jiuyi.ndr.dao.account.PlatformAccountDao;
import com.jiuyi.ndr.domain.account.CompensatoryAcctLog;
import com.jiuyi.ndr.domain.account.PlatformAccount;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


/**
 * 代偿账户流水服务
 * Created by lixiaolei on 2017/9/5.
 */
@Service
public class CompensatoryAcctLogService {

    @Autowired
    private CompensatoryAcctLogDao compensatoryAcctLogDao;
    @Autowired
    private PlatformAccountDao platformAccountDao;
    @Autowired
    private UserAccountService userAccountService;

    /**
     * 插入代偿流水
     * @param scheduleId
     * @param subjectId
     * @param term
     * @param amount
     * @param extSn
     * @param extStatus
     * @param type
     * @return
     */
    public CompensatoryAcctLog log(Integer scheduleId, String subjectId, Integer term, Integer repayBillId, String account, Integer amount, String extSn, Integer extStatus, Integer type) {
//        PlatformAccount cpsAccount = platformAccountDao.getByName(account);
        UserAccount cpsAccount = userAccountService.findUserAccount(account);
        CompensatoryAcctLog compensatoryAcctLog = new CompensatoryAcctLog();
        compensatoryAcctLog.setScheduleId(scheduleId);
        compensatoryAcctLog.setSubjectId(subjectId);
        compensatoryAcctLog.setTerm(term);
        compensatoryAcctLog.setRepayBillId(repayBillId);
        compensatoryAcctLog.setAccount(account);
        if (Arrays.asList(CompensatoryAcctLog.TYPE_CPS_OUT, CompensatoryAcctLog.TYPE_DERATE_RETURN_OUT, CompensatoryAcctLog.TYPE_OFFLINE_OUT, CompensatoryAcctLog.TYPE_CONTINUE_OUT,CompensatoryAcctLog.TYPE_CPS_RECORD_OUT).stream().anyMatch(i -> i.equals(type))) {
            compensatoryAcctLog.setAmount(-amount);
            compensatoryAcctLog.setBalance(BigDecimal.valueOf(cpsAccount.getBalance()*100).intValue() - amount);
        } else {
            compensatoryAcctLog.setAmount(amount);
            compensatoryAcctLog.setBalance(BigDecimal.valueOf(cpsAccount.getBalance()*100).intValue() + amount);
        }
        compensatoryAcctLog.setExtSn(extSn);
        compensatoryAcctLog.setExtStatus(extStatus);
        compensatoryAcctLog.setType(type);
        compensatoryAcctLog.setProfit(0);
        compensatoryAcctLog.setStatus(extStatus.equals(BaseResponse.STATUS_SUCCEED)?CompensatoryAcctLog.STATUS_HANDLED_LOCAL_FREEZE:CompensatoryAcctLog.STATUS_NOT_HANDLED);
        this.insert(compensatoryAcctLog);
        return compensatoryAcctLog;
    }
    public CompensatoryAcctLog logLend(Integer scheduleId, String subjectId, Integer term, Integer repayBillId, String account, Integer amount, String extSn, Integer extStatus, Integer type) {
//        PlatformAccount cpsAccount = platformAccountDao.getByName(account);
        UserAccount cpsAccount = userAccountService.findUserAccount(account);
        CompensatoryAcctLog compensatoryAcctLog = new CompensatoryAcctLog();
        compensatoryAcctLog.setScheduleId(scheduleId);
        compensatoryAcctLog.setSubjectId(subjectId);
        compensatoryAcctLog.setTerm(term);
        compensatoryAcctLog.setRepayBillId(repayBillId);
        compensatoryAcctLog.setAccount(account);
        if (Arrays.asList(CompensatoryAcctLog.TYPE_CPS_OUT, CompensatoryAcctLog.TYPE_DERATE_RETURN_OUT, CompensatoryAcctLog.TYPE_OFFLINE_OUT, CompensatoryAcctLog.TYPE_CONTINUE_OUT,CompensatoryAcctLog.TYPE_CPS_RECORD_OUT).stream().anyMatch(i -> i.equals(type))) {
            compensatoryAcctLog.setAmount(-amount);
            compensatoryAcctLog.setBalance(BigDecimal.valueOf(cpsAccount.getBalance()*100).intValue() - amount);
        } else {
            compensatoryAcctLog.setAmount(amount);
            compensatoryAcctLog.setBalance(BigDecimal.valueOf(cpsAccount.getBalance()*100).intValue() + amount);
        }
        compensatoryAcctLog.setExtSn(extSn);
        compensatoryAcctLog.setExtStatus(extStatus);
        compensatoryAcctLog.setType(type);
        compensatoryAcctLog.setProfit(0);
        compensatoryAcctLog.setStatus(CompensatoryAcctLog.STATUS_HANDLED_LOCAL_TOFREEZE);
        this.insert(compensatoryAcctLog);
        return compensatoryAcctLog;
    }

    public List<CompensatoryAcctLog> getCpsAcctLogs (Integer status, Integer extStatus) {
        if (status == null || extStatus == null) {
            throw new IllegalArgumentException("status or extStatus can not is null or <= 0");
        }
        return compensatoryAcctLogDao.selectByStatusAndExtStatus(status, extStatus);
    }

    public CompensatoryAcctLog getCpsAcctLog(Integer repayBillId, Integer type, Integer extStatus, Integer status) {
        if (repayBillId == null || type == null || extStatus == null || status == null) {
            throw new IllegalArgumentException("repayBillId or type or extStatus or status can not is null or <= 0");
        }
        return compensatoryAcctLogDao.selectByRepayBillIdAndTypeAndExtStatusAndStatus(repayBillId, type, extStatus, status);
    }

    public CompensatoryAcctLog getCpsAcctLogByScheduleId(Integer scheduleId, Integer type, Integer extStatus, Integer status) {
        if (scheduleId == null || type == null || extStatus == null || status == null) {
            throw new IllegalArgumentException("scheduleId or type or extStatus or status can not is null or <= 0");
        }
        return compensatoryAcctLogDao.selectByScheduleIdAndTypeAndExtStatusAndStatus(scheduleId, type, extStatus, status);
    }

    public List<CompensatoryAcctLog> getCpsAcctLogsByStatusAndScheduleId(Integer scheduleId,Integer status,Integer extStatus) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new IllegalArgumentException("scheduleId can not is null or <= 0");
        }
        return compensatoryAcctLogDao.selectByScheduleId(scheduleId,status,extStatus);
    }

    public int update(Integer extStatus, Integer status, Integer id) {
        if (id == null || extStatus == null || status == null) {
            throw new IllegalArgumentException("id or extStatus or status can not is null or <= 0");
        }
        return compensatoryAcctLogDao.update(extStatus, status, DateUtil.getCurrentDateTime19(), id);
    }

    public CompensatoryAcctLog update(CompensatoryAcctLog compensatoryAcctLog) {
        if (compensatoryAcctLog.getId() == null || compensatoryAcctLog.getId() <= 0) {
            throw new IllegalArgumentException("scheduleId can not is null or <= 0");
        }
        compensatoryAcctLogDao.updateAll(compensatoryAcctLog);
        return compensatoryAcctLog;
    }

    public CompensatoryAcctLog insert(CompensatoryAcctLog compensatoryAcctLog) {
        if (compensatoryAcctLog == null) {

        }
        if (StringUtils.isBlank(compensatoryAcctLog.getSubjectId())) {

        }
        compensatoryAcctLogDao.insert(compensatoryAcctLog);
        return compensatoryAcctLog;
    }

    public CompensatoryAcctLog getCpsAcctLogsByScheduleIdAndType(Integer scheduleId,Integer type){
        return  compensatoryAcctLogDao.selectByScheduleIdAndType(scheduleId,type);
    }
    //更新代偿出的流水
    public int updateProfit(Integer profit, Integer scheduleId, Integer type) {
        if (scheduleId == null || type == null || profit == null) {
            throw new IllegalArgumentException("scheduleId or type or profit can not is null or <= 0");
        }
        CompensatoryAcctLog comLog = this.getCpsAcctLogsByScheduleIdAndType(scheduleId,CompensatoryAcctLog.TYPE_CPS_OUT);
        if(comLog!=null){
            profit +=comLog.getProfit();
        }
        return compensatoryAcctLogDao.updateProfit(profit, DateUtil.getCurrentDateTime19(), scheduleId,type);
    }

    public int updateByStatusAndId(Integer status, Integer id) {
        if (id == null ||  status == null) {
            throw new IllegalArgumentException("id or  status can not is null or <= 0");
        }
        return compensatoryAcctLogDao.updateByStatusAndId(status, DateUtil.getCurrentDateTime19(), id);
    }
}
