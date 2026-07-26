package net.engineerAnsh.journalApp.Config.security;

import net.engineerAnsh.journalApp.Filter.JwtFilter;
import net.engineerAnsh.journalApp.Service.UserDetailsServiceImpl;
import net.engineerAnsh.journalApp.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

// @Profile("dev") // It means this particular config. will get applied on the 'dev' profile server...
@Configuration// Marks this class as a Spring configuration class, so Spring will read it and create beans defined here...
@EnableWebSecurity// Enables Spring Security’s web security support and allows you to customize the security configuration for your application...
@EnableMethodSecurity // Without @EnableMethodSecurity, Spring will ignore every @PreAuthorize.
public class SecurityConfig { // This 'SecurityConfig' class controls how your entire application handles authentication and authorization...

    @Autowired
    private JwtFilter jwtFilter;

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    // Here we will be start customizing our Spring Security...
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {  // We will be using the 'HttpSecurity' instance to appy all the required filters on our spring security...
        return http
                .cors(Customizer.withDefaults()) // enable CORS in SecurityFilterChain...
                .csrf(AbstractHttpConfigurer::disable) // Disables CSRF protection,CSRF is useful for browser sessions but often disabled in APIs for simplicity...
                .authorizeHttpRequests(request -> request // 'authorizeHttpRequests()' tells the spring to start authorizing the requests...
                        .requestMatchers("/api/v1/journals/**", "/api/v1/users/**").authenticated() // Only users who are logged in can access endpoints starting with /journals/ or /users/...
                        .requestMatchers("/api/v1/admin/**").hasRole(Role.ADMIN.name()) // Only users who are logged in can access endpoints starting with /admin/ and has roles "ADMIN" ...
                        .anyRequest().permitAll())// All other endpoints are accessible without login...
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // applying filter before everytime we do userName and password authentication...
                .build(); // Builds the SecurityFilterChain bean that Spring Security uses to secure your app...
    }

    // We’re injecting our own implementation of UserDetailsService (which loads users from your database)...
    // This is how Spring Security knows where to fetch user details (username, password, roles, etc.) when someone tries to log in...
    @Autowired
    private UserDetailsServiceImpl userDetails;

    // This method used for matching the users details (that is sent in basic auth like: password & userName) with the details of that particular user stored in our dataBase...
    // We will be integrating our 'UserDetailsServiceImpl' in our spring security with the help of this method...
    @Bean
    public DaoAuthenticationProvider configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // 'DaoAuthenticationProvider' is a built-in provider that checks username/password against a database (or any userDetails)...
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetails); // → Uses your custom service (here: by the name of the user) to fetch user data ...
        authProvider.setPasswordEncoder(passwordEncoder()); // → Uses BCrypt hashing to check passwords...

        return authProvider; // Returning this bean registers it with Spring Security so authentication works...
    }

    @Bean
    // Creates a BCryptPasswordEncoder bean, which is used to hash passwords securely in your database...
    // Whenever a password is checked during login, Spring will hash the input password and compare it with the stored hash...
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration auth) throws Exception {
        return auth.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(allowedOrigin));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
