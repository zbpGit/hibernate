package ${package.ServiceImpl};

import ${package.Entity}.${entity};
import ${package.Mapper}.${table.mapperName};
import ${package.Service}.${table.serviceName};
import ${superServiceImplClassPackage};
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * ${package.ServiceImpl}.${table.serviceImplName}.java
 * ==============================================
 * Copy right 2015-${year} by ${cfg.domain}
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @desc : ${table.comment!}服务实现类
 * @author : ${author}
 * @version : v1.0.0
 * @since : ${dateTime}
 */
@Service
<#if kotlin>
open class ${table.serviceImplName} : ${superServiceImplClass}<${entity}>(), ${table.serviceName} {

}
<#else>
public class ${table.serviceImplName} extends ${superServiceImplClass}<${entity}> implements ${table.serviceName} {

    @Resource
    private ${table.mapperName} ${firstCharToLowerEntity}Dao;

    @Override
    public List<${entity}> get${entity}List(Map<String, Object> parameterMap) {
        return ${firstCharToLowerEntity}Dao.queryForList(parameterMap);
    }
}
</#if>
