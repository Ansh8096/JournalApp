package net.engineerAnsh.journalApp.Controller;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import net.engineerAnsh.journalApp.Config.security.OAuth2SessionConstants;
import net.engineerAnsh.journalApp.enums.OAuthFlow;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth/oauth2/google")
@RequiredArgsConstructor
public class GoogleOAuth2Controller {

    /**
     * Starts Google OAuth from the LOGIN page.
     */
    @GetMapping("/login")
    public ResponseEntity<Void> login(
            HttpSession session
    ) {

        session.setAttribute(
                OAuth2SessionConstants.GOOGLE_OAUTH_FLOW,
                OAuthFlow.LOGIN
        );

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(
                        URI.create(
                                "/oauth2/authorization/google"
                        )
                )
                .build();
    }

    /**
     * Starts Google OAuth from the SIGNUP page.
     */
    @GetMapping("/signup")
    public ResponseEntity<Void> signup(
            HttpSession session
    ) {

        session.setAttribute(
                OAuth2SessionConstants.GOOGLE_OAUTH_FLOW,
                OAuthFlow.SIGNUP
        );

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(
                        URI.create(
                                "/oauth2/authorization/google"
                        )
                )
                .build();
    }
}