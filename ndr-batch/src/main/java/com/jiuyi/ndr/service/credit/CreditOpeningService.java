package com.jiuyi.ndr.service.credit;


import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.credit.CreditTransferLogDao;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.dao.lplan.LPlanTransLogDao;
import com.jiuyi.ndr.dao.subject.SubjectAccountDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.subject.SubjectTransLogDao;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.credit.CreditTransferLog;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.lplan.LPlanTransLog;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectAccount;
import com.jiuyi.ndr.domain.subject.SubjectTransLog;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanRepayScheduleService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.notice.support.TemplateId;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestCancelDebentureSale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by zhangyibo on 2017/4/11.
 */
@Service
public class CreditOpeningService {

    private static final Logger logger = LoggerFactory.getLogger(CreditOpeningService.class);
    @Autowired
    private CreditOpeningDao creditOpeningDao;
    @Autowired
    private CreditDao creditDao;
    @Autowired
    private CreditTransferLogDao creditTransferLogDao;
    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;
    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private LPlanTransLogDao lPlanTransLogDao;
    @Autowired
    private SubjectTransLogDao subjectTransLogDao;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private CreditService creditService;
    @Autowired
    private SubjectAccountDao subjectAccountDao;
    @Autowired
    UserService userService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private IPlanAccountDao iPlanAccountDao;
    @Autowired
    private IPlanDao iPlanDao;
    @Autowired
    private IPlanRepayScheduleService iPlanRepayScheduleService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
     /**
     * 债权购买
     * @param openingCreditId 开放的债权ID
     * @param principal 要购买的本金部分
     * @param transfeeId 受让人id
     * @param sourceChannel 购买该债权的来源
     * @param sourceChannelId sourceChannelID为对应的是账户交易记录ID
     */
    @Transactional
    public void buyCredit(Integer openingCreditId,Integer principal,
                          String transfeeId,Integer sourceChannel,Integer sourceChannelId,Integer dedcutAmt){
        logger.info("开始调用债权购买接口->输入参数:开放中的债权ID={},购买本金={},受让人ID={},购买债权的来源={},来源ID={},使用的抵扣券金额={}",
                openingCreditId,principal,transfeeId,sourceChannel,sourceChannelId,dedcutAmt);
        //查到对应的开放中债权（加锁）
        CreditOpening creditOpening = creditOpeningDao.findByIdForUpdate(openingCreditId);
        if(creditOpening ==null){
            logger.warn("不存在该开放中的债权,开放中债权ID为:"+openingCreditId);
            throw new ProcessException(Error.NDR_0202.getCode(),"不存在该开放中的债权,开放中债权ID为:"+openingCreditId);
        }
        //不是开放中的债权不能购买
        if(!CreditOpening.OPEN_FLAG_ON.equals(creditOpening.getOpenFlag())){
            logger.warn("该债权尚未开放，不可购买:openingCreditId="+openingCreditId);
            throw new ProcessException(Error.NDR_0302.getCode(), Error.NDR_0302.getMessage()+":openingCreditId="+openingCreditId);
        }
        //保存新的债权关系
        Credit newCredit = saveCredits(creditOpening,transfeeId,principal,sourceChannel,sourceChannelId,dedcutAmt);
        //更新转让中的这笔债权的数据
        updateOpeningCredit(creditOpening,principal);
        //保存债权交易流水日志
        saveCreditTransferLog(creditOpening,transfeeId,newCredit.getId(),principal);
    }
    /**
     * 保存债权交易日志
     * @param creditOpening
     * @param transfeeId
     * @param newCreditId
     * @param transferPrincipal
     */
    public CreditTransferLog saveCreditTransferLog(CreditOpening creditOpening, String transfeeId, Integer newCreditId, Integer transferPrincipal) {
        CreditTransferLog creditTransferLog = new CreditTransferLog();
        creditTransferLog.setCreditId(creditOpening.getCreditId());
        creditTransferLog.setSubjectId(creditOpening.getSubjectId());
        creditTransferLog.setTransferorId(creditOpening.getTransferorId());
        creditTransferLog.setTransfereeId(transfeeId);
        creditTransferLog.setNewCreditId(newCreditId);
        creditTransferLog.setTransferPrincipal(transferPrincipal);
        creditTransferLog.setTransferDiscount(creditOpening.getTransferDiscount());
        creditTransferLog.setTransferTime(DateUtil.getCurrentDateTime());
        creditTransferLog.setCreateTime(DateUtil.getCurrentDateTime19());
        creditTransferLogDao.insert(creditTransferLog);
        return creditTransferLog;
    }
    /**
     * 新增新债权人的债权关系
     * @param creditOpening
     * @param transfeeId
     * @param sourceChannel
     * @param sourceChannelId
     * @return Credit 新债权
     */
    private Credit saveCredits(CreditOpening creditOpening,String transfeeId,Integer principal,Integer sourceChannel,Integer sourceChannelId,Integer deductAmt) {
        //Credit transferorCredit = creditDao.findById(creditOpening.getCreditId());
        Subject subject = subjectDao.findBySubjectIdForUpdate(creditOpening.getSubjectId());
        /*if(transferorCredit==null){
            logger.error("查询不到开放中债权对应的原债权,creditOpeningId={},creditId={}",creditOpening.getId(),creditOpening.getCreditId());
            throw new ProcessException(Error.NDR_0202.getCode(),"查询不到开放中债权对应的原债权," +
                    "creditOpeningId=" +creditOpening.getId()+
                    ",creditId={}"+creditOpening.getCreditId());
        }*/
        if(subject==null){
            logger.error("查询不到对应的标的：标的ID={}",creditOpening.getSubjectId());
            throw new ProcessException(Error.NDR_0202.getCode(),"查询不到对应的标的：标的ID="+creditOpening.getSubjectId());
        }

        //新增新债权人的债权关系
        Credit newCredit = new Credit();

        //根据sourceChannelId查询到是哪一笔交易买的这笔债权
        if(Credit.SOURCE_CHANNEL_LPLAN==sourceChannel){
            LPlanTransLog lPlanTransLog = lPlanTransLogDao.findById(sourceChannelId);
            newCredit.setUserIdXM(lPlanTransLog.getUserId());
        }else if(Credit.SOURCE_CHANNEL_IPLAN==sourceChannel||Credit.SOURCE_CHANNEL_YJT==sourceChannel){
            IPlanTransLog iPlanTransLog = iPlanTransLogDao.findByIdForUpdate(sourceChannelId);
            newCredit.setUserIdXM(iPlanTransLog.getUserId());
            newCredit.setSourceAccountId(iPlanTransLog.getAccountId());
        }

        newCredit.setSubjectId(creditOpening.getSubjectId());
        newCredit.setUserId(transfeeId);
        newCredit.setHoldingPrincipal(principal);
        newCredit.setInitPrincipal(principal);
        //剩余期数=标的期数-当前期数+1
        newCredit.setResidualTerm(subject.getTerm()-subject.getCurrentTerm()+1);
        newCredit.setStartTime(DateUtil.getCurrentDateTime());
        newCredit.setEndTime(creditOpening.getEndTime());
        newCredit.setCreditStatus(Credit.CREDIT_STATUS_WAIT);
        newCredit.setSourceChannel(sourceChannel);
        newCredit.setSourceChannelId(sourceChannelId);
        newCredit.setTarget(Credit.TARGET_CREDIT);
        newCredit.setTargetId(creditOpening.getId());
        newCredit.setMarketingAmt(deductAmt);
        newCredit.setCreateTime(DateUtil.getCurrentDateTime19());
        creditDao.insert(newCredit);
        return newCredit;
    }

