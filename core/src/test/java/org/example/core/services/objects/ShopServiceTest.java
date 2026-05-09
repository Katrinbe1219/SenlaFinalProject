package org.example.core.services.objects;

import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.ShopCreateDto;
import org.example.core.dto.getting.statistics.shops.ShopGetDto;
import org.example.core.dto.patching.ShopPatchDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.dictionaries.DistrictHibImpl;
import org.example.core.hibernate.objects.ShopHibImpl;
import org.example.core.mapping.ShopGetMapper;
import org.example.core.models.District;
import org.example.core.models.Shop;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ShopServiceTest {

    @Mock
    ShopHibImpl shopHib;
    @Mock
    DistrictHibImpl districtHib;
    @Mock
    ShopGetMapper mapper;

    @InjectMocks
    ShopService service;

    @Test
    @Tag("negative")
    @DisplayName("fundByIdIfNotFound")
    void fundByIdIfNotFound(){
        when(shopHib.findByIdFullVersion(anyLong()))
                .thenReturn(null);
        Exception ex = Assertions.assertThrows(
                DoesNoeExist.class, ()-> service.findById(1L)
        );
        Assertions.assertEquals("Shop does not exist with given credentials", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("fundByIdIfSuccessful")
    void fundByIdIfSuccessful(){
        when(shopHib.findByIdFullVersion(anyLong()))
                .thenReturn(new Shop());
        ShopGetDto shop = new ShopGetDto();
        when(mapper.toDto(any(Shop.class)))
                .thenReturn(shop);

        Assertions.assertEquals(shop, service.findById(1L));
    }


    @Test
    @Tag("negative")
    @DisplayName("createIfDistrictNotFound")
    void createIfDistrictNotFound(){
        ShopCreateDto dto = new ShopCreateDto();
        dto.setName("perek");
        dto.setDistrictId(1L);
        when(districtHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(null);
        Exception ex = Assertions.assertThrows(DoesNoeExist.class,()->
                service.create(dto));

        Assertions.assertEquals("District does not exist with given credentials", ex.getMessage());

    }

    @Test
    @Tag("positive")
    @DisplayName("createIfSuccessful")
    void createIfSuccessful(){
        ShopCreateDto dto = new ShopCreateDto();
        dto.setName("perek");
        dto.setDistrictId(1L);
        when(districtHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(new District());
        ShopGetDto ret = new ShopGetDto();
        when(shopHib.save(any(Shop.class), any(Logger.class)))
                .thenReturn(new Shop());
        when(mapper.toDto(any(Shop.class)))
                .thenReturn(ret);
        Assertions.assertEquals(ret, service.create(dto));
    }

    @Test
    @Tag("negative")
    @DisplayName("patchIfShopNotFound")
    void patchIfShopNotFound(){
        ShopPatchDto dto = new ShopPatchDto();
        dto.setId(1L);
        when(shopHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(null);
        Exception ex = Assertions.assertThrows(DoesNoeExist.class, ()->
                service.patch(dto));
        Assertions.assertEquals("Shop does not exist with given credentials", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("patchIfDistrictNotFound")
    void patchIfDistrictNotFound(){
        ShopPatchDto dto = new ShopPatchDto();
        dto.setId(1L);
        dto.setDistrictId(2L);
        when(shopHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(new Shop());
        when(districtHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(null);
        Exception ex = Assertions.assertThrows(DoesNoeExist.class, ()->
                service.patch(dto));
        Assertions.assertEquals("District does not exist with given credentials", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("patchIfSuccessful")
    void patchIfSuccessful(){
        ShopPatchDto dto = new ShopPatchDto();
        dto.setId(1L);
        dto.setDistrictId(2L);
        when(shopHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(new Shop());
        when(districtHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(new District());

        Assertions.assertDoesNotThrow(()->service.patch(dto));
        verify(shopHib).update(any(), any(Logger.class));
         }
}
