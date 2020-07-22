package info.joyc.core.service.impl;

import com.github.pagehelper.PageHelper;
import info.joyc.core.dao.BaseDao;
import info.joyc.core.enums.DataStatusEnum;
import info.joyc.core.service.BaseService;
import info.joyc.tool.lang.Assert;
import info.joyc.tool.util.IdUtil;
import info.joyc.tool.util.StringUtil;
import info.joyc.tool.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.MapperException;
import tk.mybatis.mapper.entity.EntityColumn;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.mapperhelper.EntityHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.util.*;

/**
 * info.joyc.core.service.impl.BaseServiceImpl.java
 * ==============================================
 * Copy right 2015-2017 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 * <p>
 * 系统设计标准（对应的冗余表与日志表互斥，不能同时支持两者，会发生不可预知错误）
 * 标准表：存在主键、创建时间、修改时间、数据状态（四要素字段）
 * 冗余表：利用数据状态字段在本表中做日志记录（数据修改即把原数据的数据状态修改为已过时，并新建一条数据状态已启用的数据；删除即把原数据的数据状态修改为已过时。）
 * 日志表：数据库存在本表相对应的日志表，并相应增加四字段（操作主键、操作类型、操作时间、操作人）
 * <p>
 * 新增：不区分是否冗余表，只针对是否标准表而插入。若有日志表，则新增insert记录。
 * 修改：若为标准冗余表，修改的同时新增一条记录；若为标准表，则直接修改当前记录，自动更新修改时间；否则按一般表修改处理。若有日志表，则新增modify、update记录。
 * 删除：若为标准表，则为逻辑删除（将数据状态修改为已过时）；否则，为物理删除（直接删除数据）。若有日志表，则新增delete记录。
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : 项目通用Mapper服务实现类父类
 * @since : 2017-12-07 16:14
 */
@SuppressWarnings("unchecked")
public abstract class BaseServiceImpl<T> implements BaseService<T> {

    private static final Logger log = LoggerFactory.getLogger(BaseServiceImpl.class);
    /**
     * 定义约定标准的数据库表结构字段：主键（ID）
     */
    private final static String ID = "id";
    /**
     * 定义约定标准的数据库表结构字段：创建时间（CREATE_TIME）
     */
    private final static String CREATE_TIME = "createTime";
    /**
     * 定义约定标准的数据库表结构字段：修改时间（MODIFY_TIME）
     */
    private final static String MODIFY_TIME = "modifyTime";
    /**
     * 定义约定标准的数据库表结构字段：数据状态（STATUS）
     */
    private final static String STATUS = "status";
    /**
     * 定义约定标准的数据库日志表命名规则：日志表后缀（LOG_TABLE_SUFFIX）
     */
    private final static String LOG_TABLE_SUFFIX = "log";
    /**
     * 定义约定标准的数据库日志表结构字段：日志标识（LOG_ID）
     */
    private final static String LOG_ID = "logId";
    /**
     * 定义约定标准的数据库日志表结构字段：操作类型（LOG_OPERATING_TYPE）
     */
    private final static String LOG_OPERATING_TYPE = "operatingType";
    /**
     * 定义约定标准的数据库日志表结构字段：操作时间（LOG_OPERATING_TIME）
     */
    private final static String LOG_OPERATING_TIME = "operatingTime";
    /**
     * 定义约定标准的数据库日志表结构字段：操作人（LOG_OPERATING_PERSON）
     */
    private final static String LOG_OPERATING_PERSON = "operatingPerson";
    /**
     * 定义日志操作方法：新增（LOG_METHOD_CREATE）
     */
    private final static String LOG_METHOD_CREATE = "insert";
    /**
     * 定义日志操作类型：新增（LOG_OPERATING_TYPE_CREATE）
     */
    private final static String LOG_OPERATING_TYPE_CREATE = "create";
    /**
     * 定义日志操作类型：修改（全量）（LOG_OPERATING_TYPE_MODIFY）
     */
    private final static String LOG_OPERATING_TYPE_MODIFY = "modify";
    /**
     * 定义日志操作类型：修改（部分）（LOG_OPERATING_TYPE_UPDATE）
     */
    private final static String LOG_OPERATING_TYPE_UPDATE = "update";
    /**
     * 定义日志操作类型：删除（LOG_OPERATING_TYPE_DELETE）
     */
    private final static String LOG_OPERATING_TYPE_DELETE = "delete";
    private static final ThreadLocal<String> logIds = new ThreadLocal<>();
    /**
     * 对应的泛型类
     */
    private final Class<T> clazz;

    /**
     * 泛型对应的日志类
     */
    private Class<?> logClazz;

    @Autowired
    private BaseDao<T> baseDao;

    @Autowired
    private ApplicationContext applicationContext;

    public BaseServiceImpl() {
        // 得到泛型化超类（返回值为参数化类型）
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.clazz = (Class<T>) type.getActualTypeArguments()[0];
        try {
            // 尝试获取对应的日志类
            this.logClazz = Class.forName(this.clazz.getName() + LOG_TABLE_SUFFIX);
        } catch (ClassNotFoundException e) {
            // 不处理
        }
    }

    public static String getLogIds() {
        return logIds.get();
    }

    public static void setLogIds(String logId) {
        if (StringUtil.isNotBlank(logIds.get()) && StringUtil.isNotBlank(logId)) {
            logId = logIds.get().concat(",").concat(logId);
        }
        logIds.set(logId);
    }

    public static void removeLogIds() {
        logIds.remove();
    }

    /**
     * 通过泛型类型获取新对象
     *
     * @return 新对象
     */
    private T getNewEntity() {
        try {
            return this.clazz.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(this.clazz.getName() + "类通过clazz创建新对象时发生异常");
        }
    }

    /**
     * <p>
     * 判断数据库操作是否成功
     * </p>
     *
     * @param result 数据库操作返回影响条数
     * @return boolean
     */
    @Override
    public boolean retBool(Integer result) {
        return null != result && result >= 1;
    }

