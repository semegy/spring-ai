package ai.agent;

import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgent;
import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgentOptions;
import com.alibaba.cloud.ai.dashscope.api.DashScopeAgentApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static ai.config.BailianAutoconfiguration.APP_ID;

/**
 * mcp服务调用测试
 */
@RestController
@RequestMapping("/agent/bailian/mcp")
public class AliBaiLianAgentMCPController {

    private final DashScopeAgent dashScopeAgent;

    public AliBaiLianAgentMCPController(DashScopeAgentApi dashscopeAgentApi) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode bizParams = objectMapper.createObjectNode();
        bizParams.put("name", "Alice");
        bizParams.put("age", 30);

        this.dashScopeAgent = new DashScopeAgent(dashscopeAgentApi,
                DashScopeAgentOptions.builder()
                        .withAppId(APP_ID)
                        .withSessionId("current_session_id")
                        .withIncrementalOutput(true)
                        .withHasThoughts(true)
                        .enableThinking(true)
                        .withBizParams(bizParams)
                        .build());
    }

    @RequestMapping("/stream")
    public Flux<String> stream(@RequestParam(value = "message", defaultValue = "https://developer.aliyun.com/article/1682017,帮我概括下这个网页内容") String message) {
        return dashScopeAgent.stream(new Prompt(message)).map(response -> {
            if (response != null) {
                Generation result = response.getResult();
                if (result == null) {
                    return "chat response result is null";
                }
                AssistantMessage output = result.getOutput();
                if (output == null) {
                    return "chat response output is null";
                } else {
                    return output.getText();
                }
            } else {
                return "chat response is null";
            }
        });
    }
}
