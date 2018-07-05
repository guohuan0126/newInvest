package com.jiuyi.test;

import com.jiuyi.ndr.BatchApplication;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by zhangyibo on 2017/8/29.
 */
@SpringBootTest(classes = BatchApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev01")
public class GenerateRequestUrlTest {

    @Autowired
    private IPlanAccountDao iPlanAccountDao;

    @Test
    public void test() throws IOException {
        FileWriter writer = new FileWriter("E:/请求1.txt");
        FileWriter writer2 = new FileWriter("E:/请求2.txt");
        List<IPlanAccount> iPlanAccountList = iPlanAccountDao.findAllUser();
        for (IPlanAccount iPlanAccount:iPlanAccountList){
            String url = "https://soa-ttz.duanrong.com/trans/freeze/"+iPlanAccount.getUserId()+"/"+iPlanAccount.getInvestRequestNo()+"/"+iPlanAccount.getAmtToInvest()+"?token=duanrong123";
            String url2 = "https://soa-ttz.duanrong.com/trans/unfreezeAndCommission/"+iPlanAccount.getInvestRequestNo()+"/"+iPlanAccount.getAmtToInvest()+"/{commission}?token=duanrong123";
        }
    }
}
