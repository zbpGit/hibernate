package info.joyc.tool.function;

import java.math.BigDecimal;

/**
 * info.joyc.util.function.ToBigDecimalFunction.java
 * ==============================================
 * Copy right 2015-2018 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : Jalan
 * @version : v1.0.0
 * @desc : BigDecimal函数接口，用于CollectorUtil.summingBigDecimal(...)
 * @since : 2018/9/12 17:49
 */
@FunctionalInterface
public interface ToBigDecimalFunction<T> {
    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */
    BigDecimal applyAsBigDecimal(T value);
}