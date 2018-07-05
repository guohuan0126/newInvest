package com.jiuyi.ndr.resource.credit;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Created by mayognbo on 2017/11/3.
 */

@RestController
public class CreditTransferMobileResource {

    private final static Logger logger = LoggerFactory.getLogger(CreditTransferMobileResource.class);


    @Autowired
    private SubjectService subjectService;

    @Autowired
    private IPlanAccountService iPlanAccountService;

    private DecimalFormat df = new DecimalFormat("######0.00");
    private DecimalFormat df2 = new DecimalFormat("######0");
    private static DecimalFormat df3 = new DecimalFormat("0.####");
    private DecimalFormat df4 = new DecimalFormat("######0.##");
    private static DecimalFormat df5 = new DecimalFormat("######0.######");
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Value("${duanrong.subject.transferExplainUrl}")
    private String transferExplain;   //转让说明
    @Value("${duanrong.subject.yjtTransferExplainUrl}")
    private String yjtTransferExplainUrl;   //转让说明
    //app-转让发起页
    @GetMapping("/authed/app/credit/creditTransfer")
    public RestResponse creditTransfer(@RequestParam("id") int subjectAccountId,
                                 @RequestParam(value = "userId") String userId) {
        AppCreditTransferDto appCreditTransferDto = subjectService.creditTransfer(subjectAccountId, userId);
        appCreditTransferDto.setTransferExplain(transferExplain);
        return new RestResponseBuilder<>().success(appCreditTransferDto);
    }

    //yjtapp-转让发起页
    @GetMapping("/authed/app/yjt/creditTransfer")
    public RestResponse yjtCreditTransfer(@RequestParam("id") int iPlanAccountId,
                                       @RequestParam(value = "userId") String userId) {
        AppCreditTransferDto appCreditTransferDto = iPlanAccountService.creditTransfer(iPlanAccountId, userId);
        appCreditTransferDto.setTransferExplain(yjtTransferExplainUrl);
        return new RestResponseBuilder<>().success(appCreditTransferDto);
    }

    //app-转让确认页
    @PostMapping("/authed/app/credit/creditTransfer/confirm")
    public RestResponse creditTransferConfirm(@RequestBody Map<String,String> map) {
        AppCreditTransferConfirmDto appCreditTransferConfirmDto = subjectService.creditTransferConfirm(map);
        return new RestResponseBuilder<>().success(appCreditTransferConfirmDto);
    }

    //app-一键投转让确认页
    @PostMapping("/authed/app/yjt/creditTransfer/confirm")
    public RestResponse yjtCreditTransferConfirm(@RequestBody Map<String,String> map) {
        AppCreditTransferConfirmDto appCreditTransferConfirmDto = iPlanAccountService.creditTransferConfirm(map);
        return new RestResponseBuilder<>().success(appCreditTransferConfirmDto);
    }

    //app-债权实际调用接口
    @AutoLogger
    @PostMapping("/authed/app/credit/creditTransfer/finish")
    public RestResponse creditTransferFinsh(@RequestBody Map<String,String> map) {

        AppCreditTransferSuccessDto appCreditTransferSuccessDto = subjectService.creditTransferFinsh(map);

        return new RestResponseBuilder<>().success(appCreditTransferSuccessDto);
    }

}
