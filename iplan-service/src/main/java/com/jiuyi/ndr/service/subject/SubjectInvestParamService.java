package com.jiuyi.ndr.service.subject;

import com.jiuyi.ndr.dao.subject.SubjectInvestParamDefDao;
import com.jiuyi.ndr.domain.subject.SubjectInvestParamDef;
import com.jiuyi.ndr.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lixiaolei on 2017/4/13.
 */
@Service
public class SubjectInvestParamService {

    @Autowired
    private SubjectInvestParamDefDao subjectInvestParamDefDao;

    /**
     * 新增投资参数定义
     */
    public SubjectInvestParamDef addInvestParamDef(SubjectInvestParamDef subjectInvestParam) {
        if (subjectInvestParam == null) {
            throw new IllegalArgumentException("subjectTransfer is can not null");
        }

        subjectInvestParam.setCreateTime(DateUtil.getCurrentDateTime19());
        subjectInvestParam.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectInvestParamDefDao.insert(subjectInvestParam);

        Integer id = subjectInvestParam.getId();
        //随机生成code
        String prefix = "investCode";
        String investCode = prefix + id;
        subjectInvestParam.setCode(investCode);
        subjectInvestParam.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectInvestParamDefDao.updateCode(subjectInvestParam);
        return subjectInvestParam;
    }

    /**
     * 查询投资参数定义
     */
    public SubjectInvestParamDef getInvestParamDef(Integer paramDefId) {
        if (paramDefId == null) {
            throw new IllegalArgumentException("查询投资参数定义表时，id不能为空");
        }
        return subjectInvestParamDefDao.findById(paramDefId);
    }

    /**
     * 查询标的投资参数定义表
     * @return
     */
    public List<SubjectInvestParamDef> findSubjectInvestParam() {
        return subjectInvestParamDefDao.findSubjectInvestParam();
    }

    /**
     * 根据Id更新标的投资参数
     * @param subjectInvestParam
     * @return
     */
    public SubjectInvestParamDef update(SubjectInvestParamDef subjectInvestParam) {
        if (subjectInvestParam == null) {
            throw new IllegalArgumentException("subjectInvestParam is can not null");
        }
        subjectInvestParam.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectInvestParamDefDao.update(subjectInvestParam);
        return subjectInvestParam;
    }

    /**
     * 查询最后一条投资参数
     * @return
     */
    public SubjectInvestParamDef findLastInvestParam() {
        return subjectInvestParamDefDao.findLastInvestParam();
    }


}
