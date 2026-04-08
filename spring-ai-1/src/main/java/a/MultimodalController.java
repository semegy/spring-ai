package a;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.chat.MessageFormat;
import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.HashMap;

@RestController
@RequestMapping("/multimodal/chat")
public class MultimodalController {
    @Value("classpath:/multimodel/dog_and_girl.jpeg")
    org.springframework.core.io.Resource resource;

    @Value("${spring.ai.dashscope.chat.options.model}")
    String DEFAULT_MODEL;

    @Autowired
    ChatClient chatClient;

    @GetMapping("/stream/test")
    public Flux<String> chatImage() {

        Media media = new Media(MimeTypeUtils.IMAGE_PNG, resource);
        UserMessage message = UserMessage.builder().text("描述一下图片").media(media).metadata(new HashMap<>()).build();
        message.getMetadata().put(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);
        return chatClient
                .prompt(new Prompt(message, DashScopeChatOptions.builder().withMultiModel(true).build()))
                .stream()
                .chatResponse().map(chatResponse -> chatResponse.getResult().getOutput().getText());
    }


    @GetMapping("/stream/chat")
    public Flux<String> streamChat(@RequestParam("message") String message, @RequestParam(name = "imageFile")MultipartFile imageFile) {
        // 校验图片文件
        return chatClient
                .prompt(
                        Prompt.builder()
                                .messages(UserMessage.builder().text(message).media(Media.builder().mimeType(MimeTypeUtils.ALL).data(
                                        imageFile.getResource()
                                ).build()).build())
                                .chatOptions(DashScopeChatOptions.builder()
                                        .withMultiModel(true)
                                        .model(DEFAULT_MODEL)
                                        .build())
                                .build())
                .stream()
                .chatResponse()
                .map(ChatResponse::getResult)
                .map(Generation::getOutput)
                .map(AssistantMessage::getText);
    }
}