    private void updateOpeningCredit(CreditOpening creditOpening,Integer principal) {
        int availablePrincipal = creditOpening.getAvailablePrincipal() - principal;
        creditOpening.setAvailablePrincipal(availablePrincipal);
        if(availablePrincipal==0){
            logger.info("openingCreditId={}的开放中债权剩余金额为0，准备关闭。",creditOpening.getId());
            //如果剩余份数==0 将这笔转让中的债权结束掉
            creditOpening.setStatus(CreditOpening.STATUS_FINISH);
            creditOpening.setCloseTime(DateUtil.getCurrentDateTime());
        }
        creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
        creditOpeningDao.update(creditOpening);
    }



    public CreditOpening findById(Integer id) {
        return creditOpeningDao.findById(id);
    }


    public List<CreditOpening> getBySubjectId(String subjectId){
        return creditOpeningDao.findBySubjectId(subjectId);
    }

    /**
     * 根据开放渠道查询开放的债权
     * @param openChannel
     * @return
     */
    public List<CreditOpening> findOpeningCreditByOpenChannelAndSourceChannel(Integer openChannel,Integer sourceChannel){

        int[] factors = new int[]{1,2,3,4,5,6,7};
        Set<Integer> set = new HashSet<>(4);
        for(int factor:factors){
            //找出满足条件的所有组合
            set.add(factor|openChannel);
        }
        return creditOpeningDao.findByOpenChannelInAndSourceChannelAndStatus(set.toArray(new Integer[]{}),sourceChannel, CreditOpening.STATUS_OPENING);
    }


