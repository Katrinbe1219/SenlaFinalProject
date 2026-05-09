package org.example.core.services.objects;

import io.jsonwebtoken.lang.Assert;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.GoodCreateDto;
import org.example.core.dto.getting.goods.GoodGetForUserDto;
import org.example.core.dto.getting.goods.GoodGetFullDto;
import org.example.core.dto.getting.goods.GoodIdDto;
import org.example.core.dto.patching.GoodPatchDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.goods.GoodFilter;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.hibernate.dictionaries.TagHibImpl;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.mapping.goods.GoodGetForUserMapper;
import org.example.core.mapping.goods.GoodGetFullDtoMapper;
import org.example.core.models.Category;
import org.example.core.models.Good;
import org.example.core.models.Unit;
import org.hibernate.HibernateException;
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
public class GoodServiceTest {
    @Mock
    GoodHibImpl goodHib;

    @Mock
    TagHibImpl tagHib;

    @Mock
    CategoryHibImpl categoryHib;

    @Mock
    UnitHibImpl unitHib;

    @Mock
    GoodGetFullDtoMapper fullMapper;

    @Mock
    GoodGetForUserMapper userMapper;

    @InjectMocks
    GoodService service;

    @Test
    @Tag("positive")
    @DisplayName("findAllForUserIfCategoryIdsNotNullAndGoodsNotFound")
    void findAllForUserIfCategoryIdsNotNullAndGoodsNotFound(){
        GoodFilter filters= new GoodFilter();
        filters.setCategoryIds(List.of(1l,2l));

        when(goodHib.findAllByFilters(filters, true))
                .thenReturn(List.of());

        List res = Assertions.assertDoesNotThrow(()-> service.findAllForUser(filters));
        Assertions.assertTrue(res.isEmpty());

        verify(categoryHib).findAllChildCategoryIds(eq(List.of(1l,2l)));
        verify(goodHib).findAllByFilters(filters, true);

    }

    @Test
    @Tag("positive")
    @DisplayName("findAllForUserIfCategoryIdsNotNull")
    void findAllForUserIfNotCategoryIdsAndGoodsFound(){
        GoodFilter filters= new GoodFilter();


        when(goodHib.findAllByFilters(filters, true))
                .thenReturn(List.of(new Good()));
        when(userMapper.toDto(any(Good.class))).thenReturn(new GoodGetForUserDto());

        List res = Assertions.assertDoesNotThrow(()-> service.findAllForUser(filters));
        Assertions.assertFalse(res.isEmpty());

        verify(categoryHib, never()).findAllChildCategoryIds(any());
        verify(goodHib).findAllByFilters(filters, true);

    }

    @Test
    @Tag("negative")
    @DisplayName("findAllForUserIfCategoryIdsFailed")
    void findAllForUserIfCategoryIdsFailed(){
        GoodFilter filters= new GoodFilter();
        filters.setCategoryIds(List.of(1l,2l));

        when(categoryHib.findAllChildCategoryIds(any())).thenThrow(new RuntimeException("testing"));
        Exception ex = Assertions.assertThrows(RuntimeException.class, ()-> service.findAllForUser(filters));
        Assertions.assertTrue(ex.getMessage().contains("testing"));
        verify(goodHib, never()).findAllByFilters(any(GoodFilter.class), anyBoolean());

    }

    @Test
    @Tag("negative")
    @DisplayName("findAllForUserIfNotCategoryIdsFailed")
    void findAllForUserIfNotCategoryIdsAndGoodsFailed(){
        GoodFilter filters= new GoodFilter();

        when(goodHib.findAllByFilters(filters, true)).thenThrow(new RuntimeException("testing"));
        Exception ex = Assertions.assertThrows(RuntimeException.class, ()-> service.findAllForUser(filters));
        Assertions.assertTrue(ex.getMessage().contains("testing"));

        verify(categoryHib, never()).findAllChildCategoryIds(any());
    }

    @Test
    @Tag("negative")
    @DisplayName("findAllForAnalystIfCategoriesFailed")
    void findAllForAnalystIfCategoriesFailed(){
        GoodFilter filters= new GoodFilter();
        filters.setCategoryIds(List.of(1l,2l));

        when(categoryHib.findAllChildCategoryIds(any(List.class)))
                .thenThrow(new RuntimeException("testing"));
        Exception ex = Assertions.assertThrows(RuntimeException.class,
                ()-> service.findAllForAnalyst(filters));

        Assertions.assertEquals("testing", ex.getMessage());
        verify(goodHib, never()).findAllByFilters(any(GoodFilter.class), anyBoolean());
    }

