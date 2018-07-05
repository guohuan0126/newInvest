package com.jiuyi.ndr.rest.advice;


import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by WangGang on 2017/2/22.
 */
@ControllerAdvice
public class RestExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler
    @ResponseBody
    public RestResponse handleRestException(HttpServletRequest request, Exception e) {
        logger.error("URI [{}]请求异常", request.getRequestURI(), e);
        if (e instanceof ProcessException) {
            return new RestResponseBuilder<>().fail(null, ((ProcessException) e).getErrorCode(), e.getMessage());
        }
        return new RestResponseBuilder<>().fail(null, Error.INTERNAL_ERROR.getCode(), Error.INTERNAL_ERROR.getMessage());
    }
}
