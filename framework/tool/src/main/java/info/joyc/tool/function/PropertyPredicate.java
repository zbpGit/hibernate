package info.joyc.tool.function;

/**
 * info.joyc.tool.function.PropertyPredicate.java
 * ==============================================
 * Copy right 2015-2018 by http://www.rejoysoft.com
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : 把对象属性抽象出来，可做为参数传递
 * @since : 2018-01-25 11:13
 */
@FunctionalInterface
public interface PropertyPredicate<T, R> {
    /**
     * 把属性当参数传递到方法中，由方法去处理这个属性的值做什么
     * 传参使用: T -> T.getPropertyName() 或 T::getPropertyName
     * 接收参数方法内使用:
     *  参数：PropertyPredicate<DataVO, String> express
     *  PropertyPredicate<T, R> express = (T) -> T.getPropertyName();
     *  express.getProperty(T)
     * @param t 需要处理的对象
     * @return R 对象属性对应的类型
     */
    R getProperty(T t);
}
