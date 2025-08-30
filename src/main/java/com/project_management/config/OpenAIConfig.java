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
            apiKey = "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"; 
        }
        return new OpenAiService(apiKey);
    }
}