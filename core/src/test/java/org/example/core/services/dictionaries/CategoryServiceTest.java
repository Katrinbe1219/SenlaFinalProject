package org.example.core.services.dictionaries;

import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.CategoryCreateDto;
import org.example.core.dto.getting.categories.CategoryGetDto;
import org.example.core.dto.patching.CategoryPatchDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.mapping.categories.CategoryGetDtoMapper;
import org.example.core.models.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    // deleteCategory, getAllCategories -> функции переброса,
    // проблемы передаются в Controller и затем в RestControllerADvice ->
    // другой тест

    @Mock
    CategoryHibImpl categoryHib;

    @InjectMocks
    CategoryService categoryService;

    @Mock
    CategoryGetDtoMapper categoryGetDtoMapper;


    @Test
    @DisplayName("createCategoryIfParentWasNotFound")
    @Tag("negative")
    void createCategoryIfParentWasNotFound(){
        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setParentId(1L);
        dto.setName("test");

        when(categoryHib.findById(anyLong(), any(Logger.class))).thenReturn(null);
        Assertions.assertThrows(DoesNoeExist.class, () -> {
            categoryService.createCategory(dto);
        });

    }

    @Test
    @DisplayName("createCategoryIfNameIsNotAlpha")
    @Tag("negative")
    void createCategoryIfNameIsNotAlpha(){
        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setName("test123%");


        Assertions.assertThrows(NotCorrectInput.class, () -> {
            categoryService.createCategory(dto);
        });

    }

    @Test
    @DisplayName("")
    @Tag("positive")
    void createCategorySuccessful(){
        CategoryGetDto dto = new CategoryGetDto();
        CategoryCreateDto newCat = new CategoryCreateDto();
        Category c = new Category();
        newCat.setName("test");
        when(categoryHib.save(any(Category.class), any(Logger.class))).thenReturn(c);
        when(categoryGetDtoMapper.toCategoryGetDto(any(Category.class))).thenReturn(dto);

        Assertions.assertEquals(dto, categoryService.createCategory(newCat));
    }



    @Test
    @DisplayName("patchCategoryIfParentWasNotFound")
    @Tag("negative")
    void patchCategoryIfParentWasNotFound(){
        CategoryPatchDto dto = new CategoryPatchDto();
        dto.setParentId(1L);

        when(categoryHib.findById(anyLong(), any(Logger.class))).thenReturn(null);
        Assertions.assertThrows(DoesNoeExist.class, () -> {
            categoryService.patchCategory(dto);
        });
    }

    @Test
    @DisplayName("getByIdIfWasNotFound")
    @Tag("negative")
    void getByIdIfWasNotFound(){
        when(categoryHib.findById(anyLong(), any(Logger.class))).thenReturn(null);
        Assertions.assertThrows(DoesNoeExist.class, () -> {
            categoryService.getById(1L);
        });
    }

    @Test
    @DisplayName("getByIdIfSuccessful")
    @Tag("negative")
    void getByIdIfSuccessful(){
        CategoryGetDto dto = new CategoryGetDto();
        when(categoryHib.findById(anyLong(), any(Logger.class))).thenReturn(new Category());
        when(categoryGetDtoMapper.toCategoryGetDto(any(Category.class))).thenReturn(dto);
        Assertions.assertEquals(dto, categoryService.getById(1L));
    }

    @Test
    @DisplayName("patchCategoryIfNameIsNotAlpha")
    @Tag("negative")
    void patchCategoryIfNameIsNotAlpha(){
        CategoryPatchDto dto = new CategoryPatchDto();
        dto.setName("test123%");

        Assertions.assertThrows(NotCorrectInput.class, () -> {
            categoryService.patchCategory(dto);
        });
    }

    @Test
    @DisplayName("patchCategoryIfUpdatingFailed")
    @Tag("negative")
    void patchCategoryIfUpdatingFailed(){
        CategoryPatchDto dto = new CategoryPatchDto();
        dto.setName("test");

        when(categoryHib.update(any(Category.class))).thenThrow(NonHibernateException.class);
        Assertions.assertThrows(Exception.class, () -> {
            categoryService.patchCategory(dto);
        });

    }

    @Test
    @DisplayName("patchCategorySuccessful")
    @Tag("positive")
    void patchCategorySuccessful(){
        CategoryPatchDto dto = new CategoryPatchDto();
        dto.setName("test");

        when(categoryHib.update(any(Category.class))).thenReturn(null);
        Assertions.assertDoesNotThrow( () -> {
            categoryService.patchCategory(dto);
        });

    }


}
