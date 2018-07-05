package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * Created by YU on 2017/11/8.
 */
public class VehicleInfoPic extends BaseDomain {


    //标题
    private String title;
    //路径
    private String url;

    private Integer seqNum;

    private String odsUpdateTime;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public Integer getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(Integer seqNum) {
        this.seqNum = seqNum;
    }

    public String getOdsUpdateTime() {
        return odsUpdateTime;
    }

    public void setOdsUpdateTime(String odsUpdateTime) {
        this.odsUpdateTime = odsUpdateTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
