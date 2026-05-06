package org.example.core.controllers.reviews;

import jakarta.validation.Valid;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.getting.reviews.ReviewFullDto;
import org.example.core.hibernate.base_settings.filters.reviews.ReviewAdvancedFilters;
import org.example.core.models.User;
import org.example.core.services.documents.reviews.ReviewAdvancedService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/moderator/reviews")
public class ReviewModeratorController {
    private ReviewAdvancedService reviewService;

    public ReviewModeratorController(ReviewAdvancedService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public ReviewFullDto getReviewById (@PathVariable("id") Long reviewId){
        return reviewService.getReviewById(reviewId);
    }

    @PatchMapping("/{id}/block")
    public StringResponse blockReviewById(@PathVariable("id") Long reviewId,
                                          @AuthenticationPrincipal User moderator){

        reviewService.blockReviewById(reviewId, moderator.getLogin());
        return new StringResponse("Review blocked");
    }

    @DeleteMapping("/{id}/block")
    public StringResponse unblockReviewById(@PathVariable("id") Long reviewId,
                                            @AuthenticationPrincipal User moderator){
        reviewService.unblockReviewById(reviewId, moderator.getLogin());
        return new StringResponse("Review unblocked");
    }

    @GetMapping
    public List<ReviewFullDto> getAllReviews(@Valid @RequestBody ReviewAdvancedFilters filters){
        return reviewService.getByFilters(filters);
    }
}
