package info.joyc.core.service;

import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

/**
 * info.joyc.core.service.BaseService.java
 * ==============================================
 * Copy right 2015-2017 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : 项目通用Mapper服务类父类
 * @since : 2017-12-07 16:20
 */
public interface BaseService<T> {

    /**
     * 判断数据库操作是否成功
     *
     * @param result 数据库操作返回影响条数
     * @return boolean
     */
    boolean retBool(Integer result);

    /**
     * 单表查询
     *
     * @return 返回全量数据列表
     */
    List<T> getList();

    /**
     * 单表查询（按entity）
     *
     * @param entity 查询条件数据对象
     * @return 返回查询条件对应数据列表
     */
    List<T> getList(T entity);

    /**
     * 单表查询（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页的记录数
     * @return 返回全量数据列表
     */
    List<T> getList(int pageNum, int pageSize);

    /**
     * 单表分页查询（按entity分页）
     *
     * @param entity   查询条件数据对象
     * @param pageNum  页码
     * @param pageSize 每页的记录数
     * @return 返回查询页码对应数据列表
     */
    List<T> getList(T entity, int pageNum, int pageSize);

    /**
     * 单表分页查询（通用方法）
     *
     * @param entity   查询条件数据对象
     * @param example  MyBatis自定义条件查询对象
     * @param pageNum  页码
     * @param pageSize 每页的记录数
     * @return 返回查询页码对应数据列表
     */
    List<T> getList(T entity, Example example, int pageNum, int pageSize);

    /**
     * 单表分页查询（按example）
     *
     * @param example MyBatis自定义条件查询对象
     * @return 返回查询页码对应数据列表
     */
    List<T> getList(Example example);

    /**
     * 单表分页查询（按example分页）
     *
     * @param example  MyBatis自定义条件查询对象
     * @param pageNum  页码
     * @param pageSize 每页的记录数
     * @return 返回查询页码对应数据列表
     */
    List<T> getList(Example example, int pageNum, int pageSize);

    /**
     * 单表分页查询（按example分页）(同时支持分表的操作)
     *
     * @param example     MyBatis自定义条件查询对象
     * @param pageNum     页码
     * @param pageSize    每页的记录数
     * @param shardKeyMap 分片键与分片建属性
     * @return 返回查询页码对应数据列表
     */
    List<T> getList(Example example, int pageNum, int pageSize, Map<String, Object> shardKeyMap);

    /**
     * 根据对象的PrimaryKey集合查询数据对象
     *
     * @param ids PrimaryKey集合条件
     * @return 数据列表
     */
    List<T> getListByIds(Iterable<?> ids);

    /**
     * 根据对象的PrimaryKey集合查询数据对象(同时支持分表的操作)
     *
     * @param ids         PrimaryKey集合条件
     * @param shardKeyMap 分片键与分片建属性
     * @return 数据列表
     */
    List<T> getListByIds(Iterable<?> ids, Map<String, Object> shardKeyMap);

    /**
     * 若PrimaryKey不为null，根据对象的PrimaryKey查询；否则根据对象中字段属性值不为null查询
     * 只能有一个返回值，有多个结果将抛出异常
     *
     * @param entity 查询的数据对象
     * @return 返回查询的单一的完整对象
     */
    T find(T entity);

    /**
     * 若PrimaryKey不为null，根据对象的PrimaryKey查询；否则根据对象中字段属性值不为null查询
     * 可查询多个结果，随机返回其中一条
     *
     * @param entity 查询的数据对象
     * @return 返回查询的单一的完整对象
     */
    T findOne(T entity);

    /**
     * 插入新对象，并返回该对象
     *
     * @param entity 插入数据对象
     * @return 返回所插入的完整数据对象
     */
    T create(T entity);

    /**
     * 插入新对象，并返回该对象
     *
     * @param entity     插入数据对象
     * @param isStandard 是否标准表
     * @return
     */
    T create(T entity, boolean isStandard);

    /**
     * 插入新对象，并返回该对象(同时支持分表的操作)
     *
     * @param entity      插入数据对象
     * @param isStandard  是否标准表
     * @param shardKeyMap 分片键与分片建属性
     * @return 返回所插入的完整数据对象
     */
    T create(T entity, boolean isStandard, Map<String, Object> shardKeyMap);

    /**
     * 根据对象的PrimaryKey全量更新
     *
     * @param entity 修改的数据对象
     * @return 返回修改的完整对象
     */
    T modify(T entity);

