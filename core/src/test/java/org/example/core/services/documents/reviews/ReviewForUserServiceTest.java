package org.example.core.services.documents.reviews;

import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.ReviewCreateDto;
import org.example.core.dto.getting.reviews.ReviewDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.exceptions.PermissionDenied;
import org.example.core.hibernate.base_settings.filters.reviews.ReviewForUserFilters;
import org.example.core.hibernate.documents.ReviewHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.mapping.reviews.ReviewForUserDtoMapper;
import org.example.core.models.Good;
import org.example.core.models.Review;
import org.example.core.models.User;
import org.example.core.models.types.GoodStatusFromModerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewForUserServiceTest {
    @Mock
    ReviewHibImpl reviewHib;
    @Mock
    UserHibImpl userHib;
    @Mock
    GoodHibImpl goodHib;
    @Mock
    ReviewForUserDtoMapper mapper;
    @InjectMocks
    ReviewForUserService service;

    @Test
    @Tag("negative")
    @DisplayName("getByUserIdIfUserNotFound")
    void getByUserIdIfUserNotFound(){
        when(userHib.getUserIdByLogin(anyString())).thenReturn(null);
       Exception ex =  Assertions.assertThrows(NotCorrectInput.class, ()->
                service.getByUserId("username", 1, 1));
        Assertions.assertEquals("Пользователь был не найден", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("getByUserIdIfRepositoryFailed")
    void getByUserIdIfRepositoryFailed(){
        when(userHib.getUserIdByLogin(anyString())).thenReturn(1L);
        when(reviewHib.getByUserSmallVersion(anyLong(), anyInt(), anyInt()))
                .thenThrow(new NonHibernateException("test"));
        Exception ex =  Assertions.assertThrows(NonHibernateException.class, ()->
                service.getByUserId("username", 1, 1));
        Assertions.assertEquals("test", ex.getMessage());
    }

    @Test
    @Tag("positive")
    @DisplayName("getByUserIdIfEmpty")
    void getByUserIdIfEmpty(){
        when(userHib.getUserIdByLogin(anyString())).thenReturn(1L);
        when(reviewHib.getByUserSmallVersion(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of());
        service.getByUserId("username", 1, 1);
        verify(reviewHib).getByUserSmallVersion(anyLong(), anyInt(), anyInt());
        verify(mapper, never()).toDto(any());
    }

    @Test
    @Tag("positive")
    @DisplayName("getByUserIdIfNotEmpty")
    void getByUserIdIfNotEmpty(){
        ReviewDto dto = new ReviewDto();
        dto.setId(1L);
        ReviewDto dto1 = new ReviewDto();
        dto.setId(2L);

        when(userHib.getUserIdByLogin(anyString())).thenReturn(1L);

        when(reviewHib.getByUserSmallVersion(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(new Review(), new Review()));
        when(mapper.toDto(any())).thenReturn(dto)
                .thenReturn(dto1);

        Assertions.assertEquals(List.of(dto,dto1),
                service.getByUserId("username", 1, 1));
        verify(reviewHib).getByUserSmallVersion(anyLong(), anyInt(), anyInt());
        verify(mapper, times(2)).toDto(any());
    }

    @Test
    @Tag("negative")
    @DisplayName("getByUserAndGoodIfUserNotFound")
    void getByUserAndGoodIfUserNotFound() {
        when(userHib.getUserIdByLogin(anyString())).thenReturn(null);

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> service.getByUserAndGood("login", 1L)
        );
        Assertions.assertTrue(ex.getMessage().contains("Пользователь был не найден"));
    }

    @Test
    @Tag("negative")
    @DisplayName("getByUserAndGoodIfReviewNotFound")
    void getByUserAndGoodIfReviewNotFound() {
        when(userHib.getUserIdByLogin(anyString())).thenReturn(1L);
        when(reviewHib.getByUserAndGood(anyLong(), anyLong())).thenReturn(null);

        Exception ex = Assertions.assertThrows(
                DoesNoeExist.class,
                () -> service.getByUserAndGood("login", 1L)
        );
        Assertions.assertTrue(ex.getMessage().contains("Does not exist"));
    }

    @Test
    @Tag("positive")
    @DisplayName("getByUserAndGoodIfSuccessful")
    void getByUserAndGoodIfSuccessful() {
        Review review = new Review();
        ReviewDto dto = new ReviewDto();

        when(userHib.getUserIdByLogin(anyString())).thenReturn(1L);
        when(reviewHib.getByUserAndGood(anyLong(), anyLong())).thenReturn(review);
        when(mapper.toDto(review)).thenReturn(dto);

        ReviewDto result = service.getByUserAndGood("login", 1L);

        Assertions.assertEquals(dto, result);
    }

    @Test
    @Tag("negative")
    @DisplayName("createReviewIfUserNotFound")
    void createReviewIfUserNotFound() {
        when(userHib.getByLoginSmallVersion(anyString())).thenReturn(null);

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> service.createReview(new ReviewCreateDto(), "login", 1L)
        );
        Assertions.assertTrue(ex.getMessage().contains("Пользователь не был найден"));
    }

    @Test
    @Tag("negative")
    @DisplayName("createReviewIfGoodNotFound")
    void createReviewIfGoodNotFound() {
        when(userHib.getByLoginSmallVersion(anyString())).thenReturn(new User());
        when(goodHib.findById(anyLong(), any(Logger.class))).thenReturn(null);

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> service.createReview(new ReviewCreateDto(), "login", 1L)
        );
        Assertions.assertTrue(ex.getMessage().contains("Такой продукт не найден"));
    }

    @Test
    @Tag("negative")
    @DisplayName("createReviewIfGoodIsSuspicious")
    void createReviewIfGoodIsSuspicious() {
        Good good = new Good();
        good.setModeratorStatus(GoodStatusFromModerator.SUSPICIOUS);

        when(userHib.getByLoginSmallVersion(anyString())).thenReturn(new User());
        when(goodHib.findById(anyLong(), any(Logger.class))).thenReturn(good);

        Exception ex = Assertions.assertThrows(
                PermissionDenied.class,
                () -> service.createReview(new ReviewCreateDto(), "login", 1L)
        );
        Assertions.assertTrue(ex.getMessage().contains("Currently good is unavailable for reviews"));
    }

    @Test
    @Tag("positive")
    @DisplayName("createReviewIfSuccessful")
    void createReviewIfSuccessful() {
        Good good = new Good();
        good.setModeratorStatus(GoodStatusFromModerator.APPROVED);
        Review review = new Review();
        ReviewDto dto = new ReviewDto();

        when(userHib.getByLoginSmallVersion(anyString())).thenReturn(new User());
        when(goodHib.findById(anyLong(), any(Logger.class))).thenReturn(good);
        when(reviewHib.createReview(any(), any(Good.class), any(User.class))).thenReturn(review);
        when(mapper.toDto(review)).thenReturn(dto);

        ReviewDto result = service.createReview(new ReviewCreateDto(), "login", 1L);

        Assertions.assertEquals(dto, result);
        verify(reviewHib).createReview(any(), eq(good), any(User.class));
    }

    @Test
    @Tag("negative")
    @DisplayName("deleteReviewIfUserNotFound")
    void deleteReviewIfUserNotFound() {
        when(userHib.getUserIdByLogin(anyString())).thenReturn(null);

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> service.deleteReview(1L, "login")
        );
        Assertions.assertTrue(ex.getMessage().contains("Пользователь не был найден"));
    }

    @Test
    @Tag("positive")
    @DisplayName("deleteReviewIfSuccessful")
    void deleteReviewIfSuccessful() {
        when(userHib.getUserIdByLogin(anyString())).thenReturn(1L);

        service.deleteReview(1L, "login");

        verify(reviewHib).deleteReview(eq(1L), eq(1L));
    }

    @Test
    @Tag("positive")
    @DisplayName("getByFiltersIfEmpty")
    void getByFiltersIfEmpty() {
        when(reviewHib.getMinByFilters(any())).thenReturn(List.of());

        List<ReviewDto> result = service.getByFilters(new ReviewForUserFilters());

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @Tag("positive")
    @DisplayName("getByFiltersIfSuccessful")
    void getByFiltersIfSuccessful() {
        Review review = new Review();
        ReviewDto dto = new ReviewDto();

        when(reviewHib.getMinByFilters(any())).thenReturn(List.of(review, review));
        when(mapper.toDto(any())).thenReturn(dto);

        List<ReviewDto> result = service.getByFilters(new ReviewForUserFilters());

        Assertions.assertEquals(2, result.size());
        verify(mapper, times(2)).toDto(any());
    }

    @Test
    @Tag("negative")
    @DisplayName("getByFiltersIfRepositoryThrows")
    void getByFiltersIfRepositoryThrows() {
        when(reviewHib.getMinByFilters(any()))
                .thenThrow(new RuntimeException("db error"));

        Exception ex = Assertions.assertThrows(
                RuntimeException.class,
                () -> service.getByFilters(new ReviewForUserFilters())
        );

        Assertions.assertEquals("db error", ex.getMessage());
    }

}
