# 从零开始,手写RPC框架


### 引言
RPC概念与价值：简述远程过程调用（RPC）的基本概念、工作原理及其在构建分布式系统中的重要作用。 <br>
手写RPC框架的意义：探讨自研RPC框架对于深入理解分布式通信原理、定制化需求满足、技术栈掌握提升等方面的价值。<br>

### 总体设计
**架构概览**：RPC框架的整体架构，包括客户端、服务端、网络传输层、序列化层、注册中心、负载均衡策略等核心组件及其相互关系。 <br>
**技术选型**：阐述所选用的编程语言(Java)、网络库、序列化库、注册中心实现、日志库、测试工具等关键依赖。

### 核心功能实现
1. 客户端
   - **服务发现**：如何通过注册中心获取服务列表，以及实现服务订阅与更新机制。 
   - **请求发送**：构建RPC请求、选择服务提供者、封装网络请求、发送请求的具体步骤。项目通过Java8动态代理, 代理了Spring中被标记了@RpcConsumer的Bean，实现远程访问
   - **响应处理**：响应接收、反序列化、异常处理与结果返回的过程。
2. 服务端
   - **服务注册**：服务提供者如何向注册中心注册服务，包括服务信息的定义与发布。
   - **请求接收**：服务端如何监听并接收客户端请求，包括网络端口绑定、请求解析等环节。
   - **请求处理与响应**：服务端对请求的业务逻辑处理、结果序列化及响应发送过程。项目通过反射, 找出被标记@RpcProvider的接口实现类，通过方法签名和反序列化参数寻找并调用。
3. 网络传输层
   - **协议设计**：定义RPC通信协议，包括消息头、消息体结构、错误码等规范。
   - **网络通信**：采用何种网络库实现HTTP/TCP/UDP通信，如何处理连接管理、心跳检测、超时重试等问题。项目采用HTTP做为网络通信,框架采用SpringWeb、Netty两种方式，可以根据配置来选择。
4. 序列化层
   - **序列化方案**：选择合适的序列化库（如JSON、Protobuf、Thrift等），描述其优点及在框架中的应用。项目采用JSON做为序列化协议, 并手写支持了大部分Pojo的对象转换。
   - **编解码实现**：请求与响应数据的编码、解码逻辑。
5. 注册中心
   - **注册中心选型**：可采用的注册中心类型（如Zookeeper、Etcd、Consul等）及其特性。项目采用Zookeeper、registry-man (由作者实现的AP模型注册中心)。
   - **服务注册与发现机制**：服务提供者注册服务、消费者发现服务的具体实现。
6. 负载均衡
   - **负载均衡策略**：列举支持的负载均衡算法（如轮询、随机、权重、一致性哈希等），并解释其实现原理。
   - **策略选择与实现**：框架中集成并切换不同的负载均衡策略。 
7. 配置中心
   - **配置下中心选型**：可以采用的配置中心(如Nacos、Apollo等)及其应用场景。项目采用Apollo做为配置中心，自研配置中心还在研发中。
   

### 高级特性与优化
1. **服务治理**：介绍熔断、降级、限流、隔离等服务治理功能的设计与实现。 
2. **容错与高可用**：探讨如何实现服务端的故障检测、自动恢复、主备切换，以及客户端的重试、失败快速反馈等高可用机制。 
3. **性能优化**：分享在网络传输、序列化效率、并发处理等方面的优化实践与技巧。

### 测试与部署
1. **单元测试与集成测试**：描述如何编写覆盖各模块功能的测试用例，以及进行端到端的集成测试。 
2. **部署与运维**：提供框架的部署指南，包括环境准备、配置说明、服务启动与停止等操作步骤，以及日志监控、故障排查等运维建议。


