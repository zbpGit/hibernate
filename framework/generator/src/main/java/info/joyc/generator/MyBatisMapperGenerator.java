package info.joyc.generator;

import info.joyc.generator.annotation.IdType;
import info.joyc.generator.config.*;
import info.joyc.generator.config.po.TableInfo;
import info.joyc.generator.config.rules.DateType;
import info.joyc.generator.config.rules.NamingStrategy;
import info.joyc.generator.engine.FreemarkerTemplateEngine;
import info.joyc.generator.util.StringPool;
import info.joyc.generator.util.StringUtils;
import info.joyc.tool.io.YmlUtil;
import info.joyc.tool.lang.Assert;
import info.joyc.tool.util.StringUtil;
import java.util.*;
import java.util.stream.Collectors;

/**
 * com.example.java.engine.mybatis.plus.MyBatisPlusGenerator.java
 * ==============================================
 * Copy right 2015-2018 by http://www.rejoysoft.com
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : MyBatis-Mapper 代码生成器
 * @since : 2018-11-12 17:28
 */
public class MyBatisMapperGenerator {

    /**
     * 非当前项目
     */
    private Boolean noCurrent = false;

    /**
     * 项目所在路径
     */
    private static String projectPath;

    /**
     * 组织名
     */
    private String groupName;

    /**
     * 项目名
     */
    private String projectName;

    /**
     * 表前缀与系统模块对应关系集合
     */
    private Map<String, String> tablePrefixMap = new HashMap<>();

    /**
     * 模板配置
     */
    private TemplateConfig templateConfig;

    /**
     * 日期类型
     */
    private DateType dateType = DateType.ONLY_DATE;

    /**
     * 是否生成前端页面
     */
    private Boolean createFront = false;

    /**
     * 作者
     */
    private String author;

    /**
     * 根据表自动生成
     *
     * @param tableNames 表名
     * @author qiuzh
     */
    public void generateByTables(String tableNames) {
        String scannerAuthor = scanner("作者（请输入您的大名）", false);
        author = StringUtil.isNotBlank(scannerAuthor) ? scannerAuthor : System.getProperty("user.name");
        if (StringUtil.isBlank(tableNames)) {
            tableNames = scanner("表名（支持多个，以英文逗号[,]分隔）");
        }
        Assert.notEmpty(tableNames, "参数[tableNames]未输入");
        Assert.notEmpty(groupName, "参数[groupName]未配置");
        Assert.notEmpty(projectName, "参数[projectName]未配置");
        if (noCurrent == null) {
            // 是否当前项目
            String current = scanner("是否当前项目（1 是，0 不是）");
            if ("0".equals(current)) {
                noCurrent = true;
            } else {
                noCurrent = false;
            }
        }
        String replace = tableNames.replace(" ", "");
        String[] split = replace.split(",");
        Map<String, List<String>> tableNameCollect = Arrays.stream(split).collect(Collectors.groupingBy(g -> g.substring(0, g.indexOf("_")), Collectors.toList()));
        for (Map.Entry<String, List<String>> table : tableNameCollect.entrySet()) {
            String moduleName = tablePrefixMap.getOrDefault(table.getKey(), "temp");
            // 配置数据源
            DataSourceConfig dataSourceConfig = getDataSourceConfig();
            // 全局变量配置
            GlobalConfig globalConfig = getGlobalConfig();
            // 包配置
            PackageConfig packageConfig = getPackageConfig(moduleName);
            // 策略配置
            StrategyConfig strategyConfig = getStrategyConfig(table.getKey(), table.getValue().toArray(new String[0]));
            // 模板配置
            TemplateConfig templateConfig = getTemplateConfig();
            // 自定义配置
            InjectionConfig injectionConfig = getInjectionConfig(moduleName);
            // 自动生成
            atuoGenerator(dataSourceConfig, globalConfig, packageConfig, strategyConfig, templateConfig, injectionConfig);
        }
    }

    /**
     * 集成
     *
     * @param dataSourceConfig 配置数据源
     * @param globalConfig     全局变量配置
     * @param packageConfig    包名配置
     * @param strategyConfig   策略配置
     * @param templateConfig   模板配置
     * @param injectionConfig  自定义配置
     * @author qiuzh
     */
    private static void atuoGenerator(DataSourceConfig dataSourceConfig, GlobalConfig globalConfig, PackageConfig packageConfig, StrategyConfig strategyConfig, TemplateConfig templateConfig, InjectionConfig injectionConfig) {
        new AutoGenerator().setGlobalConfig(globalConfig)
                .setDataSource(dataSourceConfig)
                .setStrategy(strategyConfig)
                .setPackageInfo(packageConfig)
                .setTemplateEngine(new FreemarkerTemplateEngine())
                .setCfg(injectionConfig)
                .setTemplate(templateConfig)
                .execute();
    }

