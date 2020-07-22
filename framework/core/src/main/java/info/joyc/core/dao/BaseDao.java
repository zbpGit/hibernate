package info.joyc.core.dao;

import info.joyc.core.dao.extra.CustomMapper;
import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * info.joyc.core.dao.BaseDao.java
 * ==============================================
 * Copy right 2015-2017 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : 项目通用Mapper父类
 * @since : 2017-12-01 10:46
 * <p>
 * 通用Mapper常用方法:
 * <p>
 * 等号的CRUD:
 * List<T> select(T record); 根据实体中的属性值进行查询，查询条件使用等号
 * T selectByPrimaryKey(Object key); 根据主键字段进行查询，方法参数必须包含完整的主键属性，查询条件使用等号
 * List<T> selectAll(); 查询全部结果，select(null)方法能达到同样的效果
 * T selectOne(T record); 根据实体中的属性进行查询，只能有一个返回值，有多个结果是抛出异常，查询条件使用等号
 * int selectCount(T record); 根据实体中的属性查询总数，查询条件使用等号
 * int insert(T record); 保存一个实体，null的属性也会保存，不会使用数据库默认值
 * int insertSelective(T record); 保存一个实体，null的属性不会保存，会使用数据库默认值
 * int updateByPrimaryKey(T record); 根据主键更新实体全部字段，null值会被更新
 * int updateByPrimaryKeySelective(T record); 根据主键更新属性不为null的值
 * int delete(T record); 根据实体属性作为条件进行删除，查询条件使用等号
 * int deleteByPrimaryKey(Object key); 根据主键字段进行删除，方法参数必须包含完整的主键属性
 * <p>
 * 条件的CRUD:
 * List<T> selectByExample(Object example); 根据Example条件进行查询
 * int selectCountByExample(Object example); 根据Example条件进行查询总数
 * int updateByExample(@Param("record") T record, @Param("example") Object example); 根据Example条件更新实体record包含的全部属性，null值会被更新
 * int updateByExampleSelective(@Param("record") T record, @Param("example") Object example); 根据Example条件更新实体record包含的不是null的属性值
 * int deleteByExample(Object example); 根据Example条件删除数据
 * <p>
 * Ids接口:
 * List<T> selectByIds(String ids); 根据主键字符串进行查询，类中只有存在一个带有@Id注解的字段
 * int deleteByIds(String ids); 根据主键字符串进行删除，类中只有存在一个带有@Id注解的字段
 * <p>
 * MySQL专用:
 * int insertList(List<T> recordList); 批量插入，支持批量插入的数据库可以使用，例如MySQL,H2等，另外该接口限制实体包含id属性并且必须为自增列
 * int insertUseGeneratedKeys(T record); 插入数据，限制为实体包含id属性并且必须为自增列，实体配置的主键策略无效
 * <p>
 * 自定义专用：
 * insertEntityList(List<T> recordList); 批量插入，全字段都需要直接赋值（包括id属性）
 */
public interface BaseDao<T> extends Mapper<T>, IdsMapper<T>, MySqlMapper<T>, CustomMapper<T> {
}
