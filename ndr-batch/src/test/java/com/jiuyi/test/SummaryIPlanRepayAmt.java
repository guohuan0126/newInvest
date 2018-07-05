package com.jiuyi.test;

import com.jiuyi.ndr.BatchApplication;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.dao.iplan.IPlanRepayScheduleDao;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanRepaySchedule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by zhangyibo on 2017/8/7.
 */
@SpringBootTest(classes = BatchApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SummaryIPlanRepayAmt {

    @Autowired
    private IPlanRepayScheduleDao iPlanRepayScheduleDao;

    @Autowired
    private IPlanAccountDao iPlanAccountDao;

    /*@Test
    public void test(){
        Map<String,SummaryEntity> map = new TreeMap<>();

        List<IPlanRepaySchedule> iPlanRepayScheduleList = iPlanRepayScheduleDao.findAll();
        for(IPlanRepaySchedule iPlanRepaySchedule:iPlanRepayScheduleList){
            List<IPlanAccount> iPlanAccounts = iPlanAccountDao.findByIPlanId(iPlanRepaySchedule.getIplanId());
            Integer freezeInterest = iPlanAccounts.stream().map(IPlanAccount::getPaidInterest).reduce(Integer::sum).orElse(0);
            if(map.containsKey(iPlanRepaySchedule.getDueDate())){
                SummaryEntity oldEntity = map.get(iPlanRepaySchedule.getDueDate());
                SummaryEntity newEntity = new SummaryEntity();
                newEntity.setDueDate(iPlanRepaySchedule.getDueDate());
                newEntity.setDuePrincipal(oldEntity.getDuePrincipal()+iPlanRepaySchedule.getDuePrincipal());
                newEntity.setComposateInterest(oldEntity.getComposateInterest()+iPlanRepaySchedule.getDueInterest()-freezeInterest);
                map.put(iPlanRepaySchedule.getDueDate(),newEntity);
            }else{
                SummaryEntity entity = new SummaryEntity();
                entity.setDueDate(iPlanRepaySchedule.getDueDate());
                entity.setDuePrincipal(iPlanRepaySchedule.getDuePrincipal());
                entity.setComposateInterest(iPlanRepaySchedule.getDueInterest()-freezeInterest);
                map.put(iPlanRepaySchedule.getDueDate(),entity);
            }
        }

        map.entrySet().stream().forEach(System.out::println);
    }
*/
    private class SummaryEntity{

        private String dueDate;

        private Integer duePrincipal;

        private Integer composateInterest;

        public String getDueDate() {
            return dueDate;
        }

        public void setDueDate(String dueDate) {
            this.dueDate = dueDate;
        }

        public Integer getDuePrincipal() {
            return duePrincipal;
        }

        public void setDuePrincipal(Integer duePrincipal) {
            this.duePrincipal = duePrincipal;
        }

        public Integer getComposateInterest() {
            return composateInterest;
        }

        public void setComposateInterest(Integer composateInterest) {
            this.composateInterest = composateInterest;
        }

        @Override
        public String toString() {
            return "SummaryEntity{" +
                    "dueDate='" + dueDate + '\'' +
                    ", duePrincipal=" + duePrincipal +
                    ", composateInterest=" + composateInterest +
                    '}';
        }
    }

}
