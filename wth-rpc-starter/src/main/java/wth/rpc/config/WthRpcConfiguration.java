package wth.rpc.config;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


/**
 * 获取yml配置的具体的yml信息,并注入到对应的bean中
 * */
@Data
@Component
@ConfigurationProperties(prefix = "wth.rpc.config")
public class WthRpcConfiguration {
    private RpcConfig rpcConfig;

    // feat :如果没有默认配置就进行使用@Value注解指定默认值, 以下示例

    @Value("${wth.rpc.config.serverUrl:http://default-url.com}")
    private String serverUrl;


}
