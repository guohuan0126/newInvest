package com.jiuyi.ndr.service.iplan;

import com.github.pagehelper.PageHelper;
import com.jiuyi.ndr.dao.iplan.IPlanRepayDetailDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.iplan.IPlanRepayDetail;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lixiaolei on 2017/6/16.
 */
@Service
public class IPlanRepayDetailService {

    @Autowired
    private IPlanRepayDetailDao iPlanRepayDetailDao;
    @ProductSlave
    public List<IPlanRepayDetail> getByPageHelper(String userId, Integer iPlanId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return this.getByUserIdAndIPlanId(userId, iPlanId);
    }

    public List<IPlanRepayDetail> getByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId is can not null when find iPlan repay detail");
        }
        return this.getByUserIdAndIPlanId(userId, null);
    }
    @ProductSlave
    public List<IPlanRepayDetail> getByUserIdAndIPlanId(String userId, Integer iPlanId) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId is can not null when find iPlan repay detail");
        }
        return iPlanRepayDetailDao.findByUserIdAndIPlanId(userId, iPlanId);
    }

    @ProductSlave
    public List<IPlanRepayDetail> getByUserIdAndStatus(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId is can not null when find iPlan repay detail");
        }
        return iPlanRepayDetailDao.findByUserIdAndStatus(userId);
    }

}
