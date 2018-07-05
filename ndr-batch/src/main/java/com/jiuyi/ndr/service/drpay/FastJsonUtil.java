package com.jiuyi.ndr.service.drpay;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;

/**
 * FastJson 序列化工具
 * 
 * @author xiao
 * 
 */
public final class FastJsonUtil {

	/**
	 * 序列化参数
	 */
	private static final SerializerFeature[] features = {
			SerializerFeature.WriteMapNullValue,
			SerializerFeature.WriteNullBooleanAsFalse,
			SerializerFeature.WriteNullStringAsEmpty,
			SerializerFeature.WriteNullListAsEmpty,
			SerializerFeature.WriteNullNumberAsZero,
			SerializerFeature.DisableCircularReferenceDetect,
			SerializerFeature.WriteDateUseDateFormat};
	
	/**
	 * 类型转换 过滤器(处理null的情况)
	 */
	private static final SerializeFilter[] filters = { new ValueToEmptyFilter() };


	/**
	 * 对象转换成json 支持list,map,array
	 * 不序列化null
	 * @param obj
	 * @return
	 */
	public static String objToJson(Object obj) {
		return JSON.toJSONString(obj,  features);
	}
	
	/**
	 * 对象转换成json 支持list,map,array
	 * 
	 * @param obj
	 * @return
	 */
	public static String objToJsonWriteNullToEmpty(Object obj) {
		return JSON.toJSONString(obj, filters, features);
	}
	
	/**
	 * 对象转换成json 支持list,map,array
	 * 
	 * @param obj
	 * @return
	 */
	public static String objToJsonNoWriteNull(Object obj) {
		return JSON.toJSONString(obj);
	}

	/**
	 * json 转换成对象
	 * 
	 * @param json
	 * @param clazz
	 * @return
	 */
	public static Object jsonToObj(String json, Class<? extends Object> clazz) {
		return JSON.parseObject(json, clazz);
	}
	
	/**
	 * json 转换成对象
	 * 
	 * @param json
	 * @param clazz
	 * @return
	 */
	public static <T> List<T> jsonToArray(String json, Class<T> clazz) {
		return  JSON.parseArray(json, clazz);
	}

}
