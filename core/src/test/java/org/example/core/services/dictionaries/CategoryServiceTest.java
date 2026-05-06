package org.example.core.services.dictionaries;

import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.CategoryCreateDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    CategoryHibImpl categoryHib;

    @InjectMocks
    CategoryService categoryService;

    @Mock
    Logger logger;


    @Test
    @DisplayName("createCategoryIfParentWasNotFound")
    @Tag("negative")
    void createCategoryIfParentWasNotFound(){
        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setParentId(1L);
        dto.setName("test");

        when(categoryHib.findById(anyLong(), logger)).thenReturn(null);
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
}
