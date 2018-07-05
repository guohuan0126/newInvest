package com.jiuyi.ndr.resource.dradmin;

import com.jiuyi.ndr.domain.iplan.IPlanParam;

import com.jiuyi.ndr.dto.dradmin.IPlanParamDto;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.service.iplan.IPlanParamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by yumin on 2017/6/12.
 */
@RestController
public class IPlanParamResource {
    private final static Logger logger = LoggerFactory.getLogger(IPlanParamResource.class);
   @Autowired
   IPlanParamService iPlanParamService;
    @GetMapping("/iplan/iPlanParam")
    public RestResponse<List> getIPlanParam() {
        List<IPlanParam> iPlanParamList = iPlanParamService.findAll();
        return new RestResponseBuilder<List>().success(iPlanParamList);

    }
    @PutMapping("/iplan/saveiPlanParam")
    public RestResponse<IPlanParamDto> saveiPlanParam(@RequestBody IPlanParamDto iPlanParamDto) {
        IPlanParam iPlanParam = new IPlanParam();
        BeanUtils.copyProperties(iPlanParamDto, iPlanParam);
        iPlanParam = iPlanParamService.insert(iPlanParam);
        iPlanParamDto.setId(iPlanParam.getId());
        return new RestResponseBuilder<IPlanParamDto>().success(iPlanParamDto);
    }
    @RequestMapping(value = "/iplan/iPlanParam/{id}", method = RequestMethod.GET)
    public RestResponse<IPlanParam> getAccount(@PathVariable("id") String id) {
        IPlanParam iPlanParam = iPlanParamService.getIPlanParamById(Integer.parseInt(id));
        if (iPlanParam == null) {
            return new RestResponseBuilder<IPlanParam>().success(null);
        }
        return new RestResponseBuilder<IPlanParam>().success(iPlanParam);
    }
    @PostMapping("/iplan/updateiPlanParam")
    public RestResponse<IPlanParamDto> updateiPlanParam(@RequestBody IPlanParamDto iPlanParamDto) {
        IPlanParam iPlanParam = new IPlanParam();
        BeanUtils.copyProperties(iPlanParamDto, iPlanParam);
        iPlanParam = iPlanParamService.update(iPlanParam);
        return new RestResponseBuilder<IPlanParamDto>().success(iPlanParamDto);
    }

    @GetMapping("/iplan/iPlanParamOrderById")
    public RestResponse<List> getPlanParamOrderById() {
        List<IPlanParam> iPlanParamList = iPlanParamService.findIPlanParamOrderById();
        return new RestResponseBuilder<List>().success(iPlanParamList);

    }
}
