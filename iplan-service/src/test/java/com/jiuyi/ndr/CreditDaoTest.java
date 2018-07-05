package com.jiuyi.ndr;

import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.subject.SubjectAccountDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.user.UserOtherInfoDao;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectAccount;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.domain.user.UserOtherInfo;
import com.jiuyi.ndr.dto.iplan.IPlanTalentDto;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.redis.RedisClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangyibo on 2017/8/1.
 */
@ActiveProfiles("dev01")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class CreditDaoTest {

    @Autowired
    private CreditDao creditDao;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private UserOtherInfoDao userOtherInfoDao;
    @Autowired
    SubjectDao subjectDao;
    @Autowired
    SubjectAccountDao subjectAccountDao;
    @Autowired
    SubjectService subjectService;
    @Autowired
    SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    RedPacketService redPacketService;
    @Autowired
    IPlanAccountService iPlanAccountService;
    @Autowired
    CreditOpeningDao creditOpeningDao;


    @Test
    public void test1234(){
        Subject subject = subjectDao.findBySubjectId("NDR180122143733000051");
        List<RedPacket> redPacketList = redPacketService.getUsablePacketCreditAll("EnyeauequeYbjtkl", subject, "ios_4.6.0", "credit");
        System.out.println(redPacketList);
    }

    @Test
    public void test12(){
        double d = 1/(2*1.0);
        System.out.println("+++++++++++++"+d);
    }

    @Test
    public  void test345(){
        RedPacket redPacket = redPacketService.getRedPacketById(20);
        System.out.println(redPacket.getInvestMoney());
    }

    @Test
    public  void testTrans(){
        IPlan iPlan = iPlanService.getIPlanById(4576);
       /* int minTerm = iPlanAccountService.getYjtMinTerm(iPlan);
        double expectedInterest = subjectService.getInterestByRepayType(200000, iPlan.getFixRate(), iPlanAccountService.getRate(iPlan),
                minTerm, minTerm * 30, iPlan.getRepayType());
        System.out.println(expectedInterest);*/
        //System.out.println(iPlan.getIncreaseRate().doubleValue());
        //System.out.println(iPlan.getExitLockDays()/31==0?1:iPlan.getExitLockDays()/31);
        System.out.println(iPlan.getTerm() - iPlan.getExitLockDays()/31==0?1:iPlan.getExitLockDays()/31);
        System.out.println(iPlanAccountService.getMaxRate(iPlan));
        double d = (iPlan.getFixRate().doubleValue()+iPlan.getBonusRate().doubleValue()+iPlanAccountService.getMaxRate(iPlan))*100;
        System.out.println(d);
    }

    @Test
    public void testStream(){
        /*String date = "2018-01-30 10:00:00";
        Boolean flag = DateUtil.compareDateTime(date,"2018-01-30 00:00:00");
        System.out.println("flag-:"+flag);*/
        CreditOpening creditOpening = creditOpeningDao.findById(1736758);
        System.out.println(creditOpening.getTransferDiscount().intValue());
        System.out.println(1==(creditOpening.getTransferDiscount().intValue()));
    }

    @Test
    public void test(){
        Map<String, String> map = new HashMap<>(5);
        map.put("userId", "YBZBvmQbQfQbsmvj");
        map.put("amount", String.valueOf(10000));
        map.put("iPlanId", String.valueOf(1071));
        map.put("investRequestNo", "NDR20171121182357fa9514");
        map.put("device", "mobile");
        String iPlanTransLog = JSON.toJSONString(map);
        //logger.info("双11活动投资-理财计划id：{},用户id:{},金额：{},投资流水号：{}",iPlanId,userId,amount,investRequestNo);
        redisClient.product(GlobalConfig.DOUBLE_11_TRANS_LOG, iPlanTransLog);

    }
    @Test
    public void testChannnel(){
        String userId = "yAr2yqV7Rfi2abwn";
        UserOtherInfo userOtherInfo = userOtherInfoDao.getUserById(userId);
        if(userOtherInfo.getUserSource().contains("d_jjjx") || userOtherInfo.getUserSource().contains("d_flmf") || userOtherInfo.getUserSource().contains("d_flmf1")){
            String time = "2017-12-13 00:00:00";
            Credit credit = creditDao.findBySubjectAccountIdAndTarget(1,time);
            if(credit != null){
                System.out.println("渠道用户不可转让");
            }
        }

    }

    @Test
    public void testUpdate(){
        IPlan iPlan = iPlanService.getIPlanById(5);
        List<IPlanTalentDto> iPlanTalentDtos = null;
        if (IPlan.STATUS_RAISING_FINISH.equals(iPlan.getStatus())||IPlan.STATUS_EARNING.equals(iPlan.getStatus())||IPlan.STATUS_END.equals(iPlan.getStatus())){
            try {
                iPlanTalentDtos = redisClient.getStringToList(GlobalConfig.IPLAN_TALENT+5,IPlanTalentDto.class);
                System.out.println("redis获取对象");
                if (iPlanTalentDtos==null){
                    iPlanTalentDtos = iPlanService.getInvestorAcct(5);
                    String iPlanTransLog = JSON.toJSONString(iPlanTalentDtos);
                    System.out.println("iPlanTransLog:"+iPlanTransLog);
                    redisClient.set(GlobalConfig.IPLAN_TALENT+5,iPlanTransLog);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            iPlanTalentDtos = iPlanService.getInvestorAcct(5);
        }
        for (IPlanTalentDto i:iPlanTalentDtos) {
            System.out.println("username:"+i.getUserName()+",amount:"+i.getAmount());
        }
    }

    @Test
    public  void testCalc(){
        BigDecimal tiscount = new BigDecimal("1.005");
        Double transferDiscount = tiscount.multiply(new BigDecimal(100)).doubleValue();
        Integer totalAmt = 500;
        Double tryFee = (totalAmt /100.0) * (transferDiscount - 100) / 100.0 * 0.2;
        System.out.println(tryFee);
    }

    @Test
    public  void checkTransfer(){
        IPlanAccount iPlanAccount = iPlanAccountService.findById(13);
        iPlanAccountService.checkCondition(iPlanAccount);
    }
}
