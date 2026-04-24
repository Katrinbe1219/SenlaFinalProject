package org.example.core.services;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.StringResponse;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.exceptions.UnavailableExecution;
import org.example.core.hibernate.documents.prices.PriceForCalculationHibImpl;
import org.example.core.hibernate.documents.RateHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.models.User;
import org.example.core.models.types.ModeratorVerdict;
import org.example.core.models.types.RatingTriggerType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RecalculationService {



    private static final Logger logger = LogManager.getLogger(RecalculationService.class);
    private GoodHibImpl goodHib;
    private PriceForCalculationHibImpl priceHib;
    private RateHibImpl rateHib;
    private AsyncRecalculationService asyncRecalculationService;

    private final AtomicBoolean isRecalculating = new AtomicBoolean(false);

    public RecalculationService(GoodHibImpl goodHib, PriceForCalculationHibImpl priceHib, RateHibImpl rateHib,
                                AsyncRecalculationService asyncRecalculationService) {
        this.goodHib = goodHib;
        this.priceHib = priceHib;
        this.rateHib = rateHib;
        this.asyncRecalculationService = asyncRecalculationService;
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
    public StringResponse moderatorRequest(Long goodId){
        if (isRecalculating.get()){
            return new StringResponse("Пересчет уже выполняется, попробуйте позже");
        }
        if (!isRecalculating.compareAndSet(false, true)) {
            logger.warn("Пересчет уже выполняется, отказ");
            return new StringResponse("Пересчет уже выполняется, попробуйте позже");

        }else{

                LocalDateTime targetTime = LocalDate.now().plusDays(1).atStartOfDay().plusHours(3);
                LocalDateTime currentTime = LocalDateTime.now();
                //TODO switch to admin
                if (goodId == null ){
                    if (ChronoUnit.HOURS.between(currentTime, targetTime) < 3){
                        return new StringResponse("Время пересчета ограничено для всех продуктов, попробуйте позже");
                    }
                    asyncRecalculationService.recalculationForAll(RatingTriggerType.MODERATOR, isRecalculating);
                }else{

                    if (ChronoUnit.MINUTES.between(currentTime, targetTime) < 5){
                        return new StringResponse("Время пересчета ограничено для всех продуктов, попробуйте позже");
                    }
                    try{
                        asyncRecalculationService.recalculationForGood(goodId, isRecalculating);

                    }catch (Exception e){
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
