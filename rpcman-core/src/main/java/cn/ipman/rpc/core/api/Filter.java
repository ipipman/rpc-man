package cn.ipman.rpc.core.api;

/**
 * 过滤器接口。用于在RPC调用前后执行额外的逻辑。
 */
public interface Filter {

    /**
     * 请求前置过滤器。在RPC请求发送前调用。
     *
     * @param request RPC请求对象。
     * @return 返回可能修改后的请求或特定的业务对象。
     */
    Object preFilter(RpcRequest request);

    /**
     * 请求后置过滤器。在RPC响应接收后调用。
     *
     * @param request  RPC请求对象。
     * @param response RPC响应对象。
     * @param result   RPC调用结果。
     * @return 返回可能修改后的结果或特定的业务对象。
     */
    Object postFilter(RpcRequest request, RpcResponse<?> response, Object result);


    // 提供一个默认的过滤器实现，不做任何操作。
    @SuppressWarnings("unused")
    Filter Default = new Filter() {
        @Override
        public Object preFilter(RpcRequest request) {
            return null;
        }

        @Override
        public Object postFilter(RpcRequest request, RpcResponse<?> response, Object result) {
            return null;
        }
    };

}
