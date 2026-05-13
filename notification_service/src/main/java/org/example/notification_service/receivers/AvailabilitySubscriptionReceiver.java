package org.example.notification_service.receivers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.notification_service.dto.PriceChangedMessage;
import org.example.notification_service.dto.PriceCreatedMessage;
import org.example.notification_service.email.EmailService;
import org.example.notification_service.hibernate.AvailabilitySubHib;
import org.example.notification_service.models.AvailabilitySubscription;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AvailabilitySubscriptionReceiver {

    private AvailabilitySubHib availabilitySubHib;
    private ObjectMapper mapper;
    private EmailService emailService;
    private static final Logger logger = LogManager.getLogger(AvailabilitySubscriptionReceiver.class);

    public AvailabilitySubscriptionReceiver(AvailabilitySubHib availabilitySubHib, ObjectMapper mapper, EmailService emailService) {
        this.availabilitySubHib = availabilitySubHib;
        this.mapper = mapper;
        this.emailService = emailService;
    }

    @KafkaListener(
            topics = "price.created",
            groupId = "notification-availability-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(String dto, Acknowledgment ack) {
        try{
            PriceCreatedMessage message = mapper.readValue(dto, PriceCreatedMessage.class);
            logger.info("Availability Message received");
            processPrice(message);
            ack.acknowledge();
        }
        catch(JsonProcessingException e){
            logger.error("JsonProcessingException AvailabilitySubscriptionReceiver handle:" + e.getMessage());
            ack.acknowledge();
            // нельзя интерпретировать -> никогда не прочтем, пока пока
        }catch (Exception e){
            logger.error("Exception AvailabilitySubscriptionReceiver handle:" + e.getMessage());

        }
    }

    void processPrice(PriceCreatedMessage dto){
        List<AvailabilitySubscription> subscriptions = availabilitySubHib.findAll(dto.getGoodId(), dto.getShopId());
        if (subscriptions.isEmpty())  return;
        logger.info("Availability Messages are about to be send");
        subscriptions.forEach(sub -> {
            if (sub.getUser().getEmail()!=null){
                emailService.sendAvailableASync(
                        sub.getUser().getEmail(),
                        sub.getGood().getName(),
                        sub.getShop().getId(),
                        sub.getShop().getName(),
                        sub.getShop().getAddress()
                );
            }
        });

        List<Long> ids = subscriptions.stream().map(AvailabilitySubscription::getId).toList();
        availabilitySubHib.deleteByIds(ids);

    }



}
