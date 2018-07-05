package com.jiuyi.ndr.service.subject;

import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.subject.SubjectAdvancedPayOffPenaltyDefDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectAdvancedPayOffPenaltyDef;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 标的提前结清服务
 * Created by lixiaolei on 2017/4/11.
 */
@Service
public class SubjectAdvancedPayOffService {

    private final static Logger logger = LoggerFactory.getLogger(SubjectAdvancedPayOffService.class);

    @Autowired
    private SubjectAdvancedPayOffPenaltyDefDao subjectAdvancedPayOffPenaltyDefDao;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;

    /**
     * 提前结清罚息
     */
    @ProductSlave
    public Integer advancedPayOff(String subjectId) {
        Subject subject = subjectService.getBySubjectId(subjectId);
        SubjectAdvancedPayOffPenaltyDef subjectAdvancedPayOffPenaltyDef = this.findAdvancedPayOffPenaltyDef(subject.getAdvancedPayoffPenalty());
        List<SubjectRepaySchedule> subjectRepaySchedules = subjectRepayScheduleService.findRepayScheduleBySubjectId(subjectId);
        Integer penalty = 0;
        int days = Long.valueOf(DateUtil.betweenDays(subjectRepaySchedules.get(subjectRepaySchedules.size() - 1).getDueDate(), DateUtil.getCurrentDateShort())).intValue();
        if (SubjectAdvancedPayOffPenaltyDef.PENALTY_BASE_LOAN_PRINCIPAL.equalsIgnoreCase(subjectAdvancedPayOffPenaltyDef.getPenaltyBase())) {
            penalty = this.calcAdvancedPayOffPenalty(subject.getTotalAmt(), subjectAdvancedPayOffPenaltyDef.getPenaltyRate(), days);
        }
        if (SubjectAdvancedPayOffPenaltyDef.PENALTY_BASE_RESIDUAL_PRINCIPAL.equalsIgnoreCase(subjectAdvancedPayOffPenaltyDef.getPenaltyBase())) {
            penalty = this.calcAdvancedPayOffPenalty(subject.getTotalAmt() - subject.getPaidPrincipal(), subjectAdvancedPayOffPenaltyDef.getPenaltyRate(), days);
        }
        return penalty;
    }

    /**
     * 新增提前结清记录
     *
     * @param subjectAdvancedPayOffPenaltyDef 实体类
     */
    public int addAdvancedPayOffPenaltyDef(SubjectAdvancedPayOffPenaltyDef subjectAdvancedPayOffPenaltyDef) {
        subjectAdvancedPayOffPenaltyDef.setCreateTime(DateUtil.getCurrentDateTime19());
        return subjectAdvancedPayOffPenaltyDefDao.insert(subjectAdvancedPayOffPenaltyDef);
    }

    /**
     * 查询提前结清记录
     *
     * @param payOffId 主键id
     */
    public SubjectAdvancedPayOffPenaltyDef findAdvancedPayOffPenaltyDef(Integer payOffId) {
        if (payOffId == null) {
            throw new IllegalArgumentException("查询提前结清罚息定义时，罚息定义id不能为空");
        }
        return subjectAdvancedPayOffPenaltyDefDao.findById(payOffId);
    }

    /**
     * 提前结清罚息计算
     * 算法 ： 基数 * 利率（年）/ 一年的天数（360） * 天数
     *
     * @param baseAmt   基数
     * @param rate      利率
     * @param days      天数
     */
    private Integer calcAdvancedPayOffPenalty(Integer baseAmt, BigDecimal rate, Integer days) {
        Integer penaltyDaily = new BigDecimal(baseAmt).multiply(rate).multiply(new BigDecimal(days))
                .divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS), 6, BigDecimal.ROUND_HALF_UP).intValue();
        return penaltyDaily;
    }

}
