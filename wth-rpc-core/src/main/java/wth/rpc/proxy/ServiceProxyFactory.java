package wth.rpc.proxy;


import wth.rpc.RpcApplication;
import wth.rpc.config.RpcConfig;

import java.lang.reflect.Proxy;

/**
 * 代理类工厂
 * 决定使用哪一种的代理法方式
 * */
public class ServiceProxyFactory {

    /**
     * 根据服务类获取代理对象
     *
     *
     * */

    private static <T> T getProxy(Class<T> serviceClass) {
        if (RpcApplication.getRpcConfig().isMock()) {
            return getMockProxy(serviceClass);
        }

        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy()
        );

    }

    private static <T> T getMockProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new MockServiceProxy());
    }
}
