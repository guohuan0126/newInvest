package com.jiuyi.ndr.service.marketing;

import com.jiuyi.ndr.dao.marketing.MarketingMemberDao;
import com.jiuyi.ndr.dao.marketing.MarketingPrivilegeDao;
import com.jiuyi.ndr.dao.marketing.MarketingVipPrivilegeDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.marketing.MarketingMember;
import com.jiuyi.ndr.domain.marketing.MarketingPrivilege;
import com.jiuyi.ndr.domain.marketing.MarketingVipPrivilege;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class MarketService {

    @Autowired
    private MarketingMemberDao marketingMemberDao;

    @Autowired
    private MarketingPrivilegeDao marketingPrivilegeDao;

    @Autowired
    private MarketingVipPrivilegeDao marketingVipPrivilegeDao;

    //获取用户月月盈vip特权加息利率
    public Map<String,Object> getUserIPlanVipRateAndVipLevel(String userId){
        MarketingMember marketingMember = marketingMemberDao.findById(userId);
        MarketingPrivilege marketingPrivilege = marketingPrivilegeDao.findByKey(MarketingPrivilege.KEY_IPLAN_RATE);
        Map<String ,Object > map = new HashMap<>();
        BigDecimal vipRate = new BigDecimal(0);
        Integer vipLevel = 0;
        if (marketingMember!=null&&marketingMember.getCurrentLevel()!=null&& MarketingMember.CURRENT_STATUS_ON.equals(marketingMember.getCurrentStatus())){
            vipLevel = marketingMember.getCurrentLevel();
            MarketingVipPrivilege marketingVipPrivilege =  marketingVipPrivilegeDao.findByVipIdAndPrivilegeId(marketingMember.getCurrentLevel(),marketingPrivilege.getId());
            if (marketingVipPrivilege!=null){
                vipRate =new BigDecimal(marketingVipPrivilege.getInterestRate()==null? 0 : marketingVipPrivilege.getInterestRate());
            }
        }
        map.put("vipRate",vipRate);
        map.put("vipLevel",vipLevel);
        return map;
    }
    @ProductSlave
    public double getIplanVipRate(String userId) {
        double vipRate = 0;
        if (StringUtils.isNotBlank(userId)) {
            MarketingMember marketingMember = marketingMemberDao.findById(userId);
            if (marketingMember != null) {
                Integer vipLevel = marketingMember.getCurrentLevel();
                MarketingPrivilege marketingPrivilege = marketingPrivilegeDao.findByKey(MarketingPrivilege.KEY_IPLAN_RATE);
                if (marketingPrivilege != null) {
                    MarketingVipPrivilege marketingVipPrivilege = marketingVipPrivilegeDao.findByVipIdAndPrivilegeId(vipLevel, marketingPrivilege.getId());
                    if (marketingVipPrivilege != null) {
                        vipRate = marketingVipPrivilege.getInterestRate();
                    }
                }
            }
        }
        return vipRate;
    }

    @ProductSlave
    public Integer getCreditFreeTimes(String userId){
        Integer times = 0;
        if (StringUtils.isNotBlank(userId)) {
            MarketingMember marketingMember = marketingMemberDao.findById(userId);
            if (marketingMember != null) {
                Integer vipLevel = marketingMember.getCurrentLevel();
                MarketingPrivilege marketingPrivilege = marketingPrivilegeDao.findByKey(MarketingPrivilege.KEY_CREDIT_FREE);
                if (marketingPrivilege != null) {
                    MarketingVipPrivilege marketingVipPrivilege = marketingVipPrivilegeDao.findByVipIdAndPrivilegeId(vipLevel, marketingPrivilege.getId());
                    if (marketingVipPrivilege != null) {
                        times = marketingVipPrivilege.getWithdrawalsTimes();
                    }
                }
            }
        }
        return times;
    }

    @ProductSlave
    public Integer getVip(String userId){
        Integer level = 0;
        if (StringUtils.isNotBlank(userId)) {
            MarketingMember marketingMember = marketingMemberDao.findById(userId);
            if (marketingMember != null) {
                level = marketingMember.getCurrentLevel();
            }
        }
        return level;
    }

}
