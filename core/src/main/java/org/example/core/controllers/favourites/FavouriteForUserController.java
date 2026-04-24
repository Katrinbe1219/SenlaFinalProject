package org.example.core.controllers.favourites;

import org.example.core.dto.getting.favourites.FavouriteGetForUserDto;
import org.example.core.services.documents.FavouriteService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/favourites")
public class FavouriteForUserController {
    private FavouriteService favouriteService;

    public FavouriteForUserController(FavouriteService favouriteService) {
        this.favouriteService = favouriteService;
    }

    @GetMapping
    public List<FavouriteGetForUserDto> findAll(){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return favouriteService.getAllForUser(user.getUsername());
    }


}
