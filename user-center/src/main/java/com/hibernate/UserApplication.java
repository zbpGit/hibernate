package com.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Set;

/**
 * com.cloud.UserApplication.java
 * ==============================================
 * Copy right 2015-2019 by http://www.rejoysoft.com
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : zhangbeiping
 * @version : v1.0.0
 * @desc : 用户服务
 * @since : 2020-07-23 17:40
 */
@Slf4j
@EnableDiscoveryClient  //(可以是其他注册中心)服务发现注解,@EnableEurekaClient只适用于Eureka作为注册中心
@SpringBootApplication
@MapperScan(basePackages = "com.hibernate.dao")
public class UserApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String hostname = InetAddress.getLocalHost().getHostAddress();
        InetAddress[] addresses = InetAddress.getAllByName(hostname);
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> objectNames = mBeanServer.queryNames(new ObjectName("*:type=Connector,*"), Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
        log.info("以下为程序API入口：");
        for (ObjectName objectName : objectNames) {
            String scheme = mBeanServer.getAttribute(objectName, "scheme").toString();
            String port = objectName.getKeyProperty("port");
            for (InetAddress addr : addresses) {
                String host = addr.getHostAddress();
                log.info("{}://{}:{}/swagger-ui.html", scheme, host, port);
                log.info("{}://{}:{}/actuator", scheme, host, port);
            }
        }
    }
}
