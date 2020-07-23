package com.hibernate.service.impl.system;

import com.hibernate.dao.system.CharacterInfoDao;
import com.hibernate.entity.system.CharacterInfo;
import com.hibernate.service.system.CharacterInfoService;
import info.joyc.core.service.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * com.hibernate.service.impl.system.CharacterInfoServiceImpl.java
 * ==============================================
 * Copy right 2015-2020 by http://www.com
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : zhangbeiping
 * @version : v1.0.0
 * @desc : 系统-角色表服务实现类
 * @since : 2020-07-23 17:31
 */
@Service
public class CharacterInfoServiceImpl extends BaseServiceImpl<CharacterInfo> implements CharacterInfoService {

    @Resource
    private CharacterInfoDao characterInfoDao;

    @Override
    public List<CharacterInfo> getCharacterInfoList(Map<String, Object> parameterMap) {
        return characterInfoDao.queryForList(parameterMap);
    }
}
