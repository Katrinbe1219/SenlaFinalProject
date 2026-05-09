package org.example.core.services.documents;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.rates.RateFullDto;
import org.example.core.dto.getting.rates.RateInTimeDto;
import org.example.core.dto.getting.rates.RateValidGoodDto;
import org.example.core.dto.getting.rates.RateWithGoodNameDto;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.rates.RatesFilter;
import org.example.core.hibernate.base_settings.filters.rates.RatingRecalcFilter;
import org.example.core.hibernate.base_settings.service_dto.RateExportDto;
import org.example.core.hibernate.dictionaries.CategoryHibImpl;
import org.example.core.hibernate.documents.RateHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.models.Good;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RateService {
    private final static Logger logger= LogManager.getLogger(RateService.class);
    private final CategoryHibImpl categoryHibImpl;
    private GoodHibImpl goodHib;
    private RateHibImpl rateHib;

    public RateService(GoodHibImpl goodHib, RateHibImpl rateHib,
                       CategoryHibImpl categoryHibImpl) {
        this.goodHib = goodHib;
        this.rateHib = rateHib;
        this.categoryHibImpl = categoryHibImpl;
    }

    @Transactional
    public List<RateWithGoodNameDto> getTopRatesAmongAll(int count, boolean withSuspicious){
        try{
            return goodHib.findMaxRatesAmongAll(count, withSuspicious);
        }catch (Exception e){
            logger.error("RateService getTopRatesAmongAll: " + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public List<RateValidGoodDto> getTopRatesAmongProduct(int count, Long productId){
        try{
            return rateHib.findValidMaxRatesAmongProduct(count, productId);
        }catch (Exception e){
            logger.error("RateService getTopRatesAmongProduct: " + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public List<RateFullDto> getGoodRatesByFilter(RatingRecalcFilter filters){
        try{
            if(filters.getCategoryIds()!= null){
                List<Long> allCategories = categoryHibImpl.findAllChildCategoryIds(filters.getCategoryIds());
                filters.setCategoryIds(allCategories);
            }else if(filters.getCategoryId()!= null){
                List<Long> allCategories = categoryHibImpl.findAllChildCategoryIds(List.of(filters.getCategoryId()));
                filters.setCategoryIds(allCategories);
                filters.setCategoryId(null);
                //TODO удалить взм тогда проверку в buildPredicates
            }

            return rateHib.getRatesByFilter(filters);
        }catch (Exception e){
            logger.error("RateService getGoodRatesByFilter: " + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public List<RateExportDto> getRatesExportByFilter(RatingRecalcFilter filters){
        try{
            return rateHib.getRatesForExport(filters);
        }catch (Exception e){
            logger.error("RateService getRatesExportByFilter: " + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public List<RateInTimeDto> getGoodRateInTime(RatesFilter filters){
        try{
            return rateHib.getGoodRateInTime(filters);
        }catch (Exception e){
            logger.error("RateService getGoodRateInTime: " + e.getMessage());
            throw e;
        }

    }


}
