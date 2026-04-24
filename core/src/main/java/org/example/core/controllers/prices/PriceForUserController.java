package org.example.core.controllers.prices;

import org.example.core.dto.getting.prices.PriceComparisonRequest;
import org.example.core.dto.getting.prices.PriceGetDtoForUser;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.services.documents.prices.PriceService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prices")
public class PriceForUserController {
    private PriceService priceService;

    public PriceForUserController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping
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
            @RequestBody PriceComparisonRequest request
    ){

        if (request.getShopIds() == null || request.getShopIds().isEmpty()){
            throw new NotCorrectInput("Shop ids must be given");
        }
        if (request.getGoodId() == null){
            throw new NotCorrectInput("GoodId must be given");
        }

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
}
