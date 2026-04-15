package ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static ai.config.AIMemoryConfig.CONVERSATION_ID;
import static ai.config.AIMemoryConfig.MAX_MEMORY_SIZE;

/**
 * @author:
 * @date: 2023/7/27
 * @description: local Memory function, fast and no cost, but not adaptable for long conversation,
 * because it's Limited by local message size
 */
@RestController
@RequestMapping("/advisor/memory/in")
public class LocalMemoryController {

    private final ChatClient chatClient;

    private final ChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();

    private final MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(chatMemoryRepository)
            .maxMessages(MAX_MEMORY_SIZE)
            .build();


    public LocalMemoryController(@Qualifier("qianwen3") ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build())
                .build();
    }

    @GetMapping("chat")
    public String chat(@RequestParam("message") String message, @RequestParam("conversationId") String conversationId) {
        return chatClient.prompt(message)
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    @GetMapping("/history/session")
    public List<Message> historySession(@RequestParam(value = "conversation_id", defaultValue = "狗娃子") String conversationId) {
        return messageWindowChatMemory.get(conversationId);
    }

}
