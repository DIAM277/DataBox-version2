package com.databox.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component("redisUtils")
@Slf4j
public class RedisUtils<V> {

    @Resource
    private RedisTemplate<String, V> redisTemplate;

    // 获取 redis 值
    public V get(String key){
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key 值
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, V value){
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        }catch (Exception e){
            log.error("设置redisKey:(),Value:()失败", key, value);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key 值
     * @param value 值
     * @param time 时间(秒) time要大于0 如果time小于等于0 将设置无限制
     * @return true成功 false失败
     */
    public boolean setx(String key, V value, long time){
        try{
            if(time > 0){
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            }else {
                set(key, value);
            }
            return true;
        }catch (Exception e){
            log.error("设置redisKey:(),Value:()失败", key, value);
            return false;
        }
    }

    /**
     * 数值自增方法
     * @param key
     * @param delta
     * @return
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis自增失败 key:{}", key, e);
            return null;
        }
    }

    /**
     * 删除 Redis 中的键
     * @param key 键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 判断Redis中指定Key是否存在
     * @param key 键
     * @return true:存在 false:不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("判断Redis Key是否存在失败, key:{}", key, e);
            return false; // 异常时默认返回不存在，避免影响业务逻辑
        }
    }
}
