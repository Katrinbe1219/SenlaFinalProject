package org.example.core.services.documents.reviews;

import org.example.core.dto.getting.reviews.ReviewDto;
import org.example.core.dto.getting.reviews.ReviewFullDto;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.hibernate.documents.ReviewHibImpl;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.mapping.reviews.ReviewFullMapper;
import org.example.core.models.Review;
import org.example.core.models.User;
import org.hibernate.HibernateError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewAdvancedServiceTest {

    @Mock
    ReviewHibImpl reviewHib;
    @Mock
    UserHibImpl userHib;
    @Mock
    ReviewFullMapper mapper;
    @InjectMocks
    ReviewAdvancedService service;

    @Test
    @DisplayName("getReviewByIdIFNotFound")
    @Tag("negative")
    void getReviewByIdIFNotFound(){
        when(reviewHib.getByIdFullVersion(anyLong()))
                .thenReturn(null);
        Exception e= Assertions.assertThrows(DoesNoeExist.class, ()->
                service.getReviewById(1L));
        Assertions.assertEquals("Review does not exist with given credentials", e.getMessage());
    }

    @Test
    @DisplayName("getReviewByIdIfRepositoryFailed")
    @Tag("negative")
    void getReviewByIdIfRepositoryFailed(){
        when(reviewHib.getByIdFullVersion(anyLong()))
                .thenThrow(new HibernateError("testing"));
        Exception e= Assertions.assertThrows(HibernateError.class, ()->
                service.getReviewById(1L));
        Assertions.assertEquals("testing", e.getMessage());
    }

    @Test
    @DisplayName("getReviewByIdIfSuccessful")
    @Tag("positive")
    void getReviewByIdIfSuccessful(){
        when(reviewHib.getByIdFullVersion(anyLong()))
                .thenReturn(new Review());
        ReviewFullDto dto = new ReviewFullDto();
        when(mapper.toDto(any(Review.class))).thenReturn(dto);
        Assertions.assertEquals(dto, service.getReviewById(1L));

    }

    @Test
    @DisplayName("blockReviewByIdIfModeratorNotFound")
    @Tag("negative")
    void blockReviewByIdIfModeratorNotFound(){
        when(userHib.getByLoginSmallVersion(anyString()))
                .thenReturn(null);
        Exception e= Assertions.assertThrows(CanNotMakeExecution.class, ()->
                service.blockReviewById(1L, "login"));
        Assertions.assertTrue( e.getMessage().contains("Moderator does not exist with login "));
    }

    @Test
    @DisplayName("blockReviewByIdIfOperationNotFound")
    @Tag("negative")
    void blockReviewByIdIfRepositoryFailed(){
        when(userHib.getByLoginSmallVersion(anyString()))
                .thenThrow(new NonHibernateException("testing"));

        Exception e= Assertions.assertThrows(NonHibernateException.class, ()->
                service.blockReviewById(1L, "login"));
        Assertions.assertEquals("testing", e.getMessage());
    }

    @Test
    @DisplayName("blockReviewByIdIfSuccessful")
    @Tag("positive")
    void blockReviewByIdIfSuccessful(){
        when(userHib.getByLoginSmallVersion(anyString()))
                .thenReturn(new User());
        when(reviewHib.blockReview(anyLong(), any(User.class)))
                .thenReturn(Boolean.TRUE);
        service.blockReviewById(1L, "login");
        verify(reviewHib).blockReview(anyLong(), any(User.class));
    }


}
