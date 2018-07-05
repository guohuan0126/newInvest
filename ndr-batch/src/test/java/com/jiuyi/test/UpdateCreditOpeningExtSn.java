package com.jiuyi.test;

import com.alibaba.fastjson.JSONObject;
import com.jiuyi.ndr.BatchApplication;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.xm.http.request.RequestIntelligentProjectDebentureSale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by zhangyibo on 2017/8/14.
 */
@ActiveProfiles(value = "dev01")
@SpringBootTest(classes = BatchApplication.class)
@RunWith(value = SpringJUnit4ClassRunner.class)
public class UpdateCreditOpeningExtSn {

    @Autowired
    private CreditOpeningDao creditOpeningDao;

    private static final String requestPacket = "{\"details\":[{\"intelRequestNo\":\"NDR2017060115172559eaa3\",\"platformUserNo\":\"uyeuqmE3ERbuhqdn\",\"projectNo\":\"INDR170525125706f544f7\",\"saleRequestNo\":\"NDR20170814161119cc94d8\",\"saleShare\":1.4},{\"intelRequestNo\":\"NDR2017060115172559eaa3\",\"platformUserNo\":\"uyeuqmE3ERbuhqdn\",\"projectNo\":\"NDR170608153249000677\",\"saleRequestNo\":\"NDR20170814161119cb0b83\",\"saleShare\":2.86},{\"intelRequestNo\":\"NDR2017060115172559eaa3\",\"platformUserNo\":\"uyeuqmE3ERbuhqdn\",\"projectNo\":\"NDR170728235143000819\",\"saleRequestNo\":\"NDR201708141611194be9a4\",\"saleShare\":1.4},{\"intelRequestNo\":\"NDR2017060115172559eaa3\",\"platformUserNo\":\"uyeuqmE3ERbuhqdn\",\"projectNo\":\"NDR170608153248000643\",\"saleRequestNo\":\"NDR20170814161119fdd2e1\",\"saleShare\":2.86},{\"intelRequestNo\":\"NDR2017060115172559eaa3\",\"platformUserNo\":\"uyeuqmE3ERbuhqdn\",\"projectNo\":\"INDR17052512570637b683\",\"saleRequestNo\":\"NDR20170814161119fef7ec\",\"saleShare\":359.04},{\"intelRequestNo\":\"NDR2017060115172559eaa3\",\"platformUserNo\":\"uyeuqmE3ERbuhqdn\",\"projectNo\":\"NDR170607163919000243\",\"saleRequestNo\":\"NDR201708141611191fa665\",\"saleShare\":99.99},{\"intelRequestNo\":\"NDR2017060115172559eaa3\",\"platformUserNo\":\"uyeuqmE3ERbuhqdn\",\"projectNo\":\"NDR170526151233000453\",\"saleRequestNo\":\"NDR20170814161119f1a7cc\",\"saleShare\":72.52},{\"intelRequestNo\":\"NDR2017060115172559eaa3\",\"platformUserNo\":\"uyeuqmE3ERbuhqdn\",\"projectNo\":\"NDR170728142702000220\",\"saleRequestNo\":\"NDR20170814161119f128c2\",\"saleShare\":183.34},{\"intelRequestNo\":\"NDR2017060115172559eaa3\",\"platformUserNo\":\"uyeuqmE3ERbuhqdn\",\"projectNo\":\"NDR170729225805000608\",\"saleRequestNo\":\"NDR20170814161119c8ebf6\",\"saleShare\":350.01},{\"intelRequestNo\":\"NDR2017060115172559eaa3\",\"platformUserNo\":\"uyeuqmE3ERbuhqdn\",\"projectNo\":\"INDR170525125706aa6905\",\"saleRequestNo\":\"NDR2017081416111961c1dd\",\"saleShare\":121.81},{\"intelRequestNo\":\"NDR2017060115172559eaa3\",\"platformUserNo\":\"uyeuqmE3ERbuhqdn\",\"projectNo\":\"INDR170525125706a03e8a\",\"saleRequestNo\":\"NDR20170814161119c3bb93\",\"saleShare\":183.34},{\"intelRequestNo\":\"NDR2017060115172559eaa3\",\"platformUserNo\":\"uyeuqmE3ERbuhqdn\",\"projectNo\":\"NDR170722130736000377\",\"saleRequestNo\":\"NDR2017081416111913f39f\",\"saleShare\":72.51},{\"intelRequestNo\":\"NDR2017060115172559eaa3\",\"platformUserNo\":\"uyeuqmE3ERbuhqdn\",\"projectNo\":\"NDR170620112706000543\",\"saleRequestNo\":\"NDR201708141611193e4f63\",\"saleShare\":72.51},{\"intelRequestNo\":\"NDR2017060115172559eaa3\",\"platformUserNo\":\"uyeuqmE3ERbuhqdn\",\"projectNo\":\"NDR170526101339000135\",\"saleRequestNo\":\"NDR20170814161119b67e28\",\"saleShare\":538.66}],\"requestNo\":\"NDR2017081416111950c3a8\",\"timestamp\":\"20170814161119\",\"transCode\":\"CREDIT_TRANSFER\"}";

    /*@Test
    public void test() throws IOException {
        Writer writer = new FileWriter("E:/数据修改.sql");
        RequestIntelligentProjectDebentureSale request = JSONObject.parseObject(requestPacket, RequestIntelligentProjectDebentureSale.class);
        List<RequestIntelligentProjectDebentureSale.Detail> detailList = request.getDetails();
        for(RequestIntelligentProjectDebentureSale.Detail detail:detailList){
            String userId = detail.getPlatformUserNo();
            String subjectId = detail.getProjectNo();
            int transferPrincipal = BigDecimal.valueOf(detail.getSaleShare()).multiply(BigDecimal.valueOf(100)).intValue();
            CreditOpening creditOpening = creditOpeningDao.findUpdateData(userId,transferPrincipal,subjectId);
            String sql = "UPDATE ndr_credit_opening SET ext_sn='"+detail.getSaleRequestNo()+"' WHERE id="+creditOpening.getId()+";\n";
            writer.write(sql);
        }

        writer.close();
    }*/

}
