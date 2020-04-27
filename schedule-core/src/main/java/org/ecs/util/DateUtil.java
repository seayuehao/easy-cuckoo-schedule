package org.ecs.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DateUtil {

    private final static String DAY_FORMAT = "yyyyMMdd";

    /**
     * 锁对象
     */
    private static final Object LOCK_OBJ = new Object();

    /**
     * 存放不同的日期模板格式的sdf的Map
     */
    private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();


    /**
     * 返回一个ThreadLocal的sdf,每个线程只会new一次sdf
     *
     * @param pattern
     * @return
     */
    private static SimpleDateFormat getSdf(final String pattern) {
        ThreadLocal<SimpleDateFormat> tl = sdfMap.get(pattern);
        // 此处的双重判断和同步是为了防止sdfMap这个单例被多次put重复的sdf
        if (tl == null) {
            synchronized (LOCK_OBJ) {
                tl = sdfMap.get(pattern);
                if (tl == null) {
                    // 这里是关键,使用ThreadLocal<SimpleDateFormat>替代原来直接new SimpleDateFormat
                    tl = ThreadLocal.withInitial(() -> new SimpleDateFormat(pattern));
                    sdfMap.put(pattern, tl);
                }
            }
        }

        return tl.get();
    }

    public static Integer addIntDate(Integer intdate, int offset) {
        if (null == intdate) {
            return null;
        }
        Date date = DateUtils.addDays(parseIntDate(intdate), offset);
        return getIntDay(date);
    }

    public static Date parseIntDate(Integer intdate) {
        try {
            Date date = getSdf(DAY_FORMAT).parse(String.valueOf(intdate));
            return date;
        } catch (ParseException e) {
            log.error("format int date error");
        }
        return null;
    }

    public static Integer getIntDay(Date date) {
        return Integer.valueOf(getStringDay(date));
    }

    public static String getStringDay(Date date) {
        return getSdf(DAY_FORMAT).format(date);
    }

    public static Long getLongTime(String strDate, String format) {
        return parseDate(strDate, format).getTime();
    }

    public static Date parseDate(String strDate, String format) {
        try {
            return getSdf(format).parse(strDate);
        } catch (ParseException e) {
            log.error("err:", e);
        }
        return new Date();
    }

    public static String getStringDay(Long time, String format) {
        try {
            if (time == null || time == 0L) {
                return "";
            }
            return getSdf(format).format(new Date(time));
        } catch (Exception e) {
            log.error("err:", e);
        }
        return "";
    }

}
