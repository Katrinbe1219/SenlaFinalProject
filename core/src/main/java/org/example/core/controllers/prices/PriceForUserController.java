package org.example.core.controllers.prices;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.core.dto.creating.PriceSubCreateDto;
import org.example.core.dto.getting.prices.PriceComparisonRequest;
import org.example.core.dto.getting.prices.PriceGetDtoForUser;
import org.example.core.dto.getting.subscriptions.AvailabilitySubGetDto;
import org.example.core.dto.getting.subscriptions.PriceSubGetDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.models.User;
import org.example.core.services.documents.prices.PriceService;
import org.example.core.services.documents.subscriptions.AvailabilitySubService;
import org.example.core.services.documents.subscriptions.PriceSubService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prices")
@AllArgsConstructor
public class PriceForUserController {
    private final AvailabilitySubService availabilitySubService;
    private PriceService priceService;
    private PriceSubService priceSubService;


    @GetMapping
    // для user это обязательно указывать какой продукт и откуда!
    public List<PriceGetDtoForUser> getPrices(
            @RequestParam("goodId") Long goodId,
            @RequestParam("shopId") Long shopId,
            @RequestParam(value = "count", defaultValue = "10", required = false) int count,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page
    ){

        if (count <=0){
            throw new NotCorrectInput("Count must be greater than 0");
        }

        if (page <0) {
            throw new NotCorrectInput("Page must be >= 0");
        }
        return priceService.getAllForUser(goodId, shopId, count, page);
    }

    // Compare Prices
    @GetMapping("/comparison")
    public List<PriceGetDtoForUser> getComparison(
            @Valid @RequestBody PriceComparisonRequest request
    ){



        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean checking = auth.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_MAX_USER"));
        if (checking && request.getShopIds().size() > 4){
            throw  new NotCorrectInput("You are not allowed to compare this amount of shops");
        }

        boolean checking2 = auth.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_MIN_USER"));
        if (checking2 && request.getShopIds().size() > 2){
            throw  new NotCorrectInput("You are not allowed to compare this amount of shops");
        }

        return priceService.getComparison(request);

    }

    @PostMapping("/subscribe/price/{id}")
    @PreAuthorize("hasRole('MAX_USER')")
    public PriceSubGetDto createSubscriptionPrice(
            @PathVariable("id") Long goodId,
            @Valid @RequestBody PriceSubCreateDto dto
    ){
        if (goodId < 0){
            throw new NotCorrectInput("GoodId must be > 0");
        }

        dto.setGoodId(goodId);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return priceSubService.createSubscription(dto, user);
    }

    @PostMapping("/subscribe/availability")
    public AvailabilitySubGetDto createSubscriptionAvailability(
            @RequestParam("goodId") Long goodId,
            @RequestParam("shopId") Long shopId
    ){
        if (goodId < 0){
            throw new NotCorrectInput("goodId must be > 0");
        }

        if (shopId < 0){
            throw new NotCorrectInput("shopId must be > 0");
        }


        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return availabilitySubService.createSubscription(user, goodId, shopId);
    }
}
