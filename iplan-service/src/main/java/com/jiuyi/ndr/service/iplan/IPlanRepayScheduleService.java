package com.jiuyi.ndr.service.iplan;

import com.github.pagehelper.PageHelper;
import com.jiuyi.ndr.dao.iplan.IPlanRepayScheduleDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.iplan.IPlanRepaySchedule;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Created by lixiaolei on 2017/6/15.
 */
@Service
public class IPlanRepayScheduleService {

    @Autowired
    private IPlanRepayScheduleDao iPlanRepayScheduleDao;

    public List<IPlanRepaySchedule> getRepayScheduleByPageHelper(Integer iPlanId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return this.getRepaySchedule(iPlanId);
    }

    public List<IPlanRepaySchedule> getRepaySchedule(Integer iPlanId) {
        if (iPlanId == null) {
            throw new IllegalArgumentException("iPlan id is can not null when query iPlan repay schedule");
        }
        return iPlanRepayScheduleDao.findByIPlanId(iPlanId);
    }

    /**
     * 查询理财计划的剩余期数
     *
     * @param iPlanId   理财计划id
     */
    @ProductSlave
    public Integer getCurrentRepayTerm(Integer iPlanId) {
        List<IPlanRepaySchedule> repayScheduleList = this.getRepaySchedule(iPlanId);
        int totalTerm = repayScheduleList.size();
        for (IPlanRepaySchedule iPlanRepaySchedule : repayScheduleList) {
            Integer term = iPlanRepaySchedule.getTerm();
            String dueDate = iPlanRepaySchedule.getDueDate();//当期还款日
            LocalDate localDate = DateUtil.parseDate(dueDate, DateUtil.DATE_TIME_FORMATTER_10);//2017-06-20
            if (LocalDate.now().isBefore(localDate)) {
                return totalTerm-term;
            }
        }
        return null;
    }

    /**
     * 查询理财计划的剩余期数
     * @param date    日期
     * @param iPlanId   理财计划id
     */
    @ProductSlave
    public Integer getRepayTerm(Integer iPlanId,String date) {
        List<IPlanRepaySchedule> repayScheduleList = this.getRepaySchedule(iPlanId);
        LocalDate transferDate = DateUtil.parseDate(date, DateUtil.DATE_TIME_FORMATTER_19);
        int totalTerm = repayScheduleList.size();
        for (IPlanRepaySchedule iPlanRepaySchedule : repayScheduleList) {
            Integer term = iPlanRepaySchedule.getTerm();
            String dueDate = iPlanRepaySchedule.getDueDate();//当期还款日
            LocalDate localDate = DateUtil.parseDate(dueDate, DateUtil.DATE_TIME_FORMATTER_10);//2017-06-20
            if (transferDate.isBefore(localDate)) {
                return totalTerm-term;
            }
        }
        return null;
    }


    public IPlanRepaySchedule getCurrentRepaySchedule(Integer id) {
        if(id == null){
            throw new IllegalArgumentException("iPlan id is can not null when query iPlan repay schedule");
        }
        List<IPlanRepaySchedule> iPlanRepaySchedules = this.getRepaySchedule(id);
        LocalDate now = LocalDate.now();
        for(IPlanRepaySchedule iPlanRepaySchedule : iPlanRepaySchedules){
            LocalDate dueDate = DateUtil.parseDate(iPlanRepaySchedule.getDueDate(), DateUtil.DATE_TIME_FORMATTER_10);
            if(now.isBefore(dueDate) || now.equals(dueDate)){
                return iPlanRepaySchedule;
            }
        }
        return null;
    }

    /**
     * 获得当前期数
     * @param iplanId
     * @return
     */
    public Integer getCurrentTermByIplanId(Integer iplanId,String date){
        List<IPlanRepaySchedule> iplanRepaySchedules = this.getRepaySchedule(iplanId);
        LocalDate now = DateUtil.parseDate(date, DateUtil.DATE_TIME_FORMATTER_8);
        for(IPlanRepaySchedule iplanRepaySchedule : iplanRepaySchedules){
            LocalDate dueDate = DateUtil.parseDate(iplanRepaySchedule.getDueDate(), DateUtil.DATE_TIME_FORMATTER_10);
            if((now.isBefore(dueDate) || now.equals(dueDate))){
                return iplanRepaySchedule.getTerm();
            }else if(now.isAfter(dueDate) && dueDate.getMonthValue()==2 && now.getMonthValue()==3 && now.getDayOfMonth()==1&& dueDate.getDayOfMonth()>27){
                return iplanRepaySchedule.getTerm();
            }
        }
        return null;
    }
}
