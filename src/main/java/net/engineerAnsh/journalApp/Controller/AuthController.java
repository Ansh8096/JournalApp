package net.engineerAnsh.journalApp.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Dto.common.MessageResponseDto;
import net.engineerAnsh.journalApp.Dto.auth.LoginRequestDto;
import net.engineerAnsh.journalApp.Dto.auth.RegisterRequestDto;
import net.engineerAnsh.journalApp.Service.UserDetailsServiceImpl;
import net.engineerAnsh.journalApp.Service.UserService;
import net.engineerAnsh.journalApp.Utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
@Slf4j
@Tag(name = "Public APIs", description = "healthCheck, login or signup")
public class PublicController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @GetMapping("health-Check")
    @Operation(summary = "Application's current health status")
    public String healthCheckup() {
        return "ok";
    }

    @Operation(summary = "Registers a new user into the system")
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody RegisterRequestDto registerRequestDto) {
        userService.saveNewUser(registerRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageResponseDto.builder()
                        .message("User created successfully.")
                        .build()
                );
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticates a user and returns an access token")
    public ResponseEntity<String> logIn(@RequestBody LoginRequestDto loginRequestDto) {
        // This 'authenticate()' is internally calling our 'UserDetailsServiceImpl' to authenticate the requested user...
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getUsername(),
                        loginRequestDto.getPassword()
                )
        );

        // And also load the user by using 'loadUserByUsername()' (present inside the UserDetailsServiceImpl)...
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequestDto.getUsername());

        String jwt = jwtUtils.generateToken(userDetails.getUsername());

        return new ResponseEntity<>(jwt, HttpStatus.OK);
    }

}
