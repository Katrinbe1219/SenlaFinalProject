package org.example.application.controllers.goods;

import org.example.application.dto.creating.ReviewCreateDto;
import org.example.application.dto.getting.goods.GoodGetForUserDto;
import org.example.application.dto.getting.reviews.ReviewDto;
import org.example.application.dto.getting.StringResponse;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.hibernate.base_settings.filters.goods.GoodFilter;
import org.example.application.hibernate.base_settings.filters.reviews.ReviewForUserFilters;
import org.example.application.services.documents.FavouriteService;
import org.example.application.services.documents.ReviewForUserService;
import org.example.application.services.objects.GoodService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goods")
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
            @RequestBody GoodFilter filters
            ){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean userChecking = auth.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_MAX_USER") || g.getAuthority().equals("ROLE_MIN_USER"));

        if (filters.getSize() <=0 ){
            throw new NotCorrectInput("Size must be greater than 0 or null");
        }

        if (filters.getPage() < 0){
            throw new NotCorrectInput("Page must be more or equals 0 or null");
        }
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
    public StringResponse addReview(@PathVariable("id") Long id,
                                    @RequestBody ReviewCreateDto dto){

        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        reviewService.createReview(dto, user.getUsername(), id);
        return new StringResponse("Review was added");
    }

    // получение по goodId
    @GetMapping("/{id}/reviews")
    public List<ReviewDto> getReviewsByGoodId(
            @PathVariable("id") Long id,
            @RequestBody ReviewForUserFilters filters
    ){
        filters.setGoodId(id);
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return reviewService.getByFilters(filters, user.getUsername());
    }

    @PostMapping("/{id}/favourite")
    public StringResponse favouriteGood(@PathVariable("id") Long goodId){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        favouriteService.createFavourite(user.getUsername(), goodId);
        return new StringResponse("Favourite was added");
    }

    @DeleteMapping("/{id}/favourite")
    public StringResponse unFavouriteGood(@PathVariable("id") Long goodId){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        favouriteService.removeFavourite(user.getUsername(), goodId);
        return new StringResponse("Favourite was removed");
    }




}
