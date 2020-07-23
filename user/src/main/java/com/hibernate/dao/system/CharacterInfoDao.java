package com.hibernate.dao.system;

import com.hibernate.entity.system.CharacterInfo;
import info.joyc.core.dao.BaseDao;
import java.util.List;
import java.util.Map;

/**
 * com.hibernate.dao.system.CharacterInfoDao.java
 * ==============================================
 * Copy right 2015-2020 by http://www.com
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @desc : 系统-角色表数据访问类
 * @author : zhangbeiping
 * @version : v1.0.0
 * @since : 2020-07-23 17:31
 */
public interface CharacterInfoDao extends BaseDao<CharacterInfo> {

    /**
     * 根据参数集合查询系统-角色表列表
     *
     * @param parameterMap 参数集合
     * @return 系统-角色表列表
     */
    List<CharacterInfo> queryForList(Map<String, Object> parameterMap);
}
