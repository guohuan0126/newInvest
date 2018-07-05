package com.jiuyi.test;

import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.BatchApplication;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayScheduleDao;
import com.jiuyi.ndr.dao.subject.SubjectSendSmsDao;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.domain.subject.SubjectSendSms;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.*;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by duanrong on 2017/8/10.
 */
@SpringBootTest(classes = BatchApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(profiles = "dev01")
public class SubjectRepayText {
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;

    @Autowired
    private SubjectRepayScheduleDao subjectRepayScheduleDao;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private ConfigDao configDao;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private SubjectSendSmsDao subjectSendSmsDao;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private IPlanService iPlanService;

    @Test
    public void exitLPlanTest(){
        SubjectRepaySchedule subjectRepaySchedule = subjectRepayScheduleService.findRepaySchedule("NDR170630102100000001",1);
        subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_WAIT_REPAY);
        subjectRepayScheduleService.update(subjectRepaySchedule);
    }


    @Test
    public void exitLPlanTest2(){
        Subject subject = subjectService.findBySubjectId("NDR180517182342000040");
        List<SubjectRepaySchedule> schedules = subjectRepayScheduleService.findRepayScheduleBySubjectId("NDR180517182342000040");
        //变更标的
        BaseResponse response = subjectService.changeSubjectXM(subject, Subject.SUBJECT_STATUS_REPAY_NORMAL_XM,schedules);
        System.out.println("变更标的状态交易:"+response);
    }

    @Test
    public void exitLPlanTest3(){

    }
    private BaseResponse changeSubjectXM(String subjectId, String status) {
        RequestModifyProject modifyProject = new RequestModifyProject();
        modifyProject.setRequestNo(IdUtil.getRequestNo());
        modifyProject.setProjectNo(subjectId);
        modifyProject.setStatus(status);
        return transactionService.modifyProject(modifyProject);
    }

    @Test
    public void exitLPlanTest4(){
        //预处理解冻
        RequestCancelPreTransaction request = new RequestCancelPreTransaction();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setPreTransactionNo("NDR201806221735533ec878");
        request.setAmount(500.0);
        BaseResponse baseResponse = transactionService.cancelPreTransactionOld(request);
        System.out.println("预处理取消返回:"+baseResponse);
    }

    @Test
    public void exitLPlanTest5(){
        //单笔查询
        RequestSingleTransQuery request = new RequestSingleTransQuery();
        request.setRequestNo("NDR2017110715002526cbaa16");
        request.setTransactionType(TransactionType.TRANSACTION);
        ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
        System.out.println("查询变更标的状态:"+responseQuery);
    }

    @Test
    public void exitLPlanTest6(){
        //冻结代偿账户
        RequestUserAutoPreTransaction request = new RequestUserAutoPreTransaction();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setPlatformUserNo("SYS_GENERATE_001_01");
        //预处理业务类型:代偿
        request.setBizType(BizType.COMPENSATORY);
        request.setAmount(61.14);
        request.setProjectNo("NDR171025185655000016");
        BaseResponse base = transactionService.platformPreTransaction(request);
        System.out.println("冻结代偿账户:"+base);
    }

    @Test
    public void exitLPlanTest7(){
        //批量交易处理平台账户拨款
        RequestBatchTrans request = new RequestBatchTrans();
        request.setBatchNo(IdUtil.getRequestNo());
        List<RequestBatchTrans.TransDetail> bizDetails = new ArrayList<>();
        RequestBatchTrans.TransDetail transDetail = new RequestBatchTrans.TransDetail();
        transDetail.setRequestNo(IdUtil.getRequestNo());
        transDetail.setTradeType(TradeType.FUNDS_TRANSFER);
        List<RequestBatchTrans.TransDetail.BizDetail> details = new ArrayList<>();
        RequestBatchTrans.TransDetail.BizDetail detail1 = new RequestBatchTrans.TransDetail.BizDetail();
        detail1.setAmount(100.0);
        detail1.setBizType(BizType.FUNDS_TRANSFER);
        detail1.setSourcePlatformUserNo(GlobalConfig.MARKETING_SYS_XM);
        detail1.setTargetPlatformUserNo("SYS_GENERATE_002_01");
        details.add(detail1);

        /*RequestBatchTrans.TransDetail.BizDetail detail2 = new RequestBatchTrans.TransDetail.BizDetail();
        detail2.setAmount(187.73);
        detail2.setBizType(BizType.FUNDS_TRANSFER);
        detail2.setSourcePlatformUserNo("SYS_GENERATE_001");
        detail2.setTargetPlatformUserNo(GlobalConfig.MARKETING_ACCOUNT_XM);
        details.add(detail2);*/

        /*RequestBatchTrans.TransDetail.BizDetail detail3 = new RequestBatchTrans.TransDetail.BizDetail();
        detail3.setAmount(109.71);
        detail3.setBizType(BizType.FUNDS_TRANSFER);
        detail3.setSourcePlatformUserNo("SYS_GENERATE_001_03");
        detail3.setTargetPlatformUserNo(GlobalConfig.MARKETING_ACCOUNT_XM);
        details.add(detail3);*/
        transDetail.setDetails(details);
        bizDetails.add(transDetail);
        request.setBizDetails(bizDetails);
        BaseResponse base = transactionService.batchTrans(request);
        System.out.println("批量交易:"+base);
    }

    @Test
    public void exitLPlanTest8(){
        //营销款打款
        RequestSingleTrans marketingTrans = new RequestSingleTrans();
        marketingTrans.setTradeType(TradeType.MARKETING);
        marketingTrans.setRequestNo(IdUtil.getRequestNo());
        marketingTrans.setTransCode(TransCode.MARKET002_01_TRANSFER.getCode());
        List<RequestSingleTrans.Detail> details = new ArrayList<>();
        RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
        detail.setBizType(BizType.MARKETING);
        detail.setSourcePlatformUserNo(GlobalConfig.MARKETING_ACCOUNT_XM);
        detail.setTargetPlatformUserNo("BNzERr3u6FZnvvtb");
        detail.setAmount(358.58);
        details.add(detail);
        marketingTrans.setDetails(details);
        BaseResponse base =  transactionService.singleTrans(marketingTrans);
        System.out.println("营销款打款请求:"+base);
    }

    @Test
    public void test_establishIntelligentProject() {
        //天天赚投资追加冻结
        String requestNo = IdUtil.getRequestNo();
        RequestSingleTrans request = new RequestSingleTrans();
        request.setTransCode(TransCode.IPLAN_INVEST_FREEZE.getCode());
        request.setRequestNo(requestNo);
        request.setTradeType(TradeType.INTELLIGENT_APPEND);
        RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
        detail.setBizType(BizType.APPEND_FREEZE);
        detail.setSourcePlatformUserNo("ZjUJ7r2QzMjmbrmk");
        detail.setFreezeRequestNo("NDR20180416171329df7594");
        detail.setAmount(50597.0);
        List<RequestSingleTrans.Detail> details = new ArrayList<>();
        details.add(detail);
        request.setDetails(details);

        BaseResponse response = transactionService.singleTrans(request);
        System.out.println("response--:"+response.toString());
    }


    @Test
    public void exitLPlanTest10(){
        //解冻收佣金
        BaseResponse response = transactionService.unFreezeAmount("NBNFRbZbEfQfsybg","NDR20170622151435924034",1971196,1971196);
        System.out.println("返回:"+response);
    }


    @Test
    public void exitLPlanTest11(){
        //预处理解冻
        RequestCancelPreTransaction request = new RequestCancelPreTransaction();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setPreTransactionNo("NDR20180316133101246154d1d0ff921");
        request.setAmount(0.01);
        BaseResponse baseResponse = transactionService.cancelPreTransactionOld(request);
        System.out.println("预处理取消返回:"+baseResponse);
    }

    @Test
    public void liubiao(){
        String requestNo = IdUtil.getRequestNo();
        //预处理取消接口
        RequestCancelPreTransaction request = new RequestCancelPreTransaction();
        request.setRequestNo(requestNo);
        request.setPreTransactionNo("NDR20190930181151c13414");
        request.setAmount(20052.0);
        //调用资金解冻接口，解冻资金
        BaseResponse response = transactionService.cancelPreTransactionOld(request);
        System.out.println(request.toString());
        System.out.println(response.toString());
        System.out.println(response.getStatus());
    }

    @Test
    public void testSchedule123(){
        //生成还款计划(卡贷)
        Subject subject = subjectDao.findBySubjectId("NDR171219111214000005");
        subjectRepayScheduleService.makeUpCreditCardRepaySchedule(subject);
    }

    @Test
    public void yuchulidongjie(){
        //预处理冻结
        RequestUserAutoPreTransaction request = new RequestUserAutoPreTransaction();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setPlatformUserNo("e26VN3ZNZBb2umkq");
        request.setBizType(BizType.REPAYMENT);
        request.setAmount(600.0);
        request.setProjectNo("");
        BaseResponse baseResponse =  transactionService.userAutoPreTransaction(request);
        System.out.println("预处理冻结返回:"+baseResponse);
    }

    @Test
    public void freeze(){
        RequestFreeze request = new RequestFreeze();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setPlatformUserNo("e26VN3ZNZBb2umkq");
        request.setAmount(600.0);
        BaseResponse baseResponse =  transactionService.freeze(request);
        System.out.println("资金冻结返回:"+baseResponse);
    }

    @Test
    public void yuchulichaxun(){
        RequestSingleTransQuery request = new RequestSingleTransQuery();
        request.setRequestNo("NDR201803161117185250fb");
        request.setTransactionType(TransactionType.PRETRANSACTION);
        ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
        System.out.println("查询预处理冻结流水:"+responseQuery.toString());
    }

    @Test
    public void createSchedule(){
        FinanceCalcUtils.CalcResult calcResult = FinanceCalcUtils.calcMCEI(4504900, BigDecimal.valueOf(0.144), 12);
        System.out.println(calcResult);
    }

    //债权转让撤销
    @Test
    public void testCancel(){
        RequestCancelDebentureSale request = new RequestCancelDebentureSale();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setCreditsaleRequestNo("NDR201805211909046dddc4");
        request.setTransCode(TransCode.CREDIT_CANCEL.getCode());

        BaseResponse baseResponse = null;
        try {
            System.out.println("开始调用厦门银行取消债权出让接口->{}" + JSON.toJSONString(request));
            baseResponse = transactionService.cancelDebentureSale(request);
            System.out.println("取消债权出让接口返回->{}" + JSON.toJSONString(baseResponse));
        } catch (Exception e) {
            if (baseResponse == null) {
                baseResponse = new BaseResponse();
            }
            baseResponse.setStatus(BaseResponse.STATUS_PENDING);
        }
    }

    @Test
    public  void testString(){
        subjectService.getInterestByRepayType(5000000,BigDecimal.valueOf(0.074),BigDecimal.valueOf(0.08),3,3*30,Subject.REPAY_TYPE_MCEI);
    }

    @Test
    public void testChange(){
//        Subject subject = subjectDao.findBySubjectId("NDR180313103946000040");
//        Integer expectInterest =(int)(subjectService.getInterestByRepayType(134127,subject.getInvestRate(),subject.getRate(),2,subject.getPeriod(),subject.getRepayType())*100);
//        Integer expectBonusInterest = (int)(subjectService.getInterestByRepayType(134127,subject.getBonusRate(),subject.getRate(),2,subject.getPeriod(),subject.getRepayType())*100);
//        System.out.println(expectInterest);
//        System.out.println(expectBonusInterest);
//        LocalDate now = DateUtil.parseDate("20180512", DateUtil.DATE_TIME_FORMATTER_8);
//        System.out.println(now);
        IPlanAccount iPlanAccount = iPlanAccountService.findById(84);
        IPlan iPlan  = iPlanService.getIPlanById(80);
        iPlanAccountService.calcInterest(iPlanAccount,iPlan);
    }

    @Test
    public  void testSend(){
        List<SubjectSendSms> list = subjectSendSmsDao.findNotSendMsg();
        for (SubjectSendSms sms:list) {
            noticeService.send(sms.getMobileNumber(),sms.getMsg(),sms.getType());
//            subjectSendSmsDao.update(1,new Date().toString(),sms.getId());
            System.out.println("aaaaaaaaaa");
        }
    }
}