    /**
     * Reflect Invoke
     * 天天赚转出的债权
     * @param openChannel
     * @return
     */
    public List<CreditOpening> findWithdrawLPlan(Integer openChannel){
        int[] factors = new int[]{1,2,3,4,5,6,7};
        Set<Integer> set = new HashSet<>(4);
        for(int factor:factors){
            //找出满足条件的所有组合
            set.add(factor|openChannel);
        }
        List<CreditOpening> creditOpenings = creditOpeningDao.findByOpenChannelInAndSourceChannelAndStatusAndSourceChannelIdIsNull(set.toArray(new Integer[]{}),CreditOpening.SOURCE_CHANNEL_IPLAN, CreditOpening.STATUS_OPENING);

        return creditOpenings;
    }


    /**
     * Reflect Invoke
     * 理财计划提前转出的债权
     * @param openChannel
     * @return
     */
    public List<CreditOpening> findWithdrawIPlan(Integer openChannel){
        List<CreditOpening> creditOpenings = findOpeningCreditByOpenChannelAndSourceChannel(openChannel,CreditOpening.SOURCE_CHANNEL_IPLAN);
        return creditOpenings.stream().filter(creditOpening -> {
                    IPlanTransLog iPlanTransLog = iPlanTransLogDao.findById(creditOpening.getSourceChannelId());
                    return (IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT.equals(iPlanTransLog.getTransType()) || IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN.equals(iPlanTransLog.getTransType()) );
                }).collect(Collectors.toList());
    }

    /**
     * 理财计划到期退出的债权
     * @param openChannel
     * @return
     */
    public List<CreditOpening> findIPlanEnd(Integer openChannel){
        List<CreditOpening> creditOpenings = findOpeningCreditByOpenChannelAndSourceChannel(openChannel,CreditOpening.SOURCE_CHANNEL_IPLAN);
        return creditOpenings.stream().filter(creditOpening -> {
                    IPlanTransLog iPlanTransLog = iPlanTransLogDao.findById(creditOpening.getSourceChannelId());
                    return IPlanTransLog.TRANS_TYPE_NORMAL_EXIT.equals(iPlanTransLog.getTransType());
        }).collect(Collectors.toList());
    }


