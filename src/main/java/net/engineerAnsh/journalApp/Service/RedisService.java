package net.engineerAnsh.journalApp.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    public <T> T get(String city, Class<T> entityClass){
        try {
            Object o = redisTemplate.opsForValue().get(city);
            // Down here we are converting the Object 'o' into the required response(here i.e: 'WeatherResponse') which we want to return...
            if (o == null) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(o.toString(),entityClass);
        } catch (Exception e) {
            log.error("Exception Occurred while getting data from Redis..." ,e);
            return null;
        }
    }

    public void set(String key ,Object o, Long ttl){
        // 'ttl' -> stands for Time-Limit...
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonValue = objectMapper.writeValueAsString(o);
            redisTemplate.opsForValue().set(key, jsonValue , ttl , TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Exception Occurred while storing data in Redis..." ,e);
        }
    }
}

