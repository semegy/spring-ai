package ai.utils.mock;

import ai.utils.Mock;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.service.data.mock")
public class MockDataWrapper extends Mock {

    public static Mock MOCK;

    @PostConstruct
    void init() {
        MockDataWrapper.MOCK = this;
    }


    public <T> T mock(MockData mockData) {
        return super.enabled ? mockData.getData() : (T) super.wrapper.get();
    }

}
