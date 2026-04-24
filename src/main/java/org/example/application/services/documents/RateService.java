package org.example.application.services.documents;

import jakarta.transaction.Transactional;
import org.example.application.dto.getting.rates.RateFullDto;
import org.example.application.dto.getting.rates.RateInTimeDto;
import org.example.application.dto.getting.rates.RateValidGoodDto;
import org.example.application.dto.getting.rates.RateWithGoodNameDto;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.hibernate.base_settings.filters.rates.RatesFilter;
import org.example.application.hibernate.base_settings.filters.rates.RatingRecalcFilter;
import org.example.application.hibernate.base_settings.service_dto.RateExportDto;
import org.example.application.hibernate.documents.RateHibImpl;
import org.example.application.hibernate.objects.GoodHibImpl;
import org.example.application.models.Good;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RateService {
    GoodHibImpl goodHib;
    RateHibImpl rateHib;

    public RateService(GoodHibImpl goodHib, RateHibImpl rateHib) {
        this.goodHib = goodHib;
        this.rateHib = rateHib;
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
        if ((filters.getCurDate() != null && filters.getMinDate()!= null ) ||
                ( filters.getCurDate() != null && filters.getMaxDate() != null)
        ){
            throw new NotCorrectInput("Должна быть либо точная дата, либо диапазон");
        }

        if (   (filters.getCurRate() != null && filters.getMinRate() != null) ||
                ( filters.getCurRate() != null && filters.getMaxRate() != null)
            ){
            throw new NotCorrectInput("Должна быть либо точная рейтинг, либо диапазон");
        }

        return rateHib.getRatesByFilter(filters);
    }

    @Transactional
    public List<RateExportDto> getRatesExportByFilter(RatingRecalcFilter filters){
        if ((filters.getCurDate() != null && filters.getMinDate()!= null ) ||
                ( filters.getCurDate() != null && filters.getMaxDate() != null)
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
