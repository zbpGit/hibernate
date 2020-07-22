package info.joyc.tool.io;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * info.joyc.tool.io.YmlUtil.java
 * ==============================================
 * Copy right 2015-2018 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : yml文件操作工具类
 * @since : 2018-11-13 18:43
 */
public class YmlUtil {

    /**
     * key:文件名索引
     * value:配置文件内容
     */
    private static Map<String, LinkedHashMap> ymls = new HashMap<>();

    /**
     * string:当前线程需要查询的文件名
     */
    private static ThreadLocal<String> nowFileName = new ThreadLocal<>();

    /**
     * 加载配置文件
     *
     * @param fileName
     */
    private static void loadYml(String fileName) {
        nowFileName.set(fileName);
        if (!ymls.containsKey(fileName)) {
            if (fileName.startsWith("/") || fileName.indexOf(":") > 0) {
                try {
                    File file = new File(fileName);
                    ymls.put(fileName, new Yaml().loadAs(new FileInputStream(file), LinkedHashMap.class));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("不存在的文件");
                }
            } else {
                ymls.put(fileName, new Yaml().loadAs(YmlUtil.class.getResourceAsStream("/" + fileName), LinkedHashMap.class));
            }
        }
    }

    private static Object getValue(String key) {
        // 首先将key进行拆分
        String[] keys = key.split("[.]");

        // 将配置文件进行复制
        Map ymlInfo = (Map) ymls.get(nowFileName.get()).clone();
        for (int i = 0; i < keys.length; i++) {
            Object value = ymlInfo.get(keys[i]);
            if (i < keys.length - 1) {
                ymlInfo = (Map) value;
            } else if (value == null) {
                throw new RuntimeException("key不存在");
            } else {
                return value;
            }
        }
        throw new RuntimeException("不可能到这里的...");
    }

    public static Object getValue(String fileName, String key) {
        // 首先加载配置文件
        loadYml(fileName);
        return getValue(key);
    }


    public static void main(String[] args) {
        System.out.println((getValue("application.yml", "server.port")));
    }
}
