package com.hibernate.entity.system;

import java.util.Date;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import javax.persistence.*;

/**
 * com.hibernate.entity.system.UserInfo.java
 * ==============================================
 * Copy right 2015-2020 by http://www.com
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @desc : 系统-用户表实体类
 * @author : zhangbeiping
 * @version : v1.0.0
 * @since : 2020-07-23 17:31
 */
@Data
@Accessors(chain = true)
@ApiModel(description = "系统-用户表实体类")
@Table(name = "sys_user_info")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
    * 用户ID
    */
    @ApiModelProperty(value = "用户ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
    * 用户编号
    */
    @ApiModelProperty(value = "用户编号")
    private String code;

    /**
    * 用户名称
    */
    @ApiModelProperty(value = "用户名称")
    private String name;

    /**
    * 用户内码
    */
    @ApiModelProperty(value = "用户内码")
    private String genecode;

    /**
    * 手机号码
    */
    @ApiModelProperty(value = "手机号码")
    private String mobile;

    /**
    * 登录名称
    */
    @ApiModelProperty(value = "登录名称")
    @Column(name = "login_name")
    private String loginName;

    /**
    * 登录名称
    */
    @ApiModelProperty(value = "登录名称")
    @Column(name = "login_password")
    private String loginPassword;

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
