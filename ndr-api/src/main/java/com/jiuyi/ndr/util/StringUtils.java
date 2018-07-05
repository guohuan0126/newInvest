package com.jiuyi.ndr.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.springframework.util.StringUtils.hasLength;

/**
 * 将字符串中的某些字符用其他特殊字符替换
 *
 * @author 姜广兴
 * @date 2018-04-17
 */
public abstract class StringUtils {
    private StringUtils() {
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(StringUtils.class);

    /**
     * @param str         待替换字符串
     * @param specialStr  特殊字符串
     * @param rightOffset 字符串右侧长度
     * @return
     */
    public static String replaceLeftWithSpecialStr(String str, String specialStr, int rightOffset) {
        return replaceWithSpecialStr(str, specialStr, 0, rightOffset);
    }

    /**
     * @param str        待替换字符串
     * @param specialStr 特殊字符串
     * @param leftOffset 字符串左侧长度
     * @return
     */
    public static String replaceRightWithSpecialStr(String str, String specialStr, int leftOffset) {
        return replaceWithSpecialStr(str, specialStr, leftOffset, 0);
    }

    /**
     * @param str         待替换字符串
     * @param specialStr  特殊字符串
     * @param leftOffset  字符串左侧长度
     * @param rightOffset 字符串右侧长度
     * @return
     */
    public static String replaceWithSpecialStr(String str, String specialStr, int leftOffset, int rightOffset) {
        LOGGER.debug("str=[{}],replaceStr=[{}],leftOffset=[{}],rightOffset=[{}]", str, specialStr, leftOffset, rightOffset);
        if (!hasLength(str) || !hasLength(specialStr) || leftOffset < 0 || rightOffset < 0) {
            LOGGER.error("参数错误,str=[{}],replaceStr=[{}],leftOffset=[{}],rightOffset=[{}]", str, specialStr, leftOffset, rightOffset);
            throw new IllegalArgumentException("参数错误");
        }

        int need = leftOffset + rightOffset;
        if (need == 0) {
            return specialStr;
        }
        int length = str.length();
        if (leftOffset > 0 && rightOffset > 0) {
            if (length <= leftOffset) {
                return specialStr;
            }
            if (length <= need) {
                return replaceRightWithSpecialStr(str, leftOffset, length, specialStr);
            }
            if (length > need) {
                return str.replaceAll("(.{" + leftOffset + "})(.{" + (length - need) + "})(.{" + rightOffset + "})", "$1" + specialStr + "$3");
            }
        }
        if (leftOffset > 0) {
            if (length <= leftOffset) {
                return specialStr;
            }
            return replaceRightWithSpecialStr(str, leftOffset, length, specialStr);
        }
        if (rightOffset > 0) {
            if (length <= rightOffset) {
                return specialStr;
            }
            return str.replaceAll("(.{" + (length - rightOffset) + "})(.{" + rightOffset + "})", specialStr + "$2");
        }
        return specialStr;
    }

    private static String replaceRightWithSpecialStr(String str, int leftOffset, int length, String specialStr) {
        return str.replaceAll("(.{" + leftOffset + "})(.{" + (length - leftOffset) + "})", "$1" + specialStr);
    }

    public static void main(String[] args) {
        System.out.println(replaceRightWithSpecialStr("测试字符串啊", "**", 2));
        System.out.println(replaceWithSpecialStr("测试字符串啊", "*", 2, 1));
        System.out.println(replaceLeftWithSpecialStr("测试字符串啊", "*", 3));
    }
}
