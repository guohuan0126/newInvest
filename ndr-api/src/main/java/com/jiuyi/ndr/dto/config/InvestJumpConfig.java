package com.jiuyi.ndr.dto.config;

/**
 * @author zhq
 * @date 2018/7/2 18:14
 */
public class InvestJumpConfig {
    private int jumpSwitch;
    private String jumpUrl;

    public int getJumpSwitch() {
        return jumpSwitch;
    }

    public void setJumpSwitch(int jumpSwitch) {
        this.jumpSwitch = jumpSwitch;
    }

    public String getJumpUrl() {
        return jumpUrl;
    }

    public void setJumpUrl(String jumpUrl) {
        this.jumpUrl = jumpUrl;
    }

    @Override
    public String toString() {
        return "InvstJumpConfig{" +
                "jumpSwitch=" + jumpSwitch +
                ", jumpUrl='" + jumpUrl + '\'' +
                '}';
    }
}
