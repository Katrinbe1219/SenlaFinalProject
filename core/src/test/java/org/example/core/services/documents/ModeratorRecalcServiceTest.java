package org.example.core.services.documents;

import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.ManyModeratorLogCreateDto;
import org.example.core.dto.creating.ModeratorLogCreateDto;
import org.example.core.dto.getting.ModeratorRecalcDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.ManyIncorrectInputsException;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.ModeratorRecalcFilter;
import org.example.core.hibernate.documents.ModeratorRecalcHib;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.mapping.ModeratorRecalcMapper;
import org.example.core.models.Good;
import org.example.core.models.ModeratorRatingCheck;
import org.example.core.models.User;
import org.example.core.models.types.GoodStatusFromModerator;
import org.example.core.models.types.ModeratorVerdict;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModeratorRecalcServiceTest {

    @Mock
     ModeratorRecalcHib recalcHib;

    @Mock
     GoodHibImpl goodHib;

    @Mock
     ModeratorRecalcMapper mapper;

    @InjectMocks
     ModeratorRecalcService service;



    @Test
    @Tag("negative")
    @DisplayName("addLogIfGoodNotFound")
    void addLogIfGoodNotFound() {
        when(goodHib.findById(anyLong(), any(Logger.class))).thenReturn(null);

        Exception ex = Assertions.assertThrows(
                DoesNoeExist.class,
                () -> service.addLog(new User(), 1L, ModeratorVerdict.SUSPICIOUS, "comment")
        );
        Assertions.assertTrue(ex.getMessage().contains("Good does not exist with given credentials"));
    }

    @Test
    @Tag("negative")
    @DisplayName("addLogIfAlreadySuspicious")
    void addLogIfAlreadySuspicious() {
        Good good = new Good();
        good.setModeratorStatus(GoodStatusFromModerator.SUSPICIOUS);
        when(goodHib.findById(anyLong(), any(Logger.class))).thenReturn(good);

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> service.addLog(new User(), 1L, ModeratorVerdict.SUSPICIOUS, "comment")
        );
        Assertions.assertTrue(ex.getMessage().contains("It is already blocked"));
    }

    @Test
    @Tag("negative")
    @DisplayName("addLogIfAlreadyApprovedAndVerdictApproved")
    void addLogIfAlreadyApprovedAndVerdictApproved() {
        Good good = new Good();
        good.setModeratorStatus(GoodStatusFromModerator.APPROVED);
        when(goodHib.findById(anyLong(), any(Logger.class))).thenReturn(good);

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> service.addLog(new User(), 1L, ModeratorVerdict.APPROVED, "comment")
        );
        Assertions.assertTrue(ex.getMessage().contains("It is already unblocked"));
    }

    @Test
    @Tag("negative")
    @DisplayName("addLogIfAlreadyApprovedAndVerdictApproved")
    void addLogIfAlreadyRecalculatedAndVerdictApproved() {
        Good good = new Good();
        good.setModeratorStatus(GoodStatusFromModerator.APPROVED);
        when(goodHib.findById(anyLong(), any(Logger.class))).thenReturn(good);

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> service.addLog(new User(), 1L, ModeratorVerdict.RECALCULATED, "comment")
        );
        Assertions.assertTrue(ex.getMessage().contains("It is already unblocked"));
    }

    @Test
    @Tag("positive")
    @DisplayName("addLogSuspiciousSuccessful")
    void addLogSuspiciousSuccessful() {
        Good good = new Good();
        good.setModeratorStatus(GoodStatusFromModerator.APPROVED);
        when(goodHib.findById(anyLong(), any(Logger.class))).thenReturn(good);

        service.addLog(new User(), 1L, ModeratorVerdict.SUSPICIOUS, "comment");

        verify(recalcHib).save(any(ModeratorRatingCheck.class), any(Logger.class));
        Assertions.assertEquals(GoodStatusFromModerator.SUSPICIOUS, good.getModeratorStatus());
    }

    @Test
    @Tag("positive")
    @DisplayName("addLogApprovedSuccessful")
    void addLogApprovedSuccessful() {
        Good good = new Good();
        good.setModeratorStatus(GoodStatusFromModerator.SUSPICIOUS);
        when(goodHib.findById(anyLong(), any(Logger.class))).thenReturn(good);

        service.addLog(new User(), 1L, ModeratorVerdict.APPROVED, "comment");

        verify(recalcHib).save(any(ModeratorRatingCheck.class), any(Logger.class));
        Assertions.assertEquals(GoodStatusFromModerator.APPROVED, good.getModeratorStatus());
    }


    @Test
    @Tag("negative")
    @DisplayName("addManyLogsIfVerdictNull")
    void addManyLogsIfVerdictNull() {
        ModeratorLogCreateDto dto = new ModeratorLogCreateDto();
        dto.setGoodId(1L);
        dto.setVerdict(null);

        ManyModeratorLogCreateDto many = new ManyModeratorLogCreateDto();
        many.setVerdicts(List.of(dto));

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> service.addManyLogs(many, new User())
        );
        Assertions.assertTrue(ex.getMessage().contains("Verdict cannot be null"));
    }

    @Test
    @Tag("negative")
    @DisplayName("addManyLogsIfGoodIdsNotValid")
    void addManyLogsIfGoodIdsNotValid() {
        ModeratorLogCreateDto dto = new ModeratorLogCreateDto();
        dto.setGoodId(1L);
        dto.setVerdict(ModeratorVerdict.SUSPICIOUS);

        ManyModeratorLogCreateDto many = new ManyModeratorLogCreateDto();
        many.setVerdicts(List.of(dto));

        when(recalcHib.getModeratorRatingChecksByGoodIds(any())).thenReturn(List.of());

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> service.addManyLogs(many, new User())
        );
        Assertions.assertTrue(ex.getMessage().contains("Goods's ids are not valid"));
    }

    @Test
    @Tag("negative")
    @DisplayName("addManyLogsIfSizeMismatch")
    void addManyLogsIfSizeMismatch() {
        ModeratorLogCreateDto dto1 = new ModeratorLogCreateDto();
        dto1.setGoodId(1L);
        dto1.setVerdict(ModeratorVerdict.SUSPICIOUS);

        ModeratorLogCreateDto dto2 = new ModeratorLogCreateDto();
        dto2.setGoodId(2L);
        dto2.setVerdict(ModeratorVerdict.APPROVED);

        ManyModeratorLogCreateDto many = new ManyModeratorLogCreateDto();
        many.setVerdicts(List.of(dto1, dto2));


        ModeratorRatingCheck log = new ModeratorRatingCheck();
        Good good = new Good();
        good.setId(1L);
        log.setGood(good);
        log.setVerdict(ModeratorVerdict.APPROVED);
        when(recalcHib.getModeratorRatingChecksByGoodIds(any())).thenReturn(List.of(log));

        Exception ex = Assertions.assertThrows(
                NotCorrectInput.class,
                () -> service.addManyLogs(many, new User())
        );
        Assertions.assertTrue(ex.getMessage().contains("Goods's ids are not valid"));
    }

    @Test
    @Tag("negative")
    @DisplayName("addManyLogsIfStatusAlreadySame")
    void addManyLogsIfStatusAlreadySame() {
        ModeratorLogCreateDto dto = new ModeratorLogCreateDto();
        dto.setGoodId(1L);
        dto.setVerdict(ModeratorVerdict.SUSPICIOUS);

        ManyModeratorLogCreateDto many = new ManyModeratorLogCreateDto();
        many.setVerdicts(List.of(dto));

        ModeratorRatingCheck log = new ModeratorRatingCheck();
        Good good = new Good();
        good.setId(1L);
        log.setGood(good);
        log.setVerdict(ModeratorVerdict.SUSPICIOUS);
        when(recalcHib.getModeratorRatingChecksByGoodIds(any())).thenReturn(List.of(log));

        Assertions.assertThrows(
                ManyIncorrectInputsException.class,
                () -> service.addManyLogs(many, new User())
        );
    }

    @Test
    @Tag("positive")
    @DisplayName("addManyLogsSuccessful")
    void addManyLogsSuccessful() {
        ModeratorLogCreateDto dto = new ModeratorLogCreateDto();
        dto.setGoodId(1L);
        dto.setVerdict(ModeratorVerdict.SUSPICIOUS);

        ManyModeratorLogCreateDto many = new ManyModeratorLogCreateDto();
        many.setVerdicts(List.of(dto));

        ModeratorRatingCheck log = new ModeratorRatingCheck();
        Good good = new Good();
        good.setId(1L);
        log.setGood(good);
        log.setVerdict(ModeratorVerdict.APPROVED);
        when(recalcHib.getModeratorRatingChecksByGoodIds(any())).thenReturn(List.of(log));

        service.addManyLogs(many, new User());

        verify(recalcHib).createManyLogs(eq(many), any(User.class));
        verify(goodHib).updateStatusForMany(any());
    }


    @Test
    @Tag("positive")
    @DisplayName("findAllFullVersionIfEmpty")
    void findAllFullVersionIfEmpty() {
        when(recalcHib.findAllFullVersion(any())).thenReturn(List.of());

        List<ModeratorRecalcDto> result = service.findAllFullVersion(new ModeratorRecalcFilter());

        Assertions.assertTrue(result.isEmpty());
        verify(mapper, never()).toDto(any());
    }

    @Test
    @Tag("positive")
    @DisplayName("findAllFullVersionIfSuccessful")
    void findAllFullVersionIfSuccessful() {
        ModeratorRatingCheck check = new ModeratorRatingCheck();
        ModeratorRecalcDto dto = new ModeratorRecalcDto();

        when(recalcHib.findAllFullVersion(any())).thenReturn(List.of(check, check));
        when(mapper.toDto(any())).thenReturn(dto);

        List<ModeratorRecalcDto> result = service.findAllFullVersion(new ModeratorRecalcFilter());

        Assertions.assertEquals(2, result.size());
        verify(mapper, times(2)).toDto(any());
    }
}
