package com.example.redis;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@EnableCaching
@SpringBootApplication
public class RedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(SlowService service) {
        return _ -> {
            IO.println("result " + service.slowMethod());
            IO.println("result " + service.slowMethod());
        };
    }
}

@Service
class SlowService {

    private final RedisTemplate<String, Object> redisTemplate;

    SlowService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @CacheEvict("my-values")
    public void update() {

    }

    @Cacheable(cacheNames = "my-values")
    public String slowMethod() throws Exception {
        Thread.sleep(5000);
        return "slowMethod";
    }


}