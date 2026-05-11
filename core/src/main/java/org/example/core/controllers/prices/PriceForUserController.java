package org.example.core.controllers.prices;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.core.dto.creating.PriceSubCreateDto;
import org.example.core.dto.getting.prices.PriceComparisonRequest;
import org.example.core.dto.getting.prices.PriceGetDtoForUser;
import org.example.core.dto.getting.subscriptions.AvailabilitySubGetDto;
import org.example.core.dto.getting.subscriptions.PriceSubGetDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.exceptions.PermissionDenied;
import org.example.core.models.User;
import org.example.core.services.documents.prices.PriceService;
import org.example.core.services.documents.subscriptions.AvailabilitySubService;
import org.example.core.services.documents.subscriptions.PriceSubService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("hasAnyRole('MIN_USER', 'MAX_USER')")
    public List<PriceGetDtoForUser> getPrices(
            @RequestParam("goodId") Long goodId,
            @RequestParam("shopId") Long shopId
    ){

        if (shopId <=0){
            throw new NotCorrectInput("shopId must be > 0");
        }

        if (goodId <=0) {
            throw new NotCorrectInput("goodId must be > 0");
        }
        return priceService.getAllForUser(goodId, shopId);
    }

    // Compare Prices
    @GetMapping("/comparison")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'MODERATOR', 'MIN_USER', 'MAX_USER')")
    public List<PriceGetDtoForUser> getComparison(
            @Valid @RequestBody PriceComparisonRequest request
    ){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean checking = auth.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_MAX_USER"));
        boolean checking2 = auth.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_MIN_USER"));

        if ((checking || checking2) && (request.getShopIds() == null || request.getShopIds().isEmpty())) {
            throw new PermissionDenied("You are not allowed to compare in all shops");
        }


        if (checking && request.getShopIds().size() > 4){
            throw  new PermissionDenied("You are not allowed to compare this amount of shops. Maximum: 4");
        }


        if (checking2 &&  request.getShopIds().size() > 2 ){
            throw  new NotCorrectInput("You are not allowed to compare this amount of shops. Maximum: 2");
        }

        return priceService.getComparison(request);

    }

    @PostMapping("/subscribe/price")
    @PreAuthorize("hasRole('MAX_USER')")
    public PriceSubGetDto createSubscriptionPrice(
            @Valid @RequestBody PriceSubCreateDto dto,
            @AuthenticationPrincipal User user
    ){
        return priceSubService.createSubscription(dto, user);
    }

    @PostMapping("/subscribe/availability")
    @PreAuthorize("hasRole('MAX_USER')")
    public AvailabilitySubGetDto createSubscriptionAvailability(
            @RequestParam("goodId") Long goodId,
            @RequestParam("shopId") Long shopId,
            @AuthenticationPrincipal User user
    ){
        if (goodId <= 0){
            throw new NotCorrectInput("goodId must be > 0");
        }

        if (shopId <= 0){
            throw new NotCorrectInput("shopId must be > 0");
        }

        return availabilitySubService.createSubscription(user, goodId, shopId);
    }
}
