package cn.ipman.rpc.core.cluster;

import cn.ipman.rpc.core.meta.InstanceMeta;
import cn.ipman.rpc.core.api.Router;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 灰度发布路由类
 * 用于在服务调用时，根据指定的灰度比例选择是路由到灰度实例还是正常实例。
 *
 * @Author IpMan
 * @Date 2024/3/31 21:49
 */
@Slf4j
public class GrayRouter implements Router<InstanceMeta> {

    private final int grayRatio; // 灰度比例
    private final Random random = new Random();  // 用于随机选择

    /**
     * 灰度路由器构造函数
     *
     * @param grayRatio 灰度比例，表示灰度实例被选中的概率。
     */
    public GrayRouter(int grayRatio) {
        this.grayRatio = grayRatio;
    }

    /**
     * 根据灰度比例从服务提供者列表中路由选择实例。
     *
     * @param providers 服务提供者列表。
     * @return 路由选择后的实例列表。
     */
    @Override
    public List<InstanceMeta> route(List<InstanceMeta> providers) {
        // 如果列表为空或只有一个实例，直接返回
        if (providers == null || providers.size() <= 1) {
            return providers;
        }

        // 分别初始化正常节点和灰度节点列表
        List<InstanceMeta> normalNodes = new ArrayList<>();
        List<InstanceMeta> grayNodes = new ArrayList<>();

        // 根据实例的灰度标志，将实例分到正常或灰度节点列表
        providers.forEach(p -> {
            if ("true".equals(p.getParameters().get("gray"))) {
                grayNodes.add(p);
            } else {
                normalNodes.add(p);
            }
        });

        // 如果正常和灰度节点都为空，返回原列表
        if (normalNodes.isEmpty() || grayNodes.isEmpty()) return providers;
        if (grayRatio <= 0) {
            return normalNodes; // 灰度比例为0或负数时，返回正常节点
        } else if (grayRatio >= 100) {
            return grayNodes;   // 灰度比例为100或更大时，返回灰度节点
        }

        // 随机决定返回灰度节点还是正常节点
        if (random.nextInt(100) < grayRatio) {
            log.debug(" grayRoute grayNodes ===> {}", grayRatio);
            return grayNodes;
        } else {
            log.debug(" grayRoute normalNodes ===> {}", normalNodes);
            return normalNodes;
        }
    }
}
