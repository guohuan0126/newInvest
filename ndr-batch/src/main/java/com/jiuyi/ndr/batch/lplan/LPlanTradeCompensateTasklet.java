package com.jiuyi.ndr.batch.lplan;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.xm.TransLogDao;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.domain.xm.TransLog;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestIntelligentProjectDebentureSale;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.query.DebentureSaleQueryRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author  by zhangyibo on 2017/5/3.
 */
public class LPlanTradeCompensateTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(LPlanTradeCompensateTasklet.class);
    @Autowired
    private TransLogDao transLogDao;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private CreditOpeningDao creditOpeningDao;
    @Autowired
    private ConfigDao configDao;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectRepayScheduleService repayScheduleService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        creditTransferConfirm();
        return RepeatStatus.FINISHED;
    }

    /**
     * 债权转出确认
     */
    private void creditTransferConfirm(){
        List<TransLog> transLogListCredit = transLogDao.findByTransCodeAndStatus(TransCode.CREDIT_TRANSFER.getCode(),TransLog.STATUS_PENDING);
        for (TransLog transLog:transLogListCredit){
            logger.info("{}正在进行债权转出补偿{}",transLog.getTxnSn());
            //根据transLog查询到这次请求发送过去的开放中的债权
            RequestIntelligentProjectDebentureSale lastRequest = JSON.parseObject(transLog.getRequestPacket(),RequestIntelligentProjectDebentureSale.class);
            List<RequestIntelligentProjectDebentureSale.Detail> details = lastRequest.getDetails();
            List<CreditOpening> creditOpenings = creditOpeningDao.findByExtSnIn(details.stream().map(RequestIntelligentProjectDebentureSale.Detail::getSaleRequestNo).collect(Collectors.toList()));

            ResponseSingleTransQuery response = null;
            if(!creditOpenings.isEmpty()){
                logger.info("用于查询债权是否转让成功的流水号={}",details.get(0).getSaleRequestNo());
                //取此次批量请求中的任意一个进行查询（只要一个成功 表示此次批量交易中的其他交易都成功了） 要取saleRequestNo进行查询
                response = transactionService.singleTransQuery(constructRequest(details.get(0).getSaleRequestNo(),TransactionType.DEBENTURE_SALE));
            } else {
                continue;
            }

            if(BaseResponse.STATUS_SUCCEED.equals(response.getStatus())){
                DebentureSaleQueryRecord record = (DebentureSaleQueryRecord) response.getRecords().get(0);
                if("ONSALE".equals(record.getStatus())){
                    transLog.setStatus(BaseResponse.STATUS_SUCCEED);
                    transLogDao.update(transLog);
                    updateCreditOpening(creditOpenings);
                }else if("FAIL".equals(record.getStatus())){
                    transLog.setStatus(BaseResponse.STATUS_FAILED);
                    transLogDao.update(transLog);
                }
            }else if(BaseResponse.STATUS_FAILED.equals(response.getStatus())){
                //如果是因为没有请求到厦门银行而导致的失败
                if(GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(response.getCode())){
                    transLog.setStatus(BaseResponse.STATUS_FAILED);
                    transLogDao.update(transLog);
                    if(details.size() <= 2999){
                        RequestIntelligentProjectDebentureSale request = lastRequest;
                        logger.info("{}正在进行债权转出补偿重试,重试报文为:{}",transLog.getTxnSn(),JSON.toJSONString(request));
                        BaseResponse retryResponse = transactionService.intelligentProjectDebentureSale(request);
                        logger.info("{}债权转出补偿重试结果为:{}",transLog.getTxnSn(),JSON.toJSONString(retryResponse));
                        if(BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())){
                            updateCreditOpening(creditOpenings);
                        }
                    }else{
                        List<List<RequestIntelligentProjectDebentureSale.Detail>> parts = Lists.partition(details, 2999);
                        creditTransfer(parts);
                    }
                }else{
                    logger.error("债权出让失败->厦门银行返回:{},transLog:{}",JSON.toJSONString(response),JSON.toJSONString(transLog));
                }
            }
        }
    }

    private void creditTransfer(List<List<RequestIntelligentProjectDebentureSale.Detail>> parts) {
        for (List<RequestIntelligentProjectDebentureSale.Detail> part : parts) {
            RequestIntelligentProjectDebentureSale request = new RequestIntelligentProjectDebentureSale();
            request.setRequestNo(IdUtil.getRequestNo());
            request.setDetails(part);
            request.setTransCode(TransCode.CREDIT_TRANSFER.getCode());
            logger.info("正在进行债权转出补偿拆分,拆分报文为:{}",JSON.toJSONString(request));
            BaseResponse retryResponse = transactionService.intelligentProjectDebentureSale(request);
            logger.info("债权转出拆分报文请求结果为:{}",JSON.toJSONString(retryResponse));
            if(BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())){
                List<CreditOpening> partCreditOpenings = creditOpeningDao.findByExtSnIn(part.stream().map(RequestIntelligentProjectDebentureSale.Detail::getSaleRequestNo).collect(Collectors.toList()));
                updateCreditOpening(partCreditOpenings);
            }
        }
    }

    private void updateCreditOpening(List<CreditOpening> creditOpenings) {
        for(CreditOpening creditOpening:creditOpenings){
            creditOpening.setExtStatus(BaseResponse.STATUS_SUCCEED);
            creditOpening.setStatus(CreditOpening.STATUS_OPENING);
            //如果此次调用成功 需要将开放标识改为开放
            creditOpening.setOpenFlag(CreditOpening.OPEN_FLAG_ON);
            if(creditOpening.getIplanId() != null){
               creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_YJT);
            }else {
                //默认定期活期同时开放
                if(CreditOpening.SOURCE_CHANNEL_IPLAN == creditOpening.getSourceChannel() || CreditOpening.SOURCE_CHANNEL_LPLAN == creditOpening.getSourceChannel()){
                    if(creditOpening.getCreditId() != null){
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
                        }else{
                            creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_IPLAN|GlobalConfig.OPEN_TO_LPLAN);
                        }
                    } else {
                        creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_IPLAN|GlobalConfig.OPEN_TO_LPLAN);
                    }
                    //todo 如果债权来源是一键投，根据配置设置开放渠道(随心投修改-jgx-5.16)
                } else if (CreditOpening.SOURCE_CHANNEL_YJT == creditOpening.getSourceChannel()) {
                    this.setYjtOpenChannel(creditOpening);
                } else {
                    creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_SUBJECT);
                }
            }
            creditOpening.setOpenTime(DateUtil.getCurrentDateTime());
            creditOpeningDao.update(creditOpening);
        }
    }

    private RequestSingleTransQuery constructRequest(String requestNo, TransactionType transactionType) {
        RequestSingleTransQuery request = new RequestSingleTransQuery();
        request.setTransactionType(transactionType);
        request.setRequestNo(requestNo);
        return request;
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
                if (ifToMarket == 1  || creditOpening.getTransferDiscount().intValue() != 1) {
                    openChannel = GlobalConfig.OPEN_TO_SUBJECT;
                }
            }
        }
        creditOpening.setOpenChannel(openChannel);
    }
}
