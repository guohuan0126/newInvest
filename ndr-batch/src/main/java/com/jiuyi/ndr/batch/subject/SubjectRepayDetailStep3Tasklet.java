package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.dao.config.AccountCompensationConfigDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.domain.account.CompensatoryAcctLog;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepayDetail;
import com.jiuyi.ndr.service.account.CompensatoryAcctLogService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by lln on 2017/10/25.
 */
public class SubjectRepayDetailStep3Tasklet implements Tasklet{

    private static final Logger logger = LoggerFactory.getLogger(SubjectRepayDetailStep3Tasklet.class);

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private SubjectRepayDetailDao subjectRepayDetailDao;
    @Autowired
    private CompensatoryAcctLogService cpsAcctLogService;
    @Autowired
    private AccountCompensationConfigDao accountCompensationConfigDao;
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("开始处理借款人生成还款流水");
        long startTime = System.currentTimeMillis();
        List<SubjectRepayDetail> details = subjectRepayDetailDao.findByStatusAndCurrentStep(SubjectRepayDetail.STATUS_REPAID,SubjectRepayDetail.STEP_HAS_DEAL_INVESTOR);
        Map<Integer, List<SubjectRepayDetail>> maps = details.stream().collect(Collectors.groupingBy(SubjectRepayDetail :: getScheduleId));
        for (Map.Entry<Integer, List<SubjectRepayDetail>> entry : maps.entrySet()) {
            Integer scheduleId = entry.getKey();
            List<SubjectRepayDetail> subjectRepayDetails = entry.getValue();
            if(subjectRepayDetails.stream().allMatch(subjectRepayDetail -> SubjectRepayDetail.STATUS_REPAID.equals(subjectRepayDetail.getStatus()) && SubjectRepayDetail.STEP_HAS_DEAL_INVESTOR.equals(subjectRepayDetail.getCurrentStep()))) {
                logger.info("all repay details of the [{}] subject repay schedule has repay finished, begin deal with borrower local repay amount unfreeze", scheduleId);
                Subject subject = subjectService.findBySubjectId(subjectRepayDetails.get(0).getSubjectId());
                Integer directFlag = subject.getDirectFlag();
                String borrowerIdXM = "";
                if(directFlag.equals(Subject.DIRECT_FLAG_NO)){
                    borrowerIdXM = subject.getIntermediatorId();
                }else{
                    borrowerIdXM = subject.getBorrowerId();
                }

                Integer borrowerOutPrincipal = 0;
                Integer borrowerOutInterest = 0;
                Integer borrowerOutCommission = 0;//佣金
                Integer borrowerOutProfit = 0;//分润金额
                Integer cpsOutPrincipal = 0;
                Integer cpsOutInterest = 0;
                Integer cpsOutCommission = 0;//佣金
                Integer cpsOutProfit = 0;//分润金额
                String freezeSn="";
                String cpsFreezeSn="";
                for (SubjectRepayDetail subjectRepayDetail : subjectRepayDetails) {
                    if (SubjectRepayDetail.SOURCE_BRW.equals(subjectRepayDetail.getSourceType())) {
                        freezeSn = subjectRepayDetail.getExtSn();
                        borrowerOutPrincipal += subjectRepayDetail.getPrincipal();
                        borrowerOutInterest += subjectRepayDetail.getInterest()+ subjectRepayDetail.getPenalty() + subjectRepayDetail.getFee();
                        borrowerOutCommission +=subjectRepayDetail.getCommission();
                        borrowerOutProfit += subjectRepayDetail.getProfit()+subjectRepayDetail.getDeptPenalty();
                    } else if (SubjectRepayDetail.SOURCE_CPS.equals(subjectRepayDetail.getSourceType())) {
                        cpsFreezeSn = subjectRepayDetail.getExtSn();
                        logger.info("处理代偿账户从冻结中转出！！");
                        cpsOutPrincipal += subjectRepayDetail.getPrincipal();
                        cpsOutInterest += subjectRepayDetail.getInterest()+ subjectRepayDetail.getPenalty() + subjectRepayDetail.getFee();
                        cpsOutCommission +=subjectRepayDetail.getCommission();
                        cpsOutProfit += subjectRepayDetail.getProfit()+subjectRepayDetail.getDeptPenalty();
                    } else {
                        logger.warn("没有该出款方：【{}】", subjectRepayDetail.getSourceType());
                        continue;
                    }
                    subjectRepayDetail.setCurrentStep(SubjectRepayDetail.STEP_HAS_DEAL_BORROWER);
                    subjectRepayDetailDao.update(subjectRepayDetail);
                }
                if((borrowerOutPrincipal+borrowerOutInterest+borrowerOutCommission+borrowerOutProfit)>0){
                    userAccountService.tofreeze(borrowerIdXM, (borrowerOutPrincipal + borrowerOutInterest+borrowerOutCommission+borrowerOutProfit) / 100.0, BusinessEnum.ndr_repay,
                            "标的还款：" + subject.getName(), "标的还款，标的ID：" + subject.getSubjectId() + "，本金：" + borrowerOutPrincipal / 100.0 + "，利息：" + borrowerOutInterest / 100.0+",佣金: "+borrowerOutCommission/100.0 + ",分润: "+borrowerOutProfit/100.0, freezeSn,subject.getSubjectId(),scheduleId,
                            borrowerOutPrincipal/100.0,borrowerOutInterest/100.0,borrowerOutCommission/100.0);
                }
                if((cpsOutPrincipal+cpsOutInterest+cpsOutCommission+cpsOutProfit)>0){
                    //若记录代偿为空,则是直贷1,去配置表查询
                    if(subject.getCompensationAccount()==null){
                        borrowerIdXM = accountCompensationConfigDao.findByDepartmentAndType(subject.getAccountingDepartment(), subject.getType()).getCompensationAccount();
                    }else{
                        borrowerIdXM = subject.getCompensationAccount();
                    }
                    logger.info("本地开始处理代偿账户从冻结中转出！");
                    userAccountService.tofreeze(borrowerIdXM, (cpsOutPrincipal + cpsOutInterest+cpsOutCommission+cpsOutProfit) / 100.0, BusinessEnum.ndr_subject_repay_cps_out,
                            "标的还款代偿：" + subject.getName(), "标的还款代偿，标的ID：" + subject.getSubjectId() + "，本金：" + cpsOutPrincipal / 100.0 + "，利息：" + cpsOutInterest / 100.0+",佣金: "+cpsOutCommission/100.0 + ",分润: "+cpsOutProfit/100.0, cpsFreezeSn,subject.getSubjectId(),scheduleId,
                            cpsOutPrincipal/100.0,cpsOutInterest/100.0,cpsOutCommission/100.0);
                    //查询这个scheduleId的代偿流水
                    List<CompensatoryAcctLog> logs = cpsAcctLogService.getCpsAcctLogsByStatusAndScheduleId(subjectRepayDetails.get(0).getScheduleId(),CompensatoryAcctLog.STATUS_HANDLED_LOCAL_FREEZE, BaseResponse.STATUS_SUCCEED);
                    if(!logs.isEmpty()){
                        for (CompensatoryAcctLog log:logs) {
                            cpsAcctLogService.updateByStatusAndId(CompensatoryAcctLog.STATUS_HANDLED_LOCAL_TOFREEZE,log.getId());
                        }
                    }
                }
            } else {
                logger.info("all repay details of the [{}] subject repay schedule not finished, break!", scheduleId);
            }
        }
        long endTime = System.currentTimeMillis();
        logger.info("处理借款人生成还款流水结束,执行时间{}",endTime-startTime);
        return RepeatStatus.FINISHED;
    }
}
