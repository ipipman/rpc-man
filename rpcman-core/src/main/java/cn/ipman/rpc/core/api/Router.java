package cn.ipman.rpc.core.api;

import java.util.List;

/**
 * 路由器接口。该接口用于定义路由逻辑，可以根据特定规则对服务提供者列表进行筛选或排序等操作。
 * @param <T> 通用类型，代表需要进行路由处理的元素类型。
 */
public interface Router<T> {

    /**
     * 执行路由操作的方法。该方法会接收一个服务提供者列表，并根据路由规则处理后返回一个新地列表。
     * @param providers 待处理的服务提供者列表。
     * @return 处理后的服务提供者列表。
     */
    List<T> route(List<T> providers);


    /**
     * 默认路由器。该静态内部类提供了一个默认的路由实现，即直接返回输入的服务提供者列表。
     */
    @SuppressWarnings({"rawtypes", "unused"})
    Router Default = p -> p;

}
