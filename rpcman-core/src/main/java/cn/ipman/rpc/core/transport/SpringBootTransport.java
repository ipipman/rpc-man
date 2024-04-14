package cn.ipman.rpc.core.transport;

import cn.ipman.rpc.core.api.RpcRequest;
import cn.ipman.rpc.core.api.RpcResponse;
import cn.ipman.rpc.core.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/4/4 18:26
 */
@RestController
public class SpringBootTransport {

    @Autowired
    private ProviderInvoker providerInvoker;

    @RequestMapping("/rpcman")
    public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }

}
