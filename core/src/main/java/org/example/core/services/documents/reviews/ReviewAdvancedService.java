package org.example.core.services.documents.reviews;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.reviews.ReviewFullDto;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.hibernate.base_settings.filters.reviews.ReviewAdvancedFilters;
import org.example.core.hibernate.documents.ReviewHibImpl;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.mapping.reviews.ReviewFullMapper;
import org.example.core.models.Review;
import org.example.core.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewAdvancedService {
    private static final Logger logger = LogManager.getLogger(ReviewAdvancedService.class);

    private ReviewHibImpl reviewHib;
    private UserHibImpl userHib;
    private ReviewFullMapper mapper;

     public ReviewAdvancedService(ReviewHibImpl reviewHibImpl, UserHibImpl userHibImpl, ReviewFullMapper mapper) {
        this.reviewHib = reviewHibImpl;
        this.userHib = userHibImpl;
        this.mapper = mapper;
    }

    @Transactional
    public ReviewFullDto getReviewById(Long reviewId){
         try{
             Review review= reviewHib.getByIdFullVersion(reviewId);
             if (review == null){
                 throw new DoesNoeExist("Review does not exist with given credentials");
             }
             return mapper.toDto(review);
         }catch (DoesNoeExist e){
             throw e;
         }
         catch(Exception e){
             logger.error("ReviewAdvancedService getReviewById: " + e.getMessage());
             throw e;
         }

    }


    @Transactional
    public void blockReviewById(Long reviewId, String login){
         try{

             User moderator = userHib.getByLoginSmallVersion(login);
             if (moderator == null){
                 logger.error("Moderator does not exist with login " + login);
                 throw new CanNotMakeExecution("Moderator does not exist with login " + login);
             }
             reviewHib.blockReview(reviewId, moderator);


         }catch (DoesNoeExist | CanNotMakeExecution e){
             throw e;
         }catch(Exception e){
             logger.error("ReviewAdvancedService blockReviewById: " + e.getMessage());
             throw e;

         }
    }

    @Transactional
    public void unblockReviewById(Long reviewId, String login){
         try{
             reviewHib.unblockReview(reviewId, login);
         }catch (Exception e){
             logger.error("ReviewAdvancedService unblockReviewById: " + e.getMessage());
             throw e;
         }

    }

    @Transactional
    public List<ReviewFullDto> getByFilters(ReviewAdvancedFilters filters){
         try{
             List<Review> reviews = reviewHib.getFullByFilters(filters);
             return listToDto(reviews);
         }catch (Exception e){
             logger.error("ReviewAdvancedService getByFilters: " + e.getMessage());
             throw e;
         }

    }


    private List<ReviewFullDto> listToDto(List<Review> reviews){
         try{
             List<ReviewFullDto> dtos = new ArrayList<>();
             for (Review review : reviews){
                 dtos.add(mapper.toDto(review));
             }
             return dtos;
         }catch (Exception e){
             logger.error("ReviewAdvancedService listToDto: " + e.getMessage());
             throw e;
         }

    }


}
