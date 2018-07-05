package com.jiuyi.ndr.resource.subject;

import com.jiuyi.ndr.domain.subject.SubjectInvestParamDef;
import com.jiuyi.ndr.domain.subject.SubjectRate;
import com.jiuyi.ndr.domain.subject.SubjectTransferParam;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.service.subject.SubjectInvestParamService;
import com.jiuyi.ndr.service.subject.SubjectRateParamService;
import com.jiuyi.ndr.service.subject.SubjectTransferParamService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
  * @author daibin
  * @date 2017/10/19
  */
@RestController
public class SubjectParamResource {

    @Autowired
    SubjectRateParamService subjectRateParamService;
    @Autowired
    SubjectTransferParamService subjectTransferParamService;
    @Autowired
    SubjectInvestParamService subjectInvestParamService;

    public static String OPERATION_TYPE_M = "月";
    public static String OPERATION_TYPE_D = "天";

    /**
     * 查询标的利率表信息
     * @return
     */
    @GetMapping("/subject/subjectRateParam")
    public RestResponse<List> findSubjectRateParam() {
        List<SubjectRate> subjectRateParams = subjectRateParamService.findSubjectRateParam();
        return new RestResponseBuilder<List>().success(subjectRateParams);
    }

    /**
     * 插入新的标的利率表信息
     * @param subjectRate
     * @return
     */
    @PutMapping("/subject/saveSubjectRateParam")
    public RestResponse<SubjectRate> saveSubjectRateParam(@RequestBody SubjectRate subjectRate) {
        SubjectRate subjectRateParam = new SubjectRate();
        BeanUtils.copyProperties(subjectRate, subjectRateParam);
        subjectRateParam = subjectRateParamService.insert(subjectRateParam);
        subjectRate.setId(subjectRateParam.getId());
        return new RestResponseBuilder<SubjectRate>().success(subjectRate);
    }

    /**
     * 根据id查询标的利率表信息
     * @param id
     * @return
     */
    @RequestMapping(value = "/subject/subjectRateParam/{id}", method = RequestMethod.GET)
    public RestResponse<SubjectRate> getSubjectRateParam(@PathVariable("id") String id) {
        SubjectRate subjectRateParam = subjectRateParamService.getSubjectRateParamById(Integer.parseInt(id));
        if (subjectRateParam == null) {
            return new RestResponseBuilder<SubjectRate>().success(null);
        }
        return new RestResponseBuilder<SubjectRate>().success(subjectRateParam);
    }

    /**
     * 根据id更新标的利率表信息
     * @param subjectRate
     * @return
     */
    @PutMapping("/subject/updateSubjectRateParam")
    public RestResponse<SubjectRate> updateSubjectRateParam(@RequestBody SubjectRate subjectRate) {
        SubjectRate subjectRateParam = new SubjectRate();
        BeanUtils.copyProperties(subjectRate, subjectRateParam);
        subjectRate = subjectRateParamService.update(subjectRateParam);
        return new RestResponseBuilder<SubjectRate>().success(subjectRate);
    }

    /**
     * 查询所有债权转让配置规则
     * @return
     */
    @GetMapping("/subject/subjectTransferParam")
    public RestResponse<List> getSubjectRateParam() {
        List<SubjectTransferParam> subjectRateParams = subjectTransferParamService.findSubjectTransferParam();
        return new RestResponseBuilder<List>().success(subjectRateParams);
    }

    /**
     * 插入新的债权转让配置规则
     * @param subjectTransfer
     * @return
     */
    @PutMapping("/subject/saveSubjectTransferParam")
    public RestResponse<SubjectTransferParam> saveSubjectTransferParam(@RequestBody SubjectTransferParam subjectTransfer) {
        SubjectTransferParam subjectTransferParam = new SubjectTransferParam();
        BeanUtils.copyProperties(subjectTransfer, subjectTransferParam);
        subjectTransferParam = subjectTransferParamService.insert(subjectTransferParam);
        subjectTransfer.setId(subjectTransferParam.getId());
        subjectTransfer.setTransferParamCode(subjectTransferParam.getTransferParamCode());
        return new RestResponseBuilder<SubjectTransferParam>().success(subjectTransfer);
    }

    /**
     * 根据id查询债权转让配置规则
     * @param id
     * @return
     */
    @RequestMapping(value = "/subject/findSubjectTransferParamById/{id}", method = RequestMethod.GET)
    public RestResponse<SubjectTransferParam> getSubjectTransferParam(@PathVariable("id") String id) {
        SubjectTransferParam subjectTransferParam = subjectTransferParamService.getSubjectTransferParamById(Integer.parseInt(id));
        if (subjectTransferParam == null) {
            return new RestResponseBuilder<SubjectTransferParam>().success(null);
        }
        return new RestResponseBuilder<SubjectTransferParam>().success(subjectTransferParam);
    }

