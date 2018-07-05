package com.jiuyi.ndr.resource.credit.pc;

import com.jiuyi.ndr.dto.credit.mobile.AppCreditCancelConfirmDto;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditTransferSuccessDto;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.rest.customannotation.AutoLogger;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.iplan.IPlanTransLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CreditCancelPcResource {
    private final static Logger logger = LoggerFactory.getLogger(CreditCancelPcResource.class);

    @Autowired
    private CreditOpeningService creditOpeningService;

    @Autowired
    private IPlanTransLogService iPlanTransLogService;


    //pc-撤销确认页
    @PostMapping("/pc/credit/creditCancel/confirm")
    public RestResponse creditTransferConfirm(@RequestBody Map<String,String> map) {
        //债权撤消确认
        AppCreditCancelConfirmDto appCreditCancelConfirmDto = creditOpeningService.creditTransferConfirm(map);
        return new RestResponseBuilder<>().success(appCreditCancelConfirmDto);
    }

    //pc-一键投撤销确认页
    @PostMapping("/pc/yjt/creditCancel/confirm")
    public RestResponse yjtCreditTransferConfirm(@RequestBody Map<String,String> map) {
        //债权撤消确认
        AppCreditCancelConfirmDto appCreditCancelConfirmDto = iPlanTransLogService.creditTransferConfirm(map);
        return new RestResponseBuilder<>().success(appCreditCancelConfirmDto);
    }

    //pc-债权实际调用接口
    @AutoLogger
    @PostMapping("/pc/credit/creditCancel/finish")
    public RestResponse creditTransferCancelFinsh(@RequestBody Map<String,String> map) {

        AppCreditTransferSuccessDto appCreditTransferSuccessDto =creditOpeningService.creditTransferCancelFinsh(map);

        return new RestResponseBuilder<>().success(appCreditTransferSuccessDto);
    }
}
