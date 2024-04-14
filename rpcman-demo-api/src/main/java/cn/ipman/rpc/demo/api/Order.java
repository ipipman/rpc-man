package cn.ipman.rpc.demo.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/3/9 22:32
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Integer id;
    private String name;
}
