package org.example.core.services.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.kafka.PriceChangedMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@AllArgsConstructor
public class KafkaProducerService {
    private static final Logger logger = LogManager.getLogger(KafkaProducerService.class);

    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper mapper;


    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPriceUpdated(PriceChangedMessage dto){
        try{
            String message = mapper.writeValueAsString(dto);
            kafkaTemplate.send("price.updated", message).get(5, TimeUnit.SECONDS);
        }
        catch (TimeoutException e){
            logger.error("TimeoutException KafkaProducerService onPriceUpdated: "  + e.getMessage());

        }
        catch (Exception e){
            logger.error("NotTimeoutException KafkaProducerService onPriceUpdated: "  + e.getMessage());

        }
    }




}