### 关于Consumer的配置
| **配置项** | **值** | **功能说明** |
| --- | --- | --- |
| `server.port` | `8088` | 消费者端服务监听端口，用于接收和处理外部请求。 |
| `rpcman.zk.zkServer` | `localhost:2181` | ZooKeeper服务器地址，用于服务注册与发现、配置管理等分布式协调功能。 |
| `rpcman.zk.zkRoot` | `rpcman` | 在ZooKeeper中使用的根节点路径，用于组织RPC相关的节点数据。 |
| `rpcman.app.id` | `app1` | 应用ID，标识当前消费者所属的应用实例，参与服务发现与调用。 |
| `rpcman.app.namespace` | `public` | 配置命名空间，用于区分不同环境或模块的配置数据。 |
| `rpcman.app.env` | `dev` | 环境标识，如开发（dev）、测试（test）、生产（prod）等，影响资源配置和行为。 |
| `rpcman.app.version` | `1.0` | 应用版本号，便于管理和区分不同版本的服务实例。 |
| `rpcman.app.use-netty` | `false` | 是否使用Netty作为网络通信框架。此处设置为`false`，表示不使用Netty。 |
| `rpcman.consumer.retries` | `3` | 调用远程服务时的最大重试次数，遇到故障时尝试重新连接和发送请求。 |
| `rpcman.consumer.timeout` | `300` | 调用远程服务的超时时间（单位：毫秒），超过此时间仍未收到响应则视为失败。 |
| `rpcman.consumer.gray-ratio` | `33` | 灰度发布比例（0-100%），指定部分流量访问新版本服务，用于渐进式上线和风险控制。 |
| `rpcman.consumer.fault-limit` | `10` | 服务熔断阈值，在30秒内连续发生10次错误，则触发服务熔断，停止对该服务的请求。 |
| `rpcman.consumer.half-open-initial-delay` | `10000` | 服务熔断后进入半开状态的初始延迟时间（单位：毫秒），在此期间不会尝试重新连接。 |
| `rpcman.consumer.half-open-delay` | `60000` | 半开状态下每次尝试重新连接的间隔时间（单位：毫秒），用于探测服务是否恢复可用。 |
| `apollo.cacheDir` | `/opt/data/` | Apollo配置中心本地缓存目录，用于存储拉取的配置文件。 |
| `apollo.cluster` | `default` | Apollo集群名称，关联到特定的配置中心实例。 |
| `apollo.meta` | `http://localhost:8080` | Apollo配置中心元数据中心地址，用于获取服务列表和配置信息。 |
| `apollo.autoUpdateInjectedSpringProperties` | `true` | 是否自动更新注入到Spring Bean中的属性值，保持与配置中心同步。 |
| `apollo.bootstrap.enabled` | `true` | 是否启用Apollo的启动引导加载功能，确保应用启动时能及时获取配置。 |
| `apollo.bootstrap.namespaces` | `rpcman-app` | 启动引导加载的配置命名空间列表，优先加载这些命名空间的配置。 |
| `apollo.bootstrap.eagerLoad.enabled` | `false` | 是否启用配置的急切加载模式，即在初始化阶段就加载所有配置而非按需加载。 |
| `logging.level.root` | `error` | 全局日志级别，设定为`error`表示仅记录错误及以上级别的日志。 |
| `logging.level.cn.ipman.rpc` | `info` | 特定包（`cn.ipman.rpc`）的日志级别，设定为`info`表示记录信息、警告、错误及以上级别的日志。 |


### 关于Provider的配置
| **配置项** | **值** | **功能说明** |
| --- | --- | --- |
| `server.port` | `8080` | Provider端服务监听端口，用于对外提供RPC服务。 |
| `rpcman.zk.zkServer` | `localhost:2181` | ZooKeeper服务器地址，用于服务注册与发现、配置管理等分布式协调功能。 |
| `rpcman.zk.zkRoot` | `rpcman` | 在ZooKeeper中使用的根节点路径，用于组织RPC相关的节点数据。 |
| `rpcman.app.id` | `app1` | 应用ID，标识当前提供者所属的应用实例，参与服务注册与发现。 |
| `rpcman.app.namespace` | `public` | 配置命名空间，用于区分不同环境或模块的配置数据。 |
| `rpcman.app.env` | `dev` | 环境标识，如开发（dev）、测试（test）、生产（prod）等，影响资源配置和行为。 |
| `rpcman.app.version` | `1.0` | 应用版本号，便于管理和区分不同版本的服务实例。 |
| `rpcman.app.use-netty` | `true` | 是否使用Netty作为网络通信框架。此处设置为`true`，并将在`server.port`基础上增加1000作为实际开放端口。 |
| `rpcman.provider.metas.dc` | `bj` | 数据中心标识（dc），用于区分服务所在的地理位置或数据中心。 |
| `rpcman.provider.metas.gray` | `false` | 是否参与灰度发布，此处设置为`false`表示不参与灰度。 |
| `rpcman.provider.metas.unit` | `B002` | 业务单元标识（unit），可能代表服务所属的业务线、团队或物理机房等。 |
| `rpcman.provider.metas.tc` | `300` | 流量控制器参数，指定30秒内允许的最大访问次数（默认为20次）。 |
| `apollo.cacheDir` | `/opt/data/` | Apollo配置中心本地缓存目录，用于存储拉取的配置文件。 |
| `apollo.cluster` | `default` | Apollo集群名称，关联到特定的配置中心实例。 |
| `apollo.meta` | `http://localhost:8080` | Apollo配置中心元数据中心地址，用于获取服务列表和配置信息。 |
| `apollo.autoUpdateInjectedSpringProperties` | `true` | 是否自动更新注入到Spring Bean中的属性值，保持与配置中心同步。 |
| `apollo.bootstrap.enabled` | `true` | 是否启用Apollo的启动引导加载功能，确保应用启动时能及时获取配置。 |
| `apollo.bootstrap.namespaces` | `rpcman-app` | 启动引导加载的配置命名空间列表，优先加载这些命名空间的配置。 |
| `apollo.bootstrap.eagerLoad.enabled` | `false` | 是否启用配置的急切加载模式，即在初始化阶段就加载所有配置而非按需加载。 |
| `logging.level.root` | `info` | 全局日志级别，设定为`info`表示记录信息、警告、错误及以上级别的日志。 |
| `logging.level.cn.ipman.rpc` | `debug` | 特定包（`cn.ipman.rpc`）的日志级别，设定为`debug`表示记录调试、信息、警告、错误及以上级别的日志。 |
| `logging.level.io.netty` | `info` | Netty库的日志级别，设定为`info`表示记录信息、警告、错误及以上级别的日志。 |




