package com.jiuyi.test;

import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.BatchApplication;
import com.jiuyi.ndr.batch.iplan.IPlanTransLogCustomer;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.subject.SubjectRepayEmailDao;
import com.jiuyi.ndr.domain.subject.SubjectRepayEmail;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.redis.RedisClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

/**
 * Created by drw on 2017/8/4.
 */
@SpringBootTest(classes = BatchApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dem03")
public class
RedisTest {


    @Autowired
    RedisClient redisClient;

    @Autowired
    SubjectRepayEmailDao subjectRepayEmailDao;

    @Autowired
    NoticeService noticeService;

    @Autowired
    IPlanTransLogCustomer iPlanTransLogCustomer;

    /**
     * NDR180328145210000307
     NDR180328144429000301
     NDR180328144429000302
     NDR180328144430000303
     NDR180328144430000304
     NDR180328144430000305
     */
    @Test
    public  void test12(){
        List<String> subjectIds = Arrays.asList("NDR180328145210000307",
                "NDR180328144429000301", "NDR180328144429000302", "NDR180328144430000303",
                "NDR180328144430000304", "NDR180328144430000305");
        for (String subjectId : subjectIds) {
            Map<String, String> map1 = new HashMap<>();
            map1.put("signType", "giveMoneyToBorrower");
            map1.put("subjectId", subjectId);
            String contract = JSON.toJSONString(map1);
            redisClient.product("contract", contract);
        }
    }


    @Test
    public void test123(){
        //logger.info("快速抢标redis队列监听启动");
        redisClient.customer(GlobalConfig.DOUBLE_11_TRANS_LOG, iPlanTransLogCustomer);
        String json ="1111";
    }

    @Test
    public void test1234(){
        List<String> DOUBLE_11_IPLAN = redisClient.getVByList(GlobalConfig.DOUBLE_11_IPLAN,0,-1);
        String IPLAN_REDIS = "IPLAN_11";

        /*for (String s:DOUBLE_11_IPLAN) {
            redisClient.del(IPLAN_REDIS+s);
        }*/

        //logger.info("快速抢标redis队列监听启动");
        redisClient.del(GlobalConfig.DOUBLE_11_IPLAN);
    }


    @Test
    public void hashMapTest(){
        Map<String, String> map = new HashMap<>();
        map.put("message", "NDR180621123959000293");
        String json = JSON.toJSONString(map);
        redisClient.product("CASH_SUBJECT_LEND",json);
    }

    @Test
    public void WithdrawToRedisTest(){
        Map<String, String> map = new HashMap<>();
        map.put("type", "autoWithdraw");
        map.put("requetNo", "NODE1ATWD9228201806271031502170416");
        map.put("status", "success");
        map.put("failReason", null);
        String json = JSON.toJSONString(map);
        redisClient.product("CASH_SUBJECT_LEND",json);
    }


    @Test
    public void subjectRepayEmailTest(){
        SubjectRepayEmail subjectRepayEmail = new SubjectRepayEmail();
        subjectRepayEmail.setUserId("guohuan");
        subjectRepayEmail.setDate(DateUtil.getCurrentDate());
        subjectRepayEmail.setStatus(SubjectRepayEmail.STATUS_SENDED);
        subjectRepayEmailDao.insert(subjectRepayEmail);
    }

    @Test
    public void subjectRepayEmailTest1(){

        SubjectRepayEmail subjectRepayEmail = subjectRepayEmailDao.findByUserIdAndStatusAndDateAndType("guohuan",SubjectRepayEmail.STATUS_ALL,DateUtil.getCurrentDate(),SubjectRepayEmail.DIRECT_FLAG_ONE);
    }

    @Test
    public void sendEmail(){
        noticeService.sendRepayEmail("郭欢测试","guohuanceshi1234567898","guohuan@duanrong.com","guohuan",SubjectRepayEmail.DIRECT_FLAG_ONE,SubjectRepayEmail.STATUS_ALL);
    }
    @Autowired
    private Environment environment;

    @Test
    public void test1(){
        int a = (5 - 2 < 0 ? 0 : 5 - 2);
        System.out.println(a);
    }
}
