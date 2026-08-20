package net.engineerAnsh.journalApp.Controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import net.engineerAnsh.journalApp.Dto.weather.WeatherResponseDto;
import net.engineerAnsh.journalApp.Service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @Operation(
            summary = "Get current weather for the authenticated user"
    )
    @GetMapping
    public ResponseEntity<WeatherResponseDto> getWeather() {

        return ResponseEntity.ok(
                weatherService.getWeather()
        );
    }
}