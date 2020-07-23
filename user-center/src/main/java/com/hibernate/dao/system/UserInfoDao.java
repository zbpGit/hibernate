package com.hibernate.dao.system;

import com.hibernate.entity.system.UserInfo;
import info.joyc.core.dao.BaseDao;

import java.util.List;
import java.util.Map;

/**
 * com.hibernate.dao.system.UserInfoDao.java
 * ==============================================
 * Copy right 2015-2020 by http://www.com
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : zhangbeiping
 * @version : v1.0.0
 * @desc : 系统-用户表数据访问类
 * @since : 2020-07-23 17:31
 */
public interface UserInfoDao extends BaseDao<UserInfo> {

    /**
     * 根据参数集合查询系统-用户表列表
     *
     * @param parameterMap 参数集合
     * @return 系统-用户表列表
     */
    List<UserInfo> queryForList(Map<String, Object> parameterMap);
}
