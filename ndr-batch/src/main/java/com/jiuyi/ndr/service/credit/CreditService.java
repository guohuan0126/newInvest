package com.jiuyi.ndr.service.credit;

import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.account.PlatformTransferDao;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.dao.lplan.LPlanAccountDao;
import com.jiuyi.ndr.dao.lplan.LPlanTransLogDao;
import com.jiuyi.ndr.dao.subject.SubjectAccountDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.dao.subject.SubjectTransLogDao;
import com.jiuyi.ndr.domain.account.PlatformTransfer;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.lplan.LPlanAccount;
import com.jiuyi.ndr.domain.lplan.LPlanTransLog;
import com.jiuyi.ndr.domain.subject.*;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.account.PlatformAccountService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.iplan.IPlanTransLogService;
import com.jiuyi.ndr.service.redpacket.RedpacketService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestIntelligentProjectDebentureSale;
import com.jiuyi.ndr.xm.http.request.RequestSingleTrans;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.query.TransactionQueryRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangyibo on 2017/4/10.
 */
@Service
public class CreditService {

    private static final Logger logger = LoggerFactory.getLogger(CreditService.class);
    @Autowired
    private CreditDao creditDao;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private CreditOpeningDao creditOpeningDao;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private IPlanAccountDao iPlanAccountDao;
    @Autowired
    private LPlanAccountDao lPlanAccountDao;
    @Autowired
    private PlatformAccountService platformAccountService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private PlatformTransferDao platformTransferDao;
    @Autowired
    private IPlanDao iPlanDao;
    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;
    @Autowired
    private LPlanTransLogDao lPlanTransLogDao;
    @Autowired
    private SubjectAccountDao subjectAccountDao;
    @Autowired
    private SubjectTransLogDao subjectTransLogDao;
    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private RedpacketService redpacketService;
    @Autowired
    private IPlanTransLogService iPlanTransLogService;
    @Autowired
    private SubjectRepayDetailDao repayDetailDao;
    @Autowired
    private SubjectRepayScheduleService repayScheduleService;
    @Autowired
    private IPlanService iPlanService;
    /**
     * 债权转让
     *
     * @param transferDiscount 折让率(不折让传1)
     * @param sourceChannelId  交易来源ID(转让交易记录ID)
     * @return
     */
    public List<CreditOpening> creditTransfer(Map<Credit, Integer> paramMap, BigDecimal transferDiscount, Integer sourceChannelId,String IntelRequestNo) {
        logger.info("开始调用债权转让方法->输入参数:creditId={},转让份数={},折让率={}", Arrays.toString(paramMap.keySet().stream().map(Credit::getId).toArray())
                , Arrays.toString(paramMap.entrySet().stream().map(Map.Entry::getValue).toArray()), transferDiscount);
        RequestIntelligentProjectDebentureSale request = new RequestIntelligentProjectDebentureSale();
        List<RequestIntelligentProjectDebentureSale.Detail> details = new ArrayList<>(paramMap.size());
        request.setRequestNo(IdUtil.getRequestNo());
        request.setDetails(details);
        request.setTransCode(TransCode.CREDIT_TRANSFER.getCode());
        List<CreditOpening> creditOpenings = new ArrayList<>(paramMap.size());
        for (Map.Entry<Credit, Integer> entry : paramMap.entrySet()) {
            Credit credit = entry.getKey();
            Integer transferPrincipal = entry.getValue();
            List<SubjectRepaySchedule> subjectRepaySchedules = subjectRepayScheduleService.findRepayScheduleBySubjectId(credit.getSubjectId());
            if (subjectRepaySchedules == null || subjectRepaySchedules.isEmpty()) {
                logger.error("查询不到该标的下的还款计划,subjectId=" + credit.getSubjectId());
                throw new ProcessException(Error.NDR_0202.getCode(), "查询不到该标的下的还款计划,subjectId=" + credit.getSubjectId());
            }
            //判断还款计划中是否有逾期 逾期不能进行债转
            if (subjectRepaySchedules.stream().anyMatch(
                    subjectRepaySchedule -> SubjectRepaySchedule.STATUS_OVERDUE.equals(subjectRepaySchedule.getStatus())
            )) {
                logger.warn("要债转的债权中存在逾期标的，不能进行债转");
                throw new ProcessException(Error.NDR_0303.getCode(), Error.NDR_0303.getMessage() + ":creditId=" + credit.getId());
            }
            Subject subject = subjectService.findSubjectBySubjectId(credit.getSubjectId());
            RequestIntelligentProjectDebentureSale.Detail detail = new RequestIntelligentProjectDebentureSale.Detail();
            detail.setSaleRequestNo(IdUtil.getRequestNo());
            detail.setIntelRequestNo(IntelRequestNo);
            detail.setPlatformUserNo(credit.getUserIdXM());
            detail.setProjectNo(String.valueOf(subject.getSubjectId()));
            detail.setSaleShare(transferPrincipal/100.0);
            details.add(detail);
            //保存开放中的债权数据 再进行调用
            CreditOpening creditOpening = saveOpeningCredit(transferDiscount, credit, sourceChannelId, detail.getSaleRequestNo(),transferPrincipal);
            creditOpenings.add(creditOpening);
        }
        //调用厦门银行债权出让接口
        BaseResponse baseResponse = null;
        try {
            //存在转让出的债权为0的情况
            if (details.size() > 0) {
                logger.info("开始调用厦门银行批量债权出让接口->{}", JSON.toJSONString(request));
                baseResponse = transactionService.intelligentProjectDebentureSale(request);
                logger.info("批量债权出让接口返回->{}", JSON.toJSONString(baseResponse));
            } else {
                baseResponse = new BaseResponse();
                baseResponse.setStatus(BaseResponse.STATUS_SUCCEED);//默认为成功
            }
        } catch (Exception e) {
            if (baseResponse == null) {
                baseResponse = new BaseResponse();
            }
            baseResponse.setStatus(BaseResponse.STATUS_PENDING);
        }
        //无论交易结果如何 都更新债权的持有份数 防止转出份额超过剩余份数
        for (Map.Entry<Credit, Integer> entry : paramMap.entrySet()) {
            Credit credit = entry.getKey();
            Integer transferPrincipal = entry.getValue();
            //更新所有的持有中的债权份数
            credit.setHoldingPrincipal(credit.getHoldingPrincipal()-transferPrincipal);
            creditDao.update(credit);
        }
        for (CreditOpening creditOpening : creditOpenings) {
            //更新最终的交易状态
            creditOpening.setExtStatus(baseResponse.getStatus());
            creditOpening.setStatus(CreditOpening.STATUS_PENDING);
            if (BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                creditOpening.setStatus(CreditOpening.STATUS_OPENING);
                //如果此次调用成功 需要将开放标识改为开放
                creditOpening.setOpenFlag(CreditOpening.OPEN_FLAG_ON);
                creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_IPLAN|GlobalConfig.OPEN_TO_LPLAN);
                creditOpening.setOpenTime(DateUtil.getCurrentDateTime());
            }
            creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
            creditOpeningDao.update(creditOpening);
        }
        return creditOpenings;
    }



    private CreditOpening saveOpeningCredit(BigDecimal transferDiscount, Credit credit,
                                            Integer sourceChannelId, String extSn,Integer transferPrincipal) {
        CreditOpening creditOpening = new CreditOpening();
        creditOpening.setCreditId(credit.getId());
        creditOpening.setSubjectId(credit.getSubjectId());
        creditOpening.setTransferorId(credit.getUserId());
        creditOpening.setTransferorIdXM(credit.getUserIdXM());
        creditOpening.setTransferDiscount(transferDiscount);
        creditOpening.setSourceChannel(credit.getSourceChannel());
        creditOpening.setSourceChannelId(sourceChannelId);
        creditOpening.setSourceAccountId(credit.getSourceAccountId());
        creditOpening.setPublishTime(DateUtil.getCurrentDateTime());
        creditOpening.setEndTime(credit.getEndTime());
        creditOpening.setOpenFlag(CreditOpening.OPEN_FLAG_OFF);
        creditOpening.setAvailablePrincipal(transferPrincipal);
        creditOpening.setTransferPrincipal(transferPrincipal);
        creditOpening.setExtStatus(BaseResponse.STATUS_PENDING);
        creditOpening.setExtSn(extSn);
        creditOpening.setCreateTime(DateUtil.getCurrentDateTime19());
        creditOpeningDao.insert(creditOpening);
        return creditOpening;
    }


    /**
     * 债权转让
     *
     * @param transferDiscount 折让率(不折让传1)
     * @param sourceChannelId  交易来源ID(转让交易记录ID)
     * @return
     */
    public List<CreditOpening> creditTransfer(Map<String, List<Credit>> paramMap, BigDecimal transferDiscount, Integer sourceChannelId,String IntelRequestNo,String userId,Integer sourceAccountId) {
        /*logger.info("开始调用债权转让方法->输入参数:creditId={},转让份数={},折让率={}", Arrays.toString(paramMap.keySet().stream().map(Credit::getId).toArray())
                , Arrays.toString(paramMap.entrySet().stream().map(Map.Entry::getValue).toArray()), transferDiscount);*/
        RequestIntelligentProjectDebentureSale request = new RequestIntelligentProjectDebentureSale();
        List<RequestIntelligentProjectDebentureSale.Detail> details = new ArrayList<>(paramMap.size());
        request.setRequestNo(IdUtil.getRequestNo());
        request.setDetails(details);
        request.setTransCode(TransCode.CREDIT_TRANSFER.getCode());
        List<CreditOpening> creditOpenings = new ArrayList<>(paramMap.size());
        for (Map.Entry<String, List<Credit>> entry : paramMap.entrySet()) {
            String subjectId = entry.getKey();
            List<Credit> credits = entry.getValue();
            Integer transferPrincipal = credits.stream().map(credit -> credit.getHoldingPrincipal()).reduce(Integer::sum).orElse(0);
            if (transferPrincipal>0){
                List<SubjectRepaySchedule> subjectRepaySchedules = subjectRepayScheduleService.findRepayScheduleBySubjectId(subjectId);
                if (subjectRepaySchedules == null || subjectRepaySchedules.isEmpty()) {
                    logger.error("查询不到该标的下的还款计划,subjectId=" + subjectId);
                    throw new ProcessException(Error.NDR_0202.getCode(), "查询不到该标的下的还款计划,subjectId=" + subjectId);
                }
                //判断还款计划中是否有逾期 逾期不能进行债转
                if (subjectRepaySchedules.stream().anyMatch(
                    subjectRepaySchedule -> SubjectRepaySchedule.STATUS_OVERDUE.equals(subjectRepaySchedule.getStatus())
                )) {
                    logger.warn("要债转的债权中存在逾期标的，不能进行债转");
                    throw new ProcessException(Error.NDR_0303.getCode(), Error.NDR_0303.getMessage() + ":subjectId=" + subjectId);
                }
                Subject subject = subjectService.findSubjectBySubjectId(subjectId);
                RequestIntelligentProjectDebentureSale.Detail detail = new RequestIntelligentProjectDebentureSale.Detail();
                detail.setSaleRequestNo(IdUtil.getRequestNo());
                detail.setIntelRequestNo(IntelRequestNo);
                detail.setPlatformUserNo(userId);
                detail.setProjectNo(String.valueOf(subject.getSubjectId()));
                detail.setSaleShare(transferPrincipal/100.0);
                details.add(detail);
                //保存开放中的债权数据 再进行调用
                CreditOpening creditOpening = saveOpeningCredit(transferDiscount, sourceChannelId, detail.getSaleRequestNo(),transferPrincipal,subjectId,userId,1,credits.get(0).getEndTime(),sourceAccountId);
                creditOpenings.add(creditOpening);
            }

        }
        //调用厦门银行债权出让接口
        BaseResponse baseResponse = null;
        try {
            //存在转让出的债权为0的情况
            if (details.size() > 0) {
                logger.info("开始调用厦门银行批量债权出让接口->{}", JSON.toJSONString(request));
                baseResponse = transactionService.intelligentProjectDebentureSale(request);
                logger.info("批量债权出让接口返回->{}", JSON.toJSONString(baseResponse));
            } else {
                baseResponse = new BaseResponse();
                baseResponse.setStatus(BaseResponse.STATUS_SUCCEED);//默认为成功
            }
        } catch (Exception e) {
            if (baseResponse == null) {
                baseResponse = new BaseResponse();
            }
            baseResponse.setStatus(BaseResponse.STATUS_PENDING);
        }
        //无论交易结果如何 都更新债权的持有份数 防止转出份额超过剩余份数
        for (Map.Entry<String, List<Credit>> entry : paramMap.entrySet()) {
            //String subjcetId = entry.getKey();
            List<Credit> credits = entry.getValue();
            for (Credit credit:credits) {
                Integer transferPrincipal = credit.getHoldingPrincipal();
                //更新所有的持有中的债权份数
                credit.setHoldingPrincipal(credit.getHoldingPrincipal()-transferPrincipal);
                creditDao.update(credit);
            }
        }
        for (CreditOpening creditOpening : creditOpenings) {
            //更新最终的交易状态
            creditOpening.setExtStatus(baseResponse.getStatus());
            creditOpening.setStatus(CreditOpening.STATUS_PENDING);
            if (BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                creditOpening.setStatus(CreditOpening.STATUS_OPENING);
                //如果此次调用成功 需要将开放标识改为开放
                creditOpening.setOpenFlag(CreditOpening.OPEN_FLAG_ON);
                creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_IPLAN|GlobalConfig.OPEN_TO_LPLAN);
                creditOpening.setOpenTime(DateUtil.getCurrentDateTime());
            }
            creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
            creditOpeningDao.update(creditOpening);
        }
        return creditOpenings;
    }




    private CreditOpening saveOpeningCredit(BigDecimal transferDiscount,
                                            Integer sourceChannelId, String extSn,Integer transferPrincipal,String subjectId,String userId,Integer sourceChannel,String endTime,Integer sourceAccountId) {
        CreditOpening creditOpening = new CreditOpening();
        //creditOpening.setCreditId(credit.getId());
        creditOpening.setSubjectId(subjectId);
        creditOpening.setTransferorId(userId);
        creditOpening.setTransferorIdXM(userId);
        creditOpening.setTransferDiscount(transferDiscount);
        creditOpening.setSourceChannel(sourceChannel);
        creditOpening.setSourceChannelId(sourceChannelId);
        creditOpening.setSourceAccountId(sourceAccountId);
        creditOpening.setPublishTime(DateUtil.getCurrentDateTime());
        creditOpening.setEndTime(endTime);
        creditOpening.setOpenFlag(CreditOpening.OPEN_FLAG_OFF);
        creditOpening.setAvailablePrincipal(transferPrincipal);
        creditOpening.setTransferPrincipal(transferPrincipal);
        creditOpening.setExtStatus(BaseResponse.STATUS_PENDING);
        creditOpening.setExtSn(extSn);
        creditOpening.setCreateTime(DateUtil.getCurrentDateTime19());
        creditOpeningDao.insert(creditOpening);
        return creditOpening;
    }

    public Credit findById(Integer creditId) {
        return creditDao.findById(creditId);
    }

    public int update(Credit credit) {
        if (null == credit.getId()) {
            throw new IllegalArgumentException("id不能为空");
        }
        credit.setUpdateTime(DateUtil.getCurrentDateTime19());
        return creditDao.update(credit);
    }

    public List<Credit> findAllCreditBySubjectId(String subjectId) {
        return creditDao.findAllCreditBySubjectId(subjectId);
    }
    /**
     * 债权放款
     *
     * @param credit 债权
     * @return
     */
    public void creditLoan(Credit credit){
        logger.info("债权放款执行，债权id：{}",credit.getId());
        Integer transStatus = credit.getExtStatus();
        if (transStatus==null){
            transStatus = BaseResponse.STATUS_FAILED;
        }
        if (BaseResponse.STATUS_PENDING.equals(transStatus)){
            logger.info("上次债转交易状态未知，单笔查询，请求流水号{}", credit.getExtSn());
            RequestSingleTransQuery request = new RequestSingleTransQuery();
            request.setRequestNo(credit.getExtSn());
            request.setTransactionType(TransactionType.TRANSACTION);
            ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
            if (!"0".equals(responseQuery.getCode())) {
                logger.info("上次债转交易状态未知，单笔查询，请求流水号{},查询失败，返回码{}", credit.getExtSn(), responseQuery.getCode());
                if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(responseQuery.getCode())) {
                    //订单不存在，重新发起交易
                    credit.setExtStatus(BaseResponse.STATUS_FAILED);
                    transStatus = BaseResponse.STATUS_FAILED;
                }
            }else {
                TransactionQueryRecord transactionQueryRecord = (TransactionQueryRecord) responseQuery.getRecords().get(0);
                if ("SUCCESS".equals(transactionQueryRecord.getStatus())) {
                    logger.info("上次债转交易状态未知，单笔查询，请求流水号{},交易查询成功", credit.getExtSn());
                    credit.setExtStatus(BaseResponse.STATUS_SUCCEED);
                    transStatus = BaseResponse.STATUS_SUCCEED;
                } else if ("FAIL".equals(transactionQueryRecord.getStatus())) {
                    logger.info("上次债转交易状态未知，单笔查询，请求流水号{},交易查询失败，重新发起交易", credit.getExtSn());
                    credit.setExtStatus(BaseResponse.STATUS_FAILED);
                    transStatus=BaseResponse.STATUS_FAILED;
                } else {
                    //查询结果：交易处理中
                    logger.info("上次债转交易状态未知，单笔查询，请求流水号{},交易查询处理中", credit.getExtSn());
                }
            }
        }
        if (BaseResponse.STATUS_FAILED.equals(transStatus)) {
            RequestSingleTrans request = new RequestSingleTrans();
            request.setTransCode(TransCode.CREDIT_LEND.getCode());
            BaseResponse baseResponse;
            request = constructRequest(credit);
            credit.setExtSn(request.getRequestNo());
            logger.info("债权放款设置流水号->{}",credit.getExtSn());
            try {
                logger.info("发送到厦门银行的放款参数->{}", JSON.toJSONString(request));
                baseResponse = transactionService.singleTrans(request);
                logger.info("厦门银行返回的报文参数为->{}", JSON.toJSONString(baseResponse));
            } catch (Exception e) {
                logger.error("单笔交易调用异常->{}", e);
                baseResponse = new BaseResponse();
                baseResponse.setStatus(BaseResponse.STATUS_PENDING);
            }
            if (BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                logger.info("厦门银行返回结果->{}",baseResponse.getStatus());
                credit.setExtStatus(BaseResponse.STATUS_SUCCEED);
            } else if (BaseResponse.STATUS_FAILED.equals(baseResponse.getStatus())) {
                logger.info("厦门银行返回结果->{}",baseResponse.getStatus());
                credit.setExtStatus(BaseResponse.STATUS_FAILED);
            } else {
                credit.setExtStatus(BaseResponse.STATUS_PENDING);
            }
            logger.info("债权设置结果->{}",credit.getExtStatus());

        }
        logger.info("开始更新债权->{}",credit.getExtSn());
        creditDao.update(credit);
        logger.info("更新债权结束->{}",credit.getExtSn());

    }
    private RequestSingleTrans constructRequest(Credit credit) {
        List<RequestSingleTrans.Detail> details = new ArrayList<>();
        RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
        detail.setBizType(BizType.CREDIT_ASSIGNMENT);
        String freezeRequestNo = null;
        String appendFreezeRequestNo = null;
        String saleRequestNo = null;
        String transferorIdXM = null;
        BigDecimal transferDiscount = null;
        //根据债权userId查询到对应的活期账户 并获取到冻结投资金额的流水号
        //增加省心投债权放款
        if (credit.getSourceChannel()==Credit.SOURCE_CHANNEL_IPLAN||credit.getSourceChannel()==Credit.SOURCE_CHANNEL_YJT){
            IPlanAccount iPlanAccount = iPlanAccountDao.findById(credit.getSourceAccountId());
            freezeRequestNo = iPlanAccount.getInvestRequestNo();
        }
        if (credit.getSourceChannel()==Credit.SOURCE_CHANNEL_LPLAN){
            LPlanAccount lPlanAccount = lPlanAccountDao.findByUserId(credit.getUserId());
            freezeRequestNo = lPlanAccount.getInvestRequestNo();
        }
        if (credit.getSourceChannel()== Credit.SOURCE_CHANNEL_SUBJECT){
            SubjectAccount subjectAccount = subjectAccountDao.findById(credit.getSourceAccountId());
            freezeRequestNo = subjectAccount.getInvestRequestNo();
        }
        if (Credit.TARGET_CREDIT==credit.getTarget()){
            CreditOpening creditOpening = creditOpeningDao.findById(credit.getTargetId());
            if (CreditOpening.SOURCE_CHANNEL_IPLAN==creditOpening.getSourceChannel()){
                appendFreezeRequestNo = iPlanAccountDao.findById(creditOpening.getSourceAccountId()).getInvestRequestNo();
                transferDiscount = creditOpening.getTransferDiscount();
            }
            if (CreditOpening.SOURCE_CHANNEL_LPLAN==creditOpening.getSourceChannel()){
                appendFreezeRequestNo = lPlanAccountDao.findById(lPlanTransLogDao.findById(creditOpening.getSourceChannelId()).getAccountId()).getInvestRequestNo();
                transferDiscount = creditOpening.getTransferDiscount();
            }
            if(CreditOpening.SOURCE_CHANNEL_SUBJECT == creditOpening.getSourceChannel()){
                appendFreezeRequestNo = subjectAccountDao.findById(subjectTransLogDao.findById(creditOpening.getSourceChannelId()).getAccountId()).getInvestRequestNo();
                transferDiscount = creditOpening.getTransferDiscount();
            }
            if(CreditOpening.SOURCE_CHANNEL_YJT == creditOpening.getSourceChannel()){
                appendFreezeRequestNo = iPlanAccountDao.findById(iPlanTransLogDao.findById(creditOpening.getSourceChannelId()).getAccountId()).getInvestRequestNo();
                transferDiscount = creditOpening.getTransferDiscount();
            }
            transferorIdXM = creditOpening.getTransferorIdXM();
            saleRequestNo = creditOpening.getExtSn();
        }
        Integer marketingAmt = credit.getMarketingAmt();
        if (marketingAmt==null){
            marketingAmt = 0;
        }
        detail.setFreezeRequestNo(freezeRequestNo);
        detail.setSourcePlatformUserNo(credit.getUserIdXM());
        detail.setTargetPlatformUserNo(transferorIdXM);
        //TODO amount为用户支付金额，share为用户购买总金额
        //购买人实际要支付金额为债权持有本金*(1-折让率)
        detail.setAmount(ArithUtil.round((credit.getHoldingPrincipal()*(transferDiscount.doubleValue()) - marketingAmt) / 100.0,2));//将金额转换为元
        detail.setShare(credit.getHoldingPrincipal() / 100.0);
        details.add(detail);
        if (marketingAmt > 0){
            RequestSingleTrans.Detail detail1 = new RequestSingleTrans.Detail();
            detail1.setBizType(BizType.MARKETING);
            detail1.setFreezeRequestNo(freezeRequestNo);
            detail1.setSourcePlatformUserNo(GlobalConfig.MARKETING_ACCOUNT_02_DR);//出款方平台营销款账户
            detail1.setTargetPlatformUserNo(transferorIdXM);//收款方用户编号
            detail1.setAmount(marketingAmt / 100.0);
            details.add(detail1);
        }
        //所有债权放款都追加冻结
        RequestSingleTrans.Detail detail2 = new RequestSingleTrans.Detail();
        detail2.setBizType(BizType.APPEND_FREEZE);
        detail2.setFreezeRequestNo(appendFreezeRequestNo);
        detail2.setSourcePlatformUserNo(transferorIdXM);
        detail2.setAmount(ArithUtil.round((credit.getHoldingPrincipal()*(transferDiscount.doubleValue())) / 100.0,2));
        details.add(detail2);

        RequestSingleTrans request = new RequestSingleTrans();
        String requestNo;
        if (!StringUtils.isEmpty(credit.getExtSn()) && BaseResponse.STATUS_PENDING.equals(credit.getExtStatus())) {
            requestNo = credit.getExtSn();//取上一次放款的交易流水重复调用放款接口（接口是幂等的）
        } else {
//            logger.warn("credit{},creditOpening{} userId{} 放款失败 需要重新发起放款",credit.getId(),creditOpening.getId(),lPlanAccount.getUserId());
            //如果是第一次发起放款||上次放款状态是失败 都要进行重新放款
            requestNo = IdUtil.getRequestNo();
        }
        request.setRequestNo(requestNo);
        request.setTradeType(TradeType.CREDIT_ASSIGNMENT);
        request.setProjectNo(String.valueOf(credit.getSubjectId()));
        request.setSaleRequestNo(saleRequestNo);
        request.setDetails(details);

        return request;
    }

    /**债权放款本地数据处理
     *
     */
    public void creditLocalHandle(List<Credit> credits){
        for (Credit credit : credits) {
            credit.setCreditStatus(Credit.CREDIT_STATUS_HOLDING);
            //用户可见
            String tiFreezeOperatorInfo = null;
            String toFreezeOperatorInfo = null;
            String marketOperatorInfo = null;
            //后台可见
            String tiFreezeOperatorDetail = null;
            String toFreezeOperatorDetail = null;
            String marketOperatorDetail = null;
            //折让率
            BigDecimal transferDiscount = null;
            CreditOpening creditOpening = creditOpeningDao.findById(credit.getTargetId());
            if (Credit.SOURCE_CHANNEL_IPLAN==credit.getSourceChannel()||Credit.SOURCE_CHANNEL_YJT==credit.getSourceChannel()){
                IPlan iplan = iPlanDao.findById(iPlanAccountDao.findById(credit.getSourceAccountId()).getIplanId());
                toFreezeOperatorInfo = iplan.getName()+"投资成功";
                toFreezeOperatorDetail = "月月盈或省心投ID："+iplan.getId() + ",金额：" + credit.getHoldingPrincipal()/100.0;
                marketOperatorInfo = "投资"+iplan.getName()+"使用抵扣劵成功";
                marketOperatorDetail = "月月盈或省心投ID："+iplan.getId() + ",抵扣劵金额：" + credit.getMarketingAmt()/100.0;

            }
            if (Credit.SOURCE_CHANNEL_LPLAN==credit.getSourceChannel()){
                toFreezeOperatorInfo = "天天赚投资成功";
                toFreezeOperatorDetail = "天天赚投资成功-购买债权-userId:" + credit.getUserId();
                marketOperatorInfo = "投资天天赚使用抵扣劵成功";
                marketOperatorDetail = "抵扣劵金额：" + credit.getMarketingAmt()/100.0;
            }
            if (Credit.SOURCE_CHANNEL_SUBJECT == credit.getSourceChannel()){
                toFreezeOperatorInfo = "散标投资成功";
                toFreezeOperatorDetail = "散标投资成功-购买债权-userId:" + credit.getUserId();
                marketOperatorInfo = "投资散标使用抵扣劵成功";
                marketOperatorDetail = "抵扣劵金额：" + credit.getMarketingAmt()/100.0;
                if(Credit.TARGET_CREDIT == credit.getTarget()){
                    redpacketService.createCreditPacketInvest(credit);
                    SubjectAccount subjectAccount = subjectAccountDao.findById(credit.getSourceAccountId());
                    subjectAccount.setTotalReward(subjectAccount.getExpectedReward());
                    subjectAccount.setPaidReward(subjectAccount.getExpectedReward());
                    subjectAccount.setExpectedReward(0);
                    subjectAccountDao.update(subjectAccount);
                }
            }
            if (CreditOpening.SOURCE_CHANNEL_IPLAN==creditOpening.getSourceChannel()){
                IPlan transferIplan = null;
                transferIplan = iPlanDao.findById(iPlanAccountDao.findById(creditOpening.getSourceAccountId()).getIplanId());
                if (creditOpening.getSourceChannelId()==null){
                    transferDiscount = creditOpening.getTransferDiscount();
                    tiFreezeOperatorInfo = transferIplan.getName()+"复投冻结";
                    tiFreezeOperatorDetail = "月月盈ID："+transferIplan.getId() + ",金额：" + credit.getHoldingPrincipal()/100.0;
                }else{
                    transferDiscount = creditOpening.getTransferDiscount();
                    tiFreezeOperatorInfo = transferIplan.getName()+"退出冻结";
                    tiFreezeOperatorDetail = "月月盈ID："+transferIplan.getId() + ",金额：" + credit.getHoldingPrincipal()/100.0;
                    IPlanTransLog iPlanTransLog = iPlanTransLogDao.findById(creditOpening.getSourceChannelId());
                    iPlanTransLog.setProcessedAmt(iPlanTransLog.getProcessedAmt()+credit.getHoldingPrincipal());
                    iPlanTransLogDao.update(iPlanTransLog);
                }

            }
            if (CreditOpening.SOURCE_CHANNEL_LPLAN==creditOpening.getSourceChannel()){
                transferDiscount = creditOpening.getTransferDiscount();
                tiFreezeOperatorInfo = "天天赚退出冻结";
                tiFreezeOperatorDetail = "天天赚转让-债权成交冻结:" +credit.getHoldingPrincipal()/100.0 ;
                LPlanTransLog lPlanTransLog = lPlanTransLogDao.findById(creditOpening.getSourceChannelId());
                lPlanTransLog.setProcessedAmt(lPlanTransLog.getProcessedAmt()+credit.getHoldingPrincipal());
                lPlanTransLogDao.update(lPlanTransLog);
                LPlanAccount lPlanAccount = lPlanAccountDao.findByUserId(creditOpening.getTransferorId());
                lPlanAccount.setCurrentPrincipal(lPlanAccount.getCurrentPrincipal()-credit.getHoldingPrincipal());
                lPlanAccount.setAmtToTransfer(lPlanAccount.getAmtToTransfer()-credit.getHoldingPrincipal());
                if (lPlanAccount.getCurrentPrincipal()<0){
                    lPlanAccount.setCurrentPrincipal(0);
                }
                if (lPlanAccount.getAmtToTransfer()<0){
                    lPlanAccount.setAmtToTransfer(0);
                }
                lPlanAccountDao.update(lPlanAccount);
            }
            if (CreditOpening.SOURCE_CHANNEL_SUBJECT == creditOpening.getSourceChannel()){
                transferDiscount = creditOpening.getTransferDiscount();
                tiFreezeOperatorInfo = "散标债权转让冻结";
                tiFreezeOperatorDetail = "散标债权转让-债权成交冻结:" +credit.getHoldingPrincipal()*(transferDiscount.doubleValue()) / 100.0;
                SubjectAccount subjectAccount = subjectAccountDao.findById(subjectTransLogDao.findById(creditOpening.getSourceChannelId()).getAccountId());
                //subjectAccount.setAmtToTransfer(subjectAccount.getAmtToTransfer() - credit.getHoldingPrincipal());
                if (subjectAccount.getCurrentPrincipal()<0){
                    subjectAccount.setCurrentPrincipal(0);
                }
                if (subjectAccount.getAmtToTransfer()<0){
                    subjectAccount.setAmtToTransfer(0);
                }
                subjectAccountDao.update(subjectAccount);

            }
            if (CreditOpening.SOURCE_CHANNEL_YJT == creditOpening.getSourceChannel()){
                transferDiscount = creditOpening.getTransferDiscount();
                tiFreezeOperatorInfo = "省心投债权转让冻结";
                tiFreezeOperatorDetail = "省心投债权转让-债权成交冻结:" +credit.getHoldingPrincipal()*(transferDiscount.doubleValue()) / 100.0;
                IPlanAccount iPlanAccount = iPlanAccountDao.findById(iPlanTransLogDao.findById(creditOpening.getSourceChannelId()).getAccountId());
                //subjectAccount.setAmtToTransfer(subjectAccount.getAmtToTransfer() - credit.getHoldingPrincipal());
                if (iPlanAccount.getCurrentPrincipal()<0){
                    iPlanAccount.setCurrentPrincipal(0);
                }
                if (iPlanAccount.getAmtToTransfer()<0){
                    iPlanAccount.setAmtToTransfer(0);
                }
                iPlanAccountDao.update(iPlanAccount);

            }
            Integer marketingAmt = credit.getMarketingAmt();
            if (marketingAmt > 0){
                BusinessEnum type = Credit.SOURCE_CHANNEL_IPLAN==credit.getSourceChannel()?BusinessEnum.ndr_iplan_deduct:BusinessEnum.ndr_suject_deduct;
                String sourceChannel = credit.getSourceChannel()==Credit.SOURCE_CHANNEL_IPLAN? "定期":"散标";
                platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_02_DR, credit.getMarketingAmt() / 100.0, type, "使用抵扣劵，账户ID：" + credit.getSourceAccountId()+",来源："+sourceChannel, credit.getExtSn());
                userAccountService.tiFreeze(credit.getUserIdXM(), credit.getMarketingAmt() / 100.0,type,
                        marketOperatorInfo, marketOperatorDetail, credit.getExtSn());
                //保存platformTransfer流水，用于对账
                insertPlatformTransfer(credit);
            }
            BusinessEnum businessType = null;
            if (Credit.SOURCE_CHANNEL_IPLAN==credit.getSourceChannel()||Credit.SOURCE_CHANNEL_YJT==credit.getSourceChannel()){
                businessType = BusinessEnum.ndr_iplan_auto_invest;
            }else if(Credit.SOURCE_CHANNEL_LPLAN ==credit.getSourceChannel() ){
                businessType = BusinessEnum.ndr_ttz_auto_invest;
            }else{
                businessType = BusinessEnum.ndr_subject_auto_invest;
            }
            userAccountService.tofreeze(credit.getUserId(), ArithUtil.round((credit.getHoldingPrincipal()*(transferDiscount.doubleValue())) / 100.0,2), businessType, toFreezeOperatorInfo, toFreezeOperatorDetail, String.valueOf(credit.getExtSn()));
            //转出人转入到冻结金额
            userAccountService.tiFreeze(creditOpening.getTransferorId(),ArithUtil.round((credit.getHoldingPrincipal()*(transferDiscount.doubleValue())) / 100.0,2),BusinessEnum.ndr_credit_loan_freeze,tiFreezeOperatorInfo,tiFreezeOperatorDetail , String.valueOf(credit.getExtSn()));
            creditDao.update(credit);
            //添加月月盈转出的复投记录
            if (CreditOpening.SOURCE_CHANNEL_IPLAN==creditOpening.getSourceChannel()) {
                saveIPlanTransLogAndAmtToInvest(creditOpening);
            }
        }

    }

    private void saveIPlanTransLogAndAmtToInvest(CreditOpening creditOpening) {
        if (creditOpening.getAvailablePrincipal()!=0) {
            logger.info("开放中债权还未全部匹配完成，无法添加复投记录creditOpeningId-{}",creditOpening.getId());
            return;
        }
        List<Credit> credits = creditDao.findByTargetIdAndTarget(creditOpening.getId(), Credit.TARGET_CREDIT);
        if (credits.stream().allMatch(credit -> credit.getCreditStatus() == Credit.CREDIT_STATUS_HOLDING)) {
            IPlanAccount iPlanAccount = iPlanAccountDao.findByAccountIdForUpdate(creditOpening.getSourceAccountId());
            if (creditOpening.getTransferPrincipal() > 0) {
                logger.info("增加复投记录账户-{},当前账户金额-{},复投金额-{}",creditOpening.getSourceAccountId(),iPlanAccount.getAmtToInvest(),creditOpening.getTransferPrincipal());

                if (CreditOpening.OPEN_CHANNEL_YJT.equals(creditOpening.getOpenChannel())){
                    if (creditOpening.getSourceChannelId()==null){
                        iPlanAccount.setAmtToInvest(iPlanAccount.getAmtToInvest()+creditOpening.getTransferPrincipal());
                        //本金复投交易记录
                        saveIPlanTransLog(creditOpening,iPlanAccount,creditOpening.getTransferPrincipal());
                        iPlanAccountDao.update(iPlanAccount);
                    }
                } else if (creditOpening.getSourceChannelId()==null){
                    iPlanAccount.setAmtToInvest(iPlanAccount.getAmtToInvest()+creditOpening.getTransferPrincipal());
                    Integer freezeAmtToInvest = iPlanAccount.getFreezeAmtToInvest()==null? 0 : iPlanAccount.getFreezeAmtToInvest();
                    iPlanAccount.setFreezeAmtToInvest(freezeAmtToInvest+creditOpening.getTransferPrincipal());
                    if (iPlanAccount.getFreezeAmtToInvest()>=GlobalConfig.CREDIT_AVAIABLE){
                        saveIPlanTransLog(creditOpening,iPlanAccount,iPlanAccount.getFreezeAmtToInvest());
                        iPlanAccount.setFreezeAmtToInvest(0);
                    }
                    iPlanAccountDao.update(iPlanAccount);
                }
                //变更开发中债权状态为已放款
                creditOpeningDao.updateStatusById(CreditOpening.STATUS_LENDED,creditOpening.getId());
            }
        }
    }

    private void saveIPlanTransLog(CreditOpening creditOpening,IPlanAccount iPlanAccount ,int transAmt){
        IPlanTransLog transLog = new IPlanTransLog();
        transLog.setAccountId(creditOpening.getSourceAccountId());
        transLog.setUserId(creditOpening.getTransferorId());
        //transLog.setUserIdXm(account.getUserIdXm());
        transLog.setTransTime(DateUtil.getCurrentDateTime19());
        transLog.setTransAmt(transAmt);
        transLog.setProcessedAmt(0);
        transLog.setTransStatus(IPlanTransLog.TRANS_STATUS_PROCESSING);
        transLog.setTransType(IPlanTransLog.TRANS_TYPE_PRINCIPLE_REINVEST);
        transLog.setTransDesc("本金复投");
        transLog.setRedPacketId(0);
        transLog.setIplanId(iPlanAccount.getIplanId());
        transLog.setExtSn(iPlanAccount.getInvestRequestNo());
        transLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
        transLog.setCreateTime(DateUtil.getCurrentDateTime19());
        transLog.setFlag(0);
        iPlanTransLogService.save(transLog);
    }

    private void insertPlatformTransfer(Credit credit){
        Double marketingAmount = credit.getMarketingAmt()/100D;
        PlatformTransfer platformTransfer = new PlatformTransfer();
        platformTransfer.setId(IdUtil.randomUUID());
        platformTransfer.setActualMoney(marketingAmount);
        platformTransfer.setBillType("out");
        platformTransfer.setLoanId(credit.getId().toString());
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

    public List<Credit> findBySubjectId(String subjectId) {
        return creditDao.findBySubjectId(subjectId);
    }

    public List<Credit> findBySubjectIdAndConfirmStatus(String subjectId) {
        if (StringUtils.isBlank(subjectId)) {
            throw new IllegalArgumentException("subjectId不能为空");
        }
        return creditDao.findBySubjectIdAndConfirmStatus(subjectId);
    }

    //批量形成债权
    public void creditCreate(SubjectTransLog subjectTransLog){

        //查询购买记录对应的标的
        Subject subject = subjectDao.findBySubjectId(subjectTransLog.getSubjectId());
        if(subject == null){
            return;
        }

        //查询散标账户
        SubjectAccount subjectAccount = subjectAccountDao.findById(subjectTransLog.getAccountId());
        if (subjectAccount == null){
            return;
        }

        //形成新的债权
        Credit newCredit = new Credit();
        newCredit.setSubjectId(subjectTransLog.getSubjectId());
        newCredit.setUserId(subjectTransLog.getUserId());
        newCredit.setUserIdXM(subjectTransLog.getUserId());
        newCredit.setInitPrincipal(subjectTransLog.getTransAmt());
        newCredit.setHoldingPrincipal(subjectTransLog.getTransAmt());
        newCredit.setResidualTerm(subject.getTerm() - subject.getCurrentTerm() + 1);
        newCredit.setCreditStatus(Credit.CREDIT_STATUS_WAIT);
        newCredit.setSourceChannel(Credit.SOURCE_CHANNEL_SUBJECT);
        newCredit.setSourceChannelId(subjectTransLog.getId());
        newCredit.setSourceAccountId(subjectTransLog.getAccountId());
        newCredit.setCreateTime(DateUtil.getCurrentDateTime19());
        newCredit.setMarketingAmt(subjectAccount.getDedutionAmt());
        if(SubjectTransLog.TARGET_SUBJECT.equals(subjectTransLog.getTarget())){
            newCredit.setStartTime(DateUtil.getCurrentDateTime17());
            newCredit.setTarget(Credit.TARGET_SUBJECT);
            newCredit.setTargetId(subject.getId());
        }else {
            CreditOpening creditOpening = creditOpeningDao.findById(subjectTransLog.getTargetId());
            Credit oldCredit = creditDao.findById(creditOpening.getCreditId());
            newCredit.setStartTime(oldCredit.getStartTime());
            newCredit.setEndTime(oldCredit.getEndTime());
            newCredit.setTarget(Credit.TARGET_CREDIT);
            newCredit.setTargetId(creditOpening.getId());
        }
        creditDao.insert(newCredit);

        //更新交易记录
        subjectTransLog.setProcessedAmt(subjectTransLog.getTransAmt());
        subjectTransLog.setTransStatus(SubjectTransLog.TRANS_STATUS_SUCCEED);
        subjectTransLogDao.update(subjectTransLog);
    }

    public List<Credit> findBySourceAccountIdAndTargetAndChannel(Integer sourceAccountId,Integer sourceChannel,Integer target){
        return creditDao.findBySourceAccountIdAndTargetAndChannel(sourceAccountId,sourceChannel,target);
    }

    public  List<Credit> findAllCreditBySubjectIdAndStatus(String subjectId,Integer status){
        return this.creditDao.findAllCreditBySubjectIdAndStatus(subjectId,status);
    }

    public Map<String, Object> findCreditForWithdraw(String userId, Integer iPlanAccountId, Integer iPlanId) {
        Map<String, Object> result = new HashMap<>();
        List<Credit> credits = creditDao.findByUserIdAndCreditStatusAndSourceChannelAndSourceAccountId(userId, new HashSet<>(Arrays.asList(Credit.CREDIT_STATUS_HOLDING)), Credit.SOURCE_CHANNEL_IPLAN, iPlanAccountId);
        Map<String, List<Credit>> collect = credits.stream().collect(Collectors.groupingBy(Credit::getSubjectId));
        //Map<Credit, Integer> creditsToTransfer = new HashMap<>();
        List<Integer> overdueCredits = new ArrayList<>();
        List<Integer> repayingCredits = new ArrayList<>();
        Integer creditsValue = 0;

        for (Credit credit : credits) {
            if (credit.getHoldingPrincipal() <= 0) {
                continue;
            }
            try {
                creditsValue += this.calcCreditValueForTransfer(credit, iPlanId);
            } catch (ProcessException pe) {
                if (Error.NDR_0511.getCode().equals(pe.getErrorCode())) {
                    overdueCredits.add(credit.getId());
                }
                if (Error.NDR_0521.getCode().equals(pe.getErrorCode())) {
                    repayingCredits.add(credit.getId());
                }
                continue;
            }

        }

        if (overdueCredits.size() > 0) {
            logger.error("not enough credits to transfer, there are overdue credits");
            throw new ProcessException(Error.NDR_0510);
        }
        if (repayingCredits.size() > 0) {
            logger.error("not enough credits to transfer, there are repaying credits");
            throw new ProcessException(Error.NDR_0522);
        }
        List<Credit> unconfirmedCredits = creditDao.findByUserIdAndCreditStatusAndSourceChannelAndSourceAccountId(userId, new HashSet<>(Arrays.asList(Credit.CREDIT_STATUS_WAIT)), Credit.SOURCE_CHANNEL_IPLAN, iPlanAccountId);
        if (unconfirmedCredits.size() > 0) {
            logger.error("not enough credits to transfer, there are unconfirmed credits");
            throw new ProcessException(Error.NDR_0519);
        }

        result.put("creditsValue", creditsValue);
        result.put("creditsToTransfer", collect);
        return result;
    }

    public Integer calcCreditValueForTransfer(Credit credit, Integer iPlanId) {
        //检查债权是否还款中
        List<SubjectRepayDetail> pendingRepayDetails = repayDetailDao.findNotRepay(credit.getSubjectId(),credit.getUserId());
        if (pendingRepayDetails.size() > 0) {
            logger.warn("credit id {} has pending repay details", credit.getId());
            throw new ProcessException(Error.NDR_0521);
        }
        //查找债权对应标的还款情况
        List<SubjectRepaySchedule> schedules = repayScheduleService.findRepayScheduleBySubjectId(credit.getSubjectId());
        String nowDate = DateUtil.getCurrentDateShort();
        SubjectRepaySchedule currentTerm = null, previousTerm = null;
        for (SubjectRepaySchedule schedule : schedules) {
            if (schedule.getStatus().equals(SubjectRepaySchedule.STATUS_OVERDUE)) {
                logger.warn("credit id {} is overdue", credit.getId());
                throw new ProcessException(Error.NDR_0511);
            }
            String dueDate = schedule.getDueDate();
            if (nowDate.compareTo(dueDate) <= 0) {
                currentTerm = schedule;
                if (currentTerm.getTerm() > 1) {
                    previousTerm = schedules.get(schedule.getTerm() - 1 - 1);
                }
                break;
            }
        }

        Subject subject = subjectService.getBySubjectId(credit.getSubjectId());
        //当期已经还款
        if (currentTerm.getStatus().equals(SubjectRepaySchedule.STATUS_NORMAL_REPAID)) {
            //提前还款情况下债权价值为持有本金
            return credit.getHoldingPrincipal();
        }
        //当期未还款
        else {
            String interestStartDate = null, interestEndDate = DateUtil.getCurrentDateShort();
            if (previousTerm == null) {
                interestStartDate = subject.getLendTime().substring(0, 8);
            } else {
                interestStartDate = previousTerm.getDueDate();
            }
            String creditStartDate = credit.getStartTime().substring(0, 8);
            //债权持有时间晚于上期还款时间，则从持有时间开始算利息
            if (creditStartDate.compareTo(interestStartDate) > 0) {
                interestStartDate = creditStartDate;
            }
            //如果应还日期小于当前日期（逾期未还），则利息只计算到应还日
            if (currentTerm.getDueDate().compareTo(interestEndDate) < 0) {
                interestEndDate = currentTerm.getDueDate();
            }
            Integer principal = credit.getHoldingPrincipal();

            BigDecimal interest = this.calculateInterest(interestStartDate, interestEndDate, principal, iPlanId);

            return principal + interest.intValue();
        }
    }
    //计算指定时间段下指定本金的应付利息
    public BigDecimal calculateInterest(String startDate, String endDate, Integer principal, Integer iPlanId) {
        BigDecimal principalDecimal = BigDecimal.valueOf(principal);
        if (DateUtil.betweenDays(startDate, endDate) > 30) {
            LocalDate startDatePlus1 = DateUtil.parseDate(startDate, DateUtil.DATE_TIME_FORMATTER_8).plusDays(1);
            startDate = DateUtil.getDateStr(startDatePlus1, DateUtil.DATE_TIME_FORMATTER_8);
        }
        long days = DateUtil.betweenDays(startDate, endDate);
        BigDecimal rate = iPlanService.getIPlanById(iPlanId).getFixRate();
        BigDecimal interestTotal = principalDecimal.multiply(rate).multiply(new BigDecimal(days)).divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS), 6, BigDecimal.ROUND_DOWN);
        return interestTotal;
    }
}


