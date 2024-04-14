package cn.ipman.rpc.core.registry;

import cn.ipman.rpc.core.meta.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/3/17 21:54
 */
@Data
@AllArgsConstructor
public class Event {

    List<InstanceMeta> data;

}

