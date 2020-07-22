package info.joyc.core.dao.extra;

import info.joyc.core.util.CustomSqlHelper;
import org.apache.ibatis.mapping.MappedStatement;
import tk.mybatis.mapper.entity.EntityColumn;
import tk.mybatis.mapper.mapperhelper.EntityHelper;
import tk.mybatis.mapper.mapperhelper.MapperHelper;
import tk.mybatis.mapper.mapperhelper.MapperTemplate;
import tk.mybatis.mapper.mapperhelper.SqlHelper;

import java.util.Set;

/**
 * info.joyc.core.dao.extra.CustomProvider.java
 * ==============================================
 * Copy right 2015-2018 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : 自定义通用方法支持
 * @since : 2018-01-02 11:13
 */
public class CustomProvider extends MapperTemplate {

    public CustomProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
        super(mapperClass, mapperHelper);
    }

    public String insertEntityList(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlHelper.insertIntoTable(entityClass, tableName(entityClass)));
        sql.append(SqlHelper.insertColumns(entityClass, false, false, false));
        sql.append(" VALUES ");
        sql.append("<foreach collection=\"list\" item=\"record\" separator=\",\" >");
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        Set<EntityColumn> columnList = EntityHelper.getColumns(entityClass);
        for (EntityColumn column : columnList) {
            sql.append(column.getColumnHolder("record") + ",");
        }
        sql.append("</trim>");
        sql.append("</foreach>");
        return sql.toString();
    }

    public String insertOrUpdateEntityList(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlHelper.insertIntoTable(entityClass, tableName(entityClass)));
        sql.append(SqlHelper.insertColumns(entityClass, false, false, false));
        sql.append(" VALUES ");
        sql.append("<foreach collection=\"list\" item=\"record\" separator=\",\" >");
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        Set<EntityColumn> columnList = EntityHelper.getColumns(entityClass);
        for (EntityColumn column : columnList) {
            sql.append(column.getColumnHolder("record") + ",");
        }
        sql.append("</trim>");
        sql.append("</foreach>");
        sql.append(" ON DUPLICATE KEY UPDATE ");
        sql.append(CustomSqlHelper.insertOrUpdateValuesColumns(entityClass));
        return sql.toString();
    }

    //public String updateBatchByIds(MappedStatement ms) {
    //    Class<?> entityClass = this.getEntityClass(ms);
    //    StringBuilder sql = new StringBuilder();
    //    sql.append(SqlHelper.updateTable(entityClass, this.tableName(entityClass)));
    //    sql.append(SqlHelper.updateSetColumns(entityClass, "record", true, this.isNotEmpty()));
    //    Set<EntityColumn> columnList = EntityHelper.getPKColumns(entityClass);
    //    if (columnList.size() == 1) {
    //        EntityColumn column = (EntityColumn)columnList.iterator().next();
    //        sql.append(" where ");
    //        sql.append(column.getColumn());
    //        //sql.append(" in (#{ids})");
    //        sql.append(" in (${ids})");
    //    } else {
    //        throw new MapperException("继承 selectByIds 方法的实体类[" + entityClass.getCanonicalName() + "]中必须只有一个带有 @Id 注解的字段");
    //    }
    //    return sql.toString();
    //}
}
