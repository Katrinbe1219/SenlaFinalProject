package org.example.core.controllers.prices;

import jakarta.validation.Valid;
import org.example.core.dto.creating.PriceCreateDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.getting.prices.PriceGetResultForModerator;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.prices.PriceFilter;
import org.example.core.services.documents.prices.PriceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/moderator/prices")
public class PriceForModeratorController {

    // delete - при остановке продажи какого-то товара изменяется поле валидности товара
    private PriceService priceService;
    public PriceForModeratorController(PriceService priceService) {
        this.priceService = priceService;
    }

    @PostMapping
    public PriceGetResultForModerator createPriceForGoodInShop(@Valid  @RequestBody PriceCreateDto dto) {
        return priceService.createPrice(dto);
    }

    @PostMapping("/updated")
    public PriceGetResultForModerator updatePriceForGoodInShop(@Valid @RequestBody PriceCreateDto dto) {
        return priceService.updatePrice(dto);

    }

    @GetMapping
    public List<PriceGetResultForModerator> getPrices(
            @Valid @RequestBody PriceFilter filters
    ){
        return priceService.getByFilters(filters);

    }

    @DeleteMapping
    public StringResponse deletePrice(
            @RequestParam("goodId") Long goodId,
            @RequestParam("shopId") Long shopId
    ){
        if (goodId == null || shopId == null) {
            throw new NotCorrectInput("Not all credentials were given");
        }
        priceService.deletePriceByGoodAndShop(goodId, shopId);
        return new StringResponse("Price was deleted successfully");
    }

    @DeleteMapping("/{id}")
    public StringResponse deletePrice(@PathVariable("id") Long id){
        priceService.deletePriceById(id);
        return new StringResponse("Price was deleted successfully");
    }

    @GetMapping("/{id}")
    public PriceGetResultForModerator getPrice(@PathVariable("id") Long id){
        return priceService.getByIdForModerator(id);
    }

}
