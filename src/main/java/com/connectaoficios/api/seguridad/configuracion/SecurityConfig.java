package com.connectaoficios.api.seguridad.configuracion;

import com.connectaoficios.api.seguridad.ManejadorAccesoDenegado;
import com.connectaoficios.api.seguridad.ManejadorNoAutenticado;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final ManejadorNoAutenticado manejadorNoAutenticado;
    private final ManejadorAccesoDenegado manejadorAccesoDenegado;

    public SecurityConfig(
            JwtAuthenticationConverter jwtAuthenticationConverter,
            ManejadorNoAutenticado manejadorNoAutenticado,
            ManejadorAccesoDenegado manejadorAccesoDenegado
    ) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.manejadorNoAutenticado = manejadorNoAutenticado;
        this.manejadorAccesoDenegado = manejadorAccesoDenegado;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> {})

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                manejadorNoAutenticado
                        )
                        .accessDeniedHandler(
                                manejadorAccesoDenegado
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }
}