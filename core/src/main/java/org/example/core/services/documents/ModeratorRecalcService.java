package org.example.core.services.documents;


import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.ManyModeratorLogCreateDto;
import org.example.core.dto.creating.ModeratorLogCreateDto;
import org.example.core.dto.getting.ModeratorRecalcDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.ManyIncorrectInputsException;
import org.example.core.exceptions.NonHibernateException;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
    public void addManyLogs(ManyModeratorLogCreateDto dto, User user){
        try{

            Map<Long, ModeratorVerdict> goodsUpdate = dto.getVerdicts().stream().collect(Collectors.toMap(
                    ModeratorLogCreateDto::getGoodId,
                    s -> {
                        if (s.getVerdict() == null) {
                            throw new NotCorrectInput("Verdict cannot be null for goodId: " + s.getGoodId());
                        }
                        return s.getVerdict();
                    },
                    (old, n) -> n
            ));

            List<ModeratorRatingCheck> lastLogs = recalcHib.getModeratorRatingChecksByGoodIds(goodsUpdate.keySet());
            if (lastLogs.isEmpty()){
                throw new NotCorrectInput("Goods's ids are not valid");
            }

            if (lastLogs.size() != goodsUpdate.size()){
                throw new NotCorrectInput("Goods's ids are not valid");
            }


            Map<Long, ModeratorVerdict> lastVerdicts = lastLogs.stream()
                    .collect(Collectors.toMap(
                            log -> log.getGood().getId(),
                            ModeratorRatingCheck::getVerdict
                    ));

            List<String> errors = new ArrayList<>();
            for (Long id : goodsUpdate.keySet()) {
                ModeratorVerdict last = lastVerdicts.get(id);
                ModeratorVerdict incoming = goodsUpdate.get(id);

                if (last != null && last == incoming) {
                    errors.add("Good " + id + " already has status " + incoming);
                }
            }


            if (!errors.isEmpty()){
                throw new ManyIncorrectInputsException( errors);
            }

            recalcHib.createManyLogs(dto, user);
            goodHib.updateStatusForMany(goodsUpdate);
        }catch(Exception e){
            logger.error("ModeratorRecalcService addManyLogs:" + e.getMessage());
            throw e;
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