    /**
     * 根据对象的PrimaryKey全量更新
     *
     * @param entity            修改的数据对象
     * @param isAllowRedundancy 是否允许冗余
     * @return 返回修改的完整对象
     */
    T modify(T entity, boolean isAllowRedundancy);

    /**
     * 根据对象的PrimaryKey全量更新
     *
     * @param entity            修改的数据对象
     * @param isAllowRedundancy 是否允许冗余
     * @param isStandard        是否标准表
     * @return
     */
    T modify(T entity, boolean isAllowRedundancy, boolean isStandard);

    /**
     * 根据对象的PrimaryKey全量更新(同时支持分表的操作)
     *
     * @param entity            修改的数据对象
     * @param isAllowRedundancy 是否允许冗余
     * @param isStandard        是否标准表
     * @param shardKeyMap       分片键与分片建属性
     * @return 返回修改的完整对象
     */
    T modify(T entity, boolean isAllowRedundancy, boolean isStandard, Map<String, Object> shardKeyMap);

    /**
     * 根据对象的PrimaryKey部分更新
     *
     * @param entity 修改的数据对象
     * @return 返回修改的完整对象
     */
    T update(T entity);

    /**
     * 根据对象的PrimaryKey部分更新
     *
     * @param entity            修改的数据对象
     * @param isAllowRedundancy 是否允许冗余
     * @return 返回修改的完整对象
     */
    T update(T entity, boolean isAllowRedundancy);

    /**
     * 根据对象的PrimaryKey部分更新
     *
     * @param entity            修改的数据对象
     * @param isAllowRedundancy 是否允许冗余
     * @param isStandard        是否标准表
     * @return
     */
    T update(T entity, boolean isAllowRedundancy, boolean isStandard);

    /**
     * 根据对象的PrimaryKey部分更新(同时支持分表的操作)
     *
     * @param entity            修改的数据对象
     * @param isAllowRedundancy 是否允许冗余
     * @param isStandard        是否标准表
     * @param shardKeyMap       分片键与分片建属性
     * @return 返回修改的完整对象
     */
    T update(T entity, boolean isAllowRedundancy, boolean isStandard, Map<String, Object> shardKeyMap);

    /**
     * 根据对象的example部分更新entity属性
     *
     * @param entity  修改属性所在的数据对象
     * @param example 修改的条件
     * @return
     */
    int update(T entity, Example example);

    /**
     * 根据对象的PrimaryKey集合更新属性
     *
     * @param entity 修改后的字段属性
     * @param ids    PrimaryKey集合条件
     * @return
     */
    int updateBatchByIds(T entity, Iterable<?> ids);

    /**
     * 根据对象的PrimaryKey集合更新属性(同时支持分表的操作)
     *
     * @param entity      修改后的字段属性
     * @param ids         PrimaryKey集合条件
     * @param shardKeyMap 分片键与分片建属性
     * @return
     */
    int updateBatchByIds(T entity, Iterable<?> ids, Map<String, Object> shardKeyMap);

    /**
     * 根据对象的PrimaryKey集合更新数据状态属性
     *
     * @param ids    PrimaryKey集合条件
     * @param source 修改前的数据状态
     * @param target 修改后的数据状态
     * @return
     */
    int updateStatusByIds(Iterable<?> ids, Integer source, Integer target);

    /**
     * 根据对象的PrimaryKey集合更新数据状态属性(同时支持分表的操作)
     *
     * @param ids         PrimaryKey集合条件
     * @param source      修改前的数据状态
     * @param target      修改后的数据状态
     * @param shardKeyMap 分片键与分片建属性
     * @return
     */
    int updateStatusByIds(Iterable<?> ids, Integer source, Integer target, Map<String, Object> shardKeyMap);

    /**
     * 根据自定义条件查询对象更新数据状态属性
     *
     * @param example 自定义条件查询对象
     * @param source  修改前的数据状态
     * @param target  修改后的数据状态
     * @return
     */
    int updateStatusByExample(Example example, Integer source, Integer target);

    /**
     * 根据自定义条件查询对象更新数据状态属性(同时支持分表的操作)
     *
     * @param example     自定义条件查询对象
     * @param source      修改前的数据状态（可以不传）
     * @param target      修改后的数据状态
     * @param shardKeyMap 分片键与分片建属性
     * @return
     */
    int updateStatusByExample(Example example, Integer source, Integer target, Map<String, Object> shardKeyMap);

