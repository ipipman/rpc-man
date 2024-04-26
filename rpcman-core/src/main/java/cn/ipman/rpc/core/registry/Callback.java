package cn.ipman.rpc.core.registry;

@FunctionalInterface
public interface Callback {
    void call() throws Exception;
}