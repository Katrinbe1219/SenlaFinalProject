package org.example.core.controllers.system;

import jakarta.validation.Valid;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.kafka.DiscountMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/moderator/notification")
@RestController
public class SendNotificationController {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostMapping("/discount")
    public StringResponse sendDiscountNotification(
            @Valid @RequestBody DiscountMessage dto
    ){
        // TODO or better make service for sending
        eventPublisher.publishEvent(dto);
        return new StringResponse("Message was send");
    }
}
