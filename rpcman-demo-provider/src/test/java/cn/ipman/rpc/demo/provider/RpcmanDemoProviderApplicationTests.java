package cn.ipman.rpc.demo.provider;

import cn.ipman.rpc.core.test.TestZKServer;
import com.ctrip.framework.apollo.core.ApolloClientSystemConsts;
import com.ctrip.framework.apollo.mockserver.ApolloTestingServer;
import com.ctrip.framework.apollo.mockserver.MockApolloExtension;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {RpcmanDemoProviderApplication.class},
        properties = {"rpcman.zk.enabled=true", "registry-ipman.enabled=false"})
@ExtendWith(MockApolloExtension.class)
class RpcmanDemoProviderApplicationTests {

    static TestZKServer zkServer = new TestZKServer(2182);

    static ApolloTestingServer apollo = new ApolloTestingServer();

    @SneakyThrows
    @BeforeAll
    static void init() {
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============     ZK2182    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        zkServer.start();
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" ===========     mock apollo    ======= ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        apollo.start();
    }

    @Test
    void contextLoads() {
        System.out.println(" ===> RpcmanDemoProviderApplicationTests ...");
        System.out.println("....  ApolloClientSystemConst's.APOLLO_CONFIG_SERVICE  .....");
        System.out.println(System.getProperty(ApolloClientSystemConsts.APOLLO_CONFIG_SERVICE));
        System.out.println("....  ApolloClientSystemConst's.APOLLO_CONFIG_SERVICE  .....");
    }

    @AfterAll
    static void destroy() {
        System.out.println(" ===========     stop zookeeper server    ======= ");
        zkServer.stop();
        System.out.println(" ===========     stop apollo mockserver   ======= ");
        apollo.close();
        System.out.println(" ===========     destroy in after all     ======= ");
    }

}
