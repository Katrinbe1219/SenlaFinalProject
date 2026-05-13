package org.example.core.services;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.StringResponse;
import org.example.core.exceptions.*;
import org.example.core.hibernate.documents.prices.PriceForCalculationHibImpl;
import org.example.core.hibernate.documents.RateHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.models.types.RatingTriggerType;
import org.example.core.models.types.RoleTypes;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RecalculationService {



    private static final Logger logger = LogManager.getLogger(RecalculationService.class);

    private Clock clock;
    private AsyncRecalculationService asyncRecalculationService;
    private final AtomicBoolean isRecalculating = new AtomicBoolean(false);

    public RecalculationService(
            AsyncRecalculationService asyncRecalculationService, Clock clock) {
        this.asyncRecalculationService = asyncRecalculationService;
        this.clock = clock;
    }

    //@Scheduled(cron = "0 * * * * *")
    @Scheduled(cron = "0 0 3  * * *")
    @Transactional
    public void recalculationScheduled(){
        logger.info("Recalculation started {}", LocalDateTime.now());
        recalculation( RatingTriggerType.SCHEDULED);
        logger.info("Recalculation finished {}", LocalDateTime.now());
    }

    @Transactional
    public StringResponse personRequest(Long goodId, RoleTypes role){
        if (isRecalculating.get()){
            return new StringResponse("Пересчет уже выполняется, попробуйте позже");
        }
        if (!isRecalculating.compareAndSet(false, true)) {
            logger.warn("Пересчет уже выполняется, отказ");
            return new StringResponse("Пересчет уже выполняется, попробуйте позже");

        }else{

                LocalDateTime targetTime = LocalDate.now(clock).atStartOfDay().plusHours(3);
                LocalDateTime currentTime = LocalDateTime.now(clock);
                if (targetTime.isBefore(currentTime)) {
                    targetTime = targetTime.plusDays(1);
                }
                if (goodId == null  && role == RoleTypes.ADMIN){
                    if (ChronoUnit.HOURS.between(currentTime, targetTime) < 3){
                        return new StringResponse("Время пересчета ограничено для всех продуктов, попробуйте позже");
                    }
                    asyncRecalculationService.recalculationForAll(RatingTriggerType.ADMIN, isRecalculating);
                }else if (goodId == null  && role != RoleTypes.ADMIN){
                    throw new PermissionDenied("You are not allowed to recalculate all");
                }
                else{

                    if (ChronoUnit.MINUTES.between(currentTime, targetTime) < 6){
                        return new StringResponse("Время пересчета ограничено для всех продуктов, попробуйте позже");
                    }
                    try{
                        asyncRecalculationService.recalculationForGood(goodId, isRecalculating, role);

                    }catch (PermissionDenied | DoesNoeExist | NotCorrectInput e){
                        throw e;
                    }
                    catch (Exception e){
                        throw new NonHibernateException("RecalculationService moderatorRequest " + e.getMessage());
                    }
                }

            return new StringResponse("Успешный пересчет");

        }
    }

    @Transactional
    public void recalculation(RatingTriggerType triggeredBy){
        if (!isRecalculating.compareAndSet(false, true)) {
            logger.warn("RecalculationService recalculation Пересчет уже выполняется, отказ");
            throw new UnavailableExecution("Пересчет уже выполняется, попробуйте позже");
        }

        asyncRecalculationService.recalculationForAll(triggeredBy, isRecalculating);

    }


}
