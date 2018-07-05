package com.jiuyi.ndr;

import com.jiuyi.ndr.dao.autoinvest.AutoInvestDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.domain.autoinvest.AutoInvest;
import com.jiuyi.ndr.service.autoinvest.AutoInvestService;
import com.jiuyi.ndr.service.invest.InvestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by drw on 2017/6/9.
 */
@ActiveProfiles(value = "dem03")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class IPlanAutoInvestServiceTest {

    @Autowired
    private AutoInvestService iPlanAutoInvestService;

    @Autowired
    private AutoInvestDao autoInvestDao;

    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;

    @Test
    public void testMethod() {
        System.out.println(iPlanTransLogDao.findFirstInvestPending("Nju2miUfyERjogwr", 4));
    }

    @Test
    public void testIPlanAutoInvest(){
        AutoInvest autoInvest = iPlanAutoInvestService.getAutoInvest("7jyiMb6NvIVrqwii");
        System.out.println(autoInvest);
    }

    @Test
    public void test() {
        List<AutoInvest> autoInvests = autoInvestDao.getAutoInvests();
        System.out.println(autoInvests);
    }

}
