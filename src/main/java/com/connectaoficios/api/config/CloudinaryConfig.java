package com.connectaoficios.api.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.url}")
    private String cloudinaryUrl;

    @Bean
    public Cloudinary cloudinary() {

        if (cloudinaryUrl == null
                || cloudinaryUrl.isBlank()) {

            throw new IllegalStateException(
                    "CLOUDINARY_URL no está configurado"
            );
        }

        String url = cloudinaryUrl.trim();

        if (!url.startsWith("cloudinary://")) {

            throw new IllegalStateException(
                    "CLOUDINARY_URL no tiene un formato válido"
            );
        }

        return new Cloudinary(url);
    }
}