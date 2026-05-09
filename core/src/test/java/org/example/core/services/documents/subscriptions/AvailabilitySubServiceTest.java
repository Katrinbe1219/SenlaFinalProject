package org.example.core.services.documents.subscriptions;

import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.subscriptions.AvailabilitySubGetDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.CheckingMultiExistenceHib;
import org.example.core.hibernate.base_settings.filters.subscriptions.AvailabilitySubFilter;
import org.example.core.hibernate.documents.subscriptions.AvailabilitySubHib;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.ShopHibImpl;
import org.example.core.mapping.subscriptions.AvailabilitySubGetMapper;
import org.example.core.models.AvailabilitySubscription;
import org.example.core.models.Good;
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

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AvailabilitySubServiceTest {
    @Mock
    private AvailabilitySubHib availabilitySubHib;

    @Mock
    private AvailabilitySubGetMapper mapper;

    @Mock
    private GoodHibImpl goodHibImpl;

    @Mock
    private ShopHibImpl shopHibImpl;

    @Mock
    private CheckingMultiExistenceHib checkHib;

    @InjectMocks
    private AvailabilitySubService service;


    @Test
    @Tag("positive")
    @DisplayName("findAllIfEmpty")
    void findAllIfEmpty() {
        when(availabilitySubHib.findAll(any())).thenReturn(List.of());

        List<AvailabilitySubGetDto> result = service.findAll(new AvailabilitySubFilter());

        Assertions.assertTrue(result.isEmpty());
        verify(mapper, never()).toDto(any());
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllIfSuccessful")
    void findAllIfSuccessful() {
        AvailabilitySubscription sub = new AvailabilitySubscription();
        AvailabilitySubGetDto dto = new AvailabilitySubGetDto();

        when(availabilitySubHib.findAll(any())).thenReturn(List.of(sub, sub));
        when(mapper.toDto(any())).thenReturn(dto);

        List<AvailabilitySubGetDto> result = service.findAll(new AvailabilitySubFilter());

        Assertions.assertEquals(2, result.size());
        verify(mapper, times(2)).toDto(any());
    }

    @Test
    @Tag("negative")
    @DisplayName("findAllIfRepositoryThrows")
    void findAllIfRepositoryThrows() {
        when(availabilitySubHib.findAll(any()))
                .thenThrow(new RuntimeException("db error"));

        Assertions.assertThrows(
                RuntimeException.class,
                () -> service.findAll(new AvailabilitySubFilter())
        );
    }


    @Test
    @Tag("negative")
    @DisplayName("createSubscriptionIfGoodNotFound")
    void createSubscriptionIfGoodNotFound() {
        User user = new User();
        user.setId(1L);
        when(checkHib.checkShopAndGoodByIds(anyLong(), anyLong(), anyLong()))
                .thenReturn(Map.of("goodId", false));

        Exception ex = Assertions.assertThrows(
                DoesNoeExist.class,
                () -> service.createSubscription(user, 1L, 1L)
        );
        Assertions.assertTrue(ex.getMessage().contains("Good does not exist with given credentials"));
    }

    @Test
    @Tag("negative")
    @DisplayName("createSubscriptionIfShopNotFound")
    void createSubscriptionIfShopNotFound() {
        User user = new User();
        user.setId(1L);
        when(checkHib.checkShopAndGoodByIds(anyLong(), anyLong(), anyLong()))
                .thenReturn(Map.of("goodId", true, "shopId", false));

        Exception ex = Assertions.assertThrows(
                DoesNoeExist.class,
                () -> service.createSubscription(user, 1L, 1L)
        );
        Assertions.assertTrue(ex.getMessage().contains("Shop does not exist with given credentials"));
    }

    @Test
    @Tag("negative")
    @DisplayName("createSubscriptionIfGoodExistsInShop")
    void createSubscriptionIfGoodExistsInShop() {
        User user = new User();
        user.setId(1L);
        when(checkHib.checkShopAndGoodByIds(anyLong(), anyLong(), anyLong()))
                .thenReturn(Map.of("goodId", true, "shopId", true, "priceId", true));

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> service.createSubscription(user, 1L, 1L)
        );
        Assertions.assertTrue(ex.getMessage().contains("Good exists in current shop"));
    }

    @Test
    @Tag("negative")
    @DisplayName("createSubscriptionIfAlreadyExists")
    void createSubscriptionIfAlreadyExists() {
        User user = new User();
        user.setId(1L);
        when(checkHib.checkShopAndGoodByIds(anyLong(), anyLong(), anyLong()))
                .thenReturn(Map.of("goodId", true,
                        "shopId", true,
                        "priceId", false,
                        "sub", true));

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> service.createSubscription(user, 1L, 1L)
        );
        Assertions.assertTrue(ex.getMessage().contains("Subscription already exists"));
    }

    @Test
    @Tag("positive")
    @DisplayName("createSubscriptionIfSuccessful")
    void createSubscriptionIfSuccessful() {
        User user = new User();
        user.setId(1L);
        when(checkHib.checkShopAndGoodByIds(anyLong(), anyLong(), anyLong()))
                .thenReturn(Map.of(
                        "goodId", true,
                        "shopId", true,
                        "priceId", false,
                        "sub", false
                ));

        Shop shop = new Shop();
        Good good = new Good();
        AvailabilitySubscription saved = new AvailabilitySubscription();
        AvailabilitySubGetDto dto = new AvailabilitySubGetDto();

        when(shopHibImpl.getReferenceById(anyLong())).thenReturn(shop);
        when(goodHibImpl.getReferenceById(anyLong())).thenReturn(good);
        when(availabilitySubHib.save(any(AvailabilitySubscription.class), any(Logger.class)))
                .thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(dto);

        AvailabilitySubGetDto result = service.createSubscription(user, 1L, 1L);

        Assertions.assertEquals(dto, result);
        verify(availabilitySubHib).save(any(AvailabilitySubscription.class), any(Logger.class));
    }
}
