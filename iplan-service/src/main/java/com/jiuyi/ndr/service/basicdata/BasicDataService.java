package com.jiuyi.ndr.service.basicdata;

import com.jiuyi.ndr.dao.basicdata.BasicDataDao;
import com.jiuyi.ndr.domain.basicdata.Dictionary;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by drw on 2017/6/12.
 */
@Service
public class BasicDataService {

    @Autowired
    private BasicDataDao basicDataDao;

    public List<Dictionary> getDataDictionaryByTypeCode(String typeCode) {
        if (StringUtils.isBlank(typeCode)) {
            throw new IllegalArgumentException("typeCode can not be null");
        }
        return basicDataDao.getDataDictionaryByTypeCode(typeCode);
    }

}
