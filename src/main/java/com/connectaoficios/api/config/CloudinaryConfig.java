package com.connectaoficios.api.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {

        Map<String, String> configuracion =
                new HashMap<>();

        configuracion.put(
                "cloud_name",
                cloudName
        );

        configuracion.put(
                "api_key",
                apiKey
        );

        configuracion.put(
                "api_secret",
                apiSecret
        );

        configuracion.put(
                "secure",
                "true"
        );

        return new Cloudinary(
                configuracion
        );
    }
}