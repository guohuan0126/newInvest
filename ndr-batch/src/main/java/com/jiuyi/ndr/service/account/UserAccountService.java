package com.jiuyi.ndr.service.account;

import com.jiuyi.ndr.constant.AccountEnum;
import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.dao.account.UserAccountDao;
import com.jiuyi.ndr.dao.account.UserBillDao;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.account.UserBill;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author ke 2017/5/3
 */
@Service
public class UserAccountService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserAccountDao userAccountDao;

    @Autowired
    private UserBillDao userBillDao;

    @Autowired
    private UserService userService;

    /**
     * 查询用户账户
     *
     * @param userId
     * @return
     */
    @Transactional
    public UserAccount getUserAccount(String userId) {
        return userAccountDao.getByUserId(userId);
    }

    public UserAccount findUserAccount(String userId) {
        return userAccountDao.findByUserId(userId);
    }

    public UserBill getUserBillByUserId(UserBill userBill) {
        if (userBill == null) {
            throw new IllegalArgumentException("userBill can not be null");
        }
        return userAccountDao.getUserBill(userBill);
    }
    /**
     * 根据参数查询用户资金冻结流水
     * @param userBill
     * @return
     */
    public List<UserBill> getUserBillListByUserId(UserBill userBill) {
        if (userBill == null) {
            throw new IllegalArgumentException("userBill can not be null");
        }
        return userAccountDao.getUserBillList(userBill);
    }

    /**
     * 入账
     *
     * @param userId            用户id
     * @param money             入账金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse transferIn(String userId, Double money, BusinessEnum businessType,
                                   String operatorInfo, String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() + money);
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() + money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.ti_balance, businessType, requestNo, operatorDetail, operatorInfo);

        return new BaseResponse("入账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 入账
     *
     * @param userId            用户id
     * @param money             入账金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse transferIn(String userId, Double money, BusinessEnum businessType,
                                   String operatorInfo, String operatorDetail, String requestNo,String subjectId,Integer scheduleId) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() + money);
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() + money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.ti_balance, businessType, requestNo, operatorDetail, operatorInfo,subjectId,scheduleId);

        return new BaseResponse("入账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }
    /**
     * 入账
     *
     * @param userId            用户id
     * @param money             入账金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse transferIn(String userId, Double money, BusinessEnum businessType,
                                   String operatorInfo, String operatorDetail, String requestNo,String subjectId,Integer scheduleId,int isVisiable) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() + money);
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() + money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.ti_balance, businessType, requestNo, operatorDetail, operatorInfo,isVisiable,subjectId,scheduleId);

        return new BaseResponse("入账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }
    /**
     * 入账
     *
     * @param userId            用户id
     * @param money             入账金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse transferInForBonus(String userId, Double money, BusinessEnum businessType,
                                   String operatorInfo, String operatorDetail, String requestNo,String subjectId,Integer scheduleId,int isVisiable) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() + money);
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() + money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.pt_balance, businessType, requestNo, operatorDetail, operatorInfo,isVisiable,subjectId,scheduleId);

        return new BaseResponse("入账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 入账
     *
     * @param userId            用户id
     * @param money             入账金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse transferIn(String userId, Double money, BusinessEnum businessType,
                                   String operatorInfo, String operatorDetail, String requestNo,String subjectId) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() + money);
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() + money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.ti_balance, businessType, requestNo, operatorDetail, operatorInfo,1,subjectId);

        return new BaseResponse("入账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 入账前台不展示流水
     *
     * @param userId            用户id
     * @param money             入账金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse transferInForNotShow(String userId, Double money, BusinessEnum businessType,
                                             String operatorInfo, String operatorDetail, String requestNo) {
        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() + money);
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() + money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.ti_balance, businessType, requestNo, operatorDetail, operatorInfo,0);

        return new BaseResponse("入账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 入账
     *
     * @param userId            用户id
     * @param money             入账金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse tiFreeze(String userId, Double money, BusinessEnum businessType,
                                   String operatorInfo, String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() + money);
        //更新冻结金额
        userAccount.setFreezeAmount(userAccount.getFreezeAmount() + money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.ti_freeze, businessType, requestNo, operatorDetail, operatorInfo,0);

        return new BaseResponse("入账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }
    /**
     * 入账
     *
     * @param userId            用户id
     * @param money             入账金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse transferIn(String userId, Double money, BusinessEnum businessType,
                                   String operatorInfo, String operatorDetail, String requestNo,Integer isVisible) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() + money);
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() + money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.ti_balance, businessType, requestNo, operatorDetail, operatorInfo,isVisible);

        return new BaseResponse("入账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }


    /**
     * 出账
     *
     * @param userId            用户id
     * @param money             出账金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    @Transactional
    public BaseResponse transferOut(String userId, Double money, BusinessEnum businessType,
                                    String operatorInfo, String operatorDetail, String requestNo,Integer isVisible) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getAvailableBalance(), 2) < ArithUtil.round(money, 2)){
            logger.info("出账失败，用户[{}]账户可用余额不足",userId);
            return new BaseResponse("出账失败，账户可用余额不足", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() - money);
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() - money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.to_balance, businessType, requestNo, operatorDetail, operatorInfo,isVisible);

        return new BaseResponse("出账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }
    /**
     * 出账
     *
     * @param userId            用户id
     * @param money             出账金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse transferOut(String userId, Double money, BusinessEnum businessType,
                                    String operatorInfo, String operatorDetail, String requestNo,String subjectId,Integer scheduleId) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getAvailableBalance(), 2) < ArithUtil.round(money, 2)){
            logger.info("出账失败，用户[{}]账户可用余额不足",userId);
            return new BaseResponse("出账失败，账户可用余额不足", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() - money);
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() - money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.to_balance, businessType, requestNo, operatorDetail, operatorInfo,subjectId,scheduleId);

        return new BaseResponse("出账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }
    /**
     * 出账
     *
     * @param userId            用户id
     * @param money             出账金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse transferOut(String userId, Double money, BusinessEnum businessType,
                                    String operatorInfo, String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getAvailableBalance(), 2) < ArithUtil.round(money, 2)){
            logger.info("出账失败，用户[{}]账户可用余额不足",userId);
            return new BaseResponse("出账失败，账户可用余额不足", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() - money);
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() - money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.to_balance, businessType, requestNo, operatorDetail, operatorInfo);

        return new BaseResponse("出账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 出账
     *
     * @param userId            用户id
     * @param money             出账金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse transferOutForNotShow(String userId, Double money, BusinessEnum businessType,
                                    String operatorInfo, String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getAvailableBalance(), 2) < ArithUtil.round(money, 2)){
            logger.info("出账失败，用户[{}]账户可用余额不足",userId);
            return new BaseResponse("出账失败，账户可用余额不足", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() - money);
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() - money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.to_balance, businessType, requestNo, operatorDetail, operatorInfo,0);

        return new BaseResponse("出账成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }


    /**
     * 冻结资金
     *
     * @param userId            用户id
     * @param money             冻结金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse freeze(String userId, double money, BusinessEnum businessType, String operatorInfo,
                               String operatorDetail, String requestNo, String subjectId,Integer scheduleId) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getAvailableBalance(), 2) < ArithUtil.round(money, 2)) {
            logger.info("冻结失败，用户[{}]账户可用余额不足",userId);
            return new BaseResponse("冻结失败，账户可用余额不足", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() - money);
        // 更新冻结金额
        userAccount.setFreezeAmount(userAccount.getFreezeAmount() + money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.freeze, businessType, requestNo, operatorDetail, operatorInfo,0,subjectId,scheduleId);

        return new BaseResponse("冻结成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 冻结资金
     *
     * @param userId            用户id
     * @param money             冻结金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse freeze(String userId, double money, BusinessEnum businessType, String operatorInfo,
                               String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getAvailableBalance(), 2) < ArithUtil.round(money, 2)) {
            logger.info("冻结失败，用户[{}]账户可用余额不足",userId);
            return new BaseResponse("冻结失败，账户可用余额不足", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() - money);
        // 更新冻结金额
        userAccount.setFreezeAmount(userAccount.getFreezeAmount() + money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.freeze, businessType, requestNo, operatorDetail, operatorInfo,0);

        return new BaseResponse("冻结成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }
    /**
     * 冻结资金
     *
     * @param userId            用户id
     * @param money             冻结金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    public BaseResponse freezeInvestIplan(String userId, double money, BusinessEnum businessType, String operatorInfo,
                               String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getAvailableBalance(), 2) < ArithUtil.round(money, 2)) {
            logger.info("冻结失败，用户[{}]账户可用余额不足",userId);
            return new BaseResponse("冻结失败，账户可用余额不足", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() - money);
        // 更新冻结金额
        userAccount.setFreezeAmount(userAccount.getFreezeAmount() + money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.freeze, businessType, requestNo, operatorDetail, operatorInfo,1);

        return new BaseResponse("冻结成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 冻结资金前台不展示流水
     *
     * @param userId            用户id
     * @param money             冻结金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse freezeForNotShow(String userId, double money, BusinessEnum businessType, String operatorInfo,
                                         String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getAvailableBalance(), 2) < ArithUtil.round(money, 2)) {
            logger.info("冻结失败，用户[{}]账户可用余额不足",userId);
            return new BaseResponse("冻结失败，账户可用余额不足", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() - money);
        // 更新冻结金额
        userAccount.setFreezeAmount(userAccount.getFreezeAmount() + money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.freeze, businessType, requestNo, operatorDetail, operatorInfo,0);

        return new BaseResponse("冻结成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 解冻资金
     *
     * @param userId            用户id
     * @param money             冻结金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse  unfreeze(String userId, double money, BusinessEnum businessType, String operatorInfo,
                                  String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);

        if (null == userAccount) {
            logger.info("该用户不存在[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getFreezeAmount(), 2) < ArithUtil.round(money, 2)){
            logger.info("解冻失败，解冻金额大于已冻结金额，用户[{}]",userId);
            return new BaseResponse("解冻失败，解冻金额大于已冻结金额", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() + money);
        // 更新冻结金额
        userAccount.setFreezeAmount(userAccount.getFreezeAmount() - money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.unfreeze, businessType, requestNo, operatorDetail, operatorInfo,0);

        return new BaseResponse("解冻成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }
    /**
     * 解冻资金
     *
     * @param userId            用户id
     * @param money             冻结金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    @Transactional
    public BaseResponse  unfreezeInvestIplan(String userId, double money, BusinessEnum businessType, String operatorInfo,
                                  String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);

        if (null == userAccount) {
            logger.info("该用户不存在[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getFreezeAmount(), 2) < ArithUtil.round(money, 2)){
            logger.info("解冻失败，解冻金额大于已冻结金额，用户[{}]",userId);
            return new BaseResponse("解冻失败，解冻金额大于已冻结金额", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() + money);
        // 更新冻结金额
        userAccount.setFreezeAmount(userAccount.getFreezeAmount() - money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.unfreeze, businessType, requestNo, operatorDetail, operatorInfo,0);

        return new BaseResponse("解冻成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }
    /**
     * 解冻资金
     *
     * @param userId            用户id
     * @param money             冻结金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse  unfreezeForShow(String userId, double money, BusinessEnum businessType, String operatorInfo,
                                  String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);

        if (null == userAccount) {
            logger.info("该用户不存在[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getFreezeAmount(), 2) < ArithUtil.round(money, 2)){
            logger.info("解冻失败，解冻金额大于已冻结金额，用户[{}]",userId);
            return new BaseResponse("解冻失败，解冻金额大于已冻结金额", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() + money);
        // 更新冻结金额
        userAccount.setFreezeAmount(userAccount.getFreezeAmount() - money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.unfreeze, businessType, requestNo, operatorDetail, operatorInfo,1);

        return new BaseResponse("解冻成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse  unfreezeForNotShow(String userId, double money, BusinessEnum businessType, String operatorInfo,
                                         String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);

        if (null == userAccount) {
            logger.info("该用户不存在[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getFreezeAmount(), 2) < ArithUtil.round(money, 2)){
            logger.info("解冻失败，解冻金额大于已冻结金额，用户[{}]",userId);
            return new BaseResponse("解冻失败，解冻金额大于已冻结金额", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新可用余额
        userAccount.setAvailableBalance(userAccount.getAvailableBalance() + money);
        // 更新冻结金额
        userAccount.setFreezeAmount(userAccount.getFreezeAmount() - money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());

        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.unfreeze, businessType, requestNo, operatorDetail, operatorInfo,0);

        return new BaseResponse("解冻成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 从冻结中转出
     *
     * @param userId            用户id
     * @param money             转出金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse tofreeze(String userId, double money, BusinessEnum businessType, String operatorInfo,
                         String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getFreezeAmount(), 2) < ArithUtil.round(money, 2)){
            money = userAccount.getFreezeAmount();
            logger.info("解冻金额大于已冻结金额，则解冻所有的已冻结金额[{}]", money);
//            return new BaseResponse("冻结中转出失败，解冻金额大于已冻结金额", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() - money);
        // 更新冻结金额
        userAccount.setFreezeAmount(userAccount.getFreezeAmount() - money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());
        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.to_frozen, businessType, requestNo, operatorDetail, operatorInfo,0);

        return new BaseResponse("冻结中转出成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }
    /**
     * 从冻结中转出
     *
     * @param userId            用户id
     * @param money             转出金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse tofreeze(String userId, double money, BusinessEnum businessType, String operatorInfo,
                                 String operatorDetail, String requestNo,String subjectId) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getFreezeAmount(), 2) < ArithUtil.round(money, 2)){
            money = userAccount.getFreezeAmount();
            logger.info("解冻金额大于已冻结金额，则解冻所有的已冻结金额[{}]", money);
//            return new BaseResponse("冻结中转出失败，解冻金额大于已冻结金额", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() - money);
        // 更新冻结金额
        userAccount.setFreezeAmount(userAccount.getFreezeAmount() - money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());
        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.to_frozen, businessType, requestNo, operatorDetail, operatorInfo,0,subjectId);

        return new BaseResponse("冻结中转出成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 从冻结中转出
     *
     * @param userId            用户id
     * @param money             转出金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse tofreeze(String userId, double money, BusinessEnum businessType, String operatorInfo,
                                 String operatorDetail, String requestNo,String subjectId,Integer scheduleId) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getFreezeAmount(), 2) < ArithUtil.round(money, 2)){
            money = userAccount.getFreezeAmount();
            logger.info("解冻金额大于已冻结金额，则解冻所有的已冻结金额[{}]", money);
//            return new BaseResponse("冻结中转出失败，解冻金额大于已冻结金额", BaseResponse.STATUS_FAILED, requestNo);
        }
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() - money);
        // 更新冻结金额
        userAccount.setFreezeAmount(userAccount.getFreezeAmount() - money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());
        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.to_frozen, businessType, requestNo, operatorDetail, operatorInfo,0,subjectId,scheduleId);

        return new BaseResponse("冻结中转出成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 从冻结中转出
     *
     * @param userId            用户id
     * @param money             转出金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse tofreeze(String userId, double money, BusinessEnum businessType, String operatorInfo,
                                 String operatorDetail, String requestNo,String subjectId,Integer scheduleId,
                                 Double principal,Double interest,Double commission) {

        UserAccount userAccount = userAccountDao.getByUserId(userId);
        if (null == userAccount) {
            logger.info("该用户不存在！[{}]", userId);
            return new BaseResponse("用户不存在", BaseResponse.STATUS_FAILED, requestNo);
        }
        if (ArithUtil.round(userAccount.getFreezeAmount(), 2) < ArithUtil.round(money, 2)){
            money = userAccount.getFreezeAmount();
            logger.info("解冻金额大于已冻结金额，则解冻所有的已冻结金额[{}]", money);
        }
        // 更新总金额
        userAccount.setBalance(userAccount.getBalance() - money);
        // 更新冻结金额
        userAccount.setFreezeAmount(userAccount.getFreezeAmount() - money);
        // 设置最后更新时间
        userAccount.setTime(DateUtil.getCurrentDateTime19());
        userAccountDao.update(userAccount);
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.to_frozen, businessType, requestNo, operatorDetail, operatorInfo,0,subjectId,scheduleId,
                principal,interest,commission);

        return new BaseResponse("冻结中转出成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }
    /**
     * 插入资金流水
     *
     * @param userId            用户id
     * @param money             变动资金
     * @param balance           账户余额
     * @param freezeAmount      冻结金额
     * @param accountType       变动类型
     * @param businessType      业务类型
     * @param requestNo         流水号
     * @param operatorDetail    描述
     * @param operatorInfo      描述(用户看)
     */
    private void insertUserBill(String userId, double money, double balance,
                                double freezeAmount, AccountEnum accountType,
                                BusinessEnum businessType, String requestNo, String operatorDetail,
                                String operatorInfo) {
        // 插入资金流水
        UserBill userBill = new UserBill();
        userBill.setId(IdUtil.randomUUID());
        userBill.setMoney(money);
        userBill.setBalance(balance);
        userBill.setFreezeAmount(freezeAmount);
        userBill.setDetail(operatorDetail);
        userBill.setType(accountType.toString());
        userBill.setTypeInfo(operatorInfo);
        userBill.setBusinessType(businessType.toString());
        userBill.setRequestNo(requestNo);
        userBill.setUserId(userId);
        //时间
        userBill.setTime(DateUtil.getCurrentDateTime19());
        long seqNum = 1L;
        UserBill ub = userBillDao.findFirstByUserIdOrderBySeqNumDesc(userId);
        if (ub != null) {
            seqNum += ub.getSeqNum();
        }
        userBill.setSeqNum(seqNum);
        userBillDao.insert(userBill);
    }

    /**
     * 插入资金流水,(是否展示iSVisible)
     *
     * @param userId            用户id
     * @param money             变动资金
     * @param balance           账户余额
     * @param freezeAmount      冻结金额
     * @param accountType       变动类型
     * @param businessType      业务类型
     * @param requestNo         流水号
     * @param operatorDetail    描述
     * @param operatorInfo      描述(用户看)
     */
    private void insertUserBill(String userId, double money, double balance,
                                double freezeAmount, AccountEnum accountType,
                                BusinessEnum businessType, String requestNo, String operatorDetail,
                                String operatorInfo, int isVisiable) {
        // 插入资金流水
        UserBill userBill = new UserBill();
        userBill.setId(IdUtil.randomUUID());
        userBill.setMoney(money);
        userBill.setBalance(balance);
        userBill.setFreezeAmount(freezeAmount);
        userBill.setDetail(operatorDetail);
        userBill.setType(accountType.toString());
        userBill.setTypeInfo(operatorInfo);
        userBill.setBusinessType(businessType.toString());
        userBill.setRequestNo(requestNo);
        userBill.setUserId(userId);
        userBill.setIsVisiable(isVisiable);
        //时间
        userBill.setTime(DateUtil.getCurrentDateTime19());
        long seqNum = 1L;
        UserBill ub = userBillDao.findFirstByUserIdOrderBySeqNumDesc(userId);
        if (ub != null) {
            seqNum += ub.getSeqNum();
        }
        userBill.setSeqNum(seqNum);
        userBillDao.insert(userBill);
    }

    /**
     * 插入资金流水,(subjectId)
     *
     * @param userId            用户id
     * @param money             变动资金
     * @param balance           账户余额
     * @param freezeAmount      冻结金额
     * @param accountType       变动类型
     * @param businessType      业务类型
     * @param requestNo         流水号
     * @param operatorDetail    描述
     * @param operatorInfo      描述(用户看)
     */
    private void insertUserBill(String userId, double money, double balance,
                                double freezeAmount, AccountEnum accountType,
                                BusinessEnum businessType, String requestNo, String operatorDetail,
                                String operatorInfo, int isVisiable,String subjectId) {
        // 插入资金流水
        UserBill userBill = new UserBill();
        userBill.setId(IdUtil.randomUUID());
        userBill.setMoney(money);
        userBill.setBalance(balance);
        userBill.setFreezeAmount(freezeAmount);
        userBill.setDetail(operatorDetail);
        userBill.setType(accountType.toString());
        userBill.setTypeInfo(operatorInfo);
        userBill.setBusinessType(businessType.toString());
        userBill.setRequestNo(requestNo);
        userBill.setUserId(userId);
        userBill.setIsVisiable(isVisiable);
        userBill.setSubjectId(subjectId);
        //时间
        userBill.setTime(DateUtil.getCurrentDateTime19());
        long seqNum = 1L;
        UserBill ub = userBillDao.findFirstByUserIdOrderBySeqNumDesc(userId);
        if (ub != null) {
            seqNum += ub.getSeqNum();
        }
        userBill.setSeqNum(seqNum);
        userBillDao.insert(userBill);
    }
    /**
     * 插入资金流水,(subjectId)
     *
     * @param userId            用户id
     * @param money             变动资金
     * @param balance           账户余额
     * @param freezeAmount      冻结金额
     * @param accountType       变动类型
     * @param businessType      业务类型
     * @param requestNo         流水号
     * @param operatorDetail    描述
     * @param operatorInfo      描述(用户看)
     */
    private void insertUserBill(String userId, double money, double balance,
                                double freezeAmount, AccountEnum accountType,
                                BusinessEnum businessType, String requestNo, String operatorDetail,
                                String operatorInfo, String subjectId,Integer scheduleId) {
        // 插入资金流水
        UserBill userBill = new UserBill();
        userBill.setId(IdUtil.randomUUID());
        userBill.setMoney(money);
        userBill.setBalance(balance);
        userBill.setFreezeAmount(freezeAmount);
        userBill.setDetail(operatorDetail);
        userBill.setType(accountType.toString());
        userBill.setTypeInfo(operatorInfo);
        userBill.setBusinessType(businessType.toString());
        userBill.setRequestNo(requestNo);
        userBill.setUserId(userId);
        userBill.setSubjectId(subjectId);
        userBill.setScheduleId(scheduleId);
        //时间
        userBill.setTime(DateUtil.getCurrentDateTime19());
        long seqNum = 1L;
        UserBill ub = userBillDao.findFirstByUserIdOrderBySeqNumDesc(userId);
        if (ub != null) {
            seqNum += ub.getSeqNum();
        }
        userBill.setSeqNum(seqNum);
        userBillDao.insert(userBill);
    }
    /**
     * 插入资金流水,(subjectId)
     *
     * @param userId            用户id
     * @param money             变动资金
     * @param balance           账户余额
     * @param freezeAmount      冻结金额
     * @param accountType       变动类型
     * @param businessType      业务类型
     * @param requestNo         流水号
     * @param operatorDetail    描述
     * @param operatorInfo      描述(用户看)
     */
    private void insertUserBill(String userId, double money, double balance,
                                double freezeAmount, AccountEnum accountType,
                                BusinessEnum businessType, String requestNo, String operatorDetail,
                                String operatorInfo, int isVisiable,String subjectId,Integer scheduleId) {
        // 插入资金流水
        UserBill userBill = new UserBill();
        userBill.setId(IdUtil.randomUUID());
        userBill.setMoney(money);
        userBill.setBalance(balance);
        userBill.setFreezeAmount(freezeAmount);
        userBill.setDetail(operatorDetail);
        userBill.setType(accountType.toString());
        userBill.setTypeInfo(operatorInfo);
        userBill.setBusinessType(businessType.toString());
        userBill.setRequestNo(requestNo);
        userBill.setUserId(userId);
        userBill.setIsVisiable(isVisiable);
        userBill.setSubjectId(subjectId);
        userBill.setScheduleId(scheduleId);
        //时间
        userBill.setTime(DateUtil.getCurrentDateTime19());
        long seqNum = 1L;
        UserBill ub = userBillDao.findFirstByUserIdOrderBySeqNumDesc(userId);
        if (ub != null) {
            seqNum += ub.getSeqNum();
        }
        userBill.setSeqNum(seqNum);
        userBillDao.insert(userBill);
    }

    /**
     * 插入资金流水,(subjectId)
     *
     * @param userId            用户id
     * @param money             变动资金
     * @param balance           账户余额
     * @param freezeAmount      冻结金额
     * @param accountType       变动类型
     * @param businessType      业务类型
     * @param requestNo         流水号
     * @param operatorDetail    描述
     * @param operatorInfo      描述(用户看)
     */
    private void insertUserBill(String userId, double money, double balance,
                                double freezeAmount, AccountEnum accountType,
                                BusinessEnum businessType, String requestNo, String operatorDetail,
                                String operatorInfo, int isVisiable,String subjectId,Integer scheduleId,
                                Double principal,Double interest,Double commission) {
        // 插入资金流水
        UserBill userBill = new UserBill();
        userBill.setId(IdUtil.randomUUID());
        userBill.setMoney(money);
        userBill.setBalance(balance);
        userBill.setFreezeAmount(freezeAmount);
        userBill.setDetail(operatorDetail);
        userBill.setType(accountType.toString());
        userBill.setTypeInfo(operatorInfo);
        userBill.setBusinessType(businessType.toString());
        userBill.setRequestNo(requestNo);
        userBill.setUserId(userId);
        userBill.setIsVisiable(isVisiable);
        userBill.setSubjectId(subjectId);
        userBill.setScheduleId(scheduleId);
        userBill.setPrincipal(principal);
        userBill.setInterest(interest);
        userBill.setCommission(commission);
        //时间
        userBill.setTime(DateUtil.getCurrentDateTime19());
        long seqNum = 1L;
        UserBill ub = userBillDao.findFirstByUserIdOrderBySeqNumDesc(userId);
        if (ub != null) {
            seqNum += ub.getSeqNum();
        }
        userBill.setSeqNum(seqNum);
        userBillDao.insert(userBill);
    }

    /**
     * 检查用户是否注册及账户状态是否正常
     * @param userId
     * @return
     */
    public void checkUser(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId can not be null");
        }
        //身份验证
        User user = userService.getUserById(userId);
        if (user == null) {
            //用户不存在
            throw new ProcessException(Error.NDR_04191);
        }
        UserAccount userAccount = userAccountDao.findByUserId(userId);
        if (userAccount == null) {
            //未开户
            throw new ProcessException(Error.NDR_04192);
        }
        if (!UserAccount.STATUS_ACTIVE_Y.equals(userAccount.getStatus())) {
            throw new ProcessException(Error.NDR_04193);
        }
    }

    public UserAccount getUserAccountForUpdate(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId can not be null");
        }
        return userAccountDao.getUserAccountForUpdate(userId);
    }

}
