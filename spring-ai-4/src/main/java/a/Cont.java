package a;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.chat.MessageFormat;
import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import static com.alibaba.cloud.ai.dashscope.audio.tts.DashScopeAudioSpeechOptions.DEFAULT_MODEL;

@RestController
@RequestMapping("/dashscope/multi2")
public class Cont {

    @Autowired
    private ChatClient dashScopeChatClient;

    @jakarta.annotation.Resource
    private ResourceLoader resourceLoader;

    private static final String DEFAULT_PROMPT = "描述下这个";

    private static final String DEFAULT_VIDEO_PROMPT = "这是一组从视频中提取的图片帧，请描述此视频中的内容。";

    private static final String DEFAULT_AUDIO_PROMPT = "这是一个音频文件，请描述此音频中的内容。";

    private static final String DEFAULT_MODEL = "qwen-vl-max-latest";

    @Value("classpath:/multimodel/dog_and_girl.jpeg")
    org.springframework.core.io.Resource resource;

    @GetMapping("/stream/chatImage")
    public String qwen3_6() {

        Media media = new Media(MimeTypeUtils.IMAGE_PNG, resource);

        org.springframework.ai.chat.messages.UserMessage message =
                org.springframework.ai.chat.messages.UserMessage.builder().text("描述一下图片").media(media).metadata(new HashMap<>()).build();

        message.getMetadata().put(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);

        org.springframework.ai.chat.model.ChatResponse response = dashScopeChatClient
                .prompt(new Prompt(message,
                        DashScopeChatOptions.builder().withModel(DEFAULT_MODEL).withMultiModel(true).build()))
                .call()
                .chatResponse();

        System.out.println(response.getResult().getOutput().getText());
        return response.getResult().getOutput().getText();
    }

}
