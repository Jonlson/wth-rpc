package wth.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import wth.rpc.fault.retry.RetryStrategy;
import wth.rpc.model.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import wth.rpc.RpcApplication;
import wth.rpc.config.RpcConfig;
import wth.rpc.constant.RpcConstant;
import wth.rpc.fault.retry.RetryStrategy;
import wth.rpc.fault.retry.RetryStrategyFactory;
import wth.rpc.fault.tolerant.TolerantStrategy;
import wth.rpc.fault.tolerant.TolerantStrategyFactory;
import wth.rpc.loadbalancer.LoadBalancer;
import wth.rpc.loadbalancer.LoadBalancerFactory;
import wth.rpc.model.RpcRequest;
import wth.rpc.model.RpcResponse;
import wth.rpc.model.ServiceMetaInfo;
import wth.rpc.registry.Registry;
import wth.rpc.registry.RegistryFactory;
import wth.rpc.serializer.Serializer;
import wth.rpc.serializer.SerializerFactory;
import wth.rpc.server.tcp.VertxTcpClient;


/**
 * 实现JDK的动态代理的实现invoke的类
 *
 * 拓展:使用cglib实现动态代理
 * */
public class ServiceProxy implements InvocationHandler {

    /**
     * 调用代理
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        // 从注册中心获取服务提供者请求地址
        //
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            throw new RuntimeException("暂无服务地址");
        }

        // 负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        // 将调用方法名（请求路径）作为负载均衡参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
        // rpc 请求
        // 使用重试机制
        RpcResponse rpcResponse;
        try {
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            rpcResponse = retryStrategy.doRetry(() ->
                    VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo)
            );
        } catch (Exception e) {
            // 容错机制
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
            rpcResponse = tolerantStrategy.doTolerant(null, e);
        }
        return rpcResponse.getData();
    }
}
