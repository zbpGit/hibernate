package ${package.Entity};

<#list table.importPackages as pkg>
import ${pkg};
</#list>
<#if swagger2>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
</#if>
<#if entityLombokModel>
import lombok.Data;
    <#if superEntityClass??>
import lombok.EqualsAndHashCode;
    </#if>
import lombok.experimental.Accessors;
</#if>
import javax.persistence.*;

/**
 * ${package.Entity}.${entity}.java
 * ==============================================
 * Copy right 2015-${year} by ${cfg.domain}
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @desc : ${table.comment!}实体类
 * @author : ${author}
 * @version : v1.0.0
 * @since : ${dateTime}
 */
<#if entityLombokModel>
@Data
    <#if superEntityClass??>
@EqualsAndHashCode(callSuper = true)
    <#--<#else>-->
<#--@EqualsAndHashCode(callSuper = false)-->
    </#if>
@Accessors(chain = true)
</#if>
<#if swagger2>
<#--@ApiModel(value = "${entity}对象", description = "${table.comment!}实体类")-->
@ApiModel(description = "${table.comment!}实体类")
</#if>
<#if table.convert>
@Table(name = "${table.name}")
</#if>
<#if superEntityClass??>
public class ${entity} extends ${superEntityClass}<#if activeRecord><${entity}></#if> {
<#elseif activeRecord>
public class ${entity} extends Model<${entity}> {
<#else>
public class ${entity} implements Serializable {
</#if>

    private static final long serialVersionUID = 1L;
<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
    <#--<#if field.keyFlag>-->
        <#--<#assign keyPropertyName="${field.propertyName}"/>-->
    <#--</#if>-->

    <#if field.comment!?length gt 0>
    /**
    * ${field.comment}
    */
    <#if swagger2>
    @ApiModelProperty(value = "${field.comment}")
    </#if>
    </#if>
    <#if field.keyFlag>
    <#-- 主键 -->
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    </#if>
    <#-- 普通字段 -->
    <#if field.convert>
    @Column(name = "${field.name}")
    </#if>
<#-- 乐观锁注解 -->
    <#if (versionFieldName!"") == field.name>
    @Version
    </#if>
<#-- 逻辑删除注解 -->
    <#if (logicDeleteFieldName!"") == field.name>
    @TableLogic
    </#if>
    private ${field.propertyType} ${field.propertyName};
</#list>
<#------------  END 字段循环遍历  ---------->

<#if !entityLombokModel>
    <#list table.fields as field>
        <#if field.propertyType == "boolean">
            <#assign getprefix="is"/>
        <#else>
            <#assign getprefix="get"/>
        </#if>
    public ${field.propertyType} ${getprefix}${field.capitalName}() {
        return ${field.propertyName};
    }

        <#if entityBuilderModel>
    public ${entity} set${field.capitalName}(${field.propertyType} ${field.propertyName}) {
        <#else>
    public void set${field.capitalName}(${field.propertyType} ${field.propertyName}) {
        </#if>
        this.${field.propertyName} = ${field.propertyName};
        <#if entityBuilderModel>
        return this;
        </#if>
    }
    </#list>
</#if>

<#if entityColumnConstant>
    <#list table.fields as field>
    public static final String ${field.name?upper_case} = "${field.name}";

    </#list>
</#if>
<#if activeRecord>
    @Override
    protected Serializable pkVal() {
    <#if keyPropertyName??>
        return this.${keyPropertyName};
    <#else>
        return null;
    </#if>
    }

</#if>
<#if !entityLombokModel>
    @Override
    public String toString() {
        return "${entity}{" +
    <#list table.fields as field>
        <#if field_index==0>
        "${field.propertyName}=" + ${field.propertyName} +
        <#else>
        ", ${field.propertyName}=" + ${field.propertyName} +
        </#if>
    </#list>
        "}";
    }
</#if>
}