    @Override
    public List<T> getList() {
        return this.getList((T) null);
    }

    @Override
    public List<T> getList(T entity) {
        return this.getList(entity, 0, 0);
    }

    @Override
    public List<T> getList(int pageNum, int pageSize) {
        return this.getList(null, null, pageNum, pageSize);
    }

    @Override
    public List<T> getList(T entity, int pageNum, int pageSize) {
        return this.getList(entity, null, pageNum, pageSize);
    }

    @Override
    public List<T> getList(Example example) {
        return this.getList(example, 0, 0);
    }

    @Override
    public List<T> getList(Example example, int pageNum, int pageSize) {
        return this.getList(example, pageNum, pageSize, null);
    }

    @Override
    public List<T> getList(Example example, int pageNum, int pageSize, Map<String, Object> shardKeyMap) {
        Assert.isTrue(example != null, "MyBatis自定义条件查询对象不能为空");
        //分片建属性校验
        if (shardKeyMap != null && shardKeyMap.size() > 0) {
            for (String shardKey : shardKeyMap.keySet()) {
                Assert.isTrue(shardKeyMap.get(shardKey) != null, "当使用了分片查询时，分片属性值不能为空");
                if (shardKeyMap.get(shardKey) instanceof List) {
                    example.and().andIn(shardKey, (List) shardKeyMap.get(shardKey));
                } else {
                    example.and().andEqualTo(shardKey, shardKeyMap.get(shardKey));
                }
            }
        }
        return PageHelper.startPage(pageNum, pageSize).doSelectPage(() -> baseDao.selectByExample(example));
    }

    @Override
    public List<T> getList(T entity, Example example, int pageNum, int pageSize) {
        List<T> entityList;
        if (null == example) {
            if (null == entity) {
                entityList = PageHelper.startPage(pageNum, pageSize).doSelectPage(() -> baseDao.selectAll());
            } else {
                entityList = PageHelper.startPage(pageNum, pageSize).doSelectPage(() -> baseDao.select(entity));
            }
        } else {
            entityList = PageHelper.startPage(pageNum, pageSize).doSelectPage(() -> baseDao.selectByExample(example));
        }
        return entityList;
    }

    @Override
    public List<T> getListByIds(Iterable<?> ids) {
        return this.getListByIds(ids, null);
    }

    @Override
    public List<T> getListByIds(Iterable<?> ids, Map<String, Object> shardKeyMap) {
        Assert.notNull(ids, "批量查询的id条件集合不能为null");
        Example example = new Example(this.clazz);
        Set<EntityColumn> columnList = EntityHelper.getPKColumns(this.clazz);
        if (columnList.size() == 1) {
            EntityColumn column = columnList.iterator().next();
            example.and().andIn(column.getProperty(), ids);
            //分片建属性校验
            if (shardKeyMap != null && shardKeyMap.size() > 0) {
                for (String shardKey : shardKeyMap.keySet()) {
                    Assert.isTrue(shardKeyMap.get(shardKey) != null, "当使用了分片查询时，分片属性值不能为空");
                    if (shardKeyMap.get(shardKey) instanceof List) {
                        example.and().andIn(shardKey, (List) shardKeyMap.get(shardKey));
                    } else {
                        example.and().andEqualTo(shardKey, shardKeyMap.get(shardKey));
                    }
                }
            }
            return this.getList(example);
        } else {
            throw new MapperException("继承 getListByIds 方法的实体类[" + this.clazz.getCanonicalName() + "]中必须只有一个带有 @Id 注解的字段");
        }
    }

    @Override
    public T find(T entity) {
        Set<EntityColumn> pkColumns = EntityHelper.getPKColumns(entity.getClass());
        List<Object> objectList = new ArrayList<>();
        for (EntityColumn pkColumn : pkColumns) {
            try {
                Field pkField = entity.getClass().getDeclaredField(pkColumn.getProperty());
                pkField.setAccessible(true);
                objectList.add(pkField.get(entity));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(entity.getClass().getName() + "类所对应的表主键字段注解错误");
            }
        }
        if (objectList.size() > 0 && !objectList.contains(null)) {
            return baseDao.selectByPrimaryKey(entity);
        }
        return baseDao.selectOne(entity);
    }

    @Override
    public T findOne(T entity) {
        Set<EntityColumn> pkColumns = EntityHelper.getPKColumns(entity.getClass());
        List<Object> objectList = new ArrayList<>();
        for (EntityColumn pkColumn : pkColumns) {
            try {
                Field pkField = entity.getClass().getDeclaredField(pkColumn.getProperty());
                pkField.setAccessible(true);
                objectList.add(pkField.get(entity));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(entity.getClass().getName() + "类所对应的表主键字段注解错误");
            }
        }
        if (objectList.size() > 0 && !objectList.contains(null)) {
            return baseDao.selectByPrimaryKey(entity);
        } else {
            List<T> entityList = getList(entity, 1, 1);
            if (entityList != null && entityList.size() > 0) {
                return entityList.get(0);
            } else {
                return null;
            }
        }
    }

