package org.example.application.controllers;

import org.example.application.dto.creating.ShopCreateDto;
import org.example.application.dto.getting.statistics.shops.ShopGetDto;
import org.example.application.dto.getting.StringResponse;
import org.example.application.dto.patching.ShopPatchDto;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.services.objects.ShopService;
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
    public ShopGetDto createShop(@RequestBody ShopCreateDto shopCreateDto){
        if (shopCreateDto.getName() == null || shopCreateDto.getName().isEmpty()
        || shopCreateDto.getAddress() == null || shopCreateDto.getAddress().isEmpty()){
            throw new NotCorrectInput("Name and address must be given");
        }

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
            @RequestBody ShopPatchDto shopPatchDto
    ){
        if (shopPatchDto.getName() == null
        && shopPatchDto.getAddress() == null && shopPatchDto.getDistrictId() == null){
            throw new NotCorrectInput("Anything must be given");
        }

        if (shopPatchDto.getName() != null && shopPatchDto.getName().isBlank()){
            throw new NotCorrectInput("Name must be given");
        }

        if (shopPatchDto.getAddress() != null && shopPatchDto.getAddress().isBlank()){
            throw new NotCorrectInput("Address must be given");
        }


        shopPatchDto.setId(id);
        shopService.patch(shopPatchDto);
        return new StringResponse("Shop was updated successfully");
    }
}
