package com.jiuyi.ndr.service.credit;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.dao.subject.SubjectSendSmsDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectAccount;
import com.jiuyi.ndr.domain.subject.SubjectRepayDetail;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.notice.support.TemplateId;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.PageUtil;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.BaseResponseStr;
import com.jiuyi.ndr.xm.http.request.RequestDebentureSale;
import com.jiuyi.ndr.xm.http.request.RequestIntelligentProjectDebentureSale;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by lixiaolei on 2017/6/8.
 */
@Service
public class CreditService {

    private final static Logger logger = LoggerFactory.getLogger(CreditService.class);

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectRepayScheduleService repayScheduleService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private SubjectRepayDetailDao repayDetailDao;
    @Autowired
    private CreditOpeningDao creditOpeningDao;
    @Autowired
    private CreditDao creditDao;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    SubjectSendSmsDao subjectSendSmsDao;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    UserService userService;
    @Autowired
    ConfigDao configDao;

    /**
     * 债权转让
     *
     * @param transferDiscount 折让率(不折让传1)
     * @param sourceChannelId  交易来源ID(转让交易记录ID)
     * @return
     */
    public List<CreditOpening> creditTransfer(Map<Credit, Integer> paramMap, BigDecimal transferDiscount, Integer sourceChannelId,String intelRequestNo) {

        logger.info("开始调用债权转让方法->输入参数:creditId={},转让份数={},折让率={}", Arrays.toString(paramMap.keySet().stream().map(Credit::getId).toArray())
                , Arrays.toString(paramMap.entrySet().stream().map(Map.Entry::getValue).toArray()), transferDiscount);

        RequestIntelligentProjectDebentureSale request = new RequestIntelligentProjectDebentureSale();
        List<RequestIntelligentProjectDebentureSale.Detail> details = new ArrayList<>(paramMap.size());
        request.setRequestNo(IdUtil.getRequestNo());
        request.setDetails(details);
        request.setTransCode(TransCode.CREDIT_TRANSFER.getCode());

        List<CreditOpening> creditOpenings = new ArrayList<>(paramMap.size());
        for (Map.Entry<Credit, Integer> entry : paramMap.entrySet()) {
            Credit credit = creditDao.findByIdForUpdate(entry.getKey().getId());
            Integer transferPrincipal = entry.getValue();
            List<SubjectRepaySchedule> subjectRepaySchedules = repayScheduleService.findRepayScheduleBySubjectId(credit.getSubjectId());
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

            Subject subject = subjectService.getBySubjectId(credit.getSubjectId());
            RequestIntelligentProjectDebentureSale.Detail detail = new RequestIntelligentProjectDebentureSale.Detail();
            detail.setSaleRequestNo(IdUtil.getRequestNo());
            detail.setIntelRequestNo(intelRequestNo);
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
                if(CreditOpening.SOURCE_CHANNEL_IPLAN == creditOpening.getSourceChannel() || CreditOpening.SOURCE_CHANNEL_LPLAN == creditOpening.getSourceChannel()){
                    Config openConfig = configDao.getConfigById(Config.IPLAN_OPEN_TO_SUBJECT);
                    if (openConfig!=null&&openConfig.getValue()!=null&&Config.IPLAN_OPEN_ON.equals(openConfig.getValue())){
                        //如果是散标,开放到散标专区
                        try {
                            Subject subject = subjectService.getBySubjectId(creditOpening.getSubjectId());
                            SubjectRepaySchedule subjectRepaySchedule = repayScheduleService.findRepayScheduleNotRepayOnlyOne(creditOpening.getSubjectId());
                            LocalDate startDate = DateUtil.parseDate(subjectRepaySchedule.getDueDate(), DateUtil.DATE_TIME_FORMATTER_8);
                            Config moneyConfig = configDao.getConfigById(Config.IPLAN_OPEN_MONEY);
                            int moeny = Integer.parseInt(moneyConfig.getValue()==null?"100000":moneyConfig.getValue());
                            if (subjectRepaySchedule!=null&&DateUtil.betweenDays(LocalDate.now(), startDate) > GlobalConfig.IPLAN_OPEN_SUBJECT_DAYS&&creditOpening.getTransferPrincipal()>=moeny&&subject.getRate().compareTo(new BigDecimal("0.068"))>0){
                                creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_SUBJECT);
                            } else {
                                creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_IPLAN|GlobalConfig.OPEN_TO_LPLAN);
                            }
                        }catch (Exception e){
                            logger.info("债权开放异常，债权id-{}，默认开放到月月盈",creditOpening.getId());
                            creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_IPLAN|GlobalConfig.OPEN_TO_LPLAN);
                        }
                    } else {
                        creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_IPLAN|GlobalConfig.OPEN_TO_LPLAN);
                    }
                    //todo 如果债权来源是一键投，根据配置设置开放渠道(随心投修改-jgx-5.16)
                } else if (CreditOpening.SOURCE_CHANNEL_YJT == creditOpening.getSourceChannel()) {
                    setYjtOpenChannel(creditOpening);
                } else {
                    creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_SUBJECT);
                }
                creditOpening.setOpenTime(DateUtil.getCurrentDateTime());
                creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
            }
            creditOpeningDao.update(creditOpening);
        }

        return creditOpenings;
    }



    /**
     * 债权转让
     *
     * @param transferDiscount 折让率(不折让传1)
     * @param sourceChannelId  交易来源ID(转让交易记录ID)
     * @return
     */
    public List<CreditOpening> creditTransfer(Map<String, List<Credit>> paramMap, BigDecimal transferDiscount, Integer sourceChannelId,String intelRequestNo,String userId,Integer sourceAccountId) {
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
            List<SubjectRepaySchedule> subjectRepaySchedules = repayScheduleService.findRepayScheduleBySubjectId(subjectId);
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
            detail.setIntelRequestNo(intelRequestNo);
            detail.setPlatformUserNo(userId);
            detail.setProjectNo(String.valueOf(subject.getSubjectId()));
            detail.setSaleShare(transferPrincipal/100.0);
            details.add(detail);
            //保存开放中的债权数据 再进行调用
            CreditOpening creditOpening = saveOpeningCredit(transferDiscount, sourceChannelId, detail.getSaleRequestNo(),transferPrincipal,subjectId,userId,1,credits.get(0).getEndTime(),sourceAccountId);
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



    /**
     * 单笔债权转让
     *
     * @param transferDiscount 折让率(不折让传1)
     * @param sourceChannelId  交易来源ID(转让交易记录ID)
     * @return
     */
    public CreditOpening singleCreditTransfer(Credit credit,Integer amount, BigDecimal transferDiscount, Integer sourceChannelId) {

        logger.info("开始调用债权转让方法->输入参数:creditId={},转让金额={},折让率={}", credit.getId(),amount /100.0, transferDiscount);

        List<SubjectRepaySchedule> subjectRepaySchedules = repayScheduleService.findRepayScheduleBySubjectId(credit.getSubjectId());
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

        RequestDebentureSale request = new RequestDebentureSale();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setPlatformUserNo(credit.getUserId());
        request.setProjectNo(credit.getSubjectId());
        request.setSaleShare(amount/100.0);
        request.setTransCode(TransCode.SUBJECT_CREDIT_TRANSFER.getCode());

        //保存开放中的债权数据 再进行调用
        CreditOpening creditOpening = saveOpeningCredit(transferDiscount, credit, sourceChannelId, request.getRequestNo(),amount);

        //调用厦门银行债权出让接口
        BaseResponseStr baseResponse = null;
        try {
            logger.info("开始调用厦门银行单笔债权出让接口->{}", JSON.toJSONString(request));
            baseResponse = transactionService.debentureSale(request);
            logger.info("单笔债权出让接口返回->{}", JSON.toJSONString(baseResponse));
        } catch (Exception e) {
            if (baseResponse == null) {
                baseResponse = new BaseResponseStr();
            }
            baseResponse.setStatus(BaseResponseStr.STATUS_PENDING);
        }
        //无论交易结果如何 都更新债权的持有份数 防止转出份额超过剩余份数
        credit.setHoldingPrincipal(credit.getHoldingPrincipal()-amount);
        creditDao.update(credit);

        //更新最终的交易状态
        creditOpening.setExtStatus(Integer.valueOf(baseResponse.getStatus()));
        creditOpening.setStatus(CreditOpening.STATUS_PENDING);
        if (BaseResponseStr.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
            creditOpening.setStatus(CreditOpening.STATUS_OPENING);
            //如果此次调用成功 需要将开放标识改为开放
            creditOpening.setOpenFlag(CreditOpening.OPEN_FLAG_ON);
            if(CreditOpening.SOURCE_CHANNEL_SUBJECT != (creditOpening.getSourceChannel())){//如果是散标,开放到散标专区
                creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_IPLAN|GlobalConfig.OPEN_TO_LPLAN);
            }else {
                creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_SUBJECT);
            }
            creditOpening.setOpenTime(DateUtil.getCurrentDateTime());
            creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
        }
        creditOpeningDao.update(creditOpening);
        //发送短信
        User user = userService.getUserById(credit.getUserId());
        Subject subject = subjectService.getBySubjectId(credit.getSubjectId());
        Double actualPrincipal = ArithUtil.round((amount / 100.0) * (transferDiscount.doubleValue()),2);
        try {
            String smsTemplate = TemplateId.CREDIT_TRANSFER_SUCCEED;
            noticeService.send(user.getMobileNumber(), subject.getName()+","
                    + String.valueOf(amount/100.0)+","+String.valueOf(actualPrincipal), smsTemplate);
        } catch (Exception e) {
            logger.error("债权转让短信发送失败",user.getMobileNumber()+"债权id："+credit.getId()+"转让金额："+String.valueOf(amount/100.0));
        }
        return creditOpening;
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

    //找出用户持有的价值为amtToTransfer的债权，如果不够，则返回还差的金额
    public Map<String, Object> findCreditForWithdraw(String userId, Integer iPlanAccountId, Integer iPlanId) {
        Map<String, Object> result = new HashMap<>();
        List<Credit> credits = creditDao.findByUserIdAndCreditStatusAndSourceChannelAndSourceAccountId(userId, new HashSet<>(Arrays.asList(Credit.CREDIT_STATUS_HOLDING)), Credit.SOURCE_CHANNEL_IPLAN, iPlanAccountId);
        Map<Credit, Integer> creditsToTransfer = new HashMap<>();
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

            creditsToTransfer.put(credit, credit.getHoldingPrincipal());

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
        result.put("creditsToTransfer", creditsToTransfer);
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
    @ProductSlave
    public List<Credit> getUserHoldingCreditsInSomeIPlanByPageHelper(String userId, Integer iPlanId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return this.getUserHoldingCreditsInSomeIPlan(userId, iPlanId);
    }

    public List<Credit> getUserHoldingCreditsInSomeIPlan(String userId, Integer iPlanId) {
        if (StringUtils.isBlank(userId) || iPlanId == null) {
            throw new IllegalArgumentException("user id and iPlan id is can not null when query user holding credits in some iPlan");
        }
        IPlanAccount iPlanAccount = iPlanAccountService.getIPlanAccount(userId, iPlanId);
        if (iPlanAccount == null) {
            throw new ProcessException(Error.NDR_0452);
        }
        return this.getByUserIdAndCreditStatusAndSourceChannelAndSourceAccountId(userId, new HashSet<>(Arrays.asList(Credit.CREDIT_STATUS_HOLDING, Credit.CREDIT_STATUS_WAIT)), Credit.SOURCE_CHANNEL_IPLAN, iPlanAccount.getId());
    }

    public List<Credit> getByUserIdAndCreditStatusAndSourceChannelAndSourceAccountId(String userId, Set<Integer> statuses, Integer sourceChannel, Integer iPlanId) {
        return creditDao.findByUserIdAndCreditStatusAndSourceChannelAndSourceAccountId(userId, statuses, sourceChannel, iPlanId);
    }

    public List<Credit> getByUserIdAndCreditStatusAndSourceChannelAndSourceAccountIdAndTarget(String userId, Set<Integer> statuses, Integer sourceChannel, Integer iPlanId, Integer target) {
        return creditDao.findByUserIdAndCreditStatusAndSourceChannelAndSourceAccountIdAndTarget(userId, statuses, sourceChannel, iPlanId, target);
    }

    public Credit getById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("credit id is can not null");
        }
        return creditDao.findById(id);
    }
    public Credit getByIdAndUserId(Integer id,String userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("credit id or userId is can not null");
        }
        return creditDao.findByIdAndUserId(id,userId);
    }

    public Credit getByIdLocked(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("credit id is can not null");
        }
        return creditDao.findByIdForUpdate(id);
    }

    public int insert(Credit credit){
        if (null == credit.getId()) {
            throw new IllegalArgumentException("id不能为空");
        }
        credit.setCreateTime(DateUtil.getCurrentDateTime19());
        return creditDao.insert(credit);
    }

    public List<Credit> findBySubjectId(String subjectId) {
        return creditDao.findBySubjectId(subjectId);
    }

    public int update(Credit credit) {
        if (null == credit.getId()) {
            throw new IllegalArgumentException("id不能为空");
        }
        credit.setUpdateTime(DateUtil.getCurrentDateTime19());
        return creditDao.update(credit);
    }
    @ProductSlave
    public List<Credit> findBySourceAccountId(int sourceAccountId,Integer channel) {
        return creditDao.findBySourceAccountId(sourceAccountId,channel);
    }

    public List<Credit> findCreditsBySubjectId(String subjectId) {
        return creditDao.findBySubjectId(subjectId);
    }

    public String getContractViewPdfUrlByContractId(String contractId) {
        if (StringUtils.isBlank(contractId)) {
            throw new IllegalArgumentException("contractId不能为空");
        }
        return creditDao.getContractViewPdfUrlByContractId(contractId);
    }
    public List<Credit> findAllCreditBySubjectId(String subjectId) {
        return creditDao.findAllCreditBySubjectId(subjectId);
    }
    public Credit findBySourceChannelIdAndSourceChannel(int sourceChannelId,int sourceChannel) {
        List<Credit> credits= creditDao.findBySourceChannelIdAndSourceChannel(sourceChannelId,sourceChannel);
        Credit credit = null;
        if(credits.size()>0 && sourceChannel==Credit.SOURCE_CHANNEL_SUBJECT){
            credit=credits.get(0);
        }
        return credit;
    }

    public Credit findBySourceAccountIdAndUserId(Integer sourceAccountId,String userId,String subjectId,Integer channel){
        if(sourceAccountId==null||userId==null||subjectId==null){
            throw new ProcessException(Error.NDR_0101.getCode(), Error.NDR_0101.getMessage());
        }
        return creditDao.findBySourceAccountIdAndUserId(sourceAccountId,userId,subjectId,channel);
    }

    public Credit findBySourceAccountIdAndTarget(Integer sourceAccountId, Integer target) {
        if (sourceAccountId == null || target == null) {
            throw new IllegalArgumentException("param can not be null");
        }
        return creditDao.findBySourceAccountIdAndTarget(sourceAccountId, target);
    }

    public List<Credit> getAllCreditBytargetId(Integer targetId) {
        return creditDao.findByTargetId(targetId);
    }


    public List<Credit> getCreditByUserId(String userId, int creditStatusTransfer, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return  creditDao.getCreditHoldByUserId(userId,creditStatusTransfer);
    }

    public List<Credit> getCreditFinishByUserId(String userId, Integer target,Integer creditStatusTransfer) {
        return  creditDao.getCreditFinishByUserId(userId,target,creditStatusTransfer);
    }

    public Credit getBySubjectAccountId(Integer subjectAccountId) {
        if(subjectAccountId == null){
            throw new IllegalArgumentException("id不能为空");
        }
        return creditDao.findBySubjectAccountId(subjectAccountId);
    }

    public List<Credit> getByUserIdAndStatusAndSourceChannel(String userId, Integer sourceChannel, Integer creditStatus, Integer type) {
        if(StringUtils.isBlank(userId)){
            throw  new IllegalArgumentException("userId can not be null");
        }
        if(sourceChannel==null){
            throw  new IllegalArgumentException("sourceChannel can not be null");
        }
        if(creditStatus==null){
            throw  new IllegalArgumentException("creditStatus can not be null");
        }
        if(type==null){
            throw  new IllegalArgumentException("type can not be null");
        }
        return creditDao.findByUserIdAndStatusAndSourceChannel(userId,sourceChannel,creditStatus,type);

    }

    public List<Credit> sortByCondition(List<Credit> lists,Integer pageNum,Integer pageSize){
        if (lists == null){
            throw  new IllegalArgumentException("lists can not be null");
        }
        if (pageNum == null){
            throw  new IllegalArgumentException("pageNum can not be null");
        }
        if (pageSize == null){
            throw  new IllegalArgumentException("pageSize can not be null");
        }
        List<Credit> credits = new ArrayList<>();
        for (Credit credit : lists) {
            Subject subject = subjectService.getBySubjectId(credit.getSubjectId());
            SubjectAccount subjectAccount = subjectAccountService.findAccountById(credit.getSourceAccountId());
            Integer status = subjectService.checkCondition(subject, subjectAccount);
            if (status == 1){
                credits.add(credit);
            }
        }
        List<Credit> newList = new PageUtil().ListSplit(credits, pageNum, pageSize);
        return newList;
    }

    public Credit findBySourceAccountIdAndSubject(Integer id) {
        return creditDao.findBySourceAccountIdAndSubject(id);
    }

    //项目剩余时间
    public Integer getDays(Credit credit){
        String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
        String endDate = credit.getEndTime().substring(0, 8);
        long days = DateUtil.betweenDays(currentDate, endDate);
        return (int) days;
    }


    //找出用户持有的价值为amtToTransfer的债权
    public Map<Credit, Integer> findCreditForTransfer(String userId, Integer iPlanAccountId, Integer amount) {
        List<Credit> credits = creditDao.findByUserIdAndCreditStatusAndSourceChannelAndSourceAccountIdNew(userId, Credit.CREDIT_STATUS_HOLDING, Credit.SOURCE_CHANNEL_YJT, iPlanAccountId);
        Map<Credit, Integer> creditsToTransfer = new HashMap<>();
        Integer totalAmt = 0;
        for (Credit credit : credits) {
            if(credit.getHoldingPrincipal() < amount - totalAmt ){
                totalAmt+=credit.getHoldingPrincipal();
                creditsToTransfer.put(credit, credit.getHoldingPrincipal());
            }else if(credit.getHoldingPrincipal() == amount - totalAmt){
                totalAmt+=credit.getHoldingPrincipal();
                creditsToTransfer.put(credit, credit.getHoldingPrincipal());
                break;
            }else{
                creditsToTransfer.put(credit, amount - totalAmt);
                break;
            }
        }
        return creditsToTransfer;
    }
    /**
     * 查询一键投持有中债权
     * @param userId
     * @param sourceChannel
     * @param accountId
     * @return
     */
    public List<Credit> findByUserIdAndSourceChannelAndAccountId(String userId, Integer sourceChannel, Integer accountId, Integer creditStatus){
        return creditDao.findByUserIdAndSourceChannelAndAccountId(userId, sourceChannel, accountId, creditStatus);
    }
    /**
     * 查询一键投债权
     * @param userId
     * @param sourceChannel
     * @param accountId
     * @return
     */
    public List<Credit> findYJTByUserIdAndSourceChannelAndAccountId(String userId, Integer sourceChannel, Integer accountId){
        return creditDao.findYJTByUserIdAndSourceChannelAndAccountId(userId, sourceChannel, accountId);
    }
    /**
     * 查询一键投持有中债权
     * @param userId
     * @param sourceChannel
     * @param accountId
     * @return
     */
    public List<Credit> findByUserIdAndSourceChannelAndAccountIdAndCreditStatus(String userId, Integer sourceChannel, Integer accountId){
        return creditDao.findByUserIdAndSourceChannelAndAccountIdAndCreditStatus(userId, sourceChannel, accountId);
    }

    /**
     * 查询状态是持有中的债权
     * @param userId
     * @param sourceChannels
     * @param creditStatus
     * @return
     */
    public List<Credit> findByUserIdAndChannelsInAndStatusForRepay(String userId,Set<Integer> sourceChannels,Integer creditStatus){
        if(StringUtils.isBlank(userId) || sourceChannels==null || creditStatus==null){
            throw  new IllegalArgumentException("param can not be null");
        }
        return creditDao.findByUserIdAndChannelsAndCreditStatus(userId,sourceChannels,creditStatus);
    }

    //月月盈转投省心投债权转让转让
    public List<CreditOpening> creditTransferToYjt(Map<Credit, String> paramMap, BigDecimal transferDiscount,Integer iplanId) {
        logger.info("开始调用债权转让方法->输入参数:creditId={},转让份数={},折让率={}", Arrays.toString(paramMap.keySet().stream().map(Credit::getId).toArray())
                , Arrays.toString(paramMap.entrySet().stream().map(Map.Entry::getValue).toArray()), transferDiscount);

        RequestIntelligentProjectDebentureSale request = new RequestIntelligentProjectDebentureSale();
        List<RequestIntelligentProjectDebentureSale.Detail> details = new ArrayList<>(paramMap.size());
        request.setRequestNo(IdUtil.getRequestNo());
        request.setDetails(details);
        request.setTransCode(TransCode.CREDIT_TRANSFER.getCode());

        List<CreditOpening> creditOpenings = new ArrayList<>(paramMap.size());
        for (Map.Entry<Credit, String> entry : paramMap.entrySet()) {
            Credit credit = creditDao.findByIdForUpdate(entry.getKey().getId());
            Integer transferPrincipal = credit.getHoldingPrincipal();
            List<SubjectRepaySchedule> subjectRepaySchedules = repayScheduleService.findRepayScheduleBySubjectId(credit.getSubjectId());
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

            RequestIntelligentProjectDebentureSale.Detail detail = new RequestIntelligentProjectDebentureSale.Detail();
            detail.setSaleRequestNo(IdUtil.getRequestNo());
            detail.setIntelRequestNo(entry.getValue());
            detail.setPlatformUserNo(credit.getUserIdXM());
            detail.setProjectNo(credit.getSubjectId());
            detail.setSaleShare(transferPrincipal/100.0);
            details.add(detail);

            //保存开放中的债权数据 再进行调用
            CreditOpening creditOpening = saveOpeningCreditNew(transferDiscount, credit, detail.getSaleRequestNo(),transferPrincipal,iplanId);
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
        for (Map.Entry<Credit, String> entry : paramMap.entrySet()) {
            Credit credit = entry.getKey();
            //更新所有的持有中的债权份数
            credit.setHoldingPrincipal(0);
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
                creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_YJT);
                creditOpening.setOpenTime(DateUtil.getCurrentDateTime());
                creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
            }
            creditOpeningDao.update(creditOpening);
        }

        return creditOpenings;
    }

    private CreditOpening saveOpeningCreditNew(BigDecimal transferDiscount, Credit credit, String extSn, Integer transferPrincipal, Integer iplanId) {
        CreditOpening creditOpening = new CreditOpening();
        creditOpening.setCreditId(credit.getId());
        creditOpening.setSubjectId(credit.getSubjectId());
        creditOpening.setTransferorId(credit.getUserId());
        creditOpening.setTransferorIdXM(credit.getUserIdXM());
        creditOpening.setTransferDiscount(transferDiscount);
        creditOpening.setSourceChannel(credit.getSourceChannel());
        creditOpening.setPublishTime(DateUtil.getCurrentDateTime());
        creditOpening.setSourceAccountId(credit.getSourceAccountId());
        creditOpening.setIplanId(iplanId);
        creditOpening.setEndTime(credit.getEndTime());
        creditOpening.setOpenFlag(CreditOpening.OPEN_FLAG_OFF);
        creditOpening.setAvailablePrincipal(transferPrincipal);
        creditOpening.setTransferPrincipal(transferPrincipal);
        creditOpening.setPackPrincipal(transferPrincipal);
        creditOpening.setExtStatus(BaseResponse.STATUS_PENDING);
        creditOpening.setExtSn(extSn);
        creditOpening.setCreateTime(DateUtil.getCurrentDateTime19());
        creditOpeningDao.insert(creditOpening);
        return creditOpening;
    }

    /**
     * 设置一键投开放渠道
     *
     * @param creditOpening 开放中债权
     */
    private void setYjtOpenChannel(CreditOpening creditOpening) {
        int openChannel = GlobalConfig.OPEN_TO_YJT;
        Config config = configDao.getConfigById(Config.IF_IPLAN_CREDIT_TO_MARKET);
        if (config != null) {
            String value = config.getValue();
            if (StringUtils.isNotBlank(value)) {
                int ifToMarket = 0;
                try {
                    ifToMarket = Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    logger.error("解析省心投债转是否开放到债权市场配置失败", e);
                }
                if (ifToMarket == 1 || creditOpening.getTransferDiscount().intValue() != 1) {
                    openChannel = GlobalConfig.OPEN_TO_SUBJECT;
                }
            }
        }
        creditOpening.setOpenChannel(openChannel);
    }
}
