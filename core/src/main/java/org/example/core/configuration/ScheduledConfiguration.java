package org.example.core.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@EnableScheduling
@Configuration
@EnableAsync
public class ScheduledConfiguration  implements AsyncConfigurer {
    // Tomcat пул для веб-запросов, для которых можно указать
    // если бы был boot server.tomcat.thread.max; server.tomcat.thread.min-spare

    // А тут мы прописываем то, что необходимо для асинхронных задач,
    // отдельно от выше названных


    @Bean(name = "ratingExecutor")
    public Executor ratingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("rating-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean(name = "kafkaExecutor")
    public Executor kafkaExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(1000); // кафка пишет пачками, добавить надо
        executor.setThreadNamePrefix("kafka-");
        // если потоки переполнены, выполняется в потоке scheduler, а не теряется задача
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

//    @Bean(name="schedulerExecutor")
//    public Executor schedulerExecutor() {
//        return Executors.newSingleThreadScheduledExecutor(r -> {
//            Thread t = new Thread(r);
//            t.setName("scheduler");
//            t.setDaemon(true);
//            return t;
//        });
//    }
    // фигня, у него не было названия taskScheduler, не было нужного типа, он не подхватывался


    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1); // нам он нужен чисто для вызовов Async методов
        scheduler.setThreadNamePrefix("scheduler-");
        scheduler.setDaemon(true);
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public Executor getAsyncExecutor() {
        return ratingExecutor();
    }
}
