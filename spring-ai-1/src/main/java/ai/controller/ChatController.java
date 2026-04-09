package ai.controller;

import ai.config.AIModelConfig;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import jakarta.annotation.Resource;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Consumer;

@RestController
public class ChatController {

    @Resource(name = "chatModelMap")
    private AIModelConfig.ModelMap chatModelMap;

    @Autowired
    StreamingChatModel streamChatModel;

    @Resource(name = "langchain4j")
    private dev.langchain4j.model.chat.ChatModel langchain4j;

    @GetMapping("/chat")
    public String chat(@RequestParam("message") String message) {
        return callModel(message, "qianwen3");
    }

    @GetMapping("/localQianwen")
    public String localChat(@RequestParam("message") String message) {
        return callModel(message, "localQianwen");
    }

    @GetMapping("/langchain/chat")
    public String langchainChat(@RequestParam("message") String message) {
        return callModel(message, "langchain");
    }

    @GetMapping("stream/langchain/chat")
    public Flux<String> sttreamLangchainChat(@RequestParam("message") String message) {
        return streamCallModel(message, "langchain");
    }

    @GetMapping("stream/multimodalChat")
    public Flux<String> multimodalInteraction(@RequestParam("message") String message) throws IOException {
        return multimodalChat(message);
    }

    private <T> T callModel(String message, String qianwen) {
        if (qianwen == null) {
            chatModelMap.get("qianwen").call(message);
        }
        return (T) chatModelMap.get(qianwen).call(message);
    }

    private Flux<String> multimodalChat(String message) throws IOException {
        UserMessage userMessage = UserMessage.from(TextContent.from(message), ImageContent.from(Base64.encodeBase64String(new FileInputStream("D:\\workspace\\spring-ai\\spring-ai-1\\src\\main\\resources\\static\\images\\叶倾仙.png").readAllBytes())));
        ChatRequest chatRequest = ChatRequest.builder().messages(userMessage).build();

        ChatResponse chat = langchain4j.chat(chatRequest);
        System.out.println(chat.aiMessage().text());
        return multimodalInteraction(chatRequest);
    }

    private Flux<String> multimodalInteraction(ChatRequest message) throws IOException {
        return Flux.create(new Consumer<FluxSink<String>>() {
            @Override
            public void accept(FluxSink<String> fluxSink) {
                streamChatModel.chat(message, new StreamingChatResponseHandler() {

                    @Override
                    public void onPartialResponse(PartialResponse partialResponse, PartialResponseContext context) {
                        System.out.println(partialResponse.text());
                        fluxSink.next(partialResponse.text());
                    }

                    @Override
                    public void onCompleteResponse(ChatResponse chatResponse) {
                        fluxSink.complete();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        fluxSink.error(throwable);
                    }
                });
            }
        });
    }

    private Flux<String> streamCallModel(String message, String qianwen) {
        if (qianwen == null) {
            chatModelMap.get("qianwen").stream(message);
        }
        return chatModelMap.get(qianwen).stream(message);
    }


    @GetMapping("/stream/chat")
    public Flux<String> streamChat(@RequestParam("message") String message) {
        return streamCallModel(message, null);
    }

}
