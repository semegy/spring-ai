package ai.agent;

import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgent;
import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgentOptions;
import com.alibaba.cloud.ai.dashscope.api.DashScopeAgentApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static ai.config.BailianAutoconfiguration.APP_ID;

/**
 * 百炼rag流式接口
 */
@RestController
@RequestMapping("/agent/bailian")
public class BailianAgentRagStreamController {
    private static final Logger logger = LoggerFactory.getLogger(BailianAgentRagStreamController.class);

    private DashScopeAgent agent;

    public BailianAgentRagStreamController(DashScopeAgentApi dashscopeAgentApi) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode bizParams = objectMapper.createObjectNode();
        bizParams.put("name", "Alice");
        bizParams.put("age", 30);

        this.agent = new DashScopeAgent(dashscopeAgentApi,
                DashScopeAgentOptions.builder()
                        .withAppId(APP_ID)
                        .withSessionId("current_session_id")
                        .withIncrementalOutput(true)
                        .withHasThoughts(true)
                        .enableThinking(true)
                        .withBizParams(bizParams)
                        .build());
    }

    @GetMapping("/stream")
    public Flux<String> stream(@RequestParam(value = "message",
            defaultValue = "你好，请问你的知识库文档主要是关于什么内容的?") String message) {
        return agent.stream(new Prompt(message)).map(response -> {
            if (response == null || response.getResult() == null) {
                logger.error("chat response is null");
                return "chat response is null";
            }

            AssistantMessage app_output = response.getResult().getOutput();
            String content = app_output.getText();

            DashScopeAgentApi.DashScopeAgentResponse.DashScopeAgentResponseOutput output = (DashScopeAgentApi.DashScopeAgentResponse.DashScopeAgentResponseOutput) app_output.getMetadata().get("output");

            return content;
        });
    }

}
