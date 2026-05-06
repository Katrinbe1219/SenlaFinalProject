package org.example.core.controllers.favourites;

import jakarta.validation.Valid;
import org.example.core.dto.getting.favourites.FavouriteCountByGoodDto;
import org.example.core.dto.getting.favourites.FavouriteFullDto;
import org.example.core.hibernate.base_settings.filters.FavouritesAnalystFilters;
import org.example.core.services.documents.FavouriteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analyst/favourites")
public class FavouritesForAnalystController {

    private FavouriteService favouriteService;
    public FavouritesForAnalystController(FavouriteService favouriteService) {
        this.favouriteService = favouriteService;
    }

    @GetMapping
    public List<FavouriteFullDto> findAll( @Valid @RequestBody FavouritesAnalystFilters filters){
        return favouriteService.findAllForAnalyst(filters);
    }

    @GetMapping("/count")
    public List<FavouriteCountByGoodDto> getCountByAllGoods(
            @Valid @RequestBody FavouritesAnalystFilters filters
    ){
        return favouriteService.countAllByGoodId(filters);
    }

    @GetMapping("/count/{id}")
    public FavouriteCountByGoodDto getCountByAllGoods(@PathVariable("id") Long goodId){
        return favouriteService.countOneByGoodId(goodId);
    }
}
