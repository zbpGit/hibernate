package info.joyc.tool.http;

import info.joyc.tool.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

/**
 * info.joyc.util.http.CommunicationRequestUtil.java
 * ==============================================
 * Copy right 2015-2018 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : 通讯请求工具类
 * @since : 2018/1/17 17:08
 */
public class CommunicationRequestUtil {

    private static final Logger log = LoggerFactory.getLogger(CommunicationRequestUtil.class);

    public final static String REQUEST_METHOD_GET = "GET";

    public final static String REQUEST_METHOD_POST = "POST";

    /**
     * 发起http请求并获取结果
     *
     * @param requestUrl    请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param properties    请求头
     * @param outputStr     提交的数据
     * @param encoding      编码格式
     * @return JSONObject(通过JSONObject.get ( key)的方式获取json对象的属性值)
     */
    public static String httpRequest(String requestUrl, String requestMethod, Map<String, String> properties, String outputStr, String encoding) {
        if (StringUtil.isBlank(encoding)) {
            encoding = "UTF-8";
        }
        String jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();

            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod(requestMethod);
            // 设置请求头
            if (properties != null && properties.size() > 0) {
                for (String propertyKey : properties.keySet()) {
                    httpUrlConn.setRequestProperty(propertyKey, properties.get(propertyKey));
                }
            }

            if (REQUEST_METHOD_GET.equalsIgnoreCase(requestMethod)) {
                httpUrlConn.connect();
            }

            // 当有数据需要提交时
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(outputStr.getBytes(encoding));
                outputStream.close();
            }
            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, encoding);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            httpUrlConn.disconnect();
            jsonObject = buffer.toString();
        } catch (ConnectException ce) {
            log.error("http server connection timed out.");
        } catch (Exception e) {
            log.error("http request error:{}", e);
        }
        return jsonObject;
    }

    /**
     * 发起http请求并获取结果
     *
     * @param requestUrl    请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param properties    请求头
     * @param map           提交的数据
     * @param encoding      编码格式
     * @return JSONObject(通过JSONObject.get ( key)的方式获取json对象的属性值)
     */
    public static String httpRequest(String requestUrl, String requestMethod, Map<String, String> properties, Map<String, Object> map, String encoding) {
        String outputStr = getMapString(map);
        return httpRequest(requestUrl, requestMethod, properties, outputStr, encoding);
    }

    private static String getMapString(Map<String, Object> param) {
        if (param != null && param.size() > 0) {
            StringBuilder params = new StringBuilder();
            Iterator<String> sets = param.keySet().iterator();
            while (sets.hasNext()) {
                String key = sets.next();
                params.append(key).append("=").append(param.get(key));
                if (sets.hasNext()) {
                    params.append("&");
                }
            }
            return params.toString();
        } else return "";
    }

    /**
     * 发起https请求并获取结果
     *
     * @param requestUrl    请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param outputStr     提交的数据
     * @return JSONObject(通过JSONObject.get ( key)的方式获取json对象的属性值)
     */
    public static String httpsRequest(String requestUrl, String requestMethod, String outputStr) {
        String jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = {new MyX509TrustManager()};
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);

            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod(requestMethod);

            if (REQUEST_METHOD_GET.equalsIgnoreCase(requestMethod)) {
                httpUrlConn.connect();
            }

            // 当有数据需要提交时
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }

            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            httpUrlConn.disconnect();
            jsonObject = buffer.toString();
        } catch (ConnectException ce) {
            log.error("https server connection timed out.");
        } catch (Exception e) {
            log.error("https request error:{}", e);
        }
        return jsonObject;
    }
}
