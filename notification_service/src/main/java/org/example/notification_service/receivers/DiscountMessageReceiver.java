package org.example.notification_service.receivers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.notification_service.dto.DiscountMessage;
import org.example.notification_service.email.EmailService;
import org.example.notification_service.hibernate.GoodHib;
import org.example.notification_service.hibernate.MultiplySubHib;
import org.example.notification_service.models.Good;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.example.notification_service.models.Category;
import org.example.notification_service.hibernate.CategoryHib;

import java.util.List;


@Component

@RequiredArgsConstructor
public class DiscountMessageReceiver {

    private static final Logger logger = LogManager.getLogger(DiscountMessageReceiver.class);
    private final ObjectMapper objectMapper;
    private final GoodHib goodHib;
    private final CategoryHib catHib;
    private final MultiplySubHib multiplySubHib;
    private final EmailService emailService;


    @KafkaListener(
            topics = "message.send",
            groupId = "notification-discount-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(String dto, Acknowledgment ack) {
        try{
            logger.info("Discount Message received");
            DiscountMessage message= objectMapper.readValue(dto, DiscountMessage.class);
            proccessMessage(message);
            ack.acknowledge();
        }
        catch(JsonProcessingException e){
            logger.error("JsonProcessingException DiscountMessageReceiver handle:" + e.getMessage());
            ack.acknowledge();
            // нельзя интерпретировать -> никогда не прочтем, пока пока
        }catch (Exception e){
            logger.error("Exception DiscountMessageReceiver handle:" + e.getMessage());

        }

    }

    private void proccessMessage(DiscountMessage dto){
        try {

            List<Long> cats = null;
            if (dto.getCategoryId()!= null){
                cats = catHib.getCategories(dto.getCategoryId()).stream().map(Category::getId).toList();
                if (cats.isEmpty())  cats = List.of(dto.getCategoryId());

                logger.debug("category's size " + cats.size());
            }

            List<Long> goods=null;
            if (dto.getTagId()!= null){
                goods=  goodHib.findGoodsBytTags(dto.getTagId());
            }

            List<String> emails =multiplySubHib.getUserEmails(goods, cats, dto.getShopId());

            if (emails.isEmpty()) return;
            emails.forEach(s -> {
                emailService.sendDiscountASync(s, dto.getMessage(), dto.getTopic());
            });
        }catch (Exception e){
            logger.error("Exception DiscountMessageReceiver proccessMessage:" + e.getMessage());
        }






    }
}
