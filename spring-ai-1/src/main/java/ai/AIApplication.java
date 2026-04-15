package ai;

import ai.mcp.OpenMeteoService;
import org.springframework.ai.model.chat.memory.autoconfigure.ChatMemoryAutoConfiguration;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = ChatMemoryAutoConfiguration.class)
public class AIApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(AIApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider toolCallbackProvider(OpenMeteoService openMeteoService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(openMeteoService)
                .build();
    }


}
