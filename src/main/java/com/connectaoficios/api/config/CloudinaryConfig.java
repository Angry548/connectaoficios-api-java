package com.connectaoficios.api.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

        validarConfiguracion();

        return new Cloudinary(
                ObjectUtils.asMap(
                        "cloud_name", cloudName.trim(),
                        "api_key", apiKey.trim(),
                        "api_secret", apiSecret.trim(),
                        "secure", true
                )
        );
    }

    private void validarConfiguracion() {

        if (cloudName == null || cloudName.isBlank()) {
            throw new IllegalStateException(
                    "CLOUDINARY_CLOUD_NAME no está configurado"
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "CLOUDINARY_API_KEY no está configurado"
            );
        }

        if (apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalStateException(
                    "CLOUDINARY_API_SECRET no está configurado"
            );
        }
    }
}