package sn.lhacksrt.firdawsitech_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageProperties props;

    public WebConfig(FileStorageProperties props) {
        this.props = props;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // mappe /files/images/** => file:<uploadDir>/**
        Path uploadPath = Path.of(props.uploadDir()).toAbsolutePath().normalize();
        registry.addResourceHandler("/files/images/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/");
    }
}
