package com.atguigu.gmall.common.cache;

import com.atguigu.gmall.common.constant.RedisConst;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @title: GmallCacheAspect
 * @Author LiuXianKun
 * @Date: 2020/11/21 11:14
 */
@Aspect
@Component
public class GmallCacheAspect {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;


    @Around(value = "@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object cacheData(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();

        //方法的返回值类型
        Class returnType = methodSignature.getReturnType();

        //获取前缀
        GmallCache gmallCache = methodSignature.getMethod().getAnnotation(GmallCache.class);
        String prefix = gmallCache.prefix();

        String cacheKey = prefix + Arrays.asList(args).toString() + RedisConst.SKUKEY_SUFFIX;
        String lockKey = prefix + Arrays.asList(args).toString() + RedisConst.SKULOCK_SUFFIX;
        //先查询缓存
        Object o = redisTemplate.opsForValue().get(cacheKey);

        //2:如果有 直接返回
        if (null != o) {
            return o;
        } else {
            //解决缓存的击穿问题 上锁  分布式锁 使用redisson
            try {
                boolean b = redissonClient.getLock(lockKey).tryLock(1, 1, TimeUnit.SECONDS);
                if (b) {
                    o = pjp.proceed(args);//带着参数执行该方法
                    if (o == null) { //解决缓存穿透 将空结果保存到缓存 时间不超过5分钟
                        //o = new Object();//json 序列化 要求对象必须实现序列化接口
                        o = returnType.newInstance(); //通过反射创建对象
                        redisTemplate.opsForValue().set(cacheKey, o, 5, TimeUnit.MINUTES);
                        return o;
                    } else {
                        //备份 解决缓存雪崩  不要在同一时间同时失效
                        redisTemplate.opsForValue().set(cacheKey, o, RedisConst.SKUKEY_TIMEOUT + new Random().nextInt(2000), TimeUnit.SECONDS);
                        return o;
                    }
                } else { //没有拿到锁就等着
                    TimeUnit.SECONDS.sleep(1);
                    //从缓存中取
                    return redisTemplate.opsForValue().get(cacheKey);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }finally {
                //解锁
                redissonClient.getLock(lockKey).unlock();
            }
        }
        return null;
    }

}
