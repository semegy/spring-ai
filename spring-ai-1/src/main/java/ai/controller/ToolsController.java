package ai.controller;

import ai.tool.ModelTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ToolsController {


    @Autowired
    ChatClient chatClient;

    @Autowired
    ModelTool[] tools;

    @GetMapping("tools")
    public Flux<String> tools(@RequestParam(value = "message", defaultValue = "今天的时间") String message) {

        Flux<String> content = chatClient.prompt(message)
                .tools(tools)
                .stream()
                .content();
        return content;
    }
}
