package info.joyc.tool.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * info.joyc.util.json.JacksonMapper.java
 * ==============================================
 * Copy right 2015-2018 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : jackson Mapper 对象
 * @since : 2018-02-24 17:00
 */
public class JacksonMapper {

    /**
     * can reuse, share globally
     */
    private static final ObjectMapper object = new ObjectMapper();

    /**
     * can reuse, share globally
     */
    private static final XmlMapper xml = new XmlMapper();

    /**
     * private constructor
     */
    private JacksonMapper() {
    }

    /**
     * return a ObjectMapper that is singleton
     * @return
     */
    public static ObjectMapper getObjectMapper() {
        return object;
    }

    /**
     * return a XmlMapper that is singleton
     * @return
     */
    public static XmlMapper getXmlMapper() {
        return xml;
    }
}
