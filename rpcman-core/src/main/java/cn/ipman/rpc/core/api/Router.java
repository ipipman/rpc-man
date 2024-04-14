package cn.ipman.rpc.core.api;

import java.util.List;

/**
 * 路由器
 * @param <T>
 */
public interface Router<T> {

    List<T> route(List<T> providers);

    @SuppressWarnings("rawtypes")
    Router Default = p -> p;

}
