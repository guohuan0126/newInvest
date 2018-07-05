package com.jiuyi.ndr.service.iplan;

import com.github.pagehelper.PageHelper;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.subject.SubjectTransferParam;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditCancelConfirmDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.subject.SubjectTransLogService;
import com.jiuyi.ndr.service.subject.SubjectTransferParamService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.PageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author ke 2017/6/9
 */
@Service
public class IPlanTransLogService {

    private final static Logger logger = LoggerFactory.getLogger(IPlanTransLogService.class);

    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;

    @Autowired
    private SubjectTransferParamService subjectTransferParamService;

    @Autowired
    private CreditOpeningDao creditOpeningDao;

    @Autowired
    private IPlanDao iPlanDao;

    @Autowired
    private IPlanAccountService iPlanAccountService;

    @Autowired
    private SubjectTransLogService subjectTransLogService;

    private DecimalFormat df4 = new DecimalFormat("######0.##");



    /**
     * 查询用户已投资金额
     */
    public Integer findInvestedAmtByUserId(String userId) {
        return null;
    }

    public IPlanTransLog getByIdLocked(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("iPlanTransLog id is can not null");
        }
        return iPlanTransLogDao.findByIdForUpdate(id);
    }


    public List<IPlanTransLog> findByUserId(String userId){
//        iPlanTransLogDao
        return null;
    }
    @ProductSlave
    public List<IPlanTransLog> getByPageHelper(String userId, Integer iPlanId, String transTypes, String transStatuses, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        Set<Integer> transTypesSet = new HashSet(Arrays.asList(transTypes.split(",")));
        Set<Integer> transStatusesSet = new HashSet(Arrays.asList(transStatuses.split(",")));
        return this.getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(userId, iPlanId, transTypesSet, transStatusesSet);
    }
    @ProductSlave
    public List<IPlanTransLog> getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(String userId, Integer iPlanId, Set<Integer> transTypes, Set<Integer> transStatuses){
        if (StringUtils.hasText(userId) && iPlanId != null && !transTypes.isEmpty() && transStatuses != null) {
            return iPlanTransLogDao.findByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(userId, iPlanId, transTypes, transStatuses);
        } else {
            throw new IllegalArgumentException("userId and iPlanId and transTypes and transStatus is can not null");
        }
    }

    @ProductSlave
    public List<IPlanTransLog> getByUserIdAndIPlanTypeAndTransStatusAndTransTypeIn(String userId, Integer iPlanType, Set<Integer> transTypes, Set<Integer> transStatuses){
        if (StringUtils.hasText(userId) && iPlanType != null && !transTypes.isEmpty() && transStatuses != null) {
            return iPlanTransLogDao.getByUserIdAndIPlanTypeAndTransStatusAndTransTypeIn(userId, iPlanType, transTypes, transStatuses);
        } else {
            throw new IllegalArgumentException("userId and iPlanId and transTypes and transStatus is can not null");
        }
    }

    @ProductSlave
    public List<IPlanTransLog> getYjtTransLog(String userId, Integer iPlanId, Set<Integer> transTypes, Set<Integer> transStatuses,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.hasText(userId) && iPlanId != null && !transTypes.isEmpty() && transStatuses != null) {
            return iPlanTransLogDao.findByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(userId, iPlanId, transTypes, transStatuses);
        } else {
            throw new IllegalArgumentException("userId and iPlanId and transTypes and transStatus is can not null");
        }
    }

    public IPlanTransLog insert(IPlanTransLog iPlanTransLog) {
        if (iPlanTransLog == null) {
            throw new IllegalArgumentException("iPlanTransLog can not null");
        }
        iPlanTransLog.setCreateTime(DateUtil.getCurrentDateTime19());
        if (iPlanTransLog.getAutoInvest() == null) {
            iPlanTransLog.setAutoInvest(0);
        }
        iPlanTransLog.setCreateTime(DateUtil.getCurrentDateTime19());
        iPlanTransLogDao.insert(iPlanTransLog);
        return iPlanTransLog;
    }

    public IPlanTransLog update(IPlanTransLog iPlanTransLog) {
        if (iPlanTransLog == null || iPlanTransLog.getId() == null) {
            throw new IllegalArgumentException("iPlanTransLog or iPlanTransLog id can not null when update");
        }
        iPlanTransLog.setUpdateTime(DateUtil.getCurrentDateTime19());
        iPlanTransLogDao.update(iPlanTransLog);
        return iPlanTransLog;
    }
    @ProductSlave
    public List<IPlanTransLog> getByPageHelper(String userId, int flag, Set<Integer> transTypes, Set<Integer> transStatuses, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return this.getByUserIdAndTransTypeInAndTransStatusIn(userId, flag, transTypes, transStatuses);
    }
    @ProductSlave
    public List<IPlanTransLog> getByUserIdAndTransTypeInAndTransStatusIn(String userId, int flag, Set<Integer> transTypes, Set<Integer> transStatuses) {
        return iPlanTransLogDao.findByUserIdAndTransTypeInAndTransStatusIn(userId, flag, transTypes, transStatuses);
    }
    @ProductSlave
    public IPlanTransLog getById(Integer id) {
        return iPlanTransLogDao.findById(id);
    }

    public List<IPlanTransLog> getByIPlanIdAndTransStatusAndTransTypeIn(Integer iPlanId, String transTypes, String transStatuses) {
        Set<Integer> transTypesSet = new HashSet(Arrays.asList(transTypes.split(",")));
        Set<Integer> transStatusesSet = new HashSet(Arrays.asList(transStatuses.split(",")));
        return this.getByIPlanIdAndTransStatusAndTransTypeIn(iPlanId, transTypesSet, transStatusesSet);
    }
    @ProductSlave
    public Integer getSumCountUseRedpacket(String userId, Integer iPlanId, Set<Integer> transTypes, Set<Integer> transStatuses){
        if (StringUtils.hasText(userId) && iPlanId != null && !transTypes.isEmpty() && transStatuses != null) {
            return iPlanTransLogDao.findSumCountUseRedpacket(userId, iPlanId, transTypes, transStatuses);
        } else {
            throw new IllegalArgumentException("userId and iPlanId and transTypes and transStatus is can not null");
        }

    }

    public List<IPlanTransLog> getByAccountIdAndTransStatusAndTransTypeIn(Integer accountId, Set<Integer> transTypes, Set<Integer> transStatuses) {
        if (accountId != null && !transTypes.isEmpty() && transStatuses != null) {
            return iPlanTransLogDao.findByAccountIdAndTransStatusAndTransTypeIn(accountId, transTypes, transStatuses);
        } else {
            throw new IllegalArgumentException("iPlanAccountId or transTypes or transStatus can not null");
        }
    }

    private List<IPlanTransLog> getByIPlanIdAndTransStatusAndTransTypeIn(Integer iPlanId, Set<Integer> transTypes, Set<Integer> transStatuses) {
        if (iPlanId != null && !transTypes.isEmpty() && transStatuses != null) {
            return iPlanTransLogDao.findByIPlanIdAndTransStatusAndTransTypeIn(iPlanId, transTypes, transStatuses);
        } else {
            throw new IllegalArgumentException("userId and iPlanId and transTypes and transStatus is can not null");
        }
    }
    public List<IPlanTransLog> getByIPlanIdAndTransStatusAndTransTypeIns(Integer iPlanId, String transTypes, String transStatuses) {
        Set<Integer> transTypesSet = new HashSet(Arrays.asList(transTypes.split(",")));
        Set<Integer> transStatusesSet = new HashSet(Arrays.asList(transStatuses.split(",")));
        return this.getByIPlanIdAndTransStatusAndTransTypeIns(iPlanId, transTypesSet, transStatusesSet);
    }
    private List<IPlanTransLog> getByIPlanIdAndTransStatusAndTransTypeIns(Integer iPlanId, Set<Integer> transTypes, Set<Integer> transStatuses) {
        if (iPlanId != null && !transTypes.isEmpty() && transStatuses != null) {
            return iPlanTransLogDao.getByIPlanIdAndTransStatusAndTransTypeIns(iPlanId, transTypes, transStatuses);
        } else {
            throw new IllegalArgumentException("userId and iPlanId and transTypes and transStatus is can not null");
        }
    }

    public List<IPlanTransLog> getByAccountIdAndTypeAndStatus(Integer iplanAccountId,Integer status) {
        if (iplanAccountId != null  && status != null) {
            return iPlanTransLogDao.findByAccountIdAndTypeAndStatus(iplanAccountId, status);
        } else {
            throw new IllegalArgumentException("iplanAccountId and transStatus is can not null");
        }
    }

    public List<IPlanTransLog> getByCondition(List<IPlanTransLog> iPlanTransLogs,Integer pageNum,Integer pageSize){
        if(iPlanTransLogs!= null && iPlanTransLogs.size() > 0){
            List<IPlanTransLog> lists = new ArrayList<>();
            for (IPlanTransLog iPlanTransLog : iPlanTransLogs) {
                if(iPlanTransLog.getActualAmt() > 0){
                    lists.add(iPlanTransLog);
                }
            }
            return new PageUtil().ListSplit(lists, pageNum, pageSize);
        }else{
            throw new IllegalArgumentException("iPlanTransLogs is can not null");
        }
    }

    public AppCreditCancelConfirmDto creditTransferConfirm(Map<String, String> map) {
        Integer transLogId = 0;
        String userId = null;
        if (map.containsKey("id") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("id"))){
            transLogId = Integer.valueOf(map.get("id"));
        }
        if (map.containsKey("userId") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("userId"))){
            userId = map.get("userId");
        }
        logger.info("开始调用债权取消确认接口->输入参数:开放中债权ID={}",
                transLogId);
        IPlanTransLog iPlanTransLog = iPlanTransLogDao.findByIdForUpdate(transLogId);
        if (iPlanTransLog == null) {
            throw new ProcessException(Error.NDR_0202);
        }

        AppCreditCancelConfirmDto appCreditCancelConfirmDto = new AppCreditCancelConfirmDto();

        appCreditCancelConfirmDto.setId(transLogId);

        //转出金额
        appCreditCancelConfirmDto.setTransAmt(iPlanTransLog.getTransAmt()/ 100.0);
        appCreditCancelConfirmDto.setTransAmtStr(df4.format(appCreditCancelConfirmDto.getTransAmt()));

        Integer totolAmt = 0;//已被购买总金额
        List<CreditOpening> creditOpenings = creditOpeningDao.findByTransLogId(transLogId);
        for (CreditOpening creditOpening : creditOpenings) {
            if(creditOpening.getIplanId() != null){
                continue;
            }
            totolAmt +=creditOpening.getAvailablePrincipal();
        }
        //已成交金额
        appCreditCancelConfirmDto.setFinishAmt((iPlanTransLog.getTransAmt() - totolAmt) / 100.0);
        appCreditCancelConfirmDto.setFinishAmtStr(df4.format(appCreditCancelConfirmDto.getFinishAmt()));

        //撤销金额
        appCreditCancelConfirmDto.setCancelAmt(totolAmt / 100.0);
        appCreditCancelConfirmDto.setCancelAmtStr(df4.format(appCreditCancelConfirmDto.getCancelAmt()));


        //散标交易配置信息
        //一键投账户
        IPlanAccount iPlanAccount = iPlanAccountService.findById(iPlanTransLog.getAccountId());
        IPlan iPlan = iPlanDao.findById(iPlanAccount.getIplanId());
        SubjectTransferParam subjectTransferParam = subjectTransferParamService.getByTransferParamCode(iPlan.getTransferParamCode());


        //扣除红包奖励
        Double redFee = iPlanAccountService.calcRedFee(iPlanAccount);
        appCreditCancelConfirmDto.setRedFee((appCreditCancelConfirmDto.getFinishAmt())  * redFee);
        appCreditCancelConfirmDto.setRedFeeStr(df4.format(appCreditCancelConfirmDto.getRedFee() ));

        //溢价手续费
        Double overFee = 0.0;
        Double transferDiscount = creditOpeningDao.findByTransLogIdAllNoConditon(transLogId).getTransferDiscount().multiply(new BigDecimal(100)).doubleValue();
        if(transferDiscount > 100){
            overFee = (appCreditCancelConfirmDto.getFinishAmt()) * (transferDiscount - 100) / 100.0 * 0.2;
        }
        appCreditCancelConfirmDto.setOverFee(overFee);
        appCreditCancelConfirmDto.setOverFeeStr(df4.format(overFee));

        //todo 如果是递增利率，不收手续费(随心投修改-jgx-5.16)
        boolean old = true;
        if (iPlan.getRateType() != null && iPlan.getRateType() == 1 && iPlan.getIncreaseRate() != null && iPlan.getIncreaseRate().compareTo(BigDecimal.ZERO) > 0) {
            old = false;
        } else {
            Integer times = subjectTransLogService.getTimes(userId);
            if (times > 0) {
                old = false;
            }
        }

        if (old) {
            //转让服务费
            Double feeRate = iPlanAccountService.calcTransFee(subjectTransferParam,iPlan);
            appCreditCancelConfirmDto.setFee((appCreditCancelConfirmDto.getFinishAmt()) * feeRate / 100.0);
            appCreditCancelConfirmDto.setFeeStr(df4.format(appCreditCancelConfirmDto.getFee()));
        } else {
            appCreditCancelConfirmDto.setFee(0.0);
            appCreditCancelConfirmDto.setFeeStr(df4.format(appCreditCancelConfirmDto.getFee()));
        }

        if(SubjectTransferParam.NEW_IPLAN.equals(subjectTransferParam.getTansferReward())){
            appCreditCancelConfirmDto.setRedFee(0.0);
            appCreditCancelConfirmDto.setRedFeeStr(df4.format(appCreditCancelConfirmDto.getRedFee() ));
        }

        return appCreditCancelConfirmDto;
    }
}

