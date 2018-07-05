package com.jiuyi.ndr.exception;

import com.jiuyi.ndr.error.Error;

/**
 * Created by zhangyibo on 2017/4/10.
 */
public class ProcessException extends RuntimeException{

    private String errorCode;

    public ProcessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ProcessException(Error error){
        super(error.getMessage());
        this.errorCode = error.getCode();
    }

    public String getErrorCode() {
        return errorCode;
    }

}
