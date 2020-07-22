package info.joyc.tool.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * info.joyc.util.lang.DateUtil.java
 * ==============================================
 * Copy right 2015-2017 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : 日期工具类
 * @since : 2017-12-31 15:49
 */
public class DateUtil {

    public static Date datePlus(Date time, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.DATE, day);
        return calendar.getTime();
    }

    public static Date dateMinus(Date time, int day) {
        day = -day;
        return datePlus(time, day);
    }

    public static boolean dateGreater(Date previousTime, Date latterTime) {
        return previousTime.getTime() >= latterTime.getTime();
    }

    public static boolean dateLess(Date previousTime, Date latterTime) {
        return previousTime.getTime() <= latterTime.getTime();
    }

    /**
     * 得到Calendar实例，增加或减少年度，并把时间返回
     *
     * @param time         传入时间
     * @param intervalYear 年度
     * @return 日期时间
     */
    public static Date getYearInterval(Date time, int intervalYear) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.YEAR, intervalYear);
        return calendar.getTime();
    }

    /**
     * 得到Calendar实例，增加或减少月份，并把时间返回
     *
     * @param time          传入时间
     * @param intervalMonth 间隔月份
     * @return 日期时间
     */
    public static Date getMonthInterval(Date time, int intervalMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.MONTH, intervalMonth);
        return calendar.getTime();
    }

    /**
     * 得到Calendar实例，增加或减少天数，并把时间返回
     *
     * @param time        传入时间
     * @param intervalDay 间隔天数
     * @return 日期时间
     */
    public static Date getDayInterval(Date time, int intervalDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.DATE, intervalDay);
        return calendar.getTime();
    }

    /**
     * 得到Calendar实例，增加或减少秒，并把时间返回
     *
     * @param time           传入时间
     * @param intervalSecond 间隔秒
     * @return 日期时间
     */
    public static Date getSecondInterval(Date time, int intervalSecond) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.SECOND, intervalSecond);
        return calendar.getTime();
    }

    /**
     * 时间按格式转化输出
     *
     * @param time          Date时间类型
     * @param formatPattern 格式化为"yyyyMM",则传formatPattern参数为201710；注意月与分的大小写
     * @return 字符串
     */
    public static String dateFormat(Date time, String formatPattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(formatPattern);
        return formatter.format(time);
    }

    /**
     * string转Date
     *
     * @param dateString    要转换的时间字符串
     * @param formatPattern 要转换的时间格式（默认格式：yyyy-MM-dd）
     * @return 日期时间
     */
    public static Date dateParse(String dateString, String formatPattern) {
        if (StringUtil.isBlank(formatPattern)) {
            formatPattern = "yyyy-MM-dd";
            if (dateString.length() > formatPattern.length()) {
                formatPattern = "yyyy-MM-dd HH:mm:ss";
            }
        }
        SimpleDateFormat formatter = new SimpleDateFormat(formatPattern);
        try {
            return formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 时间戳转换成日期格式字符串
     *
     * @param seconds 精确到毫秒的字符串
     * @param format
     * @return
     */
    public static String timeConversionDateString(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return null;
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds)));
    }

    /**
     * 时间戳转换成日期
     *
     * @param seconds 精确到毫秒的字符串
     * @param format
     * @return
     */
    public static Date timeConversionDate(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return null;
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String dateString = sdf.format(new Date(Long.valueOf(seconds)));
        return dateParse(dateString, format);
    }

    /**
     * 时间戳转日期
     *
     * @param ms
     * @return
     */
    public static Date transForDate(Long ms) {
        if (ms == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date temp = null;
        if (ms != null) {
            try {
                String str = sdf.format(ms);
                temp = sdf.parse(str);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return temp;
    }

    /**
     * 从Date中获取年度
     *
     * @param date Date时间类型
     * @return 年度
     */
    public static Integer getYear(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    /**
     * 从Date中获取月份
     *
     * @param date Date时间类型
     * @return 月份
     */
    public static Integer getMonth(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    /**
     * 从Date中获取日期
     *
     * @param date Date时间类型
     * @return 月份
     */
    public static Integer getDay(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 从Date中获取当天0点0分0秒
     *
     * @param date Date时间类型
     * @return 当天0点0分0秒
     */
    public static Date getDayStart(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 从Date中获取当天23点59分59秒
     *
     * @param date Date时间类型
     * @return 当天23点59分59秒
     */
    public static Date getDayEnd(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * 获取传入日期所在月的第一天
     *
     * @param date
     * @return
     */
    public static Date getFirstDayDateOfMonth(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final int last = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, last);
        return cal.getTime();
    }

    /**
     * 获取传入日期所在月的最后一天
     *
     * @param date
     * @return
     */
    public static Date getLastDayOfMonth(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final int last = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, last);
        return cal.getTime();
    }

    /**
     * 获取传入日期所在年的第一天
     *
     * @param date
     * @return
     */
    public static Date getFirstDayDateOfYear(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final int last = cal.getActualMinimum(Calendar.DAY_OF_YEAR);
        cal.set(Calendar.DAY_OF_YEAR, last);
        return cal.getTime();
    }

    /**
     * 获取传入日期所在年的最后一天
     *
     * @param date
     * @return
     */
    public static Date getLastDayOfYear(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final int last = cal.getActualMaximum(Calendar.DAY_OF_YEAR);
        cal.set(Calendar.DAY_OF_YEAR, last);
        return cal.getTime();
    }

    /**
     * 获取某年第一天日期
     * @param year 年份
     * @return Date
     */
    public static Date getFirstDayDateOfYear(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    /**
     * 获取某年最后一天日期
     * @param year 年份
     * @return Date
     */
    public static Date getLastDayOfYear(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTime();
    }

    /**
     * 生日转为年龄，计算法定年龄
     *
     * @param birthDay 生日，标准日期字符串
     * @return 年龄
     */
    //public static int ageOfNow(String birthDay) {
    //    return ageOfNow(parse(birthDay));
    //}

    /**
     * 生日转为年龄，计算法定年龄
     *
     * @param birthDay 生日
     * @return 年龄
     */
    public static int ageOfNow(Date birthDay) {
        return age(birthDay, new Date());
    }

    /**
     * 计算相对于dateToCompare的年龄，长用于计算指定生日在某年的年龄
     *
     * @param birthDay      生日
     * @param dateToCompare 需要对比的日期
     * @return 年龄
     */
    public static int age(Date birthDay, Date dateToCompare) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateToCompare);

        if (cal.before(birthDay)) {
            throw new IllegalArgumentException(StringUtil.format("Birthday is after date {}!", dateFormat(dateToCompare, "yyyy-MM-dd")));
        }

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        cal.setTime(birthDay);
        int age = year - cal.get(Calendar.YEAR);

        int monthBirth = cal.get(Calendar.MONTH);
        if (month == monthBirth) {
            int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
            if (dayOfMonth < dayOfMonthBirth) {
                // 如果生日在当月，但是未达到生日当天的日期，年龄减一
                age--;
            }
        } else if (month < monthBirth) {
            // 如果当前月份未达到生日的月份，年龄计算减一
            age--;
        }

        return age;
    }

    /**
     * 当前时间的时间戳
     *
     * @param isNano 是否为高精度时间
     * @return 时间
     */
    public static long current(boolean isNano) {
        return isNano ? System.nanoTime() : System.currentTimeMillis();
    }

    /**
     * 当前时间的时间戳（秒）
     *
     * @return 当前时间秒数
     * @since 4.0.0
     */
    public static long currentSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 是否闰年
     *
     * @param year 年
     * @return 是否闰年
     */
    public static boolean isLeapYear(int year) {
        return new GregorianCalendar().isLeapYear(year);
    }

    /**
     * 获取两个时间相差的月份
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return
     */
    public static Integer getDifMonth(Date startDate, Date endDate) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.setTime(startDate);
        end.setTime(endDate);
        int result = end.get(Calendar.MONTH) - start.get(Calendar.MONTH);
        int month = (end.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12;
        return Math.abs(month + result);
    }
}
