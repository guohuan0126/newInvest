package com.jiuyi.ndr.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Copyright : duanrong.com.cn All Rights Reserved Company : 久亿财富（北京）投资有限公司
 * 
 * @Author : 孙铮
 * @CreateTime : 2014-8-27 下午12:29:57
 * @Description : Utils com.duanrong.util ArithUtil.java 工具类
 */
public class ArithUtil {
	// 默认除法运算精度
	private static final int DEF_DIV_SCALE = 10;

	public ArithUtil() {

	}

	/**
	 * 
	 * @description 提供精确的加法运算
	 * @author 孙铮
	 * @time 2014-8-27 下午12:31:43
	 * @param v1 被加数
	 * @param v2 加数
	 * @return 两个参数的和
	 */
	public static double add(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.add(b2).doubleValue();
	}

	/**
	 * 
	 * @description 提供精确的加法运算
	 * @author 孙铮
	 * @time 2014-8-27 下午12:31:43
	 * @param v1 被加数
	 * @param v2 加数
	 * @return 两个参数的和
	 */
	public static double addRound(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return round(b1.add(b2).doubleValue(), 2);
	}

	/**
	 * 
	 * @description 提供精确的减法运算
	 * @author 孙铮
	 * @time 2014-8-27 下午12:32:36
	 * @param v1 被减数
	 * @param v2 减数
	 * @return 两个参数的差
	 */
	public static double sub(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.subtract(b2).doubleValue();
	}

	/**
	 * 
	 * @description 提供精确的乘法运算
	 * @author 孙铮
	 * @time 2014-8-27 下午12:33:13
	 * @param v1 被乘数
	 * @param v2 乘数
	 * @return 两个参数的积
	 */
	public static double mul(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.multiply(b2).doubleValue();
	}

	/**
	 * 
	 * @description 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到 小数点以后10位，以后的数字四舍五入
	 * @author 孙铮
	 * @time 2014-8-27 下午1:54:50
	 * @param v1 被除数
	 * @param v2 除数
	 * @return 两个参数的商
	 */
	public static double div(double v1, double v2) {
		return div(v1, v2, DEF_DIV_SCALE);
	}

