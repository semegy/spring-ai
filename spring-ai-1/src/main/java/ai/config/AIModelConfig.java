package ai.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeImageApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.HashMap;
import java.util.function.Consumer;

@Configuration
public class AIModelConfig {

    @Component("chatModelMap")
    public
    class ModelMap extends HashMap<String, ChatModel> implements ApplicationContextAware {
        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            put("qianwen", applicationContext.getBean("qianwen", ChatModel.class));
            put("qianwen3", applicationContext.getBean("qianwen3", ChatModel.class));
            put("localQianwen", applicationContext.getBean("localQianwen", ChatModel.class));
            put("langchain", applicationContext.getBean(LangchainChatModel.class));
        }
    }

    @Bean("localQianwen")
    public ChatModel localQianwen() {
        return OllamaChatModel.builder()
                .ollamaApi(new OllamaApi.Builder().baseUrl("http://localhost:11434").build())
                .defaultOptions(OllamaChatOptions.builder().model("qwen2:0.5b").build())
                .build();
    }


    @Bean("qianwen")
    public ChatModel qianwen() {
        return DashScopeChatModel.builder()
                .dashScopeApi(DashScopeApi
                        .builder()
                        .apiKey("sk-50da44c4f24a468182538b8fdc173a5d")
                        .build())
                .defaultOptions(DashScopeChatOptions.builder()
                        .model("qwen3.6-plus")
                        .withMultiModel(true)
                        .build())
                .build();
    }

    @Bean("qwen3_6")
    public ImageModel qwen3_6() {
        return DashScopeImageModel.builder()
                .dashScopeApi(DashScopeImageApi
                        .builder()
                        .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                        .build())
                .defaultOptions(DashScopeImageOptions.builder()
                        .model("qwen3.6-plus")
                        .build())
                .build();
    }

    @Bean("qianwen3")
    public ChatModel qianwen3() {
        return DashScopeChatModel.builder()
                .dashScopeApi(DashScopeApi
                        .builder()
                        .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                        // 默认使用新加坡地域的模型，若使用新加坡地域的模型，可更改路径
                        .build())
                .defaultOptions(DashScopeChatOptions.builder()
                        .model("qwen3")
                        .build())
                .build();
    }

    @Component
    class LangchainChatModel implements ChatModel {

        @Resource(name = "streamChatModel")
        private dev.langchain4j.model.chat.StreamingChatModel streamLangchain4j;

        @Resource(name = "langchain4j")
        private dev.langchain4j.model.chat.ChatModel langchain4j;

        @Override
        public String call(String message) {
            return langchain4j.chat(message);
        }

        @Override
        public org.springframework.ai.chat.model.ChatResponse call(Prompt prompt) {
            return null;
        }

        @Override
        public Flux<String> stream(String message) {
            return Flux.create(
                    (Consumer<FluxSink<String>>) fluxSink -> streamLangchain4j.chat(message, new StreamingChatResponseHandler() {

                        // 完成回调
                        @Override
                        public void onCompleteResponse(ChatResponse chatResponse) {
                            fluxSink.complete();
                        }

                        // 错误回调
                        @Override
                        public void onError(Throwable throwable) {
                            fluxSink.error(throwable);
                        }

                        // 新数据回调
                        @Override
                        public void onPartialResponse(String partialResponse) {
                            fluxSink.next(partialResponse);
                        }
                    })
            );
        }
    }

    @Bean("langchain4j")
    public dev.langchain4j.model.chat.ChatModel langchain4j() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .modelName("qwen-plus")
                .build();
    }

    @Bean("streamChatModel")
    public StreamingChatModel streamChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                .modelName("qwen-vl-plus")
                .baseUrl("https://dashscope.aliyuncs.com")
                .build();
    }

    @Bean("chatClient")
    public ChatClient chatClient(@Qualifier("qianwen3") ChatModel chatModel) {
        ChatClient chatClient = ChatClient.create(chatModel);
        return chatClient;
    }

    @Bean("embeddingModel")
    public EmbeddingModel embeddingModel() {
        return DashScopeEmbeddingModel.builder()
                .dashScopeApi(DashScopeApi.builder().apiKey(System.getenv("AI_DASHSCOPE_API_KEY")).build())
                .defaultOptions(DashScopeEmbeddingOptions.builder().model("text-embedding-v4").build())
                .build();
    }
}
