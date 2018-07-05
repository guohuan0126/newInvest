package com.jiuyi.ndr;

import com.jiuyi.ndr.service.credit.CreditTransferRecordService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author 姜广兴
 * @date 2018-04-17
 */
@ActiveProfiles(value = "dev01")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class CreditTransferRecordTest {

    @Autowired
    private CreditTransferRecordService creditTransferRecordService;

    @Test
    public void testGetCreditTransferRecords() {
        creditTransferRecordService.getCreditTransferRecords("NDR180416172120000007");
    }
}
