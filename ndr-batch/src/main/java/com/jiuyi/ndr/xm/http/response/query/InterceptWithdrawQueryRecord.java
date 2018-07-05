package com.jiuyi.ndr.xm.http.response.query;

/**
 * 提现拦截查询返回
 * Created by lixiaolei on 2017/4/24.
 */
public class InterceptWithdrawQueryRecord implements Record {

    private static final long serialVersionUID = -8323585551828508787L;

    private String requestNo;//请求流水号
    private String withdrawRequestNo;//提现请求流水号
    private String createTime;//发起时间
    private String completedTime;//完成时间
    private String status;//见【提现拦截状态】

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public String getWithdrawRequestNo() {
        return withdrawRequestNo;
    }

    public void setWithdrawRequestNo(String withdrawRequestNo) {
        this.withdrawRequestNo = withdrawRequestNo;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(String completedTime) {
        this.completedTime = completedTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
