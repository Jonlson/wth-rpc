package wth.rpc.fault.tolerant;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import wth.rpc.model.RpcResponse;

import javax.annotation.Resource;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 其实不用做持久化再消费，因为这个rpc本身用于实时性的调用
 *
 * 失败了3次之后就先使用redis记录，如果超过次数就进行限流 在在外层展示：
 *
 * 记录key: @rpc@tolerant@serviceName@ + @小时  -> 次数，时间戳  过期时间：一个小时
 *
 * 根据记录Key有且大于3次之后就进行限流，一个小时内不允许调用
 *
 *
 * 降级到其他服务 - 容错策略
 *
 * context为ServiceName的map，e为异常信息
 *
 */
@Slf4j
public class FailBackTolerantStrategy implements TolerantStrategy {

    @Resource
    private RedisTemplate<String, Serializable> redisTemplate;

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // 获取当前小时数：
        int hour = LocalDateTime.now().getHour();
        String key = "@rpc@tolerant@" + context.get("serviceName") + "@" + hour;
        // 判断是否超过次数：
        Long count = redisTemplate.opsForValue().increment(key);
        return RpcResponse.builder()
                .message("服务调用失败，请稍后再试")
                .exception(e)
                .build();
    }
}
