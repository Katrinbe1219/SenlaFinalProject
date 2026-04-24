package org.example.application.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = "org.example.application")
@PropertySource("classpath:./database.properties")
@Import({DbConfiguration.class, SecurityConfiguration.class, WebConfiguration.class, ScheduledConfiguration.class})
public class AppConfiguration {
}
