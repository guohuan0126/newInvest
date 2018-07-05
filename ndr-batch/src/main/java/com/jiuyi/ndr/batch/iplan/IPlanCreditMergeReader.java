package com.jiuyi.ndr.batch.iplan;

import com.jiuyi.ndr.batch.AbstractListItemReader;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.iplan.IPlanCreditMerge;
import com.jiuyi.ndr.service.credit.IPlanCreditMergeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 姜广兴
 * @date 2018-04-27
 */
@Component
@Scope("step")
public class IPlanCreditMergeReader extends AbstractListItemReader<String> {
    private CreditDao creditDao;
    private IPlanCreditMergeService iPlanCreditMergeService;

    @Autowired
    public IPlanCreditMergeReader(CreditDao creditDao, IPlanCreditMergeService iPlanCreditMergeService) {
        this.creditDao = creditDao;
        this.iPlanCreditMergeService = iPlanCreditMergeService;
    }

    @Override
    protected List<String> setList() {
        if (iPlanCreditMergeService.getByStatus(IPlanCreditMerge.MergeStatus.STATUS_NOT_DEAL.getCode()).isPresent()) {
            List<Credit> subjects = creditDao.findNeedMergeSubjects(GlobalConfig.CREDIT_AVAIABLE, GlobalConfig.PLATFORM_USER);
            if (!CollectionUtils.isEmpty(subjects)) {
                //按标的下债权总额倒叙排序
                return subjects.parallelStream()
                        .sorted(Comparator.comparing(Credit::getHoldingPrincipal).reversed())
                        .map(Credit::getSubjectId)
                        .collect(Collectors.toList());
            }
        } else {
            logger.info("未查询到待合并记录。");
        }
        return null;
    }
}
