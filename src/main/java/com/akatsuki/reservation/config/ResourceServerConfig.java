package com.akatsuki.reservation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ResourceServerConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/v1/reservation").hasRole("GUEST")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/reservation/cancel/{reservationId}").hasRole("GUEST")
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservation/by-user-and-status").hasRole("GUEST")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/reservation/deny/{reservationId}").hasRole("HOST")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/reservation/approve/{reservationId}").hasRole("HOST")
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservation/check-reservations-of-accommodation").hasRole("HOST")
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservation/check-host-reservations").hasRole("HOST")
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservation/check-guest-reservations").hasRole("GUEST")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                        )
                );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        final JwtGrantedAuthoritiesConverter gac = new JwtGrantedAuthoritiesConverter();
        gac.setAuthoritiesClaimName("roles");
        gac.setAuthorityPrefix("ROLE_");

        final JwtAuthenticationConverter jac = new JwtAuthenticationConverter();
        jac.setJwtGrantedAuthoritiesConverter(gac);
        return jac;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withIssuerLocation(issuer).build();
    }

}