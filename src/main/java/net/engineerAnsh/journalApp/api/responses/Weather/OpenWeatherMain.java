package net.engineerAnsh.journalApp.api.responses.Weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenWeatherMain {

    private double temp;

    @JsonProperty("feels_like")
    private double feelsLike;

    private int humidity;
}