
# Eureka是什么
Eureka是Netflix的一个子模块，也是核心模块之一。Eureka是一个基于REST的服务，用于定位服务，以实现云端中间层服务发现和故障转移。
服务注册与发现对于微服务架构来说是非常重要的，有了服务发现与注册，只需要使用服务的标识符，就可以访问到服务，而不需要修改服务调用的配置文件了。
功能类似于dubbo的注册中心，比如Zookeeper。

# Eureka基本架构
![blockchain](images\eureka架构.png)
- Spring Cloud 封装了 Netflix 公司开发的 Eureka 模块来实现服务注册和发现(请对比Zookeeper)。
- Eureka 采用了 C-S 的设计架构。
- Eureka Server 作为服务注册功能的服务器，它是服务注册中心。
- 而系统中的其他微服务，使用 Eureka 的客户端连接到 Eureka Server并维持心跳连接。
这样系统的维护人员就可以通过 Eureka Server 来监控系统中各个微服务是否正常运行。
SpringCloud 的一些其他模块（比如Zuul）就可以通过 Eureka Server 来发现系统中的其他微服务，并执行相关的逻辑。
- 请注意和Dubbo的架构对比
- Eureka包含两个组件：Eureka Server和Eureka Client。Eureka Server提供服务注册服务。各个节点启动后，会在EurekaServer中进行注册，
  这样EurekaServer中的服务注册表中将会存储所有可用服务节点的信息，服务节点的信息可以在界面中直观的看到。
- EurekaClient是一个Java客户端，用于简化Eureka Server的交互，客户端同时也具备一个内置的、使用轮询(round-robin)负载算法的负载均衡器。
在应用启动后，将会向Eureka Server发送心跳(默认周期为30秒)。如果Eureka Server在多个心跳周期内没有接收到某个节点的心跳，
EurekaServer将会从服务注册表中把这个服务节点移除（默认90秒） 

# 自我包含机制
自我保护机制主要在Eureka Client和Eureka Server之间存在网络分区的情况下发挥保护作用，在服务器端和客户端都有对应实现。
假设在某种特定的情况下（如网络故障）, Eureka Client和Eureka Server无法进行通信，此时Eureka Client无法向Eureka Server发起**注册和续约**请求，
Eureka Server中就可能因注册表中的服务实例租约出现大量过期而面临被剔除的危险，然而此时的Eureka Client可能是处于健康状态的（可接受服务访问），
如果直接将注册表中大量过期的服务实例租约剔除显然是不合理的。自我保护机制提高了eureka的服务可用性。当自我保护机制触发时，
Eureka不再从注册列表中移除因为长时间没收到心跳而应该过期的服务，仍能查询服务信息并且接受新服务注册请求，也就是其他功能是正常的。这里思考下，
如果eureka节点A触发自我保护机制过程中，有新服务注册了然后网络回复后，其他peer节点能收到A节点的新服务信息，
数据同步到peer过程中是有网络异常重试的，也就是说，是能保证**最终一致性**的。

# 服务发现原理
eureka server可以集群部署，多个节点之间会进行（异步方式）数据同步，保证数据最终一致性，Eureka Server作为一个开箱即用的服务注册中心，
提供的功能包括：服务注册、接收服务心跳、服务剔除、服务下线等。需要注意的是，Eureka Server同时也是一个Eureka Client，
在不禁止Eureka Server的客户端行为时，它会向它配置文件中的其他Eureka Server进行拉取注册表、服务注册和发送心跳等操作。
eureka server端通过**appName**和**instanceInfoId**来唯一区分一个服务实例，服务实例信息是保存在哪里呢？其实就是一个Map中：
`
// 第一层的key是appName，第二层的key是
instanceInfoIdprivate final ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>> registry = new ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>>();
`
# 服务注册
Service Provider启动时会将服务信息（InstanceInfo）发送给eureka server，eureka server接收到之后会写入registry中，
服务注册默认过期时间DEFAULT_DURATION_IN_SECS = 90秒。
InstanceInfo写入本地registry，服务信息（InstanceInfo）保存在Lease中。写入本地registry对应方法com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl#register，
Lease统一保存在内存的ConcurrentHashMap中，在服务注册过程中，首先加个读锁，然后从registry中判断该Lease是否已存在，如果已存在则比较lastDirtyTimestamp时间戳，
取二者最大的服务信息，避免发生数据覆盖。InstanceInfo写入到本地registry之后，然后同步给其他peer节点，对应方法com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl#replicateToPeers。
如果当前节点接收到的InstanceInfo本身就是另一个节点同步来的，则不会继续同步给其他节点，避免形成“广播效应”；InstanceInfo同步时会排除当前节点。

