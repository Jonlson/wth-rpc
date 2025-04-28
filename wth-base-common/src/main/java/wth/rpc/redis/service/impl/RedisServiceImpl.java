package wth.rpc.redis.service.impl;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import wth.rpc.redis.RedisIDKeyEnum;
import wth.rpc.redis.service.RedisService;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RedisServiceImpl implements RedisService {
    @Resource
    private RedisTemplate<String, Serializable> redisTemplate;

    @Override
    public Long getLoopId(RedisIDKeyEnum keyEnum) {
        String script = "local val = tonumber(redis.call('INCRBY', KEYS[1], 1)) " +
                        "if val > " + keyEnum.getMax() + " then " +
                        "   redis.call('SET', KEYS[1], '0') " +
                        "   return tonumber(redis.call('INCRBY', KEYS[1], 1)) " +
                        "else " +
                        "   return val " +
                        "end;";

        List<String> keyList = new ArrayList<>();
        keyList.add("@id@" + keyEnum.getKey());

        return redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), keyList);
    }

    @Override
    public List<Long> getBatchLoopIds(RedisIDKeyEnum keyEnum, int count) {
        String script = "local key = KEYS[1] " +
                        "local count = ARGV[1] " +
                        "local results = {} " +
                        "for i = 1, count do " +
                        "   local val = redis.call('INCR', key) " +
                        "   if val > " + keyEnum.getMax() + " then " +
                        "       redis.call('SET', key, 1) " +
                        "       val = 1 " +
                        "   end " +
                        "   table.insert(results, val) " +
                        "end " +
                        "return results";

        List<String> keys = Collections.singletonList("@id@" + keyEnum.getKey());

        @SuppressWarnings("unchecked")
        List<Long> loopIdList = redisTemplate.execute(new DefaultRedisScript<>(script, List.class), keys, count);
        return loopIdList;
    }


}
