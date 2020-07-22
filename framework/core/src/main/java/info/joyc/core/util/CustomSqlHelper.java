package info.joyc.core.util;

import tk.mybatis.mapper.entity.EntityColumn;
import tk.mybatis.mapper.mapperhelper.EntityHelper;
import tk.mybatis.mapper.mapperhelper.SqlHelper;

import java.util.Set;

/**
 * info.joyc.core.util.CustomSqlHelper.java
 * ==============================================
 * Copy right 2015-2019 by http://www.rejoysoft.com
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : 自定义拼常用SQL的工具类
 * @since : 2019-03-20 14:47
 */
public class CustomSqlHelper {

    /**
     * insertOrUpdate-values()列
     *
     * @param entityClass
     * @return
     */
    public static String insertOrUpdateValuesColumns(Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("<trim suffixOverrides=\",\">");
        // 获取全部列
        Set<EntityColumn> columnSet = EntityHelper.getColumns(entityClass);
        for (EntityColumn column : columnSet) {
            if (!column.isInsertable()) {
                continue;
            }
            // ID列一定在唯一索引中，不会触发修改
            if (column.isId()) {
                continue;
            }
            sql.append(column.getColumn()).append("=VALUES(").append(column.getColumn()).append("),");
        }
        sql.append("</trim>");
        return sql.toString();
    }
}
