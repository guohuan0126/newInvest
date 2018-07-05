package com.jiuyi.ndr.service.config;

import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.domain.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfigService {

    @Autowired
    ConfigDao configDao;

    public Optional<String> getValueById(String id){
        Config config = configDao.getConfigById(id);
        return Optional.of(config.getValue());
    }
}
