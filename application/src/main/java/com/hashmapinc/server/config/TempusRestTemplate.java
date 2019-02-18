package com.hashmapinc.server.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TempusRestTemplate {

    @Bean
    public RestTemplate simpleRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}

