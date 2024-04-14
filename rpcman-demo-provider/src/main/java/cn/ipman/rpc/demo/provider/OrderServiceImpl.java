package cn.ipman.rpc.demo.provider;

import cn.ipman.rpc.core.annotation.RpcProvider;
import cn.ipman.rpc.demo.api.Order;
import cn.ipman.rpc.demo.api.OrderService;
import org.springframework.stereotype.Component;


/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/3/9 20:07
 */

@Component
@RpcProvider
public class OrderServiceImpl implements OrderService {

    @Override
    public Order findById(int id) {
        // test
        if (id == 404) {
            throw new RuntimeException("404 exception");
        }
        return new Order(id, "RpcMan-" + System.currentTimeMillis() + ", id=" + id);
    }

}
