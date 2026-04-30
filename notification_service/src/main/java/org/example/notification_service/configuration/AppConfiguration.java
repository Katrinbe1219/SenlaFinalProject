package org.example.notification_service.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({KafkaConfiguration.class,
        DbConfiguration.class, EmailConfiguration.class})
@ComponentScan("org.example.notification_service")
public class AppConfiguration {
}
