package com.jiuyi.ndr.service.credit;

import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.dao.iplan.IPlanCreditMergeDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlanCreditMerge;
import com.jiuyi.ndr.domain.subject.SubjectRepayDetail;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestIntelligentProjectDebentureSale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author 姜广兴
 * @date 2018-04-20
 */
@Service
public class IPlanCreditMergeService {
    private static final Logger logger = LoggerFactory.getLogger(IPlanCreditMergeService.class);

    @Autowired
    private CreditDao creditDao;
    @Autowired
    private CreditOpeningDao creditOpeningDao;
    @Autowired
    private IPlanAccountDao iPlanAccountDao;
    @Autowired
    private SubjectRepayDetailDao subjectRepayDetailDao;
    @Autowired
    private IPlanCreditMergeDao iPlanCreditMergeDao;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private SubjectRepayScheduleService repayScheduleService;

    /**
     * 月月盈债权金额小于100债权合并
     *
     * @param subjectId
     */
    public void iPlanCreditMerge(String subjectId) {
        //查询待合并记录
        Optional<IPlanCreditMerge> optionalIPlanCreditMerge = this.getByStatus(IPlanCreditMerge.MergeStatus.STATUS_NOT_DEAL.getCode());
        //如果IPlanCreditMerge不为null
        optionalIPlanCreditMerge.ifPresent(iPlanCreditMerge -> {
            logger.info("[{subjectId}]债权合并开始.", subjectId);
            //根据subjectId查询今日的还款计划
            List<SubjectRepaySchedule> repaySchedules = repayScheduleService.findRepayScheduleBySubjectId(subjectId);
            if (CollectionUtils.isEmpty(repaySchedules)) {
                logger.error("标的[{}]没有还款计划，不能合并", subjectId);
                return;
            }
            Optional<SubjectRepaySchedule> optionalSubjectRepaySchedule = repaySchedules.parallelStream().filter(src -> DateUtil.getCurrentDateShort().equals(src.getDueDate())).findFirst();
            optionalSubjectRepaySchedule.ifPresent(subjectRepaySchedule -> {
                //根据还款计划id查询所有还款明细
                List<SubjectRepayDetail> subjectRepayDetails = subjectRepayDetailDao.findByScheduleId(subjectRepaySchedule.getId());
                if (subjectRepayDetails.parallelStream().anyMatch(subjectRepayDetail ->
                        !(SubjectRepayDetail.STATUS_REPAID.equals(subjectRepayDetail.getStatus())
                                && SubjectRepayDetail.STEP_HAS_DEAL_BORROWER.equals(subjectRepayDetail.getCurrentStep())))) {
                    //如果所有还款明细中有状态不是已还（1）或处理步骤不是处理完成（3），说明有还款未完成
                    logger.warn("标的[{}]今日有还款计划未还款，不能合并", subjectId);
                    return;
                }
            });

            //查询标的下所有待合并债权并加锁
            List<Credit> credits = creditDao.findNeedMergeCreditsForUpdate(GlobalConfig.CREDIT_AVAIABLE, subjectId);

            Map<Credit, String> paramMap = new HashMap<>(credits.size());
            Integer totalAmt = 0;
            for (Credit credit : credits) {
                String investRequestNo = iPlanAccountDao.findById(credit.getSourceAccountId()).getInvestRequestNo();
                totalAmt += credit.getHoldingPrincipal();
                paramMap.put(credit, investRequestNo);
            }

            //转让债权
            if (this.creditMerge(paramMap, BigDecimal.ONE)) {
                int processedAmt = totalAmt + iPlanCreditMerge.getProcessedAmt();
                //更新已处理金额
                iPlanCreditMerge.setProcessedAmt(processedAmt);
                //如果已处理金额超过总金额，更新状态为已处理
                if (processedAmt >= iPlanCreditMerge.getTotalAmt()) {
                    iPlanCreditMerge.setStatus(IPlanCreditMerge.MergeStatus.STATUS_DEALED.getCode());
                }
                iPlanCreditMergeDao.update(iPlanCreditMerge);
                logger.info("[{subjectId}]债权合并成功.", subjectId);
            }
        });
    }

