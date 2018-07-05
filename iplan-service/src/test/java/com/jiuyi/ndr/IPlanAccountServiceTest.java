package com.jiuyi.ndr;

import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.marketing.MarketService;
import com.jiuyi.ndr.util.redis.RedisClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by zhq on 2017/6/17.
 */
@ActiveProfiles("dev01")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class IPlanAccountServiceTest {

    @Autowired
    private IPlanAccountService iPlanAccountService;

    @Autowired
    private MarketService marketService;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private IPlanDao iPlanDao;

    @Test
    public void investTest() {
        iPlanAccountService.invest("BN7VZjYvqiuenxwn", 16, 10000, 0, "pc", 0);
    }

    @Test
    public void marketTest() {
        Map<String, Object> map = marketService.getUserIPlanVipRateAndVipLevel("BN7VZjYvqiuenxwn");
        System.out.println(map.toString());
    }

    @Test
    public void snathInvestTest() {

        try {
            redisClient.set("INVEST", "100000");
            System.out.println(redisClient.get("dev01_MQ_INVEST"));
        } catch (Exception e) {
            e.printStackTrace();
        }
       // iPlanAccountService.SnathInvest(Thread.currentThread().getName(), 1, 500, null);
    }

    @Test
    public void testIplan() {
        Map<String,Map<String,List<IPlan>>> iPlanMap = iPlanDao.getIPlans(0).stream().collect(Collectors.groupingBy((iPlan) -> {
            if (iPlan.getTerm() == 1) {
                return "1";
            } else if (iPlan.getTerm() == 3) {
                return "3";
            } else if (iPlan.getTerm() == 6) {
                return "6";
            } else if (iPlan.getTerm() == 12) {
                return "12";
            } else {
                return "";
            }
        }, Collectors.groupingBy((iPlan) -> {
            if (IPlan.STATUS_RAISING.equals(iPlan.getStatus())) {
                return "raising";
            } else if (IPlan.STATUS_ANNOUNCING.equals(iPlan.getStatus())) {
                return "announcing";
            } else {
                return "finished";
            }
        })));
        Map<String,List<IPlan>> iPlan1Term = iPlanMap.get("1");
        Map<String,List<IPlan>> iPlan3Term = iPlanMap.get("3");
        Map<String,List<IPlan>> iPlan6Term = iPlanMap.get("6");
        Map<String,List<IPlan>> iPlan12Term = iPlanMap.get("12");
        List<IPlan> iPlans1 = getIPlanListAll(iPlan1Term.get("raising"),iPlan1Term.get("announcing"),iPlan1Term.get("finished"));
        List<IPlan> iPlans3 = getIPlanListAll(iPlan3Term.get("raising"),iPlan3Term.get("announcing"),iPlan3Term.get("finished"));
        List<IPlan> iPlans6 = getIPlanListAll(iPlan6Term.get("raising"),iPlan6Term.get("announcing"),iPlan6Term.get("finished"));
        List<IPlan> iPlans12 = getIPlanListAll(iPlan12Term.get("raising"),iPlan12Term.get("announcing"),iPlan12Term.get("finished"));
        System.out.println("ok");
    }
    private List getIPlanListAll(List... params) {
        List resultList = new ArrayList<>();
        if (params != null && params.length > 0) {
            for (List list : params) {
                if (list != null && list.size() > 0) {
                    resultList.addAll(list);
                }
            }
        }
        return resultList;
    }

}
