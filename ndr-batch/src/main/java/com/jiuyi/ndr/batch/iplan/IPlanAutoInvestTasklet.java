package com.jiuyi.ndr.batch.iplan;


import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.config.AutoMatchConfigDao;
import com.jiuyi.ndr.dao.config.AutoMatchPlanConfigDao;
import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.dao.lplan.LPlanTransLogDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.domain.config.AutoMatchConfig;
import com.jiuyi.ndr.domain.config.AutoMatchPlanConfig;
import com.jiuyi.ndr.domain.config.Config;
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
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zhangyibo on 2017/6/8.
 */
public class IPlanAutoInvestTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(IPlanAutoInvestTasklet.class);

    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;

    @Autowired
    private IPlanAccountDao iPlanAccountDao;

    @Autowired
    private CreditOpeningService creditOpeningService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private CreditOpeningDao creditOpeningDao;

    @Autowired
    private IPlanDao iPlanDao;

    @Autowired
    private AutoMatchConfigDao autoMatchConfigDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private AutoMatchPlanConfigDao autoMatchPlanConfigDao;

    @Autowired
    private LPlanTransLogDao lPlanTransLogDao;

    @Autowired
    private ConfigDao configDao;

    @Autowired
    private CreditDao creditDao;

    @Autowired
    private NoticeService noticeService;

    private final Map<Integer, Integer> processedAmtMap = new HashMap<>();

    private final Map<Integer, Integer> deductAmtRemainMap = new HashMap<>();

    private int currentMatchTransLogIndex;

    private long totalTransInMoney;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            long startTime = System.currentTimeMillis();

            //加载匹配策略配置
            AutoMatchPlanConfig autoMatchPlanConfig = autoMatchPlanConfigDao.findByStatus(AutoMatchPlanConfig.STATUS_ON);

            List<IPlanTransLog> iPlanTransLogs = iPlanTransLogDao.findNeedMatchTransLog();
            logger.info("待匹配的转入记录id:{}",iPlanTransLogs.stream().map(IPlanTransLog::getId).collect(Collectors.toList()));

            // 查询天天赚、月月赢待匹配资金
            long iplanTransLogTotalAmt = iPlanTransLogDao.sumRemainAmt()==null?0:iPlanTransLogDao.sumRemainAmt();
            totalTransInMoney +=iplanTransLogTotalAmt;
            logger.info("初始待匹配金额totalTransInMoney={}",iplanTransLogTotalAmt);
            logger.info("定期资产自动匹配任务开始,共有{}条定期转入记录待匹配,总资金={}", iPlanTransLogs.size(), iPlanTransLogs.parallelStream().map(IPlanTransLog::getTransAmt).reduce(Integer::sum).orElse(0));
            String[] configIds = autoMatchPlanConfig.getConfigIds().split(",");
            List<AutoMatchConfig> autoMatchConfigs = new ArrayList<>();
            for (String configId : configIds) {
                autoMatchConfigs.add(autoMatchConfigDao.findById(Integer.parseInt(configId)));
            }
            logger.info("当前时间={},所用策略={}",DateUtil.getCurrentDateTime(), autoMatchPlanConfig.getDesc());
            for (int i = 0; i < autoMatchConfigs.size(); i++) {
                AutoMatchConfig autoMatchConfig = autoMatchConfigs.get(i);
                if (AutoMatchConfig.TYPE_SUBJECT.equals(autoMatchConfig.getType())) {
                    List<Subject> subjects = (List<Subject>) SubjectService.class.getDeclaredMethod(autoMatchConfig.getMethodName(), Integer.class).invoke(subjectService, GlobalConfig.OPEN_TO_IPLAN);
                    //根据项目期限正序
                    Comparator<Subject> subjectComparator = Comparator.comparing(Subject::getTerm);
                    Collections.sort(subjects,subjectComparator);
                    logger.info("当前查询步骤:{},待匹配标的数量={}",autoMatchConfig.getDesc(),subjects.size());
                    if (subjects.size() > 0 && currentMatchTransLogIndex < iPlanTransLogs.size()) {
                        matchSubjects(iPlanTransLogs, subjects,i > autoMatchPlanConfig.getCriticalPoint());
                    }
                } else if (AutoMatchConfig.TYPE_CREDIT.equals(autoMatchConfig.getType())) {
                    List<CreditOpening> creditOpenings = (List<CreditOpening>) CreditOpeningService.class.getDeclaredMethod(autoMatchConfig.getMethodName(), Integer.class).invoke(creditOpeningService, GlobalConfig.OPEN_TO_IPLAN);
                    //根据原标的结束日期正序
                    Comparator<CreditOpening> creditOpeningComparator = Comparator.comparing(CreditOpening::getEndTime).thenComparing(CreditOpening::getOpenTime);
                    Collections.sort(creditOpenings,creditOpeningComparator);
                    logger.info("当前查询步骤:{},待匹配债权数量={}",autoMatchConfig.getDesc(),creditOpenings.size());
                    if (creditOpenings.size() > 0 && currentMatchTransLogIndex < iPlanTransLogs.size()) {
                        matchCreditOpenings(iPlanTransLogs, creditOpenings,i > autoMatchPlanConfig.getCriticalPoint());
                    }
                } else {
                    throw new ProcessException(Error.NDR_0206);
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
                Integer deductRemainAmt = deductAmtRemainMap.get(processedEntry.getKey());//已使用的抵扣券金额
                logger.info("更新账户{}，已匹配金额{},抵扣券剩余金额{}",iPlanAccount.getUserId(),processedEntry.getValue(),deductRemainAmt);
                if (deductRemainAmt < 0) {
                    logger.warn("iplan acount of user {} deductionAmt is {}, set to 0 instead.", iPlanAccount.getUserId(), deductRemainAmt);
                    deductRemainAmt = 0;
                    throw new ProcessException(Error.INTERNAL_ERROR);
                }
                if (amtToInvest < 0) {
                    logger.warn("iplan acount of user {} amtToInvest is {}, set to 0 instead.", iPlanAccount.getUserId(), amtToInvest);
                    amtToInvest = 0;
                    //throw new ProcessException(Error.INTERNAL_ERROR);
                    //return RepeatStatus.FINISHED;
                    logger.warn("月月盈自动匹配该用户{}月月盈账户{}待投资金额(amtToInvest)小于零,跳过！",iPlanAccount.getUserId(),iPlanAccount.getId());
                    noticeService.sendEmail("月月盈自动匹配待投资金额(amtToInvest)小于零,跳过","用户："+iPlanAccount.getUserId()+"月月盈账户id"+iPlanAccount.getId(),"guohuan@duanrong.com");
                    continue;
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
                List<Credit> credits = creditDao.findBySourceChannelIdAndSourceChannel(iPlanTransLog.getId(),Credit.SOURCE_CHANNEL_IPLAN);
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
            currentMatchTransLogIndex = 0;
            totalTransInMoney = 0;
        }


        return RepeatStatus.FINISHED;
    }


    private void matchCreditOpenings(List<IPlanTransLog> iPlanTransLogs, List<CreditOpening> openToIPlanCredits,boolean needRemainAmt) {
        int matchingCreditIndex = 0;//匹配到了第几个债权
        while (currentMatchTransLogIndex < iPlanTransLogs.size()) {
            logger.info("currentMatchTransLogIndex={},matchingSubjectIndex={}",currentMatchTransLogIndex,matchingCreditIndex);
            IPlanTransLog iPlanTransLog = iPlanTransLogs.get(currentMatchTransLogIndex);
            //只对正在匹配的数据进行加锁
            iPlanTransLogDao.findByIdForUpdate(iPlanTransLog.getId());
            //查询到对应的计划
            IPlan iPlan = iPlanDao.findById(iPlanTransLog.getIplanId());
            //如果资金剩余天数<=2天 则不进行匹配
            if (!checkIPlanResidualDays(iPlan)) {
                logger.warn("ID={}的理财计划剩余天数<={}天，不进行匹配操作", iPlan.getId(), GlobalConfig.IPLAN_RESIDUAL_DAYS_MIN);
                currentMatchTransLogIndex++;
                continue;
            }
            if (checkIPlanAccount(iPlanTransLog)) {
                logger.warn("ID={}的复投记录的账户已经退出，不进行匹配操作", iPlanTransLog.getId());
                currentMatchTransLogIndex++;
                continue;
            }
            logger.info("processing  invest trans {} with processedAmt {},transAmt {}", iPlanTransLog.getId(), iPlanTransLog.getProcessedAmt(), iPlanTransLog.getTransAmt());
            //查询到这笔交易对应的定期账户
            IPlanAccount iPlanAccount = iPlanAccountDao.findByAccountIdForUpdate(iPlanTransLog.getAccountId());
            processedAmtMap.putIfAbsent(iPlanTransLog.getAccountId(), 0);//init
            deductAmtRemainMap.putIfAbsent(iPlanTransLog.getAccountId(), iPlanAccount.getDedutionAmt() == null ? Integer.valueOf(0) : iPlanAccount.getDedutionAmt());//init Integer.valueOf(0)避免重复拆装箱

            //剩余可匹配金额=交易金额-已处理金额
            int remainInvestAmt = iPlanTransLog.getTransAmt() - iPlanTransLog.getProcessedAmt();

            while (matchingCreditIndex < openToIPlanCredits.size() && remainInvestAmt > 0) {
                if(needRemainAmt){
                    Config config = configDao.getConfigById(GlobalConfig.REMAIN_AMT_THRESHOLD_KEY);
                    logger.info("current lplan+iplan transferIn amt={},configAmt={}",totalTransInMoney,config.getValue());
                    if(totalTransInMoney<=Integer.parseInt(config.getValue())){
                        logger.warn("资金剩余小于预留资金阈值,不再进行匹配");
                        matchingCreditIndex++;
                        break;
                    }
                }
                CreditOpening creditOpening = openToIPlanCredits.get(matchingCreditIndex);
                logger.info("当前匹配债权：{},结束时间:{},开放时间：{}",creditOpening.getId(),creditOpening.getEndTime(),creditOpening.getOpenTime());
                creditOpeningDao.findByIdForUpdate(creditOpening.getId());
                //如果转出人跟转出人是同一个人 匹配下一个债权
                if (checkSamePerson(iPlanTransLog.getUserId(), creditOpening.getTransferorId())) {
                    logger.info("债权出让人与投资人是同一个人，不进行匹配,userId={}", iPlanTransLog.getUserId());
                    matchingCreditIndex++;
                    continue;
                }
                //是否匹配判断
                boolean isAutoInvest = isAutoInvest(creditOpening,iPlanTransLog);
                if (!isAutoInvest){
                    logger.info("是否匹配判断返回结果-{}",isAutoInvest);
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
                    if (remainDeductAmt > 0) {
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
                            Credit.SOURCE_CHANNEL_IPLAN, iPlanTransLog.getId(), deductAmt);//传交易记录的ID
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
                    if (remainDeductAmt > 0) {
                        deductAmtRemainMap.put(iPlanTransLog.getAccountId(), 0);
                        dedcutAmt = remainDeductAmt;
                        logger.info("剩余可使用抵扣券金额={}，全部使用", dedcutAmt);
                    } else {
                        dedcutAmt = 0;
                    }
                    //调用债权购买
                    creditOpeningService.buyCredit(creditOpening.getId(), remainInvestAmt, iPlanTransLog.getUserId(),
                            Credit.SOURCE_CHANNEL_IPLAN, iPlanTransLog.getId(), dedcutAmt);//传交易记录的ID
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


    private boolean isAutoInvest(CreditOpening creditOpening, IPlanTransLog iPlanTransLog) {
        if (iPlanTransLog.getUserId().equals(GlobalConfig.PLATFORM_USER)){
            IPlan iPlan = iPlanDao.findById(iPlanTransLog.getIplanId());
            if (IPlan.IPLAN_TYPE_TP.equals(iPlan.getIplanType())&&creditOpening.getSourceChannelId()!=null){
                return true;
            }
            if (creditOpening.getTransferPrincipal()<GlobalConfig.CREDIT_AVAIABLE&&creditOpening.getSourceChannelId()==null){
                return true;
            }
            return false;
        } else {
            if (creditOpening.getSourceChannelId()!=null){
                return true;
            }
            return false;
        }
    }

    private boolean checkIPlanAccount(IPlanTransLog iPlanTransLog) {
        IPlanAccount iPlanAccount = iPlanAccountDao.findById(iPlanTransLog.getAccountId());
        if (IPlanAccount.STATUS_PROCEEDS.equals(iPlanAccount.getStatus())){
            return false;
        }
        return true;
    }

    /**
     * 月月赢资金（待匹配的记录）去匹配subject标的；
     * @param iPlanTransLogs
     * @param investableSubjects
     * @param needRemainAmt
     */
    private void matchSubjects(List<IPlanTransLog> iPlanTransLogs, List<Subject> investableSubjects,boolean needRemainAmt) {
        int matchingSubjectIndex = 0;//匹配到了第几个标的
        while (currentMatchTransLogIndex < iPlanTransLogs.size()) {
            logger.info("currentMatchTransLogIndex={},matchingSubjectIndex={}",currentMatchTransLogIndex,matchingSubjectIndex);
            IPlanTransLog iPlanTransLog = iPlanTransLogs.get(currentMatchTransLogIndex);
            //只对正在匹配的数据进行加锁
            iPlanTransLogDao.findByIdForUpdate(iPlanTransLog.getId());
            IPlan iPlan = iPlanDao.findById(iPlanTransLog.getIplanId());
            if (IPlan.IPLAN_TYPE_TTZ.equals(iPlan.getIplanType())&&iPlanTransLog.getUserId().equals(GlobalConfig.PLATFORM_USER)){
                logger.warn("ID={}的投资记录为劫镖账户,月月盈为天天赚专属-{}，无需匹配", iPlanTransLog.getId(),iPlan.getId());
                currentMatchTransLogIndex++;
                continue;
            }
            //如果资金剩余天数<=2天 则不进行匹配
            if (!checkIPlanResidualDays(iPlan)) {
                logger.warn("ID={}的理财计划剩余天数<={}天，不进行匹配操作", iPlan.getId(), GlobalConfig.IPLAN_RESIDUAL_DAYS_MIN);
                currentMatchTransLogIndex++;
                continue;
            }
            if (checkIPlanAccount(iPlanTransLog)) {
                logger.warn("ID={}的复投记录的账户已经退出，不进行匹配操作", iPlanTransLog.getId());
                currentMatchTransLogIndex++;
                continue;
            }
            logger.info("processing  invest trans {} with processedAmt {},transAmt {}", iPlanTransLog.getId(), iPlanTransLog.getProcessedAmt(), iPlanTransLog.getTransAmt());
            //查询到这笔交易对应的定期账户
            IPlanAccount iPlanAccount = iPlanAccountDao.findByAccountIdForUpdate(iPlanTransLog.getAccountId());
            processedAmtMap.putIfAbsent(iPlanTransLog.getAccountId(), 0);//init
            deductAmtRemainMap.putIfAbsent(iPlanTransLog.getAccountId(), iPlanAccount.getDedutionAmt() == null ? Integer.valueOf(0) : iPlanAccount.getDedutionAmt());//init

            //剩余可匹配金额=交易金额-已处理金额
            int remainInvestAmt = iPlanTransLog.getTransAmt() - iPlanTransLog.getProcessedAmt();
            while (matchingSubjectIndex < investableSubjects.size() && remainInvestAmt > 0) {
                if(needRemainAmt){
                    Config config = configDao.getConfigById(GlobalConfig.REMAIN_AMT_THRESHOLD_KEY);
                    logger.info("current lplan+iplan transferIn amt={},configAmt={}",totalTransInMoney,config.getValue());
                    if(totalTransInMoney <= Integer.parseInt(config.getValue())){
                        logger.warn("资金剩余小于预留资金阈值,不再进行匹配");
                        matchingSubjectIndex++;
                        break;
                    }
                }
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
                    if (remainDeductAmt > 0) {
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
                    if (remainDeductAmt > 0) {
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


    private boolean checkIPlanResidualDays(IPlan iPlan) {
        if (iPlan.getEndTime() == null || StringUtils.isEmpty(iPlan.getEndTime())) {
            LocalDate raiseOpenDate = DateUtil.parseDateTime(iPlan.getRaiseOpenTime(), DateUtil.DATE_TIME_FORMATTER_19).toLocalDate();
            LocalDate iplanEndDate = raiseOpenDate.plusDays(iPlan.getTerm() * GlobalConfig.ONEMONTH_DAYS);
            return DateUtil.betweenDays(LocalDate.now(), iplanEndDate) > GlobalConfig.IPLAN_RESIDUAL_DAYS_MIN;
        } else {
            LocalDate endDate = DateUtil.parseDateTime(iPlan.getEndTime(), DateUtil.DATE_TIME_FORMATTER_19).toLocalDate();
            return DateUtil.betweenDays(LocalDate.now(), endDate) > GlobalConfig.IPLAN_RESIDUAL_DAYS_MIN;
        }
    }

    private Map<String, String> constructParamMap(IPlanTransLog iPlanTransLog, String subjectId, Integer investAmt) {
        Map<String, String> paramMap = new HashMap<>(6);
        paramMap.put("subjectId", subjectId);
        paramMap.put("investorId", iPlanTransLog.getUserId());
        paramMap.put("investorIdXm", iPlanTransLog.getUserId());
        paramMap.put("investAmt", String.valueOf(investAmt));
        paramMap.put("investChannel", String.valueOf(Credit.SOURCE_CHANNEL_IPLAN));
        paramMap.put("investChannelId", String.valueOf(iPlanTransLog.getId()));
        paramMap.put("investDevice", GlobalConfig.INVEST_DEVICE_LPLAN_AUTO);
        paramMap.put("iplanAccountId", iPlanTransLog.getAccountId() + "");

        return paramMap;
    }

}
