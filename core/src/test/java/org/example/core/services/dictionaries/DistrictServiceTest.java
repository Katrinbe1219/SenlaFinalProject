package org.example.core.services.dictionaries;


import org.apache.logging.log4j.Logger;
import org.example.core.dto.DistrictDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.hibernate.dictionaries.DistrictHibImpl;
import org.example.core.models.District;
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
public class DistrictServiceTest {
    // deleteDistrict, createDistrict - пропускают ошибки
    // до контроллеров -> RestControllerAdvice,
    // в других тестах проверка

    @Mock
    DistrictHibImpl districtHib;

    @InjectMocks
    DistrictService districtService;

    @Test
    @DisplayName("pathIfDoesNotExist")
    @Tag("negative")
    void pathIfDoesNotExist() {
        DistrictDto dto = new DistrictDto();
        dto.setId(1L);

        when(districtHib.findById(anyLong(),any(Logger.class))).thenReturn(null);
        Assertions.assertThrows(DoesNoeExist.class, () -> {
            districtService.patch(dto);
        });
    }

    @Test
    @DisplayName("pathIfNameIsNotAlpha")
    @Tag("negative")
    void patchIfNameIsNotAlpha(){

        DistrictDto dto = new DistrictDto();
        dto.setName("test123%");
        dto.setId(1L);
        when(districtHib.findById(anyLong(),any(Logger.class))).thenReturn(new District());
        Assertions.assertThrows(NotCorrectInput.class, ()->{
            districtService.patch(dto);
        });
    }

    @Test
    @DisplayName("patchIfSuccessful")
    @Tag("positive")
    void patchIfSuccessful(){
        DistrictDto dto = new DistrictDto();
        dto.setId(1L);
        dto.setName("test");
        when(districtHib.findById(anyLong(),any(Logger.class))).thenReturn(new District());
        when(districtHib.update(any(District.class), any(Logger.class))).thenReturn(null);
        Assertions.assertDoesNotThrow(()->{districtService.patch(dto);});
    }


    @Test
    @DisplayName("getByIdIfDoesNotExist")
    @Tag("negative")
    void getByIdIfDoesNotExist() {
        when(districtHib.findById(anyLong(),any(Logger.class))).thenReturn(null);
        Assertions.assertThrows(DoesNoeExist.class, () -> {
            districtService.getById(1L);
        });
    }

    @Test
    @DisplayName("getByIdIfNameIsNotAlpha")
    @Tag("negative")
    void getByIdIfNameIsNotAlpha() {
        District dto = new District();
        dto.setName("test123#");

        when(districtHib.findById(anyLong(),any(Logger.class))).thenReturn(dto);
        Assertions.assertThrows(NotCorrectInput.class, () -> {
            districtService.getById(1L);
        });
    }

    @Test
    @DisplayName("getByIdIfSuccessful")
    @Tag("positive")
    void getByIdIfSuccessful() {
        District dto = new District();
        dto.setName("test");

        when(districtHib.findById(anyLong(),any(Logger.class))).thenReturn(dto);
        Assertions.assertDoesNotThrow( () -> {
            districtService.getById(1L);
        });
    }

    @Test
    @DisplayName("getByIdIfNameIsNotAlpha")
    @Tag("negative")
    void getAllIfNotFound(){
        when(districtHib.findAllWithSort(anyInt(), anyInt(), any(BaseSortTypes.class), any(List.class), any(Logger.class)))
                .thenReturn(null)
                .thenReturn(List.of());
        Assertions.assertNull(districtService.getAll(1, 1, BaseSortTypes.ASC, List.of()));
        Assertions.assertNull(districtService.getAll(1, 1, BaseSortTypes.ASC, List.of()));

    }

    @Test
    @DisplayName("getByIdIfNameIsNotAlpha")
    @Tag("negative")
    void getAllIfSuccessful(){
        when(districtHib.findAllWithSort(anyInt(), anyInt(), any(BaseSortTypes.class), any(List.class), any(Logger.class)))
                .thenReturn(List.of(new District()));
        Assertions.assertNotNull(districtService.getAll(1, 1, BaseSortTypes.ASC, List.of()));

    }
}
