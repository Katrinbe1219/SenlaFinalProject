package org.example.core.controllers.favourites;

import org.example.core.dto.getting.favourites.FavouriteCountByGoodDto;
import org.example.core.dto.getting.favourites.FavouriteFullDto;
import org.example.core.services.documents.FavouriteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/analyst/favourites")
public class FavouritesForAnalystController {

    private FavouriteService favouriteService;
    public FavouritesForAnalystController(FavouriteService favouriteService) {
        this.favouriteService = favouriteService;
    }

    @GetMapping
    public List<FavouriteFullDto> findAll(){
        return favouriteService.findAllForAnalyst();
    }

    @GetMapping("/count")
    public List<FavouriteCountByGoodDto> getCountByAllGoods(){
        return favouriteService.countAllByGoodId();
    }

    @GetMapping("/count/{id}")
    public FavouriteCountByGoodDto getCountByAllGoods(@PathVariable("id") Long goodId){
        return favouriteService.countOneByGoodId(goodId);
    }
}
