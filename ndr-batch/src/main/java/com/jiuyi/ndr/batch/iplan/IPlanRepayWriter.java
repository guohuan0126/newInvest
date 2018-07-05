package com.jiuyi.ndr.batch.iplan;

import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.iplan.*;
import com.jiuyi.ndr.dao.user.UserDao;
import com.jiuyi.ndr.domain.account.PlatformTransfer;
import com.jiuyi.ndr.domain.iplan.*;
import com.jiuyi.ndr.service.account.PlatformAccountService;
import com.jiuyi.ndr.service.account.PlatformTransferService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.notice.support.TemplateId;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestIntelligentProjectUnfreeze;
import com.jiuyi.ndr.xm.http.request.RequestSingleTrans;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.query.CancelPreTransactionQueryRecord;
import com.jiuyi.ndr.xm.http.response.query.TransactionQueryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 理财计划还款
 * Created by zhangyibo on 2017/6/13.
 */
public class IPlanRepayWriter implements ItemWriter<IPlanRepayDetail> {

    private static final Logger logger = LoggerFactory.getLogger(IPlanRepayWriter.class);

    @Autowired
    private PlatformAccountService platformAccountService;

    @Autowired
    private PlatformTransferService platformTransferService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private IPlanRepayDetailDao iPlanRepayDetailDao;

    @Autowired
    private IPlanAccountDao iPlanAccountDao;

    @Autowired
    private IPlanRepayScheduleDao iPlanRepayScheduleDao;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private IPlanDao iPlanDao;

    @Autowired
    private UserDao userDao;

    @Override
    public void write(List<? extends IPlanRepayDetail> IPlanRepayDetails) throws Exception {
        logger.info("开始进行理财计划还款");
        try {
            iplanRepay((List<IPlanRepayDetail>) IPlanRepayDetails);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("理财计划还款异常->{}", e);
            noticeService.sendEmail("理财计划还款异常", "异常信息：" + e, "guohuan@duanrong.com,zhangyibo@duanrong.com,zhangjunying@duanrong.com,lixiaolei@duanrong.com");
        }
        logger.info("理财计划还款结束");
    }


