package org.example.notification_service.receivers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.notification_service.dto.GoodShopPair;
import org.example.notification_service.dto.PriceChangedMessage;
import org.example.notification_service.dto.PriceCreatedMessage;
import org.example.notification_service.email.EmailService;
import org.example.notification_service.hibernate.PriceSubHib;
import org.example.notification_service.models.PriceSubscription;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PriceSubscriptionReceiver {
    private PriceSubHib priceSubHib;



    private ObjectMapper mapper;
    private static final Logger logger = LogManager.getLogger(PriceSubscriptionReceiver.class);
    private EmailService service;

    public PriceSubscriptionReceiver(PriceSubHib priceSubHib, ObjectMapper mapper, EmailService service) {
        this.priceSubHib = priceSubHib;
        this.mapper = mapper;
        this.service = service;
    }

    @KafkaListener(
            topics = "price.updated",
            groupId = "notification-price-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(List<String> messages, Acknowledgment ack) {
        try{
            System.out.println("ПОЛУЧИЛ СООБЩЕНИЕ");
            List<PriceChangedMessage> dtos = new ArrayList<>();
            for(String dto : messages){
                PriceChangedMessage message = mapper.readValue(dto, PriceChangedMessage.class);
                dtos.add(message);

            }
            proccessMessages(dtos);



            ack.acknowledge();
        }
        catch(JsonProcessingException e){
            logger.error("JsonProcessingException PriceSubscriptionReceiver handle:" + e.getMessage());
            ack.acknowledge();
            // нельзя интерпретировать -> никогда не прочтем, пока пока
        }catch (Exception e){
            logger.error("Exception PriceSubscriptionReceiver handle:" + e.getMessage());

        }

    }

    public void proccessMessages(List<PriceChangedMessage> dtos){
        try{


            Map<GoodShopPair, BigDecimal> prices = dtos.stream().collect(Collectors.toMap(
                    m -> new GoodShopPair(m.getGoodId(), m.getShopId()),
                    m -> m.getNewPrice(),
                    (existing, dupl) -> existing
            ));
            List<PriceSubscription> subscriptions = priceSubHib.findAllByPairs(prices.keySet().stream().toList());
            if(subscriptions.isEmpty()) return;



            for (GoodShopPair pair: prices.keySet()){
                BigDecimal price = prices.get(pair);

                Map<Boolean, List<PriceSubscription>> partitioned = subscriptions.stream().collect(
                        Collectors.partitioningBy(sub ->
                                    sub.getGood().getId().equals(pair.goodId())
                                && sub.getShop().getId().equals(pair.shopId()))
                                );
                proccessPrice(price, partitioned.get(true));
                subscriptions = partitioned.get(false);

                if (subscriptions.isEmpty()) break;

            }





        }
        catch (Exception e){
            logger.error("Exception PriceSubscriptionReceiver proccessMessages:" + e.getMessage());

        }
    }

    public void proccessPrice(BigDecimal price,   List<PriceSubscription> subscriptions) {

        subscriptions.stream()
                .filter(sub -> price.compareTo(sub.getTargetPrice()) <= 0)
                .forEach( sub ->{
                    System.out.println("LALALA");
                    System.out.println(sub.getUser().getEmail() + " adasdasd");

                    if (sub.getUser().getEmail() != null){
                        service.sendPriceASync(sub.getUser().getEmail(), sub.getGood().getName(),
                                sub.getTargetPrice(), price);
                    }

                    // отправка сообщений
        });

    }
}
