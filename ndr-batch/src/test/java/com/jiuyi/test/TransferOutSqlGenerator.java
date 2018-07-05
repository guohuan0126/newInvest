package com.jiuyi.test;

import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.BatchApplication;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.lplan.LPlanAccountDao;
import com.jiuyi.ndr.dao.xm.TransLogDao;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.lplan.LPlanAccount;
import com.jiuyi.ndr.domain.xm.TransLog;
import com.jiuyi.ndr.util.DateUtil;
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
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by zhangyibo on 2017/8/30.
 */
@SpringBootTest(classes = BatchApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev01")
public class TransferOutSqlGenerator {

    @Autowired
    private CreditOpeningDao creditOpeningDao;

    @Autowired
    private TransLogDao transLogDao;

    @Autowired
    private LPlanAccountDao lPlanAccountDao;

    @Test
    public void test() throws IOException {
        Writer writer = new FileWriter("E:/修改转出数据-201709032112.txt");
        List<CreditOpening> creditOpeningList = creditOpeningDao.findStatusPending();
        for(CreditOpening creditOpening:creditOpeningList){
//            LocalDateTime startTime = LocalDateTime.parse(creditOpening.getCreateTime(),DateUtil.DATE_TIME_FORMATTER_19);
//            LocalDateTime endTime = startTime.plusMinutes(1L);
            List<TransLog> transLogs = transLogDao.findTransLogBtwTime(creditOpening.getExtSn());
            TransLog transLog;
            if(transLogs.stream().anyMatch(t -> t.getStatus()==0)){
                transLog = transLogs.stream().filter(t -> t.getStatus()==0).findFirst().get();
            }else{
                transLog = transLogs.get(0);
                throw new RuntimeException();
            }
            String requestPacket = transLog.getRequestPacket();
            RequestIntelligentProjectDebentureSale request = JSON.parseObject(requestPacket, RequestIntelligentProjectDebentureSale.class);
            String respMsg = transLog.getRespMsg();
            String oldSaleRequestNo = request.getDetails().get(0).getSaleRequestNo();
            CreditOpening firstCreditOpening = creditOpeningDao.findByExtSn(oldSaleRequestNo);
            String sql1 = "UPDATE ndr_credit_opening SET ext_sn='"+oldSaleRequestNo+"M'"+" WHERE id = "+firstCreditOpening.getId()+";\n";

            request.getDetails().get(0).setSaleRequestNo(oldSaleRequestNo+"M");
            String oldRequestNo = transLog.getTxnSn();
            request.setRequestNo(oldRequestNo+"M");
            Integer tradeAmt = getSendAmt(respMsg);//此次发送了多少钱
            Integer realAmt = getRealAmt(respMsg);
            Integer subAmt = tradeAmt-realAmt;
            CreditOpening creditOpening1 = creditOpeningDao.findCreditByAmt(tradeAmt,creditOpening.getTransferorId());//查询到对应的本地多一分钱的债权
            String sql2 = "UPDATE ndr_credit_opening set transfer_principal=transfer_principal-"+subAmt+",available_principal=available_principal-"+subAmt+" WHERE id = "+creditOpening1.getId()+";\n";
            int count = 0;
            for(RequestIntelligentProjectDebentureSale.Detail detail:request.getDetails()){
                if(BigDecimal.valueOf(detail.getSaleShare()).compareTo(BigDecimal.valueOf(tradeAmt).divide(BigDecimal.valueOf(100)))==0){
                    Double newSaleShare = BigDecimal.valueOf(tradeAmt).subtract(BigDecimal.valueOf(subAmt)).divide(BigDecimal.valueOf(100)).doubleValue();
                    detail.setSaleShare(newSaleShare);
                    count++;
                }
            }
            if(count>1) throw new RuntimeException();

            String sql3 = "UPDATE ndr_xm_trans_log SET txn_sn='"+oldRequestNo+"M'"+",request_packet='"+JSON.toJSONString(request)+"' WHERE txn_sn='"+oldRequestNo+"' AND status = 0;\n";

            //LPlanAccount lPlanAccount = lPlanAccountDao.findByUserId(creditOpening.getTransferorId());

            //String sql4 = "UPDATE ndr_lplan_account SET expected_interest=expected_interest+"+subAmt+" WHERE id = "+lPlanAccount.getId()+";\n";

            if(!getUserId(respMsg).equals(creditOpening.getTransferorId())) throw new RuntimeException();

            writer.write(sql1);
            writer.write(sql2);
            writer.write(sql3);
            //writer.write(sql4);
            writer.write("\n");
        }

        writer.close();
    }

    public Integer getSendAmt(String str){
        int index = str.indexOf("本次交易债权为:-");
        String amtStr = str.substring(index+9);
        Integer amt = new BigDecimal(amtStr).multiply(new BigDecimal(100)).intValue();
        return amt;
    }

    public Integer getRealAmt(String str){
        int index = str.indexOf("用户手持债权为:");
        int endIndex = str.indexOf(",本次交易债权为");
        String amtStr = str.substring(index+8,endIndex);
        Integer amt = new BigDecimal(amtStr).multiply(new BigDecimal(100)).intValue();
        return amt;
    }

    public String getUserId(String str){
        int start = str.indexOf("platformUserNo:");
        int end = str.indexOf(",用户手持债权为");
        return str.substring(start+15,end);
    }


}
