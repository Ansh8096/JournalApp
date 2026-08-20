package net.engineerAnsh.journalApp.Config.weather;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "weather.openweather")
public class WeatherProperties {

    private String baseUrl;

    private String geocodingUrl;

    private String apiKey;

    private String units;

    private String temperatureUnit;

    private String windSpeedUnit;

    private long cacheTtlSeconds = 600;

}