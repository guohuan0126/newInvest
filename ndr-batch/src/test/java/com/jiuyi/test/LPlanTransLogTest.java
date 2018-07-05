package com.jiuyi.test;

import com.jiuyi.ndr.BatchApplication;
import com.jiuyi.ndr.constant.CheckFileConsts;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.guohuanCreditDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.dao.lplan.LPlanAccountDao;
import com.jiuyi.ndr.dao.lplan.LPlanTransLogDao;
import com.jiuyi.ndr.dao.xm.TransactionDetailDao;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.guohuanCredit;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.lplan.LPlanAccount;
import com.jiuyi.ndr.domain.lplan.LPlanTransLog;
import com.jiuyi.ndr.domain.xm.TransactionDetail;
import com.jiuyi.ndr.service.lplan.LPlanTransLogService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestSingleTrans;
import com.jiuyi.ndr.xm.http.request.RequestUnfreezeProject;
import com.jiuyi.ndr.xm.http.response.ResponseQueryIntelligentProjectOrder;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by drw on 2017/8/4.
 */
@SpringBootTest(classes = BatchApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(profiles = "dem01")
public class LPlanTransLogTest {

    @Autowired
    private LPlanTransLogDao lPlanTransLogDao;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LPlanAccountDao lPlanAccountDao;

    @Autowired
    private CreditDao creditDao;

    @Autowired
    private guohuanCreditDao guohuanCreditDao;

    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;

    @Autowired
    private LPlanTransLogService lPlanTransLogService;

    @Autowired
    private TransactionDetailDao transactionDetailDao;


    @Test
    public void paidInterestTest(){
        LPlanTransLog lPlanTransLog = lPlanTransLogDao.findBytransTypeAndDate(12, LPlanTransLog.TRANS_TYPE_EXPECT_INCOME, DateUtil.getCurrentDateShort());
        System.out.println(lPlanTransLog);
    }

    @Test
    public void testTrans(){
           List<LPlanAccount> lPlanAccounts = lPlanAccountDao.findForLPlanDailyInterest();
        for (LPlanAccount  lPlanAccount: lPlanAccounts) {
            ResponseQueryIntelligentProjectOrder response = transactionService.queryIntelligentProjectOrder(lPlanAccount.getInvestRequestNo());
            List<ResponseQueryIntelligentProjectOrder.Detail> details = response.getDetails();
            if (details!=null&&details.size()>0){
                for (ResponseQueryIntelligentProjectOrder.Detail detail : details) {
                    if ("0.00".equals(detail.getMatchShare())){
                        continue;
                    }
                    List<Credit> credits = creditDao.findBySubjectIdAndUserIdAndSourceChannel(detail.getMatchProjectNo(),response.getPlatformUserNo(), Credit.SOURCE_CHANNEL_LPLAN);
                    Integer oldHolding = credits.stream().mapToInt(credit->credit.getHoldingPrincipal()).sum();
                    System.out.println("oldHolding-----:"+oldHolding);
                    Integer newHolding = (int)(Double.valueOf(detail.getMatchShare())*100);
                    System.out.println("newHolding-----:"+newHolding);
                    Integer s = oldHolding-newHolding;
                    System.out.println("s----:"+s);
                    if (s>0){
                        Credit credit = credits.get(0);
                        credit.setHoldingPrincipal(credit.getHoldingPrincipal()-s);
                        lPlanAccount.setExpectedInterest(lPlanAccount.getExpectedInterest()+s);
                        lPlanAccountDao.updateExpectedInterest(lPlanAccount);
                        creditDao.updateHoldingPrincipal(credit);
                        guohuanCredit guohuanCredit = new guohuanCredit();
                        guohuanCredit.setUserId(lPlanAccount.getUserId());
                        guohuanCredit.setSubjectId(detail.getMatchProjectNo());
                        guohuanCredit.setMoney(newHolding);
                        guohuanCredit.setLocalMoney(oldHolding);
                        guohuanCreditDao.insert(guohuanCredit);
                    }
                }
            }
        }

    }

    @Test
    public void testInt(){
        BigDecimal principalPaid = new BigDecimal(1011201).multiply(new BigDecimal(12258)).divide(BigDecimal.valueOf(18890700), 0, BigDecimal.ROUND_DOWN);
        //BigDecimal principalPaid = new BigDecimal(1011201).multiply(new BigDecimal(12258)).divide(BigDecimal.valueOf(18890700), 6, BigDecimal.ROUND_DOWN);
        //BigDecimal principal = new BigDecimal(1011201).multiply(new BigDecimal(12258).divide(new BigDecimal(18890700),6, BigDecimal.ROUND_DOWN));
        BigDecimal principal = new BigDecimal(1011201).multiply(new BigDecimal(12258).divide(new BigDecimal(18890700),6, BigDecimal.ROUND_DOWN));
        System.out.println("principalPaid---:"+principalPaid);
        System.out.println("principal---:"+principal);
        Integer duePrincipal = principal.setScale(0, BigDecimal.ROUND_DOWN).intValue();
        System.out.println("duePrincipal---:"+duePrincipal);
    }

    @Test
    public void testAdd(){
        ResponseQueryIntelligentProjectOrder response = transactionService.queryIntelligentProjectOrder("NDR201705251253228a241f");
        List<ResponseQueryIntelligentProjectOrder.Detail> details = response.getDetails();
        Integer s = 0 ;
        if (details!=null&&details.size()>0){
            for (ResponseQueryIntelligentProjectOrder.Detail detail : details) {
                Integer newHolding = (int)(Double.valueOf(detail.getMatchShare())*100);
                System.out.println("newHolding-----:"+newHolding);
                s+=newHolding;

            }
            System.out.println("s---:"+s);
        }
    }

    @Test
    public void accontCredit(){
            LPlanAccount lPlanAccount = lPlanAccountDao.findByUserIdForUpdate("BFzaaimAn6vymxuy");
            ResponseQueryIntelligentProjectOrder response = transactionService.queryIntelligentProjectOrder(lPlanAccount.getInvestRequestNo());
            List<ResponseQueryIntelligentProjectOrder.Detail> details = response.getDetails();
            if (details!=null&&details.size()>0){
                for (ResponseQueryIntelligentProjectOrder.Detail detail : details) {
                    List<Credit> credits = creditDao.findBySubjectIdAndUserIdAndSourceChannel(detail.getMatchProjectNo(),response.getPlatformUserNo(), Credit.SOURCE_CHANNEL_LPLAN);
                    Integer oldHolding = credits.stream().mapToInt(credit->credit.getHoldingPrincipal()).sum();
                    System.out.println("oldHolding-----:"+oldHolding);
                    Integer newHolding = (int)(Double.valueOf(detail.getMatchShare())*100);
                    System.out.println("newHolding-----:"+newHolding);
                    Integer s = oldHolding-newHolding;
                    System.out.println("s----:"+s);
                    if (s>0){
                        Credit credit = credits.get(0);
                        credit.setHoldingPrincipal(credit.getHoldingPrincipal()-s);
                        lPlanAccount.setExpectedInterest(lPlanAccount.getExpectedInterest()+s);
                        lPlanAccountDao.updateExpectedInterest(lPlanAccount);
                        creditDao.updateHoldingPrincipal(credit);
                    }
                    guohuanCredit guohuanCredit = new guohuanCredit();
                    guohuanCredit.setUserId(lPlanAccount.getUserId());
                    guohuanCredit.setSubjectId(detail.getMatchProjectNo());
                    guohuanCredit.setMoney(newHolding);
                    guohuanCredit.setLocalMoney(oldHolding);
                    guohuanCredit.setValue(s);
                    guohuanCreditDao.insert(guohuanCredit);

                }
            }
    }
    @Test
    public void test12(){
        String s = StringUtils.remove("1234.37",".");
        Integer newHolding = Integer.valueOf(s);
        System.out.println("newHolding--:"+newHolding);
    }

    @Test
    public void test13(){
        IPlanAccount iPlanAccount = new IPlanAccount();
        iPlanAccount.setUserId("yyIBRrj6FNFbdhzz");
        iPlanAccount.setInvestRequestNo("NDR201710231755175088a9");
        unFreezeAmount(iPlanAccount,10363,0);
    }
    @Test
    public void test14(){
        IPlanTransLog iPlanTransLog = new IPlanTransLog();
        iPlanTransLog.setUserId("vEbe6zeiMFZrqbvv");
        BaseResponse baseResponse = transactionService.singleTrans(constructCompensateRequest(iPlanTransLog,30000));
    }

    private BaseResponse unFreezeAmount(IPlanAccount iPlanAccount, Integer unFreezeAmount, Integer commission) {
        RequestUnfreezeProject request = constructRequest(iPlanAccount.getInvestRequestNo(),unFreezeAmount,commission);
        BaseResponse response = transactionService.IntetligentProjectUnfreeze(request);
        if (commission>0){
            TransactionDetail transactionDetail = new TransactionDetail();
            transactionDetail.setBizType("COMMISSION");
            transactionDetail.setBusinessType(CheckFileConsts.BIZ_TYPE_11);
            transactionDetail.setAmount(commission/100.0);
            transactionDetail.setSourcePlatformUserNo(iPlanAccount.getUserId());
            transactionDetail.setTargetPlatformUserNo("");
            transactionDetail.setSubjectId("");
            transactionDetail.setRequestNo(request.getRequestNo());
            transactionDetail.setCreditUnit(null);//债权份额（债权转让且需校验债权关系的必传）
            transactionDetail.setStatus(TransactionDetail.STATUS_PENDING);
            transactionDetail.setRequestTime(DateUtil.getCurrentDateTime14());
            transactionDetail.setType(TransactionDetail.FILE_COMMISSION);//批量投标请求解冻
            transactionDetailDao.insert(transactionDetail);
        }
        return response;
    }
    private RequestUnfreezeProject constructRequest(String freezeNo,Integer unFreezeAmount,Integer commission){
        RequestUnfreezeProject request = new RequestUnfreezeProject();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setIntelRequestNo(freezeNo);
        request.setAmount(unFreezeAmount/100.0);
        if (commission>0){
            request.setCommission(commission/100.0);
        }
        return request;
    }

    private RequestSingleTrans constructCompensateRequest(IPlanTransLog iPlanTransLog, int compensateAmt) {
        RequestSingleTrans compensateRequest = new RequestSingleTrans();
        compensateRequest.setRequestNo(IdUtil.getRequestNo());
        compensateRequest.setTransCode(TransCode.CREDIT_LEND_COMPENSATE.getCode());
        compensateRequest.setTradeType(TradeType.MARKETING);
        List<RequestSingleTrans.Detail> compensateRequestDetails = new ArrayList<>(1);
        RequestSingleTrans.Detail compensateRequestDetail = new RequestSingleTrans.Detail();
        compensateRequestDetail.setBizType(BizType.MARKETING);
        compensateRequestDetail.setAmount(compensateAmt / 100.0);
        compensateRequestDetail.setSourcePlatformUserNo(GlobalConfig.MARKETING_ACCOUNT_XM);
        compensateRequestDetail.setTargetPlatformUserNo(iPlanTransLog.getUserId());
        compensateRequestDetails.add(compensateRequestDetail);
        compensateRequest.setDetails(compensateRequestDetails);
        return compensateRequest;
    }
}
