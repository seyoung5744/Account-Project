package com.example.account.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisRepositoryConfig {
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort);

        return Redisson.create(config); // 위의 설정을 활용해 Redisson 생성 후 Bean으로 딱 1개만 등록.
        // 어떤 다른 service, controller에서 RedissonClient를 주입 받게 되면 여기서 생성된 RedissonClient가 사용됨.
    }
}
