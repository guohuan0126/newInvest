package com.jiuyi.ndr.service.xm.util;

import com.jiuyi.ndr.util.DateUtil;

import java.util.UUID;

/**
 * @author ke
 * @since 2017/4/18 16:26
 */
public class IdUtil {

    public static String getRequestNo() {
        String prefix = "NDR";
        String currentDateTime14 = DateUtil.getCurrentDateTime14();
        String suffix = randomUUID().substring(0, 6);
        return prefix + currentDateTime14 + suffix;
    }

    public static void main(String[] args) {
        String uuid = getRequestNo();
        System.out.println(uuid);
    }

    public static String randomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
