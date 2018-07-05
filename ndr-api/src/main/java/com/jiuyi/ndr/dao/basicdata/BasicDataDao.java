package com.jiuyi.ndr.dao.basicdata;

import com.jiuyi.ndr.domain.basicdata.Dictionary;
import com.jiuyi.ndr.domain.config.Config;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by YU on 2017/11/6.
 */
@Mapper
public interface BasicDataDao {

    @Select("SELECT * from data_dictionary WHERE status = 1 and type_code = #{typeCode}" +
            " ORDER BY item_code ASC")
    List<Dictionary> getDataDictionaryByTypeCode(String typeCode);

}
