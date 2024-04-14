package cn.ipman.rpc.core.cluster;

import cn.ipman.rpc.core.meta.InstanceMeta;
import cn.ipman.rpc.core.api.Router;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 灰度发布路由类
 *
 * @Author IpMan
 * @Date 2024/3/31 21:49
 */
@Slf4j
public class GrayRouter implements Router<InstanceMeta> {

    private final int grayRatio;
    private final Random random = new Random();

    public GrayRouter(int grayRatio) {
        this.grayRatio = grayRatio;
    }

    @Override
    public List<InstanceMeta> route(List<InstanceMeta> providers) {
        if (providers == null || providers.size() <= 1) {
            return providers;
        }
        // 正常的节点
        List<InstanceMeta> normalNodes = new ArrayList<>();
        // 灰度的节点
        List<InstanceMeta> grayNodes = new ArrayList<>();

        providers.forEach(p -> {
            if ("true".equals(p.getParameters().get("gray"))) {
                grayNodes.add(p);
            } else {
                normalNodes.add(p);
            }
        });

        if (normalNodes.isEmpty() || grayNodes.isEmpty()) return providers;
        // 如果灰度比例是10
        if (grayRatio <= 0) {
            return normalNodes;
        } else if (grayRatio >= 100) {
            return grayNodes;
        }

        // 再A的情况下, 返回 normal nodes, ==> 不管LB的算法情况下, 一定是normal
        // B的情况下, 返回 gray nodes ==> 不管LB不管算法情况下, 一定是gray
        if (random.nextInt(100) < grayRatio) {
            log.debug(" grayRoute grayNodes ===> {}", grayRatio);
            return grayNodes;
        } else {
            log.debug(" grayRoute normalNodes ===> {}", normalNodes);
            return normalNodes;
        }
    }
}
