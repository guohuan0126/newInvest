package com.jiuyi.ndr.service.user;

import com.jiuyi.ndr.dao.user.UserDao;
import com.jiuyi.ndr.domain.user.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author ke 2017/6/9
 */
@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    /**
     * 查询用户
     *
     * @param username 用户名
     */
    public User findByUsername(String username){
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("userId can not be null");
        }
        return userDao.findByUsername(username);
    }

    public User getUserById(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId can not be null");
        }
        return userDao.getUserById(userId);
    }



}
