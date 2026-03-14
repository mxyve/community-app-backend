package top.xym.community.app.common.cache;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存工具类
 */
@Component
@RequiredArgsConstructor
public class RedisCache {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存
     * @Param key 键
     * @Param value 值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存，带过期时间
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取换从（指定类型）
     *
     * @param key 键
     * @param clazz 类型
     * @return 值
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    /**
     * 删除缓存
     * @Param key 键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

}
