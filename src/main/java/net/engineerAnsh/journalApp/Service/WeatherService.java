package net.engineerAnsh.journalApp.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Config.weather.WeatherProperties;
import net.engineerAnsh.journalApp.Dto.user.UserProfileResponseDto;
import net.engineerAnsh.journalApp.Dto.weather.WeatherResponseDto;
import net.engineerAnsh.journalApp.api.clients.weather.OpenWeatherClient;
import net.engineerAnsh.journalApp.api.responses.Weather.GeocodingResponse;
import net.engineerAnsh.journalApp.api.responses.Weather.OpenWeatherCondition;
import net.engineerAnsh.journalApp.api.responses.Weather.OpenWeatherResponse;
import net.engineerAnsh.journalApp.exception.exceptions.BadRequestException;
import net.engineerAnsh.journalApp.exception.exceptions.ResourceNotFoundException;
import net.engineerAnsh.journalApp.exception.exceptions.WeatherServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final OpenWeatherClient openWeatherClient;
    private final RedisService redisService;
    private final WeatherProperties weatherProperties;
    private final UserService userService;
    private static final String WEATHER_CACHE_PREFIX = "weather:v1:";


    public WeatherResponseDto getWeather() {

        UserProfileResponseDto user = userService.getUser();

        String city =
                user.getCity();

        if (city == null ||
                city.isBlank()) {

            throw new BadRequestException(
                    "Please set your city before requesting weather."
            );
        }

        return getWeatherForCity(city);
    }


    private WeatherResponseDto getWeatherForCity(
            String city
    ) {

        String normalizedCity =
                normalizeCity(city);

        String cacheKey =
                buildCacheKey(normalizedCity);

        // -----------------------------------------------------
        // Redis cache
        // -----------------------------------------------------

        WeatherResponseDto cachedWeather =
                redisService.get(
                        cacheKey,
                        WeatherResponseDto.class
                );

        if (cachedWeather != null) {
            return cachedWeather;
        }

        // -----------------------------------------------------
        // Geocoding
        // -----------------------------------------------------

        List<GeocodingResponse> locations =
                openWeatherClient.geocodeCity(
                        normalizedCity
                );

        if (locations == null ||
                locations.isEmpty()) {

            throw new ResourceNotFoundException(
                    "Weather location not found for city: "
                            + city
            );
        }

        GeocodingResponse location =
                locations.get(0);

        // -----------------------------------------------------
        // Current weather
        // -----------------------------------------------------

        OpenWeatherResponse weather =
                openWeatherClient.getCurrentWeather(
                        location.getLat(),
                        location.getLon()
                );

        if (weather == null) {

            throw new WeatherServiceException(
                    "Unable to retrieve current weather.",
                    HttpStatus.BAD_GATEWAY
            );
        }

        // -----------------------------------------------------
        // Validate provider response
        // -----------------------------------------------------

        validateWeatherResponse(
                weather
        );

        // -----------------------------------------------------
        // Map provider response
        // -----------------------------------------------------

        WeatherResponseDto response =
                mapToWeatherResponse(
                        weather,
                        location
                );

        // -----------------------------------------------------
        // Cache application DTO
        // -----------------------------------------------------

        redisService.set(
                cacheKey,
                response,
                weatherProperties
                        .getCacheTtlSeconds()
        );

        return response;
    }


    private void validateWeatherResponse(
            OpenWeatherResponse weather
    ) {

        if (weather.getMain() == null) {

            throw new WeatherServiceException(
                    "Weather response is missing temperature data.",
                    HttpStatus.BAD_GATEWAY
            );
        }

        if (weather.getWind() == null) {

            throw new WeatherServiceException(
                    "Weather response is missing wind data.",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    private double convertMetersPerSecondToKilometersPerHour(
            double metersPerSecond
    ) {

        return metersPerSecond * 3.6;
    }

    private WeatherResponseDto mapToWeatherResponse(
            OpenWeatherResponse weather,
            GeocodingResponse location
    ) {

        String description = "Unknown";

        String icon = null;

        if (weather.getWeather() != null &&
                !weather.getWeather().isEmpty()) {

            OpenWeatherCondition condition =
                    weather.getWeather().get(0);

            if (condition.getDescription() != null &&
                    !condition.getDescription().isBlank()) {

                description =
                        condition.getDescription();
            }

            icon =
                    condition.getIcon();
        }

        return WeatherResponseDto.builder()
                .city(
                        location.getName()
                )
                .temperature(
                        weather.getMain().getTemp()
                )
                .temperatureUnit(
                        weatherProperties
                                .getTemperatureUnit()
                )
                .description(description)
                .humidity(
                        weather.getMain().getHumidity()
                )
                .windSpeed(
                        convertMetersPerSecondToKilometersPerHour(
                                weather.getWind().getSpeed()
                        )
                )
                .windSpeedUnit(
                        weatherProperties
                                .getWindSpeedUnit()
                )
                .feelsLike(
                        weather.getMain()
                                .getFeelsLike()
                )
                .feelsLikeUnit(
                        weatherProperties
                                .getTemperatureUnit()
                )
                .icon(icon)
                .build();
    }


    private String normalizeCity(
            String city
    ) {

        return city
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }


    private String buildCacheKey(String city) {

        return WEATHER_CACHE_PREFIX
                + city.toLowerCase(Locale.ROOT);
    }
}