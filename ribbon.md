# Ribbon
Ribbon是Netflix发布的**客户端**的负载均衡

Ribbon与Eureka配合使用时，Ribbon可自动从EurekaServer获取服务提供者地址列表，并基于负载均衡算法，请求其中一个服务提供者实例。
![blockchain](images\ribbon和eureka架构.png)

# 使用负载均衡
整合只需要在RestTemplate上加上@LoadBalanced注解，即可为RestTemplate整合Ribbon，使其具有负载均衡的能力。在客户端，也就是调用方的地方加该注解

    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate()
    {
        return new RestTemplate();
    }
    
算法配置类
    
    @Configuration
    public class SelfRandomRule {
        @Bean
        public IRule myRule(){
            // AbstractLoadBalancerRule下有继承的各种算法
            return new RandomRule();
        }
    }

在主启动类上，开启负载均衡并指定连接的微服务和负载均衡使用的算法配置类

    @RibbonClient(name = "MICROSERVICECLOUD-DEPT",configuration = SelfRandomRule.class)

# 全局配置
如果要对所有的Ribbon Client提供默认的配置，则可以在**yml文件中**使用属性自定义的方式。这种方式比Java代码更加方便。
**Spring cloud CamdenRELEASE以上版本**
支持的属性如下，配置的前缀是<clientName>.ribbon.。其中<clientName>是Ribbon Client的名称，**如果省略，则表示全局配置**。

- NFLoadBalancerClassName: 配置ILoadBalancer的实现类
- NFLoadBalancerRuleClassName：配置IRule的实现类
- NFLoadBalancerPingClassName：配置IPing的实现类
- NIWSServerListClassName：配置ServerList的实现类
- NIWSServerListFilterClassName：配置ServerListFilter的实现类
    
这样就可以将名为microservice-provider-user的Ribbon Client的负载均衡规则设为随机：

    microservice-provider-user:
        ribbon:
            NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule
            
若配置成如下形式，则表示对所有Ribbon Client都使用RandomRule：

    ribbon:
        NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule
