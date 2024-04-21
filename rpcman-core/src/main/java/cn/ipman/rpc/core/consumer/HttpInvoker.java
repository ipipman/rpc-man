package cn.ipman.rpc.core.consumer;

import cn.ipman.rpc.core.api.RpcRequest;
import cn.ipman.rpc.core.api.RpcResponse;
import cn.ipman.rpc.core.consumer.http.OkHttpInvoker;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Interface for http invoke.
 *
 * @Author IpMan
 * @Date 2024/3/23 12:19
 */
public interface HttpInvoker {

    Logger log = LoggerFactory.getLogger(HttpInvoker.class);

    RpcResponse<?> post(RpcRequest rpcRequest, String url);

    HttpInvoker Default = new OkHttpInvoker(500);

    /**
     * 执行HTTP POST请求。
     *
     * @param requestString 请求体字符串。
     * @param url 请求的URL。
     * @return 响应的字符串内容。
     */
    String post(String requestString, String url);

    /**
     * 执行HTTP GET请求。
     *
     * @param url 请求的URL。
     * @return 响应的字符串内容。
     */
    String get(String url);

    /**
     * 使用HTTP GET方法获取信息，并将其解析为指定的Java类型。
     *
     * @param url 请求的URL。
     * @param clazz 需要解析成的Java类型。
     * @param <T> 返回对象的类型。
     * @return 解析后的对象实例。
     */
    @SneakyThrows
    @SuppressWarnings("unused")
    static <T> T httpGet(String url, Class<T> clazz) {
        log.debug(" =====>>>>>> httpGet: " + url);
        String respJson = Default.get(url);
        log.debug(" =====>>>>>> respJson: " + respJson);
        return JSON.parseObject(respJson, clazz);
    }


    @SneakyThrows
    static <T> T httpGet(String url, TypeReference<T> typeReference) {
        log.debug(" =====>>>>>> httpGet: " + url);
        String respJson = Default.get(url);
        log.debug(" =====>>>>>> respJson: " + respJson);
        return JSON.parseObject(respJson, typeReference);
    }

    /**
     * 使用HTTP POST方法发送请求，并将其解析为指定的Java类型。
     *
     * @param requestString 请求体字符串。
     * @param url 请求的URL。
     * @param clazz 需要解析成的Java类型。
     * @param <T> 返回对象的类型。
     * @return 解析后的对象实例。
     */
    @SneakyThrows
    @SuppressWarnings("unused")
    static <T> T httpPost(String requestString, String url, Class<T> clazz) {
        log.debug(" =====>>>>>> httpPost: " + url);
        String respJson = Default.post(requestString, url);
        log.debug(" =====>>>>>> respJson: " + respJson);
        return JSON.parseObject(respJson, clazz);
    }

}