    /**
     * 根据状态查询待合并记录
     *
     * @param status
     * @return
     */
    public Optional<IPlanCreditMerge> getByStatus(int status) {
        return Optional.ofNullable(iPlanCreditMergeDao.getByStautsForUpdate(status));
    }

    public boolean creditMerge(Map<Credit, String> paramMap, BigDecimal transferDiscount) {
        logger.info("开始调用债权转让方法->输入参数:creditId={},转让份数={},折让率={}", Arrays.toString(paramMap.keySet().stream().map(Credit::getId).toArray())
                , Arrays.toString(paramMap.entrySet().stream().map(Map.Entry::getValue).toArray()), transferDiscount);

        RequestIntelligentProjectDebentureSale request = new RequestIntelligentProjectDebentureSale();
        List<RequestIntelligentProjectDebentureSale.Detail> details = new ArrayList<>(paramMap.size());
        request.setRequestNo(IdUtil.getRequestNo());
        request.setDetails(details);
        request.setTransCode(TransCode.CREDIT_TRANSFER.getCode());

        List<CreditOpening> creditOpenings = new ArrayList<>(paramMap.size());
        for (Map.Entry<Credit, String> entry : paramMap.entrySet()) {
            Credit credit = entry.getKey();
            Integer transferPrincipal = credit.getHoldingPrincipal();
            List<SubjectRepaySchedule> subjectRepaySchedules = repayScheduleService.findRepayScheduleBySubjectId(credit.getSubjectId());
            if (subjectRepaySchedules == null || subjectRepaySchedules.isEmpty()) {
                logger.error("查询不到该标的下的还款计划,subjectId=" + credit.getSubjectId());
                return false;
            }
            //判断还款计划中是否有逾期 逾期不能进行债转
            if (subjectRepaySchedules.stream().anyMatch(
                    subjectRepaySchedule -> SubjectRepaySchedule.STATUS_OVERDUE.equals(subjectRepaySchedule.getStatus())
            )) {
                logger.warn("要债转的债权中存在逾期标的，不能进行债转");
                return false;
            }

            RequestIntelligentProjectDebentureSale.Detail detail = new RequestIntelligentProjectDebentureSale.Detail();
            detail.setSaleRequestNo(IdUtil.getRequestNo());
            detail.setIntelRequestNo(entry.getValue());
            detail.setPlatformUserNo(credit.getUserIdXM());
            detail.setProjectNo(credit.getSubjectId());
            detail.setSaleShare(transferPrincipal / 100.0);
            details.add(detail);

            //保存开放中的债权数据 再进行调用
            CreditOpening creditOpening = saveOpeningCreditNew(transferDiscount, credit, detail.getSaleRequestNo(), transferPrincipal);
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
                creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_IPLAN | GlobalConfig.OPEN_TO_LPLAN);
                creditOpening.setOpenTime(DateUtil.getCurrentDateTime());
                creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
            }
            creditOpeningDao.update(creditOpening);
        }

        return true;
    }

    private CreditOpening saveOpeningCreditNew(BigDecimal transferDiscount, Credit credit, String extSn, Integer transferPrincipal) {
        CreditOpening creditOpening = new CreditOpening();
        creditOpening.setCreditId(credit.getId());
        creditOpening.setSubjectId(credit.getSubjectId());
        creditOpening.setTransferorId(credit.getUserId());
        creditOpening.setTransferorIdXM(credit.getUserIdXM());
        creditOpening.setTransferDiscount(transferDiscount);
        creditOpening.setSourceChannel(credit.getSourceChannel());
        creditOpening.setPublishTime(DateUtil.getCurrentDateTime());
        creditOpening.setSourceAccountId(credit.getSourceAccountId());
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
}
