package com.project_management.config;

//import com.theokanning.openai.OpenAiService;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenAIConfig {
    @Value("${openai.api.key}")
    String apiKey;

    @Bean
    public OpenAiService openAiService() {

        if (apiKey == null) {
            apiKey = "api_key";
        }
        return new OpenAiService(apiKey);
    }
}