package com.jiuyi.test;

import com.jiuyi.ndr.BatchApplication;
import com.jiuyi.ndr.batch.subject.SubjectTradeCompensateTasklet;
import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.dao.iplan.IPlanRepayDetailDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayScheduleDao;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestModifyProject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by drw on 2017/8/10.
 */
@ActiveProfiles(value = "dev01")
@SpringBootTest(classes = BatchApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SubjectTest {


    @Autowired
    private IPlanRepayDetailDao iPlanRepayDetailDao;

    @Autowired
    private SubjectRepayScheduleDao subjectRepayScheduleDao;

    @Autowired
    private ConfigDao configDao;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    SubjectTradeCompensateTasklet subjectTradeCompensateTasklet;

    @Autowired
    IPlanAccountService iPlanAccountService;

    @Autowired
    UserAccountService userAccountService;

    @Autowired
    SubjectRepayScheduleService subjectRepayScheduleService;

    @Autowired
    SubjectDao subjectDao;


    @Test
    public void test2(){
        String dayTime = DateUtil.getCurrentDateTime14();
        String time = dayTime.substring(8);
        String timestamp = null;
        Config config = configDao.getConfigById(Config.TIME_SWITCH);
        if (config==null){
            timestamp = dayTime;
        } else {
            timestamp = config.getValue()+time;
        }
        System.out.println("timestamp-:"+timestamp);
    }

    /**
     * 厦门银行更改标的状态
     */
    private BaseResponse changeSubjectXM(String subjectId, String status) {
        RequestModifyProject modifyProject = new RequestModifyProject();
        modifyProject.setRequestNo(IdUtil.getRequestNo());
        modifyProject.setProjectNo(subjectId);
        modifyProject.setStatus(status);
        return transactionService.modifyProject(modifyProject);
    }


    @Test
   public void test112(){

        Subject subject = subjectDao.findBySubjectId("NDR180327141301000232");
        List<SubjectRepaySchedule> subjectRepaySchedules = subjectRepayScheduleDao.findBySubjectIdOrderByTerm("NDR180327141301000232");
       subjectService.changeSubjectXM(subject,Subject.SUBJECT_STATUS_REPAY_NORMAL_XM,subjectRepaySchedules);

   }
   @Test
    public void subjectTransaction(){
       subjectTradeCompensateTasklet.transferInCompensate();
   }

    @Test
    public void test111(){
        Map<String,Integer> map = new HashMap<>();
        map.put("uid",1);
        int s = map.get("uid");
        AtomicLong a = new AtomicLong(s);
        AtomicLong b = new AtomicLong(s+1000);
        List<Thread> list = new ArrayList<>();

        for(int i=0;i<100;i++){
            list.add(new Thread(()->{
                map.put("uid",b.intValue());
                Long uid = a.getAndIncrement();
                System.out.println("uid = "+uid);
                while (uid.equals(b)){
                    map.get("uid");
                }


            }));
        }
        for (Thread thread:list){
            thread.start();
        }
    }

    @Test
    public void changeSubject(){
        Subject subject = subjectService.findSubjectBySubjectId("NDR180130162209000101");
        List<SubjectRepaySchedule> schedules = subjectRepayScheduleService.findRepayScheduleBySubjectId("NDR180130162209000101");
        BaseResponse response = subjectService.changeSubjectXM(subject, Subject.SUBJECT_STATUS_REPAY_NORMAL_XM, schedules);
        System.out.println(response);
    }

    public static void main(String[] args) {

        List<GarbageCollectorMXBean> l = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean b : l) {
            System.out.println(b.getName());
        }
    }
}
