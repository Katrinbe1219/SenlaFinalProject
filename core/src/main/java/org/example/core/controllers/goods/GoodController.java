package org.example.core.controllers.goods;

import jakarta.validation.Valid;
import org.example.core.dto.creating.ReviewCreateDto;
import org.example.core.dto.getting.goods.GoodGetForUserDto;
import org.example.core.dto.getting.reviews.ReviewDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.goods.GoodFilter;
import org.example.core.hibernate.base_settings.filters.reviews.ReviewForUserFilters;
import org.example.core.services.documents.FavouriteService;
import org.example.core.services.documents.reviews.ReviewForUserService;
import org.example.core.services.objects.GoodService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goods")
// TODO почему если убрать raw in postman -> error invalid credentials
public class GoodController {
    GoodService goodService;
    ReviewForUserService reviewService;
    FavouriteService favouriteService;

    public GoodController(GoodService goodService, ReviewForUserService reviewService, FavouriteService favouriteService) {
        this.goodService = goodService;
        this.reviewService = reviewService;
        this.favouriteService = favouriteService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MAX_USER', 'MIN_USER')")
    public List<GoodGetForUserDto> findAll(
            @Valid @RequestBody GoodFilter filters
            ){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean userChecking = auth.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_MAX_USER") || g.getAuthority().equals("ROLE_MIN_USER"));

        if (userChecking && (filters.getMinCreatedAt() != null ||
                filters.getMaxCreatedAt() != null || filters.getCurCreatedAt() != null
        || filters.getMinUpdatedAt() != null || filters.getMaxUpdatedAt() != null ||
                filters.getCurUpdatedAt() !=null || filters.getStatus() != null)) {
            throw new NotCorrectInput("Your provided prohibited filters");
        }
        return goodService.findAllForUser(filters);
    }



    @GetMapping("/{id}")
    public GoodGetForUserDto findById(@PathVariable("id") Long id){
        return goodService.findForUserById(id);
    }



    // добвление отзыва
    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('MAX_USER', 'MIN_USER')")
    public StringResponse addReview(@PathVariable("id") Long id,
                                    @Valid @RequestBody ReviewCreateDto dto){

        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        reviewService.createReview(dto, user.getUsername(), id);
        return new StringResponse("Review was added");
    }

    // получение по goodId
    @GetMapping("/{id}/reviews")
    @PreAuthorize("hasAnyRole('MAX_USER', 'MIN_USER')")
    public List<ReviewDto> getReviewsByGoodId(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReviewForUserFilters filters
    ){
        filters.setGoodId(id);
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return reviewService.getByFilters(filters, user.getUsername());
    }

    @PostMapping("/{id}/favourite")
    @PreAuthorize("hasAnyRole('MAX_USER', 'MIN_USER')")
    public StringResponse favouriteGood(@PathVariable("id") Long goodId){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        favouriteService.createFavourite(user.getUsername(), goodId);
        return new StringResponse("Favourite was added");
    }

    @DeleteMapping("/{id}/favourite")
    @PreAuthorize("hasAnyRole('MAX_USER', 'MIN_USER')")
    public StringResponse unFavouriteGood(@PathVariable("id") Long goodId){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        favouriteService.removeFavourite(user.getUsername(), goodId);
        return new StringResponse("Favourite was removed");
    }




}
