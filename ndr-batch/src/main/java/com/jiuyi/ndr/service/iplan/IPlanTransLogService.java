package com.jiuyi.ndr.service.iplan;

import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.constant.CheckFileConsts;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.*;
import com.jiuyi.ndr.dao.subject.SubjectSendSmsDao;
import com.jiuyi.ndr.dao.subject.SubjectTransferParamDao;
import com.jiuyi.ndr.dao.user.UserDao;
import com.jiuyi.ndr.dao.xm.TransactionDetailDao;
import com.jiuyi.ndr.domain.account.PlatformTransfer;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.*;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectSendSms;
import com.jiuyi.ndr.domain.subject.SubjectTransLog;
import com.jiuyi.ndr.domain.subject.SubjectTransferParam;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.domain.xm.TransactionDetail;
import com.jiuyi.ndr.service.account.PlatformAccountService;
import com.jiuyi.ndr.service.account.PlatformTransferService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.notice.support.TemplateId;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.subject.SubjectTransLogService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestSingleTrans;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.request.RequestUnfreezeProject;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.query.TransactionQueryRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by drw on 2017/6/15.
 */
@Service
public class IPlanTransLogService {

    private static final Logger logger = LoggerFactory.getLogger(IPlanTransLogService.class);

    @Autowired
    IPlanTransLogDao iPlanTransLogDao;

    @Autowired
    CreditOpeningDao creditOpeningDao;

    @Autowired
    IPlanAccountDao iPlanAccountDao;

    @Autowired
    CreditDao creditDao;

    @Autowired
    IPlanDao iPlanDao;

    @Autowired
    IPlanRepayDetailDao iPlanRepayDetailDao;

    @Autowired
    TransactionService transactionService;

    @Autowired
    PlatformTransferService platformTransferService;

    @Autowired
    PlatformAccountService platformAccountService;

    @Autowired
    UserAccountService userAccountService;

    @Autowired
    TransactionDetailDao transactionDetailDao;

    @Autowired
    NoticeService noticeService;

    @Autowired
    UserDao userDao;

    @Autowired
    IPlanRepayScheduleDao iPlanRepayScheduleDao;

    @Autowired
    private SubjectTransferParamDao subjectTransferParamDao;

    @Autowired
    private SubjectTransLogService subjectTransLogService;

    @Autowired
    private IPlanAccountService iPlanAccountService;

    @Autowired
    private UserService userService;

    @Autowired
    private IPlanRepayScheduleService iPlanRepayScheduleService;

    @Autowired
    private SubjectService subjectService;

    public IPlanTransLog save(IPlanTransLog iPlanTransLog){
        iPlanTransLogDao.insert(iPlanTransLog);
        return iPlanTransLog;
    }

