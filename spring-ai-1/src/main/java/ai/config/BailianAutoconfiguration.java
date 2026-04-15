package ai.config;

import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgent;
import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgentOptions;
import com.alibaba.cloud.ai.dashscope.api.DashScopeAgentApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.ai.dashscope.agent")
public class BailianAutoconfiguration {

    public static String APP_ID;

    public void setAppId(String appId) {
        APP_ID = appId;
    }

    @Bean(name = "dashScopeAgentApi")
    public DashScopeAgentApi dashScopeAgentApi() {
        return DashScopeAgentApi.builder()
                .apiKey("sk-50da44c4f24a468182538b8fdc173a5d")
                .build();
    }

    @Bean("dashScopeApi")
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder().apiKey("sk-50da44c4f24a468182538b8fdc173a5d").build();
    }

    @Bean("dashScopeAgent")
    public DashScopeAgent dashScopeAgent(DashScopeAgentApi dashScopeAgentApi) {
        DashScopeAgent dashScopeAgent = new DashScopeAgent(dashScopeAgentApi, DashScopeAgentOptions.builder().withAppId(APP_ID).build());
        return dashScopeAgent;
    }
}
