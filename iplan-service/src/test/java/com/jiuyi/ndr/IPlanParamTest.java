package com.jiuyi.ndr;

import com.duanrong.util.client.DRHTTPClient;
import com.jiuyi.ndr.dao.iplan.IPlanParamDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanParam;
import com.jiuyi.ndr.service.drpay.DrpayService;
import com.jiuyi.ndr.service.drpay.FastJsonUtil;
import com.jiuyi.ndr.service.drpay.MarketingResponse;
import com.jiuyi.ndr.service.drpay.Sign;
import com.jiuyi.ndr.service.invest.InvestService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.redis.RedisClient;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.bouncycastle.util.encoders.Base64;
import org.jasypt.encryption.StringEncryptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by yumin on 2017/6/12.
 */
@ActiveProfiles("dem03")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class IPlanParamTest {

    @Autowired
    private IPlanParamDao iPlanParamDao;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private InvestService investService;
    /*@Autowired
    private RedisLock redisLock;*/
    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private IPlanAccountService iPlanAccountService;

    @Test
    public void insertIPlanParam(){
        IPlanParam iPlanParam = new IPlanParam();
        iPlanParam.setInvestMin(100);
        iPlanParam.setInvestMax(1000000);
        iPlanParam.setInvestIncrement(100);
        iPlanParam.setAutoInvestRatio(new BigDecimal(0.2));
        iPlanParam.setExitFeeRate(new BigDecimal(0.1));
        iPlanParam.setCreateTime(DateUtil.getCurrentDateTime19());
        int total = iPlanParamDao.insert(iPlanParam);
        System.out.println("total = " + total);
    }
   @Test
    public void  findAll(){
       List<IPlanParam> list = iPlanParamDao.findAll();
       for (int i = 0; i < list.size(); i++) {
           System.out.println(list.get(i).getId()+"最大金额："+list.get(i).getInvestMax());
       }
   }
    @Test
    public void test1() {
        IPlan iPlan = iPlanService.getIPlanById(12);
        redPacketService.getBestRedPackets("niQZza3uUJVzrovn", iPlan, 10000, "1");
    }

    @Test
    public void testRedis() {
        IPlanAccount account = iPlanAccountService.findById(67);
        IPlan iplan = iPlanService.getIPlanById(70);
       iPlanAccountService.calcInterest(account,iplan);
    }

    @Test
    public void testRedisLock() {
        System.out.println(BigDecimal.valueOf(0.0023).compareTo(BigDecimal.ZERO)>0);
    }

    @Test
    public void testDrpay() {
//        Map<String, Object> map = new HashMap<>();
//        map.put("userId", "feUV3qnA3uIjbihn");
//        map.put("iplanTransLogId", 15);
//        map.put("money", 87.93D);
//        map.put("rechargeWay", "quick");
//        map.put("userSource", "android_1.0");
//        DrpayResponse drpayResponse = DrpayResponse.toGeneratorJSON(DrpayService.post(DrpayService.RECHARGE_AND_INVEST, map));
//        System.out.println("充值并投资Drpay请求结果：" + drpayResponse.toString());
        Map<String, Object> map = new HashMap<>();
        map.put("source", MarketingResponse.SOURCE_APP);
        map.put("invest_type", MarketingResponse.INVEST_TYPE_IPLAN);
        map.put("userId", "RbYZjqRVNNNvitqm");
        MarketingResponse response = MarketingResponse.toGeneratorJSON(DrpayService.send("http://dem01-soa-marketing.duanrong.com/web/invest/getInvestSuccess.do", map));
        System.out.println(response.toString());
    }

    @Test
    public void test() throws IOException {
        String timestamp = Long.toString(new Date().getTime());
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("userId", "feUV3qnA3uIjbihn");
        map.put("iplanTransLogId", 15);
        map.put("money", 87.93D);
        map.put("rechargeWay", "quick");
        map.put("userSource", "android_1.0");
        String specifyJson = FastJsonUtil.objToJson(map);
        String data = new String(Base64.encode(specifyJson.getBytes()), "utf-8");
        String version = "1.0.0";
        String source = "pc";
        String str = timestamp + "|" + source + "|" + version + "|" + data;
        String sign = Sign.sign(str, "duanrongf0f22ac60d07407cfb7c587f9cab");
        System.out.println("sign:"+sign);
        System.out.println("data:"+data);
        System.out.println("timestamp:"+timestamp);
        System.out.println("version:"+version);
        System.out.println("source:"+source);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("version", version));
        params.add(new BasicNameValuePair("source", source));
        params.add(new BasicNameValuePair("timestamp", timestamp));
        params.add(new BasicNameValuePair("sign", sign));
        params.add(new BasicNameValuePair("data", data));
        String result = DRHTTPClient.sendHTTPRequestPostToString("http://localhost:8080/trade/iplanRechargeAndInvest.do",
                new BasicHeader[0], params);
        System.out.println(result);
    }

    @Test
    public void testDate() {
        LocalDate raiseCloseDate = DateUtil.parseDate("2017-07-10 19:44:36", DateUtil.DATE_TIME_FORMATTER_19);//募集结束时间
        LocalDate localDateTransfer = DateUtil.parseDate("2017-08-11 03:00:02", DateUtil.DATE_TIME_FORMATTER_19);//转出时间
        LocalDate localDateJoin = DateUtil.parseDate("2017-07-10 18:05:53", DateUtil.DATE_TIME_FORMATTER_19);//开放募集时间
        long holdingDays = DateUtil.betweenDays(localDateJoin, localDateTransfer);
        long restDays = DateUtil.betweenDays(localDateTransfer, raiseCloseDate.plusMonths(1).plusDays(1));
        System.out.println("restDays = " + restDays);
    }

   /* @Test
    public void redisTest(){

        Subject subject = subjectDao.findById(1);
        System.out.println("subject:"+subject.toString());
        redisClient.set(subject.getSubjectId(),subject);
        Subject subject1 = redisClient.get(subject.getSubjectId(),Subject.class);
        System.out.println("subject1"+subject1.toString());
    }*/
    @Autowired
    StringEncryptor stringEncryptor;

    @Test
    public void encryptTest() {
        String result = stringEncryptor.encrypt("querytest!1");
        System.out.println(result);
        result = stringEncryptor.decrypt(result);
        System.out.println(result);
    }

}