InstanceInfo的状态有依以下几种：Heartbeat, Register, Cancel, StatusUpdate,DeleteStatusOverride，默认情况下同步操作时批量异步执行的，
同步请求首先缓存到Map中，key为requestType+appName+id，然后由发送线程将请求发送到peer节点。

Peer之间的状态是采用异步的方式同步的，所以不保证节点间的状态一定是一致的，不过基本能保证**最终状态是一致的**。实际上也并不需要节点间的状态强一致。
在一段时间内（比如30秒），节点A比节点B多一个服务实例或少一个服务实例，在业务上也是完全可以接受的（Service Consumer侧一般也会实现错误重试和负载均衡机制）。
所以按照C(一致性)A(高可用)P(分区容错)理论，Eureka的选择就是放弃C，选择AP。如果同步过程中，出现了异常怎么办呢，这时会根据异常信息做对应的处理，
如果是读取超时或者网络连接异常，则稍后重试；如果其他异常则打印错误日志不再后续处理。

- 服务注册机制
  
服务提供者、服务消费者、以及服务注册中心自己，启动后都会向注册中心注册服务（如果配置了注册）。下图是介绍如何完成服务注册的：

![blockchain](images\eureka服务注册机制.png)

注册中心服务接收到 register 请求后：

1. 保存服务信息，将服务信息保存到 registry 中；
2. 更新队列，将此事件添加到更新队列中，供 Eureka Client 增量同步服务信息使用。
3. 清空二级缓存，即 readWriteCacheMap，用于保证数据的一致性。
4. 更新阈值，供剔除服务使用。
5. 同步服务信息，将此事件同步至其他的 Eureka Server 节点。

# 服务续约
Renew（服务续约）操作由Service Provider定期调用，类似于heartbeat。主要是用来告诉Eureka Server Service Provider还活着，避免服务被剔除掉。
renew接口实现方式和register基本一致：首先更新自身状态，再同步到其它Peer，服务续约也就是把过期时间设置为当前时间加上duration的值。
注意：服务注册如果InstanceInfo不存在则加入，存在则更新；而服务预约只是进行更新，如果InstanceInfo不存在直接返回false。

# 服务续约机制
  服务注册后，要定时（默认 30S，可自己配置）向注册中心发送续约请求，告诉注册中心“我还活着”。
  
  ![blockchain](images\eureka服务续约机制.png)
  
注册中心收到续约请求后：
1. 更新服务对象的最近续约时间，即 Lease 对象的 lastUpdateTimestamp;
2. 同步服务信息，将此事件同步至其他的 Eureka Server 节点。

剔除服务之前会先判断服务是否已经过期，判断服务是否过期的条件之一是续约时间和当前时间的差值是不是大于阈值。

# 服务下线
Cancel（服务下线）一般在Service Provider shutdown的时候调用，用来把自身的服务从Eureka Server中删除，以防客户端调用不存在的服务，
eureka从本地”删除“（设置为删除状态）之后会同步给其他peer，对应方法com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl#cancel。

# 服务注销机制
  服务正常停止之前会向注册中心发送注销请求，告诉注册中心“我要下线了”。
  ![blockchain](images\eureka服务注销机制.png)

注册中心服务接收到 cancel 请求后：

1. 删除服务信息，将服务信息从 registry 中删除；
2. 更新队列，将此事件添加到更新队列中，供 Eureka Client 增量同步服务信息使用。
3. 清空二级缓存，即 readWriteCacheMap，用于保证数据的一致性。
4. 更新阈值，供剔除服务使用。
5. 同步服务信息，将此事件同步至其他的 Eureka Server 节点。

