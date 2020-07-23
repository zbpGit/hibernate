import info.joyc.generator.MyBatisMapperGenerator;

import java.util.HashMap;

public class TestGenerator {

    public static void main(String[] args) {
        MyBatisMapperGenerator myBatisMapperGenerator = new MyBatisMapperGenerator();
        myBatisMapperGenerator.setNoCurrent(true);
        myBatisMapperGenerator.setGroupName("info.joyc");
        myBatisMapperGenerator.setProjectName("test");
        //myBatisMapperGenerator.setTemplateConfig(new TemplateConfig().setController(null).setService(null).setServiceImpl(null).setEntity(null).setMapper(null));
        HashMap<String, String> tablePrefixMap = new HashMap<>();
        tablePrefixMap.put("bd", "basicdata");
        tablePrefixMap.put("sys", "system");
        myBatisMapperGenerator.setTablePrefixMap(tablePrefixMap);
//        myBatisMapperGenerator.setCreateFront(true); //是否生成VUE前端页面
        myBatisMapperGenerator.generateByTables(null);
    }
}
