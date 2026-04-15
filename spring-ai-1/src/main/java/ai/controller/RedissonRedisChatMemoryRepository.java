package ai.controller;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * todo redis远程记忆存储
 */
@Component("redisChatMemoryRepository")
public class RedissonRedisChatMemoryRepository implements ChatMemoryRepository {
    @Override
    public List<String> findConversationIds() {
        // todo 查询所有会话id
        return List.of();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        // todo 根据会话id查询所有消息
        return List.of();
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        // todo 保存会话新的消息
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        // todo 删除会话
    }
}
