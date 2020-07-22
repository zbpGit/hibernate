package info.joyc.tool.provide;

import info.joyc.tool.util.StringUtil;

import java.text.DecimalFormat;

/**
 * info.joyc.util.provide.SerialUtil.java
 * ==============================================
 * Copy right 2015-2018 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : 序列生成工具类
 * @since : 2018-01-25 20:36
 */
public class SerialUtil {

    private SerialUtil() {
    }

    private static class SerialHolder {
        private final static SerialUtil instance = new SerialUtil();
    }

    /**
     * 取得SerialUtil的单例实现
     *
     * @return
     */
    public static SerialUtil getInstance() {
        return SerialHolder.instance;
    }

    /**
     * 往后生成下一个编号
     * 如果传入的sno=-1，则返回Null
     *
     * @param sno    传最大的编号，才能返回累加后的，不传则默认为0
     * @param digits 编号的位数，不传值默认为4位
     * @return 下一个编号
     */
    public synchronized String generateNext(String sno, Integer digits) {
        if ("-1".equals(sno)) {
            return null;
        }
        String id = "0";
        String prefix = "";
        if (StringUtil.isNotBlank(sno)) {
            int i = sno.length();
            while (i > 0) {
                int y = i--;
                if (!"9".equals(sno.substring(y - 1, y))) {
                    break;
                }
            }
            if (i != sno.length() && i != 0) {
                prefix = sno.substring(0, i - 1);
                sno = sno.substring(i - 1);
            }
            if (digits == null) {
                digits = sno.length();
            }
        } else {
            sno = id;
            if (digits == null) {
                digits = 4;
            }
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < digits - id.length(); i++) {
            sb.append("0");
        }
        sb.append(id);
        id = sb.toString();
        DecimalFormat df = new DecimalFormat(id);
        int number = Integer.parseInt(sno) + 1;
        id = df.format(number);
        if (StringUtil.isNotBlank(prefix)) {
            id = prefix + id;
        }
        return id;
    }

    /**
     * 往前生成下一个编号
     *
     * @param sno    传最大的编号，才能返回累加后的，不传则默认为0
     * @param digits 编号的位数，不传值默认为4位
     * @return 下一个编号
     */
    public synchronized String generatePrevious(String sno, Integer digits) {
        String id = "0";
        if (digits == null) {
            if (StringUtil.isEmpty(sno)) {
                digits = 4;
            } else {
                digits = sno.length();
            }
        }
        if (StringUtil.isEmpty(sno)) {
            sno = id;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < digits - id.length(); i++) {
            sb.append("0");
        }
        sb.append(id);
        id = sb.toString();
        DecimalFormat df = new DecimalFormat(id);
        int i = Integer.parseInt(sno);
        if (i > 1) {
            id = df.format(i - 1);
        } else {
            id = df.format(0);
        }
        return id;
    }

    //public static void main(String[] args) {
    //    System.out.println(getInstance().generateNext("999", null));
    //}
}
