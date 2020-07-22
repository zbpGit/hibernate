package info.joyc.tool.bean;

import info.joyc.tool.collection.CollectionUtil;
import info.joyc.tool.util.ArrayUtil;
import info.joyc.tool.util.ClassUtil;
import info.joyc.tool.util.ReflectUtil;
import info.joyc.tool.util.StringUtil;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Bean工具类
 * 
 * <p>
 * 把一个拥有对属性进行set和get方法的类，我们就可以称之为JavaBean。
 * </p>
 */
public class BeanUtil {

	/**
	 * 判断是否为Bean对象<br>
	 * 判定方法是是否存在只有一个参数的setXXX方法
	 * 
	 * @param clazz 待测试类
	 * @return 是否为Bean对象
	 */
	public static boolean isBean(Class<?> clazz) {
		if (ClassUtil.isNormalClass(clazz)) {
			final Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if (method.getParameterTypes().length == 1 && method.getName().startsWith("set")) {
					// 检测包含标准的setXXX方法即视为标准的JavaBean
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 查找类型转换器 {@link PropertyEditor}
	 * 
	 * @param type 需要转换的目标类型
	 * @return {@link PropertyEditor}
	 */
	public static PropertyEditor findEditor(Class<?> type) {
		return PropertyEditorManager.findEditor(type);
	}

	/**
	 * 判断Bean中是否有值为null的字段
	 * 
	 * @param bean Bean
	 * @return 是否有值为null的字段
	 */
	public static boolean hasNull(Object bean) {
		final Field[] fields = ClassUtil.getDeclaredFields(bean.getClass());

		Object fieldValue = null;
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				fieldValue = field.get(bean);
			} catch (Exception e) {

			}
			if (null == fieldValue) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获得字段值，通过反射直接获得字段值，并不调用getXXX方法<br>
	 * 对象同样支持Map类型，fieldNameOrIndex即为key
	 * 
	 * @param bean Bean对象
	 * @param fieldNameOrIndex 字段名或序号，序号支持负数
	 * @return 字段值
	 */
	public static Object getFieldValue(Object bean, String fieldNameOrIndex) {
		if (null == bean || null == fieldNameOrIndex) {
			return null;
		}

		if (bean instanceof Map) {
			return ((Map<?, ?>) bean).get(fieldNameOrIndex);
		} else if (bean instanceof Collection) {
			return CollectionUtil.get((Collection<?>) bean, Integer.parseInt(fieldNameOrIndex));
		} else if (ArrayUtil.isArray(bean)) {
			return ArrayUtil.get(bean, Integer.parseInt(fieldNameOrIndex));
		} else {// 普通Bean对象
			return ReflectUtil.getFieldValue(bean, fieldNameOrIndex);
		}
	}

	// --------------------------------------------------------------------------------------------- copyProperties
	/**
	 * 对象属性复制，忽略空值
	 * @param source 复制源对象
	 * @param target 复制目标对象
	 * @author hejw
	 * @since 2017-05-08 17:32:12
	 */
	public static void copyProperties(Object source, Object target){
		if (source != null && target != null) {
			org.springframework.beans.BeanUtils.copyProperties(source, target);
		}
	}

	/**
	 * 对象属性复制，忽略空值
	 * @param source 复制源对象
	 * @param target 复制目标对象
	 * @author hejw
	 * @since 2017-05-08 17:32:12
	 */
	public static void copyPropertiesIgnoreNull(Object source, Object target){
		if (source != null && target != null) {
			org.springframework.beans.BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
		}
	}

	/**
	 * 获得对象中属性值为空的属性名称
	 * @param source JAVA对象
	 * @author hejw
	 * @since 2017-05-08 17:32:12
	 * @return String[] 属性名称数组
	 */
	public static String[] getNullPropertyNames (Object source) {
		Set<String> emptyNames = new HashSet<String>();
		if (source != null) {
			BeanWrapper beanWrapper = new BeanWrapperImpl(source);
			PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();
			for(PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				Object propertyValue = beanWrapper.getPropertyValue(propertyDescriptor.getName());
				if (propertyValue == null) {
					emptyNames.add(propertyDescriptor.getName());
				}
			}
		}
		String[] result = new String[emptyNames.size()];
		return emptyNames.toArray(result);
	}


	/**
	 * 给定的Bean的类名是否匹配指定类名字符串<br>
	 * 如果isSimple为{@code false}，则只匹配类名而忽略包名，例如：cn.hutool.TestEntity只匹配TestEntity<br>
	 * 如果isSimple为{@code true}，则匹配包括包名的全类名，例如：cn.hutool.TestEntity匹配cn.hutool.TestEntity
	 * 
	 * @param bean Bean
	 * @param beanClassName Bean的类名
	 * @param isSimple 是否只匹配类名而忽略包名，true表示忽略包名
	 * @return 是否匹配
	 * @since 4.0.6
	 */
	public static boolean isMatchName(Object bean, String beanClassName, boolean isSimple) {
		return ClassUtil.getClassName(bean, isSimple).equals(isSimple ? StringUtil.upperFirst(beanClassName) : beanClassName);
	}

	/**
	 * 把Bean里面的String属性做trim操作。
	 * 
	 * 通常bean直接用来绑定页面的input，用户的输入可能首尾存在空格，通常保存数据库前需要把首尾空格去掉
	 * 
	 * @param <T> Bean类型
	 * @param bean Bean对象
	 * @param ignoreFields 不需要trim的Field名称列表（不区分大小写）
	 */
	public static <T> T trimStrFields(T bean, String... ignoreFields) {
		if (bean == null) {
			return bean;
		}

		final Field[] fields = ReflectUtil.getFields(bean.getClass());
		for (Field field : fields) {
			if (ignoreFields != null && ArrayUtil.containsIgnoreCase(ignoreFields, field.getName())) {
				// 不处理忽略的Fields
				continue;
			}
			if (String.class.equals(field.getType())) {
				// 只有String的Field才处理
				final String val = (String) ReflectUtil.getFieldValue(bean, field);
				if (null != val) {
					final String trimVal = StringUtil.trim(val);
					if (false == val.equals(trimVal)) {
						// Field Value不为null，且首尾有空格才处理
						ReflectUtil.setFieldValue(bean, field, trimVal);
					}
				}
			}
		}

		return bean;
	}

	/**
	 * 判断Bean是否为空对象，空对象表示本身为<code>null</code>或者所有属性都为<code>null</code>
	 *
	 * @param bean Bean对象
	 * @return 是否为空，<code>true</code> - 空 / <code>false</code> - 非空
	 * @since 4.1.10
	 */
	public static boolean isEmpty(Object bean) {
		if (null != bean) {
			for (Field field : ReflectUtil.getFields(bean.getClass())) {
				if (null != ReflectUtil.getFieldValue(bean, field)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 判断Bean是否包含值为<code>null</code>的属性<br>
	 * 对象本身为<code>null</code>也返回true
	 *
	 * @param bean Bean对象
	 * @return 是否包含值为<code>null</code>的属性，<code>true</code> - 包含 / <code>false</code> - 不包含
	 * @since 4.1.10
	 */
	public static boolean hasNullField(Object bean) {
		if (null == bean) {
			return true;
		}
		for (Field field : ReflectUtil.getFields(bean.getClass())) {
			if (null == ReflectUtil.getFieldValue(bean, field)) {
				return true;
			}
		}
		return false;
	}
}
