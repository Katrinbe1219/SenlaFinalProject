package org.example.core.controllers.prices;

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
    // patch - не может быть, так как хранится история
    // delete - при остановке продажи какого-то товара изменяется поле валидности товара
    private PriceService priceService;
    public PriceForModeratorController(PriceService priceService) {
        this.priceService = priceService;
    }

    @PostMapping
    public PriceGetResultForModerator createPriceForGoodInShop(@RequestBody PriceCreateDto dto) {
        if (dto.getGoodId() == null || dto.getShopId() == null || dto.getPrice() == null) {
            throw new NotCorrectInput("Not all credentials were given");
        }
        return priceService.createPrice(dto);
    }

    @GetMapping
    public List<PriceGetResultForModerator> getPrices(
            @RequestBody PriceFilter filters
            ){

        if (filters.getSize() <=0 ){
            throw new NotCorrectInput("Size must be greater than 0");
        }

        if (filters.getPage() < 0){
            throw new NotCorrectInput("Page must be more or equals 0");
        }

        if (filters.getMaxPrice() != null && (filters.getMinPrice() != null || filters.getCurPrice() != null)) {
            throw new NotCorrectInput("Either price range or specific one");
        }

        if (filters.getCurrent() != null && filters.getOld() != null) {
            throw new NotCorrectInput("Either current prices or old ones");
        }

        if (filters.getShopsId() != null && filters.getDistrictsId() != null) {
            throw new NotCorrectInput("Either shops id or districts id");
        }
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
