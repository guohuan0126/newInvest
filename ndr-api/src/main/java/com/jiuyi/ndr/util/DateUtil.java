package com.jiuyi.ndr.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JDK8新的日期API实现的日期工具类
 * Created by zhangyibo on 2017/2/28.
 */
public class DateUtil {

    public static final int HUOR_16 = 16;
    public static final int HUOR_12 = 12;
    public static final int HUOR_20 = 23;
    public static final String DATE_8 = "yyyyMMdd";
    public static final DateTimeFormatter DATE_TIME_FORMATTER_8 = DateTimeFormatter.ofPattern(DATE_8);
    public static final String DATE_10 = "yyyy-MM-dd";
    public static final DateTimeFormatter DATE_TIME_FORMATTER_10 = DateTimeFormatter.ofPattern(DATE_10);
    public static final String DATETIME_19 = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DATE_TIME_FORMATTER_19 = DateTimeFormatter.ofPattern(DATETIME_19);
    public static final String DATETIME_17 = "yyyyMMdd HH:mm:ss";
    public static final DateTimeFormatter DATE_TIME_FORMATTER_17 = DateTimeFormatter.ofPattern(DATETIME_17);
    public static final String DATETIME_14 = "yyyyMMddHHmmss";
    public static final DateTimeFormatter DATE_TIME_FORMATTER_14 = DateTimeFormatter.ofPattern(DATETIME_14);
    public static final String DATETIME_14_01 = "yyyyMMddHHmmssSSS";
    public static final DateTimeFormatter DATE_TIME_FORMATTER_14_01 = DateTimeFormatter.ofPattern(DATETIME_14_01);

    public static final SimpleDateFormat SDF_10 = new SimpleDateFormat("yyyy-MM-dd");

    public static String getCurrentDate() {
        return DATE_TIME_FORMATTER_10.format(LocalDate.now());
    }

    public static String getCurrentDateShort() {
        return DATE_TIME_FORMATTER_8.format(LocalDate.now());
    }

    public static String getCurrentDateTime() {
        return DATE_TIME_FORMATTER_17.format(LocalDateTime.now());
    }

    public static String getCurrentDateTime14() {
        return DATE_TIME_FORMATTER_14.format(LocalDateTime.now());
    }

    public static String getCurrentDateTime14_01() {
        return DATE_TIME_FORMATTER_14_01.format(LocalDateTime.now());
    }

    public static String getCurrentDateTime17() {
        return DATE_TIME_FORMATTER_17.format(LocalDateTime.now());
    }

    public static String getCurrentDateTime19() {
        return DATE_TIME_FORMATTER_19.format(LocalDateTime.now());
    }

    public static String getCurrentDateTime(String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(LocalDateTime.now());
    }

    public static String getCurrentDateTime(DateTimeFormatter formatter) {
        return formatter.format(LocalDateTime.now());
    }

    public static String getDateTimeStr(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return formatter.format(dateTime);
    }

    public static String getDateStr(LocalDate dateTime, DateTimeFormatter formatter) {
        return formatter.format(dateTime);
    }

    public static LocalDateTime parseDateTime(String dateTime,DateTimeFormatter formatter){
        return LocalDateTime.parse(dateTime,formatter);
    }

    public static LocalDate parseDate(String date,DateTimeFormatter formatter){
        return LocalDate.parse(date,formatter);
    }

    public static long betweenDays(String startDateStr, String endDateStr){
        LocalDate startDate = parseDate(startDateStr, DateUtil.DATE_TIME_FORMATTER_8);
        LocalDate endDate = parseDate(endDateStr, DateUtil.DATE_TIME_FORMATTER_8);
        return endDate.toEpochDay()-startDate.toEpochDay();
    }

    public static long betweenDays(LocalDate startDate, LocalDate endDate) {
        return endDate.toEpochDay()-startDate.toEpochDay();
    }

    /**
     * WARN:此方法是根据时分秒的差值来计算相差天数
     * 如果计算2017-05-26 23:59:00 到 2017-05-27 00:01:00 的相差天数并不会是一天 而是0
     * 要计算天数差值 请将LocalDateTime.toLocalDate() 后调用上面的重载方法 betweenDays(LocalDate,LocalDate)
     * @param startDateTime
     * @param endDateTime
     * @return
     */
    public static long betweenDays(LocalDateTime startDateTime,LocalDateTime endDateTime){
        return Duration.between(startDateTime,endDateTime).toDays();
    }
    /**
     *
     * @description 计算两个日期相差天数
     * @author 孙铮
     * @time 2014-8-27 下午2:00:43
     * @return 相差天数
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
    /** 日期是早于还是晚于另一个日期
     * @author guohuan
     * @time 2017-10-30
     * @return true or false true 为前一个时间在后一个时间之前
     */
    public static Boolean compareDateTime(String startDay, String endDay) {
        LocalDateTime startDayTime = DateUtil.parseDateTime(startDay,DateUtil.DATE_TIME_FORMATTER_19);
        LocalDateTime endDayTime = DateUtil.parseDateTime(endDay,DateUtil.DATE_TIME_FORMATTER_19);
        return startDayTime.isAfter(endDayTime);

    }



}