    /**
     * 自定义配置
     * 注入自定义配置，可以在 VM 中使用 cfg.abc 【可无】  ${cfg.abc}
     *
     * @param moduleName 模块路径包名
     * @return InjectionConfig
     */
    private InjectionConfig getInjectionConfig(String moduleName) {
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                Map<String, Object> map = new HashMap<>();
                StringBuilder domain = new StringBuilder("http://www.");
                if (StringUtil.isNotBlank(groupName)) {
                    String[] split = groupName.split("\\.");
                    for (int i = split.length; i > 0; i--) {
                        if (i != split.length) {
                            domain.append(".");
                        }
                        domain.append(split[i - 1]);
                    }
                } else {
                    domain.append("rejoysoft.com");
                }
                map.put("domain", domain.toString());
                map.put("moduleName", moduleName);
                this.setMap(map);
            }
        };

        if (createFront) {
            // 设置WEB文件生成
            List<FileOutConfig> focList = getWebGeneratorFileOutConfig(moduleName);
            cfg.setFileOutConfigList(focList);
        }
        return cfg;
    }

    /**
     * 配置WEB页面生成需要的模板及生成的文件输出路径
     *
     * @param moduleName 模块名
     * @return
     */
    private List<FileOutConfig> getWebGeneratorFileOutConfig(String moduleName) {
        List<FileOutConfig> focList = new ArrayList<>();
        // API接口模板
        focList.add(new FileOutConfig("/templates/web/api.js.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件路径及名称
                return getProjectPath() + "/web/api/" + moduleName + "/" + StringUtils.firstCharToLower(tableInfo.getEntityPath()) + StringPool.DOT_JS;
            }
        });
        // 列表页面模板
        focList.add(new FileOutConfig("/templates/web/index.vue.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件路径及名称
                return getProjectPath() + "/web/views/" + moduleName + "/" + tableInfo.getEntityName().toLowerCase() + "/index" + StringPool.DOT_VUE;
            }
        });
        // 查看页面模板
        focList.add(new FileOutConfig("/templates/web/show.vue.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件路径及名称
                return getProjectPath() + "/web/views/" + moduleName + "/" + tableInfo.getEntityName().toLowerCase() + "/show" + StringPool.DOT_VUE;
            }
        });
        // 编辑页面模板
        focList.add(new FileOutConfig("/templates/web/edit.vue.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件路径及名称
                return getProjectPath() + "/web/views/" + moduleName + "/" + tableInfo.getEntityName().toLowerCase() + "/edit" + StringPool.DOT_VUE;
            }
        });
        // 路由注册模板
        focList.add(new FileOutConfig("/templates/web/router.js.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件路径及名称
                return getProjectPath() + "/web/router/" + tableInfo.getEntityName().toLowerCase() + "/index" + StringPool.DOT_JS;
            }
        });
        return focList;
    }

    /**
     * 模板配置
     *
     * @return TemplateConfig
     * @author qiuzh
     */
    private TemplateConfig getTemplateConfig() {
        return templateConfig;
    }

    public void setTemplateConfig(TemplateConfig templateConfig) {
        this.templateConfig = templateConfig;
    }

    /**
     * 策略配置
     *
     * @param tablePrefix 表名前缀
     * @param tableNames  表名
     * @return StrategyConfig
     * @author qiuzh
     */
    private StrategyConfig getStrategyConfig(String tablePrefix, String... tableNames) {
        return new StrategyConfig()
                .setNaming(NamingStrategy.underline_to_camel)
                .setColumnNaming(NamingStrategy.underline_to_camel)
                .setEntityLombokModel(true)
                .setRestControllerStyle(true)
                .setInclude(tableNames)
                .setControllerMappingHyphenStyle(true)
                //.setSuperEntityClass(groupName + ".core.BaseVO")
                //.setSuperEntityColumns("id", "status", "create_time", "modify_time")
                //.setLogicDeleteFieldName("status")
                //.setVersionFieldName("modify_time")
                //.setSuperMapperClass(groupName + ".core.dao.BaseDao")
                .setSuperMapperClass("info.joyc.core.dao.BaseDao")
                //.setSuperServiceClass(groupName + ".core.service.BaseService")
                .setSuperServiceClass("info.joyc.core.service.BaseService")
                //.setSuperServiceImplClass(groupName + ".core.service.impl.BaseServiceImpl")
                .setSuperServiceImplClass("info.joyc.core.service.impl.BaseServiceImpl")
                .setTablePrefix(tablePrefix + "_");
    }

    /**
     * 包配置
     *
     * @param moduleName 模块路径包名
     * @return PackageConfig 包名配置
     * @author qiuzh
     */
    private PackageConfig getPackageConfig(String moduleName) {
        return new PackageConfig()
                .setModuleName(projectName)
                .setParent(groupName)
                .setXml("dao." + moduleName)
                .setMapper("dao." + moduleName)
                .setEntity("entity." + moduleName)
                .setService("service." + moduleName)
                .setServiceImpl("service.impl." + moduleName)
                .setController("web." + moduleName);
    }

    /**
     * 全局配置
     *
     * @return GlobalConfig
     * @author qiuzh
     */
    private GlobalConfig getGlobalConfig() {
        return new GlobalConfig()
                .setOutputDir(getProjectPath() + "/src/main/java/")
                .setFileOverride(true)
                .setSwagger2(true)
                .setBaseResultMap(true)
                .setBaseColumnList(true)
                .setIdType(IdType.UUID)
                .setAuthor(author)
                .setOpen(false)
                .setDateType(dateType)
                .setXmlName("%sDao")
                .setMapperName("%sDao")
                .setServiceName("%sService");
    }

    /**
     * 数据源配置
     *
     * @return 数据源配置 DataSourceConfig
     * @author qiuzh
     */
    private DataSourceConfig getDataSourceConfig() {
        String drivername = null;
        String url = null;
        String username = null;
        String password = null;
        // 直接从开发配置文件中读取
        Map datasource = (Map) YmlUtil.getValue(getProjectPath() + "/src/main/resources/application-dev.yml", "spring.datasource");
        if (datasource.containsKey("drivername")) {
            drivername = datasource.get("drivername").toString();
        } else {
            throw new RuntimeException("请检查application-dev.yml中是否含有spring.datasource.drivername属性！");
        }
        if (datasource.containsKey("url")) {
            url = datasource.get("url").toString();
        } else {
            throw new RuntimeException("请检查application-dev.yml中是否含有spring.datasource.url属性！");
        }
        if (datasource.containsKey("username")) {
            username = datasource.get("username").toString();
        } else {
            throw new RuntimeException("请检查application-dev.yml中是否含有spring.datasource.username属性！");
        }
        if (datasource.containsKey("password")) {
            password = datasource.get("password").toString();
        } else {
            throw new RuntimeException("请检查application-dev.yml中是否含有spring.datasource.password属性！");
        }
        DataSourceConfig dataSourceConfig = new DataSourceConfig()
                .setDriverName(drivername)
                .setUrl(url)
                .setUsername(username)
                .setPassword(password);
        dataSourceConfig.setDbType(dataSourceConfig.getDbType());
        return dataSourceConfig;
    }

    private String getProjectPath() {
        if (StringUtil.isBlank(projectPath)) {
            if (noCurrent) {
                projectPath = scanner("项目路径");
            } else {
                projectPath = System.getProperty("user.dir");
            }
        }
        return projectPath;
    }

    private static String scanner(String tip) {
        return scanner(tip, true);
    }

    /**
     * 读取控制台内容
     */
    private static String scanner(String tip, boolean isThrow) {
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("/n");
        System.out.println("请输入" + tip + "：");
        if (scanner.hasNextLine()) {
            String ipt = scanner.nextLine();
            if (StringUtil.isNotEmpty(ipt)) {
                return ipt;
            }
        }
        if (isThrow) {
            throw new RuntimeException("请输入正确的" + tip + "！");
        }
        return null;
    }

    public void setNoCurrent(Boolean noCurrent) {
        this.noCurrent = noCurrent;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setTablePrefixMap(Map<String, String> tablePrefixMap) {
        this.tablePrefixMap = tablePrefixMap;
    }

    public void setDateType(DateType dateType) {
        this.dateType = dateType;
    }

    public void setCreateFront(Boolean createFront) {
        this.createFront = createFront;
    }
}
