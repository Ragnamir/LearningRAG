package dev.learningrag.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.learningrag.store.JdbcSupport;

@Configuration
public class StoreConfig {

    @Bean
    public JdbcSupport jdbcSupport(ObjectMapper objectMapper) {
        return new JdbcSupport(objectMapper);
    }
}
