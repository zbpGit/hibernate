package com.hibernate.service.system;

import com.hibernate.entity.system.CharacterInfo;
import info.joyc.core.service.BaseService;
import java.util.List;
import java.util.Map;

/**
 * com.hibernate.service.system.CharacterInfoService.java
 * ==============================================
 * Copy right 2015-2020 by http://www.com
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @desc : 系统-角色表服务类
 * @author : zhangbeiping
 * @version : v1.0.0
 * @since : 2020-07-23 17:31
 */
public interface CharacterInfoService extends BaseService<CharacterInfo> {

    /**
     * 根据Map条件查询CharacterInfo对象列表
     *
     * @param parameterMap Map条件集合
     * @return CharacterInfo对象列表
     */
    List<CharacterInfo> getCharacterInfoList(Map<String, Object> parameterMap);
}
