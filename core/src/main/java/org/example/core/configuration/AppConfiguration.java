package org.example.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.*;

import java.time.Clock;

@Configuration
@ComponentScan(basePackages = "org.example.core")
@PropertySource("classpath:./database.properties")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import({DbConfiguration.class, SecurityConfiguration.class, WebConfiguration.class,
        ScheduledConfiguration.class, KafkaConfiguration.class})
public class AppConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
