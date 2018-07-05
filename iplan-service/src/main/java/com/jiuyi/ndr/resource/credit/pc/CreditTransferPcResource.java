package com.jiuyi.ndr.resource.credit.pc;

import com.jiuyi.ndr.dto.credit.mobile.AppCreditTransferConfirmDto;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditTransferDto;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditTransferSuccessDto;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.rest.customannotation.AutoLogger;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.subject.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Map;

@RestController
public class CreditTransferPcResource implements Serializable {

    private final static Logger logger = LoggerFactory.getLogger(CreditTransferPcResource.class);

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private IPlanAccountService iPlanAccountService;

    //pc-转让发起页
    @GetMapping("/pc/credit/creditTransfer")
    public RestResponse creditTransfer(@RequestParam("id") int subjectAccountId,
                                       @RequestParam(value = "userId") String userId) {
        AppCreditTransferDto appCreditTransferDto = subjectService.creditTransfer(subjectAccountId, userId);
        return new RestResponseBuilder<>().success(appCreditTransferDto);
    }

    //pc-一键投转让发起页
    @GetMapping("/pc/yjt/creditTransfer")
    public RestResponse yjtCreditTransfer(@RequestParam("id") int iPlanAccountId,
                                          @RequestParam(value = "userId") String userId) {
        AppCreditTransferDto appCreditTransferDto = iPlanAccountService.creditTransfer(iPlanAccountId, userId);
        return new RestResponseBuilder<>().success(appCreditTransferDto);
    }

    //pc-转让确认页
    @AutoLogger
    @PostMapping("/pc/credit/creditTransfer/confirm")
    public RestResponse creditTransferConfirm(@RequestBody Map<String,String> map) {
        AppCreditTransferConfirmDto appCreditTransferConfirmDto = subjectService.creditTransferConfirm(map);
        return new RestResponseBuilder<>().success(appCreditTransferConfirmDto);
    }
    //pc-一键投转让确认页
    @PostMapping("/pc/yjt/creditTransfer/confirm")
    public RestResponse yjtCreditTransferConfirm(@RequestBody Map<String,String> map) {
        AppCreditTransferConfirmDto appCreditTransferConfirmDto = iPlanAccountService.creditTransferConfirm(map);
        return new RestResponseBuilder<>().success(appCreditTransferConfirmDto);
    }

    //pc-债权实际调用接口
    @PostMapping("/pc/credit/creditTransfer/finish")
    public RestResponse creditTransferFinsh(@RequestBody Map<String,String> map) {

        AppCreditTransferSuccessDto appCreditTransferSuccessDto =subjectService.creditTransferFinsh(map);

        return new RestResponseBuilder<>().success(appCreditTransferSuccessDto);
    }
}
