package com.jiuyi.ndr.resource.autoinvest;


import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.service.autoinvest.AutoInvestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by drw on 2017/6/9.
 */
@RestController
public class AutoInvestResource {

    @Autowired
    AutoInvestService autoInvestService;


    /**
     * 月月盈项目自动投标
     * @param iPlanId
     * @return
     */
    @RequestMapping(value = "/autoInvestIPlan", method = RequestMethod.POST)
    public RestResponse autoInvestIPlan(@RequestParam("iPlanId") int iPlanId) {
        autoInvestService.autoInvestIPlan(iPlanId);
        return new RestResponseBuilder<>().success(null);
    }

    /**
     * 月月盈单笔投标
     * @param userId
     * @param iPlanId
     * @param amount
     * @param transDevice
     * @return
     */
    @RequestMapping(value = "/autoInvestIPlanPersonal", method = RequestMethod.POST)
    public RestResponse get(@RequestParam(value = "userId") String userId, @RequestParam("iPlanId") int iPlanId,
                            @RequestParam("amount") int amount, @RequestParam("transDevice") String transDevice) {
        autoInvestService.autoInvestIPlanPersonal(userId, iPlanId, amount, 0, transDevice);
        return new RestResponseBuilder<>().success(null);
    }

    /**
     * 散标单笔自动投标
     * @param userId
     * @param subjectId
     * @param amount
     * @param transDevice
     * @return
     */
    @RequestMapping(value = "/autoInvestSubjectPersonal", method = RequestMethod.POST)
    public RestResponse autoInvestSubjectPersonal(@RequestParam(value = "userId") String userId,
                                                  @RequestParam("subjectId") String subjectId,
                                                  @RequestParam("amount") int amount,
                                                  @RequestParam("transDevice") String transDevice) {
        autoInvestService.autoInvestSubjectPersonal(userId,subjectId,amount,0, transDevice);
        return new RestResponseBuilder<>().success(null);
    }

    /**
     * 散标单笔流标
     * @param subjectTransLogId
     * @return
     */
    @RequestMapping(value = "/autoSubjectInvestCancel", method = RequestMethod.POST)
    public RestResponse autoSubjectInvestCancel(@RequestParam("subjectTransLogId") int subjectTransLogId) {
        autoInvestService.subjectInvestCancel(subjectTransLogId);
        return new RestResponseBuilder<>().success(null);
    }

    /**
     * 散标项目自动投标
     * @param subjectId
     * @return
     */
    @RequestMapping(value = "/autoInvestSubject", method = RequestMethod.POST)
    public RestResponse autoInvestSubject(@RequestParam("subjectId") String subjectId) {
        autoInvestService.autoInvestSubject(subjectId);
        return new RestResponseBuilder<>().success(null);
    }
}
