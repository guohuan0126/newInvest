package com.jiuyi.ndr.dto.iplan.mobile;

import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lixiaolei on 2017/6/26.
 */
public class IPlanAppInvestedShareDto implements Serializable {

    private static final long serialVersionUID = -1573174136451170833L;

    public static final double IPLAN_INVEST_AMT = 50000;

    public static final String TYJ_FLAG_Y = "1";
    public static final String TYJ_FLAG_N = "0";

    private String title = "恭喜您加入成功！";
    private String desc = "将在次日计息，您可以进入我的-月月盈中查看\n";
    private String pictureUrl = "http://duanrongweb.oss-cn-qingdao.aliyuncs.com/app/loan/img_yaoqing2@2x.png";
    private Map<String, String> shares;
    private String tyjUrl;//体验金链接
    private String tyjMsg;//体验金信息
    private String tyjFlag;//体验金标识
    private Share shareMsg;
    public static class Share {
        public static final String TYPE_18 = "18";
        public static final String TYPE_WEIGHT = "weight";
        public static final int DIRECT_JUMP_FLAG_Y = 1;

        private String title;//活动标题
        private String desc;//活动内容
        private String pictureUrl;//活动图片
        private String buttonName;//按钮名称
        private String buttonUrl;//按钮链接
        private String shareTitle;//分享标题
        private String shareDesc;//分享内容
        private String shareUrl;//分享链接
        private String sharePicture;//分享图片
        private String iplanInvestSuccessDesc;//月月盈投资完成信息
        private String type;//类型区分，18,18日活动，weight，加权收益
        private int directJumpFlag = 0;//是否跳转，0为不跳转，1为跳转
        private String directJumpUrl = "";

        public String getDirectJumpUrl() {
            return directJumpUrl;
        }

        public void setDirectJumpUrl(String directJumpUrl) {
            this.directJumpUrl = directJumpUrl;
        }

        public int getDirectJumpFlag() {
            return directJumpFlag;
        }

        public void setDirectJumpFlag(int directJumpFlag) {
            this.directJumpFlag = directJumpFlag;
        }

        public String getIplanInvestSuccessDesc() {
            return iplanInvestSuccessDesc;
        }

        public void setIplanInvestSuccessDesc(String iplanInvestSuccessDesc) {
            this.iplanInvestSuccessDesc = iplanInvestSuccessDesc;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPictureUrl() {
            return pictureUrl;
        }

        public void setPictureUrl(String pictureUrl) {
            this.pictureUrl = pictureUrl;
        }

        public String getButtonUrl() {
            return buttonUrl;
        }

        public void setButtonUrl(String buttonUrl) {
            this.buttonUrl = buttonUrl;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getButtonName() {
            return buttonName;
        }

        public void setButtonName(String buttonName) {
            this.buttonName = buttonName;
        }

        public String getShareTitle() {
            return shareTitle;
        }

        public void setShareTitle(String shareTitle) {
            this.shareTitle = shareTitle;
        }

        public String getShareDesc() {
            return shareDesc;
        }

        public void setShareDesc(String shareDesc) {
            this.shareDesc = shareDesc;
        }

        public String getShareUrl() {
            return shareUrl;
        }

        public void setShareUrl(String shareUrl) {
            this.shareUrl = shareUrl;
        }

        public String getSharePicture() {
            return sharePicture;
        }

        public void setSharePicture(String sharePicture) {
            this.sharePicture = sharePicture;
        }
    }

    public String getTyjFlag() {
        return tyjFlag;
    }

    public void setTyjFlag(String tyjFlag) {
        this.tyjFlag = tyjFlag;
    }

    public Share getShareMsg() {
        return shareMsg;
    }

    public void setShareMsg(Share shareMsg) {
        this.shareMsg = shareMsg;
    }

    public String getTyjUrl() {
        return tyjUrl;
    }

    public void setTyjUrl(String tyjUrl) {
        this.tyjUrl = tyjUrl;
    }

    public String getTyjMsg() {
        return tyjMsg;
    }

    public void setTyjMsg(String tyjMsg) {
        this.tyjMsg = tyjMsg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public Map<String, String> getShares() {
        return shares;
    }

    public void setShares(String phoneNo) {
        Map<String, String> shares = new HashMap<>();
        shares.put("title", "短融网送你360元现金现金，邀你一起轻松赚钱!");
        shares.put("shareDesc", "加入立拿360元现金，灵活理财，动动手指就赚钱");
        if (StringUtils.hasText(phoneNo)) {
            shares.put("url", "http://m.duanrong.com/activities/2016inviteFriend/register1?referrer=" + phoneNo.trim());//各环境不同
        } else {
            shares.put("url", "http://m.duanrong.com/activities/2016inviteFriend/register1");//各环境不同
        }
        shares.put("picture", "http://m.duanrong.com/images/activities/2016inviteFriend/share2.jpg");
        this.shares = shares;
    }
}
