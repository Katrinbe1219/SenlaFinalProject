package org.example.application.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@EnableScheduling
@Configuration
@EnableAsync
public class ScheduledConfiguration {
    // Tomcat пул для веб-запросов, для которых можно указать
    // если бы был boot server.tomcat.thread.max; server.tomcat.thread.min-spare

    // А тут мы прописываем то, что необходимо для асинхронных задач,
    // отдельно от выше названных

    // ДЛя Scheduler будет прописан один executor, 1 поток,
    // в котором существует isRecalculating

    // ASync executor нужен для самых пересчетов,
    // чтобы не блокировать всю работу приложения

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }

    @Bean(name="schedulerExecutor")
    public Executor schedulerExecutor() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("scheduler");
            t.setDaemon(true);
            return t;
        });
    }
}
