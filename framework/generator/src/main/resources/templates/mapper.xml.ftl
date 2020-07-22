<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${package.Mapper}.${table.mapperName}">

<#if enableCache>
    <!-- 开启二级缓存 -->
    <cache type="org.mybatis.caches.ehcache.LoggingEhcache"/>

</#if>
<#if baseResultMap>
    <!-- 通用查询映射结果 -->
    <resultMap id="BaseResultMap" type="${package.Entity}.${entity}">
<#list table.fields as field>
<#if field.keyFlag><#--生成主键排在第一位-->
        <id column="${field.name}" property="${field.propertyName}" />
</#if>
</#list>
<#list table.commonFields as field><#--生成公共字段 -->
    <result column="${field.name}" property="${field.propertyName}" />
</#list>
<#list table.fields as field>
<#if !field.keyFlag><#--生成普通字段 -->
        <result column="${field.name}" property="${field.propertyName}" />
</#if>
</#list>
    </resultMap>

</#if>
<#if baseColumnList>
    <!-- 通用查询结果列 -->
    <sql id="BaseColumnList">
<#list table.commonFields as field>
        ${field.name},
</#list>
        ${table.fieldNames}
    </sql>

    <select id="queryForList" resultMap="BaseResultMap" parameterType="map">
        SELECT
        <include refid="BaseColumnList"/>
        FROM ${table.name} t
        <where>
            <#if table.fieldNameList?seq_contains("id")>
            <if test="id != null and id != ''">
                AND t.id = ${r'#{id}'}
            </if>
            <if test="ids != null and ids.size() > 0">
                AND t.id IN
                <foreach item="item" index="index" collection="ids" open="(" separator="," close=")">${r'#{item}'}</foreach>
            </if>
            </#if>
            <#if table.fieldNameList?seq_contains("status")>
            <if test="status != null">
                AND t.status = ${r'#{status}'}
            </if>
            <if test="statusIds != null and statusIds.size > 0">
                AND t.status IN
                <foreach item="item" index="index" collection="statusIds" open="(" separator="," close=")">${r'#{item}'}</foreach>
            </if>
            </#if>
            <#if table.fieldNameList?seq_contains("source_id")>
            <if test="sourceId != null and sourceId != ''">
                AND t.source_id = ${r'#{sourceId}'}
            </if>
            </#if>
            <#if table.fieldNameList?seq_contains("genecode")>
            <if test="genecode != null and genecode != ''">
                AND t.genecode = ${r'#{genecode}'}
            </if>
            <if test="genecodes != null and genecodes.size > 0">
                AND t.genecode IN
                <foreach item="item" index="index" collection="genecodes" open="(" separator="," close=")">${r'#{item}'}</foreach>
            </if>
            </#if>
            <#if table.fieldNameList?seq_contains("code") || table.fieldNameList?seq_contains("name")>
            <if test="q != null and q != ''">
                AND <#if table.fieldNameList?seq_contains("code") && table.fieldNameList?seq_contains("name")>(</#if><#if table.fieldNameList?seq_contains("code")>t.code LIKE CONCAT(${r'#{q}'}, '%')</#if><#if table.fieldNameList?seq_contains("code") && table.fieldNameList?seq_contains("name")> OR </#if><#if table.fieldNameList?seq_contains("name")>t.name LIKE CONCAT('%', ${r'#{q}'}, '%')</#if><#if table.fieldNameList?seq_contains("code") && table.fieldNameList?seq_contains("name")> OR CONCAT(t.code, ' ', t.name) LIKE CONCAT(${r'#{q}'}, '%'))</#if>
            </if>
            </#if>
        </where>
        <#if table.fieldNameList?seq_contains("sort")>
        ORDER BY t.sort
        </#if>
    </select>
</#if>
</mapper>
