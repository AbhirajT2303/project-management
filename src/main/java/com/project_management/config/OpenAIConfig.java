package com.project_management.config;

//import com.theokanning.openai.OpenAiService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.theokanning.openai.service.OpenAiService;


@Configuration
public class OpenAIConfig {

    @Bean
    public OpenAiService openAiService() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null) {
            apiKey = "api_key"; 
        }
        return new OpenAiService(apiKey);
    }
}