package net.engineerAnsh.journalApp.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WeatherConfig {

    @Bean
    public RestTemplate getInstance(){
        return new RestTemplate();
    }
}