服务正常停止才会发送 Cancel，如果是非正常停止，则不会发送，此服务由 Eureka Server 主动剔除。

# 服务失效剔除
Eureka Server中有一个EvictionTask，用于检查服务是否失效。
Eviction（失效服务剔除）用来定期（默认为每60秒）在Eureka Server检测失效的服务，检测标准就是超过一定时间没有Renew的服务。
默认失效时间为90秒，也就是如果有服务超过90秒没有向Eureka Server发起Renew请求的话，就会被当做失效服务剔除掉。
失效时间可以通过eureka.instance.leaseExpirationDurationInSeconds进行配置，定期扫描时间可以通过eureka.server.evictionIntervalTimerInMs进行配置。
服务剔除#evict方法中有很多限制，都是为了保证Eureka Server的可用性：
比如自我保护时期不能进行服务剔除操作、过期操作是分批进行、服务剔除是随机逐个剔除，剔除均匀分布在所有应用中，防止在同一时间内同一服务集群中的服务全部过期被剔除，以致大量剔除发生时，在未进行自我保护前促使了程序的崩溃。

# 服务剔除机制
  Eureka Server 提供了服务剔除的机制，用于剔除没有正常下线的服务。
  ![blockchain](images\eureka服务剔除机制.png)
  
服务的剔除包括三个步骤，首先判断是否满足服务剔除的条件，然后找出过期的服务，最后执行剔除。

- 判断是否满足服务剔除的条件
  1. 有两种情况可以满足服务剔除的条件：
  2. 关闭了自我保护。如果开启了自我保护，需要进一步判断是 Eureka Server 出了问题，还是 Eureka Client 出了问题，如果是 Eureka Client 出了问题则进行剔除。
  
这里比较核心的条件是自我保护机制，Eureka 自我保护机制是为了防止误杀服务而提供的一个机制。
Eureka 的自我保护机制“谦虚”的认为如果大量服务都续约失败，则认为是自己出问题了（如自己断网了），也就不剔除了；
反之，则是 Eureka Client 的问题，需要进行剔除。
而自我保护阈值是区分 Eureka Client 还是 Eureka Server 出问题的临界值：如果超出阈值就表示大量服务可用，少量服务不可用，则判定是 Eureka Client 出了问题。

如果未超出阈值就表示大量服务不可用，则判定是 Eureka Server 出了问题。

条件 1 中如果关闭了自我保护，则统统认为是 Eureka Client 的问题，把没按时续约的服务都剔除掉（这里有剔除的最大值限制）。

这里比较难理解的是阈值的计算：

- 自我保护阈值 = 服务总数 * 每分钟续约数 * 自我保护阈值因子。
- 每分钟续约数 =（60S/ 客户端续约间隔）

最后自我保护阈值的计算公式为：
自我保护阈值 = 服务总数 * （60S/ 客户端续约间隔） * 自我保护阈值因子。
举例：如果有 100 个服务，续约间隔是 30S，自我保护阈值 0.85。
自我保护阈值 =100 * 60 / 30 * 0.85 = 170。
如果上一分钟的续约数 =180>170，则说明大量服务可用，是服务问题，进入剔除流程；
如果上一分钟的续约数 =150<170，则说明大量服务不可用，是注册中心自己的问题，进入自我保护模式，不进入剔除流程。

- 找出过期的服务
    
    遍历所有的服务，判断上次续约时间距离当前时间大于阈值就标记为过期。并将这些过期的服务保存到集合中。

- 剔除服务

    在剔除服务之前先计算剔除的数量，然后遍历过期服务，通过洗牌算法确保每次都公平的选择出要剔除的任务，最后进行剔除。

执行剔除服务后：

1. 删除服务信息，从 registry 中删除服务。
2. 更新队列，将当前剔除事件保存到更新队列中。
3. 清空二级缓存，保证数据的一致性。

