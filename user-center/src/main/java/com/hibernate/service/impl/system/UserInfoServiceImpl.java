package com.hibernate.service.impl.system;

import com.hibernate.dao.system.UserInfoDao;
import com.hibernate.entity.system.UserInfo;
import com.hibernate.service.system.UserInfoService;
import info.joyc.core.service.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * com.hibernate.service.impl.system.UserInfoServiceImpl.java
 * ==============================================
 * Copy right 2015-2020 by http://www.com
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : zhangbeiping
 * @version : v1.0.0
 * @desc : 系统-用户表服务实现类
 * @since : 2020-07-23 17:31
 */
@Service
public class UserInfoServiceImpl extends BaseServiceImpl<UserInfo> implements UserInfoService {

    @Resource
    private UserInfoDao userInfoDao;

    @Override
    public List<UserInfo> getUserInfoList(Map<String, Object> parameterMap) {
        return userInfoDao.queryForList(parameterMap);
    }
}