    /**
     * 为标准冗余表时，修改数据的步骤为：
     * 1. 将原数据状态置为过期
     * 2. 新增一条启用状态数据
     * 3. 此时该表不应该存在对应的日志表，否则将在日志表记录一条操作类型为insert的日志
     *
     * @param entity 数据对象
     * @return 新对象
     */
    private T createForUpdate(T entity, Map<String, Object> shardKeyMap) {
        try {
            Field statusField = entity.getClass().getDeclaredField(STATUS);
            statusField.setAccessible(true);
            if (DataStatusEnum.Obsolete.getIndex() == (Integer) statusField.get(entity)) {
                statusField.set(entity, DataStatusEnum.Enabled.getIndex());
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(entity.getClass().getName() + "类所对应的表非约定标准表");
        }
        return this.create(entity, true, shardKeyMap);
    }

    @Override
    public T create(T entity) {
        return this.create(entity, true);
    }

    @Override
    public T create(T entity, boolean isStandard) {
        return this.create(entity, isStandard, null);
    }

    @Override
    public T create(T entity, boolean isStandard, Map<String, Object> shardKeyMap) {
        if (isStandard) {
            Date now = new Date();
            try {
                Field idField = entity.getClass().getDeclaredField(ID);
                idField.setAccessible(true);
                idField.set(entity, null);
                Field createTimeField = entity.getClass().getDeclaredField(CREATE_TIME);
                createTimeField.setAccessible(true);
                createTimeField.set(entity, StringUtil.equals("class java.util.Date", TypeUtil.getType(createTimeField).toString()) ? now : LocalDateTime.now());
                Field modifyTimeField = entity.getClass().getDeclaredField(MODIFY_TIME);
                modifyTimeField.setAccessible(true);
                modifyTimeField.set(entity, StringUtil.equals("class java.util.Date", TypeUtil.getType(modifyTimeField).toString()) ? now : LocalDateTime.now());
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(entity.getClass().getName() + "类所对应的表非约定标准表");
            }
        }
        if (shardKeyMap != null && shardKeyMap.size() > 0) {
            //分片建属性校验
            for (String shardKey : shardKeyMap.keySet()) {
                Assert.isTrue(Objects.nonNull(shardKeyMap.get(shardKey)), this.getClass().getName() + "类所对应的分表属性值不能为空");
                try {
                    Field shard = entity.getClass().getDeclaredField(shardKey);
                    shard.setAccessible(true);
                    if (!Objects.equals(shard.get(entity), shardKeyMap.get(shardKey))) {
                        shard.set(entity, shardKeyMap.get(shardKey));
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(entity.getClass().getName() + "类所对应的分片键属性错误");
                }
            }
            int result = baseDao.insert(entity);
            this.saveLogVo(entity, LOG_OPERATING_TYPE_CREATE);
            Assert.state(result != 0, "新增数据失败");
            Example exampleShard = new Example(this.clazz);
            try {
                Field shard = entity.getClass().getDeclaredField(ID);
                shard.setAccessible(true);
                exampleShard.and().andEqualTo(ID, shard.get(entity));
                for (String shardKey : shardKeyMap.keySet()) {
                    exampleShard.and().andEqualTo(shardKey, shardKeyMap.get(shardKey));
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(entity.getClass().getName() + "类所对应的表非约定标准表");
            }
            return baseDao.selectByExample(exampleShard).stream().findFirst().get();
        } else {
            int result = baseDao.insert(entity);
            this.saveLogVo(entity, LOG_OPERATING_TYPE_CREATE);
            Assert.state(result != 0, "新增数据失败");
            return baseDao.selectByPrimaryKey(entity);
        }
    }

    @Override
    public T modify(T entity) {
        return this.modify(entity, false);
    }

    @Override
    public T modify(T entity, boolean isAllowRedundancy) {
        return this.modify(entity, isAllowRedundancy, true);
    }

    @Override
    public T modify(T entity, boolean isAllowRedundancy, boolean isStandard) {
        return this.modify(entity, isAllowRedundancy, isStandard, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public T modify(T entity, boolean isAllowRedundancy, boolean isStandard, Map<String, Object> shardKeyMap) {
        int result;
        //分表之后的修改操作
        if (shardKeyMap != null && shardKeyMap.size() > 0) {
            T before;
            boolean check = false;
            Map<String, Object> shardMap = new HashMap<>(6);//修改之前的分片键对应的值
            //分片建校验，ID不为空时，需要校验分片属性是否改动过，如过改动过需要删除之前的，然后再次添加到对应的节点表中
            try {
                Field idField = entity.getClass().getDeclaredField(ID);
                idField.setAccessible(true);
                Example example = new Example(entity.getClass());
                example.and().andEqualTo(ID, idField.get(entity));
                for (String shardKey : shardKeyMap.keySet()) {
                    Assert.isTrue(Objects.nonNull(shardKeyMap.get(shardKey)), entity.getClass().getName() + "类所对应的分表属性值不能为空");
                    Field shard = entity.getClass().getDeclaredField(shardKey);
                    shard.setAccessible(true);
                    example.and().andEqualTo(shardKey, shard.get(entity));
                    Assert.isTrue(Objects.nonNull(shard.get(entity)), entity.getClass().getName() + "类分表属性赋值不能为空");
                    if (!Objects.equals(shard.get(entity), shardKeyMap.get(shardKey))) {
                        check = true;
                    }
                }
                List<T> exampleList = baseDao.selectByExample(example);
                Assert.isTrue(exampleList != null && exampleList.size() == 1, entity.getClass().getName() + "类修改时，修改前后的分片属性不一致，无法获取之前的数据");
                before = exampleList.stream().findFirst().get();
                for (String shardKey : shardKeyMap.keySet()) {
                    try {
                        Field shard = before.getClass().getDeclaredField(shardKey);
                        shard.setAccessible(true);
                        shardMap.put(shardKey, shard.get(before));
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        Assert.state(!isAllowRedundancy, before.getClass().getName() + "类所对应的表分片属性错误");
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e.getMessage());
            }
            Example example = this.getExampleAndUpdateEntity(entity, isAllowRedundancy, shardMap);
            if (isStandard && isAllowRedundancy) {
                T newEntity = this.getNewEntityAndEmptied(entity);
                result = baseDao.updateByExampleSelective(newEntity, example);
                Assert.state(result != 0, "修改失败，请关闭页面后，重新尝试");
                entity = this.createForUpdate(entity, shardKeyMap);
            } else {
                Assert.state(!isAllowRedundancy, entity.getClass().getName() + "类所对应的表非标准表不支持数据冗余模式");
                if (check) {
                    //分片属性变动过,先删除之前节点表的数据，再新增到对应的节点表数据中
                    result = this.delete(before, false, shardMap);
                    Assert.state(result != 0, "修改失败，请关闭页面后，重新尝试");
                    entity = this.create(entity, false, shardKeyMap);
                } else {
                    //分片属性未变动过
                    Example exampleShard = new Example(this.clazz);
                    try {
                        Field idField = entity.getClass().getDeclaredField(ID);
                        idField.setAccessible(true);
                        exampleShard.and().andEqualTo(ID, idField.get(entity));
                        for (String shardKey : shardKeyMap.keySet()) {
                            example.and().andEqualTo(shardKey, shardKeyMap.get(shardKey));
                            exampleShard.and().andEqualTo(shardKey, shardKeyMap.get(shardKey));
                        }
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        throw new RuntimeException(entity.getClass().getName() + "类所对应的表非约定标准表");
                    }
                    result = baseDao.updateByExample(entity, example);
                    Assert.state(result != 0, "修改失败，请关闭页面后，重新尝试");
                    entity = baseDao.selectByExample(exampleShard).stream().findFirst().get();
                    this.saveLogVo(entity, LOG_OPERATING_TYPE_MODIFY);
                }
            }
        } else {
            //正常单表修改
            if (isStandard) {
                Example example = this.getExampleAndUpdateEntity(entity, isAllowRedundancy, null);
                if (isAllowRedundancy) {
                    T newEntity = this.getNewEntityAndEmptied(entity);
                    result = baseDao.updateByExampleSelective(newEntity, example);
                    Assert.state(result != 0, "修改失败，请关闭页面后，重新尝试");
                    entity = this.createForUpdate(entity, null);
                } else {
                    result = baseDao.updateByExample(entity, example);
                    Assert.state(result != 0, "修改失败，请关闭页面后，重新尝试");
                    entity = baseDao.selectByPrimaryKey(entity);
                    this.saveLogVo(entity, LOG_OPERATING_TYPE_MODIFY);
                }
            } else {
                Assert.state(!isAllowRedundancy, entity.getClass().getName() + "类所对应的表非标准表不支持数据冗余模式");
                result = baseDao.updateByPrimaryKey(entity);
                Assert.state(result != 0, "修改失败，请关闭页面后，重新尝试");
                entity = baseDao.selectByPrimaryKey(entity);
                this.saveLogVo(entity, LOG_OPERATING_TYPE_MODIFY);
            }
        }
        return entity;
    }

    @Override
    public T update(T entity) {
        return this.update(entity, false);
    }

    @Override
    public T update(T entity, boolean isAllowRedundancy) {
        return this.update(entity, isAllowRedundancy, true);
    }

    @Override
    public T update(T entity, boolean isAllowRedundancy, boolean isStandard) {
        return this.update(entity, isAllowRedundancy, isStandard, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public T update(T entity, boolean isAllowRedundancy, boolean isStandard, Map<String, Object> shardKeyMap) {
        int result;
        if (shardKeyMap != null && shardKeyMap.size() > 0) {
            T before;
            boolean check = false;
            Map<String, Object> shardMap = new HashMap<>(6);//修改之前的分片键对应的值
            //分片建校验，ID不为空时，需要校验分片属性是否改动过，如过改动过需要删除之前的，然后再次添加到对应的节点表中
            try {
                Field idField = entity.getClass().getDeclaredField(ID);
                idField.setAccessible(true);
                Example example = new Example(entity.getClass());
                example.and().andEqualTo(ID, idField.get(entity));
                for (String shardKey : shardKeyMap.keySet()) {
                    Assert.isTrue(Objects.nonNull(shardKeyMap.get(shardKey)), entity.getClass().getName() + "类所对应的分表属性值不能为空");
                    Field shard = entity.getClass().getDeclaredField(shardKey);
                    shard.setAccessible(true);
                    example.and().andEqualTo(shardKey, shard.get(entity));
                    Assert.isTrue(Objects.nonNull(shard.get(entity)), entity.getClass().getName() + "类分表属性赋值不能为空");
                    if (!Objects.equals(shard.get(entity), shardKeyMap.get(shardKey))) {
                        check = true;
                    }
                }
                List<T> exampleList = baseDao.selectByExample(example);
                Assert.isTrue(exampleList != null && exampleList.size() == 1, entity.getClass().getName() + "类修改时，修改前后的分片属性不一致，无法获取之前的数据");
                before = exampleList.stream().findFirst().get();
                for (String shardKey : shardKeyMap.keySet()) {
                    try {
                        Field shard = before.getClass().getDeclaredField(shardKey);
                        shard.setAccessible(true);
                        shardMap.put(shardKey, shard.get(before));
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        Assert.state(!isAllowRedundancy, before.getClass().getName() + "类所对应的表分片属性错误");
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e.getMessage());
            }
            Example example = this.getExampleAndUpdateEntity(entity, isAllowRedundancy, shardMap);
            if (isStandard && isAllowRedundancy) {
                T newEntity = this.getNewEntityAndEmptied(entity);
                result = baseDao.updateByExampleSelective(newEntity, example);
                Assert.state(result != 0, "修改失败，请关闭页面后，重新尝试");
                entity = this.createForUpdate(this.cloneAndOverwrite(entity, shardMap), shardKeyMap);
            } else {
                Assert.state(!isAllowRedundancy, entity.getClass().getName() + "类所对应的表非标准表不支持数据冗余模式");
                if (check) {
                    //分片属性变动过,先删除之前节点表的数据，再新增到对应的节点表数据中
                    result = this.delete(before, false, shardMap);
                    Assert.state(result != 0, "修改失败，请关闭页面后，重新尝试");
                    entity = this.create(entity, false, shardKeyMap);
                } else {
                    //分片属性未变动过
                    Example exampleShard = new Example(this.clazz);
                    try {
                        Field idField = entity.getClass().getDeclaredField(ID);
                        idField.setAccessible(true);
                        exampleShard.and().andEqualTo(ID, idField.get(entity));
                        for (String shardKey : shardKeyMap.keySet()) {
                            example.and().andEqualTo(shardKey, shardKeyMap.get(shardKey));
                            exampleShard.and().andEqualTo(shardKey, shardKeyMap.get(shardKey));
                        }
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        throw new RuntimeException(entity.getClass().getName() + "类所对应的表非约定标准表");
                    }
                    result = baseDao.updateByExampleSelective(entity, example);
                    Assert.state(result != 0, "修改失败，请关闭页面后，重新尝试");
                    entity = baseDao.selectByExample(exampleShard).stream().findFirst().get();
                    this.saveLogVo(entity, LOG_OPERATING_TYPE_MODIFY);
                }
            }
        } else {
            if (isStandard) {
                Example example = this.getExampleAndUpdateEntity(entity, isAllowRedundancy, null);
                if (isAllowRedundancy) {
                    T newEntity = this.getNewEntityAndEmptied(entity);
                    result = baseDao.updateByExampleSelective(newEntity, example);
                    Assert.state(result != 0, "更新失败，请关闭页面后，重新尝试");
                    entity = this.createForUpdate(this.cloneAndOverwrite(entity, null), null);
                } else {
                    result = baseDao.updateByExampleSelective(entity, example);
                    Assert.state(result != 0, "更新失败，请关闭页面后，重新尝试");
                    entity = baseDao.selectByPrimaryKey(entity);
                    this.saveLogVo(entity, LOG_OPERATING_TYPE_UPDATE);
                }
            } else {
                Assert.state(!isAllowRedundancy, entity.getClass().getName() + "类所对应的表非标准表不支持数据冗余模式");
                result = baseDao.updateByPrimaryKeySelective(entity);
                Assert.state(result != 0, "更新失败，请关闭页面后，重新尝试");
                entity = baseDao.selectByPrimaryKey(entity);
                this.saveLogVo(entity, LOG_OPERATING_TYPE_UPDATE);
            }
        }
        return entity;
    }

    private Example getExampleAndUpdateEntity(T entity, boolean isAllowRedundancy, Map<String, Object> shardKeyMap) {
        Example example = new Example(entity.getClass());
        Example.Criteria criteria = example.createCriteria();
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (StringUtil.equals(field.getName(), ID) || StringUtil.equals(field.getName(), CREATE_TIME) || StringUtil.equals(field.getName(), MODIFY_TIME)) {
                    criteria.andEqualTo(field.getName(), field.get(entity));
                    if (StringUtil.equals(field.getName(), MODIFY_TIME)) {
                        field.set(entity, StringUtil.equals("class java.util.Date", TypeUtil.getType(field).toString()) ? new Date() : LocalDateTime.now());
                    }
                } else if (isAllowRedundancy && StringUtil.equals(field.getName(), STATUS) && DataStatusEnum.Enabled.getIndex() == (Integer) field.get(entity)) {
                    field.set(entity, DataStatusEnum.Obsolete.getIndex());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(entity.getClass().getName() + "类根据对象抽取条件及更新本对象时发生异常");
            }
            //分片键查询赋值
            if (shardKeyMap != null && shardKeyMap.containsKey(field.getName())) {
                if (shardKeyMap.get(field.getName()) instanceof List) {
                    criteria.andIn(field.getName(), (List) shardKeyMap.get(field.getName()));
                } else {
                    criteria.andEqualTo(field.getName(), shardKeyMap.get(field.getName()));
                }
            }
        }
        return example;
    }

    /**
     * 当标准冗余表进行修改时，根据传入对象获取新对象，并清空除主键、修改时间、数据状态三要素外其它元素
     *
     * @param entity           数据对象
     * @param ignoreProperties 忽略字段
     * @return 新对象 除忽略字段属性，其它均清空
     */
    private T getNewEntityAndEmptied(T entity, String... ignoreProperties) {
        Assert.notNull(entity, "传入对象不能为空");
        try {
            T newEntity = this.getNewEntity();
            BeanUtils.copyProperties(entity, newEntity);
            String[] defaultIgnoreProperties = {ID, MODIFY_TIME, STATUS};
            List<String> ignoreList = (null != ignoreProperties && ignoreProperties.length != 0) ? Arrays.asList(ignoreProperties) : Arrays.asList(defaultIgnoreProperties);
            Field[] fields = newEntity.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (!ignoreList.contains(field.getName())) {
                    field.set(newEntity, null);
                }
            }
            return newEntity;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(entity.getClass().getName() + "类进行克隆并清空属性值时发生异常");
        }
    }

    /**
     * 当标准冗余表进行修改时,根据传入的对象查找到原对象属性，将此次修改的属性覆盖原对象属性，交由create方法创建
     *
     * @param entity 数据对象
     * @return 新对象
     */
    private T cloneAndOverwrite(T entity, Map<String, Object> shardKeyMap) {
        Example example = new Example(entity.getClass());
        try {
            Field idField = entity.getClass().getDeclaredField(ID);
            idField.setAccessible(true);
            example.and().andEqualTo(ID, idField.get(entity));
            if (shardKeyMap != null && shardKeyMap.size() > 0) {
                for (String shardKey : shardKeyMap.keySet()) {
                    if (shardKeyMap.get(shardKey) instanceof List) {
                        example.and().andIn(shardKey, (List) shardKeyMap.get(shardKey));
                    } else {
                        example.and().andEqualTo(shardKey, shardKeyMap.get(shardKey));
                    }
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(entity.getClass().getName() + "类所对应的表非约定标准表");
        }
        List<T> exampleList = baseDao.selectByExample(example);
        Assert.isTrue(exampleList != null && exampleList.size() == 1, entity.getClass().getName() + "类修改时，修改前后的分片属性不一致，无法获取之前的数据");
        T newEntity = exampleList.stream().findFirst().get();
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (null != field.get(entity)) {
                    Field newfield = newEntity.getClass().getDeclaredField(field.getName());
                    newfield.setAccessible(true);
                    newfield.set(newEntity, field.get(entity));
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(entity.getClass().getName() + "类进行克隆并覆盖属性值时发生异常");
            }
        }
        return newEntity;
    }

    /**
     * TODO update(T entity, Example example)方法对冗余表及日志表未作相应处理
     *
     * @param entity  修改属性所在的数据对象
     * @param example 修改的条件
     * @return 成功数
     */
    @Override
    public int update(T entity, Example example) {
        int result;
        Assert.notNull(entity, "传入的entity对象不能为空");
        Assert.notNull(example, "传入的example对象条件不能为空");
        result = baseDao.updateByExampleSelective(entity, example);
        return result;
    }

    @Override
    public int updateBatchByIds(T entity, Iterable<?> ids) {
        return this.updateBatchByIds(entity, ids, null);
    }

    /**
     * TODO updateBatchByIds(T entity, Iterable<?> ids)方法对冗余表及日志表未作相应处理
     *
     * @param entity 修改后的字段属性
     * @param ids    PrimaryKey集合条件
     * @return 成功数
     */
    @Override
    public int updateBatchByIds(T entity, Iterable<?> ids, Map<String, Object> shardKeyMap) {
        Assert.notNull(entity, "批量更新的对象不能为null");
        Assert.notNull(ids, "批量更新的id条件集合不能为null");
        Example example = new Example(entity.getClass());
        Set<EntityColumn> columnList = EntityHelper.getPKColumns(entity.getClass());
        if (columnList.size() == 1) {
            EntityColumn column = columnList.iterator().next();
            example.and().andIn(column.getProperty(), ids);
            //针对分表后的处理
            if (shardKeyMap != null && shardKeyMap.size() > 0) {
                for (String shardKey : shardKeyMap.keySet()) {
                    Assert.isTrue(Objects.nonNull(shardKeyMap.get(shardKey)), entity.getClass().getName() + "类所对应的分表属性值不能为空");
                    if (shardKeyMap.get(shardKey) instanceof List) {
                        example.and().andIn(shardKey, (List) shardKeyMap.get(shardKey));
                    } else {
                        example.and().andEqualTo(shardKey, shardKeyMap.get(shardKey));
                    }
                }
            }
        } else {
            throw new MapperException("继承 updateBatchByIds 方法的实体类[" + entity.getClass().getCanonicalName() + "]中必须只有一个带有 @Id 注解的字段");
        }
        return baseDao.updateByExampleSelective(entity, example);
    }

    @Override
    public int updateStatusByIds(Iterable<?> ids, Integer source, Integer target) {
        return this.updateStatusByIds(ids, source, target, null);
    }

    @Override
    public int updateStatusByIds(Iterable<?> ids, Integer source, Integer target, Map<String, Object> shardKeyMap) {
        Assert.notNull(ids, "批量更新的id条件集合不能为null");
        Assert.notNull(target, "修改后的数据状态不能为null");
        try {
            T entity = getNewEntity();
            Field statusField = entity.getClass().getDeclaredField(STATUS);
            statusField.setAccessible(true);
            statusField.set(entity, target);
            Example example = new Example(entity.getClass());
            Set<EntityColumn> columnList = EntityHelper.getPKColumns(entity.getClass());
            if (columnList.size() == 1) {
                EntityColumn column = columnList.iterator().next();
                example.and().andIn(column.getProperty(), ids);
                if (source != null) {
                    example.and().andEqualTo("status", source);
                }
                if (shardKeyMap != null && shardKeyMap.size() > 0) {
                    for (String shardKey : shardKeyMap.keySet()) {
                        if (shardKeyMap.get(shardKey) instanceof List) {
                            example.and().andIn(shardKey, (List) shardKeyMap.get(shardKey));
                        } else {
                            example.and().andEqualTo(shardKey, shardKeyMap.get(shardKey));
                        }
                    }
                }
                return baseDao.updateByExampleSelective(entity, example);
            } else {
                throw new MapperException("继承 updateDataStatusByIds 方法的实体类[" + entity.getClass().getCanonicalName() + "]中必须只有一个带有 @Id 注解的字段");
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(this.clazz.getName() + "类进行标准化数据状态修改时发生异常");
        }
    }

    @Override
    public int updateStatusByExample(Example example, Integer source, Integer target) {
        return this.updateStatusByExample(example, source, target, null);
    }

    @Override
    public int updateStatusByExample(Example example, Integer source, Integer target, Map<String, Object> shardKeyMap) {
        Assert.notNull(example, "批量更新的自定义条件对象不能为null");
        Assert.notNull(target, "修改后的数据状态不能为null");
        try {
            T entity = getNewEntity();
            Field statusField = entity.getClass().getDeclaredField(STATUS);
            statusField.setAccessible(true);
            statusField.set(entity, target);
            if (source != null) {
                example.and().andEqualTo("status", source);
            }
            if (shardKeyMap != null && shardKeyMap.size() > 0) {
                for (String shardKey : shardKeyMap.keySet()) {
                    if (shardKeyMap.get(shardKey) instanceof List) {
                        example.and().andIn(shardKey, (List) shardKeyMap.get(shardKey));
                    } else {
                        example.and().andEqualTo(shardKey, shardKeyMap.get(shardKey));
                    }
                }
            }
            return baseDao.updateByExampleSelective(entity, example);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(this.clazz.getName() + "类进行标准化数据状态修改时发生异常");
        }
    }

    @Override
    public T save(T entity) {
        return this.save(entity, false);
    }

    @Override
    public T save(T entity, boolean isAllowRedundancy) {
        return this.save(entity, isAllowRedundancy, true);
    }

    @Override
    public T save(T entity, boolean isAllowRedundancy, boolean isStandard) {
        return this.save(entity, isAllowRedundancy, isStandard, null);
    }

    @Override
    public T save(T entity, boolean isAllowRedundancy, boolean isStandard, Map<String, Object> shardKeyMap) {
        if (isStandard) {
            try {
                Field idField = entity.getClass().getDeclaredField(ID);
                idField.setAccessible(true);
                if (idField.get(entity) == null || StringUtil.isBlank(idField.get(entity).toString())) {
                    return this.create(entity, true, shardKeyMap);
                } else {
                    return this.modify(entity, isAllowRedundancy, true, shardKeyMap);
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(entity.getClass().getName() + "类所对应的表非约定标准表");
            }
        } else {
            if (shardKeyMap != null && shardKeyMap.size() > 0) {
                for (String shardKey : shardKeyMap.keySet()) {
                    Assert.isTrue(Objects.nonNull(shardKeyMap.get(shardKey)), this.getClass().getName() + "类所对应的分表属性值不能为空");
                    try {
                        Field shard = entity.getClass().getDeclaredField(shardKey);
                        shard.setAccessible(true);
                        Assert.isTrue(Objects.equals(shard.get(entity), shardKeyMap.get(shardKey)), this.getClass().getName() + "类所对应的分表属性值与对应参数值不一致");
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(entity.getClass().getName() + "类所对应的表非约定标准表");
                    }
                }
                baseDao.insert(entity);
                return baseDao.select(entity).stream().findFirst().get();
            } else {
                baseDao.insert(entity);
                return baseDao.selectByPrimaryKey(entity);
            }
        }
    }

    @Override
    public int delete(T entity) {
        return this.delete(entity, true);
    }

    @Override
    public int delete(T entity, boolean isStandard) {
        return this.delete(entity, isStandard, null);
    }

    @Override
    public int delete(T entity, Map<String, Object> shardKeyMap) {
        return this.delete(entity, true, null);
    }

    @Override
    public int delete(T entity, boolean isStandard, Map<String, Object> shardKeyMap) {
        int result = 0;
        if (isStandard) {
            try {
                T newEntity = this.getNewEntity();
                Field idField = newEntity.getClass().getDeclaredField(ID);
                idField.setAccessible(true);
                idField.set(newEntity, idField.get(entity));
                Field modifyTimeField = newEntity.getClass().getDeclaredField(MODIFY_TIME);
                modifyTimeField.setAccessible(true);
                modifyTimeField.set(newEntity, StringUtil.equals("class java.util.Date", TypeUtil.getType(modifyTimeField).toString()) ? new Date() : LocalDateTime.now());
                Field statusField = newEntity.getClass().getDeclaredField(STATUS);
                statusField.setAccessible(true);
                statusField.set(newEntity, DataStatusEnum.Deleted.getIndex());
                //针对分表后的处理
                if (shardKeyMap != null && shardKeyMap.size() > 0) {
                    Example example = new Example(this.clazz);
                    for (String shardKey : shardKeyMap.keySet()) {
                        Assert.isTrue(Objects.nonNull(shardKeyMap.get(shardKey)), this.getClass().getName() + "类所对应的分表属性值不能为空");
                        if (shardKeyMap.get(shardKey) instanceof List) {
                            example.and().andIn(shardKey, (List) shardKeyMap.get(shardKey));
                        } else {
                            example.and().andEqualTo(shardKey, shardKeyMap.get(shardKey));
                        }
                    }
                    example.and().andEqualTo(ID, idField.get(entity));
                    result = baseDao.updateByExampleSelective(newEntity, example);
                } else {
                    result = baseDao.updateByPrimaryKeySelective(newEntity);
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(entity.getClass().getName() + "类进行标准化删除时发生异常");
            }
        } else {
            //针对分表后的处理
            if (shardKeyMap != null && shardKeyMap.size() > 0) {
                Example example = new Example(this.clazz);
                try {
                    Field idField = entity.getClass().getDeclaredField(ID);
                    idField.setAccessible(true);
                    example.and().andEqualTo(ID, idField.get(entity));
                    for (String shardKey : shardKeyMap.keySet()) {
                        Assert.isTrue(Objects.nonNull(shardKeyMap.get(shardKey)), this.getClass().getName() + "类所对应的分表属性值不能为空");
                        if (shardKeyMap.get(shardKey) instanceof List) {
                            example.and().andIn(shardKey, (List) shardKeyMap.get(shardKey));
                        } else {
                            example.and().andEqualTo(shardKey, shardKeyMap.get(shardKey));
                        }
                    }
                    example.and().andEqualTo(ID, idField.get(entity));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(entity.getClass().getName() + "类所对应的表非约定标准表");
                }
                result = baseDao.deleteByExample(example);
            } else {
                result = baseDao.deleteByPrimaryKey(entity);
            }
        }
        this.saveLogVo(entity, LOG_OPERATING_TYPE_DELETE);
        return result;
    }

    @Override
    public int delete(Example example) {
        return delete(example, true);
    }

    @Override
    public int delete(Example example, boolean isStandard) {
        return delete(example, isStandard, null);
    }

    @Override
    public int delete(Example example, Map<String, Object> shardKeyMap) {
        return delete(example, true, shardKeyMap);
    }

    /**
     * TODO delete(Example example, boolean isStandard)方法对日志表未作相应处理
     *
     * @param example     删除的条件
     * @param isStandard  是否标准表
     * @param shardKeyMap 分片键与分片建属性
     * @return 成功数
     */
    @Override
    public int delete(Example example, boolean isStandard, Map<String, Object> shardKeyMap) {
        Assert.notNull(example, "删除的条件集合不能为null");
        int result = 0;
        //针对分表后的处理
        if (shardKeyMap != null && shardKeyMap.size() > 0) {
            for (String shardKey : shardKeyMap.keySet()) {
                Assert.isTrue(Objects.nonNull(shardKeyMap.get(shardKey)), this.getClass().getName() + "类所对应的分表属性值不能为空");
                if (shardKeyMap.get(shardKey) instanceof List) {
                    example.and().andIn(shardKey, (List) shardKeyMap.get(shardKey));
                } else {
                    example.and().andEqualTo(shardKey, shardKeyMap.get(shardKey));
                }
            }
        }
        if (isStandard) {
            T newEntity = this.getNewEntity();
            try {
                Field modifyTimeField = newEntity.getClass().getDeclaredField(MODIFY_TIME);
                modifyTimeField.setAccessible(true);
                modifyTimeField.set(newEntity, StringUtil.equals("class java.util.Date", TypeUtil.getType(modifyTimeField).toString()) ? new Date() : LocalDateTime.now());
                Field statusField = newEntity.getClass().getDeclaredField(STATUS);
                statusField.setAccessible(true);
                statusField.set(newEntity, DataStatusEnum.Deleted.getIndex());
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(newEntity.getClass().getName() + "类进行标准化删除时发生异常");
            }
            result = baseDao.updateByExampleSelective(newEntity, example);
        } else {
            result = baseDao.deleteByExample(example);
        }
        return result;
    }

    @Override
    public int deleteBatchByIds(Iterable<?> ids) {
        return this.deleteBatchByIds(ids, true);
    }

    @Override
    public int deleteBatchByIds(Iterable<?> ids, boolean isStandard) {
        return this.deleteBatchByIds(ids, isStandard, null);
    }

    @Override
    public int deleteBatchByIds(Iterable<?> ids, Map<String, Object> shardKeyMap) {
        return this.deleteBatchByIds(ids, true, shardKeyMap);
    }

    /**
     * TODO deleteBatchByIds(Iterable<?> ids, boolean isStandard)方法对日志表未作相应处理
     *
     * @param ids         PrimaryKey集合条件
     * @param isStandard  是否标准表
     * @param shardKeyMap 分片键与分片建属性
     * @return 成功数
     */
    @Override
    public int deleteBatchByIds(Iterable<?> ids, boolean isStandard, Map<String, Object> shardKeyMap) {
        Assert.notNull(ids, "批量删除的id条件集合不能为null");
        int result = 0;
        T newEntity = this.getNewEntity();
        if (isStandard) {
            try {
                Field modifyTimeField = newEntity.getClass().getDeclaredField(MODIFY_TIME);
                modifyTimeField.setAccessible(true);
                modifyTimeField.set(newEntity, StringUtil.equals("class java.util.Date", TypeUtil.getType(modifyTimeField).toString()) ? new Date() : LocalDateTime.now());
                Field statusField = newEntity.getClass().getDeclaredField(STATUS);
                statusField.setAccessible(true);
                statusField.set(newEntity, DataStatusEnum.Deleted.getIndex());
                result = this.updateBatchByIds(newEntity, ids, shardKeyMap);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(this.clazz.getName() + "类进行标准化删除时发生异常");
            }
        } else {
            Example example = new Example(this.clazz);
            Set<EntityColumn> columnList = EntityHelper.getPKColumns(this.clazz);
            if (columnList.size() == 1) {
                EntityColumn column = columnList.iterator().next();
                example.and().andIn(column.getProperty(), ids);
                //针对分表后的处理
                if (shardKeyMap != null && shardKeyMap.size() > 0) {
                    for (String shardKey : shardKeyMap.keySet()) {
                        Assert.isTrue(Objects.nonNull(shardKeyMap.get(shardKey)), this.getClass().getName() + "类所对应的分表属性值不能为空");
                        if (shardKeyMap.get(shardKey) instanceof List) {
                            example.and().andIn(shardKey, (List) shardKeyMap.get(shardKey));
                        } else {
                            example.and().andEqualTo(shardKey, shardKeyMap.get(shardKey));
                        }
                    }
                }
                result = baseDao.deleteByExample(example);
            } else {
                throw new MapperException("继承 deleteBatchByIds 方法的实体类[" + this.clazz.getCanonicalName() + "]中必须只有一个带有 @Id 注解的字段");
            }
        }
        return result;
    }

    /**
     * 单个对象新增、修改、删除时，将记录日志
     *
     * @param entity        日志对象对应的实体类
     * @param operatingType 操作类型
     */
    private void saveLogVo(T entity, String operatingType) {
        if (entity != null) {
            if (this.logClazz != null) {
                try {
                    Object logVo = this.logClazz.newInstance();
                    BeanUtils.copyProperties(entity, logVo);
                    Field logIdField = logVo.getClass().getDeclaredField(LOG_ID);
                    logIdField.setAccessible(true);
                    logIdField.set(logVo, IdUtil.simpleUUID());
                    Field operatingTypeField = logVo.getClass().getDeclaredField(LOG_OPERATING_TYPE);
                    operatingTypeField.setAccessible(true);
                    operatingTypeField.set(logVo, operatingType);
                    Field operatingTimeField = logVo.getClass().getDeclaredField(LOG_OPERATING_TIME);
                    operatingTimeField.setAccessible(true);
                    operatingTimeField.set(logVo, StringUtil.equals("class java.util.Date", TypeUtil.getType(operatingTimeField).toString()) ? new Date() : LocalDateTime.now());
                    Field operatingPersonField = logVo.getClass().getDeclaredField(LOG_OPERATING_PERSON);
                    operatingPersonField.setAccessible(true);
                    // 获取登陆用户内码，并写入日志表操作人
                    //operatingPersonField.set(logVo, JwtUserUtil.getGenecode());
                    Object objectDao = applicationContext.getBean(StringUtil.toLowerCaseFirstOne(logVo.getClass().getSimpleName()) + "Dao");
                    if (objectDao != null) {
                        log.info("-----开始写入日志-----");
                        Method insertMethod = objectDao.getClass().getDeclaredMethod(LOG_METHOD_CREATE, Object.class);
                        insertMethod.invoke(objectDao, logVo);
                        log.info("-----成功写入日志-----");
                    }
                } catch (ReflectiveOperationException e) {
//                    throw new RuntimeException(entity.getClass().getName() + "存在日志VO，但未正常储存！");
                    log.error(entity.getClass().getName() + "存在日志VO，但未正常储存！", e);
                }
            } else {
                try {
                    Field idField = entity.getClass().getDeclaredField(ID);
                    idField.setAccessible(true);
                    setLogIds(idField.get(entity).toString());
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
