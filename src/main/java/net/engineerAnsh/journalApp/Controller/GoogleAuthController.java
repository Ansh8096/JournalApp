package net.engineerAnsh.journalApp.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Service.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth/google")
@Tag(name = "Google login APIs" , description = "Login via google using OAuth2")
@Slf4j
public class GoogleAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Autowired
    GoogleAuthService googleAuthService;

    @GetMapping("/callback")
    @Operation(summary = "Handle Google OAuth2 callback")
    public ResponseEntity<?> handleGoogleCallback(@RequestParam String code){
            try{
                return googleAuthService.GoogleCallback(code,clientId,clientSecret);
            }catch (Exception e){
                log.error("Exception occurred while handleGoogleCallback ", e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
    }

}


