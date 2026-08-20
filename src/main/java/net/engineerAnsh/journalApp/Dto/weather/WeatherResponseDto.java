package net.engineerAnsh.journalApp.Dto.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherResponseDto {

    private String city;

    private double temperature;

    private String temperatureUnit;

    private String description;

    private int humidity;

    private double windSpeed;

    private String windSpeedUnit;

    private double feelsLike;

    private String feelsLikeUnit;

    private String icon;
}