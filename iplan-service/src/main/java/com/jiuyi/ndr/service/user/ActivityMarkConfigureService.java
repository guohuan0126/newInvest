package com.jiuyi.ndr.service.user;

import com.jiuyi.ndr.dao.redpacket.ActivityMarkConfigureDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author ke 2017/6/16
 */
@Service
public class ActivityMarkConfigureService {

    @Value("${iPlan.oss.imagePrefix}")
    private String imageUrlPrefix;

    @Autowired
    private ActivityMarkConfigureDao activityMarkConfigureDao;

    /**
     * 根据活动id取得图片id
     */
    @ProductSlave
    public String getImgUrl(Integer activityId) {
        ActivityMarkConfigure activity = activityMarkConfigureDao.findById(activityId);
        if (activity == null) {
            return "";
        }
        return imageUrlPrefix + activity.getImageUrl();
    }

    public String getNewBieUrl() {
        return imageUrlPrefix + "/app/loan/newbieenjoy_v3.png";
    }
    @ProductSlave
    public ActivityMarkConfigure findById(Integer activityId){
        ActivityMarkConfigure amc = activityMarkConfigureDao.findById(activityId);
        return amc != null ? amc : new ActivityMarkConfigure();
    }
}
