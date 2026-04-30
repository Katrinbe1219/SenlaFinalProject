package org.example.core.configuration;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EnableKafka
@Configuration
public class KafkaConfiguration {
    @Bean
    public ProducerFactory<String,String> producerFactory(){
        Map<String, Object> props = new HashMap();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "kafka-broker-1:9092,kafka-broker-2:9093,kafka-broker-3:9094");

        // получила сообщение, записала во все реплики - точно есть сообщение
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "org.apache.kafka.clients.producer.RoundRobinPartitioner");
        // idempotence -> sequence num for message, нет дублей при повторе retry
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        //props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "tr-" + UUID.randomUUID());
        return new DefaultKafkaProducerFactory<>(props);

    }

    @Bean
    public KafkaTemplate<String,String> kafkaTemplate(
            ProducerFactory<String,String> producerFactory
    ){
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaAdmin kafkaAdmin(){
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                "kafka-broker-1:9092,kafka-broker-2:9093,kafka-broker-3:9094");
        return  new KafkaAdmin(props);
    }

    @Bean
    public NewTopic priceUpdatedTopic(){
        return TopicBuilder.name("price.updated")
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic priceCreatedTopic(){
        return TopicBuilder.name("price.created")
                .partitions(3)
                .replicas(3)
                .build();
    }
}
