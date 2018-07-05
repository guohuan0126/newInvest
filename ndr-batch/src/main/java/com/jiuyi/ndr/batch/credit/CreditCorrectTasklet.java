package com.jiuyi.ndr.batch.credit;

import com.jiuyi.ndr.batch.iplan.IPlanAutoInvestTasklet;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.guohuanCreditDao;
import com.jiuyi.ndr.dao.lplan.LPlanAccountDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.guohuanCredit;
import com.jiuyi.ndr.domain.lplan.LPlanAccount;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.xm.http.response.ResponseQueryIntelligentProjectOrder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

public class CreditCorrectTasklet implements Tasklet{

    private static final Logger logger = LoggerFactory.getLogger(CreditCorrectTasklet.class);

    @Autowired
    private LPlanAccountDao lPlanAccountDao;

    @Autowired
    private CreditDao creditDao;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private guohuanCreditDao guohuanCreditDao;

    @Autowired
    private SubjectRepayDetailDao subjectRepayDetailDao;

    @Autowired
    private NoticeService noticeService;


    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        //List<String> lPlanAccountIds = subjectRepayDetailDao.findUserIdByCreateTime();
        List<LPlanAccount> lPlanAccounts = lPlanAccountDao.findAll();
        logger.info("债权数据修正开始，修正用户数量--{}",lPlanAccounts.size());
        if(lPlanAccounts != null && lPlanAccounts.size() > 0) {
            for (LPlanAccount lPlanAccount : lPlanAccounts) {
                if (lPlanAccount == null) {
                    logger.info("该用户id-{}没有活期账户！！！", lPlanAccount.getUserId());
                    continue;
                }
                if (lPlanAccount.getCurrentPrincipal() == 0) {
                    logger.info("该用户{}活期账户没有持有金额！！！", lPlanAccount.getUserId());
                    continue;
                }
                ResponseQueryIntelligentProjectOrder response = null;
                try {
                    response = transactionService.queryIntelligentProjectOrder(lPlanAccount.getInvestRequestNo());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (response == null) {
                    continue;
                }
                List<ResponseQueryIntelligentProjectOrder.Detail> details = response.getDetails();
                if (details != null && details.size() > 0) {
                    for (ResponseQueryIntelligentProjectOrder.Detail detail : details) {
                        String x = StringUtils.remove(detail.getMatchShare(), ".");
                        Integer newHolding = Integer.valueOf(x);
                        System.out.println("newHolding--:" + newHolding);
                        logger.info("修正用户-{},标的—{},存管持有份额-{}", lPlanAccount.getUserId(), detail.getMatchProjectNo(), newHolding);
                        System.out.println("newHolding-----:" + newHolding);
                        if (newHolding < 0) {
                            continue;
                        }
                        List<Credit> credits = creditDao.findBySubjectIdAndUserIdAndSourceChannel(detail.getMatchProjectNo(), response.getPlatformUserNo(), Credit.SOURCE_CHANNEL_LPLAN);
                        Integer oldHolding = credits.stream().mapToInt(credit -> credit.getHoldingPrincipal()).sum();
                        System.out.println("oldHolding-----:" + oldHolding);
                        logger.info("修正用户-{},标的—{},本地持有份额-{}", lPlanAccount.getUserId(), detail.getMatchProjectNo(), oldHolding);
                        Integer s = oldHolding - newHolding;
                        System.out.println("s----:" + s);
                        if (s > 0) {
                            for (Credit credit : credits) {
                                if (credit.getHoldingPrincipal() - s > 0) {
                                    //creditDao.updateHoldingPrincipal(credit);
                                    logger.info("债权-{}已为负,无法更新,差额：{},当前持有本金：{}", credit.getId(), s, credit.getHoldingPrincipal());
                                    //noticeService.sendEmail("债权数据修正", "债权ID：" + credit.getId() + "，修正为" + credit.getHoldingPrincipal() + ",差额为：" + s, "guohuan@duanrong.com");
                                    break;
                                }

                            }

                            //lPlanAccount.setExpectedInterest(lPlanAccount.getExpectedInterest() + s);
                            //lPlanAccountDao.updateExpectedInterest(lPlanAccount);
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
            }
        }
        return RepeatStatus.FINISHED;
    }
}
