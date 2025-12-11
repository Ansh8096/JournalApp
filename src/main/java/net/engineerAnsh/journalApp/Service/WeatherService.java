package net.engineerAnsh.journalApp.Service;

import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Cache.AppCache;
import net.engineerAnsh.journalApp.Constants.Placeholders;
import net.engineerAnsh.journalApp.api.responses.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class WeatherService {

    @Value("${weather.API.Key}")
    private String apiKey; // we can't use static here , ...

    @Autowired
    private RestTemplate restTemplate; // RestTemplate is used to hit the external API's...

    @Autowired
    private AppCache appCacheClass;

    @Autowired
    private RedisService redisService;

    public WeatherResponse getWeather(String city) {

        WeatherResponse weatherResponseByRedis = redisService.get("weather_of_" + city, WeatherResponse.class);
        if (weatherResponseByRedis != null) {
            return weatherResponseByRedis;
        }
        else {
            String weatherApi = AppCache.Keys.WEATHER_API.toString();
            String finalAPI = appCacheClass.getAppCache().get(weatherApi).replace(Placeholders.ApiKey, apiKey).replace(Placeholders.City, city);
            ResponseEntity<WeatherResponse> response = restTemplate.exchange(finalAPI, HttpMethod.GET, null, WeatherResponse.class); // here 'WeatherResponse' is an POJO class for the Json that I will get (after hitting thr API)...
            HttpStatusCode statusCode = response.getStatusCode();// It gives us the HttpCode ,which we get by hitting this API...
            if (response.hasBody()) {
                WeatherResponse body = response.getBody();
                redisService.set("weather_of_" + city, body, 600l);
                return body;
            }
            if(statusCode.is4xxClientError() || statusCode.is5xxServerError()){
                log.error("Error occurred while getting the weather...");
            }
            return null;
        }
    }
}






// Steps for the External API postCall:--------->
// We can send 'Json' body or the 'Object' of class in the Post call of an API with the help of 'HttpEntity'...

        // 1. JsonBody:--
        /*  String jsonBody = "{\n" +
                "    \"userName\": \"Ansh\",\n" +
                "    \"password\" : \"Ansh\"\n" +
                "}";
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonBody);

        // 2. User:---
        User user = User.builder().userName("Ansh").password("Ansh").build();
        HttpEntity<User> httpEntity2 = new HttpEntity<>(user);


        // We can also send the headers in the Post call of an API , because it can expect the arguments in the header...
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Key","Value");


        ResponseEntity<WeatherResponse> responseForPostCall = restTemplate.exchange(finalAPI, HttpMethod.POST, httpEntity, WeatherResponse.class);  */
