package com.jiuyi.ndr;

import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.service.autoinvest.AutoInvestService;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestCancelDebentureSale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@ActiveProfiles("dev01")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ServiceApplication.class)
public class SubjectCreditTransferTest {

    @Autowired
    SubjectAccountService subjectAccountService;

    @Autowired
    CreditOpeningDao creditOpeningDao;

    @Autowired
    TransactionService transactionService;
    @Autowired
    AutoInvestService autoInvestService;

    @Autowired

    @Test
    public void subjectCreditTransferTest(){
        Integer subjectTransLogId = 11;
        int amount = 500;
        BigDecimal transferDiscount = new BigDecimal("0.005");
        String device = "pc";
        subjectAccountService.subjectCreditTransfer(subjectTransLogId,50000,transferDiscount,device);
    }

    @Test
    public void  cancelCreditTransferTest(){
        CreditOpening creditOpening = creditOpeningDao.findById(9);
        subjectAccountService.cancelCreditTransfer(creditOpening.getId());
    }

    @Test
    public void  cancelCreditTransferTestNew(){

        RequestCancelDebentureSale request = new RequestCancelDebentureSale();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setTransCode(TransCode.CREDIT_CANCEL.getCode());
        request.setCreditsaleRequestNo("NDR2018012812280781cda1");
        BaseResponse baseResponse = null;
        try {
           // logger.info("开始调用厦门银行取消债权出让接口->{}", JSON.toJSONString(request));
            baseResponse = transactionService.cancelDebentureSale(request);
            //logger.info("取消债权出让接口返回->{}", JSON.toJSONString(baseResponse));
        } catch (Exception e) {
            if (baseResponse == null) {
                baseResponse = new BaseResponse();
            }
            baseResponse.setStatus(BaseResponse.STATUS_PENDING);
        }
    }

    /**
     * 散标购买测试
     */
    @Test
    public  void  testSubjectInvest(){
        subjectAccountService.investSubject("7VnEj2FFrQNblpqz","NDR171028150035000001",100000,
                127484,"PC",0);

    }
    /**
     * 散标债权购买测试
     */
    @Test
    public void testCreditInvest(){
//        subjectAccountService.investSubjectCredit(10,10000,9950,"BN7VZjYvqiuenxwn",
//                0,17,"pc");
    }
    @Test
    public void testTime(){
        String time="20171028 15:09:02";
        String openTime=DateUtil.parseDateTime(time, DateUtil.DATE_TIME_FORMATTER_17).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    }
    /**
     * 散标流标测试
     */
    @Test
    public void testInvestCancel(){
        autoInvestService.subjectInvestCancel(316);
    }
    }
