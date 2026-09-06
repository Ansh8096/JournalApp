package net.engineerAnsh.journalApp.Config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class GoogleOAuth2FailureHandler
        implements AuthenticationFailureHandler {

    @Value("${app.oauth2.frontend-failure-url}")
    private String frontendFailureUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        String errorCode =
                "google_auth_failed";

        /*
         * OAuth2AuthenticationException contains
         * our application-level OAuth error code.
         */
        if (
                exception
                        instanceof OAuth2AuthenticationException oauthException
                        && oauthException.getError() != null
        ) {

            errorCode =
                    oauthException
                            .getError()
                            .getErrorCode();
        }

        String encodedError =
                URLEncoder.encode(
                        errorCode,
                        StandardCharsets.UTF_8
                );

        response.sendRedirect(
                frontendFailureUrl
                        + "?error="
                        + encodedError
        );
    }
}



//  the frontend will later interpret:
//
//  ?error=google_auth_failed
//
//  and show a user-friendly message such as:
//
//  "Google sign-in failed. Please try again."
//
//  We deliberately don't send:
//
//  exception.getMessage()
//
//  to the browser.