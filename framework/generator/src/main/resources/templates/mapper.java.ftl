package ${package.Mapper};

import ${package.Entity}.${entity};
import ${superMapperClassPackage};
import java.util.List;
import java.util.Map;

/**
 * ${package.Mapper}.${table.mapperName}.java
 * ==============================================
 * Copy right 2015-${year} by ${cfg.domain}
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @desc : ${table.comment!}数据访问类
 * @author : ${author}
 * @version : v1.0.0
 * @since : ${dateTime}
 */
<#if kotlin>
interface ${table.mapperName} : ${superMapperClass}<${entity}>
<#else>
public interface ${table.mapperName} extends ${superMapperClass}<${entity}> {

    /**
     * 根据参数集合查询${table.comment!}列表
     *
     * @param parameterMap 参数集合
     * @return ${table.comment!}列表
     */
    List<${entity}> queryForList(Map<String, Object> parameterMap);
}
</#if>
