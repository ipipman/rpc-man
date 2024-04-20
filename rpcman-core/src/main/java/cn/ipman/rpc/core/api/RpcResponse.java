package cn.ipman.rpc.core.api;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * RPC响应结构体
 *
 * @Author IpMan
 * @Date 2024/3/9 20:07
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse<T> {

    boolean status;     // 状态:true
    T data;             // 如: new User
    RpcException ex;    // RPC异常类

}
