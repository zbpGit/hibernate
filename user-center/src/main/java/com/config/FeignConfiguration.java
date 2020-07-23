package com.config;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RoundRobinRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {

    @Bean
    public IRule myRule() {
        return new RoundRobinRule();//轮询算法
        //return new RandomRule();//随机算法
        //return new RetryRule();//首先轮询，轮询发现错误的会再次重试，几次之后会自动跳过错误的请求
        //return new MyRandomRule();
    }

}