    /**
     * 月月盈转出处理
     * @param iPlanTransLogId
     */
    public void exitIPlan(Integer iPlanTransLogId){
        IPlanTransLog iPlanTransLog = iPlanTransLogDao.findByIdForUpdate(iPlanTransLogId);
        IPlanAccount transferIPlanAccount = iPlanAccountDao.findById(iPlanTransLog.getAccountId());//应该不需要加锁，没有其他操作处理此账户

        //List<CreditOpening> creditOpenings = creditOpeningDao.findBySourceChannelIdAndStatusNot(iPlanTransLog.getId(), CreditOpening.SOURCE_CHANNEL_IPLAN,CreditOpening.STATUS_LENDED);
        List<CreditOpening> creditOpenings = creditOpeningDao.findBySourceSccountIdAndSourceChannelAndStatusNot(transferIPlanAccount.getId(), CreditOpening.SOURCE_CHANNEL_IPLAN,CreditOpening.STATUS_LENDED);

        if (creditOpenings.stream().anyMatch(creditOpening -> creditOpening.getAvailablePrincipal()!=0)) {
            logger.info("转出记录 {} 用户Id- {} 还有剩余金额,跳过...", iPlanTransLog.getId(), iPlanTransLog.getUserId());
            return;
        }
        logger.info("月月盈转出交易-id{}，用户-id{}",iPlanTransLog.getId(),iPlanTransLog.getUserId());
        boolean creditOpeningLoan = true;
        for (CreditOpening creditOpening:creditOpenings) {
            List<Credit> credits = creditDao.findByTargetIdAndTarget(creditOpening.getId(), Credit.TARGET_CREDIT);

            if (credits.stream().allMatch(credit -> credit.getCreditStatus()==Credit.CREDIT_STATUS_HOLDING)){
                creditOpening.setStatus(CreditOpening.STATUS_LENDED);
            } else {
                creditOpeningLoan = false;
            }
            creditOpeningDao.update(creditOpening);
        }

        if (!creditOpeningLoan){
            logger.warn("转出记录 {},开发中的债权还没有全部放款",iPlanTransLog.getId());
            return;
        }
        //转出本金
        Integer currentPrincipal = iPlanTransLog.getProcessedAmt();
        logger.info("处理本金-{}",currentPrincipal);
        IPlan iPlan = iPlanDao.findById(transferIPlanAccount.getIplanId());
        Map<String,Integer> bonusAmtMap = getBonusAmt(iPlanTransLog,transferIPlanAccount,iPlan);
        //加息理财计划加息金额
        Integer bonusAmt = bonusAmtMap.get("bonusAmt");
        //转出金额
        Integer tranferAmt = bonusAmtMap.get("tranferAmt");
        //补息金额
        Integer compensateAmt = bonusAmtMap.get("compensateAmt");
        //提前退出费用
        Integer exitCommission = bonusAmtMap.get("exitCommission");
        //佣金
        Integer commission = bonusAmtMap.get("commission");
        //vip特权收益
        Integer vipAmt = bonusAmtMap.get("vipAmt");
        //解冻金额
        Integer unFreezeAmt = currentPrincipal + transferIPlanAccount.getAmtToInvest() + transferIPlanAccount.getPaidInterest();
        logger.info("理财计划转出，转出id：{}，加息理财计划加息金额-{}，转出金额-{}，补息金额-{}，提前退出费用-{}，佣金-{}，解冻金额-{}",iPlanTransLog.getId(),bonusAmt,tranferAmt,compensateAmt,exitCommission,commission,unFreezeAmt);
        boolean doCompensate = true;
        boolean compensateStatus = false;
        boolean doUnFreeze = false;
        boolean unFreezeStatus = false;
        Integer totalCompensateAmt = bonusAmt + compensateAmt + vipAmt;
        String compensateRequestNo = null;
        if (totalCompensateAmt>0){
            if (iPlanTransLog.getExtSn() != null && iPlanTransLog.getExtSn().startsWith("MARKET")) {
                if (BaseResponse.STATUS_PENDING.equals(iPlanTransLog.getExtStatus())) {
                    String requestNo = iPlanTransLog.getExtSn().substring(6);
                    //上次交易状态未知，查询
                    //不能重新发起交易
                    logger.info("上次补息交易状态未知，单笔查询，请求流水号{}", requestNo);
                    RequestSingleTransQuery request = new RequestSingleTransQuery();
                    request.setRequestNo(requestNo);
                    request.setTransactionType(TransactionType.TRANSACTION);
                    ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
                    if (!"0".equals(responseQuery.getCode())) {
                        //查询交易失败
                        logger.info("上次补息交易状态未知，单笔查询，请求流水号{},查询失败，返回码", requestNo, responseQuery.getCode());
                        if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(responseQuery.getCode())) {
                            //订单不存在，重新发起交易
                            iPlanTransLog.setExtStatus(BaseResponse.STATUS_FAILED);
                        }
                    } else {
                        TransactionQueryRecord transactionQueryRecord = (TransactionQueryRecord) responseQuery.getRecords().get(0);
                        if ("SUCCESS".equals(transactionQueryRecord.getStatus())) {
                            //交易成功，本地数据处理
                            logger.info("上次补息交易状态未知，单笔查询，请求流水号{},交易查询成功", requestNo);
                            compensateRequestNo = requestNo;
                            iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                            compensateStatus = true;
                            doCompensate = false;
                            doUnFreeze = true;
                        } else if ("FAIL".equals(transactionQueryRecord.getStatus())) {
                            //查询结果，交易失败，重新发起交易
                            logger.info("上次补息交易状态未知，单笔查询，请求流水号{},交易查询失败，重新发起交易", requestNo);
                            iPlanTransLog.setExtStatus(BaseResponse.STATUS_FAILED);
                            doCompensate = true;
                        } else {
                            doCompensate = false;
                            //查询结果：交易处理中
                            logger.info("上次交易状态未知，单笔查询，请求流水号{},交易查询处理中", requestNo);
                        }
                    }
                }
            } else if (iPlanTransLog.getExtSn() != null && iPlanTransLog.getExtSn().startsWith("UNFREEZE")){
                doCompensate = false;
            }
            if (doCompensate){
                if (totalCompensateAmt-10>transferIPlanAccount.getExpectedInterest()+transferIPlanAccount.getIplanExpectedBonusInterest()+transferIPlanAccount.getIplanExpectedVipInterest()){
                    logger.warn("理财计划补息金额过大，被拦截！转出id-{}",iPlanTransLog.getId());
                    noticeService.sendEmail("理财计划补息拦截","理财计划补息金额过大，补息金额-{"+totalCompensateAmt+"}，被拦截！转出id-{"+iPlanTransLog.getId()+"}","guohuan@duanrong.com");
                    return;
                }
                RequestSingleTrans compensateRequest = constructCompensateRequest(iPlanTransLog, totalCompensateAmt);
                BaseResponse baseResponse = transactionService.singleTrans(compensateRequest);
                iPlanTransLog.setExtSn("MARKET"+baseResponse.getRequestNo());
                if (BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                    iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                    compensateRequestNo = baseResponse.getRequestNo();
                    compensateStatus = true;
                    doUnFreeze = true;
                } else if (BaseResponse.STATUS_FAILED.equals(baseResponse.getStatus())) {
                    //查询结果，交易失败，重新发起交易
                    logger.info("上次补息交易状态未知，单笔查询，请求流水号{},交易查询失败，重新发起交易", baseResponse.getRequestNo());
                    iPlanTransLog.setExtStatus(BaseResponse.STATUS_FAILED);
                } else {
                    iPlanTransLog.setExtStatus(BaseResponse.STATUS_PENDING);
                    //查询结果：交易处理中
                    logger.info("上次交易状态未知，单笔查询，请求流水号{},交易查询处理中", baseResponse.getRequestNo());
                }
            }
        } else {
            compensateStatus = false;
            doUnFreeze = true;
        }
        if (compensateStatus){
            if (compensateAmt>0){
                platformTransferService.out002(iPlanTransLog.getUserId(), compensateAmt / 100.0, String.valueOf(iPlanTransLog.getId()),compensateRequestNo);
                platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_DR, compensateAmt / 100.0, BusinessEnum.ndr_iplan_interest_compensate, "定期理财计划转出补息-userId=" + iPlanTransLog.getUserId(), compensateRequestNo);
                //更新转出人本地账户余额
                if(IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN.equals(iPlanTransLog.getTransType())){
                    userAccountService.transferIn(iPlanTransLog.getUserId(), compensateAmt / 100.0, BusinessEnum.ndr_before_compensate, iPlan.getName()+"清退还款", "定期理财转让到账-补息-userId=" + iPlanTransLog.getUserId(), compensateRequestNo);
                }else{
                    userAccountService.transferIn(iPlanTransLog.getUserId(), compensateAmt / 100.0, BusinessEnum.ndr_iplan_interest_compensate, iPlan.getName()+"还款", "定期理财转让到账-补息-userId=" + iPlanTransLog.getUserId(), compensateRequestNo);
                }
            }
            if (bonusAmt>0){
                platformTransferService.out002(iPlanTransLog.getUserId(), bonusAmt / 100.0, String.valueOf(iPlanTransLog.getId()), compensateRequestNo, PlatformTransfer.TYPE_3);
                platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_DR, bonusAmt / 100.0, BusinessEnum.ndr_iplan_bonus_interest, "定期加息理财计划转出补息-userId=" + iPlanTransLog.getUserId(), compensateRequestNo);
                //更新转出人本地账户余额
                if(IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN.equals(iPlanTransLog.getTransType())){
                    userAccountService.transferIn(iPlanTransLog.getUserId(), bonusAmt / 100.0, BusinessEnum.ndr_before_bonus, iPlan.getName()+"清退加息奖励", "定期加息理财划转让到账-理财计划活动加息-userId=" + iPlanTransLog.getUserId(), compensateRequestNo);
                }else{
                    userAccountService.transferIn(iPlanTransLog.getUserId(), bonusAmt / 100.0, BusinessEnum.ndr_iplan_bonus_interest, iPlan.getName()+"加息奖励", "定期加息理财划转让到账-理财计划活动加息-userId=" + iPlanTransLog.getUserId(), compensateRequestNo);
                }
            }
            if (vipAmt>0){
                platformTransferService.out002(iPlanTransLog.getUserId(), vipAmt / 100.0, String.valueOf(iPlanTransLog.getId()), compensateRequestNo, PlatformTransfer.TYPE_3);
                platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_DR, vipAmt / 100.0, BusinessEnum.ndr_iplan_vip_interest, "定期加息理财计划转出补息-userId=" + iPlanTransLog.getUserId(), compensateRequestNo);
                //更新转出人本地账户余额
                if(IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN.equals(iPlanTransLog.getTransType())){
                    userAccountService.transferIn(iPlanTransLog.getUserId(), vipAmt / 100.0, BusinessEnum.ndr_before_vip, iPlan.getName()+"清退VIP特权加息奖励", "定期加息理财划转让到账-特权加息-userId=" + iPlanTransLog.getUserId(), compensateRequestNo);
                }else{
                    userAccountService.transferIn(iPlanTransLog.getUserId(), vipAmt / 100.0, BusinessEnum.ndr_iplan_vip_interest, iPlan.getName()+"VIP特权加息奖励", "定期加息理财划转让到账-特权加息-userId=" + iPlanTransLog.getUserId(), compensateRequestNo);
                }

            }
            //补息
        }
        Integer totalCommission = exitCommission + commission;
        String unFreezeRequestNo = null;
        if (iPlanTransLog.getExtSn() != null && iPlanTransLog.getExtSn().startsWith("UNFREEZE")) {
            if (BaseResponse.STATUS_PENDING.equals(iPlanTransLog.getExtStatus())) {
                String requestNo = iPlanTransLog.getExtSn().substring(8);
                //上次交易状态未知，查询
                //不能重新发起交易
                logger.info("上次解冻交易状态未知，单笔查询，请求流水号{}", requestNo);
                RequestSingleTransQuery request = new RequestSingleTransQuery();
                request.setRequestNo(requestNo);
                request.setTransactionType(TransactionType.TRANSACTION);
                ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
                if (!"0".equals(responseQuery.getCode())) {
                    //查询交易失败
                    logger.info("上次补息交易状态未知，单笔查询，请求流水号{},查询失败，返回码", requestNo, responseQuery.getCode());
                    if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(responseQuery.getCode())) {
                        //订单不存在，重新发起交易
                        iPlanTransLog.setExtStatus(BaseResponse.STATUS_FAILED);
                        doUnFreeze = true;
                    }
                } else {
                    TransactionQueryRecord transactionQueryRecord = (TransactionQueryRecord) responseQuery.getRecords().get(0);
                    if ("SUCCESS".equals(transactionQueryRecord.getStatus())) {
                        //交易成功，本地数据处理
                        logger.info("上次解冻交易状态未知，单笔查询，请求流水号{},交易查询成功", requestNo);
                        iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                        unFreezeRequestNo = requestNo;
                        unFreezeStatus = true;
                    } else if ("FAIL".equals(transactionQueryRecord.getStatus())) {
                        //查询结果，交易失败，重新发起交易
                        logger.info("上次解冻交易状态未知，单笔查询，请求流水号{},交易查询失败，重新发起交易", requestNo);
                        iPlanTransLog.setExtStatus(BaseResponse.STATUS_FAILED);
                        doUnFreeze = true;
                    } else {
                        //查询结果：交易处理中
                        logger.info("上次交易状态未知，单笔查询，请求流水号{},交易查询处理中", requestNo);
                    }
                }
            }
        }
        //解冻用户资金
        if (doUnFreeze){
            BaseResponse response = unFreezeAmount(transferIPlanAccount,unFreezeAmt,totalCommission);
            iPlanTransLog.setExtSn("UNFREEZE"+response.getRequestNo());
            unFreezeRequestNo = response.getRequestNo();
            if (BaseResponse.STATUS_SUCCEED.equals(response.getStatus())) {
                iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                unFreezeStatus = true;
            } else if (BaseResponse.STATUS_FAILED.equals(response.getStatus())) {
                //查询结果，交易失败，重新发起交易
                iPlanTransLog.setExtStatus(BaseResponse.STATUS_FAILED);
                logger.info("上次补息交易状态未知，单笔查询，请求流水号{},交易查询失败，重新发起交易", response.getRequestNo());
            } else {
                iPlanTransLog.setExtStatus(BaseResponse.STATUS_PENDING);
                //查询结果：交易处理中
                logger.info("上次交易状态未知，单笔查询，请求流水号{},交易查询处理中", response.getRequestNo());
            }
        }
        if (unFreezeStatus){
            transferIPlanAccount.setExpectedInterest(transferIPlanAccount.getExpectedInterest() - (tranferAmt-currentPrincipal));
            transferIPlanAccount.setIplanPaidInterest(transferIPlanAccount.getIplanPaidInterest()+tranferAmt-transferIPlanAccount.getInitPrincipal());
            transferIPlanAccount.setIplanPaidBonusInterest(transferIPlanAccount.getIplanPaidBonusInterest()+bonusAmt);
            transferIPlanAccount.setIplanExpectedBonusInterest(transferIPlanAccount.getIplanExpectedBonusInterest()-bonusAmt);
            transferIPlanAccount.setIplanPaidVipInterest(transferIPlanAccount.getIplanPaidVipInterest()+vipAmt);
            transferIPlanAccount.setIplanExpectedVipInterest(transferIPlanAccount.getIplanExpectedVipInterest()-vipAmt);
            transferIPlanAccount.setAmtToTransfer(transferIPlanAccount.getAmtToTransfer()-compensateAmt-bonusAmt-vipAmt);
            transferIPlanAccount.setFreezeAmtToInvest(0);
            iPlanTransLog.setTransStatus(IPlanTransLog.TRANS_STATUS_SUCCEED);
            transferIPlanAccount.setAmtToTransfer(transferIPlanAccount.getAmtToTransfer()-unFreezeAmt);
            transferIPlanAccount.setCurrentPrincipal(0);
            iPlanTransLog.setProcessedAmt(iPlanTransLog.getProcessedAmt()+transferIPlanAccount.getAmtToInvest());
            transferIPlanAccount.setAmtToInvest(0);
            if (totalCommission>0){
                updateTransactionDetail(unFreezeRequestNo);
            }
            updateIPlanTransLogForPrincipleReinvest(transferIPlanAccount.getId());
            if (IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT.equals(iPlanTransLog.getTransType())){
                transferIPlanAccount.setStatus(IPlanAccount.STATUS_ADVANCED_EXIT);
                userAccountService.unfreezeForShow(transferIPlanAccount.getUserId(), unFreezeAmt / 100.0, BusinessEnum.ndr_iplan_withdraw, iPlan.getName()+"提前退出到账", "还款ID：" + transferIPlanAccount.getId()+" 月月盈ID：" + iPlan.getId()+" 本金：" +transferIPlanAccount.getInitPrincipal()/100.0 +"利息："+(tranferAmt-transferIPlanAccount.getInitPrincipal())/100.0, unFreezeRequestNo);
                if (totalCommission>0){
                    userAccountService.transferOut(transferIPlanAccount.getUserId(), totalCommission / 100.0, BusinessEnum.ndr_commission, iPlan.getName()+"提前退出扣减费用", "月月盈ID："+iPlan.getId()+"费用："+commission/100.0 + transferIPlanAccount.getUserId(),unFreezeRequestNo);
                    platformAccountService.transferIn(GlobalConfig.MARKETING_SYS_DR, totalCommission / 100.0, BusinessEnum.ndr_commission, iPlan.getName()+"退出佣金收取："+commission/100.0,unFreezeRequestNo);

                }
            } else if(IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN.equals(iPlanTransLog.getTransType())){
                IPlanRepayDetail iPlanRepayDetail = iPlanRepayDetailDao.findByUserIdAndIPlanIdAndClean(iPlanTransLog.getUserId(), iPlan.getId());
                if(iPlanRepayDetail != null){
                    iPlanRepayDetail.setRepayPrincipal(transferIPlanAccount.getInitPrincipal());
                    iPlanRepayDetail.setRepayInterest(tranferAmt-transferIPlanAccount.getInitPrincipal());
                    iPlanRepayDetail.setRepayBonusInterest(bonusAmt);
                    iPlanRepayDetail.setRepayDate(DateUtil.getCurrentDate());
                    iPlanRepayDetail.setRepayTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    iPlanRepayDetail.setUpdateTime(DateUtil.getCurrentDateTime19());
                    iPlanRepayDetailDao.update(iPlanRepayDetail);
                }
                transferIPlanAccount.setStatus(IPlanAccount.STATUS_CLEAN);
                userAccountService.unfreezeForShow(transferIPlanAccount.getUserId(), (unFreezeAmt -totalCommission)/ 100.0, BusinessEnum.ndr_iplan_before, iPlan.getName()+"月月盈清退到账", "还款ID：" + transferIPlanAccount.getId()+" 月月盈ID：" + iPlan.getId()+" 本金：" +transferIPlanAccount.getInitPrincipal()/100.0 +"利息："+(tranferAmt-transferIPlanAccount.getInitPrincipal())/100.0, unFreezeRequestNo);
                if (totalCommission>0){
                    userAccountService.tofreeze(transferIPlanAccount.getUserId(), totalCommission / 100.0, BusinessEnum.ndr_commission, iPlan.getName()+"月月盈清退扣减费用", "月月盈ID："+iPlan.getId()+"费用："+commission/100.0 + transferIPlanAccount.getUserId(),unFreezeRequestNo);
                    platformAccountService.transferIn(GlobalConfig.MARKETING_SYS_DR, totalCommission / 100.0, BusinessEnum.ndr_commission, iPlan.getName()+"月月盈清退佣金收取："+commission/100.0,unFreezeRequestNo);

                }
            }else {
                //更改个人还款计划
                updateIPlanRepayDetail(iPlan.getId(),transferIPlanAccount.getUserId());
                transferIPlanAccount.setStatus(IPlanAccount.STATUS_NORMAL_EXIT);
                userAccountService.unfreezeForShow(transferIPlanAccount.getUserId(), (unFreezeAmt -totalCommission) / 100.0, BusinessEnum.ndr_iplan_withdraw, iPlan.getName()+"还款", "还款ID：" + transferIPlanAccount.getId()+" 月月盈ID：" + iPlan.getId()+" 本金：" +transferIPlanAccount.getInitPrincipal()/100.0 +"利息："+(tranferAmt-transferIPlanAccount.getInitPrincipal())/100.0, unFreezeRequestNo);
                if (totalCommission>0){
                    userAccountService.tofreeze(transferIPlanAccount.getUserId(), totalCommission / 100.0, BusinessEnum.ndr_commission, iPlan.getName()+"到期退出佣金", "月月盈ID："+iPlan.getId()+"费用："+commission/100.0 + transferIPlanAccount.getUserId(), unFreezeRequestNo);
                    platformAccountService.transferIn(GlobalConfig.MARKETING_SYS_DR, totalCommission / 100.0, BusinessEnum.ndr_commission, iPlan.getName()+"退出佣金收取："+commission/100.0,unFreezeRequestNo);

                }
            }
            try {
                logger.info("用户{}转出到账短信发送！",transferIPlanAccount.getUserId());
                if (IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT.equals(iPlanTransLog.getTransType())){
                    String params2 = userDao.findByUsername(iPlanTransLog.getUserId()).getRealname() + ","
                            + iPlan.getName() + ","
                            + transferIPlanAccount.getInitPrincipal()/100.0 + ","
                            + (tranferAmt-transferIPlanAccount.getInitPrincipal())/100.0 + ","
                            + exitCommission/100.0 + ","
                            + (tranferAmt-exitCommission)/100.0 + ","
                            + userAccountService.getUserAccount(iPlanTransLog.getUserId()).getAvailableBalance();
                        //理财计划提前转出全部到账发送短信
                    noticeService.send(userDao.findByUsername(iPlanTransLog.getUserId()).getMobileNumber().trim(), params2, TemplateId.IPLAN_ADVANCE_EXIT);
                    if (bonusAmt>0){
                        String params3 = userDao.findByUsername(iPlanTransLog.getUserId()).getRealname() + ","
                                + iPlan.getName() + ","
                                + iPlan.getBonusRate().doubleValue()*100 + "%,"
                                + bonusAmt/100.0 ;
                        noticeService.send(userDao.findByUsername(iPlanTransLog.getUserId()).getMobileNumber().trim(), params3, TemplateId.IPLAN_PAY_INTEREST_WHEN_ADVANCE_EXIT);
                    }
                } else if(IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN.equals(iPlanTransLog.getTransType())){
                    String params2 =iPlan.getName() + ","
                            + transferIPlanAccount.getInitPrincipal()/100.0 + ","
                            + (tranferAmt-transferIPlanAccount.getInitPrincipal())/100.0 + ","
                            + tranferAmt/100.0 + ","
                            + userAccountService.getUserAccount(iPlanTransLog.getUserId()).getAvailableBalance();
                    //理财计划清退全部到账发送短信
                    noticeService.send(userDao.findByUsername(iPlanTransLog.getUserId()).getMobileNumber().trim(), params2, TemplateId.IPLAN_CLEAN);
                    if (bonusAmt>0){
                        String params3 = userDao.findByUsername(iPlanTransLog.getUserId()).getRealname() + ","
                                + iPlan.getName() + ","
                                + iPlan.getBonusRate().doubleValue()*100 + "%,"
                                + bonusAmt/100.0 ;
                        noticeService.send(userDao.findByUsername(iPlanTransLog.getUserId()).getMobileNumber().trim(), params3, TemplateId.IPLAN_PAY_INTEREST_WHEN_CLEAN);
                    }
                } else {
                    String params2 = userDao.findByUsername(iPlanTransLog.getUserId()).getRealname() + ","
                            + iPlan.getName() + ","
                            + transferIPlanAccount.getInitPrincipal()/100.0 + ","
                            + (tranferAmt-transferIPlanAccount.getInitPrincipal())/100.0 + ","
                            + tranferAmt/100.0 + ","
                            + userAccountService.getUserAccount(iPlanTransLog.getUserId()).getAvailableBalance();
                        //理财计划到期转出全部到账发送短信
                    noticeService.send(userDao.findByUsername(iPlanTransLog.getUserId()).getMobileNumber().trim(), params2, TemplateId.IPLAN_EXIT);
                    if (bonusAmt>0){
                        String params3 = userDao.findByUsername(iPlanTransLog.getUserId()).getRealname() + ","
                                + iPlan.getName() + ","
                                + iPlan.getBonusRate().doubleValue()*100 + "%,"
                                + bonusAmt/100.0 ;
                        noticeService.send(userDao.findByUsername(iPlanTransLog.getUserId()).getMobileNumber().trim(), params3, TemplateId.IPLAN_PAY_INTEREST_WHEN_EXIT);
                    }
                }
            } catch (Exception e) {
                logger.warn("发送短信异常",e);
            }
        }
        if (transferIPlanAccount.getExpectedInterest()<0){
            transferIPlanAccount.setExpectedInterest(0);
        }
        if (transferIPlanAccount.getAmtToTransfer()<0){
            transferIPlanAccount.setAmtToTransfer(0);
        }
        iPlanTransLogDao.update(iPlanTransLog);
        iPlanAccountDao.update(transferIPlanAccount);
        //更改理财计划, 理财计划还款计划
        updateIPlanAndIPlanSchedule(iPlan);
    }

    private void updateTransactionDetail(String unFreezeRequestNo){
        List<TransactionDetail> transactionDetails = transactionDetailDao.findByRequestNoAndStatus(unFreezeRequestNo,TransactionDetail.STATUS_PENDING);
        for (TransactionDetail transactionDetail:transactionDetails) {
            transactionDetail.setUpdateTime(DateUtil.getCurrentDateTime14());
            transactionDetail.setStatus(TransactionDetail.STATUS_SUCCEED);
            transactionDetailDao.update(transactionDetail);
        }
    }

    /**
     * 获取转出人应该加息金额
     * @param iPlanTransLog
     * @param iPlanAccount
     * @param iPlan
     * @return
     */
    private Map<String,Integer> getBonusAmt(IPlanTransLog iPlanTransLog ,IPlanAccount iPlanAccount,IPlan iPlan){
        Map<String,Integer> map = new HashMap<>();
        long beforeHoldingDays;
        Integer beforeBonusAmt = 0;
        Integer bonusAmt = 0;
        Integer vipAmt = 0;
        Integer beforeVipAmt = 0;
        Integer currentPrincipal = iPlanAccount.getInitPrincipal();
        Integer transferPrincipal = iPlanTransLog.getProcessedAmt();
        //加息活动利率
        BigDecimal bonusRate = iPlan.getBonusRate();
        BigDecimal vipRate = iPlanAccount.getVipRate();
        if (bonusRate.compareTo(BigDecimal.ZERO)==1||vipRate.compareTo(BigDecimal.ZERO)==1){
            if (IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT.equals(iPlanTransLog.getTransType()) || IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN.equals(iPlanTransLog.getTransType())){
                String raiseCloseDate = iPlan.getRaiseCloseTime().substring(0, 10).replace("-", "");
                List<IPlanRepayDetail> iPlanRepayDetails = iPlanRepayDetailDao.findByUserIdAndIPlanId(iPlanAccount.getUserId(),iPlan.getId());
                Collections.sort(iPlanRepayDetails,Comparator.comparing(IPlanRepayDetail::getTerm).reversed());
                for(IPlanRepayDetail iPlanRepayDetail:iPlanRepayDetails){
                    if(iPlanRepayDetail.getStatus().equals(IPlanRepayDetail.STATUS_REPAY_FINISH)){
                        raiseCloseDate = iPlanRepayDetail.getDueDate().replaceAll("-","");
                        break;
                    }
                }
                beforeHoldingDays = DateUtil.betweenDays(raiseCloseDate, iPlanTransLog.getTransTime().substring(0,10).replaceAll("-",""));//转出当日到锁定期开始相差的天数 （满标的下一天是锁定期）
                //iPlanHoldingDays = DateUtil.betweenDays(raiseCloseDate, dateNow);
                logger.info("raiseCloseDate={},holdingDays={}",raiseCloseDate,beforeHoldingDays);
                if (iPlan.getExitLockDays() != 0 && beforeHoldingDays<= iPlan.getExitLockDays()) {
                    logger.warn("[{}] wanna exit in iPlan [{}] status [{}]", iPlanAccount.getUserId(), iPlan.getId(), iPlan.getStatus());
                }
                logger.info("对userId为{}的用户进行加息理财计划派息,之前持有天数{},加息利率{}",iPlanAccount.getUserId(),beforeHoldingDays,bonusRate);
                //logger.info("对userId为{}的用户进行加息理财计划派息,当前持有天数{},加息利率{}",iPlanAccount.getUserId(),iPlanHoldingDays,bonusRate);
                //3.计算提前退出金额，不包含手续费
                beforeBonusAmt = FinanceCalcUtils.calcInterest(currentPrincipal, bonusRate, (int) beforeHoldingDays);
                beforeVipAmt = FinanceCalcUtils.calcInterest(currentPrincipal, vipRate, (int) beforeHoldingDays);
            } else {
                IPlanRepayDetail iPlanRepayDetail = iPlanRepayDetailDao.findLastTermByUserIdAndIPlanId(iPlan.getId(),iPlanAccount.getUserId());
                beforeBonusAmt = iPlanRepayDetail.getDueBonusInterest();
                beforeVipAmt = iPlanRepayDetail.getDueVipInterest();
            }
            String date = DateUtil.getCurrentDateTime(DateUtil.DATE_10);
            String transTime = DateUtil.parseDateTime(iPlanTransLog.getTransTime(),DateUtil.DATE_TIME_FORMATTER_19).format(DateUtil.DATE_TIME_FORMATTER_10);
            Integer dayDifference = DateUtil.dayDifference(transTime,date )-1;// 补多少天的利息
            Integer allowanceBonusAmt = 0;
            Integer allowanceVipAmt = 0;
            if (dayDifference > 0) {
                allowanceBonusAmt = FinanceCalcUtils.calcInterest(currentPrincipal, bonusRate, dayDifference);
                allowanceVipAmt = FinanceCalcUtils.calcInterest(currentPrincipal, vipRate, dayDifference);
            }
            bonusAmt = beforeBonusAmt + allowanceBonusAmt;
            vipAmt = beforeVipAmt + allowanceVipAmt;
        }
        Integer investAllowanceInterest = getInvestAllowanceInterest(iPlanTransLog,iPlanAccount,iPlan);
        Integer tranferAmt = investAllowanceInterest+iPlanTransLog.getTransAmt()-beforeBonusAmt-beforeVipAmt;
        //佣金
        Integer commission = 0;
        //补息金额
        Integer compensateAmt = 0;
        commission = transferPrincipal+iPlanAccount.getAmtToInvest()+iPlanAccount.getPaidInterest()-tranferAmt;
        if (commission<0){
            compensateAmt += -commission;
        }
        Integer exitCommission = 0;
        if(IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT.equals(iPlanTransLog.getTransType())){
            exitCommission = iPlanAccount.getExitFee();
        }
        map.put("exitCommission",exitCommission);
        map.put("commission",commission>0?commission:0);
        map.put("compensateAmt",compensateAmt);
        map.put("tranferAmt",tranferAmt);
        map.put("bonusAmt",bonusAmt);
        map.put("vipAmt",vipAmt);
        return map;
    }

    /**
     * 计算补息金额
     * @param iPlanTransLog
     * @param iPlanAccount
     * @param iPlan
     * @return
     */
    public Integer getInvestAllowanceInterest(IPlanTransLog iPlanTransLog,IPlanAccount iPlanAccount,IPlan iPlan){
        String date = DateUtil.getCurrentDateTime(DateUtil.DATE_10);
        Integer currentPrincipal = iPlanAccount.getInitPrincipal();
        BigDecimal rate = iPlan.getFixRate();
        String transTime = DateUtil.parseDateTime(iPlanTransLog.getTransTime(),DateUtil.DATE_TIME_FORMATTER_19).format(DateUtil.DATE_TIME_FORMATTER_10);
        Integer dayDifference = DateUtil.dayDifference(transTime,date )-1;// 补多少天的利息
        Integer investAllowanceInterest = 0;
        if (dayDifference > 0) {
            investAllowanceInterest = FinanceCalcUtils.calcInterest(currentPrincipal, rate, dayDifference);
        }
        return investAllowanceInterest;
    }

    private RequestSingleTrans   constructCompensateRequest(IPlanTransLog iPlanTransLog, int compensateAmt) {
        RequestSingleTrans compensateRequest = new RequestSingleTrans();
        compensateRequest.setRequestNo(IdUtil.getRequestNo());
        compensateRequest.setTransCode(TransCode.CREDIT_LEND_COMPENSATE.getCode());
        compensateRequest.setTradeType(TradeType.MARKETING);
        List<RequestSingleTrans.Detail> compensateRequestDetails = new ArrayList<>(1);
        RequestSingleTrans.Detail compensateRequestDetail = new RequestSingleTrans.Detail();
        compensateRequestDetail.setBizType(BizType.MARKETING);
        compensateRequestDetail.setAmount(compensateAmt / 100.0);
        compensateRequestDetail.setSourcePlatformUserNo(GlobalConfig.MARKETING_ACCOUNT_XM);
        compensateRequestDetail.setTargetPlatformUserNo(iPlanTransLog.getUserId());
        compensateRequestDetails.add(compensateRequestDetail);
        compensateRequest.setDetails(compensateRequestDetails);
        return compensateRequest;
    }

    private BaseResponse unFreezeAmount(IPlanAccount iPlanAccount, Integer unFreezeAmount, Integer commission) {
        RequestUnfreezeProject request = constructRequest(iPlanAccount.getInvestRequestNo(),unFreezeAmount,commission);
        BaseResponse response = transactionService.IntetligentProjectUnfreeze(request);
        if (commission>0){
            TransactionDetail transactionDetail = new TransactionDetail();
            transactionDetail.setBizType("COMMISSION");
            transactionDetail.setBusinessType(CheckFileConsts.BIZ_TYPE_11);
            transactionDetail.setAmount(commission/100.0);
            transactionDetail.setSourcePlatformUserNo(iPlanAccount.getUserId());
            transactionDetail.setTargetPlatformUserNo("");
            transactionDetail.setSubjectId("");
            transactionDetail.setRequestNo(request.getRequestNo());
            transactionDetail.setCreditUnit(null);//债权份额（债权转让且需校验债权关系的必传）
            transactionDetail.setStatus(TransactionDetail.STATUS_SUCCEED);
            transactionDetail.setRequestTime(DateUtil.getCurrentDateTime14());
            transactionDetail.setType(TransactionDetail.FILE_COMMISSION);//批量投标请求解冻
            transactionDetailDao.insert(transactionDetail);
        }
        return response;
    }

    private RequestUnfreezeProject constructRequest(String freezeNo,Integer unFreezeAmount,Integer commission){
        RequestUnfreezeProject request = new RequestUnfreezeProject();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setIntelRequestNo(freezeNo);
        request.setAmount(unFreezeAmount/100.0);
        if (commission>0){
            request.setCommission(commission/100.0);
        }
        return request;
    }


    private void updateIPlanAndIPlanSchedule(IPlan iPlan){
        logger.info("更改理财计划还款计划-理财计划id-{}",iPlan.getId());
        List<IPlanAccount> iPlanAccounts = iPlanAccountDao.findByIPlanId(iPlan.getId());
        if(!iPlanAccounts.stream().anyMatch(iPlanAccount ->(iPlanAccount.getInitPrincipal()>0&&IPlanAccount.STATUS_PROCEEDS.equals(iPlanAccount.getStatus()) || iPlanAccount.getInitPrincipal()>0&&IPlanAccount.STATUS_CLEAN_PENDING.equals(iPlanAccount.getStatus())))){
            List<IPlanRepaySchedule> iPlanRepaySchedules = iPlanRepayScheduleDao.findByIPlanId(iPlan.getId());
            for (IPlanRepaySchedule iPlanRepaySchedule:iPlanRepaySchedules) {
                if (IPlanRepaySchedule.STATUS_NOT_REPAY.equals(iPlanRepaySchedule.getStatus())) {
                    logger.info("更改理财计划还款计划-还款计划id-{}",iPlanRepaySchedule.getId());
                    iPlanRepaySchedule.setStatus(IPlanRepaySchedule.STATUS_REPAY_FINISH);
                    iPlanRepaySchedule.setRepayDate(DateUtil.getCurrentDate());
                    iPlanRepaySchedule.setRepayTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    iPlanRepaySchedule.setUpdateTime(DateUtil.getCurrentDateTime19());
                    iPlanRepayScheduleDao.update(iPlanRepaySchedule);
                }
            }
            iPlan.setStatus(IPlan.STATUS_END);
            if(iPlanAccounts.stream().anyMatch(iPlanAccount -> IPlanAccount.STATUS_CLEAN.equals(iPlanAccount.getStatus()))){
                iPlan.setStatus(IPlan.STATUS_CLEAR);
            }
            iPlanDao.update(iPlan);
        }
    }



    private void updateIPlanRepayDetail(Integer iPlanId,String userId){
        IPlanRepayDetail iPlanRepayDetail = iPlanRepayDetailDao.findLastTermByUserIdAndIPlanId(iPlanId,userId);
        iPlanRepayDetail.setStatus(IPlanRepayDetail.STATUS_REPAY_FINISH);
        iPlanRepayDetail.setRepayDate(DateUtil.getCurrentDate());
        iPlanRepayDetail.setRepayTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        iPlanRepayDetail.setUpdateTime(DateUtil.getCurrentDateTime19());
        iPlanRepayDetailDao.update(iPlanRepayDetail);

    }
    //查询一下该账户下是否还有本金复投的转入记录
    private void updateIPlanTransLogForPrincipleReinvest(int accountId){
        try {
            List<IPlanTransLog> iPlanTransLogs = iPlanTransLogDao.findByAccountIdAndTransTypePending(accountId,IPlanTransLog.TRANS_TYPE_PRINCIPLE_REINVEST);
            if(iPlanTransLogs!=null&&iPlanTransLogs.size()>0){
                for(IPlanTransLog iPlanTransLog:iPlanTransLogs){
                    iPlanTransLog.setProcessedAmt(iPlanTransLog.getTransAmt());
                    iPlanTransLog.setTransStatus(IPlanTransLog.TRANS_STATUS_SUCCEED);
                    iPlanTransLog.setUpdateTime(DateUtil.getCurrentDateTime19());
                    iPlanTransLogDao.update(iPlanTransLog);
                }
            }
        } catch (Exception e){
            logger.error("更改用户本金复投记录-月月盈账户id：{},异常信息：{}",accountId,e.getMessage());
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

    @Transactional
    public IPlanTransLog getByIdLocked(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("iPlanTransLog id is can not null");
        }
        return iPlanTransLogDao.findByIdForUpdate(id);
    }

    //一键投债权转让transLog处理
    @Transactional
    public IPlanTransLog exit(Integer iPlanTransLogId) {
        logger.info("一键投债权转让退出处理开始，transLogID：{}",iPlanTransLogId);
        IPlanTransLog iPlanTransLog = iPlanTransLogDao.findByIdForUpdate(iPlanTransLogId);
        List<CreditOpening> creditOpeningsAll = creditOpeningDao.findByTransLogIdByCondition(iPlanTransLogId);
        if(creditOpeningsAll != null && creditOpeningsAll.size() > 0){
            logger.info("转出记录 {} 用户Id- {} 存在未转让成功的债权....", iPlanTransLog.getId(), iPlanTransLog.getUserId());
            return iPlanTransLog;
        }
        //查询折让率
        CreditOpening opening = creditOpeningDao.findByTransLogIdAllNoConditon(iPlanTransLogId);
        BigDecimal transferDiscount = opening.getTransferDiscount();
        //转让记录对应的账户
        IPlanAccount iPlanAccount = iPlanAccountDao.findByIdForUpdate(iPlanTransLog.getAccountId());
        //一键投
        IPlan iPlan = iPlanDao.findById(iPlanAccount.getIplanId());

        List<CreditOpening> creditOpenings = creditOpeningDao.findByTransLogIdAll(iPlanTransLogId,CreditOpening.STATUS_LENDED);

        if(iPlanTransLog.getTransType().equals(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL) && "债权转让全部取消".equals(iPlanTransLog.getTransDesc())){
            if (creditOpenings.stream().allMatch(creditOpening -> creditOpening.getStatus().equals(CreditOpening.STATUS_CANCEL_ALL))){
                if(creditOpenings.stream().anyMatch(creditOpening -> creditOpening.getExtStatus() == 0)){
                    logger.info("转出记录 {} 用户Id- {} 有未撤消的债权,跳过...", iPlanTransLog.getId(), iPlanTransLog.getUserId());
                    return iPlanTransLog;
                }else{
                    iPlanTransLog.setTransStatus(IPlanTransLog.TRANS_STATUS_SUCCEED);
                    iPlanTransLogDao.update(iPlanTransLog);
                    //发送短信
                    User user = userService.getUserById(iPlanTransLog.getUserId());
                    Integer amount = iPlanTransLog.getTransAmt();
                    Double actualPrincipal = ArithUtil.round((amount / 100.0) * (transferDiscount.doubleValue()), 2);
                    try {
                        String smsTemplate = TemplateId.CREDIT_CANCLE_SUCCESS;
                        noticeService.send(user.getMobileNumber(), iPlan.getName() + ","
                                + String.valueOf(amount / 100.0) + "," + String.valueOf(actualPrincipal), smsTemplate);
                    } catch (Exception e) {
                        logger.error("债权撤消短信发送失败", user.getMobileNumber() + "transLog id：" + iPlanTransLogId + "撤消金额：" + String.valueOf(amount / 100.0));
                    }
                    return iPlanTransLog;
                }
            }else{
                logger.info("转出记录 {} 用户Id- {} 有未撤消的债权,跳过...", iPlanTransLog.getId(), iPlanTransLog.getUserId());
                return iPlanTransLog;
            }
        }
        Integer totalBuyAmt = 0;//被购买债权的总金额
        BigDecimal totalAmt = BigDecimal.ZERO;//购买人实际出钱总额
        if(creditOpenings != null && creditOpenings.size() > 0){
            for (CreditOpening creditOpening : creditOpenings) {
                if (creditOpening.getAvailablePrincipal()!=0) {
                    logger.info("转出记录 {} 用户Id- {} 还有剩余金额,跳过...", iPlanTransLog.getId(), iPlanTransLog.getUserId());
                    return iPlanTransLog;
                }
                if(CreditOpening.STATUS_CANCEL_ALL.equals(creditOpening.getStatus())){
                    if(BaseResponse.STATUS_PENDING.equals(creditOpening.getExtStatus())){
                        logger.info("转出记录 {} 用户Id- {} 全部撤消的债权还在处理中...", iPlanTransLog.getId(), iPlanTransLog.getUserId());
                        return iPlanTransLog;
                    }
                }else{
                    List<Credit> credits = creditDao.findByTargetIdAndTarget(creditOpening.getId(), Credit.TARGET_CREDIT);
                    if (credits != null && credits.size() ==0) {
                        logger.info("转出记录 {} 用户Id- {} 被购买的债权还未形成...", iPlanTransLog.getId(), iPlanTransLog.getUserId());
                        return iPlanTransLog;
                    }
                    if (credits.stream().allMatch(credit -> credit.getCreditStatus() == Credit.CREDIT_STATUS_HOLDING)) {
                        for (Credit credit : credits) {
                            totalBuyAmt += credit.getHoldingPrincipal();
                            if(transferDiscount.compareTo(BigDecimal.ONE) != 0){
                                totalAmt = totalAmt.add(new BigDecimal(ArithUtil.round((credit.getHoldingPrincipal() /100.0) * (transferDiscount.doubleValue()) ,2)+""));
                            }
                        }
                    } else {
                        logger.warn("转出记录 {},开放中的债权被购买的还没有全部放款", iPlanTransLog.getId());
                        return iPlanTransLog;
                    }
                }
            }
            if (IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL.equals(iPlanTransLog.getTransType()) && !iPlanTransLog.getProcessedAmt().equals(totalBuyAmt)) {
                logger.info("债权部分撤销转出记录 {} 用户Id- {} 已放款的债权总额不等于已购买的债权总额", iPlanTransLog.getId(), iPlanTransLog.getUserId());
                return iPlanTransLog;
            }else if(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER.equals(iPlanTransLog.getTransType()) && !iPlanTransLog.getTransAmt().equals(totalBuyAmt)){
                logger.info("债权全部被购买转出记录 {} 用户Id- {} 已放款的债权总额不等于已购买的债权总额", iPlanTransLog.getId(), iPlanTransLog.getUserId());
                return iPlanTransLog;
            }
        }
        //散标交易配置信息
        SubjectTransferParam subjectTransferParam = subjectTransferParamDao.findByTransferParamCode(iPlan.getTransferParamCode());

        //计算转让服务费
        Double fee = 0.0;
        Double feeRate = iPlanAccountService.calcTransFee(subjectTransferParam);
        if(iPlanTransLog.getTransFee() == 0){
            fee = (totalBuyAmt /100.0) * (feeRate / 100.0);
        }
        //计算溢价手续费
        Double overPriceFee = 0.0;
        Double transferDis = transferDiscount.multiply(new BigDecimal(100)).doubleValue();
        if (transferDiscount.compareTo(new BigDecimal(1)) == 1){
            overPriceFee = (totalBuyAmt /100.0) * (transferDis - 100) / 100.0 * 0.2;
        }

        Double returnReward = 0.0;//要回收的红包奖励
        if(!isNewFixIplan(subjectTransferParam)){
            Integer currentRepayTerm = iPlanRepayScheduleService.getCurrentRepayTerm(iPlanAccount.getIplanId());
            returnReward = (iPlanAccount.getTotalReward()/100.0) *((totalBuyAmt/1.0)/(iPlanAccount.getInitPrincipal()/1.0))*(currentRepayTerm+1)/iPlan.getTerm();
        }

        //转让人账户上的冻结金额
        Double totalFreeze = 0.0;

        //要扣除的费用总和
        Double totalCommission = 0.0;

        if (StringUtils.isNotBlank(iPlanTransLog.getExtSn()) && BaseResponse.STATUS_PENDING.equals(iPlanTransLog.getExtStatus())) {
            RequestSingleTransQuery request = new RequestSingleTransQuery();
            request.setRequestNo(iPlanTransLog.getExtSn());
            request.setTransactionType(TransactionType.TRANSACTION);
            ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
            if (!"0".equals(responseQuery.getCode())) {
                if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(responseQuery.getCode())) {
                    //订单不存在，设置交易失败，重新发起交易
                    iPlanTransLog.setExtStatus(BaseResponse.STATUS_FAILED);
                    iPlanTransLogDao.update(iPlanTransLog);
                }
                return iPlanTransLog;
            } else {
                TransactionQueryRecord transactionQueryRecord = (TransactionQueryRecord) responseQuery.getRecords().get(0);
                if ("SUCCESS".equals(transactionQueryRecord.getStatus())) {
                    iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                } else if ("PROCESSING".equals(transactionQueryRecord.getStatus())) {
                    iPlanTransLog.setExtStatus(BaseResponse.STATUS_PENDING);
                    iPlanTransLogDao.update(iPlanTransLog);
                    return iPlanTransLog;
                }else {
                    iPlanTransLog.setExtSn(iPlanTransLog.getExtSn());
                    iPlanTransLog.setExtStatus(BaseResponse.STATUS_FAILED);
                    iPlanTransLogDao.update(iPlanTransLog);
                    return iPlanTransLog;
                }
            }
        } else {
            totalFreeze = ArithUtil.round((totalBuyAmt /100.0) * (transferDiscount.doubleValue()) ,2);
            if(transferDiscount.compareTo(BigDecimal.ONE) != 0){
                if(totalFreeze > totalAmt.doubleValue()){
                    totalFreeze = totalAmt.doubleValue();
                }
            }
            totalCommission = ArithUtil.round(returnReward,2) + ArithUtil.round(fee,2) + ArithUtil.round(overPriceFee,2);
            totalCommission = ArithUtil.round(totalCommission,2);
            if("jMVfayj22m22oqah".equals(iPlanTransLog.getUserId())){
                totalCommission = 0.0;
                returnReward = 0.0;
                fee = 0.0;
                overPriceFee = 0.0;
            }
            BaseResponse response = subjectTransLogService.cancelPreTransaction(iPlanTransLog.getUserId(),GlobalConfig.MARKETING_SYS_DR,String.valueOf(iPlan.getId()),iPlanAccount.getInvestRequestNo(),totalFreeze,totalCommission);
            if (BaseResponse.STATUS_FAILED.equals(response.getStatus())) {//交易失败
                iPlanTransLog.setExtSn(response.getRequestNo());
                iPlanTransLog.setExtStatus(response.getStatus());
                iPlanTransLogDao.update(iPlanTransLog);
                return iPlanTransLog;
            }
            if (BaseResponse.STATUS_PENDING.equals(response.getStatus())) {//处理中
                iPlanTransLog.setExtSn(response.getRequestNo());
                iPlanTransLog.setExtStatus(response.getStatus());
                iPlanTransLogDao.update(iPlanTransLog);
                return iPlanTransLog;
            }
            iPlanTransLog.setExtSn(response.getRequestNo());
            iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
        }
            if(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER.equals(iPlanTransLog.getTransType())){
                iPlanTransLog.setProcessedAmt(iPlanTransLog.getProcessedAmt() + totalBuyAmt);
            }
            iPlanTransLog.setActualAmt((int) ArithUtil.round((totalFreeze * 100 - totalCommission * 100),2));

            //更新本地translog和creditOpening
            iPlanTransLog.setTransStatus(SubjectTransLog.TRANS_STATUS_SUCCEED);
            iPlanTransLogDao.update(iPlanTransLog);
            //更新账户信息
            //更新预期收益
            if (iPlanRepayScheduleService.isNewIplan(iPlan)) {
                iPlanAccountService.calcInterest(iPlanAccount,iPlan);
            }else{
                Integer term = iPlanRepayScheduleService.getCurrentRepayTerm(iPlan.getId()) + 1;
                Integer expectInterest =(int)(subjectService.getInterestByRepayType(iPlanAccount.getCurrentPrincipal(),iPlan.getFixRate(),this.getRate(iPlan),term,term * 30,iPlan.getRepayType())*100);
                Integer expectBonusInterest = (int)(subjectService.getInterestByRepayType(iPlanAccount.getCurrentPrincipal(),iPlan.getBonusRate(),this.getRate(iPlan),term,term * 30,iPlan.getRepayType())*100);
                iPlanAccount.setExpectedInterest(expectInterest);
                iPlanAccount.setIplanExpectedBonusInterest(expectBonusInterest);
            }
            iPlanAccount.setAmtToTransfer(iPlanAccount.getAmtToTransfer() - totalBuyAmt);
            iPlanAccount.setPaidReward(iPlanAccount.getPaidReward() - BigDecimal.valueOf(returnReward * 100).intValue());
            if(iPlanAccount.getCurrentPrincipal() == 0 && iPlanAccount.getAmtToTransfer() == 0){
                iPlanAccount.setStatus(IPlanAccount.STATUS_NORMAL_EXIT);
            }
            iPlanAccountDao.update(iPlanAccount);


        userAccountService.unfreezeForShow(iPlanTransLog.getUserId(),totalFreeze,BusinessEnum.ndr_credit_return,iPlan.getName()+"债权转让到账",iPlan.getName()+"债权转让到账"+totalFreeze+"元"+"本金"+totalBuyAmt/100.0+"元",iPlanTransLog.getExtSn());
        //用户流水出账
        //扣除奖励
        if (ArithUtil.round(returnReward,2) > 0){
            userAccountService.transferOut(iPlanTransLog.getUserId(),ArithUtil.round(returnReward,2),BusinessEnum.ndr_credit_reward,iPlan.getName()+"债权转让扣除奖励",iPlan.getName()+"债权转让扣除奖励"+ArithUtil.round(returnReward,2)+"元",iPlanTransLog.getExtSn(),1);
        }
        //扣除手续费
        if(fee > 0.0){
            userAccountService.transferOut(iPlanTransLog.getUserId(),totalCommission - ArithUtil.round(overPriceFee,2) - ArithUtil.round(returnReward,2),BusinessEnum.ndr_credit_fee,iPlan.getName()+"债权转让扣除手续费",iPlan.getName()+"债权转让扣除手续费"+ArithUtil.round(fee,2)+"元",iPlanTransLog.getExtSn(),1);
        }

        if(ArithUtil.round(overPriceFee,2) > 0){
            userAccountService.transferOut(iPlanTransLog.getUserId(),ArithUtil.round(overPriceFee,2),BusinessEnum.ndr_credit_over_price,iPlan.getName()+"债权转让扣除溢价手续费",iPlan.getName()+"债权转让扣除溢价手续费"+ArithUtil.round(overPriceFee.doubleValue(),2)+"元",iPlanTransLog.getExtSn(),1);
        }
        //平台自有账户佣金流水
        if(totalCommission >= 0.01){
            platformAccountService.transferIn(GlobalConfig.MARKETING_SYS_DR, totalCommission, BusinessEnum.ndr_commission, iPlanTransLog.getUserId()+"债权转让,佣金收取："+totalCommission+"元",iPlanTransLog.getExtSn(),String.valueOf(iPlan.getId()),null);
        }
        User user = userService.getUserById(iPlanTransLog.getUserId());
        if(iPlanTransLog.getTransType().equals(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL ) && iPlanTransLog.getActualAmt() != 0){
            for (CreditOpening creditOpening : creditOpenings) {
                if(!CreditOpening.STATUS_CANCEL.equals(creditOpening.getStatus())){
                    creditOpening.setStatus(CreditOpening.STATUS_CANCEL);
                    creditOpeningDao.update(creditOpening);
                }
            }
            //发送短信
            Integer amount = iPlanTransLog.getTransAmt();
            Double transAmt = ArithUtil.round((amount /100.0) * (transferDiscount.doubleValue()) ,2);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = now.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = now.withHour(7).withMinute(0).withSecond(0);
            if(now.isAfter(start) && now.isBefore(end)){
                //插入一条短信
                String msg = iPlan.getName()+","+String.valueOf(iPlanTransLog.getTransAmt()/100.0)+","
                             +String.valueOf(transAmt)+","+String.valueOf(totalBuyAmt/100.0)+","
                             +String.valueOf(totalCommission)+","
                             +String.valueOf(ArithUtil.round(totalFreeze -totalCommission,2))+","
                             +String.valueOf((amount - totalBuyAmt)/100.0);
               subjectService.insertMsg(iPlanTransLog.getUserId(),msg,user.getMobileNumber(),TemplateId.CREDIT_TRANSFER_PART);
            }else{
                try {
                    String smsTemplate = TemplateId.CREDIT_TRANSFER_PART;
                    noticeService.send(user.getMobileNumber(), iPlan.getName()+","
                            + String.valueOf(amount/100.0)+","+String.valueOf(transAmt)+","+String.valueOf(totalBuyAmt/100.0)+","+String.valueOf(totalCommission)+","+String.valueOf(ArithUtil.round(totalFreeze -totalCommission,2))+","+String.valueOf((amount - totalBuyAmt)/100.0), smsTemplate);
                } catch (Exception e) {
                    logger.error("债权部分短信发送失败",user.getMobileNumber()+"债权转让记录id："+iPlanTransLog.getId()+"成交金额："+String.valueOf(totalBuyAmt/100.0));
                }
                return iPlanTransLog;
            }
        }else {
            for (CreditOpening creditOpening : creditOpenings) {
                creditOpening.setStatus(CreditOpening.STATUS_LENDED);
                creditOpeningDao.update(creditOpening);
            }
        }
        try {
            String smsTemplate = TemplateId.CREDIT_TRANSFER_DEAL_OUT;
            noticeService.send(user.getMobileNumber(), iPlan.getName()+","
                    + String.valueOf(totalBuyAmt/100.0)+","+String.valueOf(totalFreeze)+","+String.valueOf(totalCommission)+","+String.valueOf(ArithUtil.round(totalFreeze -totalCommission,2)), smsTemplate);
        } catch (Exception e) {
            logger.error("债权全部成交短信发送失败",user.getMobileNumber()+"债权转让记录id："+iPlanTransLog.getId()+"成交金额："+String.valueOf(totalBuyAmt/100.0));
        }
        return iPlanTransLog;
    }

    /**
     * 判断是否新版省心投或者散标
     * @param subjectTransferParam
     * @return
     */
    public boolean isNewFixIplan(SubjectTransferParam subjectTransferParam) {
        return SubjectTransferParam.NEW_IPLAN.equals(subjectTransferParam.getTansferReward());
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

    public List<IPlanTransLog> getByIPlanIdAndTransStatusAndTransTypeIn(Integer iPlanId, String transTypes, String transStatuses) {
        Set<Integer> transTypesSet = new HashSet(Arrays.asList(transTypes.split(",")));
        Set<Integer> transStatusesSet = new HashSet(Arrays.asList(transStatuses.split(",")));

        if (iPlanId != null && !transTypes.isEmpty() && transStatuses != null) {
            return iPlanTransLogDao.findByIPlanIdAndTransStatusAndTransTypeIn(iPlanId, transTypesSet, transStatusesSet);
        } else {
            throw new IllegalArgumentException("userId and iPlanId and transTypes and transStatus is can not null");
        }
    }

    public List<IPlanTransLog> getByAccountIdAndTransStatusAndTransTypeIn(Integer accountId, String transTypes, String transStatuses) {
        Set<Integer> transTypesSet = new HashSet(Arrays.asList(transTypes.split(",")));
        Set<Integer> transStatusesSet = new HashSet(Arrays.asList(transStatuses.split(",")));

        if (accountId != null && !transTypes.isEmpty() && transStatuses != null) {
            return iPlanTransLogDao.findByAccountIdAndTransStatusAndTransTypeIn(accountId, transTypesSet, transStatusesSet);
        } else {
            throw new IllegalArgumentException("userId and iPlanId and transTypes and transStatus is can not null");
        }
    }

    public List<IPlanTransLog> getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(String userId, Integer iPlanId, Set<Integer> transTypes, Set<Integer> transStatuses){
        if (org.springframework.util.StringUtils.hasText(userId) && iPlanId != null && !transTypes.isEmpty() && transStatuses != null) {
            return iPlanTransLogDao.findByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(userId, iPlanId, transTypes, transStatuses);
        } else {
            throw new IllegalArgumentException("userId and iPlanId and transTypes and transStatus is can not null");
        }
    }

    public IPlanTransLog findById(Integer id){
        return iPlanTransLogDao.findById(id);
    }

    public IPlanTransLog getTransLogByCreditData( Integer sourceAccountId,String userId,Integer channel, String subjectId){
        return this.iPlanTransLogDao.getTransLogByCreditData(sourceAccountId, userId, channel, subjectId);
    }
    /**
     * 获取合同利率
     * @param iPlan
     * @return
     */
    public BigDecimal getRate(IPlan iPlan) {
        return !BigDecimal.ZERO.equals(BigDecimal.valueOf(iPlan.getSubjectRate().intValue()))?iPlan.getSubjectRate():iPlan.getFixRate();
    }
}
