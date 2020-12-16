package rules;

import com.netflix.loadbalancer.IRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义Ribbon的负载均衡算法配置类
 * 注意：该类不可再主应用程序上下文的@ComponentScan所扫描的包中！！！
 * @Author mrliz
 * @Date 2020/12/10 23:19
 */
@Configuration
public class SelfRandomRule {
    @Bean
    public IRule myRule(){
        // AbstractLoadBalancerRule下有继承的各种算法
        return new RandomRule();
    }
}
