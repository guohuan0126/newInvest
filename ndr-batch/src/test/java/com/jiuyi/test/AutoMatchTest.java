package com.jiuyi.test;

import com.jiuyi.ndr.batch.iplan.IPlanAutoInvestTasklet;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by zhangyibo on 2017/8/7.
 */

@RunWith(Parameterized.class)
public class AutoMatchTest {

    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;

    @Test
    public void test() throws NoSuchMethodException {

        List<CreditOpening> creditOpeningList = new ArrayList<>();
        CreditOpening creditOpening1 = new CreditOpening();
        creditOpening1.setSourceChannelId(5);
        creditOpening1.setSourceChannel(CreditOpening.SOURCE_CHANNEL_IPLAN);
        creditOpening1.setCreditId(1);
        creditOpening1.setId(1);

        CreditOpening creditOpening2 = new CreditOpening();
        creditOpening2.setSourceChannel(CreditOpening.SOURCE_CHANNEL_LPLAN);
        creditOpening2.setId(2);

        CreditOpening creditOpening3 = new CreditOpening();
        creditOpening3.setCreditId(3);
        creditOpening3.setSourceChannelId(7);
        creditOpening3.setSourceChannel(CreditOpening.SOURCE_CHANNEL_IPLAN);
        creditOpening3.setId(3);
        CreditOpening creditOpening4 = new CreditOpening();
        CreditOpening creditOpening5 = new CreditOpening();
        CreditOpening creditOpening6 = new CreditOpening();
        CreditOpening creditOpening7 = new CreditOpening();

        creditOpeningList.add(creditOpening1);
        creditOpeningList.add(creditOpening2);
        creditOpeningList.add(creditOpening3);
        creditOpeningList.add(creditOpening4);
        creditOpeningList.add(creditOpening5);
        creditOpeningList.add(creditOpening6);
        creditOpeningList.add(creditOpening7);

        final IPlanAutoInvestTasklet target = new IPlanAutoInvestTasklet();
        Class<IPlanAutoInvestTasklet> clazz = IPlanAutoInvestTasklet.class;
        final Method sortCredits = clazz.getDeclaredMethod("sortCredits", CreditOpening.class, CreditOpening.class);
        sortCredits.setAccessible(true);
        Collections.sort(creditOpeningList, (o1, o2) -> {
            try {
                return (int) sortCredits.invoke(target,o1,o2);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return 0;
        });

        creditOpeningList.stream().map(CreditOpening::getId).forEach(System.out::println);
    }

    @Test
    public void test2(){
        if(iPlanTransLogDao.findByIPlanId(7).stream().filter(iPlanTransLog -> iPlanTransLog.getTransStatus()!= IPlanTransLog.TRANS_STATUS_FAILED&&iPlanTransLog.getExtStatus()==null)//要先过滤掉本地失败 没有调用厦门银行的交易 比如说充值并投资失败的交易
                .anyMatch(iPlanTransLog -> !BaseResponse.STATUS_SUCCEED.equals(iPlanTransLog.getExtStatus()))){
            System.out.println("---");
        }else{
            System.out.printf("xxxx");
        }
    }
    @Test
    public void test3(){
        List<IPlanTransLog> iPlanTransLogs = iPlanTransLogDao.findNeedMatchYjtTransLog();
        System.out.println("待匹配的转入记录id:"+iPlanTransLogs.stream().map(IPlanTransLog::getId).collect(Collectors.toList()));
        System.out.println("一键投资产自动匹配任务开始,共有{"+iPlanTransLogs.size()+"}条定期转入记录待匹配,总资金={"+iPlanTransLogs.parallelStream().map(IPlanTransLog::getTransAmt).reduce(Integer::sum).orElse(0)+"}");
        //根据iplanid分组
        Map<Integer,List<IPlanTransLog>> groupMap = iPlanTransLogs.stream().collect(Collectors.groupingBy(IPlanTransLog::getIplanId));
        for (Map.Entry<Integer,List<IPlanTransLog>> groupMapEntry:groupMap.entrySet()){
            int iplanId = groupMapEntry.getKey();
            //IPlan iPlan = iPlanDao.findById(iplanId);

            List<IPlanTransLog> matchIplanTransLogs = groupMapEntry.getValue();
            System.out.println("待匹配的省心投：{"+iplanId+"},下面的转入记录id:{"+matchIplanTransLogs.stream().map(IPlanTransLog::getId).collect(Collectors.toList())+"}");

        }
    }

}
