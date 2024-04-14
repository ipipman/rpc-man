package cn.ipman.rpc.core.api;

import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC请求体结构, 定义生产者借口、方法、方法参数
 * 用于RPCMan框架启动时生产者注册,以及运行时调用
 *
 * @Author IpMan
 * @Date 2024/3/9 20:07
 */

@Data
@ToString
public class RpcRequest {

    private String service; // 接口, 如: cn.ipman.rpc.demo.api.UserService
    private String methodSign; // 方法签名
    private Object[] args; // 方法参数

    // 跨调用方需要传递的参数
    private Map<String, String> params = new HashMap<>();
}
