package org.example.core.services.documents;

import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.favourites.FavouriteCountByGoodDto;
import org.example.core.exceptions.CanNotMakeExecution;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.hibernate.base_settings.filters.FavouritesAnalystFilters;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.hibernate.documents.FavouriteHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.UserHibImpl;
import org.example.core.models.Favourite;
import org.example.core.models.Good;
import org.example.core.models.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavouriteServiceTest {
    @Mock
    GoodHibImpl goodHib;

    @Mock
    UserHibImpl userHib;

    @Mock
    CategoryHibImpl categoryHib;
    @Mock
    FavouriteHibImpl favouriteHib;


    @InjectMocks
    FavouriteService service;

    @Test
    @DisplayName("findAllForAnalystIfCategoriesExpanded")
    void findAllForAnalystIfCategoriesExpanded(){
        FavouritesAnalystFilters filters = new FavouritesAnalystFilters();
        filters.setCategoryIds(List.of(1L,2L));

        List<Long> expanded= List.of(1L,2L,3L,4L);
        when(categoryHib.findAllChildCategoryIds(any(List.class)))
                .thenReturn(expanded);
        when(favouriteHib.findAllFullVersion(any(FavouritesAnalystFilters.class)))
                .thenReturn(List.of());

        service.findAllForAnalyst(filters);
        verify(categoryHib).findAllChildCategoryIds(any(List.class));
        Assertions.assertEquals(expanded, filters.getCategoryIds());

    }

    @Test
    @DisplayName("findAllForAnalystIfCategoriesNotExpanded")
    void findAllForAnalystIfCategoriesNotExpanded(){
        FavouritesAnalystFilters filters = new FavouritesAnalystFilters();

        service.findAllForAnalyst(filters);
        verify(favouriteHib).findAllFullVersion(any(FavouritesAnalystFilters.class));
        verify(categoryHib, never()).findAllChildCategoryIds(any());

    }

    @Test
    @Tag("negative")
    @DisplayName("getAllForUserIfUserNotFound")
    void getAllForUserIfUserNotFound(){
        when(userHib.getUserIdByLogin(anyString()))
                .thenReturn(null);
        Exception ex = Assertions.assertThrows(CanNotMakeExecution.class, () ->
                service.getAllForUser("login"));
        Assertions.assertTrue(ex.getMessage().contains("FavouriteService getAllForUser - не найден пользователь по login:"));
    }

    @Test
    @Tag("positive")
    @DisplayName("getAllForUserIfSuccessful")
    void getAllForUserIfSuccessful(){
        when(userHib.getUserIdByLogin(anyString()))
                .thenReturn(1L);
        service.getAllForUser("login");
        verify(favouriteHib).findAllByUser(any());
    }

    @Test
    @DisplayName("countAllByGoodIdIfCategoriesExpanded")
    void countAllByGoodIdIfCategoriesExpanded(){
        FavouritesAnalystFilters filters = new FavouritesAnalystFilters();
        filters.setCategoryIds(List.of(1L,2L));

        List<Long> expanded= List.of(1L,2L,3L,4L);
        when(categoryHib.findAllChildCategoryIds(any(List.class)))
                .thenReturn(expanded);
        when(favouriteHib.countAllByGoodId(any(FavouritesAnalystFilters.class)))
                .thenReturn(List.of());

        service.countAllByGoodId(filters);
        verify(categoryHib).findAllChildCategoryIds(any(List.class));
        Assertions.assertEquals(expanded, filters.getCategoryIds());

    }

    @Test
    @DisplayName("countAllByGoodIdIfCategoriesNotExpanded")
    void countAllByGoodIdIfCategoriesNotExpanded(){
        FavouritesAnalystFilters filters = new FavouritesAnalystFilters();

        service.countAllByGoodId(filters);
        verify(favouriteHib).countAllByGoodId(any(FavouritesAnalystFilters.class));
        verify(categoryHib, never()).findAllChildCategoryIds(any());

    }
    @Test
    @Tag("negative")
    @DisplayName("countOneByGoodIdIfFavouriteNotFound")
    void countOneByGoodIdIfFavouriteNotFound(){
        when(favouriteHib.countByGoodId(any()))
                .thenReturn(null);
        Exception ex = Assertions.assertThrows(
                DoesNoeExist.class, () ->
                        service.countOneByGoodId(1L)
        );
        Assertions.assertEquals("Favourite does not exist with given credentials",
                ex.getMessage());
    }

    @Test
    @Tag("positive")
    @DisplayName("countOneByGoodIdIfSuccessful")
    void countOneByGoodIdIfSuccessful(){
        when(favouriteHib.countByGoodId(anyLong()))
                .thenReturn(new FavouriteCountByGoodDto());

        service.countOneByGoodId(2L);
        verify(favouriteHib).countByGoodId(anyLong());
    }

    @Test
    @Tag("negative")
    @DisplayName("createFavouriteIfUserNotFound")
    void createFavouriteIfUserNotFound(){
        when(userHib.getByLoginSmallVersion(anyString())).thenReturn(null);
        Exception ex = Assertions.assertThrows(
                CanNotMakeExecution.class, () -> service.createFavourite("login", 1L)
        );
        Assertions.assertTrue( ex.getMessage().contains("getAllForUser - не найден пользователь по login: "));
    }

    @Test
    @Tag("negative")
    @DisplayName("createFavouriteIfGoodNotFound")
    void createFavouriteIfGoodNotFound(){
        when(userHib.getByLoginSmallVersion(anyString())).thenReturn(new User());
        when(goodHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(null);
        Exception ex = Assertions.assertThrows(
                DoesNoeExist.class, () -> service.createFavourite("login", 1L)
        );
        Assertions.assertTrue( ex.getMessage().contains("Good does not exist with given credentials"));
    }

    @Test
    @Tag("positive")
    @DisplayName("createFavouriteIfSuccessful")
    void createFavouriteIfSuccessful(){
        when(userHib.getByLoginSmallVersion(anyString())).thenReturn(new User());
        when(goodHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(new Good());

        service.createFavourite("login", 1L);
        verify(favouriteHib).save(any(), any(Logger.class));
    }

    @Test
    @Tag("negative")
    @DisplayName("removeFavouriteIfUserNotFound")
    void removeFavouriteIfUserNotFound(){
        when(userHib.getUserIdByLogin(anyString())).thenReturn(null);

        Exception ex = Assertions.assertThrows(
                CanNotMakeExecution.class, () -> service.removeFavourite("login", 1L)
        );
        Assertions.assertTrue( ex.getMessage().contains("getAllForUser - не найден пользователь по login:"));
    }

    @Test
    @Tag("negative")
    @DisplayName("removeFavouriteIfFavNotFound")
    void removeFavouriteIfFavNotFound(){
        when(userHib.getUserIdByLogin(anyString())).thenReturn(1L);
        when(favouriteHib.findByUserIdAndGoodIdPureVersion(anyLong(), anyLong()))
                .thenReturn(null);
        Exception ex = Assertions.assertThrows(
                DoesNoeExist.class, () -> service.removeFavourite("login", 1L)
        );
        Assertions.assertTrue( ex.getMessage().contains("Favourite does not exist with given credentials"));
    }

    @Test
    @Tag("positive")
    @DisplayName("removeFavouriteIfSuccessful")
    void removeFavouriteIfSuccessful(){
        when(userHib.getUserIdByLogin(anyString())).thenReturn(1L);
        when(favouriteHib.findByUserIdAndGoodIdPureVersion(anyLong(), anyLong()))
                .thenReturn(new Favourite());

        service.removeFavourite("login", 1L);
        verify(favouriteHib).remove(any());
    }

}
