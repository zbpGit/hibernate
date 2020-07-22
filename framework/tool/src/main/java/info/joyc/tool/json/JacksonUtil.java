package info.joyc.tool.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

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
 * @desc : jackson工具类
 * @since : 2018-02-24 17:00
 */
public class JacksonUtil {

    private static final Logger log = LoggerFactory.getLogger(JacksonUtil.class);

    /**
     * Json To JsonNode
     *
     * @param json
     * @return
     */
    public static JsonNode parseJsonNode(String json) {
        ObjectMapper mapper = JacksonMapper.getObjectMapper();
        try {
            return mapper.readTree(json);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Object Convert
     * Support for JsonNode and Object
     *
     * @param object
     * @param toValueType
     * @return
     */
    public static <T> T convert(Object object, Class<T> toValueType) {
        ObjectMapper mapper = JacksonMapper.getObjectMapper();
        return mapper.convertValue(object, toValueType);
    }

    /**
     * Object To Json
     *
     * @param obj
     * @return
     */
    public static String beanToJson(Object obj) {
        ObjectMapper mapper = JacksonMapper.getObjectMapper();
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Json To Object
     *
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T jsonTobean(String json, Class<T> clazz) {
        ObjectMapper mapper = JacksonMapper.getObjectMapper();
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            log.error("IOException is " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * XML To Object
     *
     * @param xmlFile
     * @param cls
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T xmlToBean(File xmlFile, Class<T> cls) throws IOException {
        XmlMapper xml = JacksonMapper.getXmlMapper();
        return xml.readValue(xmlFile, cls);
    }

    /**
     * XML To Object
     *
     * @param xmlContent
     * @param valueTypeRef
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T xmlContentToBean(String xmlContent, TypeReference<?> valueTypeRef) throws IOException {
        XmlMapper xml = JacksonMapper.getXmlMapper();
        return xml.readValue(xmlContent, valueTypeRef);
    }

    /**
     * XML To Object
     *
     * @param xmlInputStream
     * @param cls
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T xmlToBean(InputStream xmlInputStream, Class<T> cls) throws IOException {
        XmlMapper xml = JacksonMapper.getXmlMapper();
        return xml.readValue(xmlInputStream, cls);
    }

    /**
     * XML To Object
     *
     * @param xmlContent
     * @param cls
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T xmlToBean(String xmlContent, Class<T> cls) throws IOException {
        XmlMapper xml = JacksonMapper.getXmlMapper();
        return xml.readValue(xmlContent, cls);
    }

    /**
     * Object To XML
     *
     * @param bean
     * @param <T>
     * @return
     * @throws JsonProcessingException
     */
    public static <T> String beanToXml(T bean) throws JsonProcessingException {
        XmlMapper xml = JacksonMapper.getXmlMapper();
        return xml.writeValueAsString(bean);
    }

    /**
     * Json To XML
     *
     * @param jsonStr
     * @return
     */
    public static String jsonToXml(String jsonStr) throws Exception {
        ObjectMapper mapper = JacksonMapper.getObjectMapper();
        XmlMapper xml = JacksonMapper.getXmlMapper();
        JsonNode root = mapper.readTree(jsonStr);
        return xml.writeValueAsString(root);
    }

    /**
     * JsonNode To XML
     *
     * @param jsonNode
     * @return
     */
    public static String jsonToXml(JsonNode jsonNode) throws Exception {
        XmlMapper xml = JacksonMapper.getXmlMapper();
        return xml.writeValueAsString(jsonNode);
    }

    /**
     * XML To Json
     *
     * @param xmlString
     * @return
     */
    public static String xmlToJson(String xmlString) {
        StringWriter w = new StringWriter();
        JsonParser jp;
        try {
            ObjectMapper mapper = JacksonMapper.getObjectMapper();
            XmlMapper xml = JacksonMapper.getXmlMapper();
            jp = xml.getFactory().createParser(xmlString);
            JsonGenerator jg = mapper.getFactory().createGenerator(w);
            while (jp.nextToken() != null) {
                jg.copyCurrentEvent(jp);
            }
            jp.close();
            jg.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return w.toString();
    }
}