    /**
     * 根据对象智能保存（标准表）
     *
     * @param entity 保存的数据对象
     * @return 返回保存成功的数据对象
     */
    T save(T entity);

    /**
     * 根据对象智能保存（标准可冗余表）
     *
     * @param entity            保存的数据对象
     * @param isAllowRedundancy 是否允许冗余
     * @return 返回保存成功的数据对象
     */
    T save(T entity, boolean isAllowRedundancy);

    /**
     * 根据对象智能保存
     *
     * @param entity            保存的数据对象
     * @param isAllowRedundancy 是否允许冗余
     * @param isStandard        是否标准表
     * @return
     */
    T save(T entity, boolean isAllowRedundancy, boolean isStandard);

    /**
     * 根据对象智能保存(同时支持分表的操作)
     *
     * @param entity            保存的数据对象
     * @param isAllowRedundancy 是否允许冗余
     * @param isStandard        是否标准表
     * @param shardKeyMap       分片键与分片建属性
     * @return 返回保存成功的数据对象
     */
    T save(T entity, boolean isAllowRedundancy, boolean isStandard, Map<String, Object> shardKeyMap);

    /**
     * 根据对象的PrimaryKey删除（标准表）
     *
     * @param entity 删除的数据对象
     * @return 返回操作的结果（成功的条数）
     */
    int delete(T entity);

    /**
     * 根据对象的PrimaryKey删除
     *
     * @param entity     删除的数据对象
     * @param isStandard 是否标准表
     * @return
     */
    int delete(T entity, boolean isStandard);

    /**
     * 根据对象的PrimaryKey删除(同时支持分表的操作) 默认标准表
     *
     * @param entity      删除的数据对象
     * @param shardKeyMap 分片键与分片建属性
     * @return 返回操作的结果（成功的条数）
     */
    int delete(T entity, Map<String, Object> shardKeyMap);

    /**
     * 根据对象的PrimaryKey删除(同时支持分表的操作)
     *
     * @param entity      删除的数据对象
     * @param isStandard  是否标准表
     * @param shardKeyMap 分片键与分片建属性
     * @return 返回操作的结果（成功的条数）
     */
    int delete(T entity, boolean isStandard, Map<String, Object> shardKeyMap);

    /**
     * 根据对象的PrimaryKey删除（标准表）
     *
     * @param example 删除的条件
     * @return 返回操作的结果（成功的条数）
     */
    int delete(Example example);

    /**
     * 根据对象的PrimaryKey删除
     *
     * @param example    删除的条件
     * @param isStandard 是否标准表
     * @return
     */
    int delete(Example example, boolean isStandard);

    /**
     * 根据对象的PrimaryKey删除(同时支持分表的操作)(标准表)
     *
     * @param example     删除的条件
     * @param shardKeyMap 分片键与分片建属性
     * @return 返回操作的结果（成功的条数）
     */
    int delete(Example example, Map<String, Object> shardKeyMap);

    /**
     * 根据对象的PrimaryKey删除(同时支持分表的操作)
     *
     * @param example     删除的条件
     * @param isStandard  是否标准表
     * @param shardKeyMap 分片键与分片建属性
     * @return 返回操作的结果（成功的条数）
     */
    int delete(Example example, boolean isStandard, Map<String, Object> shardKeyMap);

    /**
     * 根据对象的PrimaryKey集合删除数据对象
     *
     * @param ids PrimaryKey集合条件
     * @return
     */
    int deleteBatchByIds(Iterable<?> ids);


    /**
     * 根据对象的PrimaryKey集合删除数据对象
     *
     * @param ids        PrimaryKey集合条件
     * @param isStandard 是否标准表
     * @return
     */
    int deleteBatchByIds(Iterable<?> ids, boolean isStandard);

    /**
     * 根据对象的PrimaryKey集合删除数据对象(同时支持分表的操作)(标准表)
     *
     * @param ids         PrimaryKey集合条件
     * @param shardKeyMap 分片键与分片建属性
     * @return
     */
    int deleteBatchByIds(Iterable<?> ids, Map<String, Object> shardKeyMap);

    /**
     * 根据对象的PrimaryKey集合删除数据对象(同时支持分表的操作)
     *
     * @param ids         PrimaryKey集合条件
     * @param isStandard  是否标准表
     * @param shardKeyMap 分片键与分片建属性
     * @return
     */
    int deleteBatchByIds(Iterable<?> ids, boolean isStandard, Map<String, Object> shardKeyMap);
}
