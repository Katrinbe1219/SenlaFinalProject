package org.example.core.controllers.reviews;

import lombok.AllArgsConstructor;
import org.example.core.dto.getting.reviews.ReviewDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.models.User;
import org.example.core.services.documents.reviews.ReviewForUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@AllArgsConstructor
public class ReviewController {


    ReviewForUserService reviewService;

    //  get all users reviews
    @GetMapping
    public List<ReviewDto> getReviewsByUser(
            @RequestParam(value = "count", defaultValue = "10", required = false) Integer count,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @AuthenticationPrincipal User user
    ){
        if (count <=0){
            throw new NotCorrectInput("Count must be greater than 0");
        }

        if (page <0) {
            throw new NotCorrectInput("Page must be >= 0");
        }
        return reviewService.getByUserId(user.getUsername(), page, count);
    }

    @GetMapping("/{id}")
    public ReviewDto getReviewByUserAndGood(
        @PathVariable("id") Long id,
        @AuthenticationPrincipal User user
    ){
        if (id <=0){
            throw new NotCorrectInput("id must be >0");
        }
        return reviewService.getByUserAndGood(user.getUsername(), id);

    }


    // delete by userId and goodId
    @DeleteMapping("/{id}")
    public StringResponse deleteReview(@PathVariable("id") Long id,
                                       @AuthenticationPrincipal User user) {
        if (id <=0){
            throw new NotCorrectInput("id must be >0");
        }
        reviewService.deleteReview(id, user.getUsername());
        return new StringResponse("Review was deleted");
    }

    // изменять уже не может
    // добавление идет через good

}
