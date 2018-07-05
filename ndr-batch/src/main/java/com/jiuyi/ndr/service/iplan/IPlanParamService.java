package com.jiuyi.ndr.service.iplan;

import com.jiuyi.ndr.dao.iplan.IPlanParamDao;
import com.jiuyi.ndr.domain.iplan.IPlanParam;
import com.jiuyi.ndr.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by drw on 2017/6/10.
 */
@Service
public class IPlanParamService {

    private final static Logger logger = LoggerFactory.getLogger(IPlanParamService.class);

    @Autowired
    private IPlanParamDao iPlanParamDao;

    /**
     * 根据id查询详细信息
     */
    public IPlanParam getIPlanParamById(int id) {
        if (id == 0) {
            throw new IllegalArgumentException("id can not be null");
        }
        return iPlanParamDao.getIPlanParamById(id);
    }

    //创建定期理财计划参数
    public IPlanParam create(IPlanParam iPlanParam){

        return this.insert(iPlanParam);
    }

    //插入参数配置表
    public IPlanParam insert(IPlanParam iPlanParam) {
        if (iPlanParam == null) {
            throw new IllegalArgumentException("iPlanParam is can not null");
        }
        iPlanParam.setCreateTime(DateUtil.getCurrentDateTime19());
        iPlanParamDao.insert(iPlanParam);
        return iPlanParam;
    }
    //查询所有的参数
    public List<IPlanParam> findAll() {
        return iPlanParamDao.findAll();
    }

    /**
     * 修改参数配置
     */
    public IPlanParam update(IPlanParam iPlanParam) {
        if (iPlanParam == null) {
            throw new IllegalArgumentException("iPlanParam is can not null");
        }
        iPlanParam.setUpdateTime(DateUtil.getCurrentDateTime19());
        iPlanParamDao.update(iPlanParam);
        return iPlanParam;
    }

    //查询所有的参数按最新添加的倒叙
    public List<IPlanParam> findIPlanParamOrderById() {
        return iPlanParamDao.findIPlanParamOrderById();
    }
}
