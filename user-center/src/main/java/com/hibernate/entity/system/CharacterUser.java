package com.hibernate.entity.system;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * com.hibernate.entity.system.CharacterUser.java
 * ==============================================
 * Copy right 2015-2020 by http://www.com
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : zhangbeiping
 * @version : v1.0.0
 * @desc : 系统-用户角色关联表实体类
 * @since : 2020-07-23 17:31
 */
@Data
@Accessors(chain = true)
@ApiModel(description = "系统-用户角色关联表实体类")
@Table(name = "sys_character_user")
public class CharacterUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户角色ID
     */
    @ApiModelProperty(value = "用户角色ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 角色ID
     */
    @ApiModelProperty(value = "角色ID")
    @Column(name = "character_id")
    private String characterId;

    /**
     * 角色ID
     */
    @ApiModelProperty(value = "角色ID")
    @Column(name = "user_id")
    private String userId;

    /**
     * 数据状态 枚举：-2临时保存 -1删除 0停用 1启用 2已过时
     */
    @ApiModelProperty(value = "数据状态 枚举：-2临时保存 -1删除 0停用 1启用 2已过时")
    private Integer status;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    @Column(name = "modify_time")
    private Date modifyTime;


}
