package ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.ai.memory", ignoreUnknownFields = false)
public class AIMemoryConfig {

    public static int MAX_MEMORY_SIZE;

    public static String CONVERSATION_ID;

    public void setMaxMemorySize(int maxMemorySize) {
        MAX_MEMORY_SIZE = maxMemorySize;
    }

    public void setConversationId(String conversationId) {
        CONVERSATION_ID = conversationId;
    }

}
