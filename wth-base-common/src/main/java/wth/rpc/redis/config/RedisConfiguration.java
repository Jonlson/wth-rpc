package wth.rpc.redis.config;


import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Configuration
public class RedisConfiguration {
    @Bean
    public RedisTemplate<String, Serializable> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        FastJsonRedisSerializer<Serializable> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Serializable.class);


        redisTemplate.setDefaultSerializer(fastJsonRedisSerializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(fastJsonRedisSerializer);

        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    public static class FastJsonRedisSerializer<T> implements RedisSerializer<T> {

        public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
        private final Class<T> clazz;

        public FastJsonRedisSerializer(Class<T> clazz) {
            super();
            this.clazz = clazz;
        }

        @Override
        public byte[] serialize(T t) throws SerializationException {
            if (null == t) {
                return new byte[0];
            }

            return JSON.toJSONString(t, String.valueOf(SerializerFeature.WriteClassName)).getBytes(DEFAULT_CHARSET);
        }

        @Override
        public T deserialize(byte[] bytes) throws SerializationException {
            if (null == bytes || bytes.length <= 0) {
                return null;
            }

            String str = new String(bytes, DEFAULT_CHARSET);
            return JSON.parseObject(str, clazz, JSONReader.Feature.SupportAutoType);
        }

    }
}
