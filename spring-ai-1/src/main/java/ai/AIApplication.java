package ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AIApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(AIApplication.class, args);
    }
}
