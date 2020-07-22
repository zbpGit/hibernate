package info.joyc.tool.function;

/**
 * info.joyc.util.function.ObjectPropertyPredicate.java
 * ==============================================
 * Copy right 2015-2018 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : Jalan
 * @version : v1.0.0
 * @desc : 把对象属性抽象出来，可做为参数传递
 * @since : 2018/2/7 21:07
 */
@FunctionalInterface
public interface ObjectPropertyPredicate<T> {
    /**
     * 把属性当参数传递到方法中，由方法去处理这个属性的值做什么。
     * 传参使用: o -> o.propertyName
     * 接收参数方法内使用:
     *  参数：ObjectPropertyPredicate<FreeReportDataVO> express
     *  Predicate<Object> press = (obj) -> express.getProperty(obj).toString().startsWith("测试");
     *  express.getProperty(obj)
     * @param o
     * @return
     */
    Object getProperty(T o);
}