### Provider Server IO框架的选择与性能的评估

### 1.Provider Server 性能分布火焰图

关键信息：
1. 框架无业务处理逻辑，火焰图用于分析框架性能损耗分布，以优化Latency。 
2. 火焰图显示大部分损耗集中在IO操作上。
3. 结论：高性能RPC框架需选择高性能IO通信框架。

从火焰图表现上看,大部分损耗都在IO上,那么就可以得出一个结论 “一个高性能的RPC框架,必须选择一个高性能的IO通信框架”

<img src="https://ipman-blog-1304583208.cos.ap-nanjing.myqcloud.com/rpcman/2024-03-24-153134.png" alt="image-20240324222246295" style="width:600px;"  />


### 2 使用SpringBoot作为Provider Server的性能

压测工具： wrk <br>
命令： wrk http://localhost:8088/?id=101 <br>

压测结果：<br>
请求速率（Req/Sec）： 1916.85 <br>
吞吐量（Transfer/sec）： 316.70KB <br>

Arthas分析：<br>
监控命令： monitor -c 5 consumer.cn.ipman.rpc.core.RpcInvocationHandler invoke "#cost>10" <br>
平均响应时间（RT）： 24ms <br>

<img src="https://ipman-blog-1304583208.cos.ap-nanjing.myqcloud.com/rpcman/2024-03-24-140101.png" alt="image-20240324220054409"  />



### 3 总结 Provider Server IO 框架选型

在深入探讨RPC框架的IO组件选型时，首先从压测数据出发，我们发现Netty Server展现出显著的性能优势，其吞吐量远超采用Spring Boot（内嵌Tomcat）的方案。本次对比的核心即为Tomcat与Netty这两款同样支持NIO的通信框架之间的较量。尽管对两者内部架构（如Tomcat的Container、Connector机制与Netty的Bootstrap/Channel、EventLoop设计）进行详尽剖析对于理解其工作原理具有重要意义，但在实际应用于RPC框架的场景中，若性能差距未达到显著影响系统可接受程度的程度，这些底层特性的差异并不构成决定性因素。
然而，当我们从RPC框架设计与应用的角度重新审视，Tomcat与Netty在集成性和扩展性方面的差异则显得尤为重要。以下几点值得着重考量：<br>

**扩展性** <br>

Netty的优势在于其对多种协议的广泛支持。相较于专注于Web服务的Tomcat，Netty的设计初衷旨在构建高效、灵活的网络通信解决方案，使其能够轻松应对各种非HTTP协议的需求，如TCP、UDP、WebSocket等。这对于RPC框架而言尤为关键，因为RPC调用往往涉及定制化通信协议及编解码过程：<br>
1. 非HTTP协议支持：当RPC框架采用非HTTP作为传输协议时，Netty凭借其丰富的协议库与高度可配置性，能够无缝对接，无需额外开发工作，确保通信效率与稳定性。
2. 自定义编解码需求：在RPC场景中，传输的数据结构可能与HTTP Body的标准形式大相径庭，需要特定的序列化与反序列化逻辑。Netty提供了强大的编解码器体系，使得用户能够便捷地实现自定义编解码逻辑，以适应复杂的RPC数据交互需求。

**集成性** <br>