    /**
     * 债权转让撤销
     * @param creditOpeningId 开放中债权
     */
    @Transactional
    public void cancleCredit(Integer creditOpeningId) {
        CreditOpening creditOpening = creditOpeningDao.findByIdForUpdate(creditOpeningId);
        Subject subject = subjectDao.findBySubjectId(creditOpening.getSubjectId());
        SubjectTransLog subjectTransLog = new SubjectTransLog();
        if(CreditOpening.SOURCE_CHANNEL_SUBJECT == creditOpening.getSourceChannel()){
            subjectTransLog = subjectTransLogDao.findByIdAndStatus(creditOpening.getSourceChannelId());
            if (subjectTransLog == null) {//查询不到转让交易记录,不能撤消
                return;
            }

            if (!SubjectTransLog.TRANS_STATUS_PROCESSING.equals(subjectTransLog.getTransStatus())) {//该交易记录不是处理中的,不能撤消
                return;
            }
        }
        if (creditOpening.getAvailablePrincipal() == 0) {//剩余转让份额为零,不能撤消
            return;
        }
        if (CreditOpening.OPEN_FLAG_OFF.equals(creditOpening.getOpenFlag())) {//债权未开放,不能撤消
            return;
        }
        if (!CreditOpening.STATUS_OPENING.equals(creditOpening.getStatus())) { //债权不是转让中的,不能撤消
            return;
        }
        if (!creditOpening.getTransferPrincipal().equals(creditOpening.getAvailablePrincipal())) {
            List<Credit> credits = creditDao.findByTargetIdAndTarget(creditOpening.getId(), Credit.TARGET_CREDIT);
            if (credits != null && credits.size() == 0) {
                return;
            }
            if (credits.stream().allMatch(credit -> credit.getCreditStatus() == Credit.CREDIT_STATUS_HOLDING)) {
                int totalAmt = 0;
                for (Credit credit : credits) {
                    totalAmt += credit.getHoldingPrincipal();
                }
                if (creditOpening.getTransferPrincipal() != (creditOpening.getAvailablePrincipal() + totalAmt)) {
                    return;
                }
            } else {
                logger.warn("转出记录 {},开放中的债权被购买的还没有全部放款", subjectTransLog.getId());
                return;
            }
        }

        if (BaseResponse.STATUS_PENDING.equals(creditOpening.getExtStatus())) {
            return;
        }
        if(CreditOpening.SOURCE_CHANNEL_SUBJECT != (creditOpening.getSourceChannel())){//把天天赚月月盈的债权开放回去
            creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_IPLAN|GlobalConfig.OPEN_TO_LPLAN);
            creditOpeningDao.update(creditOpening);
            return;
        }
        RequestCancelDebentureSale request = new RequestCancelDebentureSale();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setCreditsaleRequestNo(creditOpening.getExtSn());
        request.setTransCode(TransCode.CREDIT_CANCEL.getCode());

        BaseResponse baseResponse = null;
        try {
            logger.info("开始调用厦门银行取消债权出让接口->{}", JSON.toJSONString(request));
            baseResponse = transactionService.cancelDebentureSale(request);
            logger.info("取消债权出让接口返回->{}", JSON.toJSONString(baseResponse));
        } catch (Exception e) {
            if (baseResponse == null) {
                baseResponse = new BaseResponse();
            }
            baseResponse.setStatus(BaseResponse.STATUS_PENDING);
        }
        //更新开放中的债权状态
        creditOpening.setExtSn(request.getRequestNo());
        creditOpening.setExtStatus(baseResponse.getStatus());
        creditOpeningDao.update(creditOpening);
        if (BaseResponse.STATUS_FAILED.equals(baseResponse.getStatus())){
            return;
        }

        //查询对应的原债权
        Credit credit = creditDao.findByIdForUpdate(creditOpening.getCreditId());
        //将对应的原债权的持有本金加回
        credit.setHoldingPrincipal(credit.getHoldingPrincipal() + creditOpening.getAvailablePrincipal());
        credit.setCreditStatus(Credit.CREDIT_STATUS_HOLDING);
        creditDao.update(credit);
        //查询到对应的散标账户
        SubjectAccount subjectAccount = subjectAccountDao.findByIdForUpdate(credit.getSourceAccountId());
        //将对应账户的当前计息本金加回
        subjectAccount.setCurrentPrincipal(subjectAccount.getCurrentPrincipal() + creditOpening.getAvailablePrincipal());
        subjectAccount.setAmtToTransfer(subjectAccount.getAmtToTransfer() - creditOpening.getAvailablePrincipal());
        Integer expectInterest =(int)(subjectService.getInterestByRepayType(subjectAccount.getCurrentPrincipal(),subject.getInvestRate(),subject.getRate(),credit.getResidualTerm(),subject.getPeriod(),subject.getRepayType())*100);
        Integer expectBonusInterest = (int)(subjectService.getInterestByRepayType(subjectAccount.getCurrentPrincipal(),subject.getBonusRate(),subject.getRate(),credit.getResidualTerm(),subject.getPeriod(),subject.getRepayType())*100);
        subjectAccount.setExpectedInterest(expectInterest);
        subjectAccount.setSubjectExpectedBonusInterest(expectBonusInterest);
        subjectAccount.setStatus(SubjectAccount.STATUS_PROCEEDS);
        subjectAccountDao.update(subjectAccount);

