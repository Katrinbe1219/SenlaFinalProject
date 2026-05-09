package org.example.core.services.documents.prices;

import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.PriceCreateDto;
import org.example.core.dto.getting.prices.PriceGetResultForModerator;
import org.example.core.dto.kafka.PriceChangedMessage;
import org.example.core.dto.kafka.PriceCreatedMessage;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.prices.PriceFilter;
import org.example.core.hibernate.base_settings.service_dto.CheckingPriceGoodShopExistence;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.hibernate.documents.prices.PriceHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.ShopHibImpl;
import org.example.core.models.Good;
import org.example.core.models.Price;
import org.example.core.models.Shop;
import org.example.core.services.documents.prices.data.OptionForUpload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PriceServiceTest {
    @Mock
    PriceHibImpl priceHib;
    @Mock
    GoodHibImpl goodHib;
    @Mock
    ShopHibImpl shopHib;

    @Mock
    ApplicationEventPublisher eventPublisher;
    @Mock
    CategoryHibImpl categoryHibImpl;

    @InjectMocks
    PriceService service;

    @Test
    @Tag("negative")
    @DisplayName("getByIdForModeratorIfNotFound")
    void getByIdForModeratorIfNotFound() {
        when(priceHib.getByIdForModerator(anyLong())).thenReturn(null);

        Exception ex = Assertions.assertThrows(
                DoesNoeExist.class,
                () -> service.getByIdForModerator(1L)
        );
        Assertions.assertEquals("Price does not exist with given credentials",
                ex.getMessage());
    }

    @Test
    @Tag("positive")
    @DisplayName("getByIdForModeratorIfSuccessful")
    void getByIdForModeratorIfSuccessful() {
        PriceGetResultForModerator expected = new PriceGetResultForModerator();
        when(priceHib.getByIdForModerator(anyLong())).thenReturn(expected);

        PriceGetResultForModerator result = service.getByIdForModerator(1L);

        Assertions.assertEquals(expected, result);
    }

    @Test
    @Tag("positive")
    @DisplayName("getByFiltersIfNoCategoryIds")
    void getByFiltersIfNoCategoryIds() {
        PriceFilter filters = new PriceFilter();
        filters.setCategoryIds(null);
        when(priceHib.getPricesByFilter(any(), any(), any())).thenReturn(List.of());

        service.getByFilters(filters);

        verify(categoryHibImpl, never()).findAllChildCategoryIds(any());
        verify(priceHib).getPricesByFilter(any(), any(), any());
    }

    @Test
    @Tag("positive")
    @DisplayName("getByFiltersIfCategoryIdsExpanded")
    void getByFiltersIfCategoryIdsExpanded() {
        PriceFilter filters = new PriceFilter();
        filters.setCategoryIds(List.of(1L, 2L));

        List<Long> expanded = List.of(1L, 2L, 3L, 4L);
        when(categoryHibImpl.findAllChildCategoryIds(any())).thenReturn(expanded);
        when(priceHib.getPricesByFilter(any(), any(), any())).thenReturn(List.of());

        service.getByFilters(filters);

        verify(categoryHibImpl).findAllChildCategoryIds(any());
        Assertions.assertEquals(expanded, filters.getCategoryIds());
    }

    @Test
    @Tag("positive")
    @DisplayName("getByFiltersIfDatesConverted")
    void getByFiltersIfDatesConverted() {
        PriceFilter filters = new PriceFilter();
        filters.setStartDate(LocalDate.of(2024, 1, 1));
        filters.setEndDate(LocalDate.of(2024, 1, 31));
        when(priceHib.getPricesByFilter(any(), any(), any())).thenReturn(List.of());

        service.getByFilters(filters);


        verify(priceHib).getPricesByFilter(
                any(),
                argThat(Objects::nonNull),
                argThat(Objects::nonNull)
        );
    }

    @Test
    @Tag("negative")
    @DisplayName("updatePriceIfCheckingNull")
    void updatePriceIfCheckingNull() {
        PriceCreateDto dto = new PriceCreateDto();
        dto.setShopId(1L);
        dto.setGoodId(2L);
        dto.setPrice(BigDecimal.ONE);

        when(priceHib.checkBeforeAddPrice(anyLong(), anyLong())).thenReturn(null);

        Exception ex = Assertions.assertThrows(
                DoesNoeExist.class,
                () -> service.updatePrice(dto)
        );
        Assertions.assertEquals("Price does not exist with given credentials",ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("updatePriceIfPriceIdNull")
    void updatePriceIfPriceIdNull() {
        PriceCreateDto dto = new PriceCreateDto();
        dto.setShopId(1L);
        dto.setGoodId(2L);
        dto.setPrice(BigDecimal.ONE);

        CheckingPriceGoodShopExistence checking = new CheckingPriceGoodShopExistence();
        checking.setPriceId(null);
        when(priceHib.checkBeforeAddPrice(anyLong(), anyLong())).thenReturn(checking);

        Exception ex = Assertions.assertThrows(
                DoesNoeExist.class,
                () -> service.updatePrice(dto)
        );
        Assertions.assertEquals("Price does not exist with given credentials",ex.getMessage());
    }

    @Test
    @Tag("positive")
    @DisplayName("updatePriceIfSuccessfulWithEvent")
    void updatePriceIfSuccessfulWithEvent() {
        CheckingPriceGoodShopExistence checking = new CheckingPriceGoodShopExistence();
        checking.setPriceId(1L);

        PriceCreateDto dto = new PriceCreateDto();
        dto.setGoodId(1L);
        dto.setShopId(1L);
        dto.setPrice(BigDecimal.valueOf(100));

        Good good = new Good();
        Shop shop = new Shop();
        Price saved = new Price();
        saved.setGood(good);
        saved.setShop(shop);
        saved.setPrice(BigDecimal.valueOf(100));
        saved.setValidFrom(Instant.now());

        when(priceHib.checkBeforeAddPrice(anyLong(), anyLong())).thenReturn(checking);
        when(goodHib.getReferenceById(anyLong())).thenReturn(good);
        when(shopHib.getReferenceById(anyLong())).thenReturn(shop);
        when(priceHib.makeInvalidPrice(anyLong(), anyLong())).thenReturn(1);
        when(priceHib.update(any(Price.class), any(Logger.class))).thenReturn(saved);

        service.updatePrice(dto);

        verify(eventPublisher).publishEvent(any(PriceChangedMessage.class));
        verify(priceHib).update(any(Price.class), any(Logger.class));
    }

    @Test
    @Tag("negative")
    @DisplayName("createPriceIfAlreadyExists")
    void createPriceIfAlreadyExists() {
        CheckingPriceGoodShopExistence checking = new CheckingPriceGoodShopExistence();
        checking.setPriceId(1L);

        when(priceHib.checkBeforeAddPrice(anyLong(), anyLong())).thenReturn(checking);

        PriceCreateDto dto = new PriceCreateDto();
        dto.setGoodId(1L);
        dto.setShopId(1L);

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> service.createPrice(dto)
        );
        Assertions.assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @Tag("positive")
    @DisplayName("createPriceIfSuccessful")
    void createPriceIfSuccessful() {
        PriceCreateDto dto = new PriceCreateDto();
        dto.setGoodId(1L);
        dto.setShopId(1L);
        dto.setPrice(BigDecimal.valueOf(100));

        Good good = new Good();
        Shop shop = new Shop();
        Price saved = new Price();
        saved.setGood(good);
        saved.setShop(shop);
        saved.setPrice(BigDecimal.valueOf(100));
        saved.setValidFrom(Instant.now());

        CheckingPriceGoodShopExistence checking = new CheckingPriceGoodShopExistence();
        checking.setShopId(1L);
        checking.setGoodId(1L);

        when(priceHib.checkBeforeAddPrice(anyLong(), anyLong())).thenReturn(checking);
        when(goodHib.getReferenceById(anyLong())).thenReturn(good);
        when(shopHib.getReferenceById(anyLong())).thenReturn(shop);
        when(priceHib.save(any(Price.class), any(Logger.class))).thenReturn(saved);

        service.createPrice(dto);

        verify(eventPublisher).publishEvent(any(PriceCreatedMessage.class));
        verify(priceHib).save(any(Price.class), any(Logger.class));
    }


    @Test
    @Tag("negative")
    @DisplayName("createPriceIfShopNotFound")
    void createPriceIfShopNotFound() {
        CheckingPriceGoodShopExistence checking = new CheckingPriceGoodShopExistence();
        checking.setPriceId(null);
        checking.setShopId(null);
        when(priceHib.checkBeforeAddPrice(anyLong(), anyLong())).thenReturn(checking);

        PriceCreateDto dto = new PriceCreateDto();
        dto.setGoodId(1L);
        dto.setShopId(1L);

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> service.createPrice(dto)
        );
        Assertions.assertEquals("Shop does not exist with given credentials",ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("createPriceIfGoodNotFound")
    void createPriceIfGoodNotFound() {
        CheckingPriceGoodShopExistence checking = new CheckingPriceGoodShopExistence();
        checking.setPriceId(null);
        checking.setShopId(1L);
        checking.setGoodId(null);
        when(priceHib.checkBeforeAddPrice(anyLong(), anyLong())).thenReturn(checking);

        PriceCreateDto dto = new PriceCreateDto();
        dto.setGoodId(1L);
        dto.setShopId(1L);

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> service.createPrice(dto)
        );
        Assertions.assertEquals("Good does not exist with given credentials",ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("saveAllIfRepositoryFailed")
    void saveAllIfRepositoryFailed(){

        when(priceHib.makeInvalidManyWithReturning(any(), any()))
                .thenThrow(new NonHibernateException("testing"));
        Exception ex = Assertions.assertThrows(NonHibernateException.class,
                () ->  service.saveAll(List.of(), OptionForUpload.REPLACE, true));
        Assertions.assertEquals("testing", ex.getMessage());

    }

    @Test
    @Tag("positive")
    @DisplayName("saveAllIfStopAndSendPublishesCreatedEvents")
    void saveAllIfStopAndSendPublishesCreatedEvents() {
        PriceCreateDto dto1 = new PriceCreateDto();
        dto1.setGoodId(1L);
        dto1.setShopId(1L);
        dto1.setPrice(BigDecimal.valueOf(100));

        PriceCreateDto dto2 = new PriceCreateDto();
        dto2.setGoodId(2L);
        dto2.setShopId(2L);
        dto2.setPrice(BigDecimal.valueOf(200));

        service.saveAll(List.of(dto1, dto2), OptionForUpload.STOP, true);

        verify(priceHib).saveAll(any(), eq(OptionForUpload.STOP));
        verify(eventPublisher,
                times(2)).publishEvent(any(PriceCreatedMessage.class));
        verify(eventPublisher,
                never()).publishEvent(any(PriceChangedMessage.class));
    }

    @Test
    @Tag("positive")
    @DisplayName("saveAllIfStopAndNotSendNoEvents")
    void saveAllIfStopAndNotSendNoEvents() {
        PriceCreateDto dto = new PriceCreateDto();
        dto.setGoodId(1L);
        dto.setShopId(1L);
        dto.setPrice(BigDecimal.valueOf(100));

        service.saveAll(List.of(dto), OptionForUpload.STOP, false);

        verify(priceHib).saveAll(any(), eq(OptionForUpload.STOP));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @Tag("positive")
    @DisplayName("saveAllIfSkipAndSendNoEvents")
    void saveAllIfSkipAndSendNoEvents() {
        PriceCreateDto dto = new PriceCreateDto();
        dto.setGoodId(1L);
        dto.setShopId(1L);
        dto.setPrice(BigDecimal.valueOf(100));

        service.saveAll(List.of(dto), OptionForUpload.SKIP, true);

        verify(priceHib).saveAll(any(), eq(OptionForUpload.SKIP));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @Tag("positive")
    @DisplayName("saveAllIfReplaceAndSendWithOldPricePublishesChangedEvent")
    void saveAllIfReplaceAndSendWithOldPricePublishesChangedEvent() {
        PriceCreateDto dto = new PriceCreateDto();
        dto.setGoodId(1L);
        dto.setShopId(1L);
        dto.setPrice(BigDecimal.valueOf(200));

        PriceCreateDto dto1 = new PriceCreateDto();
        dto1.setGoodId(2L);
        dto1.setShopId(2L);
        dto1.setPrice(BigDecimal.valueOf(150));

        // старая цена существует — PriceChangedMessage
        Object[] row = new Object[]{1L, 1L, null, BigDecimal.valueOf(100)};
        Object[] row1 = new Object[]{2L, 2L, null, BigDecimal.valueOf(130)};
        List<Object[]> list = List.of(row,row1);
        when(priceHib.makeInvalidManyWithReturning(any(), any()))
                .thenReturn( list);

        service.saveAll(List.of(dto, dto1), OptionForUpload.REPLACE, true);

        verify(priceHib).saveAll(any(), eq(OptionForUpload.REPLACE));
        verify(eventPublisher, times(2)).publishEvent(any(PriceChangedMessage.class));
        verify(eventPublisher, never()).publishEvent(any(PriceCreatedMessage.class));
    }

    @Test
    @Tag("positive")
    @DisplayName("saveAllIfReplaceAndSendWithoutOldPricePublishesCreatedEvent")
    void saveAllIfReplaceAndSendWithoutOldPricePublishesCreatedEvent() {
        PriceCreateDto dto = new PriceCreateDto();
        dto.setGoodId(1L);
        dto.setShopId(1L);
        dto.setPrice(BigDecimal.valueOf(200));

        // старой цены нет — PriceCreatedMessage
        when(priceHib.makeInvalidManyWithReturning(any(), any()))
                .thenReturn(List.of());

        service.saveAll(List.of(dto), OptionForUpload.REPLACE, true);

        verify(priceHib).saveAll(any(), eq(OptionForUpload.REPLACE));
        verify(eventPublisher).publishEvent(any(PriceCreatedMessage.class));
        verify(eventPublisher, never()).publishEvent(any(PriceChangedMessage.class));
    }

    @Test
    @Tag("positive")
    @DisplayName("saveAllIfReplaceAndNotSendNoEvents")
    void saveAllIfReplaceAndNotSendNoEvents() {
        PriceCreateDto dto = new PriceCreateDto();
        dto.setGoodId(1L);
        dto.setShopId(1L);
        dto.setPrice(BigDecimal.valueOf(100));

        service.saveAll(List.of(dto), OptionForUpload.REPLACE, false);

        // makeInvalidManyWithReturning не должен вызываться
        verify(priceHib, never()).makeInvalidManyWithReturning(any(), any());
        verify(priceHib).saveAll(any(), eq(OptionForUpload.REPLACE));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @Tag("positive")
    @DisplayName("saveAllIfReplaceMixedOldAndNewPrices")
    void saveAllIfReplaceMixedOldAndNewPrices() {
        PriceCreateDto existing = new PriceCreateDto();
        existing.setGoodId(1L);
        existing.setShopId(1L);
        existing.setPrice(BigDecimal.valueOf(200));

        PriceCreateDto newOne = new PriceCreateDto();
        newOne.setGoodId(2L);
        newOne.setShopId(2L);
        newOne.setPrice(BigDecimal.valueOf(300));

        // только первый dto имеет старую цену
        Object[] row = new Object[]{1L, 1L, null, BigDecimal.valueOf(100)};
        List<Object[]> rows = new ArrayList<>();
        rows.add(row);
        when(priceHib.makeInvalidManyWithReturning(any(), any()))
                .thenReturn(rows);

        service.saveAll(List.of(existing, newOne), OptionForUpload.REPLACE, true);

        verify(eventPublisher, times(1)).publishEvent(any(PriceChangedMessage.class));
        verify(eventPublisher, times(1)).publishEvent(any(PriceCreatedMessage.class));
    }


}
