package com.jiuyi.ndr.batch.iplan;


import com.jiuyi.ndr.domain.marketing.MarketingIplanAppointRecord;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guohuan
 * @date 2018/01/05
 */
public class NewIplanAutoInvestWriter implements ItemWriter<MarketingIplanAppointRecord> {

    @Autowired
    IPlanAccountService iPlanAccountService;

    @Override
    public void write(List<? extends MarketingIplanAppointRecord> lists) throws Exception {
        List<Integer> listIds = lists.stream().map(marketingIplanAppointRecord ->marketingIplanAppointRecord.getId()).collect(Collectors.toList());
        for (Integer id : listIds) {
            iPlanAccountService.newIplanAutoInvest(id);
        }
    }
}
