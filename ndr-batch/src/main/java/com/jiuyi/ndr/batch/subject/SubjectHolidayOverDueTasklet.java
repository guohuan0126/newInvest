package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayScheduleDao;
import com.jiuyi.ndr.dao.user.UserDao;
import com.jiuyi.ndr.domain.account.PlatformAccount;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepayDetail;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.service.account.PlatformAccountService;
import com.jiuyi.ndr.service.account.PlatformTransferService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.notice.support.TemplateId;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestSingleTrans;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.query.TransactionQueryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubjectHolidayOverDueTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(SubjectHolidayOverDueTasklet.class);

    @Autowired
    private SubjectRepayScheduleDao subjectRepayScheduleDao;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private SubjectRepayDetailDao subjectRepayDetailDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private IPlanDao iPlanDao;

    @Autowired
    private PlatformAccountService platformAccountService;

    @Autowired
    private PlatformTransferService platformTransferService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private NoticeService noticeService;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        logger.info("正在进行节假日逾期补息{}");
        makeOverfee();
        return RepeatStatus.FINISHED;
    }

    public void makeOverfee() {
        //逾期补息金额
        Double overFee=0.0;
        //逾期天数
        Integer days = 0;
        //存放项目信息,利率
        Map<String,Object> map = new HashMap<>();
        //查询出当天还款完成的逾期的还款明细
        List<SubjectRepayDetail> details = subjectRepayScheduleDao.getOverDueDetailsByRepayDate(DateUtil.getCurrentDateShort());
        for (SubjectRepayDetail detail : details) {
            if (BaseResponse.STATUS_PENDING.equals(detail.getExtStatus())) {
                RequestSingleTransQuery request = new RequestSingleTransQuery();
                request.setRequestNo(detail.getExtOverSn());
                request.setTransactionType(TransactionType.TRANSACTION);
                ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
                if (!"0".equals(responseQuery.getCode())) {
                    if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(responseQuery.getCode())) {
                        //订单不存在，设置交易失败，重新发起交易
                        detail.setExtStatus(BaseResponse.STATUS_FAILED);
                        subjectRepayDetailDao.update(detail);
                    }
                    continue;
                } else {
                    TransactionQueryRecord transactionQueryRecord = (TransactionQueryRecord) responseQuery.getRecords().get(0);
                    if ("SUCCESS".equals(transactionQueryRecord.getStatus())) {
                        detail.setExtStatus(BaseResponse.STATUS_SUCCEED);
                    } else if ("PROCESSING".equals(transactionQueryRecord.getStatus())) {
                        detail.setExtStatus(BaseResponse.STATUS_PENDING);
                        subjectRepayDetailDao.update(detail);
                        continue;
                    }else {
                        detail.setExtStatus(BaseResponse.STATUS_FAILED);
                        subjectRepayDetailDao.update(detail);
                        continue;
                    }
                }
            } else {
                //计算预期天数
                SubjectRepaySchedule repaySchedule = subjectRepayScheduleDao.findById(detail.getScheduleId());
                days = (int) DateUtil.betweenDays(repaySchedule.getDueDate(),repaySchedule.getRepayDate());
                //获取投资利率
                map = getInvestRate(detail);
                BigDecimal investRate = (BigDecimal) map.get("rate");
                //计算补息
                BigDecimal fee = new BigDecimal(detail.getPrincipal() + detail.getInterest() + detail.getBonusInterest()).multiply(investRate).multiply(new BigDecimal(days)).divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS), 6, BigDecimal.ROUND_DOWN);
                overFee = ArithUtil.round(fee.doubleValue() / 100.0, 2);
                //判断账户余额
                PlatformAccount account = platformAccountService.getPlatformAccount(GlobalConfig.MARKETING_ACCOUNT_02_DR);
                BigDecimal accoutBalance = BigDecimal.valueOf(account.getAvailableBalance());
                if(accoutBalance.compareTo(fee) < 0){
                    logger.info("营销款账户{}余额不足",GlobalConfig.MARKETING_ACCOUNT_02_DR);
                    continue;
                }
                if(overFee > 0){
                    BaseResponse response = this.constructCompensateAmt(overFee,detail.getUserId(), TransCode.HOLIDAY_OVERDUE.getCode());
                    if (BaseResponse.STATUS_FAILED.equals(response.getStatus())) {//交易失败
                        detail.setExtOverSn(response.getRequestNo());
                        detail.setExtStatus(response.getStatus());
                        subjectRepayDetailDao.update(detail);
                        logger.info("标的逾期{}还款,给用户{}发放补息奖励{},失败",detail.getSubjectId(),detail.getUserId(),overFee);
                        continue;
                    }
                    if (BaseResponse.STATUS_PENDING.equals(response.getStatus())) {//处理中
                        detail.setExtOverSn(response.getRequestNo());
                        detail.setExtStatus(response.getStatus());
                        subjectRepayDetailDao.update(detail);
                        logger.info("标的逾期{}还款,给用户{}发放补息奖励{},处理中",detail.getSubjectId(),detail.getUserId(),overFee);
                        continue;
                    }
                    detail.setOverFee(fee.intValue());
                    detail.setExtOverSn(response.getRequestNo());
                    detail.setExtStatus(BaseResponse.STATUS_SUCCEED);
                    logger.info("标的逾期{}还款,给用户{}发放补息奖励{},成功",detail.getSubjectId(),detail.getUserId(),overFee);
                    //营销款账户出款
                    platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_02_DR,overFee,
                            BusinessEnum.ndr_holiday_overdue_interest,"节假日逾期补息,标的ID:"+detail.getSubjectId()+",金额:"+overFee,response.getRequestNo(),detail.getSubjectId(),detail.getScheduleId());
                    platformTransferService.out002ForBouns(detail.getUserId(), overFee, String.valueOf(detail.getScheduleId()), response.getRequestNo(),BusinessEnum.ndr_holiday_overdue_interest,"营销款002_02,给投资人发放节假日逾期补息",detail.getSubjectId());
                    //投资人入账
                    Subject subject = (Subject) map.get("subject");
                    IPlan iPlan = (IPlan) map.get("iplan");
                    userAccountService.transferIn(detail.getUserId(),overFee,BusinessEnum.ndr_holiday_overdue_interest,
                            subject.getName()+"节假日逾期补息"+",金额:"+overFee,"节假日逾期补息,标的ID:"+detail.getSubjectId()+",金额:"+overFee,response.getRequestNo(),detail.getSubjectId(),detail.getScheduleId(),1);
                    //发送短信
                    try {
                        User user = userDao.getUserById(detail.getUserId());
                        //插入一条短信记录
                        String msg = (detail.getChannel()==0?subject.getName():iPlan.getName())+","+String.valueOf(days)+","
                                +String.valueOf(overFee);
                        noticeService.send(user.getMobileNumber(), msg,TemplateId.HOLIDAY_OVERDUE_INTEREST);
                    }catch (Exception e){
                        logger.error("插入短信记录异常,detailID-{}",detail.getId());
                    }
                }else{
                    detail.setOverFee(-1);
                }
                subjectRepayDetailDao.update(detail);
            }
        }
    }


    public Map<String,Object> getInvestRate(SubjectRepayDetail detail){
        Map<String,Object> map = new HashMap<>();
        BigDecimal investRate = BigDecimal.ZERO;
        Subject subject = subjectDao.findBySubjectId(detail.getSubjectId());
        map.put("subject",subject);
        if(detail.getChannel() == 0){
            investRate = subject.getInvestRate();
            if(detail.getBonusInterest() > 0){
                investRate = subject.getInvestRate().add(subject.getBonusRate());
            }
        }else if(detail.getChannel() == 3){
            IPlan iPlan = iPlanDao.findBySourceAccountId(detail.getSourceAccountId());
            map.put("iPlan",iPlan);
            investRate = iPlan.getFixRate();
            if(detail.getBonusInterest() > 0){
                investRate = iPlan.getFixRate().add(iPlan.getBonusRate());
            }
        }
        map.put("rate",investRate);
        return map;
    }


    /**
     * 发放奖励
     * @param compensateAmt
     * @param userId
     * @return
     */
    public BaseResponse constructCompensateAmt(Double compensateAmt, String userId,String transCode) {
        RequestSingleTrans compensateRequest = new RequestSingleTrans();
        compensateRequest.setRequestNo(IdUtil.getRequestNo());
        compensateRequest.setTransCode(transCode);
        compensateRequest.setTradeType(TradeType.MARKETING);
        List<RequestSingleTrans.Detail> details = new ArrayList<>(1);
        RequestSingleTrans.Detail compensateRequestDetail = new RequestSingleTrans.Detail();
        compensateRequestDetail.setBizType(BizType.MARKETING);
        compensateRequestDetail.setAmount(compensateAmt);
        compensateRequestDetail.setSourcePlatformUserNo(GlobalConfig.MARKETING_ACCOUNT_02_DR);
        compensateRequestDetail.setTargetPlatformUserNo(userId);
        details.add(compensateRequestDetail);
        compensateRequest.setDetails(details);
        return transactionService.singleTrans(compensateRequest);
    }
}
