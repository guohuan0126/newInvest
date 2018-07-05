package com.jiuyi.ndr.service.subject;

import com.github.pagehelper.PageHelper;
import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.config.TransferConfigDao;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.dao.user.UserOtherInfoDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.config.TransferConfig;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.subject.*;
import com.jiuyi.ndr.domain.transferstation.AgricultureLoanInfo;
import com.jiuyi.ndr.domain.transferstation.LoanIntermediaries;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.domain.user.UserOtherInfo;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditTransferConfirmDto;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditTransferDto;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditTransferSuccessDto;
import com.jiuyi.ndr.dto.subject.SubjectDto;
import com.jiuyi.ndr.dto.subject.SubjectInvestorDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.rest.customannotation.PutRedis;
import com.jiuyi.ndr.service.account.PlatformAccountService;
import com.jiuyi.ndr.service.account.PlatformTransferService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.lplan.LPlanAccountService;
import com.jiuyi.ndr.service.marketing.MarketService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.DealUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import com.jiuyi.ndr.util.redis.RedisLock;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestSingleTrans;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.request.RequestUserAutoPreTransaction;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.query.TransactionQueryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubjectService {

    private final static Logger logger = LoggerFactory.getLogger(SubjectService.class);
    private static final String SCREDITTRANSFER = "SUBJECT_CREDIT_TRANSFER";
    private static final String YJTCREDITTRANSFER = "YJT_CREDIT_TRANSFER";

    private DecimalFormat df = new DecimalFormat("######0.00");
    private DecimalFormat df2 = new DecimalFormat("######0");
    private static DecimalFormat df3 = new DecimalFormat("0.####");
    private DecimalFormat df4 = new DecimalFormat("######0.##");
    private static DecimalFormat df5 = new DecimalFormat("######0.######");

    @Autowired
    private CreditService creditService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private SubjectAdvancedPayOffService subjectAdvancedPayOffService;
    @Autowired
    private PlatformAccountService platformAccountService;
    @Autowired
    private PlatformTransferService platformTransferService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private CreditOpeningDao creditOpeningDao;
    @Autowired
    private SubjectRepayDetailDao repayDetailDao;
    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private UserOtherInfoDao userOtherInfoDao;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private LPlanAccountService lPlanAccountService;
    @Autowired
    private SubjectTransLogService subjcetTransLogService;
    @Autowired
    private UserService userService;
    @Autowired
    private SubjectTransferParamService subjectTransferParamService;
    @Autowired
    private CreditOpeningService creditOpeningService;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private RedisLock redisLock;
    @Autowired
    private CreditDao creditDao;
    @Autowired
    private SubjectTransLogService subjectTransLogService;
    @Autowired
    private MarketService marketService;
    @Autowired
    private TransferConfigDao transferConfigDao;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private IPlanAccountDao iPlanAccountDao;

    //标的正常还款 提前结清
    @Transactional(noRollbackFor = Exception.class)
    public void advancedPayOff(String subjectId) {
        Subject subject = this.findBySubjectIdForUpdate(subjectId);
        if (Subject.REPAY_PAYOFF.equals(subject.getRepayStatus().trim()) || Subject.REPAY_ADVANCED_PAYOFF.equals(subject.getRepayStatus().trim())) {
            //已还款完成，无需再还
            logger.warn("标的{}已到期结束或已提前结清，请勿重复还款", subjectId);
            throw new ProcessException(Error.NDR_0408);
        }
        if (Subject.REPAY_OVERDUE.equals(subject.getRepayStatus().trim())) {
            //标的有逾期，需先还清逾期
            logger.warn("标的{}有逾期未还清，请先还清逾期", subjectId);
            throw new ProcessException(Error.NDR_0409);
        }
        List<CreditOpening> unlendedCreditOpenings = creditOpeningDao.findNotLendedBySubjectId(subjectId);
        if (unlendedCreditOpenings != null && unlendedCreditOpenings.size() > 0) {
            //标的有未完成的转让中债权，不能还款
            logger.warn("标的{}有未完成的转让中债权，暂不能还款", subjectId);
            throw new ProcessException(Error.NDR_0426);
        }
        List<Credit> credits = creditService.findCreditsBySubjectId(subjectId);//查询债权关系
        if (credits.stream().anyMatch(credit -> credit.getCreditStatus().equals(Credit.CREDIT_STATUS_WAIT))) {
            //标的有未确认债权，不能还款
            logger.warn("标的{}有未确认债权，暂不能还款", subjectId);
            throw new ProcessException(Error.NDR_0427);
        }
        int currentTerm = subject.getCurrentTerm();
        SubjectRepaySchedule subjectRepaySchedule = subjectRepayScheduleService.findRepaySchedule(subjectId, currentTerm);

        boolean isDirect = this.isDirect(subject.getDirectFlag());//是否直贷

        boolean needMarket = isDirect, needFreeze = true, needRepay = true;
        String currentStep = "";
        if (isDirect) {
            currentStep = subjectRepaySchedule.getCurrentStep() == null ? "market" : subjectRepaySchedule.getCurrentStep().toLowerCase();
        } else {
            currentStep = subjectRepaySchedule.getCurrentStep() == null ? "freeze" : subjectRepaySchedule.getCurrentStep().toLowerCase();
        }

        String extSn = subjectRepaySchedule.getExtSn();
        Integer extStatus = subjectRepaySchedule.getExtStatus();
        switch (currentStep) {
            case "market":
                if (extStatus != null && extStatus.equals(BaseResponse.STATUS_SUCCEED)) {
                    needMarket = false;
                }
                break;
            case "freeze":
                needMarket = false;
                if (extStatus != null && extStatus.equals(BaseResponse.STATUS_SUCCEED)) {
                    needFreeze = false;
                }
                break;
            case "repay":
                needMarket = false;
                needFreeze = false;
                needRepay = false;
                break;
        }

        //计算提前结清罚息
        Integer payOffPenalty = subjectAdvancedPayOffService.advancedPayOff(subjectId);

        Map<String, Integer> borrowerDetails = new HashMap<>();
        borrowerDetails.put("duePrincipal", subject.getTotalAmt() - subject.getPaidPrincipal());
        borrowerDetails.put("dueInterest", subjectRepaySchedule.getDueInterest());
        borrowerDetails.put("duePenalty", subjectRepaySchedule.getDuePenalty() + payOffPenalty);
        borrowerDetails.put("dueFee", subjectRepaySchedule.getDueFee());

        if (needMarket) {
            String requestNo = null;
            if (extStatus != null && extStatus.equals(BaseResponse.STATUS_PENDING) && "market".equals(currentStep)) {
                //上次发放营销款状态未知，同样的流水号再发送一次
                requestNo = extSn;
            }
            double marketingAmount = (borrowerDetails.get("duePrincipal") + borrowerDetails.get("dueInterest") + borrowerDetails.get("duePenalty") + borrowerDetails.get("dueFee")) / 100.0;
            double borrowerPrincipal = borrowerDetails.get("duePrincipal") / 100.0;
            double borrowerInterest = (borrowerDetails.get("dueInterest") + borrowerDetails.get("duePenalty") + borrowerDetails.get("dueFee")) / 100.0;
            BaseResponse marketingResponse;
            try {
                marketingResponse = this.marketingForRepay(subject.getBorrowerIdXM(), marketingAmount, requestNo);
                subjectRepaySchedule.setExtSn(marketingResponse.getRequestNo());
                subjectRepaySchedule.setExtStatus(marketingResponse.getStatus());
                subjectRepaySchedule.setCurrentStep("market");
                if (marketingResponse.getStatus().equals(BaseResponse.STATUS_FAILED)) {
                    logger.warn("标的{}提前结清失败，发放营销款失败", subjectId);
                    noticeService.sendEmail("天天赚结清营销款账户异常", "直贷营销款到借款人充值失败", "zhangjunying@duanrong.com,zhangyibo@duanrong.com,lixiaolei@duanrong.com");
                    throw new ProcessException(Error.NDR_0412.getCode(), Error.NDR_0412.getMessage() + marketingResponse.getDescription());
                }
                if (marketingResponse.getStatus().equals(BaseResponse.STATUS_PENDING)) {
                    logger.warn("标的{}提前结清失败，发放营销款状态未知", subjectId);
                    noticeService.sendEmail("天天赚结清营销款账户异常", "直贷营销款到借款人充值状态未知", "zhangjunying@duanrong.com,zhangyibo@duanrong.com,lixiaolei@duanrong.com");
                    throw new ProcessException(Error.NDR_0413.getCode(), Error.NDR_0413.getMessage() + marketingResponse.getDescription());
                }
                //营销款短融本地账户扣款
                platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_01_DR, marketingAmount, BusinessEnum.ndr_repay, "借款人提前结清代充-" + subject.getName(), marketingResponse.getRequestNo());
                //营销款出账-插入表platform_transfer
                platformTransferService.out(subject.getBorrowerId(), marketingAmount, String.valueOf(subjectRepaySchedule.getId()), marketingResponse.getRequestNo(), subject.getIntermediatorId());
                userAccountService.transferIn(subject.getBorrowerIdXM(), marketingAmount, BusinessEnum.ndr_pt_transfer, "借款人结清代充-"+ subject.getName(), "借款人结清代充，标的ID:"+ subject.getSubjectId() + "，本金：" + borrowerPrincipal + "，利息：" + borrowerInterest, marketingResponse.getRequestNo());
            } catch (ProcessException pe) {
                subjectRepayScheduleService.update(subjectRepaySchedule);
                throw pe;
            }
        }
        if (needFreeze) {
            String requestNo = null;
            if (extStatus != null && extStatus.equals(BaseResponse.STATUS_PENDING) && "freeze".equals(currentStep)) {
                //上次还款预处理状态未知，同样的流水号再发送一次
                requestNo = extSn;
            }
            double freezeAmount = (borrowerDetails.get("duePrincipal") + borrowerDetails.get("dueInterest") + borrowerDetails.get("duePenalty") + borrowerDetails.get("dueFee")) / 100.0;
            BaseResponse freezeResponse;
            try {
                freezeResponse = this.freezeForRepay(subjectId, isDirect ? subject.getBorrowerIdXM() : subject.getIntermediatorIdXM(), freezeAmount, requestNo);
                subjectRepaySchedule.setExtSn(freezeResponse.getRequestNo());
                subjectRepaySchedule.setExtStatus(freezeResponse.getStatus());
                subjectRepaySchedule.setCurrentStep("freeze");
                if (freezeResponse.getStatus().equals(BaseResponse.STATUS_FAILED)) {
                    logger.warn("标的{}提前结清失败，冻结借款人资金失败", subjectId);
                    throw new ProcessException(Error.NDR_0414.getCode(), Error.NDR_0414.getMessage() + freezeResponse.getDescription());
                }
                if (freezeResponse.getStatus().equals(BaseResponse.STATUS_PENDING)) {
                    logger.warn("标的{}提前结清失败，冻结借款人资金状态未知", subjectId);
                    throw new ProcessException(Error.NDR_0415.getCode(), Error.NDR_0415.getMessage() + freezeResponse.getDescription());
                }
                userAccountService.freeze(isDirect ? subject.getBorrowerIdXM() : subject.getIntermediatorIdXM(), freezeAmount, BusinessEnum.ndr_repay, "标的结清冻结", "标的结清冻结,标的号：" + subjectId, freezeResponse.getRequestNo());
            } catch (ProcessException pe) {
                subjectRepayScheduleService.update(subjectRepaySchedule);
                throw pe;
            }
        }

        if (needRepay) {
            Map<String, Map<String, Object>> lplanDetails = null, subjectDetails = null, iplanDetails = null;
            Set<Integer> channels = credits.stream().map(credit -> credit.getSourceChannel()).collect(Collectors.toSet());//查询该标的的所有购买渠道
            for (Integer channel : channels) {
                if (Credit.SOURCE_CHANNEL_SUBJECT == channel) {
                }
                if (Credit.SOURCE_CHANNEL_IPLAN == channel) {
                    // 调用定期账户还款服务，计算还款金额明细
                    iplanDetails = iPlanAccountService.subjectRepayForIPlan(subjectId, currentTerm, borrowerDetails);
                }
                if (Credit.SOURCE_CHANNEL_LPLAN == channel) {
                    lplanDetails = lPlanAccountService.subjectRepayForLPlan(subjectId, currentTerm, borrowerDetails);
                }
            }

            subjectRepaySchedule.setCurrentStep("repay");
            this.saveRepayDetails(subjectRepaySchedule.getId(), subject.getSubjectId(), borrowerDetails, subjectDetails, iplanDetails, lplanDetails);
        }

        //还款发起成功，更新本地债权、转让中债权、还款计划、标的表数据
        //更新债权表
        for (Credit credit : credits) {
            //将当前标的对应的所有债权清0，并结束所有债权
            credit.setHoldingPrincipal(0);
            credit.setResidualTerm(0);
            credit.setCreditStatus(Credit.CREDIT_STATUS_FINISH);
            creditService.update(credit);

            //更新该债权转出中部分
            /*List<CreditOpening> creditsOpening = openingCreditService.findByCreditId(credit.getId());
            for (CreditOpening creditOpening : creditsOpening) {
                if (creditOpening.getStatus().equals(CreditOpening.STATUS_OPENING)) {
                    creditOpening.setStatus(CreditOpening.STATUS_FINISH);
                    creditOpening.setCloseTime(DateUtil.getCurrentDateTime());
                    creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
                    creditOpeningDao.update(creditOpening);
                }
            }*/
        }

        //更新标的
        subject.setPaidPrincipal(subject.getTotalAmt());
        subject.setPaidInterest(subject.getPaidInterest() + borrowerDetails.get("dueInterest"));
        subject.setRepayStatus(Subject.REPAY_ADVANCED_PAYOFF);
        subject.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectDao.update(subject);

        //更新还款计划表
        subjectRepaySchedule.setRepayDate(DateUtil.getCurrentDateShort());
        subjectRepaySchedule.setRepayTime(DateUtil.getCurrentDateTime().substring(9));
        subjectRepaySchedule.setDuePrincipal(borrowerDetails.get("duePrincipal"));
        subjectRepaySchedule.setDuePenalty(borrowerDetails.get("duePenalty"));
        subjectRepaySchedule.setStatus(SubjectRepaySchedule.STATUS_ADVANCE_PAYOFF);
        subjectRepayScheduleService.update(subjectRepaySchedule);
        //更新后续所有期还款计划，设置为已提前结清
        for (int i = currentTerm + 1; i <= subject.getTerm(); i++) {
            SubjectRepaySchedule schedule = subjectRepayScheduleService.findRepaySchedule(subjectId, i);
//            schedule.setRepayDate(null);
            schedule.setRepayTime(DateUtil.getCurrentDateTime().substring(9));
            schedule.setStatus(SubjectRepaySchedule.STATUS_ADVANCE_PAYOFF);
            schedule.setDuePrincipal(0);//应财务对账要求，设置为0
            schedule.setDueInterest(0);//应财务对账要求，设置为0
            subjectRepayScheduleService.update(schedule);
        }
    }

    //标的按期还款：
    //打营销款market -> 冻结freeze -> 还款repay
    @Transactional(noRollbackFor = Exception.class)
    public void repay(String subjectId, Integer term) {
        SubjectRepaySchedule subjectRepaySchedule = subjectRepayScheduleService.findRepaySchedule(subjectId, term);
        if (SubjectRepaySchedule.STATUS_NORMAL_REPAID.equals(subjectRepaySchedule.getStatus())
                || SubjectRepaySchedule.STATUS_OVERDUE_REPAID.equals(subjectRepaySchedule.getStatus())
                || SubjectRepaySchedule.STATUS_ADVANCE_PAYOFF.equals(subjectRepaySchedule.getStatus())) {
            //已还款完成，无需再还
            logger.warn("标的{}第{}期已还款完成，请勿重复还款", subjectId, term);
            return;
        }
        List<CreditOpening> unlendedCreditOpenings = creditOpeningDao.findNotLendedBySubjectId(subjectId);
        if (unlendedCreditOpenings != null && unlendedCreditOpenings.size() > 0) {
            //标的有未完成的转让中债权，不能还款
            logger.warn("标的{}有未完成的转让中债权，暂不能还款", subjectId);
            return;
        }
        List<Credit> credits = creditService.findCreditsBySubjectId(subjectId);//查询债权关系
        if (credits.stream().anyMatch(credit -> credit.getCreditStatus().equals(Credit.CREDIT_STATUS_WAIT))) {
            //标的有未确认债权，不能还款
            logger.warn("标的{}有未确认债权，暂不能还款", subjectId);
            return;
        }
        Subject subject = this.findBySubjectIdForUpdate(subjectId);
        boolean isDirect = this.isDirect(subject.getDirectFlag());//是否直贷

        //直贷 - market/freeze/repay
        //债转 - freeze/repay
        boolean needMarket = isDirect, needFreeze = true, needRepay = true;
        String currentStep;
        if (isDirect) {
            currentStep = subjectRepaySchedule.getCurrentStep() == null ? "market" : subjectRepaySchedule.getCurrentStep().toLowerCase();
        } else {
            currentStep = subjectRepaySchedule.getCurrentStep() == null ? "freeze" : subjectRepaySchedule.getCurrentStep().toLowerCase();
        }
        String extSn = subjectRepaySchedule.getExtSn();
        Integer extStatus = subjectRepaySchedule.getExtStatus();
        switch (currentStep) {
            case "market":
                if (extStatus != null && extStatus.equals(BaseResponse.STATUS_SUCCEED)) {
                    needMarket = false;
                }
                break;
            case "freeze":
                needMarket = false;
                if (extStatus != null && extStatus.equals(BaseResponse.STATUS_SUCCEED)) {
                    needFreeze = false;
                }
                break;
            case "repay":
                needMarket = false;
                needFreeze = false;
                needRepay = false;
                break;
        }

        //借款信息
        Map<String, Integer> borrowerDetails = new HashMap<>();
        borrowerDetails.put("duePrincipal", subjectRepaySchedule.getDuePrincipal());//当期应还本金
        borrowerDetails.put("dueInterest", subjectRepaySchedule.getDueInterest());//当期应还利息
        borrowerDetails.put("duePenalty", subjectRepaySchedule.getDuePenalty());//当期应还罚息
        borrowerDetails.put("dueFee", subjectRepaySchedule.getDueFee());//当期应还费用

        if (needMarket) {
            String requestNo = this.getRequestNo(extSn,extStatus);
            double marketingAmount = (borrowerDetails.get("duePrincipal") + borrowerDetails.get("dueInterest") + borrowerDetails.get("duePenalty") + borrowerDetails.get("dueFee")) / 100.0;
            double borrowerPrincipal = borrowerDetails.get("duePrincipal") / 100.0;
            double borrowerInterest = (borrowerDetails.get("dueInterest") + borrowerDetails.get("duePenalty") + borrowerDetails.get("dueFee")) / 100.0;
            //进行单笔业务查询 是否需要给营销款打钱
            RequestSingleTransQuery request = new RequestSingleTransQuery();
            request.setRequestNo(requestNo);
            request.setTransactionType(TransactionType.TRANSACTION);
            ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
            if(!"0".equals(responseQuery.getCode())){
                //查询交易失败
                logger.info("发放营销款交易状态未知，单笔查询，请求流水号{},查询失败，返回码{}", requestNo, responseQuery.getCode());
                if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(responseQuery.getCode())) {
                    //订单不存在 重新发起交易
                    BaseResponse marketingResponse;
                    try {
                        marketingResponse = this.marketingForRepay(subject.getBorrowerIdXM(), marketingAmount, requestNo);
                        subjectRepaySchedule.setExtSn(marketingResponse.getRequestNo());
                        subjectRepaySchedule.setExtStatus(marketingResponse.getStatus());
                        subjectRepaySchedule.setCurrentStep("market");
                        if (marketingResponse.getStatus().equals(BaseResponse.STATUS_FAILED)) {
                            logger.warn("标的{}第{}期还款失败，发放营销款失败", subjectId, term);
                            noticeService.sendEmail("天天赚还款营销款账户异常", "直贷营销款到借款人充值失败", "zhangjunying@duanrong.com,zhangyibo@duanrong.com,lixiaolei@duanrong.com");
                            throw new ProcessException(Error.NDR_0412.getCode(), Error.NDR_0412.getMessage() +
                                    marketingResponse.getDescription());
                        }
                        if (marketingResponse.getStatus().equals(BaseResponse.STATUS_PENDING)) {
                            noticeService.sendEmail("天天赚还款营销款账户异常", "直贷营销款到借款人充值状态未知", "zhangjunying@duanrong.com,zhangyibo@duanrong.com,lixiaolei@duanrong.com");
                            logger.warn("标的{}第{}期还款失败，发放营销款状态未知", subjectId, term);
                            throw new ProcessException(Error.NDR_0413.getCode(), Error.NDR_0413.getMessage() +
                                    marketingResponse.getDescription());
                        }
                        //营销款短融本地账户扣款
                        platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_01_DR, marketingAmount, BusinessEnum.ndr_repay, "借款人还款代充：" + subject.getName() + "，本金：" + borrowerPrincipal + "，利息：" + borrowerInterest, marketingResponse.getRequestNo());
                        //营销款出账-插入表platform_transfer
                        platformTransferService.out(subject.getBorrowerId(), marketingAmount, String.valueOf(subjectRepaySchedule.getId()), marketingResponse.getRequestNo(), subject.getIntermediatorId());
                        //借款人入账-本地账户更新
                        userAccountService.transferIn(subject.getBorrowerIdXM(), marketingAmount, BusinessEnum.ndr_pt_transfer, "借款人还款代充："+subject.getName(), "借款人还款代充：" + subject.getName() + "，本金：" + borrowerPrincipal + "，利息：" + borrowerInterest, marketingResponse.getRequestNo());
                    }catch(ProcessException pe) {
                        subjectRepayScheduleService.update(subjectRepaySchedule);
                        throw pe;
                    }
                }
            }else{
                TransactionQueryRecord transactionQueryRecord = (TransactionQueryRecord) responseQuery.getRecords().get(0);
                if("SUCCESS".equals(transactionQueryRecord.getStatus())){
                    //交易成功
                    subjectRepaySchedule.setExtStatus(BaseResponse.STATUS_SUCCEED);
                }else if ("FAIL".equals(transactionQueryRecord.getStatus())) {
                    //查询结果，交易失败，重新发起交易
                    logger.info("发放营销款交易状态失败，单笔查询，请求流水号{},交易查询失败，重新发起交易", requestNo);
                    subjectRepaySchedule.setExtStatus(BaseResponse.STATUS_FAILED);
                } else {
                    //查询结果：交易处理中
                    logger.info("发放营销款交易状态未知，单笔查询，请求流水号{},交易查询处理中", requestNo);
                    subjectRepaySchedule.setExtStatus(BaseResponse.STATUS_PENDING);
                }

                //将当前步设为market
                subjectRepaySchedule.setCurrentStep("market");
            }
        }
        if (needFreeze) {
            String requestNo = this.getRequestNo(extSn,extStatus);
            double freezeAmount = (borrowerDetails.get("duePrincipal") + borrowerDetails.get("dueInterest") + borrowerDetails.get("duePenalty") + borrowerDetails.get("dueFee")) / 100.0;
            BaseResponse freezeResponse;
            try {
                freezeResponse = this.freezeForRepay(subjectId, isDirect ? subject.getBorrowerIdXM() : subject.getIntermediatorIdXM(), freezeAmount, requestNo);
                subjectRepaySchedule.setExtSn(freezeResponse.getRequestNo());
                subjectRepaySchedule.setExtStatus(freezeResponse.getStatus());
                subjectRepaySchedule.setCurrentStep("freeze");
                if (freezeResponse.getStatus().equals(BaseResponse.STATUS_FAILED)) {
                    logger.warn("标的{}第{}期还款失败，冻结借款人资金失败", subjectId, term);
                    throw new ProcessException(Error.NDR_0414.getCode(), Error.NDR_0414.getMessage() + freezeResponse.getDescription());
                }
                if (freezeResponse.getStatus().equals(BaseResponse.STATUS_PENDING)) {
                    logger.warn("标的{}第{}期还款失败，冻结借款人资金状态未知", subjectId, term);
                    throw new ProcessException(Error.NDR_0415.getCode(), Error.NDR_0415.getMessage() + freezeResponse.getDescription());
                }
                userAccountService.freeze(isDirect ? subject.getBorrowerIdXM() : subject.getIntermediatorIdXM(), freezeAmount, BusinessEnum.ndr_repay, "标的还款冻结-"+subject.getName(), "标的还款冻结,标的号：" + subjectId +",本金:"+subjectRepaySchedule.getDuePrincipal()+",利息:"+subjectRepaySchedule.getDueInterest(), freezeResponse.getRequestNo());
            } catch (ProcessException pe) {
                subjectRepayScheduleService.update(subjectRepaySchedule);
                throw pe;
            }
        }

        if (needRepay) {
            Map<String, Map<String, Object>> lplanDetails = null, subjectDetails = null, iplanDetails = null;
            Set<Integer> channels = credits.stream().map(credit -> credit.getSourceChannel()).collect(Collectors.toSet());//查询该标的的所有购买渠道
            for (Integer channel : channels) {
                if (Credit.SOURCE_CHANNEL_SUBJECT == channel) {
                }
                if (Credit.SOURCE_CHANNEL_IPLAN == channel) {
                    // 调用定期账户还款服务，计算还款金额明细
                    iplanDetails = iPlanAccountService.subjectRepayForIPlan(subjectId, term, borrowerDetails);
                }
                if (Credit.SOURCE_CHANNEL_LPLAN == channel) {
                    // 调用活期账户还款服务，计算还款金额明细
                    lplanDetails = lPlanAccountService.subjectRepayForLPlan(subjectId, term, borrowerDetails);
                }
            }
            subjectRepaySchedule.setCurrentStep("repay");
            this.saveRepayDetails(subjectRepaySchedule.getId(), subject.getSubjectId(), borrowerDetails, subjectDetails, iplanDetails, lplanDetails);
        }

        //还款发起成功，更新本地债权、转让中债权、还款计划、标的表数据
        //计算债权的所有本金
        Integer totalPrincipal = credits.stream().map(Credit::getHoldingPrincipal).reduce(Integer::sum).orElse(0);
        //更新债权表
        for (Credit credit : credits) {
            int principalPaid = new BigDecimal(borrowerDetails.get("duePrincipal")).multiply(new BigDecimal(credit.getHoldingPrincipal())).divide(BigDecimal.valueOf(totalPrincipal), 6, BigDecimal.ROUND_DOWN).intValue();
            if (principalPaid > 0) {//若回款本金>0
                credit.setHoldingPrincipal(credit.getHoldingPrincipal() - principalPaid);
            }
            credit.setResidualTerm(credit.getResidualTerm() - 1);
            if (subject.getTerm().equals(term)) {
                credit.setHoldingPrincipal(0);//运算过程会有舍入，怕最后一期本金会有剩余，所以手动清0
                credit.setCreditStatus(Credit.CREDIT_STATUS_FINISH);
            }
            creditService.update(credit);

            //更新该债权转出中部分
           /* List<CreditOpening> creditsOpening = openingCreditService.findByCreditId(credit.getId());
            for (CreditOpening creditOpening : creditsOpening) {
                if (CreditOpening.STATUS_OPENING.equals(creditOpening.getStatus())) {
                    boolean creditOpeningUpdated = false;
                    //如果是最后一期还款，则直接结束掉转让中的债权
                    if (subject.getTerm().equals(term)) {
                        creditOpeningUpdated = true;
                        creditOpening.setStatus(CreditOpening.STATUS_FINISH);
                        creditOpening.setCloseTime(DateUtil.getCurrentDateTime());
                    }
                    if (creditOpeningUpdated) {
                        creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
                        creditOpeningDao.update(creditOpening);
                    }
                }
            }*/
        }
        //更新标的
        subject.setPaidPrincipal(subject.getPaidPrincipal() + borrowerDetails.get("duePrincipal"));
        subject.setPaidInterest(subject.getPaidInterest() + borrowerDetails.get("dueInterest"));
        if (term.equals(subject.getTerm())) {//到期结束
            //this.changeSubjectXM(subjectId, Subject.SUBJECT_STATUS_FINISH_XM);//厦门银行标的结束
            subject.setRepayStatus(Subject.REPAY_PAYOFF);
        } else {
            subject.setCurrentTerm(term + 1);
            subject.setRepayStatus(Subject.REPAY_NORMAL);
        }
        subject.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectDao.update(subject);

        boolean overdue = Subject.REPAY_OVERDUE.equals(subject.getRepayStatus());

        //更新还款计划表
        subjectRepaySchedule.setRepayDate(DateUtil.getCurrentDateShort());
        subjectRepaySchedule.setRepayTime(DateUtil.getCurrentDateTime().substring(9));
        if (overdue) {
            subjectRepaySchedule.setStatus(SubjectRepaySchedule.STATUS_OVERDUE_REPAID);
        } else {
            subjectRepaySchedule.setStatus(SubjectRepaySchedule.STATUS_NORMAL_REPAID);
        }
        subjectRepayScheduleService.update(subjectRepaySchedule);
    }

    //给账户打营销款，为还款做准备
    private BaseResponse marketingForRepay(String dest, Double amount, String requestNo) {
        RequestSingleTrans marketingTrans = new RequestSingleTrans();
        if (requestNo == null) {
            requestNo = IdUtil.getRequestNo();
        }
        marketingTrans.setTradeType(TradeType.MARKETING);
        marketingTrans.setRequestNo(requestNo);
        marketingTrans.setTransCode(TransCode.MARKET002_01_TRANSFER.getCode());
        List<RequestSingleTrans.Detail> details = new ArrayList<>();
        RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
        detail.setBizType(BizType.MARKETING);
        detail.setSourcePlatformUserNo(GlobalConfig.MARKETING_ACCOUNT_01_XM);
        detail.setTargetPlatformUserNo(dest);
        detail.setAmount(amount);
        details.add(detail);
        marketingTrans.setDetails(details);

        return transactionService.singleTrans(marketingTrans);
    }

    //冻结账户金额，为还款做准备
    private BaseResponse freezeForRepay(String subjectId, String dest, Double amount, String requestNo) {
        RequestUserAutoPreTransaction request = new RequestUserAutoPreTransaction();
        if (requestNo == null) {
            requestNo = IdUtil.getRequestNo();
        }
        request.setRequestNo(requestNo);
        request.setPlatformUserNo(dest);
        request.setBizType(BizType.REPAYMENT);
        request.setAmount(amount);
        request.setProjectNo(subjectId);
        return transactionService.userAutoPreTransaction(request);
    }
    @ProductSlave
    public Subject findById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        return subjectDao.findById(id);
    }
    @ProductSlave
    public Subject getBySubjectId(String subjectId) {
        if (!StringUtils.hasText(subjectId)) {
            throw new IllegalArgumentException("subjectId不能为空");
        }
        return subjectDao.findBySubjectId(subjectId);
    }

    @Transactional
    public Subject findByIdForUpdate(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        return subjectDao.findByIdForUpdate(id);
    }

    @Transactional
    public Subject findBySubjectIdForUpdate(String subjectId) {
        if (!StringUtils.hasText(subjectId)) {
            throw new IllegalArgumentException("subjectId不能为空");
        }
        return subjectDao.findBySubjectIdForUpdate(subjectId);
    }

    public Subject insert(Subject subject) {
        if (subject == null) {
            throw new IllegalArgumentException("subject不能为空");
        }
        subject.setCreateTime(DateUtil.getCurrentDateTime19());
        subjectDao.insert(subject);
        return subject;
    }

    public Subject update(Subject subject) {
        if (subject.getId() == null && !StringUtils.hasText(subject.getSubjectId())) {
            throw new IllegalArgumentException("id或subjectId不能为空");
        }
        subject.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectDao.update(subject);
        return subject;
    }

    //保存还款明细
    private void saveRepayDetails(Integer scheduleId, String subjectId, Map<String, Integer> borrowerDetails,
                                  Map<String, Map<String, Object>> subjectDetails,
                                  Map<String, Map<String, Object>> iplanDetails,
                                  Map<String, Map<String, Object>> lplanDetails) {
        List<SubjectRepayDetail> list = new ArrayList<>();
        int totalPaidAmt = 0;
        if (subjectDetails != null) {
            for (Map.Entry<String, Map<String, Object>> entry : subjectDetails.entrySet()) {
                String userId = entry.getKey();
                Map<String, Object> result = entry.getValue();
                String userIdXm = (String) result.get("userIdXm");
                int interest = (int) (result.get("interest") == null ? 0 : result.get("interest"));
                int principal = (int) (result.get("principal") == null ? 0 : result.get("principal"));
                int penalty = (int) (result.get("penalty") == null ? 0 : result.get("penalty"));
                int fee = (int) (result.get("fee") == null ? 0 : result.get("fee"));
                int interestFreeze = (int) (result.get("interestFreeze") == null ? 0 : result.get("interestFreeze"));
                int principalFreeze = (int) (result.get("principalFreeze") == null ? 0 : result.get("principalFreeze"));
                int penaltyFreeze = (int) (result.get("penaltyFreeze") == null ? 0 : result.get("penaltyFreeze"));
                int feeFreeze = (int) (result.get("feeFreeze") == null ? 0 : result.get("feeFreeze"));

                totalPaidAmt += (interest + principal + penalty + fee);

                SubjectRepayDetail detail = new SubjectRepayDetail();
                detail.setChannel(Credit.SOURCE_CHANNEL_SUBJECT);
                detail.setScheduleId(scheduleId);
                detail.setSubjectId(subjectId);
                detail.setUserId(userId);
                detail.setUserIdXm(userIdXm);
                detail.setPrincipal(principal);
                detail.setInterest(interest);
                detail.setPenalty(penalty);
                detail.setFee(fee);
                detail.setFreezePrincipal(principalFreeze);
                detail.setFreezeInterest(interestFreeze);
                detail.setFreezePenalty(penaltyFreeze);
                detail.setFreezeFee(feeFreeze);
                detail.setFreezeRequestNo((String) result.get("investRequestNo"));
                detail.setStatus(SubjectRepayDetail.STATUS_PENDING);
                detail.setSourceAccountId((Integer) result.get("sourceAccountId"));
                detail.setCommission(0);

                list.add(detail);
            }
        }
        if (iplanDetails != null) {
            for (Map.Entry<String, Map<String, Object>> entry : iplanDetails.entrySet()) {
                String userIdAndIPlanId = entry.getKey();
                String userId = userIdAndIPlanId.split("_")[0];
                Map<String, Object> result = entry.getValue();
                String userIdXm = (String) result.get("userIdXm");
                int interest = (int) (result.get("interest") == null ? 0 : result.get("interest"));
                int principal = (int) (result.get("principal") == null ? 0 : result.get("principal"));
                int penalty = (int) (result.get("penalty") == null ? 0 : result.get("penalty"));
                int fee = (int) (result.get("fee") == null ? 0 : result.get("fee"));
                int interestFreeze = (int) (result.get("interestFreeze") == null ? 0 : result.get("interestFreeze"));
                int principalFreeze = (int) (result.get("principalFreeze") == null ? 0 : result.get("principalFreeze"));
                int penaltyFreeze = (int) (result.get("penaltyFreeze") == null ? 0 : result.get("penaltyFreeze"));
                int feeFreeze = (int) (result.get("feeFreeze") == null ? 0 : result.get("feeFreeze"));

                totalPaidAmt += (interest + principal + penalty + fee);

                SubjectRepayDetail detail = new SubjectRepayDetail();
                detail.setChannel(Credit.SOURCE_CHANNEL_IPLAN);
                detail.setScheduleId(scheduleId);
                detail.setSubjectId(subjectId);
                detail.setUserId(userId);
                detail.setUserIdXm(userIdXm);
                detail.setPrincipal(principal);
                detail.setInterest(interest);
                detail.setPenalty(penalty);
                detail.setFee(fee);
                detail.setFreezePrincipal(principalFreeze);
                detail.setFreezeInterest(interestFreeze);
                detail.setFreezePenalty(penaltyFreeze);
                detail.setFreezeFee(feeFreeze);
                detail.setFreezeRequestNo((String) result.get("investRequestNo"));
                detail.setStatus(SubjectRepayDetail.STATUS_PENDING);
                detail.setSourceAccountId((Integer) result.get("sourceAccountId"));
                detail.setCommission(0);

                list.add(detail);
            }
        }

        if (lplanDetails != null) {
            for (Map.Entry<String, Map<String, Object>> entry : lplanDetails.entrySet()) {
                String userId = entry.getKey();
                Map<String, Object> result = entry.getValue();
                String userIdXm = (String) result.get("userIdXm");
                int interest = (int) (result.get("interest") == null ? 0 : result.get("interest"));
                int principal = (int) (result.get("principal") == null ? 0 : result.get("principal"));
                int penalty = (int) (result.get("penalty") == null ? 0 : result.get("penalty"));
                int fee = (int) (result.get("fee") == null ? 0 : result.get("fee"));
                int interestFreeze = (int) (result.get("interestFreeze") == null ? 0 : result.get("interestFreeze"));
                int principalFreeze = (int) (result.get("principalFreeze") == null ? 0 : result.get("principalFreeze"));
                int penaltyFreeze = (int) (result.get("penaltyFreeze") == null ? 0 : result.get("penaltyFreeze"));
                int feeFreeze = (int) (result.get("feeFreeze") == null ? 0 : result.get("feeFreeze"));

                totalPaidAmt += (interest + principal + penalty + fee);

                SubjectRepayDetail detail = new SubjectRepayDetail();
                detail.setChannel(Credit.SOURCE_CHANNEL_LPLAN);
                detail.setScheduleId(scheduleId);
                detail.setSubjectId(subjectId);
                detail.setUserId(userId);
                detail.setUserIdXm(userIdXm);
                detail.setPrincipal(principal);
                detail.setInterest(interest);
                detail.setPenalty(penalty);
                detail.setFee(fee);
                detail.setFreezePrincipal(principalFreeze);
                detail.setFreezeInterest(interestFreeze);
                detail.setFreezePenalty(penaltyFreeze);
                detail.setFreezeFee(feeFreeze);
                detail.setFreezeRequestNo((String) result.get("investRequestNo"));
                detail.setStatus(SubjectRepayDetail.STATUS_PENDING);
                detail.setSourceAccountId((Integer) result.get("sourceAccountId"));
                detail.setCommission(0);

                list.add(detail);
            }
        }
        if (list.size() > 0) {
            int duePrincipal = borrowerDetails.get("duePrincipal");
            int dueInterest = borrowerDetails.get("dueInterest");
            int duePenalty = borrowerDetails.get("duePenalty");
            int dueFee = borrowerDetails.get("dueFee");
            int commission = duePrincipal + dueInterest + duePenalty + dueFee - totalPaidAmt;
            //将佣金记在第一笔还款上
            list.get(0).setCommission(commission);
            logger.info("subject {} schedule {} repay detail: borrower paid {}, investor paid {}, commission {}",
                    subjectId, scheduleId, duePrincipal + dueInterest + duePenalty + dueFee, totalPaidAmt, commission);
            List<SubjectRepayDetail> listToSave = new ArrayList<>();
            for (SubjectRepayDetail repayDetail : list) {
                int interest = repayDetail.getInterest() == null ? 0 : repayDetail.getInterest();
                int principal = repayDetail.getPrincipal() == null ? 0 : repayDetail.getPrincipal();
                int penalty = repayDetail.getPenalty() == null ? 0 : repayDetail.getPenalty();
                int fee = repayDetail.getFee() == null ? 0 : repayDetail.getFee();
                int cm = repayDetail.getCommission() == null ? 0 : repayDetail.getCommission();
                if (interest + principal + penalty + fee + cm > 0) {
                    listToSave.add(repayDetail);
                }
            }
            for (SubjectRepayDetail subjectRepayDetail : listToSave) {
                subjectRepayDetail.setCreateTime(DateUtil.getCurrentDateTime19());
                repayDetailDao.insert(subjectRepayDetail);
            }
        }
    }

    /**
     * @param extSn 交易流水号
     * @param extStatus 交易状态
     * @return
     */
    public String getRequestNo(String extSn,Integer extStatus){
        String requestNo = null;
        if (extStatus != null && extStatus.equals(BaseResponse.STATUS_PENDING)) {
            //状态处理中，同样的流水号再发送一次
            requestNo = extSn;
        }
        return requestNo;
    }

    /**
     * 是否直贷
     * @param flag
     * @return
     */
    public boolean isDirect(Integer flag){
        return Subject.DIRECT_FLAG_YES.equals(flag)?true:false;
    }
    @ProductSlave
    public Subject findSubjectBySubjectId(String subjectId){
        return subjectDao.findBySubjectId(subjectId);
    };
    @ProductSlave
    public BorrowInfo getBorrowerInfo(String subjectId, int creditId) {
        Subject subject = subjectDao.findBySubjectId(subjectId);
        if (subject == null) {
            throw new ProcessException(Error.NDR_0403);
        }
        Integer assetsSource = subject.getAssetsSource();
        String contractNo = subject.getContractNo();
        BorrowInfo borrowInfo = new BorrowInfo();
        BorrowInfo temp = null;
        String type=subject.getType();
        if (StringUtils.hasText(contractNo) || assetsSource != null) {
            if("06".equals(type)){//企业贷
       		 	temp = subjectDao.findCompanyBorrowerInfoBySub(contractNo);
           	}else{
           		switch (assetsSource) {
           		case 1://农贷主标
           			temp = subjectDao.findAgroBorrowerInfo(contractNo);
           			break;
           		case 2://农贷子标
           			temp = subjectDao.findAgroBorrowerInfoBySub(contractNo);
           			break;
           		case 3://车贷主标
           			temp = subjectDao.findVehicleBorrowerInfo(contractNo);
           			break;
           		default://车贷子标
           			temp = subjectDao.findVehicleBorrowerInfoBySub(contractNo);
           			break;
           		}
           	}
        } else {
            //查老活期宝demand_treasure_loan
            temp = subjectDao.findOldTTZBorrowerInfo(subject.getName());
        }
        
        if (temp != null) {
            borrowInfo = temp;
        }
        borrowInfo.setLoanName(subject.getName());
        borrowInfo.setRepayType(subject.getRepayType());
        borrowInfo.setMonth(String.valueOf(subject.getTerm()));
        if (creditId > 0) {
            Credit credit = creditService.getById(creditId);
            if (credit != null) {
                borrowInfo.setCreditId(String.valueOf(creditId));
                String contractId = credit.getContractId();
                if (org.apache.commons.lang3.StringUtils.isNotBlank(contractId)) {
                    String viewPdfUrl = creditService.getContractViewPdfUrlByContractId(contractId);
                    borrowInfo.setViewPdfUrl(viewPdfUrl);
                }
            }
        }

        //返回借款人披露信息
        List<SubjectRepayScheduleQuery> list = subjectDao.findLoanInfoBySubjectId(subjectId);
        borrowInfo.setBorrowerInformations(list);
        borrowInfo.setContractNo(contractNo);
        boolean timeFlag = false;
        if ("2018-04-01 00:00:00".compareTo(subject.getCreateTime()) < 0) {
            timeFlag = true;
        }
        borrowInfo.setTimeFlag(timeFlag);
        return borrowInfo;
    }

    @ProductSlave
    public List<SubjectDto> getSubjectInIplan(String iPlanCode) {
        List<SubjectDto> subjectDtos = new ArrayList<>();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(iPlanCode)) {
            IPlan iPlan = iPlanService.getByCode(iPlanCode);
            if (iPlan != null && iPlan.getId() != null) {
                if (iPlan.getStatus().equals(IPlan.STATUS_NOT_OPEN)
                        || iPlan.getStatus().equals(IPlan.STATUS_ANNOUNCING)
                        || iPlan.getStatus().equals(IPlan.STATUS_RAISING)
                        || iPlan.getStatus().equals(IPlan.STATUS_RAISING_FINISH)) {
                    subjectDtos = subjectDao.getSubjectInIplan();
                    Random random = new Random(iPlan.getId());
                    Collections.shuffle(subjectDtos, random);
                } else {
                    List<Credit> credits = creditDao.getAllCreditByIPlanId(iPlan.getId());
                    Map<String, List<Credit>> subjectMap = credits.stream().collect(Collectors.groupingBy(o -> o.getSubjectId()));
                    for (Map.Entry<String, List<Credit>> entry : subjectMap.entrySet()) {
                        String subjectId = entry.getKey();
                        List<Credit> creditList = entry.getValue();
                        Subject subject = subjectDao.findBySubjectId(subjectId);
                        String realname = "";
                        String idCard = "";
                        double amount = 0;
                        if (subject != null) {
                            String userId = "";
                            if (subject.getDirectFlag() == 0) {
                                userId = subject.getIntermediatorId();
                            } else {
                                userId = subject.getBorrowerId();
                            }
                            User user = userService.getUserById(userId);
                            if (user != null) {
                                realname = user.getRealname();
                                idCard = user.getIdCard();
                            }
                        }
                        amount = creditList.stream().mapToDouble(value -> value.getHoldingPrincipal()/100.0).sum();
                        SubjectDto subjectDto = new SubjectDto();
                        subjectDto.setBorrowName(DealUtil.dealRealname(realname));
                        subjectDto.setIdCard(DealUtil.dealIdCard(idCard));
                        subjectDto.setSubjectId(subjectId);
                        subjectDto.setAmount(df4.format(amount));
                        subjectDtos.add(subjectDto);
                    }
                    if (subjectDtos != null && subjectDtos.size() > 100) {
                        subjectDtos = subjectDtos.subList(0, 100);
                    }
                }
            }
        }
        return subjectDtos;
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(String.valueOf(i));
        }
        System.out.println(Arrays.toString(list.toArray()));
        Random random = new Random(100);
        Collections.shuffle(list, random);
        System.out.println(Arrays.toString(list.toArray()));
    }
    public Map<String, String> getSubjectRiskGradeBySubjectId(String subjectId) {
        Map<String, String> result = new HashMap<>();
        if (org.apache.commons.lang3.StringUtils.isBlank(subjectId)) {
            return result;
        }
        BorrowInfo borrowInfo = getBorrowerInfo(subjectId, 0);
        if (borrowInfo == null) {
            return result;
        }
        String borrowType = borrowInfo.getBorrowType() != null ? borrowInfo.getBorrowType() : "";
        switch (borrowType) {
            case BorrowInfo.BORROW_TYPE_PLEDGE:
                borrowType = "车辆质押";
                break;
            case BorrowInfo.BORROW_TYPE_MORTGAGE:
            case BorrowInfo.BORROW_TYPE_GPS_FULL:
            case BorrowInfo.BORROW_TYPE_GPS_DIVIDE:
                borrowType = "车辆抵押";
                break;
            case BorrowInfo.BORROW_TYPE_CREDIT:
                borrowType = "信用贷款";
                break;
            default:
                borrowType = "信用贷款";
        }
        int score = 0;
        double loanAmt = borrowInfo.getLoanAmt() != null ? borrowInfo.getLoanAmt() : 0;
        if (loanAmt <= 50000) {
            score += 15;
        } else if (loanAmt <= 100000) {
            score += 14;
        } else if (loanAmt <= 200000) {
            score += 12;
        } else {
            score += 10;
        }
        int loanTerm = Integer.valueOf(borrowInfo.getLoanTerm() != null ? borrowInfo.getLoanTerm() : "1");
        if (loanTerm <= 3) {
            score += 15;
        } else if (loanTerm <= 6) {
            score += 14;
        } else if (loanTerm <= 12) {
            score += 13;
        } else if (loanTerm <= 24) {
            score += 12;
        } else {
            score += 10;
        }
        if ("车辆质押".equals(borrowType)) {
            score += 20;
        } else if ("车辆抵押".equals(borrowType)) {
            score += 15;
        } else {
            score += 10;
        }
        String repayType = borrowInfo.getRepayType() != null ? borrowInfo.getRepayType() : "";
        if ("等额本息".equals(repayType)) {
            score += 20;
        } else if ("按月付息到期还本".equals(repayType)) {
            score += 10;
        } else {
            score += 15;
        }
        if (borrowInfo.isMarried()) {
            score += 15;
        } else {
            score += 10;
        }
        Integer age = borrowInfo.getAge() != null ? borrowInfo.getAge() : 0;
        if (age <= 22) {
            score += 10;
        } else if (age <= 50) {
            score += 15;
        } else {
            score += 12;
        }
        String level = "";
        String msg = "";
        if (60 <= score && score <= 80) {
            level = "AAAA";
            msg = "借款资料详尽，借款人还款意愿与还款能力高，项目风险可控。存在到期后借款人无力偿还本息、抵质押物贬值、抵质押物处置周期过长、资产收购不成功而导致的无法及时还款风险。";
        } else if (80 <= score && score <= 100) {
            level = "AAAAA";
            msg = "借款资料较为详尽，借款人较有还款意愿，还款能力较好，项目风险基本可控。存在到期后借款人无力偿还本息、抵质押物贬值、抵质押物处置周期过长、资产收购不成功而导致的无法及时还款风险。";
        }
        result.put("level", level);
        result.put("msg", msg);
        result.put("borrowType", borrowType);
        return result;
    }
    /**
     * subject是否可投
     */
    public Boolean subjectInvestable(Subject subject, int autoInvest) {
        if (subject != null) {
            if (!subject.getPushStatus().equals(Subject.PUSH_XM_HAS_BEEN)) {
                return false;
            }
            if (!subject.getOpenFlag().equals(Subject.FLAG_OPENED)) {
                return false;
            }
            //不是自动投标只有募集中才可以投资
            if (autoInvest == 0 ) {
                if (subject.getRaiseStatus().equals(Subject.RAISE_ING)) {
                    return true;
                }
            }
            if (autoInvest == 1) {
                if (subject.getRaiseStatus().equals(Subject.RAISE_ANNOUNCING) || subject.getRaiseStatus().equals(Subject.RAISE_ING)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 查询新散标列表-包括新手（app和pc）
     * @param type
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ProductSlave
    public List<Subject> findSubjectNewBie(String type, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return subjectDao.findSubjectNewBie(type);
    }
    /**
     * 查询新散标列表-包括新手（app和pc）
     * @param type
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ProductSlave
    public List<Subject> findSubjectNewBieAll(String type, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        //散标列表所有数据（新手标募集中，预告中，正常标募集中预告中，标完成中）
        List<Subject> allList = new ArrayList<>();
        List<Subject> newbieList = subjectDao.findSubjectNewBie1(type,null,null);
        List<Subject> generalList = subjectDao.findSubjectGeneral(type,null,null);
        List<Subject> finishList = subjectDao.findSubjectFinish(type,null,null);
        int newbieSize =newbieList!=null?newbieList.size():0;
        int generalSize = generalList !=null?generalList.size():0;
        int finishSize =finishList !=null?finishList.size():0;
        if(newbieSize>0) {
            allList.addAll(newbieList);
        }
        if(generalSize>0){
            allList.addAll(generalList);
        }
        if(finishSize>0) {
            allList.addAll(finishList);
        }
        return allList;
    }

    
    /**
     * 查询新散标列表-不包括新手（app和pc）
     * @param type
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ProductSlave
    public List<Subject> findSubjectNOAnyNewBie(String type, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        //散标列表所有数据（新手标募集中，预告中，正常标募集中预告中，标完成中）
        List<Subject> allList = new ArrayList<>();
        List<Subject> generalList = subjectDao.findSubjectGeneral(type,null,null);
        List<Subject> finishNoAnyNewBieList = subjectDao.findSubjectFinishNoAnyNewBie(type,null,null);
        int generalSize = generalList !=null?generalList.size():0;
        int finishNoAnyNewBieSize =finishNoAnyNewBieList !=null?finishNoAnyNewBieList.size():0;
        if(generalSize>0){
            allList.addAll(generalList);
        }
        if(finishNoAnyNewBieSize>0) {
            allList.addAll(finishNoAnyNewBieList);
        }
        return allList;
    }
    
    /**
     * 查询普通标
     * @param type
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ProductSlave
    public List<Subject> findSubjectGeneraAll(String type, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<Subject> generalList = subjectDao.findSubjectGeneral(type,null,null);
        return generalList;
    }

    /**
     * 查询已经完成的
     * @param type
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ProductSlave
    public List<Subject> findSubjectFinishAll(String type, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<Subject> finishList = subjectDao.findSubjectFinish(type,null,null);
        return finishList;
    }

    @ProductSlave
    public List<Subject> findSubjectAppNewBieAll(String type, Integer pageNo, Integer pageSize,Integer minMonth,Integer maxMonth) {
        PageHelper.startPage(pageNo, pageSize);
        List<Subject> allList = new ArrayList<>();
        List<Subject> newbieList = subjectDao.findSubjectNewBie1(type,minMonth,maxMonth);
        List<Subject> generalList = subjectDao.findSubjectGeneral(type,minMonth,maxMonth);
        List<Subject> finishList = subjectDao.findSubjectFinish(type,minMonth,maxMonth);
        allList.addAll(newbieList);
        allList.addAll(generalList);
        allList.addAll(finishList);
        return allList;
    }
    public List<Subject> findSubjectNoNewBieAll(String type, Integer pageNo, Integer pageSize,Integer minMonth,Integer maxMonth) {
        PageHelper.startPage(pageNo, pageSize);
        List<Subject> allList = new ArrayList<>();
        List<Subject> generalList = subjectDao.findSubjectGeneral(type,minMonth,maxMonth);
        List<Subject> finishList = subjectDao.findSubjectFinish(type,minMonth,maxMonth);
        allList.addAll(generalList);
        allList.addAll(finishList);
        return allList;
    }

    /**
     * 散标投资达人
     */
    @ProductSlave
    @PutRedis(key = GlobalConfig.SUJECT_TALENT,fieldKey = "#subjectId",expire = 600)
    public List<SubjectInvestorDto> getInvestorAcct(String subjectId) {
        List<SubjectInvestorDto> subjectInvestorDtos = new ArrayList<>();
        Subject subject = subjectDao.findBySubjectId(subjectId);
        //if(!Subject.OPEN_CHANNEL_SUBJECT.equals(subject.getOpenChannel())){
            List<SubjectTransLog>  logs = creditDao.findLogBySubjectId(subjectId);
            for (SubjectTransLog subjectTransLog : logs) {
                SubjectInvestorDto subjectInvestorDto = new SubjectInvestorDto();
                User investor = userService.findByUsername(subjectTransLog.getUserId());
                String realName = investor.getRealname();
                String fristName = realName.substring(0,1);
                String sex = this.getGender(investor.getIdCard());
                if ("男".equals(sex)){
                    realName = fristName + "先生";
                }else if ("女".equals(sex)){
                    realName = fristName + "女士";
                }

                subjectInvestorDto.setUserName(realName);
                subjectInvestorDto.setAmount(subjectTransLog.getTransAmt());
                subjectInvestorDto.setInvestTime(subjectTransLog.getCreateTime());
                subjectInvestorDto.setTransDevice(subjectTransLog.getTransDevice());
                subjectInvestorDto.setInvestWay(subjectTransLog.getAutoInvest());
                subjectInvestorDtos.add(subjectInvestorDto);
            }
       /* }else{
            List<SubjectTransLog> subjectTransLogs = subjcetTransLogService.getBySubjectIdAndTransStatusAndTransTypeIn(subjectId, "0", "0,1");
            for (SubjectTransLog subjectTransLog : subjectTransLogs) {
                SubjectInvestorDto subjectInvestorDto = new SubjectInvestorDto();
                User investor = userService.findByUsername(subjectTransLog.getUserId());
                String realName = investor.getRealname();
                String fristName = realName.substring(0,1);
                String sex = this.getGender(investor.getIdCard());
                if ("男".equals(sex)){
                    realName = fristName + "先生";
                }else if ("女".equals(sex)){
                    realName = fristName + "女士";
                }

                subjectInvestorDto.setUserName(realName);
                subjectInvestorDto.setAmount(subjectTransLog.getTransAmt());
                subjectInvestorDto.setInvestTime(subjectTransLog.getTransTime());
                subjectInvestorDto.setTransDevice(subjectTransLog.getTransDevice());
                subjectInvestorDto.setInvestWay(subjectTransLog.getAutoInvest());
                subjectInvestorDtos.add(subjectInvestorDto);
            }
        }*/
        return subjectInvestorDtos.stream().sorted(Comparator.comparing(SubjectInvestorDto::getInvestTime).reversed()).collect(Collectors.toList());//按时间倒序
    }

    /**
     * 通过身份证判断性别
     * @param idCard
     * @return
     */
    private String getGender(String idCard) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(idCard)) {
            if (idCard.length() == 18 || idCard.length() == 15) {
                // 0是男
                String sex = idCard.substring(idCard.length() - 2,
                        idCard.length() - 1);
                int se = Integer.parseInt(sex);
                if (se % 2 == 0) {
                    return "女";
                } else {
                    // 男
                    return "男";
                }
            }
        }
        return null;
    }

    public String getRepayType(String subjectId){
        Subject subject = this.getBySubjectId(subjectId);
        //回款方式
        if ("MCEI".equals(subject.getRepayType())) {
           return "等额本息";
        } else if ("IFPA".equals(subject.getRepayType())) {
           return "按月付息到期还本";
        } else if ("OTRP".equals(subject.getRepayType())) {
           return "一次性到期还本付息";
        }
        return "还款方式不存在";
    }
    public String getSubjectRepayType(String subjectId){
        Subject subject = this.getBySubjectId(subjectId);
        //回款方式
        if ("MCEI".equals(subject.getRepayType())) {
            return "等额本息";
        } else if ("IFPA".equals(subject.getRepayType())) {
            return "按月付息到期还本";
        } else if ("OTRP".equals(subject.getRepayType())) {
            return "一次性到期还本付息";
        }
        return "还款方式不存在";
    }

    //债权转让 前置条件判断
    public Integer checkCondition(Subject subject,SubjectAccount subjectAccount){
        if (!SubjectAccount.STATUS_PROCEEDS.equals(subjectAccount.getStatus())){//非收益中不能转让
            return 0;
        }
        if(subject.getPeriod() < 30){
            return 0;
        }
        SubjectTransferParam subjectTransferParam = subjectTransferParamService.getByTransferParamCode(subject.getTransferParamCode());
        if (!Subject.RAISE_PAID.equals(subject.getRaiseStatus())){//标的未放款,不可债转
            return 0;
        }
        if (Subject.REPAY_OVERDUE.equals(subject.getRepayStatus())){//标的还款逾期,不可债转
            return 0;
        }
        if (Subject.REPAY_PAYOFF.equals(subject.getRepayStatus())){//标的到期已结束,不可债转
            return 0;
        }
        if (Subject.REPAY_ADVANCED_PAYOFF.equals(subject.getRepayStatus())){//标的提前结清,不可债转
            return 0;
        }
        //购买的债权未放款或者撤消,不可转让
        if(subjectAccount.getAccountSource().equals(SubjectAccount.SOURCE_CREDIT)){
            Credit credit = creditService.getBySubjectAccountId(subjectAccount.getId());
            if(credit == null){
                return 0;
            }
            CreditOpening creditOpening = creditOpeningService.getById(credit.getTargetId());
            if (CreditOpening.STATUS_OPENING.equals(creditOpening.getStatus()) || CreditOpening.STATUS_FINISH.equals(creditOpening.getStatus()) || CreditOpening.STATUS_PENDING.equals(creditOpening.getStatus()) ){
                return 0;
            }
        }
        //之前的转让结果未知,不可转让
        List<SubjectTransLog> subjectTransLog = subjcetTransLogService.getByAccountIdAndType(subjectAccount.getId());
        if (subjectTransLog != null && subjectTransLog.size() > 0){
            for (SubjectTransLog transLog : subjectTransLog) {
                CreditOpening creditOpening = creditOpeningService.getBySourceChannelIdAndOpenChannel(transLog.getId(), CreditOpening.OPEN_CHANNEL);
                if(creditOpening != null && BaseResponse.STATUS_PENDING.equals(creditOpening.getExtStatus())){
                    return 0;
                }
            }
        }


        //放款日N天不在规定天数,不可转让
        String dateNow = DateUtil.getCurrentDateShort();
        String lendTime = subject.getLendTime().substring(0,8);
        long subjectHoldingDays = DateUtil.betweenDays(lendTime,dateNow);
        if(!"jMVfayj22m22oqah".equals(subjectAccount.getUserId())){
            if (subjectHoldingDays < subjectTransferParam.getFullInitiateTransfer()){
                return 0;
            }
        }
        //还款日N天前,不可转让
        SubjectRepaySchedule schedule = subjectRepayScheduleService.getCurrentRepaySchedule(subject.getSubjectId());
        if(schedule!=null){
            long repayDays = DateUtil.betweenDays(dateNow,schedule.getDueDate());
            if (repayDays <= subjectTransferParam.getRepayInitiateTransfer()){
                return 0;
            }
        }else{
            return 0;
        }

        String userId = subjectAccount.getUserId();
        UserOtherInfo userOtherInfo = userOtherInfoDao.getUserById(userId);
        if(userOtherInfo != null){
            List<String> sources = transferConfigDao.getTransferConfig();
            for (String source : sources) {
                if(userOtherInfo.getUserSource().contains(source)){
                    TransferConfig config = transferConfigDao.getConfigBySource(source);
                    String time = config.getTime();
                    Credit credit = null;
                    if(!TransferConfig.CREDIT_STOP.equals(config.getFlag())){
                        credit = creditDao.findBySubjectAccountIdAndTarget(subjectAccount.getId(), time);
                    }else{
                        credit = creditDao.findBySubjectAccountIdAndTime(subjectAccount.getId(), time);
                    }
                    if (credit != null) {
                        return 0;
                    }
                }
            }
        }
        return  1;
    }

    //债权转让 前置条件判断
    public String checkConditionStr(Subject subject,SubjectAccount subjectAccount){
        if (!SubjectAccount.STATUS_PROCEEDS.equals(subjectAccount.getStatus())){//非收益中不能转让
            return "项目还在撮合中,暂时不能债转哦";
        }
        if(subject.getPeriod() < 30){
            return "期限小于30天项目，无法申请转让哦";
        }
        SubjectTransferParam subjectTransferParam = subjectTransferParamService.getByTransferParamCode(subject.getTransferParamCode());
        if (!Subject.RAISE_PAID.equals(subject.getRaiseStatus())){//标的未放款,不可债转
            return "项目还在撮合中,暂时不能债转哦";
        }
        if (Subject.REPAY_OVERDUE.equals(subject.getRepayStatus())){//标的还款逾期,不可债转
            return "项目还款逾期,暂时不能债转哦";
        }
        if (Subject.REPAY_PAYOFF.equals(subject.getRepayStatus())){//标的到期已结束,不可债转
            return "项目已结束,不能债转哦";
        }
        if (Subject.REPAY_ADVANCED_PAYOFF.equals(subject.getRepayStatus())){//标的提前结清,不可债转
            return "项目已提前还款,不能债转哦";
        }
        //购买的债权未放款或者撤消,不可转让
        if(subjectAccount.getAccountSource().equals(SubjectAccount.SOURCE_CREDIT)){
            Credit credit = creditService.getBySubjectAccountId(subjectAccount.getId());
            if(credit == null){
                return "项目还在撮合中,暂时不能债转哦";
            }
            CreditOpening creditOpening = creditOpeningService.getById(credit.getTargetId());
            if (CreditOpening.STATUS_OPENING.equals(creditOpening.getStatus()) || CreditOpening.STATUS_FINISH.equals(creditOpening.getStatus()) || CreditOpening.STATUS_PENDING.equals(creditOpening.getStatus()) ){
                return "项目还在撮合中,暂时不能债转哦";
            }
        }
        //之前的转让结果未知,不可转让
        List<SubjectTransLog> subjectTransLog = subjcetTransLogService.getByAccountIdAndType(subjectAccount.getId());
        if (subjectTransLog != null && subjectTransLog.size() > 0){
            for (SubjectTransLog transLog : subjectTransLog) {
                CreditOpening creditOpening = creditOpeningService.getBySourceChannelIdAndOpenChannel(transLog.getId(), CreditOpening.OPEN_CHANNEL);
                if(creditOpening != null && BaseResponse.STATUS_PENDING.equals(creditOpening.getExtStatus())){
                    return "项目还在撮合中,暂时不能债转哦";
                }
            }
        }


        //放款日N天不在规定天数,不可转让
        String dateNow = DateUtil.getCurrentDateShort();
        String lendTime = subject.getLendTime().substring(0,8);
        long subjectHoldingDays = DateUtil.betweenDays(lendTime,dateNow);
        if(!"jMVfayj22m22oqah".equals(subjectAccount.getUserId())){
            if (subjectHoldingDays < subjectTransferParam.getFullInitiateTransfer()){
                return "项目放款后"+subjectTransferParam.getFullInitiateTransfer()+"天,才能转让哦";
            }
        }
        //还款日N天前,不可转让
        SubjectRepaySchedule schedule = subjectRepayScheduleService.getCurrentRepaySchedule(subject.getSubjectId());
        if(schedule!=null){
            long repayDays = DateUtil.betweenDays(dateNow,schedule.getDueDate());
            if (repayDays <= subjectTransferParam.getRepayInitiateTransfer()){
                return "还款前"+subjectTransferParam.getRepayInitiateTransfer()+"天,不能转让哦";
            }
        }else{
            return "项目还在撮合中,暂时不能债转哦";
        }

        String userId = subjectAccount.getUserId();
        UserOtherInfo userOtherInfo = userOtherInfoDao.getUserById(userId);
        if(userOtherInfo != null) {
            List<String> sources = transferConfigDao.getTransferConfig();
            for (String source : sources) {
                if(userOtherInfo.getUserSource().contains(source)){
                    TransferConfig config = transferConfigDao.getConfigBySource(source);
                    String time = config.getTime();
                    Credit credit = null;
                    if(!TransferConfig.CREDIT_STOP.equals(config.getFlag())){
                        credit = creditDao.findBySubjectAccountIdAndTarget(subjectAccount.getId(), time);
                    }else{
                        credit = creditDao.findBySubjectAccountIdAndTime(subjectAccount.getId(), time);
                    }
                    if (credit != null) {
                        return GlobalConfig.TRANSFER_INFER;
                    }
                }
            }
        }
        return  "可以转让";
    }


    /**
     * 查询农贷借款信息
     */
    @ProductSlave
    public List<AgricultureLoanInfo> getAgricultureLoanInformation(String contractNo) {
        if (StringUtils.isEmpty(contractNo)) {
            throw new IllegalArgumentException("合同号不能为空");
        }
        return subjectDao.findAgricultureLoanInformation(contractNo);
    }

    /**
     * 查询车贷借款信息
     * @param contractNo
     * @return
     */
    @ProductSlave
    public List<LoanIntermediaries> getVehicleLoanInformation(String contractNo) {
        if (StringUtils.isEmpty(contractNo)) {
            throw new IllegalArgumentException("合同号不能为空");
        }
        return subjectDao.findVehicleLoanInformation(contractNo);
    }

    /**
     * 查询车辆图片
     * @param subjectId
     * @return
     */
    @ProductSlave
   public List<VehicleInfoPic> getVehicleInfoPic(String subjectId) {
       if (StringUtils.isEmpty(subjectId)) {
           throw new IllegalArgumentException("项目编号不能为空");
       }
       return subjectDao.findVehicleInfoPic(subjectId);
   }

   //债转发起页
   public AppCreditTransferDto creditTransfer(Integer subjectAccountId,String userId){
       logger.info("开始调用债权转让发起接口->输入参数:账户ID={}",
               subjectAccountId);
       SubjectAccount subjectAccount = subjectAccountService.findAccountById(subjectAccountId);
       if (subjectAccount == null) {
           throw new ProcessException(Error.NDR_0202);
       }

       //标的
       Subject subject = this.getBySubjectId(subjectAccount.getSubjectId());
       AppCreditTransferDto appCreditTransferDto = new AppCreditTransferDto();
       appCreditTransferDto.setHoldingPrincipal(subjectAccount.getCurrentPrincipal()/100.0);
       appCreditTransferDto.setHoldingPrincipalStr(df4.format(subjectAccount.getCurrentPrincipal()/100.0));
       //系统规定最低转让金额
       SubjectTransferParam transferParamCode = subjectTransferParamService.getByTransferParamCode(subject.getTransferParamCode());
       Double transferAmt = transferParamCode.getTransferPrincipalMin()/100.0;
       appCreditTransferDto.setTransferAmt(transferAmt);
       appCreditTransferDto.setTransferAmtStr("最低转让金额"+df4.format(appCreditTransferDto.getTransferAmt())+"元");
       if(appCreditTransferDto.getHoldingPrincipal() <= transferAmt){
           appCreditTransferDto.setTransferAmt(appCreditTransferDto.getHoldingPrincipal());
           appCreditTransferDto.setTransferAmtStr(appCreditTransferDto.getHoldingPrincipal()+"元需要全部转出");
       }

       //转让服务费率
       Double feeRate = subjectAccountService.calcTransFeeNew(subjectAccount.getTransLogId(), subject, transferParamCode);
       appCreditTransferDto.setFeeRate(feeRate / 100.0);
       appCreditTransferDto.setFeeRateStr(df5.format(feeRate / 100.0));

       //只有买的标的才有红包
       Credit credit = creditService.getBySubjectAccountId(subjectAccount.getId());
       Double redFee = subjectAccountService.calcRedFee(subjectAccount, credit);
       appCreditTransferDto.setRedRate(redFee);
       appCreditTransferDto.setRedRateStr(df5.format(redFee));

       //溢价费率
       appCreditTransferDto.setOverFeeRate(0.2);
       appCreditTransferDto.setOverFeeRateStr(df4.format(0.2));

       //最高最低折让率
       Double discountRateMax = transferParamCode.getDiscountRateMax().multiply(new BigDecimal(100)).doubleValue()/100.0;
       Double discountRateMin = transferParamCode.getDiscountRateMin().multiply(new BigDecimal(100)).doubleValue()/100.0;

       //可用免费转让次数
       appCreditTransferDto.setTimes(subjcetTransLogService.getTimes(userId));
       if(marketService.getVip(userId) > 2){
            appCreditTransferDto.setVip("尊敬的VIP"+marketService.getVip(userId)+"用户，本月您还有"+subjcetTransLogService.getTimes(userId)+"次免转让服务费的机会！");
       }else{
           appCreditTransferDto.setVip("");
       }

       List<Map<String,String>> lists = new ArrayList<>();

       Double step = (discountRateMax - discountRateMin)/10;
       for(int i = 0;i <= 10;i++){
           Map<String,String> map = new HashMap<>();
           map.put("key",df4.format((discountRateMax - step *i) * 100));
           lists.add(map);
       }
       System.out.println(lists);
       appCreditTransferDto.setLists(lists);
       return appCreditTransferDto;
   }

    //债转确认页
    public AppCreditTransferConfirmDto creditTransferConfirm(Map<String,String> map){
        Integer subjectAccountId = 0;
        Double transferAmt = 0.0;
        Integer midValue =0;
        Double transferDiscount = 0.0;
        String userId = null;
        if (map.containsKey("id") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("id"))){
            subjectAccountId = Integer.valueOf(map.get("id"));
        }
        if (map.containsKey("transferAmt") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("transferAmt"))){
            midValue = Integer.valueOf(map.get("transferAmt"));
        }
        if (map.containsKey("transferDiscount") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("transferDiscount"))){
            transferDiscount= Double.valueOf(map.get("transferDiscount"));
        }
        if (map.containsKey("userId") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("userId"))){
            userId = map.get("userId");
        }
        logger.info("开始调用债权转让确认接口->输入参数:账户ID={}",
                subjectAccountId);
        SubjectAccount subjectAccount = subjectAccountService.findAccountById(subjectAccountId);
        if (subjectAccount == null) {
            throw new ProcessException(Error.NDR_0202);
        }

        //标的
        Subject subject = this.getBySubjectId(subjectAccount.getSubjectId());

        //散标交易配置信息
        SubjectTransferParam subjectTransferParam = subjectTransferParamService.getByTransferParamCode(subject.getTransferParamCode());

        subjectAccountService.checkCondition(subject,subjectAccount,subjectTransferParam,new BigDecimal(transferDiscount).divide(new BigDecimal(100),3, RoundingMode.HALF_UP),midValue);
        transferAmt = midValue /100.0;
        AppCreditTransferConfirmDto appCreditTransferConfirmDto = new AppCreditTransferConfirmDto();
        appCreditTransferConfirmDto.setId(subjectAccountId);
        //转让金额
        appCreditTransferConfirmDto.setTransferAmt(transferAmt);
        appCreditTransferConfirmDto.setTransferAmtStr(df4.format(transferAmt));

        //扣除红包奖励
        Credit credit = creditService.getBySubjectAccountId(subjectAccount.getId());
        Double redFee = subjectAccountService.calcRedFee(subjectAccount, credit);
        appCreditTransferConfirmDto.setRedFee(transferAmt * redFee);
        appCreditTransferConfirmDto.setRedFeeStr(df4.format(appCreditTransferConfirmDto.getRedFee() ));


        //折让率
        appCreditTransferConfirmDto.setTransferDiscount(transferDiscount);
        appCreditTransferConfirmDto.setTransferDiscountStr(df4.format(transferDiscount / 1.0));

        //溢价手续费
        Double overFee = 0.0;
        if(transferDiscount > 100){
            overFee = transferAmt * (transferDiscount - 100) / 100.0 * 0.2;
        }
        appCreditTransferConfirmDto.setOverFee(overFee);
        appCreditTransferConfirmDto.setOverFeeStr(df4.format(overFee));

        //服务费
        Integer times = subjectTransLogService.getTimes(userId);
        if(times > 0){
            appCreditTransferConfirmDto.setFee(0.0);
            appCreditTransferConfirmDto.setFeeStr(df4.format(0.0));
        }else{
            Double feeRate = subjectAccountService.calcTransFeeNew(subjectAccount.getTransLogId(), subject, subjectTransferParam);
            appCreditTransferConfirmDto.setFee(transferAmt * feeRate / 100.0);
            appCreditTransferConfirmDto.setFeeStr(df4.format(transferAmt * feeRate / 100.0));
        }
        //预期到账收益
        double expectAmt = ArithUtil.calcExp(transferAmt * (transferDiscount / 100.0), appCreditTransferConfirmDto.getRedFee(), appCreditTransferConfirmDto.getOverFee(),appCreditTransferConfirmDto.getFee());
        appCreditTransferConfirmDto.setExpectAmt(expectAmt);
        appCreditTransferConfirmDto.setExpectAmtStr(df4.format(expectAmt));

        return appCreditTransferConfirmDto;
    }

    //债转实际调用接口
    public AppCreditTransferSuccessDto creditTransferFinsh(Map<String,String> map){
        Integer accountId = 0;
        Integer transferAmt = 0;
        Double transferDiscount = 0.0;
        String userId = null;
        String device = null;
        Integer flag = 0;
        if (map.containsKey("id") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("id"))){
            accountId = Integer.valueOf(map.get("id"));
        }
        if (map.containsKey("flag") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("flag"))){
            flag = Integer.valueOf(map.get("flag"));
        }
        if (map.containsKey("transferAmt") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("transferAmt"))){
            transferAmt = Integer.valueOf(map.get("transferAmt"));
        }
        if (map.containsKey("transferDiscount") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("transferDiscount"))){
            transferDiscount= Double.valueOf(map.get("transferDiscount"));
        }
        if (map.containsKey("userId") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("userId"))){
            userId = map.get("userId");
        }
        if (map.containsKey("requestSource") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("requestSource"))){
            device = map.get("requestSource");
        }
        logger.info("开始调用债权转让调用厦门接口->输入参数:账户ID={},时间{}",
                accountId,new Date());
        if(flag == 0){
            if (redisLock.getDLock(SCREDITTRANSFER + accountId, String.valueOf(accountId))){
                try {
                    SubjectAccount subjectAccount = subjectAccountService.findAccountById(accountId);
                    if (subjectAccount == null) {
                        throw new ProcessException(Error.NDR_0202);
                    }
                    if(!subjectAccount.getUserId().equals(userId)){
                        throw new ProcessException(Error.NDR_0202);
                    }
                    Integer subjectTransLogId = subjectAccount.getTransLogId();
                    subjectAccountService.subjectCreditTransfer(subjectTransLogId,transferAmt,new BigDecimal(transferDiscount).divide(new BigDecimal(100),3,RoundingMode.HALF_UP),device);
                }finally {
                    redisLock.releaseDLock(SCREDITTRANSFER + accountId, String.valueOf(accountId));
                }
            }
        }else{
            if (redisLock.getDLock(YJTCREDITTRANSFER + accountId, String.valueOf(accountId))){
                try {
                    IPlanAccount iPlanAccount = iPlanAccountDao.findByIdAndTypeForUpdate(accountId);
                    if (iPlanAccount == null) {
                        throw new ProcessException(Error.NDR_0202);
                    }
                    if(!iPlanAccount.getUserId().equals(userId)){
                        throw new ProcessException(Error.NDR_0202);
                    }
                    iPlanAccountService.yjtCreditTransfer(accountId,transferAmt,new BigDecimal(transferDiscount).divide(new BigDecimal(100),3,RoundingMode.HALF_UP),device);
                }finally {
                    redisLock.releaseDLock(SCREDITTRANSFER + accountId, String.valueOf(accountId));
                }
            }
        }

        return new AppCreditTransferSuccessDto("申请转让成功!","正在努力转出中,请您及时关注App以及到账短信通知");
    }

    /**
     * 根据还款类型计算预期收益
     * @param contractAmt
     * @param investRate
     * @param rate
     * @param termMonth
     * @param period
     * @param repayType
     * @return
     */
    public double getInterestByRepayType(int contractAmt,BigDecimal investRate,BigDecimal rate,int termMonth,
                                         int period,String repayType){
        double totalRepayInterest=0.0;
        if(investRate.compareTo(BigDecimal.ZERO) == 0 || rate.compareTo(BigDecimal.ZERO) == 0 ){
            return totalRepayInterest;
        }
        FinanceCalcUtils.CalcResult calcResult = null;
        if (Subject.REPAY_TYPE_OTRP.equals(repayType)&& period<=30) {//针对一期的标都使用一次还本付息拆标
            calcResult = FinanceCalcUtils.calcOTRPSubject(contractAmt, investRate, period);
            totalRepayInterest= ArithUtil.roundDown(calcResult.getTotalRepayInterest()/100.0, 2);
        } else if (Subject.REPAY_TYPE_MCEI.equals(repayType)) {
            calcResult = FinanceCalcUtils.calcMCEISubject(contractAmt, rate, termMonth);
            for (int m = 1; m <= termMonth; m++) {
                //FinanceCalcUtils.CalcResult.Detail detail = calcResult.getDetails().get(m);
                double interest = 0.0;
                if (m == 1) {
                    interest =  ArithUtil.roundDown(contractAmt * investRate.doubleValue() / 12/100.0, 2);
                } else {
                    interest = ArithUtil.roundDown(calcResult.getDetails().get(m - 1).getRemainPrincipal() * investRate.doubleValue() / 12/100.0, 2);
                }
                totalRepayInterest += interest;
            }

        } else if (Subject.REPAY_TYPE_IFPA.equals(repayType)) {
            calcResult = FinanceCalcUtils.calcIFPASubject(contractAmt, investRate, termMonth);
            totalRepayInterest= ArithUtil.roundDown(calcResult.getTotalRepayInterest()/100.0, 2);
        } else if (Subject.REPAY_TYPE_MCEP.equals(repayType)) {
            calcResult = FinanceCalcUtils.calcMCEPSubject(contractAmt, investRate, termMonth);
            totalRepayInterest= ArithUtil.roundDown(calcResult.getTotalRepayInterest()/100.0, 2);
        } else {
            throw new IllegalArgumentException("不支持的还款类型！");
        }
        System.out.println("++++++"+totalRepayInterest);
        totalRepayInterest = ArithUtil.roundDown(totalRepayInterest, 2);
       return totalRepayInterest;

    }

    public double getInterestByRepayTypeNew(int contractAmt,BigDecimal investRate,BigDecimal rate,int termMonth,
                                         int period,String repayType){
        double totalRepayInterest=0.0;
        if(investRate.compareTo(BigDecimal.ZERO) == 0 || rate.compareTo(BigDecimal.ZERO) == 0 ){
            return totalRepayInterest;
        }
        FinanceCalcUtils.CalcResult calcResult = null;
        if (Subject.REPAY_TYPE_OTRP.equals(repayType)&& period<=30) {//针对一期的标都使用一次还本付息拆标
            calcResult = FinanceCalcUtils.calcOTRP(contractAmt, investRate, period);
            totalRepayInterest=calcResult.getTotalRepayInterest();
        } else if (Subject.REPAY_TYPE_MCEI.equals(repayType)) {
            calcResult = FinanceCalcUtils.calcMCEI(contractAmt, rate, termMonth);
            for (int m = 1; m <= termMonth; m++) {
                //FinanceCalcUtils.CalcResult.Detail detail = calcResult.getDetails().get(m);
                double interest = 0.0;
                if (m == 1) {
                    interest = contractAmt * investRate.doubleValue() / 12;
                } else {
                    interest = calcResult.getDetails().get(m - 1).getRemainPrincipal() * investRate.doubleValue() / 12;
                }
                totalRepayInterest += interest;
            }

        } else if (Subject.REPAY_TYPE_IFPA.equals(repayType)) {
            calcResult = FinanceCalcUtils.calcIFPA(contractAmt, investRate, termMonth);
            totalRepayInterest=calcResult.getTotalRepayInterest();
        } else if (Subject.REPAY_TYPE_MCEP.equals(repayType)) {
            calcResult = FinanceCalcUtils.calcMCEP(contractAmt, investRate, termMonth);
            totalRepayInterest=calcResult.getTotalRepayInterest();
        } else {
            throw new IllegalArgumentException("不支持的还款类型！");
        }
        System.out.println("++++++"+totalRepayInterest);
        totalRepayInterest = ArithUtil.round(totalRepayInterest/100.0, 2);
        return totalRepayInterest;

    }

    public List<Subject> getByIplanId(Integer iPlanId) {
        if (iPlanId == null || iPlanId == 0) {
            return null;
        }
        return subjectDao.getSubjectByIplanId(iPlanId);
    }
}