考虑到RPC框架的用户群体主要为业务开发人员，他们普遍采用Spring系列框架进行开发，而这些框架通常已内嵌了Tomcat容器。在这种背景下，选择Spring Boot（内嵌Tomcat）作为RPC框架的IO组件可能会引发一系列问题：<br>
1. 版本冲突：若RPC框架强行引入特定版本的Tomcat，可能导致与项目中已存在的Spring版本不兼容，增加维护复杂度。 
2. IOC冲突：Spring Boot对Bean的管理方式与RPC框架可能存在冲突，导致依赖注入（Dependency Injection, DI）混乱，影响应用的正常运行。 
3. Servlet冲突：在RPC框架中直接使用Tomcat可能导致Servlet容器层面的冲突，例如路由规则、过滤器配置等，给应用部署与调试带来困扰。 
鉴于上述集成性挑战，选用如Netty这样相对独立、轻量级的IO框架，更有利于降低与现有业务环境的摩擦，提升RPC框架对用户友好性与易用性。开发者可以更为顺畅地将RPC框架融入既有项目，减少不必要的适配工作与潜在风险，从而专注于核心业务逻辑的实现与优化。<br>
综上所述，在设计RPC框架时，虽然Tomcat与Netty在底层NIO支持上存在共性，但Netty凭借其卓越的扩展性（尤其在支持非HTTP协议与自定义编解码方面）以及良好的集成性（避免与Spring生态产生冲突），成为构建高性能、易用RPC框架的理想IO组件选择。<br>


####  模拟全流程的单元测试代码覆盖率

##### 1. 首先需要模拟一个ZK Server,便于测试

因为注册中心需要用到zokeeper, 这里用的`curator`自带的`curator-test` 来模拟,  添加 `maven` 依赖:

```java
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-test</artifactId>
    <version>5.1.0</version>
</dependency>
```

具体模拟 `zookeeper` server 的代码如下:

```java
package cn.ipman.rpcman.core.test;

import lombok.SneakyThrows;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.utils.CloseableUtils;

/**
 * 模拟ZooKeeper服务端
 *
 * @Author IpMan
 * @Date 2024/3/25 22:42
 */
public class TestZKServer {

    TestingCluster cluster;

    @SneakyThrows
    public void start() {
        // 模拟ZooKeeper服务端
        InstanceSpec instanceSpec = new InstanceSpec(null, 2182,
                -1, -1, true,
                -1, -1, -1);
        cluster = new TestingCluster(instanceSpec);
        System.out.println("TestingZooKeeperServer starting ...");
        cluster.start();
        cluster.getServers().forEach(s -> System.out.println(s.getInstanceSpec()));
        System.out.println("TestingZooKeeperServer started.");
    }

    @SneakyThrows
    public void stop() {
        System.out.println("TestingZooKeeperServer stopping ...");
        cluster.stop();
        CloseableUtils.closeQuietly(cluster);
        System.out.println("TestingZooKeeperServer stopped.");
    }
}
```



##### 2.编写Consumer测试启动类

在`consumer测试类`启动前, 先通过 `@Before` 启动`TestZKServer`和`Provider`程序.  目的是测试一个从 `consumer`端 -> `provider`端 完整的闭环链路

```java
package cn.ipman.rpcman.demo.consumer;

import test.cn.ipman.rpc.core.TestZKServer;
import cn.ipman.rpc.demo.provider.RpcmanDemoProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * MockRpcManDemoConsumerApplicationTests
 *
 * @Author IpMan
 * @Date 2024/3/26 22:22
 */
@SpringBootTest(classes = {RpcmanDemoConsumerApplication.class})
public class MockRpcManDemoConsumerApplicationTests {

    static ApplicationContext context;

    static TestZKServer zkServer = new TestZKServer();

    @BeforeAll
    static void init() {
        System.out.println(" ================================ ");
        System.out.println(" ================================ ");
        System.out.println(" ================================ ");
        System.out.println(" ================================ ");

        zkServer.start();
        context = SpringApplication.run(RpcmanDemoProviderApplication.class,
                "--server.port=8084", "--server.useNetty=true",
                "--rpcman.server=localhost:2182", "--logging.level.cn.ipman=debug");
    }

    @Test
    void contextLoads() {
        System.out.println("consumer running ... ");
    }

    @AfterAll
    static void destroy() {
        SpringApplication.exit(context, () -> 1);
        zkServer.stop();
    }
}
```




##### 3.安装 `jaCoCo` 的依赖和插件, 并运行统计代码覆盖率

添加maven依赖

