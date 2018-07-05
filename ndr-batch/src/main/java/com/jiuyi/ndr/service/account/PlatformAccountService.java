package com.jiuyi.ndr.service.account;

import com.jiuyi.ndr.constant.AccountEnum;
import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.dao.account.PlatformAccountDao;
import com.jiuyi.ndr.dao.account.PlatformBillDao;
import com.jiuyi.ndr.domain.account.PlatformAccount;
import com.jiuyi.ndr.domain.account.PlatformBill;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author ke 2017/5/3
 */
@Service
public class PlatformAccountService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PlatformAccountDao platformAccountDao;

    @Autowired
    private PlatformBillDao platformBillDao;

    /**
     * 查询平台账户
     *
     * @return
     */
    @Transactional
    public PlatformAccount getPlatformAccount(String accountName) {
        return platformAccountDao.getByName(accountName);
    }

    /**
     * 入账
     *
     * @param platformAccountName   平台账户
     * @param money                 入账金额
     * @param businessType          业务类型
     * @param typeInfo              描述
     * @param requestNo             流水号
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public BaseResponse transferIn(String platformAccountName, double money, BusinessEnum businessType,
                                   String typeInfo, String requestNo) {

        PlatformAccount account = platformAccountDao.getByName(platformAccountName);

        if (null == account) {
            logger.info("平台账户[{}]不存在", platformAccountName);
            return new BaseResponse("平台账户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        account.setBalance(account.getBalance() + money);
        account.setAvailableBalance(account.getAvailableBalance() + money);
        account.setTime(DateUtil.getCurrentDateTime19());

        platformAccountDao.update(account);
        this.insertPlatformBill(account.getId(), money, account.getBalance(),
                account.getFreezeAmount(), AccountEnum.ti_balance,
                businessType, requestNo, typeInfo);

        return new BaseResponse("入账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }


    /**
     * 入账
     *
     * @param platformAccountName   平台账户
     * @param money                 入账金额
     * @param businessType          业务类型
     * @param typeInfo              描述
     * @param requestNo             流水号
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public BaseResponse transferIn(String platformAccountName, double money, BusinessEnum businessType,
                                   String typeInfo, String requestNo,String subjectId,Integer scheduleId) {

        PlatformAccount account = platformAccountDao.getByName(platformAccountName);

        if (null == account) {
            logger.info("平台账户[{}]不存在", platformAccountName);
            return new BaseResponse("平台账户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        account.setBalance(account.getBalance() + money);
        account.setAvailableBalance(account.getAvailableBalance() + money);
        account.setTime(DateUtil.getCurrentDateTime19());

        platformAccountDao.update(account);
        this.insertPlatformBill(account.getId(), money, account.getBalance(),
                account.getFreezeAmount(), AccountEnum.ti_balance,
                businessType, requestNo, typeInfo,subjectId,scheduleId);

        return new BaseResponse("入账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }



    /**
     * 出账
     *
     * @param platformAccountName   平台账户
     * @param money                 出账金额
     * @param businessType          业务类型
     * @param typeInfo              描述
     * @param requestNo             流水号
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public BaseResponse transferOut(String platformAccountName, double money, BusinessEnum businessType,
                            String typeInfo, String requestNo) {

        PlatformAccount account = platformAccountDao.getByName(platformAccountName);
        if (null == account) {
            logger.info("平台账户[{}]不存在", platformAccountName);
            return new BaseResponse("平台账户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(account.getAvailableBalance(), 2) < ArithUtil.round(money, 2)) {
            logger.info("平台账户[{}]出账失败，账户余额不足", platformAccountName);
            return new BaseResponse("出账失败，账户余额不足", BaseResponse.STATUS_FAILED, requestNo);
        }
        account.setBalance(account.getBalance() - money);
        account.setAvailableBalance(account.getAvailableBalance() - money);
        account.setTime(DateUtil.getCurrentDateTime19());

        platformAccountDao.update(account);
        this.insertPlatformBill(account.getId(), money, account.getBalance(), account.getFreezeAmount(),
                AccountEnum.to_balance, businessType, requestNo, typeInfo);
        return new BaseResponse("出账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 出账
     * @param platformAccountName 平台账户
     * @param money
     * @param businessType
     * @param typeInfo
     * @param requestNo
     * @param subjectId
     * @param scheduleId 还款计划ID
     * @return
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public BaseResponse transferOut(String platformAccountName, double money, BusinessEnum businessType,
                                    String typeInfo, String requestNo,String subjectId,Integer scheduleId) {

        PlatformAccount account = platformAccountDao.getByName(platformAccountName);
        if (null == account) {
            logger.info("平台账户[{}]不存在", platformAccountName);
            return new BaseResponse("平台账户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(account.getAvailableBalance(), 2) < ArithUtil.round(money, 2)) {
            logger.info("平台账户[{}]出账失败，账户余额不足", platformAccountName);
            return new BaseResponse("出账失败，账户余额不足", BaseResponse.STATUS_FAILED, requestNo);
        }
        account.setBalance(account.getBalance() - money);
        account.setAvailableBalance(account.getAvailableBalance() - money);
        account.setTime(DateUtil.getCurrentDateTime19());

        platformAccountDao.update(account);
        this.insertPlatformBill(account.getId(), money, account.getBalance(), account.getFreezeAmount(),
                AccountEnum.to_balance, businessType, requestNo, typeInfo,subjectId,scheduleId);
        return new BaseResponse("出账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 插入平台流水
     *
     * @param money         变动金额
     * @param balance       账户余额
     * @param freezeAmount  冻结金额
     * @param accountType   变动类型类型
     * @param businessType  业务类型
     * @param requestNo     请求流水号
     * @param operatorInfo  操作信息
     */
    private void insertPlatformBill(Integer platformId, double money, double balance, double freezeAmount,
                                    AccountEnum accountType, BusinessEnum businessType, String requestNo, String operatorInfo) {
        // 插入资金流水
        PlatformBill bill = new PlatformBill();
        bill.setPlatformId(platformId);
        bill.setBalance(balance);
        bill.setMoney(money);
        bill.setRequestNo(requestNo);
        bill.setType(accountType.toString());
        bill.setTypeInfo(operatorInfo);
        bill.setFreezeAmount(freezeAmount);
        bill.setBusinessType(businessType.toString());

        bill.setTime(DateUtil.getCurrentDateTime19());
        platformBillDao.insert(bill);
    }

    /**
     * 插入平台流水
     *
     * @param money         变动金额
     * @param balance       账户余额
     * @param freezeAmount  冻结金额
     * @param accountType   变动类型类型
     * @param businessType  业务类型
     * @param requestNo     请求流水号
     * @param operatorInfo  操作信息
     */
    private void insertPlatformBill(Integer platformId, double money, double balance, double freezeAmount,
                                    AccountEnum accountType, BusinessEnum businessType, String requestNo, String operatorInfo,
                                    String subjectId,Integer seheduleId) {
        // 插入资金流水
        PlatformBill bill = new PlatformBill();
        bill.setPlatformId(platformId);
        bill.setBalance(balance);
        bill.setMoney(money);
        bill.setRequestNo(requestNo);
        bill.setType(accountType.toString());
        bill.setTypeInfo(operatorInfo);
        bill.setFreezeAmount(freezeAmount);
        bill.setBusinessType(businessType.toString());
        bill.setScheduleId(seheduleId);
        bill.setSubjectId(subjectId);

        bill.setTime(DateUtil.getCurrentDateTime19());
        platformBillDao.insert(bill);
    }

    /**
     * 冻结
     *
     * @param platformAccountName   平台账户
     * @param money                 冻结金额
     * @param businessType          业务类型
     * @param typeInfo              描述
     * @param requestNo             流水号
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public BaseResponse freeze(String platformAccountName, double money, BusinessEnum businessType,
                               String typeInfo, String requestNo) {

        PlatformAccount account = platformAccountDao.getByName(platformAccountName);

        if (null == account) {
            logger.info("平台账户[{}]不存在", platformAccountName);
            return new BaseResponse("平台账户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        account.setAvailableBalance(account.getAvailableBalance() - money);
        account.setFreezeAmount(account.getFreezeAmount() + money);
        account.setTime(DateUtil.getCurrentDateTime19());

        platformAccountDao.update(account);
        this.insertPlatformBill(account.getId(), money, account.getBalance(),
                account.getFreezeAmount(), AccountEnum.freeze,
                businessType, requestNo, typeInfo);

        return new BaseResponse("冻结成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }
    /**
     * 冻结中转出
     *
     * @param platformAccountName   平台账户
     * @param money                 冻结中转出金额
     * @param businessType          业务类型
     * @param typeInfo              描述
     * @param requestNo             流水号
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public BaseResponse toFreeze(String platformAccountName, double money, BusinessEnum businessType,
                                 String typeInfo, String requestNo) {

        PlatformAccount account = platformAccountDao.getByName(platformAccountName);

        if (null == account) {
            logger.info("平台账户[{}]不存在", platformAccountName);
            return new BaseResponse("平台账户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        account.setBalance(account.getBalance() - money);
        account.setFreezeAmount(account.getFreezeAmount() - money);
        account.setTime(DateUtil.getCurrentDateTime19());

        platformAccountDao.update(account);
        this.insertPlatformBill(account.getId(), money, account.getBalance(),
                account.getFreezeAmount(), AccountEnum.to_frozen,
                businessType, requestNo, typeInfo);

        return new BaseResponse("从冻结中转出成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 冻结中转出
     *
     * @param platformAccountName   平台账户
     * @param money                 冻结中转出金额
     * @param businessType          业务类型
     * @param typeInfo              描述
     * @param requestNo             流水号
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public BaseResponse toFreeze(String platformAccountName, double money, BusinessEnum businessType,
                                 String typeInfo, String requestNo,String subjectId,Integer scheduleId) {

        PlatformAccount account = platformAccountDao.getByName(platformAccountName);

        if (null == account) {
            logger.info("平台账户[{}]不存在", platformAccountName);
            return new BaseResponse("平台账户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        account.setBalance(account.getBalance() - money);
        account.setFreezeAmount(account.getFreezeAmount() - money);
        account.setTime(DateUtil.getCurrentDateTime19());

        platformAccountDao.update(account);
        this.insertPlatformBill(account.getId(), money, account.getBalance(),
                account.getFreezeAmount(), AccountEnum.to_frozen,
                businessType, requestNo, typeInfo,subjectId,scheduleId);

        return new BaseResponse("从冻结中转出成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 冻结
     *
     * @param platformAccountName   平台账户
     * @param money                 冻结金额
     * @param businessType          业务类型
     * @param typeInfo              描述
     * @param requestNo             流水号
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public BaseResponse freeze(String platformAccountName, double money, BusinessEnum businessType,
                               String typeInfo, String requestNo,String subjectId,Integer scheduleId) {

        PlatformAccount account = platformAccountDao.getByName(platformAccountName);

        if (null == account) {
            logger.info("平台账户[{}]不存在", platformAccountName);
            return new BaseResponse("平台账户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        account.setAvailableBalance(account.getAvailableBalance() - money);
        account.setFreezeAmount(account.getFreezeAmount() + money);
        account.setTime(DateUtil.getCurrentDateTime19());

        platformAccountDao.update(account);
        this.insertPlatformBill(account.getId(), money, account.getBalance(),
                account.getFreezeAmount(), AccountEnum.freeze,
                businessType, requestNo, typeInfo,subjectId,scheduleId);

        return new BaseResponse("冻结成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }
}