# 服务信息拉取
Eureka consumer服务信息的拉取分为全量式拉取和增量式拉取，
eureka consumer启动时进行全量拉取，运行过程中由定时任务进行增量式拉取，
如果网络出现异常，可能导致先拉取的数据被旧数据覆盖（比如上一次拉取线程获取结果较慢，数据已更新情况下使用返回结果再次更新，导致数据版本落后），产生脏数据。
对此，eureka通过类型AtomicLong的fetchRegistryGeneration对数据版本进行跟踪，版本不一致则表示此次拉取到的数据已过期。
fetchRegistryGeneration过程是在拉取数据之前，执行fetchRegistryGeneration.get获取当前版本号，获取到数据之后，
通过fetchRegistryGeneration.compareAndSet来判断当前版本号是否已更新。注意：如果增量式更新出现意外，会再次进行一次全量拉取更新。

# 服务获取机制
  Eureka Client 获取服务有两种方式，全量同步和增量同步。获取流程是根据 Eureka Server 的多层数据结构进行的：
  ![blockchain](images\eureka服务获取机制.png)
  
  无论是全量同步还是增量同步，都是先从缓存中获取，如果缓存中没有，则先加载到缓存中，再从缓存中获取。（registry 只保存数据结构，缓存中保存 ready 的服务信息。）
  
  - 先从一级缓存中获取
  
    a> 先判断是否开启了一级缓存
    
    b> 如果开启了则从一级缓存中获取，如果存在则返回，如果没有，则从二级缓存中获取
    
    c> 如果未开启，则跳过一级缓存，从二级缓存中获取
  
  - 再从二级缓存中获取
  
    a> 如果二级缓存中存在，则直接返回；
    
    b> 如果二级缓存中不存在，则先将数据加载到二级缓存中，再从二级缓存中获取。

注意加载时需要判断是增量同步还是全量同步，增量同步从 recentlyChangedQueue 中 load，全量同步从 registry 中 load。

# Eureka server的伸缩
- Service Provider
  Service Provider启动时首先时注册到Eureka Service上，这样其他消费者才能进行服务调用，除了在启动时之外，只要实例状态信息有变化，
  也会注册到Eureka Service。需要注意的是，需要确保配置eureka.client.registerWithEureka=true。
  register逻辑在方法AbstractJerseyEurekaHttpClient.register中，Service Provider会依次注册到配置的Eureka Server Url上，
  如果注册出现异常，则会继续注册其他的url。Renew操作会在Service Provider端定期发起，用来通知Eureka Server自己还活着。
  这里instance.leaseRenewalIntervalInSeconds属性表示Renew频率。默认是30秒，也就是每30秒会向Eureka Server发起Renew操作。这部分逻辑在HeartbeatThread类中。
  在Service Provider服务shutdown的时候，需要及时通知Eureka Server把自己剔除，从而避免客户端调用已经下线的服务，逻辑本身比较简单，
  通过对方法标记@PreDestroy，从而在服务shutdown的时候会被触发。
- Service Consumer
    Service Consumer这块的实现相对就简单一些，因为它只涉及到从Eureka Server获取服务列表和更新服务列表。Service Consumer在启动时会从Eureka Server获取所有服务列表，
    并在本地缓存。需要注意的是，需要确保配置eureka.client.shouldFetchRegistry=true。由于在本地有一份Service Registries缓存，所以需要定期更新，
    定期更新频率可以通过eureka.client.registryFetchIntervalSeconds配置。
    
# 服务同步机制
服务同步机制是用来同步 Eureka Server 节点之间服务信息的。
它包括 Eureka Server 启动时的同步，和运行过程中的同步。

- 启动时的同步 

![blockchain](images\eureka启动时同步.png)    

Eureka Server 启动后，遍历 eurekaClient.getApplications 获取服务信息，并将服务信息注册到自己的 registry 中。

注意这里是两层循环，第一层循环是为了保证已经拉取到服务信息，第二层循环是遍历拉取到的服务信息。    

- 运行过程中的同步
![blockchain](images\eureka运行过程中同步.png)    

