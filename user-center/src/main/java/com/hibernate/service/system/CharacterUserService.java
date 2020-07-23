package com.hibernate.service.system;

import com.hibernate.entity.system.CharacterUser;
import info.joyc.core.service.BaseService;

import java.util.List;
import java.util.Map;

/**
 * com.hibernate.service.system.CharacterUserService.java
 * ==============================================
 * Copy right 2015-2020 by http://www.com
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : zhangbeiping
 * @version : v1.0.0
 * @desc : 系统-用户角色关联表服务类
 * @since : 2020-07-23 17:31
 */
public interface CharacterUserService extends BaseService<CharacterUser> {

    /**
     * 根据Map条件查询CharacterUser对象列表
     *
     * @param parameterMap Map条件集合
     * @return CharacterUser对象列表
     */
    List<CharacterUser> getCharacterUserList(Map<String, Object> parameterMap);
}
