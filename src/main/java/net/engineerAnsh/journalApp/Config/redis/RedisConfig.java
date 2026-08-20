package net.engineerAnsh.journalApp.Config.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.username}")
    private String username;

    @Value("${spring.data.redis.password}")
    private String password;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {

        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration();

        config.setHostName(host);
        config.setPort(port);
        config.setUsername(username);
        config.setPassword(password);

        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory factory
    ) {

        RedisTemplate<String, String> redisTemplate =
                new RedisTemplate<>();

        redisTemplate.setConnectionFactory(factory);

        redisTemplate.setKeySerializer(
                new StringRedisSerializer()
        );

        redisTemplate.setValueSerializer(
                new StringRedisSerializer()
        );

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}