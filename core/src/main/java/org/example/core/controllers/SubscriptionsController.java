package org.example.core.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.core.dto.getting.subscriptions.AvailabilitySubGetDto;
import org.example.core.dto.getting.subscriptions.PriceSubGetDto;
import org.example.core.hibernate.base_settings.filters.subscriptions.AvailabilitySubFilter;
import org.example.core.hibernate.base_settings.filters.subscriptions.PriceSubFilter;
import org.example.core.services.documents.subscriptions.AvailabilitySubService;
import org.example.core.services.documents.subscriptions.PriceSubService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
@AllArgsConstructor
public class SubscriptionsController {
    private PriceSubService priceSubService;
    private AvailabilitySubService availabilitySubService;

    @GetMapping("/availability")
    public List<AvailabilitySubGetDto> getAvailability(
            @Valid @RequestBody AvailabilitySubFilter filters
    ) {
       return availabilitySubService.findAll(filters);
    }

    @GetMapping("/price")
    public List<PriceSubGetDto> getSubscriptions(
            @Valid @RequestBody PriceSubFilter filters){
        return priceSubService.findAll(filters);
    }

}
