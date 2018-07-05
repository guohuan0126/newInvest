package com.jiuyi.ndr.service.subject;

import com.jiuyi.ndr.dao.subject.SubjectInvestParamDefDao;
import com.jiuyi.ndr.domain.subject.SubjectInvestParamDef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lixiaolei on 2017/4/13.
 */
@Service
public class SubjectInvestParamService {

    @Autowired
    private SubjectInvestParamDefDao subjectInvestParamDefDao;

    /**
     * 查询投资参数定义
     *
     * @param paramDefId
     * @return
     */
    public SubjectInvestParamDef getInvestParamDef(Integer paramDefId) {
        if (paramDefId == null) {
            throw new IllegalArgumentException("查询投资参数定义表时，id不能为空");
        }
        return subjectInvestParamDefDao.findById(paramDefId);
    }

}
