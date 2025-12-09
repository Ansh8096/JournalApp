package net.engineerAnsh.journalApp.Services;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Disabled
    @Test
    void Test1(){ // This is just a test to check whether a connection with 'redis' is made successfully or not.
        redisTemplate.opsForValue().set("email","anshv8096@gmail.com");
        Object value = redisTemplate.opsForValue().get("name");
        int a = 109;
    }
}
