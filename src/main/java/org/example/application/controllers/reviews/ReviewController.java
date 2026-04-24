package org.example.application.controllers.reviews;

import org.example.application.dto.getting.reviews.ReviewDto;
import org.example.application.dto.getting.StringResponse;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.services.documents.prices.PriceService;
import org.example.application.services.documents.ReviewForUserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
// FOR USER!!!
public class ReviewController {

    private final PriceService priceService;
    ReviewForUserService reviewService;
    public ReviewController(ReviewForUserService reviewService, PriceService priceService) {
        this.reviewService = reviewService;
        this.priceService = priceService;
    }

    //  get all users reviews
    @GetMapping
    public List<ReviewDto> getReviewsByUser(
            @RequestParam(value = "count", defaultValue = "10", required = false) Integer count,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page
    ){
        if (count <=0){
            throw new NotCorrectInput("Count must be greater than 0");
        }

        if (page <0) {
            throw new NotCorrectInput("Page must be >= 0");
        }
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return reviewService.getByUserId(user.getUsername(), page, count);
    }

    @GetMapping("/{id}")
    public ReviewDto getReviewByUserAndGood(
        @PathVariable("id") Long id
    ){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return reviewService.getByUserAndGood(user.getUsername(), id);

    }


    // delete by userId and goodId
    @DeleteMapping("/{id}")
    public StringResponse deleteReview(@PathVariable("id") Long id) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        reviewService.deleteReview(id, user.getUsername());
        return new StringResponse("Review was deleted");
    }

    // изменять уже не может
    // добавление идет через good

}
