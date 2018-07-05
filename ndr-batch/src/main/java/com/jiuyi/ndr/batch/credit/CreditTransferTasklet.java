package com.jiuyi.ndr.batch.credit;

import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.xm.TransLogDao;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.xm.TransLog;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestDebentureSale;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.query.DebentureSaleQueryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CreditTransferTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(CreditTransferTasklet.class);

    @Autowired
    private TransLogDao transLogDao;

    @Autowired
    private CreditOpeningDao creditOpeningDao;

    @Autowired
    private TransactionService transactionService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("正在进行债权转出补偿{}");
        creditTransferConfirm();
        return RepeatStatus.FINISHED;
    }

    /**
     * 债权转出确认
     */
    private void creditTransferConfirm(){
        List<TransLog> transLogListCredit = transLogDao.findByTransCodeAndStatus(TransCode.SUBJECT_CREDIT_TRANSFER.getCode(),TransLog.STATUS_PENDING);
        for (TransLog transLog:transLogListCredit){
            logger.info("正在进行债权转出补偿{}",transLog.getTxnSn());
//            noticeService.send("18519178873",transLog.getTxnSn()+"正在进行债权转出补偿",null,null);
            //根据transLog查询到这次请求发送过去的开放中的债权
            RequestDebentureSale request = JSON.parseObject(transLog.getRequestPacket(), RequestDebentureSale.class);
            CreditOpening creditOpening = creditOpeningDao.findByExtSn(request.getRequestNo());

            ResponseSingleTransQuery response = null;
            if(creditOpening != null){
                logger.info("用于查询债权是否转让成功的流水号={}",request.getRequestNo());
                //取此次批量请求中的任意一个进行查询（只要一个成功 表示此次批量交易中的其他交易都成功了） 要取saleRequestNo进行查询
                response = transactionService.singleTransQuery(constructRequest(request.getRequestNo(), TransactionType.DEBENTURE_SALE));
            }
            if(response != null){
                if(BaseResponse.STATUS_SUCCEED.equals(response.getStatus())){
                    DebentureSaleQueryRecord record = (DebentureSaleQueryRecord) response.getRecords().get(0);
                    if("ONSALE".equals(record.getStatus())){
                        transLog.setStatus(BaseResponse.STATUS_SUCCEED);
                        transLogDao.update(transLog);
                        updateCreditOpening(creditOpening);
                    }else if("FAIL".equals(record.getStatus())){
                        transLog.setStatus(BaseResponse.STATUS_FAILED);
                        transLogDao.update(transLog);
                    }
                }else if(BaseResponse.STATUS_FAILED.equals(response.getStatus())){
                    //如果是因为没有请求到厦门银行而导致的失败
                    if(GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(response.getCode())){
                        transLog.setStatus(BaseResponse.STATUS_FAILED);
                        transLogDao.update(transLog);
                        RequestDebentureSale retryRequest = request;
                        logger.info("{}正在进行债权转出补偿重试,重试报文为:{}",transLog.getTxnSn(),JSON.toJSONString(retryRequest));
                        BaseResponse retryResponse = transactionService.debentureSale(retryRequest);
                        logger.info("{}债权转出补偿重试结果为:{}",transLog.getTxnSn(),JSON.toJSONString(retryResponse));
                        if(BaseResponse.STATUS_SUCCEED.equals(retryResponse.getStatus())){
                            updateCreditOpening(creditOpening);
                        }
                    }else{
                        logger.error("债权出让失败->厦门银行返回:{},transLog:{}",JSON.toJSONString(response),JSON.toJSONString(transLog));
                    }
                }
            }
        }
    }

    private void updateCreditOpening(CreditOpening creditOpening) {
            creditOpening.setExtStatus(BaseResponse.STATUS_SUCCEED);
            creditOpening.setStatus(CreditOpening.STATUS_OPENING);
            //如果此次调用成功 需要将开放标识改为开放
            creditOpening.setOpenFlag(CreditOpening.OPEN_FLAG_ON);
            creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_SUBJECT);
            creditOpening.setOpenTime(DateUtil.getCurrentDateTime());
            creditOpeningDao.update(creditOpening);
    }

    private RequestSingleTransQuery constructRequest(String requestNo, TransactionType transactionType) {
        RequestSingleTransQuery request = new RequestSingleTransQuery();
        request.setTransactionType(transactionType);
        request.setRequestNo(requestNo);
        return request;
    }
}
