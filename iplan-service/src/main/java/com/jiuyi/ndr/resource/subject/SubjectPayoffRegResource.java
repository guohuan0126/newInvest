package com.jiuyi.ndr.resource.subject;

import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectPayoffReg;
import com.jiuyi.ndr.domain.subject.SubjectRate;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.service.subject.SubjectPayoffRegService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
  * @author YUMIN
  * @date 2017/12/25
  */
@RestController
public class SubjectPayoffRegResource {

    @Autowired
    SubjectPayoffRegService subjectPayoffRegService;

    /**
     * 标的提前结清接口
     * @param subject
     * @return
     */
    @PostMapping("/subject/settleAndReissue")
    public RestResponse<SubjectPayoffReg> settleAndReissue(@RequestBody Subject subject) {
        SubjectPayoffReg subjectPayoffRegParam= new SubjectPayoffReg();
        if(subject!=null){
            subjectPayoffRegParam = subjectPayoffRegService.subjectSettle(subject.getSubjectId());
        }
        return new RestResponseBuilder<SubjectPayoffReg>().success(subjectPayoffRegParam);
    }



}
