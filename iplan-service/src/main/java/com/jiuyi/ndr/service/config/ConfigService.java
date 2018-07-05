package com.jiuyi.ndr.service.config;

import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.dto.config.InvestJumpConfig;
import com.jiuyi.ndr.dto.iplan.mobile.IPlanAppInvestedShareDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Created by drw on 2017/6/12.
 */
@Service
public class ConfigService {

    @Autowired
    private ConfigDao configDao;

    public Config getConfigById(String id) {
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("id can not be null");
        }
        return configDao.getConfigById(id);
    }

    public Optional<String> getValueById(String id){
        Config config = configDao.getConfigById(id);
        return Optional.of(config.getValue());
    }

    private static final int DollarToCent = 100;

    public InvestJumpConfig getInvestJumpConfig(int amount) {
        InvestJumpConfig config = new InvestJumpConfig();
        Config swithConfig = configDao.getConfigById(Config.APP_DIRECT_JUMP);
        if (swithConfig != null
                && org.apache.commons.lang3.StringUtils.isNotBlank(swithConfig.getValue())
                && org.apache.commons.lang3.StringUtils.equals(String.valueOf(IPlanAppInvestedShareDto.Share.DIRECT_JUMP_FLAG_Y), swithConfig.getValue())) {
            double investAmt = 0;
            Config investAmtConfig = configDao.getConfigById(Config.APP_DIRECT_JUMP_AMT);
            if (investAmtConfig != null
                    && org.apache.commons.lang3.StringUtils.isNotBlank(investAmtConfig.getValue())) {
                investAmt = Double.valueOf(investAmtConfig.getValue());
                if (amount >= investAmt * DollarToCent) {
                    Config urlConfig = configDao.getConfigById(Config.APP_DIRECT_JUMP_URL);
                    if (urlConfig != null
                            && org.apache.commons.lang3.StringUtils.isNotBlank(urlConfig.getValue())) {
                        config.setJumpSwitch(IPlanAppInvestedShareDto.Share.DIRECT_JUMP_FLAG_Y);
                        config.setJumpUrl(urlConfig.getValue());
                    }
                }
            }
        }
        return config;
    }

}
