package ${package.Service};

import ${package.Entity}.${entity};
import ${superServiceClassPackage};
import java.util.List;
import java.util.Map;

/**
 * ${package.Service}.${table.serviceName}.java
 * ==============================================
 * Copy right 2015-${year} by ${cfg.domain}
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @desc : ${table.comment!}服务类
 * @author : ${author}
 * @version : v1.0.0
 * @since : ${dateTime}
 */
<#if kotlin>
interface ${table.serviceName} : ${superServiceClass}<${entity}>
<#else>
public interface ${table.serviceName} extends ${superServiceClass}<${entity}> {

    /**
     * 根据Map条件查询${entity}对象列表
     *
     * @param parameterMap Map条件集合
     * @return ${entity}对象列表
     */
    List<${entity}> get${entity}List(Map<String, Object> parameterMap);
}
</#if>
