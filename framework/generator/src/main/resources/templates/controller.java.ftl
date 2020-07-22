package ${package.Controller};

import com.github.pagehelper.PageInfo;
import ${package.Entity}.${entity};
import ${package.Service}.${table.serviceName};
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import info.joyc.tool.lang.Assert;
<#if !restControllerStyle>
import org.springframework.stereotype.Controller;
</#if>
import org.springframework.web.bind.annotation.*;
<#if swagger2>
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
</#if>
<#if superControllerClassPackage??>
import ${superControllerClassPackage};
</#if>
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ${package.Controller}.${table.controllerName}.java
 * ==============================================
 * Copy right 2015-${year} by ${cfg.domain}
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @desc : ${table.comment!}控制类
 * @author : ${author}
 * @version : v1.0.0
 * @since : ${dateTime}
 */
<#if restControllerStyle>
@RestController
<#else>
@Controller
</#if>
<#if swagger2>
@Api(value = "${table.comment!}Controller", description = "${table.comment!}相关api", tags = {"${table.comment!}操作接口"})
</#if>
@RequestMapping("/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>")
<#if kotlin>
class ${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
<#if superControllerClass??>
public class ${table.controllerName} extends ${superControllerClass} {
<#else>
public class ${table.controllerName} {
</#if>

    @Autowired
    private ${table.serviceName} ${firstCharToLowerEntity}Service;

    <#if swagger2>
    @ApiOperation(value = "获取${table.comment!}列表", notes = "获取${table.comment!}列表")
    @ApiImplicitParams({
            <#if table.fieldNameList?seq_contains("status")>
            @ApiImplicitParam(paramType = "query", name = "statusIds", value = "数据状态集，逗号隔开", required = false, dataType = "String"),
            </#if>
            <#if table.fieldNameList?seq_contains("source_id")>
            @ApiImplicitParam(paramType = "query", name = "sourceId", value = "区分版本，bd_source_info表ID", required = false, dataType = "string"),
            </#if>
            <#if table.fieldNameList?seq_contains("code") || table.fieldNameList?seq_contains("name")>
            @ApiImplicitParam(paramType = "query", name = "q", value = "按code与name条件模糊匹配", required = false, dataType = "string"),
            </#if>
            @ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页显示数据行数", required = false, dataType = "int")
    })
    </#if>
    @GetMapping(value = "")
    public ResponseEntity<PageInfo<${entity}>> get${entity}List(<#if table.fieldNameList?seq_contains("status")>@RequestParam(required = false) String statusIds,</#if>
                                                                <#if table.fieldNameList?seq_contains("source_id")>
                                                                @RequestParam(required = false) String sourceId,
                                                                </#if>
                                                                <#if table.fieldNameList?seq_contains("code") || table.fieldNameList?seq_contains("name")>
                                                                @RequestParam(required = false) String q,
                                                                </#if>
                                                                @RequestParam(required = false, defaultValue = "0") Integer pageNum,
                                                                @RequestParam(required = false, defaultValue = "0") Integer pageSize) {
        Map<String, Object> parameterMap = new HashMap<>();
        <#if table.fieldNameList?seq_contains("status")>
        if (StringUtil.isNotBlank(statusIds)) {
            parameterMap.put("statusIds", Arrays.asList(statusIds.split(",")));
        } else {
            parameterMap.put("status", DataStatusEnum.Enabled.getIndex());
        }
        </#if>
        <#if table.fieldNameList?seq_contains("source_id")>
        if (StringUtil.isNotBlank(sourceId)) {
            parameterMap.put("sourceId", sourceId);
        }
        </#if>
        <#if table.fieldNameList?seq_contains("code") || table.fieldNameList?seq_contains("name")>
        if (StringUtil.isNotBlank(q)) {
            parameterMap.put("q", q);
        }
        </#if>
        parameterMap.put("pageNum", pageNum);
        parameterMap.put("pageSize", pageSize);
        return ResponseEntity.ok(new PageInfo<>(${firstCharToLowerEntity}Service.get${entity}List(parameterMap)));
    }

    <#if swagger2>
    @ApiOperation(value = "获取单个${table.comment!}详细信息", notes = "根据url的id来获取${table.comment!}详细信息")
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "<#list table.fields as field><#if field.keyFlag>${field.propertyType?uncap_first}</#if></#list>")
    </#if>
    @GetMapping(value = "/{id}")
    public ResponseEntity<${entity}> get${entity}(@PathVariable <#list table.fields as field><#if field.keyFlag>${field.propertyType}</#if></#list> id) {
        ${entity} ${firstCharToLowerEntity} = new ${entity}();
        ${firstCharToLowerEntity}.set<#list table.fields as field><#if field.keyFlag>${field.capitalName}</#if></#list>(id);
        return ResponseEntity.ok(${firstCharToLowerEntity}Service.find(${firstCharToLowerEntity}));
    }

    <#if swagger2>
    @ApiOperation(value = "保存单个${table.comment!}", notes = "根据${entity}对象保存${table.comment!}")
    @ApiImplicitParam(paramType = "body", name = "${firstCharToLowerEntity}", value = "${table.comment!}详细实体${entity}", required = true, dataType = "${entity}")
    </#if>
    @PostMapping(value = "/save")
    public ResponseEntity<${entity}> save${entity}(@RequestBody ${entity} ${firstCharToLowerEntity}) {
        Assert.notNull(${firstCharToLowerEntity}, "保存的对象不能为空!");
        <#if table.fieldNameList?seq_contains("status")>
        Assert.notNull(${firstCharToLowerEntity}.getStatus(), "保存的对象数据状态不能为空!");
        </#if>
        return new ResponseEntity<>(${firstCharToLowerEntity}Service.save(${firstCharToLowerEntity}), HttpStatus.CREATED);
    }

    <#if swagger2>
    @ApiOperation(value = "批量删除${table.comment!}", notes = "根据url的ids来指定删除对象")
    @ApiImplicitParam(paramType = "form", name = "ids", value = "${table.comment!}IDs", required = true, allowMultiple = true, dataType = "string")
    </#if>
    @PostMapping(value = "/delete")
    public ResponseEntity<Integer> delete${entity}(@RequestParam List<String> ids) {
        int deleteCount = ${firstCharToLowerEntity}Service.deleteBatchByIds(ids);
        Assert.state(deleteCount != 0, "无此ID对应的数据对象！");
        return ResponseEntity.ok(deleteCount);
    }
}
</#if>
