package com.jiuyi.ndr;

import com.alibaba.fastjson.JSON;
import com.duanrong.util.InterestUtil;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.service.autoinvest.AutoInvestService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.notice.support.TemplateId;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestCancelPreTransaction;
import com.jiuyi.ndr.xm.http.request.RequestIntelligentProjectDebentureSale;
import com.jiuyi.ndr.xm.http.request.RequestPurchaseIntelligentProject;
import com.jiuyi.ndr.xm.http.request.RequestSingleTrans;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhangyibo on 2017/6/8.
 */
@ActiveProfiles("dev01")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SubjectDaoTest {

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private SubjectService subjectService;
    @Autowired
    AutoInvestService autoInvestService;
    @Autowired
    RedPacketService redPacketService;
    @Autowired
    NoticeService noticeService;
    @Autowired
    TransactionService transactionService;

    @Test
    public void testfindOneForUpdate(){
        subjectService.findById(1);
    }

    @Transactional
    @Test
    public void testfindOneForUpdate2(){
        Subject subject = subjectDao.findBySubjectId("");
        System.out.println(1);
    }

    /*@Test
    public void testFindList() {
        List<Subject> subjects = subjectDao.findInvestable("02", new Integer[]{2, 3, 6, 7});
        System.out.println("subjects = " + subjects);
    }*/

    @Test
    public void testFindBySubjectIds(){
        List<Subject> subjects = subjectDao.findBySubjectIds(Arrays.asList("1","2","3"));
        subjects.stream().forEach(System.out::println);
    }
    @Test
    public void subjectRepay(){
        FinanceCalcUtils.CalcResult calcResult = null;
            calcResult = FinanceCalcUtils.calcMCEI(2000000, BigDecimal.valueOf(0.144), 12);
        calcResult = FinanceCalcUtils.calcOTRP(2000000, BigDecimal.valueOf(0.144), 10);
//        int totalInterest=0;
//        for (int m = 1; m <= 12; m++) {
//            FinanceCalcUtils.CalcResult.Detail detail = calcResult.getDetails().get(m);
//            int interest=0;
//            if(m==1)
//            {
//                 interest= (int)(2000000*0.08/12);
//            }
//            else {
//                interest = (int) (calcResult.getDetails().get(m - 1).getRemainPrincipal() * 0.08 / 12);
//            }
//            totalInterest+=interest;
//            System.out.print("期数"+detail.getTerm()+"--本金："+detail.getMonthRepayPrincipal()+"--利息："+detail.getMonthRepayInterest()
//            +"--本息和："+detail.getMonthRepay()+"--剩余应还本金："+detail.getRemainPrincipal()+"月还利息"+interest+"</br>");
//        }
//        System.out.print("总利息"+totalInterest);
        System.out.print("总利息"+calcResult.getTotalRepayInterest());

    }
    @Test
    public void getRepayType(){
       // double repayInterest=subjectService.getInterestByRepayType(555500,BigDecimal.valueOf(0.084),BigDecimal.valueOf(0.144),
       //         3,90,"MCEI");
        double repayInterest=subjectService.getInterestByRepayType(1000000,BigDecimal.valueOf(0.11),BigDecimal.valueOf(0.144),
                18,90,"MCEI");
        System.out.println("总利息"+repayInterest);
        System.out.println("总利息"+repayInterest*100);
        System.out.println("总利息"+(int)(repayInterest*100));
        FinanceCalcUtils.CalcResult calcResult = null;
        calcResult = FinanceCalcUtils.calcMCEISubject(1000000, BigDecimal.valueOf(0.144), 18);
          double totalInterest=0;
        for (int m = 1; m <= 18; m++) {
            FinanceCalcUtils.CalcResult.Detail detail = calcResult.getDetails().get(m);
            double interest=0;
            if(m==1)
            {
                 interest=ArithUtil.roundDown(1000000*0.11/12, 2);
            }
            else {
                interest = ArithUtil.roundDown(calcResult.getDetails().get(m - 1).getRemainPrincipal() * 0.11 / 12, 2);
            }
            totalInterest+=interest;
            System.out.println("期数"+detail.getTerm()+"--本金："+detail.getMonthRepayPrincipal()+"--利息："+detail.getMonthRepayInterest()
            +"--本息和："+detail.getMonthRepay()+"--剩余应还本金："+detail.getRemainPrincipal()+"月还利息"+interest+"</br>");
        }
        System.out.print("总利息"+totalInterest);
    }

    @Test
    public void testSchedule(){
        double otrp = subjectService.getInterestByRepayType(6900000, new BigDecimal("0.08"), new BigDecimal("0.144"), 1, 30, "OTRP");
        System.out.println(otrp);
    }


    @Test
    public void packetTest(){
        double d= InterestUtil.getInterestByPeriodMoth(20000,0.1,3,"等额本息");
        System.out.print("总利息"+d);
    }
    @Test
    public void autoInvestTest(){
        autoInvestService.autoInvestSubject("NDR171117173954000005");

    }
    @Test
    public  void  notice(){
        String smsTemplate = TemplateId.SUBJECT_INVEST_SUCCEED;
        noticeService.send("15071347862", "月月盈测试1"+","
                + String.valueOf(500)+","+String.valueOf(490)+","+20+","+String.valueOf(10.6), smsTemplate);

    }
    @Test
    public void liubiao(){
        String requestNo = IdUtil.getRequestNo();
        //预处理取消接口
        RequestCancelPreTransaction request = new RequestCancelPreTransaction();
        request.setRequestNo(requestNo);
        request.setPreTransactionNo("NDR20171202144629359a67");
        request.setAmount(300.0);
        //调用资金解冻接口，解冻资金
        BaseResponse response = transactionService.cancelPreTransaction(request);
        System.out.println(request.toString());
        System.out.println(response.toString());
        System.out.println(response.getStatus());
    }
    @Test
    public void purchase(){
        String requestNo = IdUtil.getRequestNo();
        //首次投资
        RequestPurchaseIntelligentProject request = new RequestPurchaseIntelligentProject();
        request.setTransCode(TransCode.IPLAN_INVEST_FREEZE.getCode());
        request.setIntelProjectNo("IPlan170921170633256");
        request.setPlatformUserNo("vI3yArq6VzUvtjlb");
        request.setRequestNo(requestNo);
        request.setAmount(1000.0);
        //1. 批量投标请求
        BaseResponse baseResponse = transactionService.purchaseIntelligentProject(request);
        System.out.println(request.toString());
        System.out.println(baseResponse.toString());
        System.out.println(baseResponse.getStatus());

    }

    @Test
    public void testIplan(){

        RequestSingleTrans request = new RequestSingleTrans();
        request.setTransCode(TransCode.IPLAN_INVEST_FREEZE.getCode());
        request.setRequestNo(IdUtil.getRequestNo());
        request.setTradeType(TradeType.INTELLIGENT_APPEND);
        RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
        detail.setBizType(BizType.APPEND_FREEZE);
        detail.setSourcePlatformUserNo("aQVzemZBzMBzoilb");
        detail.setFreezeRequestNo("NDR20170921234928cfd796");
        detail.setAmount(2.0);
        List<RequestSingleTrans.Detail> details = new ArrayList<>();
        details.add(detail);
        request.setDetails(details);
        BaseResponse response = transactionService.singleTrans(request);
        System.out.println(response.toString());
    }
    @Test
    public void testAutoInvest(){
        Subject subject = subjectService.getBySubjectId("NDR171218120418000003");
        RedPacket redPacket=  redPacketService.getBestRedPacketSubject("neyARnRZFjeaijwb", subject, 50000.0, "2");
        System.out.println(redPacket.toString());
       /* List<RedPacket> reList= redPacketService.getUsablePacketSubject("neyARnRZFjeaijwb", 50000.0, subject);
        System.out.println(Arrays.toString(reList.toArray()));*/
    }

    @Test
    public void testCreditTransfer() {
        RequestIntelligentProjectDebentureSale request = new RequestIntelligentProjectDebentureSale();
        List<RequestIntelligentProjectDebentureSale.Detail> details = new ArrayList<>();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setDetails(details);
        request.setTransCode(TransCode.CREDIT_TRANSFER.getCode());
        RequestIntelligentProjectDebentureSale.Detail detail = new RequestIntelligentProjectDebentureSale.Detail();
        detail.setSaleRequestNo(IdUtil.getRequestNo());
        detail.setIntelRequestNo("NDR201712221233254a30bc");
        detail.setPlatformUserNo("yA3QJrjIVjyalvie");
        detail.setProjectNo(String.valueOf("NDR170602181436000913"));
        detail.setSaleShare(18.32);
        details.add(detail);
        //调用厦门银行债权出让接口
        BaseResponse baseResponse = null;
        try {
            //存在转让出的债权为0的情况
            if (details.size() > 0) {
                System.out.println("开始调用厦门银行批量债权出让接口->{}" + JSON.toJSONString(request));
                baseResponse = transactionService.intelligentProjectDebentureSale(request);
                System.out.println("批量债权出让接口返回->{}" + JSON.toJSONString(baseResponse));
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
    }
}