当 Eureka Server 节点有 register、renew、cancel 请求进来时，会将这个请求封装成 TaskHolder 放到 acceptorQueue 队列中，然后经过一系列的处理，放到 batchWorkQueue 中。

TaskExecutor.BatchWorkerRunnable是个线程池，不断的从 batchWorkQueue 队列中 poll 出 TaskHolder，然后向其他 Eureka Server 节点发送同步请求。

这里省略了两个部分：

- 一个是在 acceptorQueue 向 batchWorkQueue 转化时，省略了中间的 processingOrder 和 pendingTasks 过程。

- 另一个是当同步失败时，会将失败的 TaskHolder 保存到 reprocessQueue 中，重试处理。

# 数据存储结构
- ZK 是将服务信息保存在树形节点上。而下面是 Eureka 的数据存储结构：
![blockchain](images\eureka数据存储.png)

Eureka 的数据存储分了两层：数据存储层和缓存层。
Eureka Client 在拉取服务信息时，先从缓存层获取（相当于 Redis），如果获取不到，先把数据存储层的数据加载到缓存中（相当于 Mysql），再从缓存中获取。
值得注意的是，数据存储层的数据结构是服务信息，而缓存中保存的是经过处理加工过的、可以直接传输到 Eureka Client 的数据结构。
Eureka 这样的数据结构设计是把内部的数据存储结构与对外的数据结构隔离开了，就像是我们平时在进行接口设计一样，对外输出的数据结构和数据库中的数据结构往往都是不一样的。
- 数据存储层
  这里为什么说是存储层而不是持久层？因为 rigistry 本质上是一个双层的 ConcurrentHashMap，存储在内存中的。
  第一层的 key 是spring.application.name，value 是第二层 ConcurrentHashMap；
  第二层 ConcurrentHashMap 的 key 是服务的 InstanceId，value 是 Lease 对象
  Lease 对象包含了服务详情和服务治理相关的属性。
- 二级缓存层
    Eureka 实现了二级缓存来保存即将要对外传输的服务信息，数据结构完全相同。
    一级缓存：ConcurrentHashMap<Key,Value> readOnlyCacheMap，本质上是 HashMap，无过期时间，保存服务信息的对外输出数据结构。
    二级缓存：Loading<Key,Value> readWriteCacheMap，本质上是 guava 的缓存，包含失效机制，保存服务信息的对外输出数据结构。
# 更新机制
下面是缓存的更新机制：
![blockchain](images\eureka缓存更新机制.png)

更新机制包含删除和加载两个部分，上图黑色箭头表示删除缓存的动作，绿色表示加载或触发加载的动作。
- 删除二级缓存

    Eureka Client 发送 register、renew 和 cancel 请求并更新 registry 注册表之后，删除二级缓存；
    Eureka Server 自身的 Evict Task 剔除服务后，删除二级缓存；
    二级缓存本身设置了 guava 的失效机制，隔一段时间后自己自动失效；

- 加载二级缓存：

    Eureka Client 发送 getRegistry 请求后，如果二级缓存中没有，就触发 guava 的 load，即从 registry 中获取原始服务信息后进行处理加工，再加载到二级缓存中。
    Eureka Server 更新一级缓存的时候，如果二级缓存没有数据，也会触发 guava 的 load。
    
- 更新一级缓存：
    
    Eureka Server 内置了一个 TimerTask，定时将二级缓存中的数据同步到一级缓存（这个动作包括了删除和加载）。
    
# Eureka和zookeeper

             Zookeeper | Eureka
    设计原则 | CP        | AP
       优点 | 数据强一致  | 服务高可用
       缺点 |网络分区会影响 Leader 选举，超过阈值后集群不可用 | 服务节点间的数据可能不一致,Client-Server 间的数据可能不一致
    适用场景| 单机房集群，对数据一致性要求较高 | 云机房集群，跨越多机房部署；对注册中心服务可用性要求较高    


# eureka有哪些不足
eureka consumer本身有缓存，服务状态更新滞后，最常见的状况就是，服务下线了但是服务消费者还未及时感知，此时调用到已下线服务会导致请求失败，只能依靠consumer端的容错机制来保证。

