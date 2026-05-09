package org.example.core.services.documents;

import org.example.core.dto.getting.rates.RateFullDto;
import org.example.core.hibernate.base_settings.filters.rates.RatingRecalcFilter;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.hibernate.documents.RateHibImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RateServiceTest {
    @Mock
    CategoryHibImpl categoryHibImpl;
    @Mock
    RateHibImpl rateHibImpl;
    @InjectMocks
    RateService service;

    @Test
    @Tag("positive")
    @DisplayName("getGoodRatesByFilterIfCategoryIdsExpanded")
    void getGoodRatesByFilterIfCategoryIdsExpanded() {
        RatingRecalcFilter filters = new RatingRecalcFilter();
        filters.setCategoryIds(List.of(1L, 2L));

        List<Long> expanded = List.of(1L, 2L, 3L, 4L);
        when(categoryHibImpl.findAllChildCategoryIds(any(List.class)))
                .thenReturn(expanded);
        when(rateHibImpl.getRatesByFilter(any(RatingRecalcFilter.class)))
                .thenReturn(List.of());

        service.getGoodRatesByFilter(filters);

        verify(categoryHibImpl).findAllChildCategoryIds(any(List.class));
        Assertions.assertEquals(expanded, filters.getCategoryIds());
    }

    @Test
    @Tag("positive")
    @DisplayName("getGoodRatesByFilterIfNoCategoryIds")
    void getGoodRatesByFilterIfNoCategoryIds() {
        RatingRecalcFilter filters = new RatingRecalcFilter();

        when(rateHibImpl.getRatesByFilter(any(RatingRecalcFilter.class)))
                .thenReturn(List.of());

        Assertions.assertEquals(List.of(),service.getGoodRatesByFilter(filters));
        verify(categoryHibImpl, never()).findAllChildCategoryIds(any());
    }

    @Test
    @Tag("positive")
    @DisplayName("getGoodRatesByFilterReturnsResult")
    void getGoodRatesByFilterReturnsResult() {
        RatingRecalcFilter filters = new RatingRecalcFilter();
        List<RateFullDto> expected = List.of(new RateFullDto(), new RateFullDto());

        when(rateHibImpl.getRatesByFilter(any(RatingRecalcFilter.class)))
                .thenReturn(expected);

        List<RateFullDto> result = service.getGoodRatesByFilter(filters);

        Assertions.assertEquals(2, result.size());
    }

    @Test
    @Tag("negative")
    @DisplayName("getGoodRatesByFilterIfRepositoryThrows")
    void getGoodRatesByFilterIfRepositoryThrows() {
        RatingRecalcFilter filters = new RatingRecalcFilter();
        when(rateHibImpl.getRatesByFilter(any(RatingRecalcFilter.class)))
                .thenThrow(new RuntimeException("db error"));

        Assertions.assertThrows(
                RuntimeException.class,
                () -> service.getGoodRatesByFilter(filters)
        );
    }

}
