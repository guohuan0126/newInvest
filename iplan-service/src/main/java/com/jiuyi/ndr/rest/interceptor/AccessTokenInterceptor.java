package com.jiuyi.ndr.rest.interceptor;

import com.jiuyi.ndr.dao.user.AccessTokenDao;
import com.jiuyi.ndr.domain.user.AccessToken;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by WangGang on 2017/5/8.
 */
@Component
public class AccessTokenInterceptor extends HandlerInterceptorAdapter {
    private static String TOKEN_PARAM_KEY = "userToken";
    private static String USER_PARAM_KEY = "userId";

    @Autowired
    private AccessTokenDao accessTokenDao;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getParameter(TOKEN_PARAM_KEY);
        String userId = request.getParameter(USER_PARAM_KEY);
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(userId)) {
            throw new ProcessException(Error.INVALID_TOKEN);
        }
        //判断token是否有效
        AccessToken accessToken = accessTokenDao.findByIdAndUserId(token, userId);
        if (accessToken == null || !"valid".equalsIgnoreCase(accessToken.getStatus())) {
            throw new ProcessException(Error.INVALID_TOKEN);
        }
        return true;
    }

}