    /**
     * 根据id更新债权转让配置规则表
     * @param subjectTransfer
     * @return
     */
    @PutMapping("/subject/updateSubjectTransferParam")
    public RestResponse<SubjectTransferParam> updateSubjectRateParam(@RequestBody SubjectTransferParam subjectTransfer) {
        SubjectTransferParam subjectTransferParam = new SubjectTransferParam();
        BeanUtils.copyProperties(subjectTransfer, subjectTransferParam);
        subjectTransfer = subjectTransferParamService.update(subjectTransferParam);
        return new RestResponseBuilder<SubjectTransferParam>().success(subjectTransfer);
    }

    /**
     * 查询标的投资参数定义表
     * @return
     */
    @GetMapping("/subject/subjectInvestParam")
    public RestResponse<List> getSubjectInvestParam() {
        List<SubjectInvestParamDef> subjectInvestParams = subjectInvestParamService.findSubjectInvestParam();
        return new RestResponseBuilder<List>().success(subjectInvestParams);
    }

    /**
     * 新增标的投资参数
     * @param subjectInvest
     * @return
     */
    @PutMapping("/subject/saveSubjectInvestParam")
    public RestResponse<SubjectInvestParamDef> saveSubjectInvestParam(@RequestBody SubjectInvestParamDef subjectInvest) {
        SubjectInvestParamDef subjectInvestParam = new SubjectInvestParamDef();
        BeanUtils.copyProperties(subjectInvest, subjectInvestParam);
        subjectInvestParam = subjectInvestParamService.addInvestParamDef(subjectInvestParam);
        subjectInvest.setId(subjectInvestParam.getId());
        return new RestResponseBuilder<SubjectInvestParamDef>().success(subjectInvest);
    }

    /**
     * 根据id查询标的投资参数
     * @param id
     * @return
     */
    @RequestMapping(value = "/subject/findSubjectInvestParamById/{id}", method = RequestMethod.GET)
    public RestResponse<SubjectInvestParamDef> getSubjectInvestParam(@PathVariable("id") String id) {
        SubjectInvestParamDef subjectInvestParam= subjectInvestParamService.getInvestParamDef(Integer.parseInt(id));
        if (subjectInvestParam == null) {
            return new RestResponseBuilder<SubjectInvestParamDef>().success(null);
        }
        return new RestResponseBuilder<SubjectInvestParamDef>().success(subjectInvestParam);
    }

    /**
     * 根据id更新标的投资参数
     * @param subjectInvest
     * @return
     */
    @PutMapping("/subject/updateSubjectInvesParam")
    public RestResponse<SubjectInvestParamDef> updateSubjectInvesParam(@RequestBody SubjectInvestParamDef subjectInvest) {
        SubjectInvestParamDef subjectInvestParam = new SubjectInvestParamDef();
        BeanUtils.copyProperties(subjectInvest, subjectInvestParam);
        subjectInvest = subjectInvestParamService.update(subjectInvestParam);
        return new RestResponseBuilder<SubjectInvestParamDef>().success(subjectInvest);
    }

    /**
     * 查询最后一条投资参数
     * @return
     */
    @GetMapping("/subject/findLastInvestParam")
    public RestResponse<SubjectInvestParamDef> findLastInvestParam() {
        SubjectInvestParamDef subjectInvestParam = subjectInvestParamService.findLastInvestParam();
        return new RestResponseBuilder<SubjectInvestParamDef>().success(subjectInvestParam);
    }

    /**
     * 查询最后一条债转配置参数
     * @return
     */
    @GetMapping("/subject/findLastTransferParam")
    public RestResponse<SubjectTransferParam> findLastTransferParam() {
        SubjectTransferParam subjectTransferParam = subjectTransferParamService.findLastTransferParam();
        return new RestResponseBuilder<SubjectTransferParam>().success(subjectTransferParam);
    }

    /**
     * 根据标的发行期数查询发行利率
     */
    @PutMapping(value = "/subject/findSubjectRateParamByParam")
    public RestResponse<SubjectRate> findSubjectRateParamByParam(@RequestBody SubjectRate subjectRate) {
        String operationType = subjectRate.getOperationType();
        SubjectRate subjectRateParam = null;
        if (OPERATION_TYPE_M.equals(operationType)){
            subjectRateParam = subjectRateParamService.findSubjectRateParamByMParam(subjectRate);
        }else if (OPERATION_TYPE_D.equals(operationType)){
            subjectRateParam = subjectRateParamService.findSubjectRateParamByDParam(subjectRate);
        }

        if (subjectRateParam == null) {
            return new RestResponseBuilder<SubjectRate>().success(null);
        }
        return new RestResponseBuilder<SubjectRate>().success(subjectRateParam);
    }


}
