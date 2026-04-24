package org.example.core.controllers;

import jakarta.validation.Valid;
import org.example.core.dto.creating.ShopCreateDto;
import org.example.core.dto.getting.statistics.shops.ShopGetDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.patching.ShopPatchDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.services.objects.ShopService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shops")
public class ShopController {

    private ShopService shopService;
    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping
    public List<ShopGetDto> getShops(
            @RequestParam(value = "count", defaultValue = "10", required = false) Integer count,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page
    ){
        if (count <=0){
            throw new NotCorrectInput("Count must be greater than 0");
        }

        if (page <0) {
            throw new NotCorrectInput("Page must be >= 0");
        }
        return shopService.findAll(count, page);
    }

    @GetMapping("/{id}")
    public ShopGetDto getShopById(@PathVariable("id") Long id){
        return shopService.findById(id);
    }

    @PostMapping
    public ShopGetDto createShop(@Valid @RequestBody ShopCreateDto shopCreateDto){
        return shopService.create(shopCreateDto);
    }

    @DeleteMapping("/{id}")
    public StringResponse deleteShop(@PathVariable("id") Long id){
        shopService.delete(id);
        return new StringResponse("Shop deleted successfully");
    }

    @PatchMapping("/{id}")
    public StringResponse patch(
            @PathVariable("id") Long id,
            @Valid @RequestBody ShopPatchDto shopPatchDto
    ){
        if (shopPatchDto.getName() == null
        && shopPatchDto.getAddress() == null && shopPatchDto.getDistrictId() == null){
            throw new NotCorrectInput("Anything must be given");
        }

        shopPatchDto.setId(id);
        shopService.patch(shopPatchDto);
        return new StringResponse("Shop was updated successfully");
    }
}
