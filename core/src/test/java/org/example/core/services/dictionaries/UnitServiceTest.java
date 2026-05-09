package org.example.core.services.dictionaries;

import org.apache.logging.log4j.Logger;
import org.example.core.dto.UnitDto;
import org.example.core.dto.creating.UnitCreateDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.sorting_types.BaseSortTypes;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.mapping.unit.UnitCreateDtoMapper;
import org.example.core.mapping.unit.UnitDtoMapper;
import org.example.core.models.District;
import org.example.core.models.Unit;
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
public class UnitServiceTest {
    //delete, getById

    @Mock
    UnitHibImpl unitHib;

    @Mock
    UnitCreateDtoMapper createMapper;

    @Mock
    UnitDtoMapper dtoMapper;

    @InjectMocks
    UnitService unitService;

    @Test
    @DisplayName("getAllIfDoesNotExist")
    @Tag("negative")
    void getAllIfDoesNotExist() {
        when(unitHib.findAllWithSort(anyInt(), anyInt(), any(BaseSortTypes.class), any(List.class), any(Logger.class)))
                .thenReturn(null)
                .thenReturn(List.of());
        Assertions.assertNull(unitService.getAll(1,1, BaseSortTypes.ASC, List.of()));
    }

    @Test
    @DisplayName("getAllIfDoesNotExist")
    @Tag("positive")
    void getAllIfSuccessful() {
        when(unitHib.findAllWithSort(anyInt(), anyInt(), any(BaseSortTypes.class), any(List.class), any(Logger.class)))
                .thenReturn(List.of(new Unit()));
        Assertions.assertNotNull(unitService.getAll(1,1, BaseSortTypes.ASC, List.of()));
    }

    @Test
    @DisplayName("getByIdIfDoesNotExist")
    @Tag("negative")
    void getByIdIfDoesNotExist() {
        when(unitHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(null);

        Assertions.assertThrows(DoesNoeExist.class, () -> unitService.getById(1L));
    }

    @Test
    @DisplayName("getAllIfDoesNotExist")
    @Tag("positive")
    void getByIdIfSuccessful() {
        when(unitHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(new Unit());

        when(dtoMapper.toDto(any(Unit.class))).thenReturn(new UnitDto());
        Assertions.assertNotNull(unitService.getById(1L));
    }

    @Test
    @DisplayName("createIfBothNameAreNotAlpha")
    @Tag("negative")
    void createIfBothNameAreNotAlpha(){
        UnitCreateDto dto1 = new UnitCreateDto();
        UnitCreateDto dto2 = new UnitCreateDto();

        dto1.setFullName("test123@");
        dto2.setFullName("test123@");

        Assertions.assertThrows(NotCorrectInput.class, () -> unitService.create(dto1));
        Assertions.assertThrows(NotCorrectInput.class, () -> unitService.create(dto2));
    }

    @Test
    @DisplayName("createIfSuccessful")
    @Tag("positive")
    void createIfSuccessful() {
        UnitCreateDto dto1 = new UnitCreateDto();
        dto1.setFullName("test");

        when(unitHib.save(any(Unit.class), any(Logger.class))
        ).thenReturn(new Unit());
        when(createMapper.toEntity(any(UnitCreateDto.class))).thenReturn(new Unit());
        when(dtoMapper.toDto(any(Unit.class))).thenReturn(new UnitDto());
        Assertions.assertNotNull(unitService.create(dto1));
    }

    @Test
    @DisplayName("updateIfNotCorrectDto")
    @Tag("negative")
    void updateIfNotCorrectDto(){

        Unit unit = new Unit();
        unit.setFullName("fName");
        unit.setShortName("sName");

        when(unitHib.findById(anyLong(), any(Logger.class))).thenReturn(new Unit());

        UnitDto notCorrectFullName = new UnitDto();
        notCorrectFullName.setId(1L);
        notCorrectFullName.setFullName("test123#");

        UnitDto notCorrectShortName = new UnitDto();
        notCorrectShortName.setId(1L);
        notCorrectShortName.setFullName("test123#");

        UnitDto equalFullName = new UnitDto();
        equalFullName.setId(1L);
        equalFullName.setFullName("fName#");

        UnitDto equalShortName = new UnitDto();
        equalShortName.setId(1L);
        equalShortName.setShortName("sName#");


        Assertions.assertThrows(NotCorrectInput.class, () -> unitService.update(notCorrectFullName));
        Assertions.assertThrows(NotCorrectInput.class, () -> unitService.update(notCorrectShortName));
        Assertions.assertThrows(NotCorrectInput.class, () -> unitService.update(equalFullName));
        Assertions.assertThrows(NotCorrectInput.class, () -> unitService.update(equalShortName));

    }

    @Test
    @DisplayName("updateIfDoesNotExist")
    @Tag("negative")
    void updateIfDoesNotExist(){
        UnitDto dto = new UnitDto();
        dto.setId(1L);

        when(unitHib.findById(anyLong(), any(Logger.class))).thenReturn(null);
        Assertions.assertThrows(DoesNoeExist.class, () -> unitService.update(dto));
    }

}
