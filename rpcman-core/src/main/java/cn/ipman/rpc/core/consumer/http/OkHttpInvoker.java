package cn.ipman.rpc.core.consumer.http;

import cn.ipman.rpc.core.api.RpcException;
import cn.ipman.rpc.core.api.RpcRequest;
import cn.ipman.rpc.core.api.RpcResponse;
import cn.ipman.rpc.core.consumer.HttpInvoker;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 消费端的Http远程调用
 *
 * @Author IpMan
 * @Date 2024/3/23 12:21
 */
@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    final static MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client;

    public OkHttpInvoker(int timeout) {
        // 用okHttp进行远程传输
        this.client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true) // 运行失败重连
                .build();

    }

    @Override
    public RpcResponse<?> post(RpcRequest rpcRequest, String url) {
        String reqJson = JSON.toJSONString(rpcRequest);
        log.debug(" ===> reqJson = " + reqJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, JSON_TYPE))
                .build();
        try {
            String respJson = Objects.requireNonNull(client.newCall(request).execute().body()).string();
            log.debug(" ===> respJson = " + respJson);
            return JSON.parseObject(respJson, RpcResponse.class);
        } catch (Exception e) {
            //log.error("okHttp post error:", e);
            throw new RpcException(e);
        }
    }


    /**
     * 执行HTTP POST请求。
     *
     * @param requestString 请求体字符串
     * @param url           请求的URL
     * @return 返回HTTP响应体的字符串内容
     */
    @Override
    public String post(String requestString, String url) {
        log.debug(" ===> post  url = {}, requestString = {}", requestString, url);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestString, JSON_TYPE))
                .build();
        try {
            String respJson = Objects.requireNonNull(client.newCall(request).execute().body()).string();
            log.debug(" ===> respJson = " + respJson);
            return respJson;
        } catch (Exception e) {
            //log.error("okHttp post error:", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行HTTP GET请求。
     *
     * @param url 请求的URL
     * @return 返回HTTP响应体的字符串内容
     */
    @Override
    public String get(String url) {
        log.debug(" ===> get url = " + url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            String respJson = Objects.requireNonNull(client.newCall(request).execute().body()).string();
            log.debug(" ===> respJson = " + respJson);
            return respJson;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
