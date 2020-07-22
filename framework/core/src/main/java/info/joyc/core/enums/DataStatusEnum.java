package info.joyc.core.enums;


/**
 * info.joyc.core.enums.DataStatusEnum.java
 * ==============================================
 * Copy right 2015-2017 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : 数据状态枚举类
 * @since : 2017-12-19 18:09
 */
public enum DataStatusEnum {

    /**
     * -2 临时保存
     */
    Temporary(-2, "临时保存"),

    /**
     * -1 已删除
     */
    Deleted(-1, "已删除"),

    /**
     * 0 已禁用
     */
    Disabled(0, "已禁用"),

    /**
     * 1 已启用
     */
    Enabled(1, "已启用"),

    /**
     * 2 已过时
     */
    Obsolete(2, "已过时"),

    /**
     * 3 每年结
     */
    Annually(3, "每年结"),

    /**
     * -99 未定义
     */
    Undefined(-99, "未定义");

    private int index;

    private String value;

    DataStatusEnum(int index, String value) {
        this.index = index;
        this.value = value;
    }

    /**
     * 根据类型的名称，返回类型的枚举实例
     *
     * @return 枚举实例
     */
    public static DataStatusEnum getEnum(int index) {
        for (DataStatusEnum dataStatusEnum : DataStatusEnum.values()) {
            if (index == dataStatusEnum.getIndex()) {
                return dataStatusEnum;
            }
        }
        return Undefined;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