    @Test
    @Tag("negative")
    @DisplayName("findAllForAnalystIfNoCategoriesAndGoodsFailed")
    void findAllForAnalystIfNoCategoriesAndGoodsFailed(){
        GoodFilter filters= new GoodFilter();


        when(goodHib.findAllByFilters(any(GoodFilter.class), anyBoolean()))
                .thenThrow(new RuntimeException("testing"));

        Exception ex = Assertions.assertThrows(RuntimeException.class,
                ()-> service.findAllForAnalyst(filters));

        Assertions.assertEquals("testing", ex.getMessage());
        verify(categoryHib, never()).findAllChildCategoryIds(any(List.class));
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllForAnalystIfGoodsNotFound")
    void findAllForAnalystIfGoodsNotFound(){
        GoodFilter filters= new GoodFilter();


        when(goodHib.findAllByFilters(any(GoodFilter.class), anyBoolean()))
                .thenReturn(List.of());

        Assertions.assertDoesNotThrow(
                ()-> service.findAllForAnalyst(filters));


        verify(categoryHib, never()).findAllChildCategoryIds(any(List.class));
        verify(goodHib).findAllByFilters(any(GoodFilter.class), anyBoolean());
    }

    @Test
    @Tag("negative")
    @DisplayName("getFullByIdIfDoesNotFound")
    void getFullByIdIfDoesNotFound(){
        when(goodHib.findByIdFullVersion(anyLong())).thenReturn(null);
        Assertions.assertThrows(DoesNoeExist.class, ()-> service.getFullById(1l));
        verify(fullMapper, never()).toDto(any(Good.class));

    }

    @Test
    @Tag("negative")
    @DisplayName("getFullByIdIfHibernateException")
    void getFullByIdIfHibernateException(){
        when(goodHib.findByIdFullVersion(anyLong()))
                .thenThrow(new HibernateException("TestEx"));
        Exception ex = Assertions.assertThrows(HibernateException.class, ()-> service.getFullById(1l));
        Assertions.assertEquals("TestEx", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("getFullByIdIfHibernateException")
    void getFullByIdIfNonHibernateException(){
        when(goodHib.findByIdFullVersion(anyLong()))
                .thenThrow(new NonHibernateException("TestEx"));
        Exception ex = Assertions.assertThrows(NonHibernateException.class, ()-> service.getFullById(1l));
        Assertions.assertEquals("TestEx", ex.getMessage());
    }

    @Test
    @Tag("positive")
    @DisplayName("getFullByIdIfSuccessful")
    void getFullByIdIfSuccessful(){
        when(goodHib.findByIdFullVersion(anyLong()))
                .thenReturn(new Good());
        GoodGetFullDto res = new GoodGetFullDto();
        when(fullMapper.toDto(any(Good.class))).thenReturn(res);
        Assertions.assertEquals(res, service.getFullById(1l));
    }

    @Test
    @Tag("negative")
    @DisplayName("findForUserByIdIfDoesNotFound")
    void findForUserByIdIfDoesNotFound(){
        when(goodHib.findByIdFullVersion(anyLong())).thenReturn(null);
        Assertions.assertThrows(DoesNoeExist.class, ()-> service.findForUserById(1l));
        verify(fullMapper, never()).toDto(any(Good.class));

    }

    @Test
    @Tag("negative")
    @DisplayName("findForUserByIdIfHibernateException")
    void findForUserByIdIfHibernateException(){
        when(goodHib.findByIdFullVersion(anyLong()))
                .thenThrow(new HibernateException("TestEx"));
        Exception ex = Assertions.assertThrows(HibernateException.class, ()-> service.findForUserById(1l));
        Assertions.assertEquals("TestEx", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("findForUserByIdIfNonHibernateException")
    void findForUserByIdIfNonHibernateException(){
        when(goodHib.findByIdFullVersion(anyLong()))
                .thenThrow(new NonHibernateException("TestEx"));
        Exception ex = Assertions.assertThrows(NonHibernateException.class, ()-> service.findForUserById(1l));
        Assertions.assertEquals("TestEx", ex.getMessage());
    }

    @Test
    @Tag("positive")
    @DisplayName("findForUserByIdIfSuccessful")
    void findForUserByIdIfSuccessful(){
        when(goodHib.findByIdFullVersion(anyLong()))
                .thenReturn(new Good());
        GoodGetForUserDto res = new GoodGetForUserDto();
        when(userMapper.toDto(any(Good.class))).thenReturn(res);
        Assertions.assertEquals(res, service.findForUserById(1l));
    }

    @Test
    @Tag("negative")
    @DisplayName("createGoodIfTagsNotFound")
    void createGoodIfTagsNotFound(){
        GoodCreateDto dto = new GoodCreateDto();
        dto.setTagIds(List.of(1l, 2L,3l));

        org.example.core.models.Tag res = new org.example.core.models.Tag();
        res.setId(1l);

        when(tagHib.findAllById(any(List.class)))
                .thenReturn(List.of(res));
        Exception ex = Assertions.assertThrows(DoesNoeExist.class,
                ()-> service.createGood(dto));

        Assertions.assertTrue( ex.getMessage().contains("Tags are not valid:"));

    }

    @Test
    @Tag("negative")
    @DisplayName("createGoodIfCategoryNotFound")
    void createGoodIfCategoryNotFound(){
        GoodCreateDto dto = new GoodCreateDto();
        dto.setCategoryId(1l);
        when(categoryHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(null);
        Exception ex = Assertions.assertThrows(DoesNoeExist.class,
                ()-> service.createGood(dto));

        Assertions.assertTrue( ex.getMessage().contains("Category does not exist with given credentials"));

    }

    @Test
    @Tag("negative")
    @DisplayName("createGoodIfUnitNotFound")
    void createGoodIfUnitNotFound(){
        GoodCreateDto dto = new GoodCreateDto();
        dto.setUnitId(1l);
        when(unitHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(null);
        Exception ex = Assertions.assertThrows(DoesNoeExist.class,
                ()-> service.createGood(dto));

        Assertions.assertTrue( ex.getMessage().contains("Unit does not exist with given credentials"));

    }

    @Test
    @Tag("positive")
    @DisplayName("createGoodIfAllFound")
    void createGoodIfAllFound(){
        GoodCreateDto dto = new GoodCreateDto();
        dto.setTagIds(List.of(1l));
        dto.setUnitId(1l);
        dto.setCategoryId(1l);

        org.example.core.models.Tag res = new org.example.core.models.Tag();
        res.setId(1l);

        when(tagHib.findAllById(any(List.class)))
                .thenReturn(List.of(res));
        when(categoryHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(new Category());
        when(unitHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(new Unit());
        when(goodHib.save(any(Good.class), any(Logger.class)))
                .thenReturn(new Good());


        Assertions.assertNotNull(service.createGood(dto));

    }



    @Test
    @Tag("negative")
    @DisplayName("patchIfGoodNotFound")
    void patchIfGoodNotFound(){
        GoodPatchDto dto = new GoodPatchDto();
        dto.setName("test");
        dto.setId(1l);

        when(goodHib.findByIdFullVersion(anyLong())).thenReturn(null);

        Exception ex = Assertions.assertThrows(DoesNoeExist.class,
                () -> service.patch(dto));
        Assertions.assertEquals("Good does not exist with given credentials", ex.getMessage());

    }

    @Test
    @Tag("negative")
    @DisplayName("patchIfNameIsTheSame")
    void patchIfNameIsTheSame(){
        GoodPatchDto dto = new GoodPatchDto();
        dto.setName("test");
        dto.setId(1l);

        Good returning = new Good();
        returning.setName("test");

        when(goodHib.findByIdFullVersion(anyLong())).thenReturn(returning);

        Exception ex = Assertions.assertThrows(NotCorrectInput.class,
                () -> service.patch(dto));
        Assertions.assertEquals("Good already has this name", ex.getMessage());

    }

    @Test
    @Tag("negative")
    @DisplayName("patchIfTagsAreTheSame")
    void patchIfTagsAreTheSame(){
        GoodPatchDto dto = new GoodPatchDto();
        dto.setName("test-changed");
        dto.setId(1l);
        dto.setTagIds(List.of(1l,2l));

        Good returning = new Good();
        returning.setName("test");
        org.example.core.models.Tag t1 = new org.example.core.models.Tag();
        t1.setId(1l);
        org.example.core.models.Tag t2 = new org.example.core.models.Tag();
        t2.setId(2l);
        returning.setTags(List.of(t1,t2));

        when(goodHib.findByIdFullVersion(anyLong())).thenReturn(returning);

        Exception ex = Assertions.assertThrows(NotCorrectInput.class,
                () -> service.patch(dto));
        Assertions.assertEquals("This good already has these tags", ex.getMessage());

    }

    @Test
    @Tag("negative")
    @DisplayName("patchIfCategoryIsTheSame")
    void patchIfCategoryIsTheSame(){
        GoodPatchDto dto = new GoodPatchDto();
        dto.setCategoryId(1L);
        dto.setId(1L);

        Good returning = new Good();
        Category c= new Category();
        c.setId(1L);
        returning.setCategory(c);

        when(goodHib.findByIdFullVersion(anyLong())).thenReturn(returning);

        Exception ex = Assertions.assertThrows(NotCorrectInput.class,
                () -> service.patch(dto));
        Assertions.assertEquals("This good already has this category", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("patchIfCategoryNotFound")
    void patchIfCategoryNotFound(){
        GoodPatchDto dto = new GoodPatchDto();
        dto.setCategoryId(1L);
        dto.setId(1L);

        Good returning = new Good();
        Category c= new Category();
        c.setId(2L);
        returning.setCategory(c);

        when(goodHib.findByIdFullVersion(anyLong())).thenReturn(returning);
        when(categoryHib.findById(anyLong(), any(Logger.class))).thenReturn(null);

        Exception ex = Assertions.assertThrows(DoesNoeExist.class,
                () -> service.patch(dto));
        Assertions.assertEquals("Category does not exist with given credentials", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("patchIfUnitIsTheSame")
    void patchIfUnitIsTheSame(){
        GoodPatchDto dto = new GoodPatchDto();
        dto.setUnitId(1L);
        dto.setId(1L);

        Good returning = new Good();
        Unit c= new Unit();
        c.setId(1L);
        returning.setUnit(c);

        when(goodHib.findByIdFullVersion(anyLong())).thenReturn(returning);

        Exception ex = Assertions.assertThrows(NotCorrectInput.class,
                () -> service.patch(dto));
        Assertions.assertEquals("This good already has this unit", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("patchIfUnitNotFound")
    void patchIfUnitNotFound(){
        GoodPatchDto dto = new GoodPatchDto();
        dto.setUnitId(1L);
        dto.setId(1L);

        Good returning = new Good();
        Unit c= new Unit();
        c.setId(2L);
        returning.setUnit(c);

        when(goodHib.findByIdFullVersion(anyLong())).thenReturn(returning);
        when(unitHib.findById(anyLong(), any(Logger.class))).thenReturn(null);

        Exception ex = Assertions.assertThrows(DoesNoeExist.class,
                () -> service.patch(dto));
        Assertions.assertEquals("Unit does not exist with given credentials", ex.getMessage());
    }

    @Test
    @Tag("positive")
    @DisplayName("patchIfSuccessful")
    void patchIfSuccessful(){
        GoodPatchDto dto = new GoodPatchDto();
        dto.setUnitId(2L);
        dto.setCategoryId(2L);
        dto.setId(2L);
        dto.setName("test-new");
        dto.setTagIds(List.of(1L,2L, 3L));
        dto.setDescription("desciption");

        Good returning = new Good();
        Unit u= new Unit();
        u.setId(1L);
        Category c= new Category();
        c.setId(1L);
        org.example.core.models.Tag t1 = new org.example.core.models.Tag();
        t1.setId(1L);
        org.example.core.models.Tag t2 = new org.example.core.models.Tag();
        t2.setId(2L);
        org.example.core.models.Tag t3 = new org.example.core.models.Tag();
        t2.setId(3L);

        returning.setUnit(u);
        returning.setCategory(c);
        returning.setName("test");
        returning.setTags(List.of(t1,t2));

        when(goodHib.findByIdFullVersion(anyLong())).thenReturn(returning);
        when(unitHib.findById(anyLong(), any(Logger.class))).thenReturn(new Unit());
        when(categoryHib.findById(anyLong(), any(Logger.class))).thenReturn(new Category());
        when(tagHib.findAllById(any(List.class))).thenReturn(List.of(t1,t2,t3));



       Assertions.assertDoesNotThrow(
                () -> service.patch(dto));
    }



}
