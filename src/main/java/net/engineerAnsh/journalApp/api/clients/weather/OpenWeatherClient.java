package net.engineerAnsh.journalApp.api.clients.weather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Config.weather.WeatherProperties;
import net.engineerAnsh.journalApp.api.responses.Weather.GeocodingResponse;
import net.engineerAnsh.journalApp.api.responses.Weather.OpenWeatherResponse;
import net.engineerAnsh.journalApp.exception.exceptions.WeatherServiceException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenWeatherClient {

    private final RestTemplate restTemplate;

    private final WeatherProperties weatherProperties;


    public List<GeocodingResponse> geocodeCity(
            String city
    ) {

        URI uri =
                UriComponentsBuilder
                        .fromUriString(
                                weatherProperties.getGeocodingUrl()
                        )
                        .queryParam(
                                "q",
                                city
                        )
                        .queryParam(
                                "limit",
                                1
                        )
                        .queryParam(
                                "appid",
                                weatherProperties.getApiKey()
                        )
                        .build()
                        .encode()
                        .toUri();

        try {

            ResponseEntity<List<GeocodingResponse>> response =
                    restTemplate.exchange(
                            uri,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {
                            }
                    );

            return response.getBody();

        } catch (ResourceAccessException ex) {

            log.error(
                    "OpenWeather geocoding request failed due to " +
                            "connection/timeout. city={}",
                    city,
                    ex
            );

            throw new WeatherServiceException(
                    "Weather service is currently unavailable.",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    ex
            );

        } catch (HttpStatusCodeException ex) {

            throw translateOpenWeatherError(
                    ex,
                    "geocoding",
                    "city=" + city
            );
        }
    }


    public OpenWeatherResponse getCurrentWeather(
            double latitude,
            double longitude
    ) {

        URI uri =
                UriComponentsBuilder
                        .fromUriString(
                                weatherProperties.getBaseUrl()
                        )
                        .queryParam(
                                "lat",
                                latitude
                        )
                        .queryParam(
                                "lon",
                                longitude
                        )
                        .queryParam(
                                "appid",
                                weatherProperties.getApiKey()
                        )
                        .queryParam(
                                "units",
                                weatherProperties.getUnits()
                        )
                        .build()
                        .encode()
                        .toUri();

        try {

            ResponseEntity<OpenWeatherResponse> response =
                    restTemplate.exchange(
                            uri,
                            HttpMethod.GET,
                            null,
                            OpenWeatherResponse.class
                    );

            return response.getBody();

        } catch (ResourceAccessException ex) {

            log.error(
                    "OpenWeather current-weather request failed due " +
                            "to connection/timeout. lat={}, lon={}",
                    latitude,
                    longitude,
                    ex
            );

            throw new WeatherServiceException(
                    "Weather service is currently unavailable.",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    ex
            );

        } catch (HttpStatusCodeException ex) {

            throw translateOpenWeatherError(
                    ex,
                    "current weather",
                    "lat=" + latitude + ", lon=" + longitude
            );
        }
    }


    private WeatherServiceException translateOpenWeatherError(
            HttpStatusCodeException ex,
            String operation,
            String context
    ) {

        HttpStatus status =
                HttpStatus.resolve(
                        ex.getStatusCode().value()
                );

        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }

        log.error(
                "OpenWeather {} request failed. status={}, {}",
                operation,
                status,
                context,
                ex
        );

        if (status == HttpStatus.UNAUTHORIZED ||
                status == HttpStatus.FORBIDDEN) {

            return new WeatherServiceException(
                    "Weather service authentication failed.",
                    HttpStatus.BAD_GATEWAY,
                    ex
            );
        }

        if (status == HttpStatus.TOO_MANY_REQUESTS) {

            return new WeatherServiceException(
                    "Weather service rate limit has been reached.",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    ex
            );
        }

        if (status.is5xxServerError()) {

            return new WeatherServiceException(
                    "Weather service is currently unavailable.",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    ex
            );
        }

        if (status == HttpStatus.BAD_REQUEST) {

            return new WeatherServiceException(
                    "Invalid weather request.",
                    HttpStatus.BAD_REQUEST,
                    ex
            );
        }

        return new WeatherServiceException(
                "Unable to retrieve weather information.",
                HttpStatus.BAD_GATEWAY,
                ex
        );
    }
}