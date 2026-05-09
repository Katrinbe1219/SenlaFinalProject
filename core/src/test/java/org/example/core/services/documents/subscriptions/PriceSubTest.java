package org.example.core.services.documents.subscriptions;

import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.PriceSubCreateDto;
import org.example.core.dto.getting.subscriptions.PriceSubGetDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.subscriptions.PriceSubFilter;
import org.example.core.hibernate.base_settings.service_dto.CheckForPriceSubscription;
import org.example.core.hibernate.documents.subscriptions.PriceSubHib;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.ShopHibImpl;
import org.example.core.mapping.subscriptions.PriceSubGetMapper;
import org.example.core.models.Good;
import org.example.core.models.PriceSubscription;
import org.example.core.models.Shop;
import org.example.core.models.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PriceSubTest {
    @InjectMocks
    PriceSubService service;
    @Mock
    GoodHibImpl goodHibImpl;
    @Mock
    ShopHibImpl shopHibImpl;
    @Mock
    PriceSubGetMapper mapper;
    @Mock
    PriceSubHib priceSubHib;

    @Test
    @Tag("positive")
    @DisplayName("findAllIfSuccessfulEmpty")
    void findAllIfSuccessfulEmpty() {
        when(priceSubHib.findAll(any())).thenReturn(List.of());
        Assertions.assertEquals(List.of(), service.findAll(new PriceSubFilter()));
        verify(priceSubHib).findAll(any());
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllIfSuccessfulNotEmpty")
    void findAllIfSuccessfulNotEmpty() {
        PriceSubGetDto dto = new PriceSubGetDto();
        PriceSubscription price = new PriceSubscription();

        when(priceSubHib.findAll(any()))
                .thenReturn(List.of(price, price));
        when(mapper.toDto(any(PriceSubscription.class))).thenReturn(dto);


        Assertions.assertEquals(List.of(dto,dto), service.findAll(new PriceSubFilter()));
        verify(mapper, times(2)).toDto(any());
    }

    @Test
    @Tag("negative")
    @DisplayName("findAllIfRepositoryFailed")
    void findAllIfRepositoryFailed() {

        when(priceSubHib.findAll(any()))
                .thenThrow(new RuntimeException("problem"));

        Exception ex = Assertions.assertThrows(RuntimeException.class, ()-> service.findAll(null));
        Assertions.assertEquals("problem", ex.getMessage());
        verify(mapper, never()).toDto(any());
    }

    @Test
    @Tag("negative")
    @DisplayName("createSubscriptionIfPriceDoesNotExist")
    void createSubscriptionIfPriceDoesNotExist() {

        PriceSubCreateDto filter = new PriceSubCreateDto();
        filter.setPrice(BigDecimal.valueOf(123));
        filter.setGoodId(1L);
        filter.setShopId(2L);



        User user   = new User();
        user.setId(1L);

        when(priceSubHib.checking(
                any(), any(), any()
        )).thenReturn(new CheckForPriceSubscription());

        Exception ex = Assertions.assertThrows(DoesNoeExist.class, ()-> service.createSubscription(filter, user));
        Assertions.assertEquals("Price does not exist with given credentials", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("createSubscriptionIfPricePresented")
    void createSubscriptionIfPricePresented() {

        CheckForPriceSubscription dto = new CheckForPriceSubscription();
        dto.setPrice(BigDecimal.valueOf(123.4));

        PriceSubCreateDto filter = new PriceSubCreateDto();
        filter.setPrice(BigDecimal.valueOf(123));
        filter.setGoodId(1L);
        filter.setShopId(2L);



        User user   = new User();
        user.setId(1L);

        when(priceSubHib.checking(
                any(), any(), any()
        )).thenReturn(dto);

        Exception ex = Assertions.assertThrows(NotCorrectInput.class,
                ()-> service.createSubscription(filter,user ));
        Assertions.assertEquals("Price is already presented ", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("createSubscriptionIfSubscriptionPresented")
    void createSubscriptionIfSubscriptionPresented() {

        CheckForPriceSubscription dto = new CheckForPriceSubscription();
        dto.setPrice(BigDecimal.valueOf(123.4));
        dto.setPriceSubId(123L);

        PriceSubCreateDto filter = new PriceSubCreateDto();
        filter.setPrice(BigDecimal.valueOf(110));
        filter.setGoodId(1L);
        filter.setShopId(2L);



        User user   = new User();
        user.setId(1L);

        when(priceSubHib.checking(
                any(), any(), any()
        )).thenReturn(dto);

        Exception ex = Assertions.assertThrows(NotCorrectInput.class,
                ()-> service.createSubscription(filter,user ));
        Assertions.assertEquals("Subscription is already presented ", ex.getMessage());
    }

    @Test
    @Tag("negative")
    @DisplayName("createSubscriptionIfSuccessful")
    void createSubscriptionIfSuccessful() {

        CheckForPriceSubscription dto = new CheckForPriceSubscription();
        dto.setPrice(BigDecimal.valueOf(123.4));


        PriceSubCreateDto filter = new PriceSubCreateDto();
        filter.setPrice(BigDecimal.valueOf(110));
        filter.setGoodId(1L);
        filter.setShopId(2L);



        User user   = new User();
        user.setId(1L);

        when(priceSubHib.checking(
                any(), any(), any()
        )).thenReturn(dto);

        Shop shop = new Shop();
        Good good = new Good();
        PriceSubscription price = new PriceSubscription();
        PriceSubGetDto res = new PriceSubGetDto();

        when(shopHibImpl.getReferenceById(any())).thenReturn(shop);
        when(goodHibImpl.getReferenceById(any())).thenReturn(good);
        when(priceSubHib.save(any(), any(Logger.class))).thenReturn(price);
        when(mapper.toDto(any(PriceSubscription.class))).thenReturn(res);

        Assertions.assertEquals(res, service.createSubscription(filter, user ));


    }
}
