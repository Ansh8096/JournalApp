package net.engineerAnsh.journalApp.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper;


    public <T> T get(
            String key,
            Class<T> entityClass
    ) {

        try {

            String value =
                    redisTemplate
                            .opsForValue()
                            .get(key);

            if (value == null) {
                return null;
            }

            return objectMapper.readValue(
                    value,
                    entityClass
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to retrieve Redis value. key={}",
                    key,
                    ex
            );

            /*
             * Cache failure should behave like a cache miss.
             * WeatherService can then fetch fresh data.
             */
            return null;
        }
    }


    public void set(
            String key,
            Object value,
            long ttlSeconds
    ) {

        try {

            String json =
                    objectMapper.writeValueAsString(
                            value
                    );

            redisTemplate
                    .opsForValue()
                    .set(
                            key,
                            json,
                            ttlSeconds,
                            TimeUnit.SECONDS
                    );

        } catch (Exception ex) {

            log.error(
                    "Failed to store Redis value. key={}",
                    key,
                    ex
            );
        }
    }


    public void delete(
            String key
    ) {

        try {

            redisTemplate.delete(key);

        } catch (Exception ex) {

            log.error(
                    "Failed to delete Redis key. key={}",
                    key,
                    ex
            );
        }
    }
}