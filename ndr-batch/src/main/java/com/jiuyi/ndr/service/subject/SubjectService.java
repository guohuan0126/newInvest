package com.jiuyi.ndr.service.subject;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.account.PlatformTransferDao;
import com.jiuyi.ndr.dao.config.AutoMatchNewIplanConfigDao;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.lplan.LPlanAccountDao;
import com.jiuyi.ndr.dao.subject.*;
import com.jiuyi.ndr.domain.account.CompensatoryAcctLog;
import com.jiuyi.ndr.domain.account.PlatformTransfer;
import com.jiuyi.ndr.domain.config.AutoMatchNewIplanConfig;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.subject.*;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.account.CompensatoryAcctLogService;
import com.jiuyi.ndr.service.account.PlatformAccountService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.redpacket.RedpacketService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import com.jiuyi.ndr.util.redis.RedisClient;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestModifyProject;
import com.jiuyi.ndr.xm.http.request.RequestSingleTrans;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.query.TransactionQueryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 标的服务
 * Created by lixiaolei on 2017/4/11.
 */
@Service
public class SubjectService {

    private final static Logger logger = LoggerFactory.getLogger(SubjectService.class);

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private CreditDao creditDao;

    @Autowired
    private SubjectInvestParamService subjectInvestParamService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private PlatformAccountService platformAccountService;

    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;

    @Autowired
    private IPlanAccountDao iPlanAccountDao;

    @Autowired
    private LPlanAccountDao lPlanAccountDao;

    @Autowired
    private IPlanDao iPlanDao;

    @Autowired
    private PlatformTransferDao  platformTransferDao;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private SubjectRepayScheduleDao subjectRepayScheduleDao;

    @Autowired
    private CompensatoryAcctLogService compensatoryAcctLogService;
    @Autowired
    private SubjectAccountDao subjectAccountDao;
    @Autowired
    private SubjectTransLogDao subjectTransLogDao;

    @Autowired
    private RedpacketService redpacketService;
    @Autowired
    private SubjectAccountService subjectAccountService;

    @Autowired
    private AutoMatchNewIplanConfigDao autoMatchNewIplanConfigDao;

    @Autowired
    private CashLoanNoticeDao cashLoanNoticeDao;
    @Autowired
    private SubjectSendSmsDao subjectSendSmsDao;
    @Autowired
    private SubjectRepayBillService subjectRepayBillService;


    /**
     * 投标
     */
    @Transactional
    public Subject tender(Map<String, String> conditions) {

        String dateTimeNow = DateUtil.getCurrentDateTime();

        String subjectId = conditions.get("subjectId");
        String investorId = conditions.get("investorId");
        String investorIdXm = conditions.get("investorIdXm");
        String iplanAccountId = conditions.get("iplanAccountId");
        Integer investAmt = Integer.valueOf(conditions.get("investAmt"));
        int investChannel = Integer.valueOf(conditions.getOrDefault("investChannel", "2"));
        int investChannelId = Integer.valueOf(conditions.get("investChannelId"));
        Integer deductAmt = Integer.valueOf(conditions.get("deductAmt"));

        if (Credit.SOURCE_CHANNEL_LPLAN != investChannel && Credit.SOURCE_CHANNEL_IPLAN != investChannel && Credit.SOURCE_CHANNEL_SUBJECT != investChannel&& Credit.SOURCE_CHANNEL_YJT != investChannel) {
            throw new IllegalArgumentException("没有该投资渠道");
        }

        Subject subject = subjectDao.findBySubjectIdForUpdate(subjectId);

        if (Subject.FLAG_CLOSED.equals(subject.getOpenFlag()) || !Subject.RAISE_ING.equals(subject.getRaiseStatus())) {
            //不能投
            logger.warn("标的[{}]未开放或已募满，不能投此标", subjectId);
            throw new ProcessException(Error.NDR_0401.getCode(), Error.NDR_0401.getMessage() + ":subjectId=" + subjectId);
        }

        if (Arrays.asList(1, 3, 5, 7).stream().anyMatch(i -> i.equals(subject.getOpenChannel()))
                && investChannel == Credit.SOURCE_CHANNEL_SUBJECT) {//只作散标卖
            SubjectInvestParamDef subjectInvestParamDef = subjectInvestParamService.getInvestParamDef(subject.getInvestParam());
            int minAmt = subjectInvestParamDef.getMinAmt() == null ? 0 : subjectInvestParamDef.getMinAmt();
            int maxAmt = subjectInvestParamDef.getMaxAmt() == null ? Integer.MAX_VALUE : subjectInvestParamDef.getMaxAmt();
            int incrementAmt = subjectInvestParamDef.getIncrementAmt() == null ? 1 : subjectInvestParamDef.getIncrementAmt();
            if (minAmt > investAmt) {
                logger.warn("标的[{}]，用户[{}]起投金额不足，起投金额[{}], 用户金额[{}]", subjectId, investorId, minAmt, investAmt);
                throw new ProcessException(Error.NDR_0404.getCode(), Error.NDR_0404.getMessage()
                        + ":subjectId=" + subjectId + ", investorId=" + investorId + ", minAmt=" + minAmt + ", investAmt=" + investAmt);
            }
            if (maxAmt < investAmt) {
                logger.warn("标的[{}]，用户[{}]起投金额超限，限定金额[{}], 用户金额[{}]", subjectId, investorId, maxAmt, investAmt);
                throw new ProcessException(Error.NDR_0405.getCode(), Error.NDR_0405.getMessage()
                        + ":subjectId=" + subjectId + ", investorId=" + investorId + ", maxAmt=" + maxAmt + ", investAmt=" + investAmt);
            }
            if ((investAmt - subjectInvestParamDef.getMinAmt()) % incrementAmt != 0) {
                logger.warn("标的[{}]，用户[{}]投资金额未按规则递增，递增梯度[{}], 用户金额[{}]", subjectId, investorId, incrementAmt, investAmt);
                throw new ProcessException(Error.NDR_0406.getCode(), Error.NDR_0406.getMessage()
                        + ":subjectId=" + subjectId + ", investorId=" + investorId + ", incrementAmt=" + incrementAmt + ", investAmt=" + investAmt);
            }
        }

        if (subject.getAvailableAmt() < investAmt) {
            logger.warn("标的[{}]，用户[{}]投资金额大于标的可投金额，标的可投金额[{}], 用户金额[{}]", subjectId, investorId, subject.getAvailableAmt(), investAmt);
            throw new ProcessException(Error.NDR_0410.getCode(), Error.NDR_0410.getMessage()
                    + ":subjectId=" + subjectId + ", investorId=" + investorId + ", availableAmt=" + subject.getAvailableAmt() + ", investAmt=" + investAmt);
        }

        Integer availableAmt = subject.getAvailableAmt() - investAmt;

        if (availableAmt == 0) {//成标
            subject.setRaiseStatus(Subject.RAISE_FINISHED);
            subject.setCloseTime(dateTimeNow);
        }
        subject.setAvailableAmt(availableAmt);//最新的可投金额
        this.update(subject);

        Credit credit = new Credit();
        credit.setSubjectId(subjectId);
        credit.setUserId(investorId);
        credit.setUserIdXM(investorIdXm);
        credit.setInitPrincipal(investAmt);
        credit.setHoldingPrincipal(investAmt);
        credit.setMarketingAmt(deductAmt);
        credit.setResidualTerm(subject.getTerm());
        credit.setStartTime(dateTimeNow);
        credit.setCreditStatus(Credit.CREDIT_STATUS_WAIT);
        if(iplanAccountId!=null){
            credit.setSourceAccountId(Integer.parseInt(iplanAccountId));
        }
        credit.setSourceChannel(investChannel);
        credit.setSourceChannelId(investChannelId);
        credit.setTarget(Credit.TARGET_SUBJECT);
        credit.setTargetId(subject.getId());
        credit.setCreateTime(DateUtil.getCurrentDateTime19());

        creditDao.insert(credit);

        return subject;
    }


