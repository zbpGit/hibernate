package com.hibernate.service.impl.system;

import com.hibernate.dao.system.CharacterUserDao;
import com.hibernate.entity.system.CharacterUser;
import com.hibernate.service.system.CharacterUserService;
import info.joyc.core.service.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * com.hibernate.service.impl.system.CharacterUserServiceImpl.java
 * ==============================================
 * Copy right 2015-2020 by http://www.com
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : zhangbeiping
 * @version : v1.0.0
 * @desc : 系统-用户角色关联表服务实现类
 * @since : 2020-07-23 17:31
 */
@Service
public class CharacterUserServiceImpl extends BaseServiceImpl<CharacterUser> implements CharacterUserService {

    @Resource
    private CharacterUserDao characterUserDao;

    @Override
    public List<CharacterUser> getCharacterUserList(Map<String, Object> parameterMap) {
        return characterUserDao.queryForList(parameterMap);
    }
}