```java
<dependency>
    <groupId>org.jacoco</groupId>
    <artifactId>org.jacoco.agent</artifactId>
    <version>0.8.7</version>
    <scope>test</scope>
</dependency>

<build>   
  <plugins>
    <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>0.8.7</version>
        <executions>
            <execution>
                <goals>
                    <goal>prepare-agent</goal>
                </goals>
            </execution>
            <execution>
                <id>report</id>
                <phase>test</phase>
                <goals>
                    <goal>report</goal>
                </goals>
            </execution>
        </executions>
    </plugin>  
  </plugins>
</build>  
```




运行`mvn test`，将生成JaCoCo代码覆盖率报告`target/site/jacoco/*`

$ mvn clean test  启动测试

![image-20240330125633711](https://ipman-blog-1304583208.cos.ap-nanjing.myqcloud.com/rpcman/2024-03-30-045640.png)......


<img src="https://ipman-blog-1304583208.cos.ap-nanjing.myqcloud.com/rpcman/2024-03-30-051111.png" alt="image-20240330131105677" style="width:400px;" />



最终查看测试代码覆盖率报告:  `xx/rpcman/rpcman-demo-consumer/target/site/jacoco/index.html`

![image-20240330130139092](https://ipman-blog-1304583208.cos.ap-nanjing.myqcloud.com/rpcman/2024-03-30-050141.png)







#### 关于高可用

一个生产级别的RPC框架,必须考虑Provider端的异常重试、故障摘除、故障恢复等问题, 主要是在Consumer进行实现,考虑如下:
1. 有节点宕机的时候, 通过多个Provider集群+注册中心,可以运行期保证服务整体可用
2. 有节点偶尔异常,但是没有宕机.可以通过重试+LB重新选节点,实现这次的调用成功.
3. 有节点在一段时间内异常 (这个实例上有很多服务,其中个别服务比如说)、没有宕机,甚至其他服务一直好使, ==> 故障隔离
   1. 探活好了,就可以做故障恢复, full open
   2. 每次定时探活就放一笔流量进来 half open





**首先创建一个滑动窗口类,用来记录一段时间内,失败的次数**

这个滑动窗口工具类可以直接使用:  new SlidingTimeWindow() 使用

默认是ring的默认长度是30, 表示30s内共存储了30个1s, 这个长度会随着时间(按秒)进行滑动,  用数据表示: [0,0,1,0...] ,其中s上元素0代表provider没有异常, 1 代表provider异常, 通过 window.getSum() 可以获取30s内共失败了多少次

```java
package cn.ipman.rpcman.core.governance;

import lombok.ToString;

/**
 * Ring Buffer implement based on an int array.
 *
 * @Author IpMan
 * @Date 2024/3/30 19:52
 */
@ToString
public class RingBuffer {

    final int size;
    final int[] ring;

    public RingBuffer(int _size) {
        // check size > 0
        this.size = _size;
        this.ring = new int[this.size];
    }

    public int sum() {
        int _sum = 0;
        for (int i = 0; i < this.size; i++) {
            _sum += ring[i];
        }
        return _sum;
    }

    public void reset() {
        for (int i = 0; i < this.size; i++) {
            ring[i] = 0;
        }
    }

    public void reset(int index, int step) {
        for (int i = index; i < index + step; i++) {
            ring[i % this.size] = 0;
        }
    }

    public void incr(int index, int delta) {
        ring[index % this.size] += delta;
    }
}
```



```java
package cn.ipman.rpcman.core.governance;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * SlidingTimeWindow implement based on RingBuffer and TS(timestamp).
 * Use TS/1000->SecondNumber to mapping an index slot in a RingBuffer.
 *
 * @Author IpMan
 * @Date 2024/3/30 19:52
 */
@Getter
@ToString
@Slf4j
public class SlidingTimeWindow {

    public static final int DEFAULT_SIZE = 30;

    private final int size;
    private final RingBuffer ringBuffer;
    private int sum = 0;

    // private int _start_mark = -1;
    // private int _prev_mark  = -1;
    private int _curr_mark = -1;

    private long _start_ts = -1L;
    //   private long _prev_ts  = -1L;
    private long _curr_ts = -1L;

    public SlidingTimeWindow() {
        this(DEFAULT_SIZE);
    }

    public SlidingTimeWindow(int _size) {
        this.size = _size;
        this.ringBuffer = new RingBuffer(this.size);
    }

    /**
     * record current ts millis.
     *
     * @param millis 秒
     */
    public synchronized void record(long millis) {
        log.debug("window before: " + this);
        log.debug("window.record(" + millis + ")");
        long ts = millis / 1000;
        if (_start_ts == -1L) {
            initRing(ts);
        } else {   // TODO  Prev 是否需要考虑
            if (ts == _curr_ts) {
                log.debug("window ts:" + ts + ", curr_ts:" + _curr_ts + ", size:" + size);
                this.ringBuffer.incr(_curr_mark, 1);
            } else if (ts > _curr_ts && ts < _curr_ts + size) {
                int offset = (int) (ts - _curr_ts);
                log.debug("window ts:" + ts + ", curr_ts:" + _curr_ts + ", size:" + size + ", offset:" + offset);
                this.ringBuffer.reset(_curr_mark + 1, offset);
                this.ringBuffer.incr(_curr_mark + offset, 1);
                _curr_ts = ts;
                _curr_mark = (_curr_mark + offset) % size;
            } else if (ts >= _curr_ts + size) {
                log.debug("window ts:" + ts + ", curr_ts:" + _curr_ts + ", size:" + size);
                this.ringBuffer.reset();
                initRing(ts);
            }
        }
        this.sum = this.ringBuffer.sum();
        log.debug("window after: " + this);
    }

    public int calcSum() {
        long ts = System.currentTimeMillis() / 1000;
        if(ts > _curr_ts && ts < _curr_ts + size) {
            int offset = (int)(ts - _curr_ts);
            log.debug("calc sum for window ts:" + ts + ", curr_ts:" + _curr_ts + ", size:" + size + ", offset:" + offset);
            this.ringBuffer.reset(_curr_mark + 1, offset);
            _curr_ts = ts;
            _curr_mark = (_curr_mark + offset) % size;
        } else if(ts >= _curr_ts + size) {
            log.debug("calc sum for window ts:" + ts + ", curr_ts:" + _curr_ts + ", size:" + size);
            this.ringBuffer.reset();
            initRing(ts);
        }
        log.debug("calc sum for window:" + this);
        return ringBuffer.sum();
    }

    private void initRing(long ts) {
        log.debug("window initRing ts:" + ts);
        this._start_ts = ts;
        this._curr_ts = ts;
        this._curr_mark = 0;
        this.ringBuffer.incr(0, 1);
    }

}
```



**在RpcInvocationHandler构造器中定义如下(详细看RpcInvocationHandler类的实现)**

providers - 所有有效的Provider实例

isolateProviders - 所有被隔离的Provider实例

halfOpenProviders - 代表半开中待探活的Provider实例, 由executorService.scheduleWithFixedDelay 定时延迟线程池, 定期从isolateProviders里将被隔离的Provider实例转移到halfOpenProviders中

windows - key是实例的url, value是以30s为单位的滑动时间窗口类

executorService - 是 scheduleWithFixedDelay 定时任务,用于定期(60s/次)将隔离区的Provider转移到半开重试区

```java
final List<InstanceMeta> providers;
final List<InstanceMeta> isolateProviders = new ArrayList<>();
final List<InstanceMeta> halfOpenProviders = new ArrayList<>();
final Map<String, SlidingTimeWindow> windows = new HashMap<>();
ScheduledExecutorService executorService;


public RpcInvocationHandler(Class<?> service, RpcContext rpcContext, List<InstanceMeta> providers) {
    this.service = service;
    this.rpcContext = rpcContext;
    this.providers = providers;

    // 初始化httpClient端
    initHttpInvoker();

    // 定时探活Provider的运行状态 , 单线程, 延迟10s执行, 每60s执行一次
    this.executorService = Executors.newScheduledThreadPool(1);
    int halfOpenInitialDelay = Integer.parseInt(rpcContext.getParameters()
            .getOrDefault("consumer.halfOpenInitialDelay", "10000"));
    int halfOpenDelay = Integer.parseInt(rpcContext.getParameters()
            .getOrDefault("consumer.halfOpenDelay", "60000"));
    this.executorService.scheduleWithFixedDelay(this::halfOpen, halfOpenInitialDelay,
            halfOpenDelay, TimeUnit.MILLISECONDS);
}

private void halfOpen() {
    // 故障半开, 服务探活
    // 定期将 isolateProviders 的实例转移到 halfOpenProviders 中
    log.debug(" ==> providers half open isolateProviders:" + isolateProviders);
    halfOpenProviders.clear();
    halfOpenProviders.addAll(isolateProviders);
}
```



**在RpcInvocationHandler#Invoke中添加故障隔离和半开重试的逻辑**

主要分为三个小逻辑:

1、首先判断 halfOpenProviders 是否为空, 空的话代表没有需要半开重试的Provider,直接走Router和LoadBanlaner获取实例即刻;如果不为空则取除一个Provider实例进行谈活访问

2、接下来通过 this.httpInvoker.post(rpcRequest, url) 访问Provider, 如果访问异常且实例30s窗口期内失败次数已经大于 faultLimit(默认20次) 时, 则继续添加到 isolateProviders 隔离区里

3、如果探活成功, 就添加到有效的 providers 中 ,视为探活尝试成功

```java
 @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 屏蔽一些Provider接口实现的方法
        if (MethodUtils.checkLocalMethod(method.getName())) {
            return null;
        }

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(this.service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setArgs(args);

        // 默认重试次数
        int retries = Integer.parseInt(rpcContext.getParameters()
                .getOrDefault("consumer.retries", "1"));
        // 最大重试次数后进入隔离区
        int faultLimit = Integer.parseInt(rpcContext.getParameters()
                .getOrDefault("consumer.faultLimit", "10"));

        while (retries-- > 0) {
            log.info(" ===> retries: " + retries);
            try {
                // [Filter Before] 前置过滤器
                for (Filter filter : this.rpcContext.getFilters()) {
                    Object preResult = filter.preFilter(rpcRequest);
                    // preResult == null 代表被过滤
                    if (preResult != null) {
                        log.info(filter.getClass().getName() + " ==> preFilter:" + preResult);
                        return preResult;
                    }
                }

                InstanceMeta instance;
                synchronized (halfOpenProviders) {
                    if (halfOpenProviders.isEmpty()) {
                        // 获取路由,通过负载均衡选取一个代理的url
                        List<InstanceMeta> instances = rpcContext.getRouter().route(this.providers);
                        instance = rpcContext.getLoadBalancer().choose(instances);
                        log.debug("loadBalancer.choose(urls) ==> {}", instance);
                    } else {
                        // 如果有半开的Provider节点, 需要做探活
                        instance = halfOpenProviders.remove(0);
                        log.debug("check alive instance ==> {}", instance);
                    }
                }

                RpcResponse<?> rpcResponse;
                Object result;
                String url = instance.toHttpUrl();
                try {
                    // 请求 Provider
                    rpcResponse = this.httpInvoker.post(rpcRequest, url);
                    result = castResponseToResult(method, rpcResponse);
                } catch (Exception e) {
                    // 故障的规则统计和隔离
                    // 每一次异常, 记录一次, 统计30s的异常数.
                    synchronized (windows) {
                        SlidingTimeWindow window = windows.computeIfAbsent(url, k -> new SlidingTimeWindow());
                        window.record(System.currentTimeMillis());
                        log.debug("instance {} in windows with {}", url, window.getSum());
                        // 规则发生10次, 就做故障隔离, 摘除节点
                        if (window.getSum() >= faultLimit) {
                            isolate(instance);
                        }
                    }
                    throw e;
                }

                synchronized (providers) {
                    // 如果Provider实例调用成功, 但不在providers里,证明是探活成功了,需要在providers中恢复这个节点
                    if (!providers.contains(instance)) {
                        isolateProviders.remove(instance);
                        providers.add(instance);
                        log.debug("instance {} is recovered, isolatedProviders={}, providers={}"
                                , instance, isolateProviders, providers);
                    }
                }

                // [Filter After] 后置过滤器, 这里拿到的可能不是最终值, 需要再设计一下
                for (Filter filter : this.rpcContext.getFilters()) {
                    Object filterResult = filter.postFilter(rpcRequest, rpcResponse, result);
                    // filterResult == null 代表不过滤
                    if (filterResult != null) {
                        log.info(filter.getClass().getName() + " ==> postFilter:" + filterResult);
                        return filterResult;
                    }
                }
                return result;
            } catch (RuntimeException ex) {
                // 如果不是超时类异常,就直接throw
                if (!(ex.getCause() instanceof SocketTimeoutException)
                        && !(ex.getCause() instanceof ReadTimeoutException)) {
                    throw ex;
                }
            }
        }
        return null;
    }


    private void isolate(InstanceMeta instance) {
        // 故障隔离, 服务拆除
        log.debug(" ==>  providers isolate instance: " + instance);
        providers.remove(instance);
        log.debug(" ==>  providers = {}", providers);
        isolateProviders.add(instance);
        log.debug(" ==>  isolateProviders = {}", isolateProviders);
    }

```



#### 关于配置中心的集成

这里使用Apollo Config 做为配置中心, 需要的参考资料: https://www.apolloconfig.com/#/zh/deployment/quick-start

官方提供了apollo-quick-start.zip网盘下载方式,

解压后, 需要将 `sql`  目录下的 `apolloconfigdb.sql` `apolloportaldb.sql` 初始化到你的 `mysql` 数据库中

启动命令使用 `sh demo.sh start`启动

```shell
==== starting service ====
Service logging file is ./service/apollo-service.log
Started [26169]
Waiting for config service startup...
Config service started. You may visit http://localhost:8080 for service status now!
Waiting for admin service startup.
Admin service started
==== starting portal ====
Portal logging file is ./portal/apollo-portal.log
Started [26223]
Waiting for portal startup...
Portal started. You can visit http://localhost:8070 now!
```



启动后, 通过访问 http://localhost:8070 ,进入Apollo 管理端

核心概念 [应用 - 环境 - 集群 - Namespace - 配置],  了解更多可以根据参考资料学习

![image-20240414125329633](https://ipman-blog-1304583208.cos.ap-nanjing.myqcloud.com/rpcman/2024-04-14-045332.png)







##### RPC-MAN 框架集成Apollo 配置中心的具体实现如下

安装pom依赖,  作者的 apollo `version` 是 `2.2.0`

```java
<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-client</artifactId>
    <version>${apollo.version}</version>
</dependency>
```



在 `consumer` 端 和 `provider` 端 `application.yaml` 添加 Apollo配置

```java
# apollo 配置中心
app:
  id: app
apollo:
  cacheDir: /opt/data/
  cluster: default
  meta: http://localhost:8080
  autoUpdateInjectedSpringProperties: true # 是否自动更新
  bootstrap:
    enabled: true
    namespaces: rpcman-app
    eagerLoad:
      enabled: false
```



在 `consumer` 端 和 `provider` 端 Application 启动类开启 `@EnableApolloConfig` 配置

当被Spring框架 `@Value` 注解标记的配置, 会被 Apollo 进行代理和发布更新

```java
@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
@EnableApolloConfig
@Slf4j
public class RpcmanDemoProviderApplication {
```



由于我们框架都是由 `@ConfigurationProperties`  注解在Spring环境启动时, 通过前缀匹配的方式将配置注入到一个单例类里, 如:

```java
@Data
@Configuration
@ConfigurationProperties(prefix = "rpcman.app")
public class AppConfigProperties {

    // for app instance
    private String id = "app1";

    private String namespace = "public";
```



**这种方式不能被 Apollo 动态代理更新 **, 这里我们需要通过监听 apollo的`ConfigChangeEvent`,当配置产生变化时, 借助 `spring-cloud-context` 的 `EnvironmentChangeEvent` 进行动态更新, 实现如下:

```java
/**
 * 更新对应的bean的属性值
 * 主要式存在@ConfigurationProperties注解的bean
 *
 * @Author IpMan
 * @Date 2024/4/13 16:37
 */
@Data
@Slf4j
public class ApolloChangedListener implements ApplicationContextAware {

    ApplicationContext applicationContext;

    @ApolloConfigChangeListener({"rpcman-app"}) // listener to namespace
    @SuppressWarnings("unused")
    private void changeHandler(ConfigChangeEvent changeEvent) {
        for (String key : changeEvent.changedKeys()) {
            ConfigChange change = changeEvent.getChange(key);
            log.info("Found change - {}", change.toString());
        }

        // 更新对应的bean的属性值,主要式存在@ConfigurationProperties注解的bean
        this.applicationContext.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));
    }
}
```

将 `ApolloChangedListener` 注入到 `Spring容器` 中,  `ConsumerConfig` 和 `ProviderConfig` 都需要

```
@Bean
@ConditionalOnMissingBean
@ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
ApolloChangedListener consumer_apolloChangedListener() {
    return new ApolloChangedListener();
}
```



##### 测试类中集成 Apollo Mock环境

添加依赖,  作者的 apollo `version` 是 `2.2.0`

```java
<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-mockserver</artifactId>
    <version>${apollo.version}</version>
</dependency>
```

```java
// 创建 Apollo Mock Server
static ApolloTestingServer apollo = new ApolloTestingServer();

@BeforeAll
@SneakyThrows
static void init() {
    ....

    System.out.println(" ====================================== ");
    System.out.println(" ====================================== ");
    System.out.println(" ===========     mock apollo    ======= ");
    System.out.println(" ====================================== ");
    System.out.println(" ====================================== ");
    apollo.start(); // 启动

    ....
}

@AfterAll
static void destroy() {
		....
      
    System.out.println(" ===========     stop apollo mockserver   ======= ");
    apollo.close(); // 关停
    System.out.println(" ===========     destroy in after all     ======= ");
}
```



