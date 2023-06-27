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

    private final String guestRole = "GUEST";
    private final String hostRole = "HOST";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservation/requested").hasRole(hostRole)
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservation/approved").hasRole(guestRole)
                        .requestMatchers(HttpMethod.POST, "/api/v1/reservation").hasRole(guestRole)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/reservation/cancel/{reservationId}").hasRole(guestRole)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/reservation/deny/{reservationId}").hasRole(hostRole)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/reservation/approve/{reservationId}").hasRole(hostRole)
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservation/check-reservations-of-accommodation").hasRole(hostRole)
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservation/check-host-reservations").hasRole(hostRole)
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservation/check-guest-reservations").hasRole(guestRole)
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