package org.example.pfabackend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String ADMIN = "admin";
    public static final String USER = "user";
    public static final String COLOCATAIRE = "colocataire";
    private final JwtConverter jwtConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Enable CORS before any other configuration
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // Grouping common access patterns to reduce redundancy
        http.authorizeHttpRequests(authz ->
                authz
                        .requestMatchers(HttpMethod.GET, "/api/hello").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/user/**").hasRole(USER)
                        .requestMatchers(HttpMethod.GET, "/api/coloc/**").hasRole(COLOCATAIRE)
                        .requestMatchers(HttpMethod.GET, "/api/coloc-and-user/**").hasAnyRole(COLOCATAIRE, USER)
                        .requestMatchers(HttpMethod.GET, "/api/colocations/**").permitAll() // GET allowed for COLOCATAIRE, ADMIN, USER
                        .requestMatchers(HttpMethod.POST, "/api/colocations/**").hasAnyRole(COLOCATAIRE, ADMIN) // POST allowed for COLOCATAIRE and ADMIN
                        .requestMatchers(HttpMethod.PUT, "/api/colocations/**").hasAnyRole(COLOCATAIRE, ADMIN) // PUT allowed for COLOCATAIRE and ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/colocations/**").hasAnyRole(COLOCATAIRE, ADMIN) // DELETE allowed for COLOCATAIRE and ADMIN
                        .anyRequest().authenticated()
        );


        // Stateless session management
        http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // OAuth2 resource server with JWT converter
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000")); // Allow frontend origin
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allowed HTTP methods
        config.setAllowedHeaders(List.of("*")); // Allow all headers
        config.setAllowCredentials(true); // Allow cookies or authorization headers

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Apply to all endpoints
        return source;
    }
}