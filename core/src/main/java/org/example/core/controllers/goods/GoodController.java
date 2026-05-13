package org.example.core.controllers.goods;

import jakarta.validation.Valid;
import org.example.core.dto.creating.ReviewCreateDto;
import org.example.core.dto.getting.goods.GoodGetForUserDto;
import org.example.core.dto.getting.reviews.ReviewDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.goods.GoodFilter;
import org.example.core.hibernate.base_settings.filters.reviews.ReviewForUserFilters;
import org.example.core.models.User;
import org.example.core.services.documents.FavouriteService;
import org.example.core.services.documents.reviews.ReviewForUserService;
import org.example.core.services.objects.GoodService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("!hasAnyRole('ADMIN', 'MODERATOR', 'ANALYST')")
    public List<GoodGetForUserDto> findAll(
            @Valid @RequestBody GoodFilter filters
    ){

        if (filters.getStartCreatedAt() != null ||
                filters.getEndCreatedAt() != null || filters.getCurCreatedAt() != null
        || filters.getStartUpdatedAt() != null || filters.getEndUpdatedAt() != null ||
                filters.getCurUpdatedAt() !=null || filters.getStatus() != null) {
            throw new NotCorrectInput("Your provided prohibited filters");
        }
        return goodService.findAllForUser(filters);
    }



    @GetMapping("/{id}")
    @PreAuthorize("!hasAnyRole('ADMIN', 'MODERATOR', 'ANALYST')")
    public GoodGetForUserDto findById(@PathVariable("id") Long id){
        if (id <=0){
            throw new NotCorrectInput("id must be > 0");
        }
        return goodService.findForUserById(id);
    }



    // добвление отзыва
    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('MAX_USER', 'MIN_USER')")
    public StringResponse addReview(@PathVariable("id") Long id,
                                    @Valid @RequestBody ReviewCreateDto dto,
                                    @AuthenticationPrincipal User user){

        reviewService.createReview(dto, user.getUsername(), id);
        return new StringResponse("Review was added");
    }

    // получение по goodId
    @GetMapping("/reviews")
    @PreAuthorize("hasAnyRole('MAX_USER', 'MIN_USER')")
    public List<ReviewDto> getReviewsByGoodId(
            @Valid @RequestBody ReviewForUserFilters filters,
            @AuthenticationPrincipal User user
        ){
        return reviewService.getByFilters(filters);
    }

    @PostMapping("/{id}/favourite")
    @PreAuthorize("hasAnyRole('MAX_USER', 'MIN_USER')")
    public StringResponse favouriteGood(@PathVariable("id") Long goodId,
                                        @AuthenticationPrincipal User user){
        if (goodId <=0){
            throw new NotCorrectInput("goodId must be > 0");
        }
        favouriteService.createFavourite(user.getUsername(), goodId);
        return new StringResponse("Favourite was added");
    }

    @DeleteMapping("/{id}/favourite")
    @PreAuthorize("hasAnyRole('MAX_USER', 'MIN_USER')")
    public StringResponse unFavouriteGood(@PathVariable("id") Long goodId,
                                          @AuthenticationPrincipal User user){

        if (goodId <=0){
            throw new NotCorrectInput("goodId must be > 0");
        }
        favouriteService.removeFavourite(user.getUsername(), goodId);
        return new StringResponse("Favourite was removed");
    }




}
