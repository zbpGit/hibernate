/*
 * Copyright (c) 2011-2016, hubin (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package info.joyc.generator.config;

import info.joyc.generator.config.rules.IColumnType;

/**
 * <p>
 * 数据库字段类型转换
 * </p>
 *
 * @author hubin
 * @since 2017-01-20
 */
public interface ITypeConvert {


    /**
     * <p>
     * 执行类型转换
     * </p>
     *
     * @param globalConfig 全局配置
     * @param fieldType    字段类型
     * @return
     */
    IColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType);
}
