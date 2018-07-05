package com.jiuyi.test;

import com.jiuyi.ndr.BatchApplication;
import com.jiuyi.ndr.service.redpacket.RedpacketService;
import com.jiuyi.ndr.util.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;

/**
 * @author 姜广兴
 * @date 2018-05-03
 */
@SpringBootTest(classes = BatchApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev01")
public class RedpacketServiceTest {
    @Autowired
    private RedpacketService redpacketService;

    @Test
    public void testRealRate() {
        System.out.println(redpacketService.getRealRate(BigDecimal.valueOf(0.09), DateUtil.parseDateTime("2018-05-04 12:00:00", DateUtil.DATE_TIME_FORMATTER_19)));
        System.out.println(redpacketService.getRealRate(BigDecimal.valueOf(0.12), DateUtil.parseDateTime("2018-05-08 12:00:00", DateUtil.DATE_TIME_FORMATTER_19)));
        System.out.println(redpacketService.getRealRate(BigDecimal.valueOf(0.12), DateUtil.parseDateTime("2018-05-06 12:00:00", DateUtil.DATE_TIME_FORMATTER_19)));
        System.out.println(redpacketService.getRealRate(BigDecimal.valueOf(0.05), DateUtil.parseDateTime("2018-05-08 12:00:00", DateUtil.DATE_TIME_FORMATTER_19)));
    }
}
