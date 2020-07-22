package info.joyc.tool.http;

import info.joyc.tool.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * info.joyc.util.http.ClientAccessUtil.java
 * ==============================================
 * Copy right 2015-2018 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : 客户端访问工具类
 * @since : 2018-07-11 11:53
 */
public class ClientAccessUtil {

    /**
     * 获取Ip地址
     *
     * <p>
     * 一开始，当然是看自家Client有没传递真实地址
     * <p>
     * 首先，获取 X-Forwarded-For 中第0位的IP地址，它就是在HTTP扩展协议中能表示真实的客户端IP。具体就像这样：
     * <p>
     * X-Forwarded-For: client, proxy1, proxy2，proxy…
     * <p>
     * 所以你应该知道为什么要取第0位了吧！
     * <p>
     * 如果 X-Forwarded-For 获取不到，就去获取X-Real-IP ，X-Real-IP 获取不到，就依次获取Proxy-Client-IP 、WL-Proxy-Client-IP 、HTTP_CLIENT_IP 、 HTTP_X_FORWARDED_FOR 。最后获取不到才通过request.getRemoteAddr()获取IP，
     * <p>
     * X-Real-IP 就是记录请求的客户端真实IP。跟X-Forwarded-For 类似。
     * <p>
     * Proxy-Client-IP 顾名思义就是代理客户端的IP，如果客户端真实IP获取不到的时候，就只能获取代理客户端的IP了。
     * <p>
     * WL-Proxy-Client-IP 是在Weblogic下获取真实IP所用的的参数。
     * <p>
     * HTTP_CLIENT_IP 与 HTTP_X_FORWARDED_FOR 可以理解为X-Forwarded-For ， 因为它们是PHP中的用法。
     *
     * @param request 请求
     * @return ip
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("Client");
        if (StringUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (StringUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        } else {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                ip = ip.substring(0, index);
            }
        }
        if (StringUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }


    /**
     * 获取本地主机Ip地址
     */
    public static String getLocalHostIpAddress() {
        try {
            NetworkInterface netInterface = null;
            InetAddress inetAddr = null;
            InetAddress candidateAddress = null;
            Enumeration<InetAddress> addresses = null;
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                netInterface = allNetInterfaces.nextElement();
                addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    inetAddr = addresses.nextElement();
                    // 排除loopback类型地址
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就返回该地址
                            return inetAddr.getHostAddress();
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress.getHostAddress();
            }
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                return "127.0.0.1";
            }
            return jdkSuppliedAddress.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }
}
