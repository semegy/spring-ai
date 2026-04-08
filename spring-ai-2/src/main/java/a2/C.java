package a2;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConfigurationProperties(prefix = "spring.io.")
public class C{
    @Bean
    public ChatModel chatModel() {
        return OllamaChatModel.builder()
                .ollamaApi(new OllamaApi.Builder().baseUrl("http://localhost:11434").build())
                .defaultOptions(OllamaOptions.builder().model("qwen2:0.5b").build())
                .build();
    }
}
