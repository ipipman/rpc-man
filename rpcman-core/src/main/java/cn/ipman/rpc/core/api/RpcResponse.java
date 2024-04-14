package cn.ipman.rpc.core.api;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * RPC 响应体
 *
 * @Author IpMan
 * @Date 2024/3/9 20:07
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse<T> {

    boolean status; // 状态:true
    T data; // new User
    RpcException ex;

}
