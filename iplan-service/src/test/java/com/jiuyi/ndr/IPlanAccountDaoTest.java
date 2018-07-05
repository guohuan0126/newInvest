package com.jiuyi.ndr;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.util.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by zhangyibo on 2017/6/1.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class IPlanAccountDaoTest {

    @Autowired
    private IPlanAccountDao iPlanAccountDao;
    @Autowired
    private IPlanAccountService iPlanAccountService;

    @Test
    public void testfindAll(){
        iPlanAccountDao.findAllUser().stream().map(IPlanAccount::getCurrentPrincipal).forEach(System.out::println);
    }

    @Test
    public void testInsertBackId() {
        IPlanAccount planAccount = new IPlanAccount();
        planAccount.setExpectedInterest(0);
        planAccount.setPaidInterest(0);
        planAccount.setAmtToInvest(0);
        planAccount.setAmtToTransfer(0);
        planAccount.setStatus(IPlanAccount.STATUS_PROCEEDS);
        planAccount.setCreateTime(DateUtil.getCurrentDateTime19());
        int total = iPlanAccountDao.insert(planAccount);
        System.out.println("total = " + total);
    }

    @Test
    public void testPageHelper(){

        PageHelper.startPage(1,10);

        List<IPlanAccount> allUser = iPlanAccountDao.findAllUser();

        PageInfo<IPlanAccount> pageInfo = new PageInfo<>(allUser);

        System.out.println(pageInfo);

        List<IPlanAccount> list = pageInfo.getList();

        System.out.println(list);
    }


    @Test
    public void test(){

        List<IPlanAccount> planList = iPlanAccountService.findByUserIdAndStatusIn("1",
                new HashSet<>(Arrays.asList(IPlanAccount.STATUS_NORMAL_EXIT, IPlanAccount.STATUS_PROCEEDS)), 0);

        for (IPlanAccount iPlanAccount : planList) {
            System.out.println(iPlanAccount);
        }

    }
    @Test
    public void testDay(){
        System.out.println(DateUtil.compareDateTime("2017-10-31 16:40:00",DateUtil.getCurrentDateTime19()));

    }
}
