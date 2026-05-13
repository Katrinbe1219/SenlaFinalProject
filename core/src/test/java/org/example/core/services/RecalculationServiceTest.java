package org.example.core.services;

import jakarta.servlet.UnavailableException;
import org.example.core.dto.getting.StringResponse;
import org.example.core.exceptions.UnavailableExecution;
import org.example.core.models.types.RatingTriggerType;
import org.example.core.models.types.RoleTypes;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RecalculationServiceTest {



    @Mock
    AsyncRecalculationService asyncRecalculationService;
    @InjectMocks
    RecalculationService recalculationService;
    @Mock
    Clock clock;

    @BeforeEach
    void setUpClock(){
        Clock daytime = Clock.fixed(
                LocalDateTime.of(2026,1,1,13,0).toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC
        );
        ReflectionTestUtils.setField(recalculationService,"clock", daytime);
    }

    @Test
    @DisplayName("personRequestIfBusy")
    @Tag("negative")
    void personRequestIfBusy(){
        ReflectionTestUtils.setField(recalculationService, "isRecalculating",
                new AtomicBoolean(true));
        Assertions.assertEquals(new StringResponse("Пересчет уже выполняется, попробуйте позже"),
                recalculationService.personRequest(1L, RoleTypes.ADMIN));
        verify(asyncRecalculationService, never()).recalculationForAll(eq(RatingTriggerType.ADMIN), any());
    }

    @Test
    @DisplayName("personRequestIfGoodId")
    @Tag("positive")
    void personRequestIfGoodId(){

        ReflectionTestUtils.setField(recalculationService, "isRecalculating",
                new AtomicBoolean(false));
        Assertions.assertEquals(new StringResponse("Успешный пересчет"),
                recalculationService.personRequest(null, RoleTypes.ADMIN));
        verify(asyncRecalculationService).recalculationForAll(eq(RatingTriggerType.ADMIN), any());
    }

    @Test
    @DisplayName("personRequestIfTimeIsInvalidForAdmin")
    @Tag("negative")
    void personRequestIfTimeIsInvalidForAdmin(){

        Clock earlyMorning = Clock.fixed(
                LocalDateTime.of(2026,1,1,1,0).toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC
        );

        ReflectionTestUtils.setField(recalculationService, "isRecalculating",
                new AtomicBoolean(false));

        ReflectionTestUtils.setField(recalculationService,"clock", earlyMorning);

        Assertions.assertEquals(new StringResponse("Время пересчета ограничено для всех продуктов, попробуйте позже"),
                recalculationService.personRequest(null, RoleTypes.ADMIN));
        verify(asyncRecalculationService, never()).recalculationForAll(eq(RatingTriggerType.ADMIN), any());
    }

    @Test
    @DisplayName("personRequestIfTimeIsValidForAdmin")
    @Tag("positive")
    void personRequestIfTimeIsValidForAdmin(){

        // дефолтный клок из setUp @BeforeEach
        ReflectionTestUtils.setField(recalculationService, "isRecalculating",
                new AtomicBoolean(false));

        Assertions.assertEquals(new StringResponse("Успешный пересчет"),
                recalculationService.personRequest(null, RoleTypes.ADMIN));
        verify(asyncRecalculationService).recalculationForAll(eq(RatingTriggerType.ADMIN), any());
    }


    @Test
    @DisplayName("personRequestIfTimeIsInvalidForModerator")
    @Tag("negative")
    void personRequestIfTimeIsInvalidForModerator(){

        Clock earlyMorning = Clock.fixed(
                LocalDateTime.of(2026,1,1,2,55).toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC
        );

        ReflectionTestUtils.setField(recalculationService, "isRecalculating",
                new AtomicBoolean(false));

        ReflectionTestUtils.setField(recalculationService,"clock", earlyMorning);

        Assertions.assertEquals(new StringResponse("Время пересчета ограничено для всех продуктов, попробуйте позже"),
                recalculationService.personRequest(1L, RoleTypes.MODERATOR));
        verify(asyncRecalculationService, never()).recalculationForGood(anyLong(), any(), any());
    }

    @Test
    @DisplayName("personRequestIfTimeIsValidForModerator")
    @Tag("positive")
    void personRequestIfTimeIsValidForModerator(){

        // дефолтный клок из setUp @BeforeEach
        ReflectionTestUtils.setField(recalculationService, "isRecalculating",
                new AtomicBoolean(false));

        Assertions.assertEquals(new StringResponse("Успешный пересчет"),
                recalculationService.personRequest(1L, RoleTypes.MODERATOR));
        verify(asyncRecalculationService).recalculationForGood(anyLong(), any(), any());
    }

    @Test
    @DisplayName("recalculationIfBusy")
    @Tag("negative")
    void recalculationIfBusy(){
        ReflectionTestUtils.setField(recalculationService, "isRecalculating",
                new AtomicBoolean(true));
        Assertions.assertThrows(UnavailableExecution.class, () -> recalculationService.recalculation(RatingTriggerType.SCHEDULED));
    }

    @Test
    @DisplayName("recalculationIfSuccessful")
    @Tag("positive")
    void recalculationIfSuccessful(){
        ReflectionTestUtils.setField(recalculationService, "isRecalculating",
                new AtomicBoolean(false));
         recalculationService.recalculation(RatingTriggerType.SCHEDULED);
         verify(asyncRecalculationService).recalculationForAll(eq(RatingTriggerType.SCHEDULED), any());

    }



}