	/**
	 * 
	 * @description 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指 定精度，以后的数字四舍五入
	 * @author 孙铮
	 * @time 2014-8-27 下午1:55:18
	 * @param v1 被除数
	 * @param v2 除数
	 * @param scale 表示表示需要精确到小数点以后几位
	 * @return 两个参数的商
	 */
	public static double div(double v1, double v2, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException("参数scale必须为整数为零!");
		}
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 
	 * @description 提供精确的小数位四舍五入处理
	 * @author 孙铮
	 * @time 2014-8-27 下午1:55:55
	 * @param v 需要四舍五入的数字
	 * @param scale 小数点后保留几位
	 * @return 四舍五入后的结果
	 */
	public static double round(double v, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException("参数scale必须为整数或零!");
		}
		BigDecimal b = new BigDecimal(Double.toString(v));
		BigDecimal one = new BigDecimal("1");
		return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * @description 提供精确的小数位四舍五入处理
	 * @author ＹＵＭＩＮ
	 * @time 2017-11-22 下午1:55:55
	 * @param v 需要四舍五入的数字
	 * @param scale 小数点后保留几位
	 * @return 向下取的结果
	 */
	public static double roundDown(double v, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException("参数scale必须为整数或零!");
		}
		BigDecimal b = new BigDecimal(Double.toString(v));
		BigDecimal one = new BigDecimal("1");
		return b.divide(one, scale, BigDecimal.ROUND_HALF_DOWN).doubleValue();
	}

	/**
	 * 
	 * @description 提供精确的类型转换(Float)
	 * @author 孙铮
	 * @time 2014-8-27 下午1:56:20
	 * @param v 需要被转换的数字
	 * @return 返回转换结果
	 */
	public static float convertsToFloat(double v) {
		BigDecimal b = new BigDecimal(v);
		return b.floatValue();
	}

	/**
	 * 
	 * @description 提供精确的类型转换(Int)不进行四舍五入
	 * @author 孙铮
	 * @time 2014-8-27 下午1:56:45
	 * @param v 需要被转换的数字
	 * @return 返回转换结果
	 */
	public static int convertsToInt(double v) {
		BigDecimal b = new BigDecimal(v);
		return b.intValue();
	}

	/**
	 * 
	 * @description 提供精确的类型转换(Long)
	 * @author 孙铮
	 * @time 2014-8-27 下午1:57:04
	 * @param v 需要被转换的数字
	 * @return 返回转换结果
	 */
	public static long convertsToLong(double v) {
		BigDecimal b = new BigDecimal(v);
		return b.longValue();
	}

	/**
	 * 
	 * @description 返回两个数中大的一个值
	 * @author 孙铮
	 * @time 2014-8-27 下午1:57:31
	 * @param v1 需要被对比的第一个数
	 * @param v2 需要被对比的第二个数
	 * @return 返回两个数中大的一个值
	 */
	public static double returnMax(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.max(b2).doubleValue();
	}

	/**
	 * 
	 * @description 返回两个数中小的一个值
	 * @author 孙铮
	 * @time 2014-8-27 下午1:57:58
	 * @param v1 需要被对比的第一个数
	 * @param v2 需要被对比的第二个数
	 * @return 返回两个数中小的一个值
	 */
	public static double returnMin(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.min(b2).doubleValue();
	}

	/**
	 * 
	 * @description 精确比较两个数字
	 * @author 孙铮
	 * @time 2014-8-27 下午1:58:23
	 * @param v1 需要被对比的第一个数
	 * @param v2 需要被对比的第二个数
	 * @return 如果两个数一样则返回0，如果第一个数比第二个数大则返回1，反之返回-1
	 */
	public static int compareTo(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.compareTo(b2);
	}

	/**
	 * 
	 * @description 获取数字小数位数
	 * @author 孙铮
	 * @time 2014-8-27 下午1:58:52
	 * @param number 数字.
	 * @return 小数位数
	 */
	public static int getDecimals(double number) {
		DecimalFormat decimalFormat = new DecimalFormat("#.####");
		String numberString = decimalFormat.format(number);
		if (numberString.indexOf(".") > 0) {
			return numberString.length() - String.valueOf(number).indexOf(".")
					- 1;
		} else {
			return 0;
		}
	}

	/**
	 * 
	 * @description 获取数字小数位数
	 * @author 孙铮
	 * @time 2014-8-27 下午1:59:19
	 * @param number 数字.
	 * @return 小数位数
	 */
	public static int getDecimals(float number) {
		DecimalFormat decimalFormat = new DecimalFormat("#.####");
		String numberString = decimalFormat.format(number);
		if (numberString.indexOf(".") > 0) {
			return numberString.length() - String.valueOf(number).indexOf(".")
					- 1;
		} else {
			return 0;
		}
	}

	/**
	 * 
	 * @description 对double数据进行取精度
	 * @author 孙铮
	 * @time 2014-8-27 下午1:59:43
	 * @param value double数据.
	 * @param scale 精度位数(保留的小数位数)
	 * @param roundingMode 精度取值方式
	 * @return 精度计算后的数据
	 */
	public static double round(double value, int scale, int roundingMode) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(scale, roundingMode);
		double d = bd.doubleValue();
		bd = null;
		return d;
	}

	/**
	 * 计算两个日期相差天数
	 * 
	 * @param startDay 开始日期
	 * @param endDay 还款日期
	 * @return
	 */
	public static Integer dayDifference(String startDay, String endDay) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		long to;
		long from;
		Integer dayNum = 0;
		try {
			to = df.parse(endDay).getTime();
			from = df.parse(startDay).getTime();
			dayNum = (int) ((to - from) / (1000 * 60 * 60 * 24));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return dayNum + 1;
	}

	public static Double calcExp(Double d1,Double d2,Double d3,Double d4){
		return round(d1,2)-round(d2,2)-round(d3,2)-round(d4,2);
	}
}
