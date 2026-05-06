package org.example.core.services.documents;

import jakarta.transaction.Transactional;
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
    private final CategoryHibImpl categoryHibImpl;
    GoodHibImpl goodHib;
    RateHibImpl rateHib;

    public RateService(GoodHibImpl goodHib, RateHibImpl rateHib, CategoryHibImpl categoryHibImpl) {
        this.goodHib = goodHib;
        this.rateHib = rateHib;
        this.categoryHibImpl = categoryHibImpl;
    }

    @Transactional
    public List<RateWithGoodNameDto> getTopRatesAmongAll(int count, boolean withSuspicious){
        return goodHib.findMaxRatesAmongAll(count, withSuspicious);
    }

    @Transactional
    public List<RateValidGoodDto> getTopRatesAmongProduct(int count, Long productId){
        return rateHib.findValidMaxRatesAmongProduct(count, productId);
    }

    @Transactional
    public List<RateFullDto> getGoodRatesByFilter(RatingRecalcFilter filters){

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
    }

    @Transactional
    public List<RateExportDto> getRatesExportByFilter(RatingRecalcFilter filters){
        if ((filters.getCurDate() != null && filters.getStartDate()!= null ) ||
                ( filters.getCurDate() != null && filters.getEndDate() != null)
        ){
            throw new NotCorrectInput("Должна быть либо точная дата, либо диапазон");
        }

        if (   (filters.getCurRate() != null && filters.getMinRate() != null) ||
                ( filters.getCurRate() != null && filters.getMaxRate() != null)
        ){
            throw new NotCorrectInput("Должна быть либо точная рейтинг, либо диапазон");
        }

        return rateHib.getRatesForExport(filters);
    }

    @Transactional
    public List<RateInTimeDto> getGoodRateInTime(RatesFilter filters){
        return rateHib.getGoodRateInTime(filters);
    }

    private RateWithGoodNameDto toDto(Good good){
        RateWithGoodNameDto dto = new RateWithGoodNameDto();
        dto.setRate(good.getRate());
        dto.setGoodName(good.getName());
        return dto;
    }

    private List<RateWithGoodNameDto> listToDto(List<Good> goods){
        List<RateWithGoodNameDto> dtos = new ArrayList<RateWithGoodNameDto>();
        for (Good good : goods) {
            dtos.add(toDto(good));
        }

        return dtos;
    }
}
