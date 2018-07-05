package com.jiuyi.ndr.service.drpay;

import java.util.Date;

/**
 * @author zhq
 * @date 2017/10/26 17:31
 */
public class MarketingResponse {
    public static final String SUCCESS = "1";
    public static final String FAIL = "0";

    public static final String SOURCE_APP = "app";
    public static final String SOURCE_PC = "pc";
    /**天天赚*/
    public static final String INVEST_TYPE_LPLAN = "lplan";
    /**月月盈*/
    public static final String INVEST_TYPE_IPLAN = "iplan";
    /**散标*/
    public static final String INVEST_TYPE_INVEST = "invest";

    /**债权*/
    public static final String INVEST_TYPE_CREDIT = "credit";

    //错误码
    private String errcode;

    //错误描述
    private String errmsg;

    //响应时间
    private Date responseTime;

    //相应的接口版本
    private String version;

    //输出数据
    private MarketingInvestSuccess data;

    public class MarketingInvestSuccess {

        private int id;
        private String name;//成功页名称
        private String times_type;//频次类型（every：每次、day：每天、week：每周）
        private int pc_show;//pc是否展示（0：否，1：是）
        private int app_show;//app是否展示（0：否，1：是）
        private String invest_type;//投资类型（lplan：天天赚、iplan：月月盈，invest：散标）
        private int user_current_level;//用户等级
        private int has_redpacket;//有无未使用红包券
        private Date start_time;//开始时间
        private Date end_time;//结束时间
        private int is_available;//是否有效（0：无效、1：有效）
        private String picture_url;//成功页图示地址
        private String picture_title;//成功页描述标题
        private String picture_content;//成功页描述文字
        private String button_name;//按钮名称
        private String button_url;//按钮去向
        private String share_title;//分享标题
        private String share_content;//分享内容
        private String share_url;//分享图url

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTimes_type() {
            return times_type;
        }

        public void setTimes_type(String times_type) {
            this.times_type = times_type;
        }

        public int getPc_show() {
            return pc_show;
        }

        public void setPc_show(int pc_show) {
            this.pc_show = pc_show;
        }

        public int getApp_show() {
            return app_show;
        }

        public void setApp_show(int app_show) {
            this.app_show = app_show;
        }

        public String getInvest_type() {
            return invest_type;
        }

        public void setInvest_type(String invest_type) {
            this.invest_type = invest_type;
        }

        public int getUser_current_level() {
            return user_current_level;
        }

        public void setUser_current_level(int user_current_level) {
            this.user_current_level = user_current_level;
        }

        public int getHas_redpacket() {
            return has_redpacket;
        }

        public void setHas_redpacket(int has_redpacket) {
            this.has_redpacket = has_redpacket;
        }

        public Date getStart_time() {
            return start_time;
        }

        public void setStart_time(Date start_time) {
            this.start_time = start_time;
        }

        public Date getEnd_time() {
            return end_time;
        }

        public void setEnd_time(Date end_time) {
            this.end_time = end_time;
        }

        public int getIs_available() {
            return is_available;
        }

        public void setIs_available(int is_available) {
            this.is_available = is_available;
        }

        public String getPicture_url() {
            return picture_url;
        }

        public void setPicture_url(String picture_url) {
            this.picture_url = picture_url;
        }

        public String getPicture_title() {
            return picture_title;
        }

        public void setPicture_title(String picture_title) {
            this.picture_title = picture_title;
        }

        public String getPicture_content() {
            return picture_content;
        }

        public void setPicture_content(String picture_content) {
            this.picture_content = picture_content;
        }

        public String getButton_name() {
            return button_name;
        }

        public void setButton_name(String button_name) {
            this.button_name = button_name;
        }

        public String getButton_url() {
            return button_url;
        }

        public void setButton_url(String button_url) {
            this.button_url = button_url;
        }

        public String getShare_title() {
            return share_title;
        }

        public void setShare_title(String share_title) {
            this.share_title = share_title;
        }

        public String getShare_content() {
            return share_content;
        }

        public void setShare_content(String share_content) {
            this.share_content = share_content;
        }

        public String getShare_url() {
            return share_url;
        }

        public void setShare_url(String share_url) {
            this.share_url = share_url;
        }

        @Override
        public String toString() {
            return "MarketingInvestSuccess{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", times_type='" + times_type + '\'' +
                    ", pc_show=" + pc_show +
                    ", app_show=" + app_show +
                    ", invest_type='" + invest_type + '\'' +
                    ", user_current_level=" + user_current_level +
                    ", has_redpacket=" + has_redpacket +
                    ", start_time=" + start_time +
                    ", end_time=" + end_time +
                    ", is_available=" + is_available +
                    ", picture_url='" + picture_url + '\'' +
                    ", picture_title='" + picture_title + '\'' +
                    ", picture_content='" + picture_content + '\'' +
                    ", button_name='" + button_name + '\'' +
                    ", button_url='" + button_url + '\'' +
                    ", share_title='" + share_title + '\'' +
                    ", share_content='" + share_content + '\'' +
                    ", share_url='" + share_url + '\'' +
                    '}';
        }
    }

    public Date getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getErrcode() {
        return errcode;
    }

    public void setErrcode(String errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public MarketingInvestSuccess getData() {
        return data;
    }

    public void setData(MarketingInvestSuccess data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MarketingResponse{" +
                "errcode='" + errcode + '\'' +
                ", errmsg='" + errmsg + '\'' +
                ", responseTime=" + responseTime +
                ", version='" + version + '\'' +
                ", data=" + data +
                '}';
    }

    public static MarketingResponse toGeneratorJSON(String json) {
        return (MarketingResponse) FastJsonUtil.jsonToObj(json, MarketingResponse.class);
    }
}
