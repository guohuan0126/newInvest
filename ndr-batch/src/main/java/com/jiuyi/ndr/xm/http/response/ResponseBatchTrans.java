package com.jiuyi.ndr.xm.http.response;

import com.jiuyi.ndr.xm.http.BaseResponse;

/**
 * @author ke 2017/4/25
 */
public class ResponseBatchTrans extends BaseResponse {

    private String batchNo;//批次号

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }


}
