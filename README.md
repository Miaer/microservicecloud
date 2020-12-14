# 微服务概述
业界大牛马丁.福勒（Martin Fowler） 这样描述微服务：
论文网址：https://martinfowler.com/articles/microservices.html

# 为什么要用微服务
关于微服务架构优点有很多讨论。但是，个人认为许多优点都可以算作一些"伪优点"。例如：
- 从单个服务的角度而言，微服务的每个服务都很简单，只关注于一个业务功能，降低了单个服务的复杂性。但是，从整体而言，作为一种分布式系统，
微服务引入额外的复杂性和问题，比如说网络延迟、容错性、异步、分布式事务等。
- 从单个服务的角度而言，每个微服务可以通过不同的编程语言与工具进行开发，针对不同的服务采用更加合适的技术，也可以快速地尝试一些新技术。
- 但是，从整个公司的角度来说，往往希望能够尽量统一技术栈，避免重复投资某些技术。假设某公司主要用Spring Boot和Spring Cloud来构建微服务，
使用Netflix Hystrix作为服务熔断的解决方案。后来，一些微服务开始使用Node.js来实现。很快，
该公司就发觉使用Node.js构建的服务无法使用已有的服务熔断解决方案，需要为Node.js技术栈重新开发。
- ……

微服务架构的核心就是解决**扩展性**的问题。从组织结构的角度来看，微服务架构使得研发部门可以快速扩张，因为每个微服务都不是特别复杂，
工作在这个服务上的研发人员不是必须对整个系统都充分了解，很多新人可以快速上手。

从技术的角度来看，微服务架构使得每个微服务可以独立部署、独立扩展，可以根据每个服务的规模来部署满足需求的规模，选择更适合于服务资源需求的硬件。

## 微服务
强调的是服务的大小，它关注的是某一个点，是具体解决某一个问题/提供落地对应服务的一个服务应用，
狭意的看,可以看作Eclipse里面的一个个微服务工程/或者Module。

## 微服务架构
微服务架构是⼀种架构模式，它提倡将单⼀应⽤程序划分成⼀组⼩的服务，服务之间互相协调、互相配合，为⽤户提供最终价值。每个服务运⾏在其独⽴的进程中，
服务与服务间采⽤轻量级的通信机制互相协作（通常是基于HTTP协议的RESTful API）。
每个服务都围绕着具体业务进⾏构建，并且能够被独⽴的部署到⽣产环境、类⽣产环境等。
另外，应当尽量避免统⼀的、集中式的服务管理机制，对具体的⼀个服务⽽⾔，应根据业务上下⽂，选择合适的语⾔、⼯具对其进⾏构建。

# 微服务技术栈
- 微服务条目落地技术包括服务开发Springboot、Spring、SpringMVC
- 服务配置与管理Netflix公司的Archaius、阿里的Diamond等；
- 服务注册与发现Eureka、Consul、Zookeeper等；
- 服务调用Rest、RPC、gRPC；
- 服务熔断器Hystrix、Envoy等；
- 负载均衡Ribbon、Nginx等；
- 服务接口调用(客户端调用服务的简化工具)Feign等；
- 消息队列Kafka、RabbitMQ、ActiveMQ等；
- 服务配置中心管理SpringCloudConfig、Chef等；
- 服务路由（API网关）Zuul等；
- 服务监控Zabbix、Nagios、Metrics、Spectator等；
- 全链路追踪Zipkin，Brave、Dapper等；
- 服务部署Docker、OpenStack、Kubernetes等；
- 数据流操作开发包SpringCloud Stream（封装与Redis,Rabbit、Kafka等发送接收消息）；
- 事件消息总线Spring Cloud Bus......

# spring cloud
基于SpringBoot提供了一套微服务解决方案，包括服务注册与发现，配置中心，全链路监控，服务网关，负载均衡，熔断器等组件，
除了基于NetFlix的开源组件做高度抽象封装之外，还有一些选型中立的开源组件。
SpringCloud利用SpringBoot的开发便利性巧妙地简化了分布式系统基础设施的开发，SpringCloud为开发人员提供了快速构建分布式系统的一些工具，
包括配置管理、服务发现、断路器、路由、微代理、事件总线、全局锁、决策竞选、分布式会话等等,它们都可以用SpringBoot的开发风格做到一键启动和部署。
SpringBoot并没有重复制造轮子，它只是将目前各家公司开发的比较成熟、经得起实际考验的服务框架组合起来，
通过SpringBoot风格进行再封装屏蔽掉了复杂的配置和实现原理，最终给开发者留出了一套简单易懂、易部署和易维护的分布式系统开发工具包

SpringCloud = 分布式微服务架构下的一站式解决方案，是各个微服务架构落地技术的集合体，俗称**微服务全家桶**

# spring cloud和spring boot
SpringBoot专注于快速方便的开发单个个体微服务。
SpringCloud是关注全局的微服务协调整理治理框架，它将SpringBoot开发的一个个单体微服务整合并管理起来，
为各个微服务之间提供，配置管理、服务发现、断路器、路由、微代理、事件总线、全局锁、决策竞选、分布式会话等等集成服务
SpringBoot可以离开SpringCloud独立使用开发项目，但是SpringCloud离不开SpringBoot，属于依赖的关系.
SpringBoot专注于快速、方便的开发单个微服务个体，SpringCloud关注全局的服务治理框架。

# spring cloud和double
最大区别：SpringCloud抛弃了Dubbo的RPC通信，采用的是基于HTTP的REST方式。
严格来说，这两种方式各有优劣。虽然从一定程度上来说，后者牺牲了服务调用的性能，但也避免了上面提到的原生RPC带来的问题。
而且REST相比RPC更为灵活，服务提供方和调用方的依赖只依靠一纸契约，不存在代码级别的强依赖，这在强调快速演化的微服务环境下，显得更加合适。

# 项目概述
本项目是微服务通用案例。集中学习微服务Consumer消费者（Client）通过REST调用Provider提供者（Server）提供的服务。项目结构如下：
- microservicecloud-api(封装的整体Entity/接口/公共配置等)
- microservicecloud-provider-dept-8001(微服务落地的服务提供者)
- microservicecloud-consumer-dept-80(微服务调用的客户端使用)
- microservicecloud-eureka-7001(eureka服务端)
