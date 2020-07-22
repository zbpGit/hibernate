package info.joyc.tool.lang;

/**
 * 拷贝接口
 *
 * @param <T> 拷贝目标类型
 */
public interface Copier<T> {
	/**
	 * 执行拷贝
	 * @return 拷贝的目标
	 */
	T copy();
}
