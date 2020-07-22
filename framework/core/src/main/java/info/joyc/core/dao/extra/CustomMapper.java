package info.joyc.core.dao.extra;

import org.apache.ibatis.annotations.InsertProvider;

import java.util.List;

/**
 * info.joyc.core.dao.extra.CustomMapper.java
 * ==============================================
 * Copy right 2015-2018 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : 自定义通用方法
 * @since : 2018-01-02 11:11
 */
@tk.mybatis.mapper.annotation.RegisterMapper
public interface CustomMapper<T> {

    /**
     * 全字段都需要直接赋值
     *
     * @param recordList
     * @return
     */
    @InsertProvider(type = CustomProvider.class, method = "dynamicSQL")
    int insertEntityList(List<T> recordList);

    /**
     * 全字段都需要直接赋值，根据ID存在与否决定新增还是修改
     *
     * @param recordList
     * @return
     */
    @InsertProvider(type = CustomProvider.class, method = "dynamicSQL")
    int insertOrUpdateEntityList(List<T> recordList);

    //@UpdateProvider(
    //        type = CustomProvider.class,
    //        method = "dynamicSQL"
    //)
    //@Options(
    //        useCache = false,
    //        useGeneratedKeys = false
    //)
    //int updateBatchByIds(@Param("record") T var1, @Param("ids") String var2);
}
