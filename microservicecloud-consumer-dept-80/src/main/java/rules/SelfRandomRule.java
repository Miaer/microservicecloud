package rules;

import com.netflix.loadbalancer.IRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
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