    public void iplanRepay(List<IPlanRepayDetail> IPlanRepayDetails) {

        for (IPlanRepayDetail iPlanRepayDetail : IPlanRepayDetails) {
            if (!IPlanRepayDetail.STATUS_NOT_REPAY.equals(iPlanRepayDetail.getStatus())){
                logger.info("此还款明细已经结束-{},无需派息",iPlanRepayDetail.getId());
                return;
            }
            IPlan iPlan = iPlanDao.findByIdForUpdate(iPlanRepayDetail.getIplanId());
            if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                logger.info("此项目为省心投项目-{},不进行派息",iPlan.getId());
                continue;
            }
            if(iPlanRepayDetail.getDueDate().equals(iPlan.getEndTime().substring(0,10))){
               //防御性判断 以防应该结清的数据到了正常还款
                logger.info("理财计划还款数据有误！理财计划结束时间为:{},当前还款的还款时间为{}",iPlan.getEndTime(),iPlanRepayDetail.getDueDate());
                continue;
            }

            IPlanAccount iPlanAccount = iPlanAccountDao.findByUserIdAndIPlanIdForUpdate(iPlanRepayDetail.getUserId(), iPlanRepayDetail.getIplanId());
            if (!IPlanAccount.STATUS_PROCEEDS.equals(iPlanAccount.getStatus())){
                logger.info("该理财计划账户已不在持有-{}",iPlanAccount.getId());
                continue;
            }
            //在还款之前 校验一下还回去的钱与根据账户上算出来的钱是否相等 不等说明存在问题 发短信告知
            if(!checkRepayAmount(iPlanAccount,iPlan,iPlanRepayDetail)){
                continue;
            }
            if (iPlanRepayDetail.getCurrentStep() == null) {
                //step init
                iPlanRepayDetail.setCurrentStep(IPlanRepayDetail.CURRENT_STEP_BONUS);
            }

                //1.加息标补息发放
                if (IPlanRepayDetail.CURRENT_STEP_BONUS.equals(iPlanRepayDetail.getCurrentStep())) {
                    Integer bonusAndVipInterest = iPlanRepayDetail.getDueBonusInterest()+iPlanRepayDetail.getDueVipInterest();
                    if (bonusAndVipInterest > 0) {
                        if (!sendExtraInterest(iPlanRepayDetail)) {
                            noticeService.sendEmail("理财计划还款进行中","还款计划明细ID为"+iPlanRepayDetail.getId()+"的数据发放加息失败","guohuan@duanrong.com,zhangyibo@duanrong.com,zhangjunying@duanrong.com,lixiaolei@duanrong.com");
                            iPlanRepayDetail.setUpdateTime(DateUtil.getCurrentDateTime19());
                            iPlanRepayDetailDao.update(iPlanRepayDetail);
                            continue;
                        }else{
                            //设置已获加息收益
                            iPlanAccount.setIplanPaidBonusInterest(iPlanAccount.getIplanPaidBonusInterest() + iPlanRepayDetail.getDueBonusInterest());
                            iPlanAccount.setIplanExpectedBonusInterest(iPlanAccount.getIplanExpectedBonusInterest()-iPlanRepayDetail.getDueBonusInterest());
                            iPlanAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
                            iPlanAccountDao.update(iPlanAccount);
                            if(iPlanRepayDetail.getExtSn()!=null){
                                iPlanRepayDetail.setExtSn(null);
                                //需要将extSn置为空 以免下一步骤操作认为是在补偿
                                iPlanRepayDetailDao.update(iPlanRepayDetail);
                            }
                            String params = userDao.findByUsername(iPlanRepayDetail.getUserId()).getRealname()+","
                                    +iPlan.getName()+","+iPlan.getBonusRate().doubleValue()*100 + "%,"+iPlanRepayDetail.getTerm()+","
                                    + iPlan.getTerm()+","+(iPlanRepayDetail.getDueBonusInterest()/100.0);
                            //加息标发放
                            noticeService.send(userDao.findByUsername(iPlanRepayDetail.getUserId()).getMobileNumber().trim(), params, TemplateId.IPLAN_GIVE_AWAY_ACTIVITY_REWARD_MONTHLY);
                        }
                    }
                    iPlanRepayDetail.setCurrentStep(IPlanRepayDetail.CURRENT_STEP_UNFREEZE);
                }


                Integer unFreezeAmount = Math.min(iPlanAccount.getPaidInterest(), iPlanRepayDetail.getDueInterest());
                //2.金额解冻
                if (IPlanRepayDetail.CURRENT_STEP_UNFREEZE.equals(iPlanRepayDetail.getCurrentStep())) {
                    if (unFreezeAmount > 0) {
                        if (!unFreezeAmount(iPlanRepayDetail, iPlanAccount, unFreezeAmount)) {
                            noticeService.sendEmail("理财计划还款进行中","还款计划明细ID为"+iPlanRepayDetail.getId()+"的数据解冻金额失败","guohuan@duanrong.com,zhangyibo@duanrong.com,zhangjunying@duanrong.com,lixiaolei@duanrong.com");
                            iPlanRepayDetail.setUpdateTime(DateUtil.getCurrentDateTime19());
                            iPlanRepayDetailDao.update(iPlanRepayDetail);
                            continue;
                        } else {
                            iPlanAccount.setIplanPaidInterest(iPlanAccount.getIplanPaidInterest() + unFreezeAmount);
                            iPlanAccount.setPaidInterest(iPlanAccount.getPaidInterest()-unFreezeAmount);
                            iPlanAccount.setExpectedInterest(iPlanAccount.getExpectedInterest()-unFreezeAmount);
                            iPlanAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
                            iPlanAccountDao.update(iPlanAccount);
                            iPlanRepayDetail.setRepayInterest(iPlanRepayDetail.getRepayInterest()+unFreezeAmount);
                            iPlanRepayDetailDao.update(iPlanRepayDetail);//在解冻金额成功后 需要更新已还利息 以免在下次计算补息金额时出错
                            if(iPlanRepayDetail.getExtSn()!=null){
                                iPlanRepayDetail.setExtSn(null);
                                iPlanRepayDetailDao.update(iPlanRepayDetail);//需要将extSn置为空 以免下一步骤操作认为是在补偿
                            }
                        }
                    }
                    iPlanRepayDetail.setCurrentStep(IPlanRepayDetail.CURRENT_STEP_COMPENSATE);
                }

                Integer repayPrincipal = iPlanRepayDetail.getDuePrincipal();
                Integer interest = iPlanRepayDetail.getDueInterest();

                //3.补息
                Integer compensateAmt = iPlanRepayDetail.getDueInterest()-iPlanRepayDetail.getRepayInterest();
                if(compensateAmt>iPlanRepayDetail.getDueInterest()){
                    logger.error("补息金额过大，请检查数据,compensateAmt={},iplanRepayDetail.dueInterest={}",compensateAmt,iPlanRepayDetail.getDueInterest());
                    continue;
                }
                if (IPlanRepayDetail.CURRENT_STEP_COMPENSATE.equals(iPlanRepayDetail.getCurrentStep())) {
                    if (compensateAmt > 0) {
                        if (!compensateAmount(iPlanRepayDetail, iPlanAccount, compensateAmt)) {
                            noticeService.sendEmail("理财计划还款进行中","还款计划明细ID为"+iPlanRepayDetail.getId()+"的数据补息失败","guohuan@duanrong.com,zhangyibo@duanrong.com,zhangjunying@duanrong.com,lixiaolei@duanrong.com");
                            iPlanRepayDetail.setUpdateTime(DateUtil.getCurrentDateTime19());
                            iPlanRepayDetailDao.update(iPlanRepayDetail);
                            continue;
                        }else{
                            iPlanAccount.setIplanPaidInterest(iPlanAccount.getIplanPaidInterest() + compensateAmt);
                            iPlanAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
                            iPlanAccount.setExpectedInterest(iPlanAccount.getExpectedInterest()-compensateAmt);
                            iPlanAccountDao.update(iPlanAccount);
                        }
                    }
                    String params = userDao.findByUsername(iPlanRepayDetail.getUserId()).getRealname()+","
                            +iPlan.getName()+","+iPlanRepayDetail.getTerm()+","+iPlan.getTerm()+","
                            +(iPlanRepayDetail.getDueInterest()/100.0)+","+userAccountService.getUserAccount(iPlanRepayDetail.getUserId()).getAvailableBalance();
                    noticeService.send(userDao.findByUsername(iPlanRepayDetail.getUserId()).getMobileNumber().trim(), params, TemplateId.IPLAN_PAY_INTEREST_MONTHLY); //每月还息
                    iPlanRepayDetail.setCurrentStep(IPlanRepayDetail.CURRENT_STEP_REPAY);
                }


                if (IPlanRepayDetail.CURRENT_STEP_REPAY.equals(iPlanRepayDetail.getCurrentStep())) {
                    //新增一条按期回款记录
                    saveIPlanTransLog(iPlanAccount, IPlanTransLog.TRANS_TYPE_IPLAN_INCOME, interest + repayPrincipal, "理财计划按期回款", IPlanTransLog.TRANS_STATUS_SUCCEED, interest + repayPrincipal);
                    iPlanRepayDetail.setCurrentStep(IPlanRepayDetail.CURRENT_STEP_REPAY_FINISH);
                    iPlanRepayDetail.setStatus(IPlanRepayDetail.STATUS_REPAY_FINISH);
                    iPlanRepayDetail.setUpdateTime(DateUtil.getCurrentDateTime19());
                    iPlanRepayDetail.setRepayPrincipal(iPlanRepayDetail.getDuePrincipal());
                    iPlanRepayDetail.setRepayInterest(iPlanRepayDetail.getDueInterest());
                    iPlanRepayDetail.setRepayBonusInterest(iPlanRepayDetail.getDueBonusInterest());
                    iPlanRepayDetail.setRepayDate(DateUtil.getCurrentDateShort());
                    iPlanRepayDetail.setRepayTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    iPlanRepayDetailDao.update(iPlanRepayDetail);
                }


            //检查一下对应的还款计划下的所有detail是否都已还款完成
            List<IPlanRepayDetail> iPlanRepayDetails = iPlanRepayDetailDao.findByRepayScheduleIdNotRepay(iPlanRepayDetail.getRepayScheduleId());
            if (iPlanRepayDetails.isEmpty()) {
                logger.info("理财计划还款计划={}下所有的detail已还款完成,更新理财计划还款计划状态为已完成",iPlanRepayDetail.getRepayScheduleId());
                IPlanRepaySchedule iPlanRepaySchedule = iPlanRepayScheduleDao.findById(iPlanRepayDetail.getRepayScheduleId());
                iPlanRepaySchedule.setStatus(IPlanRepaySchedule.STATUS_REPAY_FINISH);
                iPlanRepaySchedule.setUpdateTime(DateUtil.getCurrentDateTime19());
                iPlanRepaySchedule.setRepayDate(DateUtil.getCurrentDateShort());
                iPlanRepaySchedule.setRepayTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                iPlanRepayScheduleDao.update(iPlanRepaySchedule);
            }
        }
    }

    private boolean compensateAmount(IPlanRepayDetail iPlanRepayDetail, IPlanAccount iPlanAccount, Integer compensateAmt) {
        logger.info("理财计划还款补息金额compensateAmt={}",compensateAmt);
        if(iPlanRepayDetail.getExtSn()==null){
            Integer iplanId = iPlanRepayDetail.getIplanId();
            IPlan iPlan = iPlanDao.findById(iplanId);
            String iplanName = iPlan.getName();
            RequestSingleTrans request = constructCompensateAmt(compensateAmt, iPlanAccount.getUserId(), iPlanRepayDetail.getExtSn());
            BaseResponse response = transactionService.singleTrans(request);
            if (BaseResponse.STATUS_SUCCEED.equals(response.getStatus())) {
                platformTransferService.out002(iPlanAccount.getUserId(), compensateAmt / 100.0, String.valueOf(iPlanAccount.getId()), response.getRequestNo());
                platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_DR, compensateAmt / 100.0, BusinessEnum.ndr_iplan_interest_compensate, iplanName+"还款补息:" + compensateAmt/100.0, request.getRequestNo());
                userAccountService.transferIn(iPlanAccount.getUserId(), compensateAmt / 100.0, BusinessEnum.ndr_iplan_interest_compensate, iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"补息："+compensateAmt/100.0, String.valueOf(iplanId),1);
                iPlanRepayDetail.setExtStatus(BaseResponse.STATUS_SUCCEED);
                iPlanRepayDetailDao.update(iPlanRepayDetail);
                return true;
            } else {
                iPlanRepayDetail.setExtSn(request.getRequestNo());
                iPlanRepayDetail.setExtStatus(response.getStatus());
                iPlanRepayDetailDao.update(iPlanRepayDetail);
                return false;
            }
        }else{
            Integer iplanId = iPlanRepayDetail.getIplanId();
            IPlan iPlan = iPlanDao.findById(iplanId);
            String iplanName = iPlan.getName();
            //否则就先去查询
            RequestSingleTransQuery requestSingleTransQuery = new RequestSingleTransQuery();
            requestSingleTransQuery.setRequestNo(iPlanRepayDetail.getExtSn());
            requestSingleTransQuery.setTransactionType(TransactionType.TRANSACTION);
            ResponseSingleTransQuery response = transactionService.singleTransQuery(requestSingleTransQuery);
            if(BaseResponse.STATUS_SUCCEED.equals(response.getStatus())){
                TransactionQueryRecord record = (TransactionQueryRecord) response.getRecords().get(0);
                if("SUCCESS".equals(record.getStatus())){
                    platformTransferService.out002(iPlanAccount.getUserId(), compensateAmt / 100.0, String.valueOf(iPlanAccount.getId()), response.getRequestNo());
                    platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_DR, compensateAmt / 100.0, BusinessEnum.ndr_iplan_interest_compensate, iplanName+"还款补息:" + compensateAmt/100.0, String.valueOf(iplanId));
                    userAccountService.transferIn(iPlanAccount.getUserId(), compensateAmt / 100.0, BusinessEnum.ndr_iplan_interest_compensate, iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"补息："+compensateAmt/100.0, String.valueOf(iplanId),1);
                    iPlanRepayDetail.setExtStatus(BaseResponse.STATUS_SUCCEED);
                    iPlanRepayDetailDao.update(iPlanRepayDetail);
                    return true;
                }else if ("FAIL".equals(record.getStatus())){
                    //重新发补息
                    RequestSingleTrans request = constructCompensateAmt(compensateAmt, iPlanAccount.getUserId(), null);
                    BaseResponse retryResponse = transactionService.singleTrans(request);
                    if (BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())) {
                        platformTransferService.out002(iPlanAccount.getUserId(), compensateAmt / 100.0, String.valueOf(iPlanAccount.getId()), response.getRequestNo());
                        platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_DR, compensateAmt / 100.0, BusinessEnum.ndr_iplan_interest_compensate, iplanName+"还款补息:" + compensateAmt/100.0, String.valueOf(iplanId));
                        userAccountService.transferIn(iPlanAccount.getUserId(), compensateAmt / 100.0, BusinessEnum.ndr_iplan_interest_compensate, iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"补息："+compensateAmt/100.0, String.valueOf(iplanId));
                        iPlanRepayDetail.setExtStatus(BaseResponse.STATUS_SUCCEED);
                        iPlanRepayDetailDao.update(iPlanRepayDetail);
                        return true;
                    } else {
                        iPlanRepayDetail.setExtSn(request.getRequestNo());
                        iPlanRepayDetail.setExtStatus(retryResponse.getStatus());
                        iPlanRepayDetailDao.update(iPlanRepayDetail);
                        return false;
                    }
                }else{
                    iPlanRepayDetail.setExtStatus(response.getStatus());
                    iPlanRepayDetailDao.update(iPlanRepayDetail);
                    return false;
                }
            }else if(BaseResponse.STATUS_FAILED.equals(response.getStatus())){
                if(GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(response.getCode())){
                    RequestSingleTrans request = constructCompensateAmt(compensateAmt, iPlanAccount.getUserId(), iPlanRepayDetail.getExtSn());
                    BaseResponse retryResponse = transactionService.singleTrans(request);
                    if (BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())) {
                        platformTransferService.out002(iPlanAccount.getUserId(), compensateAmt / 100.0, String.valueOf(iPlanAccount.getId()), response.getRequestNo());
                        platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_DR, compensateAmt / 100.0, BusinessEnum.ndr_iplan_interest_compensate, iplanName+"还款补息:" + compensateAmt/100.0, String.valueOf(iplanId));
                        userAccountService.transferIn(iPlanAccount.getUserId(), compensateAmt / 100.0, BusinessEnum.ndr_iplan_interest_compensate, iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"补息："+compensateAmt/100.0, String.valueOf(iplanId));
                        iPlanRepayDetail.setExtStatus(BaseResponse.STATUS_SUCCEED);
                        iPlanRepayDetailDao.update(iPlanRepayDetail);
                        return true;
                    } else {
                        iPlanRepayDetail.setExtSn(request.getRequestNo());
                        iPlanRepayDetail.setExtStatus(retryResponse.getStatus());
                        iPlanRepayDetailDao.update(iPlanRepayDetail);
                        return false;
                    }
                }
            }
            return false;
        }
    }

    private boolean unFreezeAmount(IPlanRepayDetail iPlanRepayDetail, IPlanAccount iPlanAccount, Integer unFreezeAmount) {
        if(iPlanRepayDetail.getExtSn()==null){
            Integer iplanId = iPlanRepayDetail.getIplanId();
            IPlan iPlan = iPlanDao.findById(iplanId);
            String iplanName = iPlan.getName();
            RequestIntelligentProjectUnfreeze request = constructRequest(iPlanAccount.getInvestRequestNo(), unFreezeAmount, iPlanRepayDetail.getExtSn());
            BaseResponse response = transactionService.intelligentProjectUnfreeze(request);
            if (BaseResponse.STATUS_SUCCEED.equals(response.getStatus())) {
                userAccountService.unfreezeForShow(iPlanAccount.getUserId(), unFreezeAmount / 100.0, BusinessEnum.ndr_iplan_repay, iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"利息："+unFreezeAmount/100.0, String.valueOf(iplanId));
                iPlanRepayDetail.setExtStatus(BaseResponse.STATUS_SUCCEED);
                iPlanRepayDetailDao.update(iPlanRepayDetail);
                return true;
            } else {
                iPlanRepayDetail.setExtSn(request.getRequestNo());
                iPlanRepayDetail.setExtStatus(response.getStatus());
                iPlanRepayDetailDao.update(iPlanRepayDetail);
                return false;
            }
        }else{
            //否则就先去查询
            RequestSingleTransQuery requestSingleTransQuery = new RequestSingleTransQuery();
            requestSingleTransQuery.setRequestNo(iPlanRepayDetail.getExtSn());
            requestSingleTransQuery.setTransactionType(TransactionType.CANCEL_PRETRANSACTION);
            ResponseSingleTransQuery response = transactionService.singleTransQuery(requestSingleTransQuery);
            if(BaseResponse.STATUS_SUCCEED.equals(response.getStatus())){
                CancelPreTransactionQueryRecord record = (CancelPreTransactionQueryRecord) response.getRecords().get(0);
                Integer iplanId = iPlanRepayDetail.getIplanId();
                IPlan iPlan = iPlanDao.findById(iplanId);
                String iplanName = iPlan.getName();
                if("SUCCESS".equals(record.getStatus())){
                    userAccountService.unfreezeForShow(iPlanAccount.getUserId(), unFreezeAmount / 100.0, BusinessEnum.ndr_iplan_repay, iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"利息："+unFreezeAmount/100.0, String.valueOf(iplanId));                    iPlanRepayDetail.setExtStatus(BaseResponse.STATUS_SUCCEED);
                    iPlanRepayDetailDao.update(iPlanRepayDetail);
                    return true;
                }else if("FAIL".equals(record.getStatus())){
                    //重试
                    RequestIntelligentProjectUnfreeze request = constructRequest(iPlanAccount.getInvestRequestNo(), unFreezeAmount, null);
                    BaseResponse retryResponse = transactionService.intelligentProjectUnfreeze(request);
                    if (BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())) {
                        userAccountService.unfreezeForShow(iPlanAccount.getUserId(), unFreezeAmount / 100.0, BusinessEnum.ndr_iplan_repay, iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"利息："+unFreezeAmount/100.0, String.valueOf(iplanId));                        iPlanRepayDetail.setExtStatus(BaseResponse.STATUS_SUCCEED);
                        iPlanRepayDetailDao.update(iPlanRepayDetail);
                        return true;
                    } else {
                        iPlanRepayDetail.setExtSn(request.getRequestNo());
                        iPlanRepayDetail.setExtStatus(retryResponse.getStatus());
                        iPlanRepayDetailDao.update(iPlanRepayDetail);
                        return false;
                    }
                }else{
                    iPlanRepayDetail.setExtSn(response.getRequestNo());
                    iPlanRepayDetail.setExtStatus(response.getStatus());
                    iPlanRepayDetailDao.update(iPlanRepayDetail);
                    return false;
                }
            }else if (BaseResponse.STATUS_FAILED.equals(response.getStatus())){
                if(GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(response.getCode())){
                    //重试
                    Integer iplanId = iPlanRepayDetail.getIplanId();
                    IPlan iPlan = iPlanDao.findById(iplanId);
                    String iplanName = iPlan.getName();
                    RequestIntelligentProjectUnfreeze request = constructRequest(iPlanAccount.getInvestRequestNo(), unFreezeAmount, iPlanRepayDetail.getExtSn());
                    BaseResponse retryResponse = transactionService.intelligentProjectUnfreeze(request);
                    if (BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())) {
                        userAccountService.unfreezeForShow(iPlanAccount.getUserId(), unFreezeAmount / 100.0, BusinessEnum.ndr_iplan_repay, iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"利息："+unFreezeAmount/100.0, String.valueOf(iplanId));                        iPlanRepayDetail.setExtStatus(BaseResponse.STATUS_SUCCEED);
                        iPlanRepayDetailDao.update(iPlanRepayDetail);
                        return true;
                    } else {
                        iPlanRepayDetail.setExtSn(request.getRequestNo());
                        iPlanRepayDetail.setExtStatus(retryResponse.getStatus());
                        iPlanRepayDetailDao.update(iPlanRepayDetail);
                        return false;
                    }
                }
            }
            return false;
        }
    }

    /**
     * 加息奖励发放
     *
     * @param iPlanRepayDetail
     * @return
     */
    private boolean sendExtraInterest(IPlanRepayDetail iPlanRepayDetail) {

        Integer dueBonusInterest = iPlanRepayDetail.getDueBonusInterest();
        Integer dueVipinterest = iPlanRepayDetail.getDueVipInterest();
        Integer extraInterest = dueBonusInterest + dueVipinterest;
        String userId = iPlanRepayDetail.getUserId();
        Integer iplanId = iPlanRepayDetail.getIplanId();
        IPlan iPlan = iPlanDao.findById(iplanId);
        String iplanName = iPlan.getName();
        if(iPlanRepayDetail.getExtSn()==null){
            //如果之前流水号是空 那么就进行正常交易
            RequestSingleTrans request = constructCompensateAmt(extraInterest, userId, iPlanRepayDetail.getExtSn());
            BaseResponse response = transactionService.singleTrans(request);
            if (BaseResponse.STATUS_SUCCEED.equals(response.getStatus())) {
                platformTransferService.out002(userId, extraInterest / 100.0, userId, response.getRequestNo(), PlatformTransfer.TYPE_3);
                platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_DR, extraInterest / 100.0, BusinessEnum.ndr_iplan_bonus_interest, iplanName+"加息奖励：" + extraInterest/100.0, response.getRequestNo());
                if (dueBonusInterest>0){
                    userAccountService.transferIn(userId, dueBonusInterest / 100.0, BusinessEnum.ndr_iplan_bonus_interest,  iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"加息奖励利息："+ dueBonusInterest / 100.0, response.getRequestNo(),1);
                }
                if (dueVipinterest>0){
                    userAccountService.transferIn(userId, dueVipinterest / 100.0, BusinessEnum.ndr_iplan_vip_interest,  iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"加息奖励利息："+ dueVipinterest / 100.0, response.getRequestNo(),1);
                }
                iPlanRepayDetail.setExtStatus(BaseResponse.STATUS_SUCCEED);
                iPlanRepayDetailDao.update(iPlanRepayDetail);
                return true;
            } else {
                iPlanRepayDetail.setExtStatus(response.getStatus());
                iPlanRepayDetail.setExtSn(request.getRequestNo());
                iPlanRepayDetailDao.update(iPlanRepayDetail);
                return false;
            }
        }else{
            //否则就先去查询
            RequestSingleTransQuery requestSingleTransQuery = new RequestSingleTransQuery();
            requestSingleTransQuery.setRequestNo(iPlanRepayDetail.getExtSn());
            requestSingleTransQuery.setTransactionType(TransactionType.TRANSACTION);
            ResponseSingleTransQuery response = transactionService.singleTransQuery(requestSingleTransQuery);
            if(BaseResponse.STATUS_SUCCEED.equals(response.getStatus())){
                TransactionQueryRecord record = (TransactionQueryRecord) response.getRecords().get(0);
                if("SUCCESS".equals(record.getStatus())){
                    platformTransferService.out002(userId, extraInterest / 100.0, userId, response.getRequestNo(), PlatformTransfer.TYPE_3);
                    platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_DR, extraInterest / 100.0, BusinessEnum.ndr_iplan_bonus_interest, iplanName+"加息奖励：" + extraInterest/100.0, response.getRequestNo());
                    if (dueBonusInterest>0){
                        userAccountService.transferIn(userId, dueBonusInterest / 100.0, BusinessEnum.ndr_iplan_bonus_interest,  iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"加息奖励利息："+ dueBonusInterest / 100.0, response.getRequestNo(),1);
                    }
                    if (dueVipinterest>0){
                        userAccountService.transferIn(userId, dueVipinterest / 100.0, BusinessEnum.ndr_iplan_vip_interest,  iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"加息奖励利息："+ dueVipinterest / 100.0, response.getRequestNo(),1);
                    }
                    iPlanRepayDetail.setExtStatus(BaseResponse.STATUS_SUCCEED);
                    iPlanRepayDetailDao.update(iPlanRepayDetail);
                    return true;
                }else if ("FAIL".equals(record.getStatus())){
                    //重新发送
                    RequestSingleTrans request = constructCompensateAmt(extraInterest, userId, null);
                    BaseResponse retryResponse = transactionService.singleTrans(request);
                    if (BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())) {
                        platformTransferService.out002(userId, extraInterest / 100.0, userId, response.getRequestNo(), PlatformTransfer.TYPE_3);
                        platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_DR, extraInterest / 100.0, BusinessEnum.ndr_iplan_bonus_interest, iplanName+"加息奖励：" + extraInterest/100.0, response.getRequestNo());
                        if (dueBonusInterest>0){
                            userAccountService.transferIn(userId, dueBonusInterest / 100.0, BusinessEnum.ndr_iplan_bonus_interest,  iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"加息奖励利息："+ dueBonusInterest / 100.0, response.getRequestNo(),1);
                        }
                        if (dueVipinterest>0){
                            userAccountService.transferIn(userId, dueVipinterest / 100.0, BusinessEnum.ndr_iplan_vip_interest,  iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"加息奖励利息："+ dueVipinterest / 100.0, response.getRequestNo(),1);
                        }
                        iPlanRepayDetail.setExtStatus(BaseResponse.STATUS_SUCCEED);
                        iPlanRepayDetailDao.update(iPlanRepayDetail);
                        return true;
                    } else {
                        iPlanRepayDetail.setExtStatus(retryResponse.getStatus());
                        iPlanRepayDetail.setExtSn(request.getRequestNo());
                        iPlanRepayDetailDao.update(iPlanRepayDetail);
                        return false;
                    }
                }else{
                    iPlanRepayDetail.setExtStatus(response.getStatus());
                    iPlanRepayDetail.setExtSn(response.getRequestNo());
                    iPlanRepayDetailDao.update(iPlanRepayDetail);
                    return false;
                }
            }else if(BaseResponse.STATUS_FAILED.equals(response.getStatus())){
                if(GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(response.getCode())){
                    RequestSingleTrans request = constructCompensateAmt(extraInterest, userId, iPlanRepayDetail.getExtSn());
                    BaseResponse retryResponse = transactionService.singleTrans(request);
                    if (BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())) {
                        platformTransferService.out002(userId, extraInterest / 100.0, userId, response.getRequestNo(), PlatformTransfer.TYPE_3);
                        platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_DR, extraInterest / 100.0, BusinessEnum.ndr_iplan_bonus_interest, iplanName+"加息奖励：" + extraInterest/100.0, response.getRequestNo());
                        if (dueBonusInterest>0){
                            userAccountService.transferIn(userId, dueBonusInterest / 100.0, BusinessEnum.ndr_iplan_bonus_interest,  iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"加息奖励利息："+ dueBonusInterest / 100.0, response.getRequestNo(),1);
                        }
                        if (dueVipinterest>0){
                            userAccountService.transferIn(userId, dueVipinterest / 100.0, BusinessEnum.ndr_iplan_vip_interest,  iplanName+"还款", "还款ID：" + iPlanRepayDetail.getId()+" 月月盈ID：" +iplanId +" 本金：" +iPlanRepayDetail.getDuePrincipal()/100.0 +"加息奖励利息："+ dueVipinterest / 100.0, response.getRequestNo(),1);
                        }
                        iPlanRepayDetail.setExtStatus(BaseResponse.STATUS_SUCCEED);
                        iPlanRepayDetailDao.update(iPlanRepayDetail);
                        return true;
                    } else {
                        iPlanRepayDetail.setExtStatus(retryResponse.getStatus());
                        iPlanRepayDetail.setExtSn(request.getRequestNo());
                        iPlanRepayDetailDao.update(iPlanRepayDetail);
                        return false;
                    }
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }
    }


    private RequestIntelligentProjectUnfreeze constructRequest(String freezeNo, Integer unFreezeAmount, String requestNo) {
        RequestIntelligentProjectUnfreeze request = new RequestIntelligentProjectUnfreeze();
        if (requestNo == null) {
            request.setRequestNo(IdUtil.getRequestNo());
        } else {
            request.setRequestNo(requestNo);
        }
        request.setIntelRequestNo(freezeNo);
        request.setAmount(unFreezeAmount / 100.0);
        request.setCommission(null);
        return request;
    }

    private RequestSingleTrans constructCompensateAmt(Integer compensateAmt, String userId, String requestNo) {
        RequestSingleTrans compensateRequest = new RequestSingleTrans();
        if (requestNo == null) {
            compensateRequest.setRequestNo(IdUtil.getRequestNo());
        } else {
            compensateRequest.setRequestNo(requestNo);
        }

        compensateRequest.setTransCode(TransCode.IPLAN_REPAY_COMPENSATE.getCode());
        compensateRequest.setTradeType(TradeType.MARKETING);
        List<RequestSingleTrans.Detail> compensateRequestDetails = new ArrayList<>(1);
        RequestSingleTrans.Detail compensateRequestDetail = new RequestSingleTrans.Detail();
        compensateRequestDetail.setBizType(BizType.MARKETING);
        compensateRequestDetail.setAmount(compensateAmt / 100.0);
        compensateRequestDetail.setSourcePlatformUserNo(GlobalConfig.MARKETING_ACCOUNT_XM);
        compensateRequestDetail.setTargetPlatformUserNo(userId);
        compensateRequestDetails.add(compensateRequestDetail);
        compensateRequest.setDetails(compensateRequestDetails);
        return compensateRequest;
    }

    private IPlanTransLog saveIPlanTransLog(IPlanAccount iPlanAccount, Integer transType, Integer amount, String transDesc, Integer transStatus, Integer processedAmt) {
        IPlanTransLog iPlanTransLog = new IPlanTransLog();
        iPlanTransLog.setAccountId(iPlanAccount.getId());
        iPlanTransLog.setUserId(iPlanAccount.getUserId());
        iPlanTransLog.setIplanId(iPlanAccount.getIplanId());
        iPlanTransLog.setTransType(transType);
        iPlanTransLog.setTransAmt(amount);
        iPlanTransLog.setProcessedAmt(processedAmt);
        iPlanTransLog.setTransTime(DateUtil.getCurrentDateTime19());
        iPlanTransLog.setTransDesc(transDesc);
        iPlanTransLog.setTransStatus(transStatus);
        iPlanTransLogDao.insert(iPlanTransLog);
        return iPlanTransLog;
    }

    private boolean checkRepayAmount(IPlanAccount iPlanAccount,IPlan iPlan,IPlanRepayDetail iPlanRepayDetail){
        FinanceCalcUtils.CalcResult calcResult = null;
        if(IPlan.REPAY_TYPE_IFPA.equals(iPlan.getRepayType())){
            calcResult = FinanceCalcUtils.calcIFPA(iPlanAccount.getInitPrincipal(), iPlan.getFixRate(), iPlan.getTerm());
        }else if(IPlan.REPAY_TYPE_OTRP.equals(iPlan.getRepayType())){
            calcResult = FinanceCalcUtils.calcOTRP(iPlanAccount.getInitPrincipal(),iPlan.getFixRate(),iPlan.getTerm()*GlobalConfig.ONEMONTH_DAYS);
        }
        if(calcResult!=null){
            FinanceCalcUtils.CalcResult.Detail detail = calcResult.getDetails().get(iPlanRepayDetail.getTerm());
            if(Math.abs(detail.getMonthRepayPrincipal()-iPlanRepayDetail.getDuePrincipal())>2
                    ||Math.abs(detail.getMonthRepayInterest()-iPlanRepayDetail.getDueInterest())>2){
                //如果两边误差大于2 说明有问题
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

}
