package cn.ipman.rpc.demo.consumer;

import cn.ipman.rpc.core.test.TestZKServer;
import cn.ipman.rpc.demo.provider.RpcmanDemoProviderApplication;
import com.ctrip.framework.apollo.mockserver.ApolloTestingServer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/3/26 22:22
 */
@SpringBootTest(classes = {RpcmanDemoConsumerApplication.class},
         properties = {"rpcman.zk.zkServer=localhost:2183", "rpcman.zk.enabled=true", "registry-ipman.enabled=false"})
public class RpcManDemoConsumerApplicationTests {

    static ApplicationContext context1;

    static ApplicationContext context2;

    static TestZKServer zkServer = new TestZKServer(2183);

    static ApolloTestingServer apollo = new ApolloTestingServer();

    @Autowired
    private Environment environment;

    @BeforeAll
    @SneakyThrows
    static void init() {

        System.out.println(" ================================ ");
        System.out.println(" =========== Mock ZK 2183 ======= ");
        System.out.println(" ================================ ");
        System.out.println(" ================================ ");
        zkServer.start();

        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" ===========     mock apollo    ======= ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        apollo.start();


        System.out.println(" ================================ ");
        System.out.println(" ============  8085 ============= ");
        System.out.println(" ================================ ");
        System.out.println(" ================================ ");
        context1 = SpringApplication.run(RpcmanDemoProviderApplication.class,
                "--server.port=8085",
                "--logging.level.cn.ipman=debug",
                "--rpcman.app.useNetty=true",
                "--rpcman.zk.zkServer=localhost:2183",
                "--rpcman.zk.enabled=true",
                "--rpcman.provider.metas.dc=bj",
                "--rpcman.provider.metas.gray=false",
                "--rpcman.provider.metas.unit=B002",
                "--rpcman.provider.metas.tc=300",
                "--registry-ipman.enabled=false"
        );

        System.out.println(" ================================ ");
        System.out.println(" ============  8087 ============= ");
        System.out.println(" ================================ ");
        System.out.println(" ================================ ");
        context2 = SpringApplication.run(RpcmanDemoProviderApplication.class,
                "--server.port=8087",
                "--logging.level.cn.ipman=debug",
                "--rpcman.app.useNetty=true",
                "--rpcman.zk.zkServer=localhost:2183",
                "--rpcman.zk.enabled=true",
                "--rpcman.provider.metas.dc=bj",
                "--rpcman.provider.metas.gray=false",
                "--rpcman.provider.metas.unit=B002",
                "--rpcman.provider.metas.tc=300",
                "--registry-ipman.enabled=false"
        );
    }

    @Test
    void contextLoads() {
        System.out.println("rpcman.zk.zkServer=>" + environment.getProperty("rpcman.zk.zkServer"));
        System.out.println("consumer running ... ");
    }

    @AfterAll
    static void destroy() {
        System.out.println(" ===========     close spring context     ======= ");
        SpringApplication.exit(context1, () -> 1);
        SpringApplication.exit(context2, () -> 1);
        System.out.println(" ===========     stop zookeeper server    ======= ");
        zkServer.stop();
        System.out.println(" ===========     stop apollo mockserver   ======= ");
        apollo.close();
        System.out.println(" ===========     destroy in after all     ======= ");
    }
}
