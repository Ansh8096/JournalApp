package net.engineerAnsh.journalApp.api.responses.Weather;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeocodingResponse {

    private String name;

    private double lat;

    private double lon;

    private String country;

    private String state;
}