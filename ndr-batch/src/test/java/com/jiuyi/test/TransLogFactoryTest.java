package com.jiuyi.test;

import com.jiuyi.ndr.BatchApplication;
import com.jiuyi.ndr.domain.lplan.LPlanTransLog;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zhangyibo on 2017/8/2.
 */
@SpringBootTest(classes = BatchApplication.class)
public class TransLogFactoryTest {


    private List<LPlanTransLog> findPrincipalInvest(){
        List<LPlanTransLog> lPlanTransLogs = new ArrayList<>();
        LPlanTransLog lPlanTransLog1 = new LPlanTransLog();
        lPlanTransLog1.setId(1);
        lPlanTransLog1.setProcessedAmt(0);
        lPlanTransLog1.setTransAmt(100);
        LPlanTransLog lPlanTransLog2 = new LPlanTransLog();
        lPlanTransLog2.setId(2);
        lPlanTransLog2.setProcessedAmt(0);
        lPlanTransLog2.setTransAmt(200);
        LPlanTransLog lPlanTransLog3 = new LPlanTransLog();
        lPlanTransLog3.setId(3);
        lPlanTransLog3.setProcessedAmt(0);
        lPlanTransLog3.setTransAmt(300);
        LPlanTransLog lPlanTransLog4 = new LPlanTransLog();
        lPlanTransLog4.setId(4);
        lPlanTransLog4.setProcessedAmt(0);
        lPlanTransLog4.setTransAmt(400);
        lPlanTransLogs.add(lPlanTransLog1);
        lPlanTransLogs.add(lPlanTransLog2);
        lPlanTransLogs.add(lPlanTransLog3);
        lPlanTransLogs.add(lPlanTransLog4);
        return lPlanTransLogs;
    }

    private List<LPlanTransLog> findNewbieInvest(){
        List<LPlanTransLog> lPlanTransLogs = new ArrayList<>();
        LPlanTransLog lPlanTransLog1 = new LPlanTransLog();
        lPlanTransLog1.setId(5);
        lPlanTransLog1.setProcessedAmt(0);
        lPlanTransLog1.setTransAmt(500);
        LPlanTransLog lPlanTransLog2 = new LPlanTransLog();
        lPlanTransLog2.setId(6);
        lPlanTransLog2.setProcessedAmt(0);
        lPlanTransLog2.setTransAmt(600);
        LPlanTransLog lPlanTransLog3 = new LPlanTransLog();
        lPlanTransLog3.setId(7);
        lPlanTransLog3.setProcessedAmt(0);
        lPlanTransLog3.setTransAmt(700);
        LPlanTransLog lPlanTransLog4 = new LPlanTransLog();
        lPlanTransLog4.setId(8);
        lPlanTransLog4.setProcessedAmt(0);
        lPlanTransLog4.setTransAmt(800);
        lPlanTransLogs.add(lPlanTransLog1);
        lPlanTransLogs.add(lPlanTransLog2);
        lPlanTransLogs.add(lPlanTransLog3);
        lPlanTransLogs.add(lPlanTransLog4);
        return lPlanTransLogs;
    }

}
