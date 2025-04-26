package wth.rpc.registry;


import wth.rpc.config.RegistryConfig;
import wth.rpc.spi.SpiLoader;

/**
 * 注册中心的工厂类
 *
 *
 *
 * @author 86156*/
public class RegistryFactory {


    /**
     * 默认注册中心
     * */
    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();


    public static Registry getInstance(String key) {
        return SpiLoader.getInstance(Registry.class, key);
    }
}
