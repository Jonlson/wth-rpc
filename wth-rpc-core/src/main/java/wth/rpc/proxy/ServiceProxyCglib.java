package wth.rpc.proxy;


import cn.hutool.core.collection.CollUtil;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.data.redis.core.RedisTemplate;
import wth.rpc.RpcApplication;
import wth.rpc.config.RpcConfig;
import wth.rpc.constant.RpcConstant;
import wth.rpc.fault.retry.RetryStrategyFactory;
import wth.rpc.fault.tolerant.TolerantStrategy;
import wth.rpc.fault.tolerant.TolerantStrategyFactory;
import wth.rpc.loadbalancer.LoadBalancerFactory;
import wth.rpc.model.RpcRequest;
import wth.rpc.model.RpcResponse;
import wth.rpc.model.ServiceMetaInfo;
import wth.rpc.registry.Registry;
import wth.rpc.registry.RegistryFactory;
import wth.rpc.server.tcp.VertxTcpClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.Serializable;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Cglib的动态代理的效率比JDK动态代理要高，但是JDK的动态代理是基于接口实现的（反射），而Cglib是基于类实现的（原理是字节码）。
 * 调用效率高，因为字节码直接创建一个类，调用时直接调用一个类，不用反射来间接调用
 * 但是内存占用大，在创建是漫。
 *
 * */
public class ServiceProxyCglib implements MethodInterceptor {

    @Resource
    private RedisTemplate<String, Serializable> redisTemplate;

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        String serviceName = method.getDeclaringClass().getName();
        int hour = LocalDateTime.now().getHour();
        // 构造请求：

        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        // 寻找服务列表
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(ServiceMetaInfo.builder().serviceName(serviceName).serviceVersion(RpcConstant.DEFAULT_SERVICE_VERSION).build().getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            throw new RuntimeException("服务不存在");
        }
        // 负载均衡：
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());

        // rpc（vertx）请求+重试+容错
        RpcResponse rpcResponse = null;
        try {
            RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy()).doRetry(() -> {
                // 优化如果失败进行路由：
                // TODO 还可以使用redis记录失败次数（有过期key），超过两次进行再次负载均衡也行
                ServiceMetaInfo selectedServiceMetaInfo = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer()).select(requestParams, serviceMetaInfoList);
                return VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
            });
        } catch (Exception e) {
            e.printStackTrace();

            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
            tolerantStrategy.doTolerant(requestParams, e);
        }
        return rpcResponse.getData();
    }
}
