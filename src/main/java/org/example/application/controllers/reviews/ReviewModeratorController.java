package org.example.application.controllers.reviews;

import org.example.application.dto.getting.StringResponse;
import org.example.application.dto.getting.reviews.ReviewFullDto;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.hibernate.base_settings.filters.reviews.ReviewAdvancedFilters;
import org.example.application.models.User;
import org.example.application.services.documents.ReviewAdvancedService;
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

    @PatchMapping("block/{id}")
    public StringResponse blockReviewById(@PathVariable("id") Long reviewId){
        User moderator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        reviewService.blockReviewById(reviewId, moderator.getLogin());
        return new StringResponse("Review blocked");
    }

    @DeleteMapping("block/{id}")
    public StringResponse unblockReviewById(@PathVariable("id") Long reviewId){
        User moderator = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        reviewService.unblockReviewById(reviewId, moderator.getLogin());
        return new StringResponse("Review unblocked");
    }

    @GetMapping
    public List<ReviewFullDto> getAllReviews(@RequestBody ReviewAdvancedFilters filters){
        if (filters.getCreatedAt() != null && (filters.getStartDate() != null || filters.getLastDate() != null)){
            throw new NotCorrectInput("Either createdAt or range");
        }
        if ((filters.getStartDate()==null && filters.getLastDate()!=null)
                || (filters.getStartDate()!=null && filters.getLastDate()==null)
        ){
            throw new NotCorrectInput("Range must be full");
        }

        if (filters.getSize() <=0){
            throw new NotCorrectInput("Size must be greater than 0");
        }

        if (filters.getPage() <0){
            throw new NotCorrectInput("Page must be >= 0");
        }

        return reviewService.getByFilters(filters);
    }
}
