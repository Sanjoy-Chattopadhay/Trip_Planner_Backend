package com.sanjoy.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import com.sanjoy.auth.service.ItineraryService;

@Configuration
public class AIConfig {

    @Value("${openai.api-key}")
    private String apiKey;

    @Bean
    public ItineraryService itineraryService() {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-3.5-turbo") // or gpt-4
                .temperature(0.7)
                .build();

        return AiServices.create(ItineraryService.class, model);
    }
}
