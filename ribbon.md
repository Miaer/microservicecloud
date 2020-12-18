# Ribbon
Ribbon是Netflix发布的**客户端**的负载均衡(调用方选择如何负载均衡，还分为硬件的负载均衡-LVS 和服务器端的负载均衡-Nginx、HAProxy)

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

# 项目负载均衡情况
![blockchain](images\ribbon负载均衡.png)

# 负载均衡
load balance 负载均衡是在用于解决一台机器无法处理所有请求时产生的一种算法。

使用负载均衡带来的好处很明显：

- 当集群里的1台或者多台服务器down的时候，剩余的没有down的服务器可以保证服务的继续使用
- 使用了更多的机器保证了机器的良性使用，不会由于某一高峰时刻导致系统cpu急剧上升

负载均衡有好几种实现策略，常见的有：

- 随机 (Random)
- 轮询 (RoundRobin)
- 一致性哈希 (ConsistentHash)
- 哈希 (Hash)
- 加权（Weighted）

# ribbon的负载均衡

## ILoadBalance 负载均衡器
ribbon是一个为客户端提供负载均衡功能的服务，它内部提供了一个叫做ILoadBalance的接口代表负载均衡器的操作，比如有添加服务器操作、选择服务器操作、获取所有的服务器列表、获取可用的服务器列表等等。ILoadBalance的继承关系如下：