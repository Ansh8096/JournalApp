package net.engineerAnsh.journalApp.Service;

import lombok.RequiredArgsConstructor;

import net.engineerAnsh.journalApp.Dto.auth.GoogleProvisioningResult;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Repository.UserRepository;
import net.engineerAnsh.journalApp.enums.AuthProvider;
import net.engineerAnsh.journalApp.enums.GoogleProvisioningStatus;
import net.engineerAnsh.journalApp.enums.OAuthFlow;
import net.engineerAnsh.journalApp.enums.Role;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleUserProvisioningService {

    private final UserRepository userRepository;

    public GoogleProvisioningResult findOrCreateUser(
            OidcUser oidcUser,
            OAuthFlow oauthFlow
    ) {

        /*
         * ----------------------------------------
         * 1. READ GOOGLE IDENTITY
         * ----------------------------------------
         */
        String googleSubject =
                oidcUser.getSubject();

        String email =
                oidcUser.getEmail();

        String name =
                oidcUser.getFullName();

        String profileImageUrl =
                oidcUser.getPicture();

        /*
         * ----------------------------------------
         * 2. VALIDATE IDENTITY
         * ----------------------------------------
         */
        if (
                googleSubject == null ||
                        googleSubject.isBlank()
        ) {
            throw new IllegalStateException(
                    "Google account identifier is missing."
            );
        }

        if (
                email == null ||
                        email.isBlank()
        ) {
            throw new IllegalStateException(
                    "Google email is missing."
            );
        }

        Boolean emailVerified =
                oidcUser.getClaimAsBoolean(
                        "email_verified"
                );

        if (
                !Boolean.TRUE.equals(
                        emailVerified
                )
        ) {
            throw new IllegalStateException(
                    "Google email address is not verified."
            );
        }

        /*
         * ----------------------------------------
         * 3. FIRST LOOKUP:
         *    GOOGLE SUBJECT
         * ----------------------------------------
         */
        Optional<User> googleUser =
                userRepository.findByGoogleSubject(
                        googleSubject
                );

        if (googleUser.isPresent()) {

            /*
             * ------------------------------------
             * Existing Google account
             * ------------------------------------
             */
            return handleExistingGoogleUser(
                    googleUser.get(),
                    oauthFlow
            );
        }

        /*
         * ----------------------------------------
         * 4. SECOND LOOKUP:
         *    JOURNALFLOW EMAIL
         * ----------------------------------------
         */
        Optional<User> existingUser =
                userRepository.findByEmail(
                        email
                );

        if (existingUser.isPresent()) {

            return handleExistingEmailUser(
                    existingUser.get(),
                    googleSubject,
                    oauthFlow
            );
        }

        /*
         * ----------------------------------------
         * 5. NO EXISTING ACCOUNT
         * ----------------------------------------
         */
        if (
                oauthFlow == OAuthFlow.LOGIN
        ) {

            /*
             * LOGIN must never create an account.
             */
            return GoogleProvisioningResult.builder()
                    .status(
                            GoogleProvisioningStatus
                                    .SIGNUP_REQUIRED
                    )
                    .build();
        }

        /*
         * ----------------------------------------
         * 6. SIGNUP → CREATE ACCOUNT
         * ----------------------------------------
         */
        User newUser =
                createGoogleUser(
                        googleSubject,
                        email,
                        name,
                        profileImageUrl
                );

        return GoogleProvisioningResult.builder()
                .status(
                        GoogleProvisioningStatus
                                .ACCOUNT_CREATED
                )
                .user(newUser)
                .build();
    }

    private GoogleProvisioningResult handleExistingGoogleUser(
            User user,
            OAuthFlow oauthFlow
    ) {

        /*
         * LOGIN:
         *
         * Existing Google account → Login.
         */
        if (
                oauthFlow == OAuthFlow.LOGIN
        ) {

            return GoogleProvisioningResult.builder()
                    .status(
                            GoogleProvisioningStatus
                                    .AUTHENTICATE
                    )
                    .user(user)
                    .build();
        }

        /*
         * SIGNUP:
         *
         * Account already exists → Login page.
         */
        return GoogleProvisioningResult.builder()
                .status(
                        GoogleProvisioningStatus
                                .ACCOUNT_EXISTS
                )
                .user(user)
                .build();
    }

    private GoogleProvisioningResult handleExistingEmailUser(
            User user,
            String googleSubject,
            OAuthFlow oauthFlow
    ) {

        /*
         * ----------------------------------------
         * LOGIN
         * ----------------------------------------
         *
         * Existing LOCAL account with the same
         * verified Google email.
         *
         * Link Google and authenticate.
         */
        if (
                oauthFlow == OAuthFlow.LOGIN
        ) {

            /*
             * Preserve:
             *
             * - existing password
             * - existing username
             * - existing provider
             */
            user.setGoogleSubject(
                    googleSubject
            );

            if (
                    user.getAuthProvider() == null
            ) {

                user.setAuthProvider(
                        AuthProvider.LOCAL
                );
            }

            User savedUser =
                    userRepository.save(
                            user
                    );

            return GoogleProvisioningResult.builder()
                    .status(
                            GoogleProvisioningStatus
                                    .AUTHENTICATE
                    )
                    .user(savedUser)
                    .build();
        }

        /*
         * ----------------------------------------
         * SIGNUP
         * ----------------------------------------
         *
         * A signup attempt must NOT modify an
         * existing account.
         */
        return GoogleProvisioningResult.builder()
                .status(
                        GoogleProvisioningStatus
                                .ACCOUNT_EXISTS
                )
                .user(user)
                .build();
    }

    private User createGoogleUser(
            String googleSubject,
            String email,
            String name,
            String profileImageUrl
    ) {

        String username =
                generateUniqueUsername(
                        name,
                        email
                );

        User user =
                User.builder()
                        .username(
                                username
                        )
                        .password(null)
                        .email(email)
                        .authProvider(
                                AuthProvider.GOOGLE
                        )
                        .googleSubject(
                                googleSubject
                        )
                        .profileImageUrl(
                                profileImageUrl
                        )
                        .city(null)
                        .sentimentAnalysis(false)
                        .roles(
                                List.of(
                                        Role.USER
                                )
                        )
                        .journals(
                                new ArrayList<>()
                        )
                        .build();

        return userRepository.save(user);
    }

    private String generateUniqueUsername(
            String name,
            String email
    ) {

        String baseUsername =
                createBaseUsername(
                        name,
                        email
                );

        if (
                !userRepository.existsByUsername(
                        baseUsername
                )
        ) {

            return baseUsername;
        }

        int suffix = 2;

        while (
                userRepository.existsByUsername(
                        baseUsername + suffix
                )
        ) {

            suffix++;
        }

        return baseUsername + suffix;
    }

    private String createBaseUsername(
            String name,
            String email
    ) {

        String source =
                name != null &&
                        !name.isBlank()
                        ? name
                        : email.substring(
                        0,
                        email.indexOf('@')
                );

        String base =
                source
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .replaceAll(
                                "[^a-z0-9]",
                                ""
                        );

        if (base.isBlank()) {
            base = "user";
        }

        return base.length() > 30
                ? base.substring(0, 30)
                : base;
    }
}