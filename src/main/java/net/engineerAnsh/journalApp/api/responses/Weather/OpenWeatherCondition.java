package net.engineerAnsh.journalApp.api.responses.Weather;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenWeatherCondition {

    private int id;

    private String main;

    private String description;

    private String icon;
}
