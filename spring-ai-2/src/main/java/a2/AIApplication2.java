package a2;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

@EnableAutoConfiguration
public class AIApplication2  {

    public static void main(String[] args) {
        ConfigurableApplicationContext application = SpringApplication.run(AIApplication2.class);
        ChatModel bean = application.getBean(ChatModel.class);
        System.out.println(bean.call("你是谁"));
    }
}