    /**
     * 更新标的
     */
    public Subject update(Subject subject) {
        if (subject.getSubjectId() == null) {
            throw new IllegalArgumentException("更新标的时，标的id不能为空");
        }
        subjectDao.update(subject);
        return subject;
    }
    /**
     * 放款
     */
    @Transactional(noRollbackFor = ProcessException.class)
    public Subject lend(String subjectId) {

        String dateTimeNow = DateUtil.getCurrentDateTime();

        Subject subject = subjectDao.findBySubjectIdForUpdate(subjectId);

        if (!Subject.FLAG_OPENED.equals(subject.getOpenFlag()) || !Subject.RAISE_FINISHED.equals(subject.getRaiseStatus()) || Subject.RAISE_PAID.equals(subject.getRaiseStatus())) {
            //不能放款
            logger.warn("标的[{}]未开放或未募满或已放款，不能放款", subjectId);
            throw new ProcessException(Error.NDR_0402.getCode(), Error.NDR_0402.getMessage() + ":subjectId=" + subjectId);
        }
        if(subject.getOpenChannel() == 1){
            List<SubjectTransLog> transLogs = subjectTransLogDao.findBySubjectIdAndConfirmStatus(subjectId);
            if(transLogs != null && transLogs.size() > 0){
                for (SubjectTransLog transLog : transLogs) {
                    subjectAccountService.subjectRechargeAndInvestCancel(transLog.getId());
                }
            }
        }
        //查询此标的形成的所有债权
        List<Credit> allCredits = creditDao.findBySubjectIdByStatus(subjectId);
        Integer totalAmt = 0;
        for (Credit credit : allCredits){
            totalAmt += credit.getHoldingPrincipal();
        }
        if (!totalAmt .equals(subject.getTotalAmt())){
            logger.warn("标的[{}]对应的债权未全部形成，不能放款", subjectId);
            return subject;
            //throw new ProcessException(Error.NDR_0602.getCode(), Error.NDR_0602.getMessage() + ":subjectId=" + subjectId);
        }
        //查询债权记录
        List<Credit> credits = creditDao.findBySubjectId(subjectId);

        //按每99个一组分割
        List<List<Credit>> parts = Lists.partition(credits, 99);

        subject = creditLend(subject,parts);

        String requestNo = subject.getExtSn();

        //散标 生成补息和加息券奖励金额
        if (subject.getOpenChannel() == 1){
            redpacketService.createSubjectPacketInvest(subject);
        }
        //一键投生成补息
        if (subject.getOpenChannel()==8){
            try {
                redpacketService.createSubjectPacketInvestByYjt(subject);
            }catch (Exception e){
                logger.info("省心投异常{}",e.getMessage());
            }
        }

        List<Credit> creditList = creditDao.findAllCreditBySubjectId(subjectId);
        if (creditList.stream().allMatch(credit -> credit.getCreditStatus().equals(Credit.CREDIT_STATUS_HOLDING))){
            //更新短融本地居间人&借款人账户
            //如果是直贷模式
            if (subject.getDirectFlag().equals(Subject.DIRECT_FLAG_YES)) {
                userAccountService.transferIn(subject.getBorrowerIdXM(), subject.getTotalAmt() / 100.0, BusinessEnum.ndr_subject_lend,
                        "标的放款资金转入："+subject.getName(), "标的放款资金转入,标的名称：" + subject.getName()+"，本金："+subject.getTotalAmt() / 100.0, requestNo,subjectId,null);
                userAccountService.transferOut(subject.getBorrowerIdXM(), subject.getTotalAmt() / 100.0, BusinessEnum.ndr_subject_lend,
                        "标的放款资金转出给居间人："+subject.getName(), "标的放款资金转出给居间人,标的名称：" + subject.getName()+"，本金："+subject.getTotalAmt() / 100.0, requestNo,subjectId,null);
                userAccountService.transferIn(subject.getIntermediatorIdXM(), subject.getTotalAmt() / 100.0, BusinessEnum.ndr_subject_lend,
                        "项目放款："+subject.getName(), "标的放款资金转入标的名称：" + subject.getName()+"，本金："+subject.getTotalAmt() / 100.0, requestNo,subjectId,null);

            }else if (Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag())){//直贷二期
                double money = (subject.getTotalAmt()-subject.getFeeAmt()-subject.getMiscellaneousAmt()) / 100.0;
                userAccountService.transferIn(subject.getBorrowerIdXM(), subject.getTotalAmt() / 100.0, BusinessEnum.ndr_subject_lend,
                        "标的放款资金转入："+subject.getName(), "标的放款资金转入,标的名称：" + subject.getName()+"，本金："+ subject.getTotalAmt() / 100.0, requestNo,subjectId,null);
                if(subject.getFeeAmt() + subject.getMiscellaneousAmt() > 0){
                    userAccountService.transferOut(subject.getBorrowerIdXM(), (subject.getFeeAmt() + subject.getMiscellaneousAmt()) / 100.0, BusinessEnum.ndr_subject_lend_profit,
                            "标的放款,服务费和杂费转入给分润账户："+subject.getName(), "标的放款,服务费和杂费转入给分润账户,标的名称：" + subject.getName()+"，服务费和杂费：   +"+(subject.getFeeAmt() + subject.getMiscellaneousAmt()) / 100.0, requestNo,subjectId,null);
                    userAccountService.transferIn(subject.getProfitAccount(), (subject.getFeeAmt() + subject.getMiscellaneousAmt()) / 100.0, BusinessEnum.ndr_subject_lend_profit,
                        "标的放款,服务费和杂费转入给分润账户："+subject.getName(), "标的放款,服务费和杂费转入给分润账户,标的名称：" + subject.getName()+"，服务费和杂费："+(subject.getFeeAmt() + subject.getMiscellaneousAmt()) / 100.0, requestNo,subjectId);
                }
                if (subject.getReloanSubjectId() != null){//如果是续贷标
                    //查询是否是续贷结清
                    SubjectRepaySchedule subjectRepaySchedule = subjectRepayScheduleDao.findByScheduleByStatusAndCpsStatus(subject.getReloanSubjectId());
                    if(subjectRepaySchedule == null){
                        List<SubjectRepayBill> bills = subjectRepayBillService.getBySubjectIdAndTypeAndStatus(subject.getReloanSubjectId(),SubjectRepayBill.TYPE_DELAY_PAYOFF,SubjectRepayBill.STATUS_REPAY);
                        if(!CollectionUtils.isEmpty(bills)){
                            subjectRepaySchedule = subjectRepayScheduleService.getById(bills.get(0).getScheduleId());
                        }
                    }
                    if (subjectRepaySchedule != null){//已代偿、未还清,代偿金账户还的款
                        if (money > 0){
                            userAccountService.transferOut(subject.getBorrowerIdXM(), money, BusinessEnum.ndr_subject_lend,
                                    "续贷标,标的放款借款人资金转出给代偿金账户："+subject.getName(), "标的放款借款人资金转出给代偿金账户,标的名称：" + subject.getName()+"，本金："+money, requestNo);
//                            platformAccountService.transferIn(subject.getCompensationAccount(), money, BusinessEnum.ndr_subject_lend,
//                                    "续贷标,标的放款资金转入给代偿金账户,标的名称："+subject.getName()+"金额:"+money, requestNo);
                            userAccountService.transferIn(subject.getCompensationAccount(), money, BusinessEnum.ndr_subject_lend, "续贷标,标的放款借款人资金转出给代偿金账户：" + subject.getName(), "标的放款借款人资金转出给代偿金账户,标的名称：" + subject.getName()+"，本金："+money , requestNo,subjectId);
                            //代偿金账户流水
                            compensatoryAcctLogService.logLend(subjectRepaySchedule.getId(),subjectRepaySchedule.getSubjectId(),subjectRepaySchedule.getTerm(),
                                    0,subject.getCompensationAccount(),(subject.getTotalAmt()-subject.getFeeAmt()-subject.getMiscellaneousAmt()),
                                    requestNo,subject.getExtStatus(), CompensatoryAcctLog.TYPE_CPS_IN);
                        }

                    //判断代偿金是否已还完
                    CompensatoryAcctLog cpsAcctLogs = compensatoryAcctLogService.getCpsAcctLogsByScheduleIdAndType(subjectRepaySchedule.getId(), CompensatoryAcctLog.TYPE_CPS_OUT);
                    if (cpsAcctLogs != null){
                        if (money == cpsAcctLogs.getAmount()/100.0){
                            subjectRepaySchedule.setCpsStatus(2);
                            subjectRepayScheduleDao.update(subjectRepaySchedule);
                         }
                      }
                    }
                }
            } else {
                userAccountService.transferIn(subject.getIntermediatorIdXM(), subject.getTotalAmt() / 100.0, BusinessEnum.ndr_subject_lend,
                        "项目放款："+subject.getName(), "项目名称："+subject.getName()+"，本金："+subject.getTotalAmt() / 100.0 , requestNo,subjectId,null);
            }
            //形成还款计划
            List<SubjectRepaySchedule> schedules = null;
            schedules = subjectRepayScheduleService.makeUpRepaySchedule(subject);
            /*if(!Subject.SUBJECT_TYPE_CARD.equals(subject.getType())){
            }else{
                schedules = subjectRepayScheduleService.makeUpCreditCardRepaySchedule(subject);
            }*/
            String dueDate = subjectRepayScheduleService.findRepaySchedule(subject.getSubjectId(), subject.getTerm()).getDueDate();
            LocalDate localDate = DateUtil.parseDate(dueDate, DateUtil.DATE_TIME_FORMATTER_8);
            subject.setRepayTime(DateUtil.getDateStr(localDate, DateUtil.DATE_TIME_FORMATTER_10)+" 00:00:00");

            for (Credit credit : creditList) {
                credit.setEndTime(subjectRepayScheduleService.findRepaySchedule(subjectId, subject.getTerm()).getDueDate() + " 23:59:59");
                credit.setUpdateTime(DateUtil.getCurrentDateTime19());
                creditDao.update(credit);
            }
            //能贷,房贷,直贷二期标的放款加入消息队列
            if (!Subject.SUBJECT_TYPE_COMPANY.equals(subject.getType())){
                Map<String, String> map = new HashMap<>();
                map.put("message", subjectId);
                String json = JSON.toJSONString(map);
                redisClient.product("CASH_SUBJECT_LEND",json);
                CashLoanNotice cashLoanNotice = new CashLoanNotice();
                cashLoanNotice.setRequestNo(subject.getExtSn());
                cashLoanNotice.setSubjectId(subjectId);
                cashLoanNotice.setBusinessType("loan");
                cashLoanNotice.setReqData(json);
                cashLoanNotice.setCreateTime(DateUtil.getCurrentDateTime19());
                cashLoanNotice.setStep(1);
                cashLoanNotice.setCompanySign("jiuyi");
                cashLoanNoticeDao.insert(cashLoanNotice);

            }
            BaseResponse response = this.changeSubjectXM(subject, Subject.SUBJECT_STATUS_REPAY_NORMAL_XM,schedules);
            if (!BaseResponse.STATUS_SUCCEED.equals(response.getStatus())){
                noticeService.sendEmail("标的放款变更标的状态异常","标的号："+subjectId+",存管返回报文："+response.toString(),"zhangjunying@duanrong.com");
            }

            //更新标的状态
            subject.setRaiseStatus(Subject.RAISE_PAID);
            subject.setLendTime(dateTimeNow);
        }
        this.update(subject);
        return subject;
    }

    /**
     * 厦门银行放款
     */
    private BaseResponse lend(Subject subject, List<Credit> credits) {
        RequestSingleTrans request = new RequestSingleTrans();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setTradeType(TradeType.TENDER);
        request.setProjectNo(subject.getSubjectId().toString());
        request.setTransCode(TransCode.SUBJECT_LEND.getCode());

        boolean isDirect = (!Subject.DIRECT_FLAG_NO.equals(subject.getDirectFlag())) ? true : false;//是否直贷

        List<RequestSingleTrans.Detail> details = new ArrayList<>();
        Integer profitAmt = 0 ;
        for (Credit credit : credits) {
            profitAmt += credit.getHoldingPrincipal();
            RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
            Integer marketingAmt = credit.getMarketingAmt();
            detail.setBizType(BizType.TENDER);
            String freezeRequestNo = null;
            //如果是定期投资产生的债权，冻结流水号取定期账户上的投资流水号
            if (credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_IPLAN)||credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_YJT)) {
                freezeRequestNo = iPlanAccountDao.findById(credit.getSourceAccountId()).getInvestRequestNo();
                detail.setFreezeRequestNo(freezeRequestNo);
            }
            if (credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_LPLAN)) {
                freezeRequestNo = lPlanAccountDao.findByUserId(credit.getUserId()).getInvestRequestNo();
                detail.setFreezeRequestNo(freezeRequestNo);
            }if (credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_SUBJECT)){
                freezeRequestNo = subjectAccountDao.findByTransLogId(credit.getSourceChannelId()).getInvestRequestNo();
                detail.setFreezeRequestNo(freezeRequestNo);
            }

            detail.setSourcePlatformUserNo(credit.getUserIdXM());//出款方用户编号
            detail.setTargetPlatformUserNo(isDirect ? subject.getBorrowerIdXM() : subject.getIntermediatorIdXM());//收款方用户编号
            //TODO 抵扣劵传值
            detail.setAmount((credit.getHoldingPrincipal() - marketingAmt) / 100.0);
            //detail.setShare(credit.getHoldingPrincipal() / 100.0);
            details.add(detail);
            if (marketingAmt > 0){
                RequestSingleTrans.Detail detail1 = new RequestSingleTrans.Detail();
                detail1.setBizType(BizType.MARKETING);
                detail1.setFreezeRequestNo(freezeRequestNo);
                detail1.setSourcePlatformUserNo(GlobalConfig.MARKETING_ACCOUNT_02_DR);//出款方用户编号（真实借款人）
                detail1.setTargetPlatformUserNo(isDirect ? subject.getBorrowerIdXM() : subject.getIntermediatorIdXM());//收款方用户编号（居间人）
                detail1.setAmount(marketingAmt / 100.0);
                details.add(detail1);
            }

        }

        if (isDirect) {//是直贷
            if(Subject.DIRECT_FLAG_YES.equals(subject.getDirectFlag())){//直贷一期
                RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
                detail.setBizType(BizType.PROFIT);
                detail.setSourcePlatformUserNo(subject.getBorrowerIdXM());//出款方用户编号（真实借款人）
                detail.setTargetPlatformUserNo(subject.getIntermediatorIdXM());//收款方用户编号（居间人）
                detail.setAmount(profitAmt / 100.0);
                details.add(detail);
            } else {//直贷二期
                Integer totalFee = subject.getFeeAmt() + subject.getMiscellaneousAmt();
                if(totalFee > 0 && subject.getProfitAmt() < totalFee){//如果服务费杂费和大于零并且已分润金额小于服务费和杂费之和才进行分润
                        RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
                        detail.setBizType(BizType.PROFIT);
                        detail.setSourcePlatformUserNo(subject.getBorrowerIdXM());//出款方用户编号（真实借款人）
                        detail.setTargetPlatformUserNo(subject.getProfitAccount());//收款方用户编号,分润给分润账户
                        if(profitAmt + subject.getProfitAmt() >= totalFee){//放款金额足够,直接分润
                            detail.setAmount((totalFee - subject.getProfitAmt()) / 100.0);
                        }else {
                            detail.setAmount(profitAmt / 100.0);
                        }
                        details.add(detail);
                }

                if (subject.getReloanSubjectId() != null){ //说明是续贷标
                    Integer totalMoney = subject.getTotalAmt() - subject.getFeeAmt() - subject.getMiscellaneousAmt();
                    if(totalMoney > 0 && subject.getReloanProfitAmt() < totalMoney){ //标的金额大于零并且续贷标已分润金额小于标的金额才进行分润
                         SubjectRepaySchedule subjectRepaySchedule = subjectRepayScheduleDao.findByScheduleByStatusAndCpsStatus(subject.getReloanSubjectId());
                            if (subjectRepaySchedule != null){//已代偿、未还清
                                RequestSingleTrans.Detail detail1 = new RequestSingleTrans.Detail();
                                detail1.setBizType(BizType.PROFIT);
                                detail1.setSourcePlatformUserNo(subject.getBorrowerIdXM());//出款方用户编号（真实借款人）
                                detail1.setTargetPlatformUserNo(subject.getCompensationAccount());//收款方用户编号为代偿金账户
                                if (profitAmt + subject.getReloanProfitAmt() >= totalMoney){
                                    detail1.setAmount((totalMoney - subject.getReloanProfitAmt()) / 100.0);
                                }else {
                                    detail1.setAmount(profitAmt / 100.0);
                                }
                                details.add(detail1);
                            }
                    }
                }
            }
        }

        request.setDetails(details);
        return transactionService.singleTrans(request);
    }

    /**
     * 厦门银行更改标的状态
     */
    /**
     * 厦门银行更改标的状态
     */
    public BaseResponse changeSubjectXM(Subject subject , String status,List<SubjectRepaySchedule> schedules) {
        int terms = schedules.size();
        String subjectId = subject.getSubjectId();
        int period = subject.getPeriod();
        RequestModifyProject modifyProject = new RequestModifyProject();
        modifyProject.setRequestNo(IdUtil.getRequestNo());
        modifyProject.setProjectNo(subjectId);
        modifyProject.setStatus(status);
        modifyProject.setRepayInstallment(terms);
        modifyProject.setProjectPeriod(period);
        modifyProject.setTransCode(TransCode.MODIFY_PROJECT.getCode());
        List<RequestModifyProject.Detail> details = new ArrayList<>(terms);
        for (SubjectRepaySchedule repaySchedule:schedules) {
            RequestModifyProject.Detail detail = new RequestModifyProject.Detail();
            detail.setRepayTime(repaySchedule.getDueDate());
            detail.setRepayPrincipal(repaySchedule.getDuePrincipal()/100.0);
            details.add(detail);
        }
        modifyProject.setBizDetails(details);
        return transactionService.modifyProject(modifyProject);
    }

    /**
     * 根据标的id查询标的（加锁）
     */
    public Subject findSubjectBySubjectId(String id) {
        return subjectDao.findBySubjectIdForUpdate(id);
    }

    /**
     * 没加锁
     */
    public Subject findBySubjectId(String subjectId) {
        return subjectDao.findBySubjectId(subjectId);
    }

    private void insertPlatformTransfer(Credit credit,String subjectId){
        Double marketingAmount = credit.getMarketingAmt()/100D;
        PlatformTransfer platformTransfer = new PlatformTransfer();
        platformTransfer.setId(IdUtil.randomUUID());
        platformTransfer.setActualMoney(marketingAmount);
        platformTransfer.setBillType("out");
        platformTransfer.setLoanId(subjectId);
        platformTransfer.setUsername(credit.getUserIdXM());
        platformTransfer.setStatus("平台划款成功");
        if (credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_IPLAN)){
            platformTransfer.setRemarks("creditId:" + credit.getId()
                    + ",iplanTransLogId:" + credit.getSourceChannelId() + ",抵扣劵："
                    + marketingAmount);
        }else{
            platformTransfer.setRemarks("creditId:" + credit.getId()
                    + ",subjectTransLogId:" + credit.getSourceChannelId() + ",抵扣劵："
                    + marketingAmount);
        }
        platformTransfer.setSuccessTime(DateUtil.getCurrentDateTime19());
        platformTransfer.setTime(DateUtil.getCurrentDateTime19());
        platformTransfer.setType("deduct");
        platformTransfer.setOrderId(credit.getSourceChannelId().toString());
        platformTransfer.setPlatformId("11");
        platformTransferDao.insert(platformTransfer);
    }

    public BorrowInfo getBorrowerInfo(String subjectId) {
        Subject subject = subjectDao.findBySubjectId(subjectId);
        if (subject == null) {
            throw new ProcessException(Error.NDR_0403);
        }
        Integer assetsSource = subject.getAssetsSource();
        String contractNo = subject.getContractNo();
        BorrowInfo borrowInfo = null;
        if (StringUtils.hasText(contractNo) || assetsSource != null) {
            switch (assetsSource) {
                case 1://农贷主标
                    borrowInfo = subjectDao.findAgroBorrowerInfo(contractNo);
                    break;
                case 2://农贷子标
                    borrowInfo = subjectDao.findAgroBorrowerInfoBySub(contractNo);
                    break;
                case 3://车贷主标
                    borrowInfo = subjectDao.findVehicleBorrowerInfo(contractNo);
                    break;
                default://车贷子标
                    borrowInfo = subjectDao.findVehicleBorrowerInfoBySub(contractNo);
                    break;
            }
        } else {
            //查老活期宝demand_treasure_loan
            borrowInfo = subjectDao.findOldTTZBorrowerInfo(subject.getName());
        }
        borrowInfo.setLoanName(subject.getName());
        borrowInfo.setRepayType(subject.getRepayType());
        borrowInfo.setMonth(String.valueOf(subject.getTerm()));
        return borrowInfo;
    }

    public List<Subject> findInvestable(Integer openChannel,String[] type){
        int[] factors = new int[]{1,2,3,4,5,6,7};
        Set<Integer> set = new HashSet<>(4);
        for(int factor:factors){
            //找出满足条件的所有组合
            set.add(factor|openChannel);
        }
        return subjectDao.findInvestable(type,set.toArray(new Integer[]{}));
    }

    public List<Subject> findCashInvestable(Integer openChannel){
        return findInvestable(openChannel,new String[]{Subject.SUBJECT_TYPE_CASH});
    }

    public List<Subject> findAgricultureInvestable(Integer openChannel){
        return findInvestable(openChannel,new String[]{Subject.SUBJECT_TYPE_AGRICULTURAL});
    }

    public List<Subject> findCarInvestable(Integer openChannel){
        return findInvestable(openChannel,new String[]{Subject.SUBJECT_TYPE_CASH});
    }

    public List<Subject> findHouseInvestable(Integer openChannel){
        return findInvestable(openChannel,new String[]{Subject.SUBJECT_TYPE_HOUSE});
    }

    /**
     * Reflect Invoke
     * 查询车农贷可投 并按照开放时间和产品类型排序
     * @return
     */
    public List<Subject> findACHInvestableFirstOrderOpenTime(Integer openchannel){
        List<Subject> subjects = findInvestable(openchannel,new String[]{Subject.SUBJECT_TYPE_AGRICULTURAL,Subject.SUBJECT_TYPE_CAR,Subject.SUBJECT_TYPE_HOUSE,Subject.SUBJECT_TYPE_CARD,Subject.SUBJECT_TYPE_COMPANY,Subject.SUBJECT_TYPE_CAR_VICAL});
        Comparator<Subject> comparator = Comparator.comparing(Subject::getOpenTime);
        comparator.thenComparing(SubjectService::sortBySubjectType);
        Collections.sort(subjects,comparator);
        return subjects;
    }

    //Reflect Invoke
    public List<Subject> findACHInvestableLastOrderOpenTime(Integer openchannel){
        List<Subject> subjects = findInvestable(openchannel,new String[]{Subject.SUBJECT_TYPE_AGRICULTURAL,Subject.SUBJECT_TYPE_CAR,Subject.SUBJECT_TYPE_HOUSE,Subject.SUBJECT_TYPE_CARD,Subject.SUBJECT_TYPE_COMPANY,Subject.SUBJECT_TYPE_CAR_VICAL});
        Comparator<Subject> comparator = SubjectService::sortBySubjectType;
        comparator.thenComparing(Subject::getOpenTime);
        Collections.sort(subjects,comparator);
        return subjects;
    }

    private static int sortBySubjectType(Subject s1,Subject s2){
        if(Subject.SUBJECT_TYPE_CAR.equals(s1.getType())&&!Subject.SUBJECT_TYPE_CAR.equals(s2.getType())){
            return -1;
        }else if(!Subject.SUBJECT_TYPE_CAR.equals(s1.getType())&&Subject.SUBJECT_TYPE_CAR.equals(s2.getType())){
            return 1;
        }else if(Subject.SUBJECT_TYPE_HOUSE.equals(s1.getType())&&!Subject.SUBJECT_TYPE_HOUSE.equals(s2.getType())){
            return 1;
        }else if(!Subject.SUBJECT_TYPE_HOUSE.equals(s1.getType())&&Subject.SUBJECT_TYPE_HOUSE.equals(s2.getType())){
            return -1;
        }else{
            return 0;
        }
    }

    public Subject getByContractNo(String contractNo) {
        if (!StringUtils.hasText(contractNo)) {
            throw new IllegalArgumentException("contractNo can not is null or blank");
        }
        return subjectDao.findByContractNo(contractNo);
    }

    /**
     * 查询subject的还款方式
     * @param subject
     * @return
     */
    public String getRepayType(Subject subject){
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
    public double getInterestByRepayType(int contractAmt, BigDecimal investRate, BigDecimal rate, int termMonth,
                                         int period, String repayType){
        double totalRepayInterest=0.0;
        if(investRate.compareTo(BigDecimal.ZERO) == 0 || rate.compareTo(BigDecimal.ZERO) == 0 ){
            return totalRepayInterest;
        }
        FinanceCalcUtils.CalcResult calcResult = null;
        if (Subject.REPAY_TYPE_OTRP.equals(repayType)&& period<=30) {//针对一期的标都使用一次还本付息拆标
            calcResult = FinanceCalcUtils.calcOTRPSubject(contractAmt, investRate, period);
            totalRepayInterest=calcResult.getTotalRepayInterest();
        } else if (Subject.REPAY_TYPE_MCEI.equals(repayType)) {
            calcResult = FinanceCalcUtils.calcMCEISubject(contractAmt, rate, termMonth);
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
            calcResult = FinanceCalcUtils.calcIFPASubject(contractAmt, investRate, termMonth);
            totalRepayInterest=calcResult.getTotalRepayInterest();
        } else if (Subject.REPAY_TYPE_MCEP.equals(repayType)) {
            calcResult = FinanceCalcUtils.calcMCEPSubject(contractAmt, investRate, termMonth);
            totalRepayInterest=calcResult.getTotalRepayInterest();
        } else {
            throw new IllegalArgumentException("不支持的还款类型！");
        }
        totalRepayInterest = ArithUtil.roundDown(totalRepayInterest/100.0, 2);
        return totalRepayInterest;

    }

    /**
     * 债权放款
     * @return
     */
    private Subject creditLend(Subject subject, List<List<Credit>> credits){
        for (List<Credit> sp:credits) {
            if (BaseResponse.STATUS_PENDING.equals(subject.getExtStatus())) {
                RequestSingleTransQuery request = new RequestSingleTransQuery();
                request.setRequestNo(subject.getExtSn());
                request.setTransactionType(TransactionType.TRANSACTION);
                ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
                if (!"0".equals(responseQuery.getCode())) {
                    if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(responseQuery.getCode())) {
                        //订单不存在，设置交易失败，重新发起交易
                        subject.setExtStatus(BaseResponse.STATUS_FAILED);
                        this.update(subject);
                    }
                    break;
                } else {
                    TransactionQueryRecord transactionQueryRecord = (TransactionQueryRecord) responseQuery.getRecords().get(0);
                    if ("SUCCESS".equals(transactionQueryRecord.getStatus())) {
                        subject.setExtStatus(BaseResponse.STATUS_SUCCEED);
                    } else if ("PROCESSING".equals(transactionQueryRecord.getStatus())) {
                        subject.setExtStatus(BaseResponse.STATUS_PENDING);
                        this.update(subject);
                        break;
                    } else {
                        subject.setExtSn(subject.getExtSn());
                        subject.setExtStatus(BaseResponse.STATUS_FAILED);
                        this.update(subject);
                        break;
                    }
                }
            } else {
                BaseResponse response = this.lend(subject, sp);
                if (BaseResponse.STATUS_FAILED.equals(response.getStatus())) {//放款失败
                    subject.setExtSn(response.getRequestNo());
                    subject.setExtStatus(response.getStatus());
                    this.update(subject);
                    break;
                }
                if (BaseResponse.STATUS_PENDING.equals(response.getStatus())) {//处理中
                    subject.setExtSn(response.getRequestNo());
                    subject.setExtStatus(response.getStatus());
                    this.update(subject);
                    break;
                }
                subject.setExtSn(response.getRequestNo());
                subject.setExtStatus(BaseResponse.STATUS_SUCCEED);
            }
                if (Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag()) && BaseResponse.STATUS_SUCCEED.equals(subject.getExtStatus())){//更新本地直贷二期标的分润相关数据
                    Integer profitAmt = 0; //标的对应债权放款总金额
                    for (Credit credit : sp){
                        profitAmt += credit.getHoldingPrincipal();
                    }
                    //服务费杂费分润
                    Integer totalFee = subject.getFeeAmt() + subject.getMiscellaneousAmt();
                    if(profitAmt + subject.getProfitAmt() >= totalFee){//放款金额足够,直接分润
                        subject.setProfitAmt(subject.getProfitAmt() + totalFee - subject.getProfitAmt());
                    }else {
                        subject.setProfitAmt(subject.getProfitAmt() + profitAmt);
                    }
                    //续贷标分润
                    if (subject.getReloanSubjectId() != null){ //说明是续贷标
                        Integer totalMoney = subject.getTotalAmt() - subject.getFeeAmt() - subject.getMiscellaneousAmt();
                        if(totalMoney > 0 && subject.getReloanProfitAmt() < totalMoney){ //标的金额大于零并且续贷标已分润金额小于标的金额才进行分润
                            SubjectRepaySchedule subjectRepaySchedule = subjectRepayScheduleDao.findByScheduleByStatusAndCpsStatus(subject.getReloanSubjectId());
                            if (subjectRepaySchedule != null){//已代偿、未还清
                                if (profitAmt + subject.getReloanProfitAmt() >= totalMoney){
                                    subject.setReloanProfitAmt(subject.getReloanProfitAmt() + totalMoney - subject.getReloanProfitAmt());
                                }else {
                                    subject.setReloanProfitAmt(subject.getReloanProfitAmt() + profitAmt);
                                }
                            }
                        }
                    }
                }

            //更新债权
            updateCredit(sp,subject);
        }
        return subject;

    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void  updateCredit(List<Credit> credits,Subject subject){
        String subjectId = subject.getSubjectId();
        String requestNo =subject.getExtSn();
        for (Credit credit : credits) {
            //更新短融本地投资人账户
            Integer sourceChannel = credit.getSourceChannel();
            if (sourceChannel==Credit.SOURCE_CHANNEL_IPLAN||sourceChannel==Credit.SOURCE_CHANNEL_YJT){
                IPlanAccount iPlanAccount = iPlanAccountDao.findById(credit.getSourceAccountId());
                IPlan iPlan = iPlanDao.findById(iPlanAccount.getIplanId());
                Integer marketingAmount = credit.getMarketingAmt();
                //增加抵扣劵的入账及冻结
                if (marketingAmount > 0){
                    platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_02_DR, credit.getMarketingAmt() / 100.0, BusinessEnum.ndr_iplan_deduct, "使用抵扣劵，定期账户：" + iPlanAccount.getId(), requestNo,subjectId,null);
                    userAccountService.transferIn(credit.getUserIdXM(), credit.getMarketingAmt() / 100.0, BusinessEnum.ndr_iplan_deduct,
                            "投资"+iPlan.getName()+"使用抵扣劵成功", "投资定期理财计划成功--购买标的:" + subjectId, requestNo,subjectId,null);
                    userAccountService.freeze(credit.getUserIdXM(), credit.getMarketingAmt() / 100.0, BusinessEnum.ndr_iplan_auto_invest,
                            "冻结：投资"+iPlan.getName()+"成功", "投资定期理财计划成功--购买标的:" + subjectId, requestNo,subjectId,null);
                    //保存platformTransfer流水，用于对账
                    insertPlatformTransfer(credit,subjectId);
                }
                userAccountService.tofreeze(credit.getUserIdXM(), credit.getHoldingPrincipal() / 100.0, BusinessEnum.ndr_iplan_auto_invest,
                        "投资"+iPlan.getName()+"成功", "投资定期理财计划成功--购买标的:" + subjectId, requestNo,subjectId,null);
            } else if (sourceChannel==Credit.SOURCE_CHANNEL_LPLAN){
                userAccountService.tofreeze(credit.getUserIdXM(), credit.getInitPrincipal() / 100.0, BusinessEnum.ndr_ttz_auto_invest,
                        "投资天天赚成功:" + subject.getName(), "投资天天赚成功:购买标的:" + subject.getName(), requestNo,subjectId,null);
            } else if (sourceChannel==Credit.SOURCE_CHANNEL_SUBJECT){
                //散标业务
                SubjectAccount subjectAccount = subjectAccountDao.findByTransLogId(credit.getSourceChannelId());
                Integer marketingAmount = credit.getMarketingAmt();
                //增加抵扣劵的入账及冻结
                if (marketingAmount > 0){
                    platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_02_DR, credit.getMarketingAmt() / 100.0, BusinessEnum.ndr_suject_deduct, "使用抵扣劵，散标账户：" + subjectAccount.getId(), requestNo);
                    userAccountService.transferIn(credit.getUserIdXM(), credit.getMarketingAmt() / 100.0, BusinessEnum.ndr_suject_deduct,
                            "投资"+subject.getName()+"使用抵扣劵成功", "投资散标成功--购买标的:" + subjectId, requestNo);
                    userAccountService.freeze(credit.getUserIdXM(), credit.getMarketingAmt() / 100.0, BusinessEnum.ndr_subject_auto_invest,
                            "冻结：投资"+subject.getName()+"成功", "投资散标成功--购买标的:" + subjectId, requestNo);
                    //保存platformTransfer流水，用于对账
                    insertPlatformTransfer(credit,subjectId);
                }
                userAccountService.tofreeze(credit.getUserIdXM(), credit.getHoldingPrincipal() / 100.0, BusinessEnum.ndr_subject_auto_invest,
                        "投资"+subject.getName()+"成功", "投资散标成功--购买标的:" + subjectId, requestNo);
            } else {
                logger.error("债权id：{}，开放渠道：{}",credit.getId(),credit.getCreditStatus());
            }

            //更新债权状态
            credit.setCreditStatus(Credit.CREDIT_STATUS_HOLDING);
            credit.setUpdateTime(DateUtil.getCurrentDateTime19());
            creditDao.update(credit);
        }
    }

    /**
     *1 车直贷二期＞能贷、房贷＞农贷直贷一期（恒丰银行放款）＞车直贷一期＞农贷直贷一期（久亿放款）
     *2 能贷、房贷＞车直贷二期＞农贷直贷一期（恒丰银行放款）＞车直贷一期＞农贷直贷一期（久亿放款）
     *3 能贷＞车直贷二期＞房贷＞农贷直贷一期（恒丰银行放款）＞车直贷一期＞农贷直贷一期（久亿放款）
     * @date 2017/12/27
     * @return
     */
    public List<Subject> findSubjects(int iplanId){
        List<Subject> subjects = subjectDao.findByIplanAndStatus(iplanId,Subject.RAISE_ING);
        //车直贷二期
        List<Subject> car2Subjects = subjects.stream().filter(subject -> ((Subject.SUBJECT_TYPE_CAR.equals(subject.getType())||Subject.SUBJECT_TYPE_CAR_VICAL.equals(subject.getType()))&&Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag()))).collect(Collectors.toList());
        //能贷,卡贷s
        List<Subject> cardSubjects = subjects.stream().filter(subject -> (Subject.SUBJECT_TYPE_CASH.equals(subject.getType())||Subject.SUBJECT_TYPE_CARD.equals(subject.getType()))).collect(Collectors.toList());
        //房贷
        List<Subject> houseSubjects = subjects.stream().filter(subject -> (Subject.SUBJECT_TYPE_HOUSE.equals(subject.getType()))).collect(Collectors.toList());
        //农贷恒丰放款
        List<Subject> hfSubjects = subjects.stream().filter(subject -> (Subject.SUBJECT_TYPE_AGRICULTURAL.equals(subject.getType())&&Subject.LEND_SOURCE_HF.equals(subject.getLendSource()))).collect(Collectors.toList());
        //车贷一期
        List<Subject> car1Subjects = subjects.stream().filter(subject -> (Subject.SUBJECT_TYPE_CAR.equals(subject.getType())&&Subject.DIRECT_FLAG_YES.equals(subject.getDirectFlag()))).collect(Collectors.toList());
        //农贷久亿
        List<Subject> jySubjects = subjects.stream().filter(subject -> (Subject.SUBJECT_TYPE_AGRICULTURAL.equals(subject.getType())&&Subject.LEND_SOURCE_JY.equals(subject.getLendSource()))).collect(Collectors.toList());
        //企业贷
        List<Subject> companySubjects = subjects.stream().filter(subject -> (Subject.SUBJECT_TYPE_COMPANY.equals(subject.getType()))).collect(Collectors.toList());

        List<Subject> matchSubjects = new ArrayList<>(subjects.size());
        AutoMatchNewIplanConfig autoMatchNewIplanConfig = autoMatchNewIplanConfigDao.findByStatus(AutoMatchNewIplanConfig.STATUS_ON);
        int type = autoMatchNewIplanConfig.getType();
        if (type==2){
            matchSubjects.addAll(cardSubjects);
            matchSubjects.addAll(houseSubjects);
            matchSubjects.addAll(car2Subjects);
            matchSubjects.addAll(hfSubjects);
            matchSubjects.addAll(car1Subjects);
            matchSubjects.addAll(companySubjects);

        }else if (type==3){
            matchSubjects.addAll(cardSubjects);
            matchSubjects.addAll(car2Subjects);
            matchSubjects.addAll(houseSubjects);
            matchSubjects.addAll(hfSubjects);
            matchSubjects.addAll(car1Subjects);
            matchSubjects.addAll(jySubjects);
            matchSubjects.addAll(companySubjects);

        } else {
            matchSubjects.addAll(car2Subjects);
            matchSubjects.addAll(cardSubjects);
            matchSubjects.addAll(houseSubjects);
            matchSubjects.addAll(hfSubjects);
            matchSubjects.addAll(car1Subjects);
            matchSubjects.addAll(jySubjects);
            matchSubjects.addAll(companySubjects);

        }
        return matchSubjects;
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


    public Subject getBySubjectId(String subjectId) {
        if (!StringUtils.hasText(subjectId)) {
            throw new IllegalArgumentException("subjectId不能为空");
        }
        return subjectDao.findBySubjectId(subjectId);
    }

    /**
     * 查询农贷中转站利率
     * @param contractId
     * @return
     */
    public BigDecimal findRateByContractNoFromAgricultureLoaninfo(String contractId){
        return this.subjectDao.findRateByContractNoFromAgricultureLoaninfo(contractId);
    }


    /**
     * 获取项目原始利率
     * @param subject
     * @return
     */
    public BigDecimal getOriginalRate(Subject subject){
        boolean newFlag = Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag())&&subject.getInvestRate()!=null && subject.getRate().equals(subject.getInvestRate()) && subject.getRate().compareTo(BigDecimal.valueOf(0.144))<0 && subject.getRate().compareTo(BigDecimal.valueOf(0.068))>0;
        BigDecimal rate = subject.getRate();
        if(newFlag){
            //查询中转站原始利率 计算借款人当期应换本息
            if(Subject.SUBJECT_TYPE_CAR.equals(subject.getType())){
                rate = this.findRateByContractNoFromLoanIntermediaries(subject.getContractNo());
            }else {
                rate = this.findRateByContractNoFromAgricultureLoaninfo(subject.getContractNo());
            }
        }
        return rate;
    }

    /**
     * 查询车贷中转站利率
     * @param contractId
     * @return
     */
    public BigDecimal findRateByContractNoFromLoanIntermediaries(String contractId){
        return this.subjectDao.findRateByContractNoFromLoanIntermediaries(contractId);
    }

    /**
     * 插入短信记录
     * @param userId  用户id
     * @param msg     短信参数
     * @param mobileNumber 手机号
     * @param type      短信模板id
     */
    public void insertMsg(String userId,String msg,String mobileNumber,String type){
        SubjectSendSms sms = new SubjectSendSms();
        sms.setUserId(userId);
        sms.setStatus(SubjectSendSms.HAS_NOT_SEND_MSG);
        sms.setType(type);
        sms.setMobileNumber(mobileNumber);
        sms.setContent(msg);
        sms.setMsg(msg);
        subjectSendSmsDao.insert(sms);
    }
}
