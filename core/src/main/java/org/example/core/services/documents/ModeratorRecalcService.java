package org.example.core.services.documents;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.ModeratorRecalcDto;
import org.example.core.exceptions.DoesNoeExist;
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
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class ModeratorRecalcService {
    private static final Logger logger = LogManager.getLogger(ModeratorRecalcService.class);
    private ModeratorRecalcHib recalcHib;
    private GoodHibImpl goodHib;
    private ModeratorRecalcMapper mapper;

    @Transactional
    public void addLog(User user, Long goodId, ModeratorVerdict verdict, String comment){
        Good good = goodHib.findById(goodId, logger);
        if (good == null){
            throw new DoesNoeExist("Good does not exist with given credentials");
        }

        if (Objects.requireNonNull(verdict) == ModeratorVerdict.SUSPICIOUS && good.getModeratorStatus() ==GoodStatusFromModerator.SUSPICIOUS) {
            throw new NotCorrectInput("It is already blocked");
        }

        if ((verdict == ModeratorVerdict.APPROVED || verdict == ModeratorVerdict.RECALCULATED) &&
        good.getModeratorStatus() == GoodStatusFromModerator.APPROVED) {
            throw new NotCorrectInput("It is already unblocked");
        }


        ModeratorRatingCheck log = new ModeratorRatingCheck();
        log.setModerator(user);
        log.setGood(good);
        log.setVerdict(verdict);
        log.setComment(comment);
        log.setCheckAt(Instant.now());

        recalcHib.save(log, logger);

        if (Objects.requireNonNull(verdict) == ModeratorVerdict.SUSPICIOUS) {
            good.setModeratorStatus(GoodStatusFromModerator.SUSPICIOUS);
        } else {
            good.setModeratorStatus(GoodStatusFromModerator.APPROVED);
        }


    }

    @Transactional
    public List<ModeratorRecalcDto> findAllFullVersion(ModeratorRecalcFilter filters){
        List<ModeratorRatingCheck> ratings = recalcHib.findAllFullVersion(filters);
        if (ratings.isEmpty()){
            return List.of();
        }

        return ratings.stream().map(mapper::toDto).toList();
    }
}
