package info.joyc.core.aop;

import info.joyc.tool.http.ClientAccessUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * info.joyc.core.aop.BaseWebLogAspect.java
 * ==============================================
 * Copy right 2015-2017 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : 使用AOP统一处理Web请求日志
 * @since : 2017-12-04 15:48
 */
public abstract class BaseWebLogAspect {

    private static final Logger log = LoggerFactory.getLogger(BaseWebLogAspect.class);

    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    //@Pointcut("execution(public * info.joyc.*.web..*.*(..))")
    public abstract void webLog();

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        startTime.set(System.currentTimeMillis());
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 记录下请求内容
        log.info("URL : " + request.getRequestURL().toString());
        log.info("HTTP_METHOD : " + request.getMethod());
        log.info("IP : " + ClientAccessUtil.getIpAddress(request));
        log.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        log.info("ARGS : " + Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "webLog()", returning = "ret")
    public void doAfterReturning(Object ret) throws Throwable {
        if (ret != null && ret.toString().length() > 500) {
            ret = ret.toString().substring(0, 500);
        }
        //处理完请求，返回内容
        log.info("RESPONSE : " + ret);
        log.info("SPEND TIME : " + (System.currentTimeMillis() - startTime.get()));
        startTime.remove();
    }
}
