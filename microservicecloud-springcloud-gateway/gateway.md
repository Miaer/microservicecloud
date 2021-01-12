# Gateway网关
Spring Cloud Gate基于Spring Boot 2.x， Spring WebFlux和项目Reactor 构建。因此，使用Spring Cloud网关时，
许多熟悉的同步库（例如，Spring Data和Spring Security）和模式可能不适用。
它不能在传统的Servlet容器中或作为WAR构建*不可与servlet*同用！！！如spring web，mvc等

# 词汇表
路由：route是Gateway的基本构建模块。是由ID，目标URI，谓词集合和过滤器集合定义。如果聚合谓词为true，则匹配路由。如下所示：

    spring:
      application:
        name: gateway-01
      cloud:
        gateway:
          routes:
            # 路径匹配
            - id: path_route    ＃　ID
              uri: https://example.org　　＃ URI
              predicates:  # 谓词
                - Path=/red/{segment},/blue/{segment} # 过滤器

谓词：predicates。这是 Java 8 Function谓词。输入类型为 Spring.Framework.ServerWebExchange。这使开发人员可以*匹配HTTP请求中的任何内容*，例如请求头或请求参数。拦截哪些请求。

过滤器：Filter。这些是使用特定工厂构造的实例。Spring.Framework.GatewayFilter。在此，可以在发送下游请求之前或之后*修改请求和响应*。可以对拦截的请求，可以做的处理。

# 工作原理
![blockchain](..\images\springCloudGateway原理.png)

客户端向Spring Cloud网关发出请求。如果网关处理程序映射确定请求与路由匹配，则将其发送到网关Web处理程序。该处理程序运行通过特定于请求的筛选器链发送请求。筛选器由虚线分隔的原因是，筛选器可以在发送代理请求之前或之后执行逻辑。执行所有“前置”过滤器逻辑，然后发出代理请求。发出代理请求后，将执行“后”过滤器逻辑。
