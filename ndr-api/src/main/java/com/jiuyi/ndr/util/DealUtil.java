package com.jiuyi.ndr.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author zhq
 * @date 2018/6/13 16:00
 */
public class DealUtil {

    /**
     * 姓名脱敏
     * @param realname
     * @return
     */
    public static String dealRealname(String realname) {
        if (StringUtils.isBlank(realname)) {
            return realname;
        }
        String string = realname.substring(0, 1);
        for (int i = 0; i < realname.length() - 1; i++) {
            string += "*";
        }
        return string;
    }
    /**
     * 身份证号脱敏
     * @param idCard
     * @return
     */
    public static String dealIdCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return idCard;
        }
        int length = idCard.length();
        if (length == 15) {
            idCard = idCard.replace(idCard.substring(6, 12), "******");
        }
        if (length == 18) {
            idCard = idCard.replace(idCard.substring(6, 14), "********");
        }
        return idCard;
    }

    /**
     * userId脱敏
     * @param userId
     * @return
     */
    public static String dealUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return userId;
        }
        String start = userId.substring(0, 2);
        String end = userId.substring(userId.length() - 2);
        return start + "***" + end;
    }

}
