package org.example.core.services.dictionaries;

import org.apache.logging.log4j.Logger;
import org.example.core.dto.TagDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.hibernate.dictionaries.TagHibImpl;
import org.example.core.mapping.TagDtoMapper;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {
    @Mock
    TagHibImpl tagHib;
    @Mock
    TagDtoMapper mapper;

    @InjectMocks
    TagService tagService;

    @Test
    @DisplayName("getAllTagsIfNotFound")
    @Tag("negative")
    void getAllTagsIfNotFound(){
        when(tagHib.findAllWithSort(anyInt(), anyInt(), any(BaseSortTypes.class), any(List.class), any(Logger.class)))
                .thenReturn(null)
                .thenReturn(List.of());

        Assertions.assertNull(tagService.getAllTags(1,1,BaseSortTypes.ASC, List.of()));
        Assertions.assertNull(tagService.getAllTags(1,1,BaseSortTypes.ASC, List.of()));
    }

    @Test
    @DisplayName("getAllTagsIfSuccessful")
    @Tag("positive")
    void getAllTagsIfSuccessful(){
        when(tagHib.findAllWithSort(anyInt(), anyInt(), any(BaseSortTypes.class), any(List.class), any(Logger.class)))
                .thenReturn(List.of(new org.example.core.models.Tag()));

        Assertions.assertNotNull(tagService.getAllTags(1,1,BaseSortTypes.ASC, List.of()));
    }

    @Test
    @DisplayName("createTagIfNameIsNotAlpha")
    @Tag("negative")
    void createTagIfNameIsNotAlpha(){

        Assertions.assertThrows(NotCorrectInput.class, () -> {
            tagService.createTag("test123@");
        });
    }

    @Test
    @DisplayName("createTagIfSuccessful")
    @Tag("positive")
    void createTagIfSuccessful(){

        when(tagHib.save(any(org.example.core.models.Tag.class), any(Logger.class)))
                .thenReturn(null);
        when(mapper.toDto(any()))
                .thenReturn(null);
        Assertions.assertDoesNotThrow( () -> {
            tagService.createTag("test");
        });
    }

    @Test
    @DisplayName("editTagIfDtoIsEmpty")
    @Tag("negative")
    void editTagIfDtoIsEmpty(){
        Assertions.assertThrows(NotCorrectInput.class, () -> {
            tagService.editTag(new TagDto());
        });
    }

    @Test
    @DisplayName("editTagIfNameIsNotAlpha")
    @Tag("negative")
    void editTagIfNameIsNotAlpha(){
        TagDto tagDto = new TagDto();
        tagDto.setName("test@123");
        tagDto.setId(1L);
        Assertions.assertThrows(NotCorrectInput.class, () -> {
            tagService.editTag(tagDto);
        });
    }

    @Test
    @DisplayName("editTagIfSuccessful")
    @Tag("positive")
    void editTagIfSuccessful(){
        TagDto tagDto = new TagDto();
        tagDto.setName("test");
        tagDto.setId(1L);
        when(tagHib.update(any(TagDto.class))).thenReturn(null);
        Assertions.assertDoesNotThrow( () -> {
            tagService.editTag(tagDto);
        });
    }

    @Test
    @DisplayName("getTagByIdIfDoesNotExist")
    @Tag("negative")
    void getTagByIdIfDoesNotExist(){
        when(tagHib.findById(anyLong(), any(Logger.class))).thenReturn(null);
        Assertions.assertThrows(DoesNoeExist.class, () -> {
            tagService.getTagById(1L);
        });
    }

    @Test
    @DisplayName("getTagByIdIfSuccessful")
    @Tag("positive")
    void getTagByIdIfSuccessful(){
        when(tagHib.findById(anyLong(), any(Logger.class))).thenReturn(new org.example.core.models.Tag());
        TagDto dto = new TagDto();
        when(mapper.toDto(any(org.example.core.models.Tag.class))).thenReturn(dto);
        Assertions.assertEquals(dto, tagService.getTagById(1L));
    }


}
