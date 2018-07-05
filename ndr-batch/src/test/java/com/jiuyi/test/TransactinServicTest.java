package com.jiuyi.test;

import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.BatchApplication;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.xm.TransLogDao;
import com.jiuyi.ndr.domain.xm.TransLog;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestCancelDebentureSale;
import com.jiuyi.ndr.xm.http.request.RequestIntelligentProjectDebentureSale;
import com.jiuyi.ndr.xm.http.request.RequestSingleTrans;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = BatchApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(profiles = "dev01")
public class TransactinServicTest {

    @Autowired
    TransactionService transactionService;

    @Autowired
    TransLogDao transLogDao;



    @Test
    public void test(){
        RequestSingleTrans compensateRequest = new RequestSingleTrans();
        compensateRequest.setRequestNo(IdUtil.getRequestNo());
        compensateRequest.setTransCode(TransCode.CREDIT_LEND_COMPENSATE.getCode());
        compensateRequest.setTradeType(TradeType.MARKETING);
        List<RequestSingleTrans.Detail> compensateRequestDetails = new ArrayList<>(1);
        RequestSingleTrans.Detail compensateRequestDetail = new RequestSingleTrans.Detail();
        compensateRequestDetail.setBizType(BizType.MARKETING);
        compensateRequestDetail.setAmount(5003 / 100.0);
        compensateRequestDetail.setSourcePlatformUserNo(GlobalConfig.MARKETING_ACCOUNT_XM);
        compensateRequestDetail.setTargetPlatformUserNo("AJJj2iV32aM3ynvb");
        compensateRequestDetails.add(compensateRequestDetail);
        compensateRequest.setDetails(compensateRequestDetails);
        transactionService.singleTrans(compensateRequest);
    }

    @Test
    public  void test1(){
        List<Integer> list = new ArrayList<>(1000);
        for (int i = 0; i < 1000 ; i++){
            list.add(i);
        }
        list.parallelStream().forEach(this::send);
    }

    private void send(Integer integer) {
        RequestSingleTransQuery request = new RequestSingleTransQuery();
        request.setRequestNo("NDR2018050516302356a3a2");
        request.setTransactionType(TransactionType.PRETRANSACTION);
        ResponseSingleTransQuery singleTransQuery = transactionService.singleTransQuery(request);
        System.out.println("处理到第"+integer+"条");
    }
    @Test
    public void freezeMoney(){
       /* FreezeMoney freezeMoney = freezeMoneyDao.findById(1);
        RequestCancelPreTransaction request = new RequestCancelPreTransaction();
        request.setPreTransactionNo(freezeMoney.getInvestRequestNo());
        request.setAmount(freezeMoney.getMoney());
        BaseResponse baseResponse = transactionService.cancelPreTransaction(request);
        System.out.println(baseResponse);
        if (BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())){
            freezeMoney.setStatus(1);
            freezeMoneyDao.update(freezeMoney);
        }*/
    }

    @Test
    public  void cancel(){
        TransLog transLog = transLogDao.findById(6684);
        RequestIntelligentProjectDebentureSale request = JSON.parseObject(transLog.getRequestPacket(), RequestIntelligentProjectDebentureSale.class);
        List<RequestIntelligentProjectDebentureSale.Detail> details = request.getDetails();
        for (RequestIntelligentProjectDebentureSale.Detail detail : details) {
            System.out.println(detail.getSaleRequestNo());
            RequestCancelDebentureSale req = new RequestCancelDebentureSale();
            req.setRequestNo(IdUtil.getRequestNo());
            req.setCreditsaleRequestNo(detail.getSaleRequestNo());
            req.setTransCode(TransCode.CREDIT_CANCEL.getCode());

            BaseResponse baseResponse = null;
            try {
                //logger.info("开始调用厦门银行取消债权出让接口->{}", JSON.toJSONString(request));
                baseResponse = transactionService.cancelDebentureSale(req);
                //logger.info("取消债权出让接口返回->{}", JSON.toJSONString(baseResponse));
                System.out.println(baseResponse.toString());
            } catch (Exception e) {
                if (baseResponse == null) {
                    baseResponse = new BaseResponse();
                }
                baseResponse.setStatus(BaseResponse.STATUS_PENDING);
            }
        }
    }

}
