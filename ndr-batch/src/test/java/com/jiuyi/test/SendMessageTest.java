package com.jiuyi.test;


import com.jiuyi.ndr.BatchApplication;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.notice.support.TemplateId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = BatchApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(profiles = "dev01")
public class SendMessageTest {

    @Autowired
    NoticeService noticeService;

    @Test
    public void sendMessage(){
//        noticeService.send("18685609593","亲爱的张国新，您已成功投资月月盈201700387，期限3个月，锁定期31天，金额21700.0元，明天的财富源自今天的积累，我们将用努力回报您的支持与信任。",null,null);
//        noticeService.send("18685609593","亲爱的张国新，您已成功投资月月盈201700389，期限1个月，锁定期31天，金额20000.0元，明天的财富源自今天的积累，我们将用努力回报您的支持与信任。",null,null);
        System.out.println(123);
    }

    @Test
    public void sendMessage2(){
        noticeService.send("18801442723", "刘丽娜"+","+"月月盈201700387"+","
                + "29天"+","+7+","+1.0, "iplan_invest_succeed");
        System.out.println(123);
    }
    @Test
    public void sendMessage3(){
//        noticeService.send("15072280066", "刘学军,月月盈201700879,4,12,437.5,28258.9", TemplateId.IPLAN_PAY_INTEREST_MONTHLY); //每月还息
    }

    @Test
    public void sendtest(){
        double a1 =1.20;
        double b=1.2;
        System.out.println(a1==b);
    }
}
