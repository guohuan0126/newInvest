package com.jiuyi.ndr.service.account;

import com.jiuyi.ndr.constant.AccountEnum;
import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.dao.account.UserAccountDao;
import com.jiuyi.ndr.dao.account.UserBillDao;
import com.jiuyi.ndr.datasource.ProductSlave;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @author ke 2017/6/9
 */
@Service
public class UserAccountService {

    private Logger logger = LoggerFactory.getLogger(UserAccountService.class);

    @Autowired
    private UserAccountDao userAccountDao;
    @Autowired
    private UserService userService;
    @Autowired
    private UserBillDao userBillDao;

    /**
     * 判断该用户账户是否激活
     *
     * @param userId 用户id
     */
    public boolean checkIfAccountActive(String userId){
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId can not be null");
        }
        UserAccount userAccount = userAccountDao.getUserAccount(userId);
        if (null == userAccount || !UserAccount.STATUS_ACTIVE_Y.equals(userAccount.getStatus())) {
            return false;
        }
        return true;
    }

    /**
     * 检查是否开户
     */
    @ProductSlave
    public boolean checkIfOpenAccount(String userId){
        boolean accountFlag = false;
        if (StringUtils.isNotBlank(userId)) {
            UserAccount account = this.getUserAccount(userId);
            if (account != null){
                accountFlag = true;
            }
        }
        return accountFlag;
    }

    /**
     * 检查是否激活
     */
    @ProductSlave
    public int checkIfActive(String userId){
        UserAccount userAccount = this.getUserAccount(userId);
        return userAccount == null ? 1 : userAccount.getStatus();
    }

    public int checkBankCardFlag(String userId){
        return userAccountDao.checkBankCardFlag(userId);
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
        UserAccount userAccount = this.getUserAccount(userId);
        if (userAccount == null) {
            //未开户
            throw new ProcessException(Error.NDR_04192);
        }
        if (!UserAccount.STATUS_ACTIVE_Y.equals(userAccount.getStatus())) {
            throw new ProcessException(Error.NDR_04193);
        }
    }

    /**
     * 查询用户账户
     *
     * @param userId 用户id
     * @return
     */
    @Transactional
    public UserAccount getUserAccount(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId can not be null");
        }
        return userAccountDao.getUserAccount(userId);
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
    @Transactional
    public BaseResponse transferIn(String userId, Double money, BusinessEnum businessType,
                                   String operatorInfo, String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getUserAccountForUpdate(userId);
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
                                    String operatorInfo, String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getUserAccountForUpdate(userId);
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
     * 冻结资金
     *
     * @param userId            用户id
     * @param money             冻结金额
     * @param businessType      业务类型
     * @param operatorInfo      流水描述（用户查看）
     * @param operatorDetail    流水详情
     * @param requestNo         流水号
     */
    @Transactional
    public BaseResponse freeze(String userId, double money, BusinessEnum businessType, String operatorInfo,
                               String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getUserAccountForUpdate(userId);
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
        int isVisiable = 0;
        if (BusinessEnum.ndr_iplan_invest == businessType || BusinessEnum.ndr_iplan_recharge_invest == businessType
                || BusinessEnum.ndr_subject_invest == businessType|| BusinessEnum.ndr_subject_credit_invest == businessType|| BusinessEnum.ndr_new_iplan_freeze == businessType) {
            isVisiable = 1;
        }
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.freeze, businessType, requestNo, operatorDetail, operatorInfo, isVisiable);

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
    @Transactional
    public BaseResponse unfreeze(String userId, double money, BusinessEnum businessType, String operatorInfo,
                                 String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getUserAccountForUpdate(userId);

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
        int isVisiable = 0;
        if (BusinessEnum.ndr_iplan_invest_cancel == businessType || BusinessEnum.ndr_iplan_recharge_invest == businessType
                || BusinessEnum.ndr_subject_recharge_invest == businessType  || BusinessEnum.ndr_subject_invest_cancel == businessType) {
            isVisiable = 1;
        }
        this.insertUserBill(userId, money, userAccount.getBalance(), userAccount.getFreezeAmount(),
                AccountEnum.unfreeze, businessType, requestNo, operatorDetail, operatorInfo, isVisiable);

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
    @Transactional
    public BaseResponse tofreeze(String userId, double money, BusinessEnum businessType, String operatorInfo,
                                 String operatorDetail, String requestNo) {

        UserAccount userAccount = userAccountDao.getUserAccountForUpdate(userId);
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
                AccountEnum.to_frozen, businessType, requestNo, operatorDetail, operatorInfo);

        return new BaseResponse("冻结中转出成功", BaseResponse.STATUS_SUCCEED, requestNo);
    }

    /**
     * 插入资金流水(默认展示给用户)
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
        userBill.setIsVisiable(1);
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

    public UserAccount getUserAccountForUpdate(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId can not be null");
        }
        return userAccountDao.getUserAccountForUpdate(userId);
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

    //获取用户充值并投资次数
    public int getUserPaymentCounts(String userId, int transLogId) {
        if (StringUtils.isBlank(userId) || transLogId == 0) {
            throw new IllegalArgumentException("parameter can not be null");
        }
        return userAccountDao.getUserPaymentCounts(userId, transLogId);
    }

    /**
     * 获取用户散标充值并投资次数
     * @param userId
     * @param transLogId
     * @return
     */
    public int getSubjectPaymentCounts(String userId, int transLogId) {
        if (StringUtils.isBlank(userId) || transLogId == 0) {
            throw new IllegalArgumentException("parameter can not be null");
        }
        return userAccountDao.getSubjectPaymentCounts(userId,String.valueOf(transLogId));
    }

    public UserAccount findUserAccount(String userId) {
        return userAccountDao.findByUserId(userId);
    }
}
