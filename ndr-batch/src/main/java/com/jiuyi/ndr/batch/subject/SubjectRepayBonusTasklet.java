package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.dao.user.UserDao;
import com.jiuyi.ndr.domain.account.PlatformAccount;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepayDetail;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.service.account.PlatformAccountService;
import com.jiuyi.ndr.service.account.PlatformTransferService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanRepayScheduleService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.notice.support.TemplateId;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
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
import java.util.List;

/**
 * Created by lln on 2017/10/31.
 * @author
 * 发放加息利息
 */
public class SubjectRepayBonusTasklet implements Tasklet {

    @Autowired
    private SubjectRepayDetailDao subjectRepayDetailDao;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private PlatformAccountService platformAccountService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private PlatformTransferService platformTransferService;
    @Autowired
    private IPlanRepayScheduleService iPlanRepayScheduleService;


    private static final Logger logger = LoggerFactory.getLogger(SubjectRepayBonusTasklet.class);
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("开始处理还款时发放加息奖励");
        List<SubjectRepayDetail> details = subjectRepayDetailDao.findNotReward();
        for (SubjectRepayDetail detail:details) {
            //查询标的信息
            Subject subject = subjectService.findBySubjectId(detail.getSubjectId());
            SubjectRepaySchedule schedule = subjectRepayScheduleService.getById(detail.getScheduleId());
            Integer status = detail.getExtBonusStatus();
            String requestNo = detail.getExtBonusSn();
            BigDecimal bonusRate = new BigDecimal(0);
            String name = "";
            Integer currTerm = schedule.getTerm();
            Integer totalTerm = subject.getTerm();
            if(detail.getChannel().equals(Credit.SOURCE_CHANNEL_YJT)){
                IPlanAccount iPlanAccount = iPlanAccountService.findById(detail.getSourceAccountId());
                IPlan iplan = iPlanService.getIPlanById(iPlanAccount.getIplanId());
                name = iplan.getName();
                bonusRate = iplan.getBonusRate();
                totalTerm = iplan.getTerm();
                if(IPlan.PACKAGING_TYPE_CREDIT.equals(iplan.getPackagingType())) {
                    currTerm = iPlanRepayScheduleService.getCurrentTermByIplanId(iplan.getId(), schedule.getDueDate());
                }
            }else{
                name=subject.getName();
                bonusRate = subject.getBonusRate();
            }
            Integer amount = detail.getBonusInterest()+detail.getBonusReward();
            //若状态处理中
            if(status!=null&&BaseResponse.STATUS_PENDING.equals(status)){
                //单笔交易查询
                RequestSingleTransQuery request = new RequestSingleTransQuery();
                request.setRequestNo(detail.getExtBonusSn());
                request.setTransactionType(TransactionType.TRANSACTION);
                ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
                if (!"0".equals(responseQuery.getCode())) {
                    //查询交易失败
                    logger.info("标的还款发放加息奖励交易状态未知，单笔查询，请求流水号{},查询失败，返回码{}", detail.getExtBonusSn(), responseQuery.getCode());
                    if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(responseQuery.getCode())) {
                        detail.setExtBonusStatus(BaseResponse.STATUS_FAILED);
                        status = BaseResponse.STATUS_FAILED;
                    }
                } else {
                    TransactionQueryRecord transactionQueryRecord = (TransactionQueryRecord) responseQuery.getRecords().get(0);
                    if ("SUCCESS".equals(transactionQueryRecord.getStatus())) {
                        //交易成功
                        detail.setExtBonusStatus(BaseResponse.STATUS_SUCCEED);
                        status = BaseResponse.STATUS_SUCCEED;
                        requestNo = responseQuery.getRequestNo();
                    } else if ("FAIL".equals(transactionQueryRecord.getStatus())) {
                        //查询结果，交易失败，重新发起交易
                        logger.info("标的还款发放加息奖励交易状态失败，单笔查询，请求流水号{},交易查询失败，重新发起交易", responseQuery.getRequestNo());
                        detail.setExtBonusStatus(BaseResponse.STATUS_FAILED);
                        status = BaseResponse.STATUS_FAILED;
                    } else {
                        //查询结果：交易处理中
                        logger.info("标的还款发放加息奖励交易状态未知，单笔查询，请求流水号{},交易查询处理中", responseQuery.getRequestNo());
                        detail.setExtBonusStatus(BaseResponse.STATUS_PENDING);
                        status = BaseResponse.STATUS_PENDING;
                    }
                }
            }
            if(BaseResponse.STATUS_FAILED.equals(status)||status==null){
                //判断账户余额
                PlatformAccount account = platformAccountService.getPlatformAccount(GlobalConfig.MARKETING_ACCOUNT_02_DR);
                Integer accoutBalance = BigDecimal.valueOf(account.getAvailableBalance()).multiply(BigDecimal.valueOf(100)).intValue();
                if(accoutBalance<amount){
                    logger.info("营销款账户{}余额不足",GlobalConfig.MARKETING_ACCOUNT_02_DR);
                    continue;
                }
                //重新发起交易
                BaseResponse baseResponse = this.constructCompensateAmt(amount,detail.getUserId(),TransCode.SUBJECT_REPAY.getCode());
                detail.setExtBonusStatus(baseResponse.getStatus());
                detail.setExtBonusSn(baseResponse.getRequestNo());
                if(BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())){
                    status = BaseResponse.STATUS_SUCCEED;
                    requestNo = baseResponse.getRequestNo();
                    logger.info("标的{}还款,给用户{}发放加息奖励{},成功",detail.getSubjectId(),detail.getUserId(),amount);
                }else if(BaseResponse.STATUS_FAILED.equals(baseResponse.getStatus())){
                    status = BaseResponse.STATUS_FAILED;
                    logger.info("标的{}还款,给用户{}发放加息奖励{},失败",detail.getSubjectId(),detail.getUserId(),amount);
                }else{
                    logger.info("标的{}还款,给用户{}发放加息奖励{},处理中",detail.getSubjectId(),detail.getUserId(),amount);
                }
            }
            //成功,更新营销款账户,增加投资人入账流水
            if(BaseResponse.STATUS_SUCCEED.equals(status)){
                //营销款账户出款
                platformAccountService.transferOut(GlobalConfig.MARKETING_ACCOUNT_02_DR,amount/100.0,
                        BusinessEnum.ndr_subject_bonus_interest,"标的还款发放加息利息,标的ID:"+detail.getSubjectId()+",金额:"+amount/100.0,requestNo,detail.getSubjectId(),detail.getScheduleId());
                platformTransferService.out002ForBouns(detail.getUserId(), amount/100.0, String.valueOf(detail.getScheduleId()), requestNo,BusinessEnum.ndr_subject_bonus_interest,"营销款002_02,给投资人发放散标加息奖励",detail.getSubjectId());
                //投资人入账
                if(detail.getBonusInterest() != null && detail.getBonusInterest()>0){
                    userAccountService.transferIn(detail.getUserId(),detail.getBonusInterest()/100.0,BusinessEnum.ndr_subject_bonus_interest,
                            name+"第"+currTerm+"/"+totalTerm+"期的项目加息奖励","标的还款发放活动加息利息,标的ID:"+detail.getSubjectId()+",金额:"+detail.getBonusInterest()/100.0,requestNo,detail.getSubjectId(),detail.getScheduleId(),1);
                }
                if(detail.getBonusReward() != null && detail.getBonusReward()>0){
                    userAccountService.transferInForBonus(detail.getUserId(),detail.getBonusReward()/100.0,BusinessEnum.ndr_subject_bonus_interest,
                            name+"第"+currTerm+"/"+totalTerm+"期的加息券奖励","标的还款发放加息券利息,标的ID:"+detail.getSubjectId()+",金额:"+detail.getBonusReward()/100.0,requestNo,detail.getSubjectId(),detail.getScheduleId(),1);
                }
                //更新借款人subjectAccount(成功)
                if(detail.getChannel().equals(Credit.SOURCE_CHANNEL_YJT)){
                    iPlanAccountService.updateForBonusInterest(detail);
                }else{
                    subjectAccountService.updateForBonusInterest(detail);
                }
                try{
                    User user = userDao.getUserById(detail.getUserId());
                    //插入一条短信记录
                    String msg ="";
                    String type="";
                    //只有项目加息
                    if(detail.getBonusInterest()!=null && detail.getBonusInterest()>0 && (detail.getBonusReward() == null || detail.getBonusReward()==0 )){
                        if(SubjectRepaySchedule.STATUS_NORMAL_REPAID.equals(schedule.getStatus())){
                            msg = user.getRealname()+","+String.valueOf(detail.getBonusInterest()/100.0)+","
                                    +name+","+currTerm.toString()+","
                                    +totalTerm.toString()+","+bonusRate.multiply(BigDecimal.valueOf(100)).setScale(2).toString()+"%"+","
                                    +String.valueOf(detail.getBonusInterest()/100.0);
                            type=TemplateId.NEW_IPLAN_BONUS_ACTIVITY;
                        }else{
                            //提前还款
                            msg = user.getRealname()+","+name+","
                                    +bonusRate.multiply(BigDecimal.valueOf(100)).setScale(2).toString()+"%"+","
                                    +String.valueOf(detail.getBonusInterest()/100.0);
                            type=TemplateId.NEW_IPLAN_BONUS_ACTIVITY_ADVANCE;
                        }
                    }else if(detail.getBonusReward()!=null && detail.getBonusReward()>0 && (detail.getBonusInterest()==null || detail.getBonusInterest()==0)){
                        //只有红包
                        if(SubjectRepaySchedule.STATUS_NORMAL_REPAID.equals(schedule.getStatus())) {
                            msg = user.getRealname() + "," + String.valueOf(detail.getBonusReward() / 100.0) + ","
                                    + name + "," + currTerm.toString() + ","
                                    + totalTerm.toString() + ","
                                    + String.valueOf(detail.getBonusReward() / 100.0);
                            type = TemplateId.NEW_IPLAN_BONUS_RATE;
                        }else{
                            msg = user.getRealname() + "," + name + ","
                                    + String.valueOf(detail.getBonusReward() / 100.0);
                            type = TemplateId.NEW_IPLAN_BONUS_RATE_ADVANCE;
                        }
                    }else if(detail.getBonusReward()!=null && detail.getBonusReward()>0 && detail.getBonusInterest()!=null && detail.getBonusInterest()>0){
                        if(SubjectRepaySchedule.STATUS_NORMAL_REPAID.equals(schedule.getStatus())){
                            msg = user.getRealname()+","+String.valueOf(amount/100.0)+","
                                    +name+","+currTerm.toString()+","
                                    +totalTerm.toString()+","
                                    +String.valueOf(detail.getBonusReward()/100.0)+","
                                    +bonusRate.multiply(BigDecimal.valueOf(100)).setScale(2).toString()+"%"+","
                                    +String.valueOf(detail.getBonusInterest()/100.0);
                            type=TemplateId.NEW_IPLAN_BONUS;
                        }else{
                            msg = user.getRealname()+","+name+","
                                    +String.valueOf(amount/100.0)+","
                                    +String.valueOf(detail.getBonusReward()/100.0)+","
                                    +bonusRate.multiply(BigDecimal.valueOf(100)).setScale(2).toString()+"%"+","
                                    +String.valueOf(detail.getBonusInterest()/100.0);
                            type=TemplateId.NEW_IPLAN_BONUS_ADVANCE;
                        }
                    }

                    subjectService.insertMsg(detail.getUserId(),msg,user.getMobileNumber(),type);
                }catch (Exception e){
                    logger.error("插入短信记录异常,detailID-{}",detail.getId());
                }
            }
            subjectRepayDetailDao.update(detail);
        }
        return RepeatStatus.FINISHED;
    }



    /**
     * 发放奖励
     * @param compensateAmt
     * @param userId
     * @return
     */
    public BaseResponse constructCompensateAmt(Integer compensateAmt, String userId,String transCode) {
        RequestSingleTrans compensateRequest = new RequestSingleTrans();
        compensateRequest.setRequestNo(IdUtil.getRequestNo());
        compensateRequest.setTransCode(transCode);
        compensateRequest.setTradeType(TradeType.MARKETING);
        List<RequestSingleTrans.Detail> details = new ArrayList<>(1);
        RequestSingleTrans.Detail compensateRequestDetail = new RequestSingleTrans.Detail();
        compensateRequestDetail.setBizType(BizType.MARKETING);
        compensateRequestDetail.setAmount(compensateAmt / 100.0);
        compensateRequestDetail.setSourcePlatformUserNo(GlobalConfig.MARKETING_ACCOUNT_02_DR);
        compensateRequestDetail.setTargetPlatformUserNo(userId);
        details.add(compensateRequestDetail);
        compensateRequest.setDetails(details);
        return transactionService.singleTrans(compensateRequest);
    }
}
