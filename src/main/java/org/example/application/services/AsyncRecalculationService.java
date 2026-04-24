package org.example.application.services;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.application.dto.getting.statistics.RecalculationForGoodDto;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.hibernate.documents.prices.PriceForCalculationHibImpl;
import org.example.application.hibernate.documents.RateHibImpl;
import org.example.application.hibernate.objects.GoodHibImpl;
import org.example.application.models.Good;
import org.example.application.models.types.RatingStatus;
import org.example.application.models.types.RatingTriggerType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class AsyncRecalculationService {
    private static  final Logger logger= LogManager.getLogger(AsyncRecalculationService.class);

    private GoodHibImpl goodHib;
    private PriceForCalculationHibImpl priceHib;
    private RateHibImpl rateHib;

    public AsyncRecalculationService(GoodHibImpl goodHib, PriceForCalculationHibImpl priceHib, RateHibImpl rateHib) {
        this.goodHib = goodHib;
        this.priceHib = priceHib;
        this.rateHib = rateHib;
    }


    @Transactional
    @Async("asyncExecutor")
    public void recalculationForAll(RatingTriggerType type, AtomicBoolean isRecalculating){
        int fetchMaxAttempt = 3;
        int fetchAttempt = 0;
        while(fetchAttempt < fetchMaxAttempt){
            try{

                List<RecalculationForGoodDto> goodIds = goodHib.getAllIdsForRecalculation();
                logger.info(" RecalculationService recalculationForAll: Получены идентификаторы id, oldRates для пересчета");

                int batchSize = 100;

                for (int i=0; i<goodIds.size(); i+= batchSize){
                    int end  = Math.min(i+batchSize, goodIds.size());

                    int batchAttempt = 0;
                    int batchAttemptMax = 3;


                    Map<Long, Double> oldInfo = goodIds.subList(
                                    i, end
                            ).stream()
                            .collect(Collectors.toMap(
                                    RecalculationForGoodDto::getGoodId,
                                    RecalculationForGoodDto::getRate
                            ));
                    List<Long> batchIds = oldInfo.keySet().stream().toList();

                    while(batchAttempt < batchAttemptMax){

                        logger.info("Заход по логгированию пошел {}-{}; попытка {}", i,end , batchAttempt );
                        try {
                            // TODO remove
//                            if (batchAttempt <10){
//                                throw new Exception("thrown exception during first attemot==pt");
//                            }
                            Map<Long, Good> newRates = priceHib.recalculateForAllGoods(batchIds);
                            rateHib.saveLogs(newRates, null,
                                    RatingStatus.SUCCESS, type, oldInfo);

                            logger.info("Заход был успешный {}-{}, попытка {}", i, end, batchAttempt );

                            break;
                        } catch (Exception e) {
                            batchAttempt ++;
                            logger.error("Заход был неуспешный {}-{}, попытка {}", i, end, batchAttempt);
                            if (batchAttempt == batchAttemptMax){


                                Map<Long, Good> info = goodIds.subList(
                                        i, end
                                ).stream().collect(Collectors.toMap(
                                        RecalculationForGoodDto::getGoodId,
                                        t -> {
                                            Good g = new Good();
                                            g.setId(t.getGoodId());
                                            g.setRate(t.getRate());
                                            return g;
                                        }
                                ));
                                rateHib.saveErrors(info, e.getMessage(), RatingStatus.FAILED, type);
                            }
                        }

                    }
                }
                return;



            }catch(Exception e){
                fetchAttempt ++;
                logger.warn("Попытка {} загрузить продукты была провалена получить продукты" , fetchAttempt);
                if (fetchAttempt == fetchMaxAttempt){
                    logger.error("Логгирование не началось из-за невозможности получить продукты" );
                    // TODO оповещение админу и взм модератору
                    return;
                }
            }finally {
                isRecalculating.set(false);
            }
        }

    }


    @Transactional
//    @Async("asyncExecutor")
    public void recalculationForGood(Long goodId, AtomicBoolean isRecalculating
    ) throws  Exception{
        try {
            Good good = goodHib.findById(goodId, logger);
            if (good == null){
                logger.error("Good with id " + goodId + " not found");
                throw new NotCorrectInput("Good with id " + goodId + " not found");

            }

            Double oldRate = good.getRate();
            try {
                Double newRate = priceHib.recalculateForGood(goodId);
                rateHib.saveLog(good, null, RatingStatus.SUCCESS, RatingTriggerType.MODERATOR, oldRate, newRate);

            }catch (Exception e){
                Map<Long,Good> info = new HashMap<>();
                info.put(good.getId(), good);
                rateHib.saveErrors(info,e.getMessage(), RatingStatus.FAILED, RatingTriggerType.MODERATOR);
                throw e;
            }

        }
        finally {
            isRecalculating.set(false);
        }



    }
}
