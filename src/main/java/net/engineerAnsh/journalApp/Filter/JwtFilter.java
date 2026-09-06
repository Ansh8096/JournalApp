package net.engineerAnsh.journalApp.Filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import net.engineerAnsh.journalApp.Service.UserDetailsServiceImpl;
import net.engineerAnsh.journalApp.Utils.JwtUtils;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path =
                request.getServletPath();

        /*
         * ----------------------------------------
         * Public authentication endpoints
         * ----------------------------------------
         */
        if (
                path.startsWith("/api/v1/auth")
                        || path.startsWith("/api/v1/public")
                        || path.startsWith("/oauth2/")
                        || path.startsWith("/login/oauth2/")
        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String authorizationHeader =
                request.getHeader(
                        "Authorization"
                );

        /*
         * No Bearer token:
         * simply continue.
         */
        if (
                authorizationHeader == null
                        || !authorizationHeader.startsWith("Bearer ")
        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String jwt =
                authorizationHeader.substring(
                        7
                );

        try {

            /*
             * ----------------------------------------
             * 1. Validate JWT FIRST
             * ----------------------------------------
             */
            if (
                    !jwtUtil.validateToken(
                            jwt
                    )
            ) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            /*
             * ----------------------------------------
             * 2. Only after validation, extract subject
             * ----------------------------------------
             */
            String userName =
                    jwtUtil.extractUsername(
                            jwt
                    );

            if (
                    userName == null
                            || userName.isBlank()
            ) {

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            /*
             * ----------------------------------------
             * 3. Load application user
             * ----------------------------------------
             */
            UserDetails userDetails =
                    userDetailsService
                            .loadUserByUsername(
                                    userName
                            );

            /*
             * ----------------------------------------
             * 4. Build authenticated principal
             * ----------------------------------------
             */
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            auth.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(
                                    request
                            )
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            auth
                    );

        } catch (Exception ex) {

            /*
             * Never let malformed/invalid JWTs
             * turn into HTTP 500 responses.
             *
             * The request simply proceeds without
             * authentication and protected endpoints
             * can return 401/403 through Spring Security.
             */
            SecurityContextHolder
                    .clearContext();
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}