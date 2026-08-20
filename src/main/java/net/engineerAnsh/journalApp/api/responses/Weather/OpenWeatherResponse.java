package net.engineerAnsh.journalApp.api.responses.Weather;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class OpenWeatherResponse {

    private List<OpenWeatherCondition> weather;

    private OpenWeatherMain main;

    private OpenWeatherWind wind;

    private String name;
}