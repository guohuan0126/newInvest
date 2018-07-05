package com.jiuyi.ndr.batch.iplan;

import com.jiuyi.ndr.batch.TemplateWriter;
import com.jiuyi.ndr.domain.iplan.IPlanSettleAcount;
import com.jiuyi.ndr.service.iplan.IplanSettleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author 姜广兴
 * @date 2018-04-17
 */
@Component
@Scope("step")
public class IPlanSettleWriter extends TemplateWriter<IPlanSettleAcount> {
    @Autowired
    private IplanSettleService iplanSettleService;

    @Override
    protected void doWrite(List<? extends IPlanSettleAcount> iplanSettleAcounts) {
        iplanSettleAcounts.forEach(iplanSettleService::iplanSettle);
    }
}
