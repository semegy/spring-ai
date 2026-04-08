package a2;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Configuration
public class AIModelConfig {

    @Component("chatModelMap")
    class ModelMap extends HashMap<String, ChatModel> implements ApplicationContextAware{
        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//            put("qianwen", applicationContext.getBean("qianwen", ChatModel.class));
//            put("qianwen3", applicationContext.getBean("qianwen3", ChatModel.class));
            put("localQianwen", applicationContext.getBean("localQianwen", ChatModel.class));
        }
    }


//    @Bean("qianwen")
//    public ChatModel qianwen() {
//            return DashScopeChatModel.builder()
//                    .dashScopeApi(DashScopeApi
//                            .builder()
//                            .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
//                            .build())
//                    .defaultOptions(DashScopeChatOptions.builder()
//                            .model("qwen-plus")
//                            .build())
//                    .build();
//    }
//    @Bean("qianwen3")
//    public ChatModel qianwen3() {
//        return DashScopeChatModel.builder()
//                .dashScopeApi(DashScopeApi
//                        .builder()
//                        .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
//                        // 默认使用新加坡地域的模型，若使用新加坡地域的模型，可更改路径
//                        .baseUrl("https://dashscope-intl.aliyuncs.com")
//                        .build())
//                .defaultOptions(DashScopeChatOptions.builder()
//                        .model("qwen3-max")
//                        .build())
//                .build();
//    }

}
