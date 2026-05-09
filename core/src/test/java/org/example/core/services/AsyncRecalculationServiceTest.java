package org.example.core.services;

import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.statistics.RecalculationForGoodDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.documents.RateHibImpl;
import org.example.core.hibernate.documents.prices.PriceForCalculationHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.models.Good;
import org.example.core.models.types.RatingStatus;
import org.example.core.models.types.RatingTriggerType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AsyncRecalculationServiceTest {
    @InjectMocks
    AsyncRecalculationService asyncRecalculationService;

    @Mock
    GoodHibImpl goodHib;
    @Mock
    PriceForCalculationHibImpl priceHib;
    @Mock
    RateHibImpl rateHib;

    @Test
    @DisplayName("recalculationForGoodIfDoesNotExist")
    @Tag("negative")
    void recalculationForGoodIfDoesNotExist(){
        AtomicBoolean flag = new AtomicBoolean(true);
        when(goodHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(null);
        Assertions.assertThrows(NotCorrectInput.class, () -> {
            asyncRecalculationService.recalculationForGood(1L,flag );
        });
    }

    @Test
    @DisplayName("recalculationForGoodIfExceptionWasThrown")
    @Tag("negative")
    void recalculationForGoodIfExceptionWasThrown(){
        AtomicBoolean flag = new AtomicBoolean(true);
        when(goodHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(new Good());
        when(priceHib.recalculateForGood(anyLong()))
                .thenThrow(new RuntimeException("Проблема"));

       Assertions.assertThrows(RuntimeException.class, () -> asyncRecalculationService.recalculationForGood(1L,flag ));
        verify(rateHib).saveErrors(any(), eq("Проблема"), eq(RatingStatus.FAILED), eq(RatingTriggerType.MODERATOR));
        verify(rateHib, never()).saveLog(any(), any(), eq(RatingStatus.SUCCESS), eq(RatingTriggerType.MODERATOR), any(), any());
    }

    @Test
    @DisplayName("recalculationForGoodIfSuccessful")
    @Tag("positive")
    void recalculationForGoodIfSuccessful(){
        Good oldGood = new Good();
        oldGood.setRate(1D);
        AtomicBoolean flag = new AtomicBoolean(true);

        when(goodHib.findById(anyLong(), any(Logger.class)))
                .thenReturn(oldGood);
        when(priceHib.recalculateForGood(anyLong()))
                .thenReturn(2D);

        asyncRecalculationService.recalculationForGood(1L,flag);

        verify(rateHib, never()).saveErrors(any(), eq("Проблема"), eq(RatingStatus.FAILED), eq(RatingTriggerType.MODERATOR));
        verify(rateHib).saveLog(any(), any(), eq(RatingStatus.SUCCESS), eq(RatingTriggerType.MODERATOR), eq(oldGood.getRate()), eq(2D));
        Assertions.assertFalse(flag.get());
    }

    @Test
    @DisplayName("recalculationForAllIfBatchFailedAllTimes")
    @Tag("positive")
    void recalculationForAllIfBatchFailedAllTimes(){
        List<RecalculationForGoodDto> list = List.of(new RecalculationForGoodDto(1L,3d),
                new RecalculationForGoodDto(2L,3d),
                new RecalculationForGoodDto(3L,4d));
        AtomicBoolean flag = new AtomicBoolean(true);

        when(goodHib.getAllIdsForRecalculation()).thenReturn(list);
        when(priceHib.recalculateForAllGoods(any()))
                .thenThrow(new RuntimeException("Not Found"));


        asyncRecalculationService.recalculationForAll(RatingTriggerType.SCHEDULED, flag);

        verify(rateHib).saveErrors(any(), eq("Not Found"), eq(RatingStatus.FAILED),eq(RatingTriggerType.SCHEDULED) );
        verify(priceHib, times(3)).recalculateForAllGoods(any());
        Assertions.assertFalse(flag.get());
    }

    @Test
    @DisplayName("recalculationForAllIfBatchFailedAllTimes")
    @Tag("negative")
    void recalculationForAllIfBatchFailedTwoTimes(){
        List<RecalculationForGoodDto> list = List.of(new RecalculationForGoodDto(1L,3d),
                new RecalculationForGoodDto(2L,3d),
                new RecalculationForGoodDto(3L,4d));
        AtomicBoolean flag = new AtomicBoolean(true);

        when(goodHib.getAllIdsForRecalculation()).thenReturn(list);
        when(priceHib.recalculateForAllGoods(any()))
                .thenThrow(new RuntimeException("Not Found"))
                .thenThrow(new RuntimeException("Not Found"))
                .thenReturn(null);


        asyncRecalculationService.recalculationForAll(RatingTriggerType.SCHEDULED, flag);
        verify(rateHib).saveLogs(any(), eq(null), eq(RatingStatus.SUCCESS), eq(RatingTriggerType.SCHEDULED), any());
        verify(rateHib, never()).saveErrors(any(), any(), eq(RatingStatus.FAILED),eq(RatingTriggerType.SCHEDULED) );
        verify(priceHib, times(3)).recalculateForAllGoods(any());
        Assertions.assertFalse(flag.get());
    }

    @Test
    @DisplayName("recalculationForAllIfGoodsWereNotFoundAllTimes")
    @Tag("negative")
    void recalculationForAllIfGoodsWereNotFoundAllTimes(){
        AtomicBoolean flag = new AtomicBoolean(true);

        when(goodHib.getAllIdsForRecalculation())
                .thenThrow(new RuntimeException("Not Found"));
        asyncRecalculationService.recalculationForAll(RatingTriggerType.SCHEDULED, flag);

        verify(goodHib, times(3)).getAllIdsForRecalculation();
        Assertions.assertFalse(flag.get());
    }

    @Test
    @DisplayName("recalculationForAllIfGoodsWereNotFoundTwoTimes")
    @Tag("positive")
    void recalculationForAllIfGoodsWereNotFoundTwoTimes(){
        AtomicBoolean flag = new AtomicBoolean(true);
        List<RecalculationForGoodDto> list = List.of(new RecalculationForGoodDto(1L,3d),
                new RecalculationForGoodDto(2L,3d),
                new RecalculationForGoodDto(3L,4d));

        when(goodHib.getAllIdsForRecalculation())
                .thenThrow(new RuntimeException("Not Found"))
                .thenThrow(new RuntimeException("Not Found"))
                .thenReturn(list);

        asyncRecalculationService.recalculationForAll(RatingTriggerType.SCHEDULED, flag);

        verify(goodHib, times(3)).getAllIdsForRecalculation();
        verify(rateHib).saveLogs(any(), eq(null), eq(RatingStatus.SUCCESS), eq(RatingTriggerType.SCHEDULED), any());
        verify(rateHib, never()).saveErrors(any(),any(), eq(RatingStatus.FAILED),eq(RatingTriggerType.SCHEDULED) );
        Assertions.assertFalse(flag.get());
    }

    @Test
    @DisplayName("recalculationForAllIfSuccessful")
    @Tag("positive")
    void recalculationForAllIfSuccessful(){
        AtomicBoolean flag = new AtomicBoolean(true);
        List<RecalculationForGoodDto> list = List.of(new RecalculationForGoodDto(1L,3d),
                new RecalculationForGoodDto(2L,3d),
                new RecalculationForGoodDto(3L,4d));

        when(goodHib.getAllIdsForRecalculation())
                .thenReturn(list);

        asyncRecalculationService.recalculationForAll(RatingTriggerType.SCHEDULED, flag);
        verify(goodHib).getAllIdsForRecalculation();
        verify(rateHib).saveLogs(any(), eq(null), eq(RatingStatus.SUCCESS), eq(RatingTriggerType.SCHEDULED), any());
        verify(rateHib, never()).saveErrors(any(), any(), eq(RatingStatus.FAILED),eq(RatingTriggerType.SCHEDULED) );
        verify(priceHib).recalculateForAllGoods(any());

        Assertions.assertFalse(flag.get());
    }


}
