package org.example.notification_service;

import org.example.notification_service.configuration.AppConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);

        Runtime.getRuntime().addShutdownHook(new Thread(context::close));
    }
}
