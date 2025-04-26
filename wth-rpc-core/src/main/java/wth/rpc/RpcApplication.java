package wth.rpc;


import lombok.extern.slf4j.Slf4j;
import wth.rpc.config.RegistryConfig;
import wth.rpc.config.RpcConfig;
import wth.rpc.registry.RegistryFactory;
import wth.rpc.utils.*;
@Slf4j
public class RpcApplication {


    /**
     * volatile的作用:
     *
     * 保证不同线程之间的可见性
     *
     * 禁止指令重排序优化: 防止JVM为了提高性能而对指令进行重排序优化，从而导致并发问题和资源顺序发生改变
     * */
    private static volatile RpcConfig rpcConfig;

    public static void init(RpcConfig rpcConfig) {

        RpcApplication.rpcConfig = rpcConfig;

        log.info("初始化RPC配置:{}", rpcConfig);




        // 初始化Registry
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        RegistryFactory.getInstance(registryConfig.getRegistry()).init(registryConfig);


        // 初始化RpcTcpSever

        // 初始化RpcTcpClient

        /**
         * Spi加载Config的对应的Service的实现类.
         * 机制*/

        // 初始化LoadBalancer


        // 初始化Retry


        // 初始化降级策略
    }
    public static void init() {
        log.info("初始化RPC");

        RpcConfig rpcConfig = null;
        // 先获取配置项:
        try {
            rpcConfig = (RpcConfig) ConfigUtils.load(RpcConfig.class, "wth.rpc.config", "");
        } catch (Exception e) {
            log.error("初始化RPC配置失败", e);
            return;
        }

        init(rpcConfig);
    }

    // 获取Config类
    public static RpcConfig getRpcConfig() {
        // 避免多次初始化
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