        creditOpening.setStatus(CreditOpening.STATUS_CANCEL_PENDING);
        //交易类型 改为债权撤消
        subjectTransLog.setTransType(SubjectTransLog.TRANS_TYPE_CREDIT_CANCEL);
        subjectTransLog.setProcessedAmt(subjectTransLog.getProcessedAmt() + creditOpening.getTransferPrincipal() - creditOpening.getAvailablePrincipal());
        if(creditOpening.getTransferPrincipal().equals(creditOpening.getAvailablePrincipal())){
            subjectTransLog.setProcessedAmt(subjectTransLog.getProcessedAmt() + 0);
            subjectTransLog.setTransDesc("债权转让取消");
            subjectTransLog.setActualPrincipal(0);
            subjectTransLog.setTransStatus(SubjectTransLog.TRANS_STATUS_SUCCEED);
            creditOpening.setStatus(CreditOpening.STATUS_CANCEL_ALL);
        }
        //更新creditOpening
        creditOpening.setAvailablePrincipal(0);
        creditOpening.setOpenFlag(CreditOpening.OPEN_FLAG_OFF);
        creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
        //更新对应的转让交易记录
        subjectTransLogDao.update(subjectTransLog);
        creditOpeningDao.update(creditOpening);
    }

    //一键投 债权转让自动取消
    @Transactional
    public void cancelCreditTransferNew(Integer transLogId){
        IPlanTransLog iPlanTransLog = iPlanTransLogDao.findByIdAndStatus(transLogId);
        if (iPlanTransLog == null) {//查询不到转让交易记录,不能撤消
            throw new ProcessException(Error.NDR_0706.getCode(), Error.NDR_0706.getMessage());
        }

        if (!IPlanTransLog.TRANS_STATUS_PROCESSING.equals(iPlanTransLog.getTransStatus())) {//该交易记录不是处理中的,不能撤消
            throw new ProcessException(Error.NDR_0707.getCode(), Error.NDR_0707.getMessage());
        }
        List<CreditOpening> creditOpenings = creditOpeningDao.findByTransLogId(transLogId);
        Integer totalCancelAmt = 0;
        boolean flag = false;
        if(creditOpenings != null && creditOpenings.size() > 0){
            for (CreditOpening creditOpening : creditOpenings) {
                if(BaseResponse.STATUS_PENDING.equals(creditOpening.getExtStatus())){
                    throw new ProcessException(Error.NDR_0723.getCode(), Error.NDR_0723.getMessage());
                }
                if(creditOpening.getIplanId()!=null){
                    continue;
                }
                if(CreditOpening.SOURCE_CHANNEL_IPLAN ==creditOpening.getSourceChannel() || CreditOpening.SOURCE_CHANNEL_LPLAN ==creditOpening.getSourceChannel()){//把天天赚月月盈的债权开放回去
                    creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_IPLAN|GlobalConfig.OPEN_TO_LPLAN);
                    creditOpeningDao.update(creditOpening);
                    continue;
                }
                flag = true;

                //调用厦门银行撤消接口
                RequestCancelDebentureSale request = new RequestCancelDebentureSale();
                request.setRequestNo(IdUtil.getRequestNo());
                request.setCreditsaleRequestNo(creditOpening.getExtSn());
                request.setTransCode(TransCode.CREDIT_CANCEL.getCode());

                BaseResponse baseResponse = null;
                try {
                    logger.info("开始调用厦门银行取消债权出让接口->{}", JSON.toJSONString(request));
                    baseResponse = transactionService.cancelDebentureSale(request);
                    logger.info("取消债权出让接口返回->{}", JSON.toJSONString(baseResponse));
                } catch (Exception e) {
                    if (baseResponse == null) {
                        baseResponse = new BaseResponse();
                    }
                    baseResponse.setStatus(BaseResponse.STATUS_PENDING);
                }
                //更新开放中的债权状态
                creditOpening.setExtSn(request.getRequestNo());
                creditOpening.setExtStatus(baseResponse.getStatus());
                creditOpeningDao.update(creditOpening);
                if (BaseResponse.STATUS_FAILED.equals(baseResponse.getStatus())) {
                    return;
                }

                //查询对应的原债权
                Credit credit = creditDao.findByIdForUpdate(creditOpening.getCreditId());
                //将对应的原债权的持有本金加回
                credit.setHoldingPrincipal(credit.getHoldingPrincipal() + creditOpening.getAvailablePrincipal());
                credit.setCreditStatus(Credit.CREDIT_STATUS_HOLDING);
                creditDao.update(credit);

                IPlanAccount iPlanAccount = iPlanAccountDao.findByAccountIdForUpdate(credit.getSourceAccountId());
                //一键投
                IPlan iPlan = iPlanDao.findById(iPlanAccount.getIplanId());
                iPlanAccount.setCurrentPrincipal(iPlanAccount.getCurrentPrincipal() + creditOpening.getAvailablePrincipal());
                iPlanAccount.setAmtToTransfer(iPlanAccount.getAmtToTransfer() - creditOpening.getAvailablePrincipal());
                Integer term = iPlanRepayScheduleService.getCurrentRepayTerm(iPlan.getId()) + 1;
                Integer expectInterest =(int)(subjectService.getInterestByRepayType(iPlanAccount.getCurrentPrincipal(),iPlan.getFixRate(),new BigDecimal("0.144"),term,term * 30,iPlan.getRepayType())*100);
                Integer expectBonusInterest = (int)(subjectService.getInterestByRepayType(iPlanAccount.getCurrentPrincipal(),iPlan.getBonusRate(),new BigDecimal("0.144"),term,term * 30,iPlan.getRepayType())*100);
                iPlanAccount.setExpectedInterest(expectInterest);
                iPlanAccount.setIplanExpectedBonusInterest(expectBonusInterest);
                if(iPlanRepayScheduleService.isNewIplan(iPlan)){
                    iPlanAccountService.calcInterest(iPlanAccount,iPlan);
                }
                iPlanAccount.setStatus(IPlanAccount.STATUS_PROCEEDS);
                iPlanAccountDao.update(iPlanAccount);
                totalCancelAmt += creditOpening.getAvailablePrincipal();
                creditOpening.setStatus(CreditOpening.STATUS_CANCEL_PENDING);
                if (creditOpening.getTransferPrincipal().equals(creditOpening.getAvailablePrincipal())){
                    creditOpening.setStatus(CreditOpening.STATUS_CANCEL_ALL);
                }
                creditOpening.setAvailablePrincipal(0);
                creditOpening.setOpenFlag(CreditOpening.OPEN_FLAG_OFF);
                creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
                //更新creditOpening
                creditOpeningDao.update(creditOpening);
            }
            if(iPlanTransLog.getTransAmt() - totalCancelAmt > 0 ){
                iPlanTransLog.setTransType(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL);
                iPlanTransLog.setTransDesc("债权转让取消");
                iPlanTransLog.setProcessedAmt(iPlanTransLog.getProcessedAmt() + iPlanTransLog.getTransAmt() - totalCancelAmt );
                iPlanTransLogDao.update(iPlanTransLog);
            }else{
                if(flag){
                    iPlanTransLog.setTransType(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL);
                    iPlanTransLog.setTransDesc("债权转让全部取消");
                    iPlanTransLog.setProcessedAmt(iPlanTransLog.getProcessedAmt() + iPlanTransLog.getTransAmt() - totalCancelAmt);
                    iPlanTransLog.setActualAmt(0);
                    iPlanTransLogDao.update(iPlanTransLog);
                }else{
                    throw new ProcessException(Error.NDR_0723.getCode(), Error.NDR_0723.getMessage());
                }
            }
        }else{
            return;
        }
    }


    /**
     * 联机债权转让撤销批量处理
     * @param creditOpeningId 开放中债权
     */
    @Transactional
    public void cancleCreditTime(Integer creditOpeningId) {
        CreditOpening creditOpening = creditOpeningDao.findByIdForUpdate(creditOpeningId);
        Subject subject = subjectDao.findBySubjectId(creditOpening.getSubjectId());
        SubjectTransLog subjectTransLog = new SubjectTransLog();
        IPlanTransLog iPlanTransLog = new IPlanTransLog();
        if (CreditOpening.SOURCE_CHANNEL_SUBJECT == creditOpening.getSourceChannel()) {
            subjectTransLog = subjectTransLogDao.findByIdAndCancelStatus(creditOpening.getSourceChannelId());
            if (subjectTransLog == null) {//查询不到转让交易记录,不能撤消
                return;
            }

            if (!SubjectTransLog.TRANS_STATUS_PROCESSING.equals(subjectTransLog.getTransStatus())) {//该交易记录不是处理中的,不能撤消
                return;
            }
        }
        if (CreditOpening.SOURCE_CHANNEL_YJT == creditOpening.getSourceChannel()) {
            iPlanTransLog = iPlanTransLogDao.findByIdAndCancelStatus(creditOpening.getSourceChannelId());
            if (iPlanTransLog == null) {//查询不到转让交易记录,不能撤消
                return;
            }

            if (!IPlanTransLog.TRANS_STATUS_PROCESSING.equals(iPlanTransLog.getTransStatus())) {//该交易记录不是处理中的,不能撤消
                return;
            }
        }

        if (creditOpening.getAvailablePrincipal() == 0) {//剩余转让份额为零,不能撤消
            return;
        }
        if (CreditOpening.OPEN_FLAG_ON.equals(creditOpening.getOpenFlag())) {//债权在开放中,不能撤消
            return;
        }
        if (!CreditOpening.STATUS_CANCEL_PENDING.equals(creditOpening.getStatus())) { //不是撤销待确认的,不能撤消
            return;
        }
        if (!creditOpening.getTransferPrincipal().equals(creditOpening.getAvailablePrincipal())) {
            List<Credit> credits = creditDao.findByTargetIdAndTarget(creditOpening.getId(), Credit.TARGET_CREDIT);
            if (credits != null && credits.size() == 0) {
                return;
            }
            if (credits.stream().allMatch(credit -> credit.getCreditStatus() == Credit.CREDIT_STATUS_HOLDING)) {
                int totalAmt = 0;
                for (Credit credit : credits) {
                    totalAmt += credit.getHoldingPrincipal();
                }
                if (creditOpening.getTransferPrincipal() != (creditOpening.getAvailablePrincipal() + totalAmt)) {
                    return;
                }
            } else {
                logger.warn("转出记录 {},开放中的债权被购买的还没有全部放款", subjectTransLog.getId());
                return;
            }
        }

        if (BaseResponse.STATUS_PENDING.equals(creditOpening.getExtStatus())) {
            return;
        }
        if (CreditOpening.SOURCE_CHANNEL_IPLAN == creditOpening.getSourceChannel() || CreditOpening.SOURCE_CHANNEL_LPLAN == creditOpening.getSourceChannel()) {//把天天赚月月盈的债权开放回去
            creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_IPLAN | GlobalConfig.OPEN_TO_LPLAN);
            creditOpeningDao.update(creditOpening);
            return;
        }
        RequestCancelDebentureSale request = new RequestCancelDebentureSale();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setCreditsaleRequestNo(creditOpening.getExtSn());
        request.setTransCode(TransCode.CREDIT_CANCEL.getCode());

        BaseResponse baseResponse = null;
        try {
            logger.info("开始调用厦门银行取消债权出让接口->{}", JSON.toJSONString(request));
            baseResponse = transactionService.cancelDebentureSale(request);
            logger.info("取消债权出让接口返回->{}", JSON.toJSONString(baseResponse));
        } catch (Exception e) {
            if (baseResponse == null) {
                baseResponse = new BaseResponse();
            }
            baseResponse.setStatus(BaseResponse.STATUS_PENDING);
        }
        //更新开放中的债权状态
        creditOpening.setExtSn(request.getRequestNo());
        creditOpening.setExtStatus(baseResponse.getStatus());
        creditOpeningDao.update(creditOpening);
        if (BaseResponse.STATUS_FAILED.equals(baseResponse.getStatus())) {
            return;
        }

        //查询对应的原债权
        Credit credit = creditDao.findByIdForUpdate(creditOpening.getCreditId());
        //将对应的原债权的持有本金加回
        credit.setHoldingPrincipal(credit.getHoldingPrincipal() + creditOpening.getAvailablePrincipal());
        credit.setCreditStatus(Credit.CREDIT_STATUS_HOLDING);
        creditDao.update(credit);
        if (CreditOpening.SOURCE_CHANNEL_SUBJECT == creditOpening.getSourceChannel()) {
            //查询到对应的散标账户
            SubjectAccount subjectAccount = subjectAccountDao.findByIdForUpdate(credit.getSourceAccountId());
            //将对应账户的当前计息本金加回
            subjectAccount.setCurrentPrincipal(subjectAccount.getCurrentPrincipal() + creditOpening.getAvailablePrincipal());
            subjectAccount.setAmtToTransfer(subjectAccount.getAmtToTransfer() - creditOpening.getAvailablePrincipal());
            Integer expectInterest = (int) (subjectService.getInterestByRepayType(subjectAccount.getCurrentPrincipal(), subject.getInvestRate(), subject.getRate(), credit.getResidualTerm(), subject.getPeriod(), subject.getRepayType()) * 100);
            Integer expectBonusInterest = (int) (subjectService.getInterestByRepayType(subjectAccount.getCurrentPrincipal(), subject.getBonusRate(), subject.getRate(), credit.getResidualTerm(), subject.getPeriod(), subject.getRepayType()) * 100);
            subjectAccount.setExpectedInterest(expectInterest);
            subjectAccount.setSubjectExpectedBonusInterest(expectBonusInterest);
            subjectAccount.setStatus(SubjectAccount.STATUS_PROCEEDS);
            subjectAccountDao.update(subjectAccount);

            if (creditOpening.getTransferPrincipal().equals(creditOpening.getAvailablePrincipal())) {
                creditOpening.setStatus(CreditOpening.STATUS_CANCEL_ALL);
                subjectTransLog.setTransStatus(SubjectTransLog.TRANS_STATUS_SUCCEED);
                //发送短信
                User user = userService.getUserById(creditOpening.getTransferorId());
                Integer amount = creditOpening.getAvailablePrincipal();
                Double actualPrincipal = ArithUtil.round((amount / 100.0) * (creditOpening.getTransferDiscount().doubleValue()), 2);
                try {
                    String smsTemplate = TemplateId.CREDIT_CANCLE_SUCCESS;
                    noticeService.send(user.getMobileNumber(), subject.getName() + ","
                            + String.valueOf(amount / 100.0) + "," + String.valueOf(actualPrincipal), smsTemplate);
                } catch (Exception e) {
                    logger.error("债权撤消短信发送失败", user.getMobileNumber() + "开放债权id：" + creditOpening.getId() + "撤消金额：" + String.valueOf(amount / 100.0));
                }
            }
            subjectTransLogDao.update(subjectTransLog);
        }else if (CreditOpening.SOURCE_CHANNEL_YJT == creditOpening.getSourceChannel()){
            IPlanAccount iPlanAccount = iPlanAccountDao.findByAccountIdForUpdate(credit.getSourceAccountId());
            //一键投
            IPlan iPlan = iPlanDao.findById(iPlanAccount.getIplanId());
            iPlanAccount.setCurrentPrincipal(iPlanAccount.getCurrentPrincipal() + creditOpening.getAvailablePrincipal());
            iPlanAccount.setAmtToTransfer(iPlanAccount.getAmtToTransfer() - creditOpening.getAvailablePrincipal());
            Integer term = iPlanRepayScheduleService.getCurrentRepayTerm(iPlan.getId()) + 1;
            Integer expectInterest =(int)(subjectService.getInterestByRepayType(iPlanAccount.getCurrentPrincipal(),iPlan.getFixRate(),iPlanAccountService.getRate(iPlan),term,term * 30,iPlan.getRepayType())*100);
            Integer expectBonusInterest = (int)(subjectService.getInterestByRepayType(iPlanAccount.getCurrentPrincipal(),iPlan.getBonusRate(),iPlanAccountService.getRate(iPlan),term,term * 30,iPlan.getRepayType())*100);
            iPlanAccount.setExpectedInterest(expectInterest);
            iPlanAccount.setIplanExpectedBonusInterest(expectBonusInterest);
            if(iPlanRepayScheduleService.isNewIplan(iPlan)){
                iPlanAccountService.calcInterest(iPlanAccount,iPlan);
            }
            iPlanAccount.setStatus(IPlanAccount.STATUS_PROCEEDS);
            iPlanAccountDao.update(iPlanAccount);
            if (creditOpening.getTransferPrincipal().equals(creditOpening.getAvailablePrincipal())){
                creditOpening.setStatus(CreditOpening.STATUS_CANCEL_ALL);
            }
        }
            //更新creditOpening
            creditOpening.setAvailablePrincipal(0);
            creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
            //更新对应的转让交易记录
            creditOpeningDao.update(creditOpening);

    }


}
