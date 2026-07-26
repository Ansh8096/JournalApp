package net.engineerAnsh.journalApp.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Dto.auth.LoginResponseDto;
import net.engineerAnsh.journalApp.Dto.common.MessageResponseDto;
import net.engineerAnsh.journalApp.Dto.auth.LoginRequestDto;
import net.engineerAnsh.journalApp.Dto.auth.RegisterRequestDto;
import net.engineerAnsh.journalApp.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@Tag(name = "Auth APIs", description = "login or signup")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Registers a new user into the system")
    @PostMapping("/signup")
    public ResponseEntity<MessageResponseDto> signUp(@RequestBody @Valid RegisterRequestDto registerRequestDto) {
        authService.signup(registerRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageResponseDto.builder()
                        .message("User created successfully.")
                        .build()
                );
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticates a user and returns an access token")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

}
