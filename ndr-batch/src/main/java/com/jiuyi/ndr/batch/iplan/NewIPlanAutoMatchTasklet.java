package com.jiuyi.ndr.batch.iplan;


import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by zhangyibo on 2017/6/8.
 */
public class NewIPlanAutoMatchTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(NewIPlanAutoMatchTasklet.class);

    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;

    @Autowired
    private IPlanAccountDao iPlanAccountDao;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private IPlanDao iPlanDao;

    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private CreditDao creditDao;

    @Autowired
    private NoticeService noticeService;
    @Autowired
    private CreditOpeningService creditOpeningService;
    @Autowired
    private CreditOpeningDao creditOpeningDao;

    private final Map<Integer, Integer> processedAmtMap = new HashMap<>();

    private final Map<Integer, Integer> deductAmtRemainMap = new HashMap<>();

    private int totalTransInMoney;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            long startTime = System.currentTimeMillis();

            List<IPlanTransLog> iPlanTransLogs = iPlanTransLogDao.findNeedMatchYjtTransLog();
            logger.info("待匹配的转入记录id:{}",iPlanTransLogs.stream().map(IPlanTransLog::getId).collect(Collectors.toList()));
            logger.info("一键投资产自动匹配任务开始,共有{}条定期转入记录待匹配,总资金={}", iPlanTransLogs.size(), iPlanTransLogs.parallelStream().map(IPlanTransLog::getTransAmt).reduce(Integer::sum).orElse(0));
            //根据iplanid分组
            Map<Integer,List<IPlanTransLog>> groupMap = iPlanTransLogs.stream().collect(Collectors.groupingBy(IPlanTransLog::getIplanId));
            for (Map.Entry<Integer,List<IPlanTransLog>> groupMapEntry:groupMap.entrySet()){
                int iplanId = groupMapEntry.getKey();
                IPlan iPlan = iPlanDao.findById(iplanId);
                if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                    if (IPlan.PACKAGING_TYPE_SUBJECT.equals(iPlan.getPackagingType())){
                        List<Subject> subjects = subjectService.findSubjects(iplanId);
                        List<IPlanTransLog> matchIplanTransLogs = groupMapEntry.getValue();
                        logger.info("待匹配的省心投：{},下面的转入记录id:{},待匹配标的：{}",iplanId,matchIplanTransLogs.stream().map(IPlanTransLog::getId).collect(Collectors.toList()),subjects.stream().map(Subject::getSubjectId).collect(Collectors.toList()));
                        if (subjects.size() > 0 && matchIplanTransLogs.size()>0) {
                            matchSubjects(matchIplanTransLogs, subjects);
                        }
                    } else if (IPlan.PACKAGING_TYPE_CREDIT.equals(iPlan.getPackagingType())){
                        List<CreditOpening> creditOpenings = creditOpeningDao.findByStatusAndOpenChannelAndIplanId(CreditOpening.STATUS_OPENING,CreditOpening.OPEN_CHANNEL_YJT,iplanId);
                        List<IPlanTransLog> matchIplanTransLogs = groupMapEntry.getValue();
                        logger.info("待匹配的省心投：{},下面的转入记录id:{},待匹配债权：{}",iplanId,matchIplanTransLogs.stream().map(IPlanTransLog::getId).collect(Collectors.toList()),creditOpenings.stream().map(CreditOpening::getId).collect(Collectors.toList()));
                        if (creditOpenings.size() > 0 && matchIplanTransLogs.size()>0) {
                            matchCreditOpenings(matchIplanTransLogs, creditOpenings);
                        }
                    }
                } else {
                    logger.error("该投资记录不属于一键投产品无法匹配-{}",iplanId);
                }
            }

            logger.info("match result:{}", JSON.toJSONString(processedAmtMap));
            logger.info("deduct result:{}",JSON.toJSONString(deductAmtRemainMap));
            /**
             * 更新定期账户
             */
            for (Map.Entry<Integer, Integer> processedEntry : processedAmtMap.entrySet()) {
                IPlanAccount iPlanAccount = iPlanAccountDao.findByIdForUpdate(processedEntry.getKey());
                int amtToInvest = iPlanAccount.getAmtToInvest() - processedEntry.getValue();
                iPlanAccount.setAmtToInvest(amtToInvest);
                //已使用的抵扣券金额
                Integer deductRemainAmt = deductAmtRemainMap.get(processedEntry.getKey());
                logger.info("更新账户{}，已匹配金额{},抵扣券剩余金额{}",iPlanAccount.getUserId(),processedEntry.getValue(),deductRemainAmt);
                if (deductRemainAmt < 0) {
                    logger.warn("iplan acount of user {} deductionAmt is {}, set to 0 instead.", iPlanAccount.getUserId(), deductRemainAmt);
                    deductRemainAmt = 0;
                    throw new ProcessException(Error.INTERNAL_ERROR);
                }
                if (amtToInvest < 0) {
                    logger.warn("iplan acount of user {} amtToInvest is {}, set to 0 instead.", iPlanAccount.getUserId(), amtToInvest);
                    amtToInvest = 0;
                    throw new ProcessException(Error.INTERNAL_ERROR);
                }
                iPlanAccount.setDedutionAmt(deductRemainAmt);
                iPlanAccount.setAmtToInvest(amtToInvest);
                iPlanAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
                iPlanAccountDao.update(iPlanAccount);
            }

            logger.info("账户更新完成");
            //每次匹配完成后都将转入与形成的债权进行校验 如果发现不对立即抛出异常以回滚数据
            int count = 0;
            for(IPlanTransLog iPlanTransLog:iPlanTransLogs){
                count++;
                logger.info("开始校验匹配是否正确{},{}/{}",iPlanTransLog.getId(),count,iPlanTransLogs.size());
                if(!processedAmtMap.containsKey(iPlanTransLog.getUserId())&&iPlanTransLog.getProcessedAmt()==0) {
                    logger.info("快速跳过{}",iPlanTransLog.getId());
                    continue; //参与过匹配的才需要校验
                }
                //查询这笔转入交易买到的债权
                List<Credit> credits = creditDao.findBySourceChannelIdAndSourceChannel(iPlanTransLog.getId(),Credit.SOURCE_CHANNEL_YJT);
                Integer creditsPrincipal = credits.stream().map(Credit::getInitPrincipal).reduce(Integer::sum).orElse(0);
                if(!iPlanTransLog.getProcessedAmt().equals(creditsPrincipal)){
                    logger.warn("转入记录{}已处理金额与形成债权金额不一致,已处理金额={},债权总金额={},所形成的债权明细为:{}",iPlanTransLog.getId(),iPlanTransLog.getProcessedAmt(),creditsPrincipal,JSON.toJSONString(credits));
                    throw new ProcessException(Error.NDR_0306);
                }
            }

            long endTime = System.currentTimeMillis();
            logger.info("定期资产自动匹配执行完毕，总耗时:" + (endTime - startTime));
        } finally {
            //清空账户记录信息
            processedAmtMap.clear();
            deductAmtRemainMap.clear();
            totalTransInMoney = 0;
        }


        return RepeatStatus.FINISHED;
    }
    /**
     * 一键投资金（待匹配的记录）去匹配subject标的；
     * @param iPlanTransLogs
     * @param investableSubjects
     */
    private void matchSubjects(List<IPlanTransLog> iPlanTransLogs, List<Subject> investableSubjects) {
        int matchingSubjectIndex = 0;//匹配到了第几个标的
        int currentMatchTransLogIndex=0;
        while (currentMatchTransLogIndex < iPlanTransLogs.size()) {
            logger.info("currentMatchTransLogIndex={},matchingSubjectIndex={}",currentMatchTransLogIndex,matchingSubjectIndex);
            IPlanTransLog iPlanTransLog = iPlanTransLogs.get(currentMatchTransLogIndex);
            //只对正在匹配的数据进行加锁
            iPlanTransLogDao.findByIdForUpdate(iPlanTransLog.getId());
            logger.info("processing  invest trans {} with processedAmt {},transAmt {}", iPlanTransLog.getId(), iPlanTransLog.getProcessedAmt(), iPlanTransLog.getTransAmt());
            //查询到这笔交易对应的定期账户
            IPlanAccount iPlanAccount = iPlanAccountDao.findByAccountIdForUpdate(iPlanTransLog.getAccountId());
            processedAmtMap.putIfAbsent(iPlanTransLog.getAccountId(), 0);//init
            deductAmtRemainMap.putIfAbsent(iPlanTransLog.getAccountId(), iPlanAccount.getDedutionAmt() == null ? Integer.valueOf(0) : iPlanAccount.getDedutionAmt());//init

            //剩余可匹配金额=交易金额-已处理金额
            int remainInvestAmt = iPlanTransLog.getTransAmt() - iPlanTransLog.getProcessedAmt();
            while (matchingSubjectIndex < investableSubjects.size() && remainInvestAmt > 0) {
                Subject subject = investableSubjects.get(matchingSubjectIndex);
                subjectDao.findByIdForUpdate(subject.getId());//加锁
                if (checkSamePerson(iPlanTransLog.getUserId(), subject.getBorrowerId())) {
                    logger.info("标的借款人与投资人是同一个人，不进行不配,userId={}", iPlanTransLog.getUserId());
                    matchingSubjectIndex++;
                    continue;
                }
                if (remainInvestAmt > subject.getAvailableAmt() || remainInvestAmt == subject.getAvailableAmt()) {
                    Map<String, String> paramMap = constructParamMap(iPlanTransLog, subject.getSubjectId(), subject.getAvailableAmt());
                    logger.info("match subject {}: user {}, trans-log: {}, spend {}", subject.getSubjectId(), iPlanTransLog.getUserId(), iPlanTransLog.getId(), subject.getAvailableAmt());
                    int remainDeductAmt = deductAmtRemainMap.get(iPlanAccount.getId());
                    if (remainDeductAmt > 0 && remainInvestAmt >= remainDeductAmt) {
                        if (remainDeductAmt < subject.getAvailableAmt()) {
                            deductAmtRemainMap.put(iPlanTransLog.getAccountId(), 0);
                            paramMap.put("deductAmt", String.valueOf(remainDeductAmt));
                        } else {
                            deductAmtRemainMap.put(iPlanTransLog.getAccountId(), remainDeductAmt - subject.getAvailableAmt());
                            paramMap.put("deductAmt", String.valueOf(subject.getAvailableAmt()));
                        }
                    } else {
                        paramMap.put("deductAmt", String.valueOf(0));
                    }
                    subjectService.tender(paramMap);
                    //账户剩余可投资金额-已匹配的金额
                    remainInvestAmt -= subject.getAvailableAmt();
                    totalTransInMoney -= subject.getAvailableAmt();
                    logger.info("当前待匹配金额totalTransInMoney={},此次减掉标的金额：{}",totalTransInMoney,subject.getAvailableAmt());

                    //更新交易记录中的处理中金额 主要用来在后面统一更新交易记录用
                    iPlanTransLog.setProcessedAmt(iPlanTransLog.getProcessedAmt() + subject.getAvailableAmt());
                    Integer oldValue = processedAmtMap.get(iPlanTransLog.getAccountId());
                    processedAmtMap.put(iPlanTransLog.getAccountId(), oldValue + subject.getAvailableAmt());
                    subject.setAvailableAmt(0);
                    matchingSubjectIndex++;
                } else {
                    Map<String, String> paramMap = constructParamMap(iPlanTransLog, subject.getSubjectId(), remainInvestAmt);
                    logger.info("match subject {}: user {}, trans-log: {}, spend {}", subject.getSubjectId(), iPlanTransLog.getUserId(), iPlanTransLog.getId(), remainInvestAmt);
                    int remainDeductAmt = deductAmtRemainMap.get(iPlanAccount.getId());
                    if (remainDeductAmt > 0 && remainInvestAmt >= remainDeductAmt) {
                        deductAmtRemainMap.put(iPlanTransLog.getAccountId(), 0);
                        paramMap.put("deductAmt", String.valueOf(remainDeductAmt));
                    } else {
                        paramMap.put("deductAmt", String.valueOf(0));
                    }
                    subjectService.tender(paramMap);
                    //更新交易记录中的处理中金额 主要用来在后面统一更新交易记录用
                    iPlanTransLog.setProcessedAmt(iPlanTransLog.getProcessedAmt() + remainInvestAmt);
                    Integer oldValue = processedAmtMap.get(iPlanTransLog.getAccountId());
                    processedAmtMap.put(iPlanTransLog.getAccountId(), oldValue + remainInvestAmt);
                    subject.setAvailableAmt(subject.getAvailableAmt() - remainInvestAmt);
                    remainInvestAmt = 0;
                    totalTransInMoney -= remainInvestAmt;
                    logger.info("当前待匹配金额totalTransInMoney={},此次减掉匹配金额：{}",totalTransInMoney,remainInvestAmt);

                }
                if (remainInvestAmt == 0) {
                    //如果剩余金额为0 更新这笔交易的状态为成功
                    iPlanTransLog.setTransStatus(IPlanTransLog.TRANS_STATUS_SUCCEED);
                    currentMatchTransLogIndex++;
                }
            }
            iPlanTransLogDao.update(iPlanTransLog);

            //不再循环转入 快速跳出
            if (matchingSubjectIndex >= investableSubjects.size()){
                logger.warn("快速跳出");
                break;
            }
        }
    }

    /**
     * 检查转入人跟转出人是不是同一个人
     *
     * @return
     */
    private boolean checkSamePerson(String userId1, String userId2) {
        return userId1.equals(userId2);
    }

    private Map<String, String> constructParamMap(IPlanTransLog iPlanTransLog, String subjectId, Integer investAmt) {
        Map<String, String> paramMap = new HashMap<>(6);
        paramMap.put("subjectId", subjectId);
        paramMap.put("investorId", iPlanTransLog.getUserId());
        paramMap.put("investorIdXm", iPlanTransLog.getUserId());
        paramMap.put("investAmt", String.valueOf(investAmt));
        paramMap.put("investChannel", String.valueOf(Credit.SOURCE_CHANNEL_YJT));
        paramMap.put("investChannelId", String.valueOf(iPlanTransLog.getId()));
        paramMap.put("investDevice", GlobalConfig.INVEST_DEVICE_LPLAN_AUTO);
        paramMap.put("iplanAccountId", iPlanTransLog.getAccountId() + "");

        return paramMap;
    }
    /**
     * 一键投资金（待匹配的记录）去匹配creditOpending债权；
     * @param iPlanTransLogs
     * @param openToIPlanCredits
     */
    public void matchCreditOpenings(List<IPlanTransLog> iPlanTransLogs, List<CreditOpening> openToIPlanCredits) {
        int matchingCreditIndex = 0;//匹配到了第几个债权
        int currentMatchTransLogIndex = 0;
        while (currentMatchTransLogIndex < iPlanTransLogs.size()) {
            logger.info("currentMatchTransLogIndex={},matchingSubjectIndex={}",currentMatchTransLogIndex,matchingCreditIndex);
            IPlanTransLog iPlanTransLog = iPlanTransLogs.get(currentMatchTransLogIndex);
            //只对正在匹配的数据进行加锁
            iPlanTransLogDao.findByIdForUpdate(iPlanTransLog.getId());
            logger.info("processing  invest trans {} with processedAmt {},transAmt {}", iPlanTransLog.getId(), iPlanTransLog.getProcessedAmt(), iPlanTransLog.getTransAmt());
            //查询到这笔交易对应的定期账户
            IPlanAccount iPlanAccount = iPlanAccountDao.findByAccountIdForUpdate(iPlanTransLog.getAccountId());
            processedAmtMap.putIfAbsent(iPlanTransLog.getAccountId(), 0);//init
            deductAmtRemainMap.putIfAbsent(iPlanTransLog.getAccountId(), iPlanAccount.getDedutionAmt() == null ? Integer.valueOf(0) : iPlanAccount.getDedutionAmt());//init Integer.valueOf(0)避免重复拆装箱

            //剩余可匹配金额=交易金额-已处理金额
            int remainInvestAmt = iPlanTransLog.getTransAmt() - iPlanTransLog.getProcessedAmt();

            while (matchingCreditIndex < openToIPlanCredits.size() && remainInvestAmt > 0) {
                CreditOpening creditOpening = openToIPlanCredits.get(matchingCreditIndex);
                logger.info("当前匹配债权：{},结束时间:{},开放时间：{}",creditOpening.getId(),creditOpening.getEndTime(),creditOpening.getOpenTime());
                creditOpeningDao.findByIdForUpdate(creditOpening.getId());
                //如果转出人跟转出人是同一个人 匹配下一个债权
                if (checkSamePerson(iPlanTransLog.getUserId(), creditOpening.getTransferorId())) {
                    logger.info("债权出让人与投资人是同一个人，不进行匹配,userId={}", iPlanTransLog.getUserId());
                    matchingCreditIndex++;
                    continue;
                }
                //根据折让率计算出购买这笔债权需要的金额
                int discountAvailablePrincipal = new BigDecimal(creditOpening.getAvailablePrincipal()).multiply(creditOpening.getTransferDiscount()).intValue();

                if (remainInvestAmt > discountAvailablePrincipal || remainInvestAmt == discountAvailablePrincipal) {
                    //如果账户待投资金额+抵扣券大于该债权的本金价值 购买该债权的所有份数 并匹配下一个债权
                    logger.info("match credit-opening {}: user {}, trans-log: {},principal: {}, spend {}", creditOpening.getId(), iPlanTransLog.getUserId(), iPlanTransLog.getId(), creditOpening.getAvailablePrincipal(), discountAvailablePrincipal);
                    int remainDeductAmt = deductAmtRemainMap.get(iPlanAccount.getId());
                    int deductAmt;
                    if (remainDeductAmt > 0 && remainInvestAmt >= remainDeductAmt) {
                        if (remainDeductAmt < discountAvailablePrincipal) {
                            deductAmtRemainMap.put(iPlanTransLog.getAccountId(), 0);
                            deductAmt = remainDeductAmt;
                            logger.info("剩余可使用抵扣券金额={}，全部使用", deductAmt);
                        } else {
                            deductAmtRemainMap.put(iPlanTransLog.getAccountId(), remainDeductAmt - discountAvailablePrincipal);
                            deductAmt = discountAvailablePrincipal;
                            logger.info("剩余可使用抵扣券金额={}，部分使用", deductAmt);
                        }
                    } else {
                        deductAmt = 0;
                    }
                    creditOpeningService.buyCredit(creditOpening.getId(), creditOpening.getAvailablePrincipal(), iPlanTransLog.getUserId(),
                            Credit.SOURCE_CHANNEL_YJT, iPlanTransLog.getId(), deductAmt);//传交易记录的ID
                    //账户剩余可投资金额-已匹配的金额
                    creditOpening.setAvailablePrincipal(0);//更新当前匹配中的债权对象的金额 与数据库保持一致
                    remainInvestAmt -= discountAvailablePrincipal;
                    totalTransInMoney -= discountAvailablePrincipal;
                    logger.info("当前待匹配金额totalTransInMoney={},此次减掉债权金额：{}",totalTransInMoney,discountAvailablePrincipal);
                    matchingCreditIndex++;
                    //更新交易记录中的处理中金额 主要用来在后面统一更新交易记录用
                    iPlanTransLog.setProcessedAmt(iPlanTransLog.getProcessedAmt() + discountAvailablePrincipal);
                    Integer oldValue = processedAmtMap.get(iPlanTransLog.getAccountId());
                    processedAmtMap.put(iPlanTransLog.getAccountId(), oldValue + discountAvailablePrincipal);
                } else {
                    //如果账户待投资金额小于该债权的本金价值 购买该债权的份数=购买的本金/债权的剩余可购买本金*剩余份数
                    logger.info("match credit-opening {}: user {}, trans-log: {},units: {}, spend {}", creditOpening.getId(), iPlanTransLog.getUserId(), iPlanTransLog.getId(), remainInvestAmt, remainInvestAmt);
                    int remainDeductAmt = deductAmtRemainMap.get(iPlanAccount.getId());
                    int dedcutAmt;
                    if (remainDeductAmt > 0 && remainInvestAmt >= remainInvestAmt) {
                        deductAmtRemainMap.put(iPlanTransLog.getAccountId(), 0);
                        dedcutAmt = remainDeductAmt;
                        logger.info("剩余可使用抵扣券金额={}，全部使用", dedcutAmt);
                    } else {
                        dedcutAmt = 0;
                    }
                    //调用债权购买
                    creditOpeningService.buyCredit(creditOpening.getId(), remainInvestAmt, iPlanTransLog.getUserId(),
                            Credit.SOURCE_CHANNEL_YJT, iPlanTransLog.getId(), dedcutAmt);//传交易记录的ID
                    creditOpening.setAvailablePrincipal(creditOpening.getAvailablePrincipal() - remainInvestAmt);
                    //更新交易记录中的处理中金额 主要用来在后面统一更新交易记录用
                    iPlanTransLog.setProcessedAmt(iPlanTransLog.getProcessedAmt() + remainInvestAmt);
                    Integer oldValue = processedAmtMap.get(iPlanTransLog.getAccountId());
                    processedAmtMap.put(iPlanTransLog.getAccountId(), oldValue + remainInvestAmt);
                    remainInvestAmt = 0;
                    totalTransInMoney -= remainInvestAmt;
                    logger.info("当前待匹配金额totalTransInMoney={},此次减掉匹配掉金额：{}",totalTransInMoney,remainInvestAmt);

                }
                //如果剩余金额为0 更新这笔交易的状态为成功
                if (remainInvestAmt == 0) {
                    iPlanTransLog.setTransStatus(IPlanTransLog.TRANS_STATUS_SUCCEED);
                    currentMatchTransLogIndex++;
                }
            }
            iPlanTransLogDao.update(iPlanTransLog);

            //break fast 不再循环转入数据
            if (matchingCreditIndex >= openToIPlanCredits.size()){
                logger.warn("快速跳出");
                break;
            }

        }
    }




}
