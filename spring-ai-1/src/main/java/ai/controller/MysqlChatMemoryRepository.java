package ai.controller;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author:
 * @date: 2023/7/27
 * @description: todo mysql远程记忆存储
 */
@Component("mysqlChatMemoryRepository")
public class MysqlChatMemoryRepository implements ChatMemoryRepository {
    @Override
    public List<String> findConversationIds() {
        return List.of();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return List.of();
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {

    }

    @Override
    public void deleteByConversationId(String conversationId) {

    }
}
