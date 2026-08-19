package sn.lhacksrt.firdawsitech_server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.files")
public record FileStorageProperties(
        String uploadDir,
        String publicBaseUrl
) {}
