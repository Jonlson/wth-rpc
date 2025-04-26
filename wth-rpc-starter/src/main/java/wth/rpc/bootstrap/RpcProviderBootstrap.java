package wth.rpc.bootstrap;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import wth.rpc.annotation.RpcService;
import wth.rpc.config.RegistryConfig;
import wth.rpc.config.RpcConfig;
import wth.rpc.model.ServiceMetaInfo;
import wth.rpc.registry.LocalRegistry;
import wth.rpc.registry.Registry;
import wth.rpc.*;
import wth.rpc.registry.RegistryFactory;

/**
 * Rpc 服务提供者启动
 *

 */
@Slf4j
public class RpcProviderBootstrap implements BeanPostProcessor {
/**通常情况下，BeanPostProcessor接口的实现类用于实现一些AOP（面向切面编程）的功能，
 * 比如在Bean初始化前后进行日志记录、权限检查、性能监控等。
 */
    /**
     * Bean 初始化后执行，注册服务
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */

    /**这段代码是`BeanPostProcessor`接口中`postProcessAfterInitialization`方法的实现。
     * 这个方法在Spring容器完成Bean的初始化后调用，参数`bean`表示被处理的Bean对象，`beanName`表示该Bean在Spring容器中的名称。

     让我们来逐行解释：

     1. `@Override`：这个注解表明该方法是一个覆盖（重写）了父类或接口中的方法。
     2. `public Object postProcessAfterInitialization(Object bean, String beanName)
     throws BeansException`：这是`BeanPostProcessor`接口的方法签名。它接收两个参数：`bean`是被处理的Bean对象，
     `beanName`是该Bean在Spring容器中的名称。
     3. `Class<?> beanClass = bean.getClass();`：这一行代码获取了被处理的Bean对象的Class对象，`
     bean.getClass()`返回的是Bean对象的运行时类。
     4. `RpcService rpcService = beanClass.getAnnotation(RpcService.class);`：
     这一行代码通过反射获取了被处理的Bean对象的`RpcService`注解。`getAnnotation()`方法返回与此类相关的指定类型的注解，
     如果该类型的注解不存在，则返回null。
     总的来说，这段代码的作用是在Spring容器初始化Bean之后，检查这个Bean是否标记了`@RpcService`注解，
     如果标记了，就获取对应的注解对象，以便后续处理。*/
    @Override
    /*提供Service注册,实现bean初始化后的调用:通过bean类拿到class然后拿到注解，再那到服务实现类和其版本，
    然后注册服务：本地hashMap注册和ZTCD/Zookeeper注册：把类信息放入到注册中心中.*/
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        //
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);//获取bean类上的注解对象
        if (rpcService != null) {
            // 需要注册服务
            // 1. 获取服务基本信息
            Class<?> interfaceClass = rpcService.interfaceClass();
            // 2.默认值处理
            if (interfaceClass == void.class) {
                interfaceClass = beanClass.getInterfaces()[0];
            }
            String serviceName = interfaceClass.getName();
            String serviceVersion = rpcService.serviceVersion();
            // 2. 注册服务
            // 本地注册
            LocalRegistry.register(serviceName, beanClass);

            // 全局配置
            final RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            // 注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(serviceVersion);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 服务注册失败", e);
            }
        }

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
    /**
     * 这段代码是`BeanPostProcessor`接口中`postProcessAfterInitialization`方法的实现的一部分。它在处理Bean初始化完成后的阶段，对标记了`@RpcService`注解的Bean进行了服务注册的操作。让我们逐行解释：
     *
     * 1. `Class<?> interfaceClass = rpcService.interfaceClass();`：获取`@RpcService`注解中声明的服务接口类。
     *
     * 2. 默认值处理：如果`interfaceClass`为`void.class`，则说明在`@RpcService`注解中没有明确指定服务接口类，那么就默认取该Bean对象实现的第一个接口作为服务接口类。这样做的目的是为了提供一种便捷的方式，使得用户不必显式地在`@RpcService`注解中指定接口类。
     *
     * 3. `String serviceName = interfaceClass.getName();`：获取服务接口类的全限定名，作为服务名称。
     *
     * 4. `String serviceVersion = rpcService.serviceVersion();`：获取`@RpcService`注解中声明的服务版本号。
     *
     * 5. `LocalRegistry.register(serviceName, beanClass);`：将服务信息注册到本地注册表中。这里假设了存在一个`LocalRegistry`类用于本地服务注册，将服务名称和对应的Bean类注册到本地，以便后续的服务调用。
     *
     * 6. 获取全局配置：获取RPC框架的全局配置，其中包含了注册中心的相关配置信息。
     *
     * 7. `RegistryConfig registryConfig = rpcConfig.getRegistryConfig();`：从全局配置中获取注册中心的配置信息。
     *
     * 8. `Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());`：根据注册中心的类型，获取对应的注册中心实例。这里使用了工厂模式来创建注册中心实例，通过`RegistryFactory`根据配置的注册中心类型来创建对应的注册中心对象。
     *
     * 9. 创建服务元信息对象：将服务的基本信息封装到`ServiceMetaInfo`对象中，包括服务名称、版本号、主机地址和端口号。
     *
     * 10. `registry.register(serviceMetaInfo);`：将服务元信息注册到注册中心。这一步将服务的元信息发送给注册中心，让注册中心能够发现和管理这个服务。
     *
     * 11. 异常处理：如果注册服务过程中出现异常，将异常包装成`RuntimeException`并抛出，同时记录失败的服务名称。
     *
     * 12. 最后一行 `return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);`：调用父类的`postProcessAfterInitialization`方法，这是为了保证在处理完之后，仍然能够按照Spring的正常流程继续进行后续的Bean初始化操作。
     *
     * 总的来说，这段代码实现了对标记了`@RpcService`注解的Bean进行服务注册的逻辑。它将服务注册到本地注册表和注册中心，以便于服务的提供和发现。*/
}
