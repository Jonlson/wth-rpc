package wth.rpc.redis.service;


import wth.rpc.redis.RedisIDKeyEnum;

import java.util.List;

public interface RedisService {
    Long getLoopId(RedisIDKeyEnum keyEnum);

    List<Long> getBatchLoopIds(RedisIDKeyEnum keyEnum, int count);
}
