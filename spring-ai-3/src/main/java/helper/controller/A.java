package helper.controller;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class A {

    @Bean("chatModel")
    public ChatModel chatModel() {
        return DashScopeChatModel.builder()
                .dashScopeApi(DashScopeApi
                        .builder()
                        .apiKey("sk-50da44c4f24a468182538b8fdc173a5d")
                        .build())
                .defaultOptions(DashScopeChatOptions.builder()
                        .model("qwen3.6-plus")
                        .build())
                .build();
    }

    @Bean("chatClient")
    public ChatClient chatClient(@Qualifier("chatModel") ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}
