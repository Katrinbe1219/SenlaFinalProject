package org.example.core.controllers.system;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.getting.rates.RateFullDto;
import org.example.core.dto.getting.rates.RateInTimeDto;
import org.example.core.dto.getting.rates.RateValidGoodDto;
import org.example.core.dto.getting.rates.RateWithGoodNameDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.hibernate.base_settings.filters.rates.RatesFilter;
import org.example.core.hibernate.base_settings.filters.rates.RatingRecalcFilter;
import org.example.core.services.RecalculationService;
import org.example.core.services.documents.RateService;
import org.example.core.services.graphics.GraphicalAnalyseService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rates")
@AllArgsConstructor
public class RateController {
    RateService rateService;
    RecalculationService recalculationService;
    GraphicalAnalyseService graphicalService;




    @GetMapping("/top")
    public List<RateWithGoodNameDto> getTopRatesAmongAll(
            @RequestParam(value = "num", defaultValue = "10", required = false) int count,
            @RequestParam(value="withSuspicious", defaultValue = "true", required = false) boolean withSuspicious){

        if (count <=0){
            throw new NotCorrectInput("num request param must be  > 0");
        }
        return rateService.getTopRatesAmongAll(count, withSuspicious);
    }

    @GetMapping("/goods/{id}/top")
    public List<RateValidGoodDto> getTopValidRatesAmongGood(
            @PathVariable("id") Long id,
            @RequestParam(value = "count", defaultValue = "10") int count
    ){
        if (count <=0){
            throw new NotCorrectInput("Count must be > 0");
        }
        if (id <=0){
            throw new NotCorrectInput("id must be > 0");
        }
        return rateService.getTopRatesAmongProduct(count, id);
    }

    @GetMapping("/good")
    public List<RateFullDto> getGoodRatesByFilter(
            @Valid @RequestBody RatingRecalcFilter filters
  ){

        if (filters.getGoodId() == null){
            throw new  NotCorrectInput("goodId must be given");
        }

        if (filters.getTagIds()!= null || filters.getTagId()!= null){
            throw new NotCorrectInput("tag can not be given");
        }


        return rateService.getGoodRatesByFilter(filters);
    }

    @GetMapping(value = "/good/graph", produces = MediaType.IMAGE_PNG_VALUE)
    public void getGoodRatesInTime(HttpServletResponse response,
                                   @Valid @RequestBody RatesFilter filters) throws Exception{


        List<RateInTimeDto> rates = rateService.getGoodRateInTime(filters);
        if (rates.isEmpty()){
            throw new DoesNoeExist("Rates are empty, no image can be generated");
        }
        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"goods_rates.png\"");

        graphicalService.generateImeSeriesForGoodRateInTime(
                response.getOutputStream(), "Good " + filters.getGoodId() + " Rate", "Date", "Rate", rates,
                filters
        );
        response.getOutputStream().flush();

    }

    @GetMapping("/goods")
    public List<RateFullDto> getAllGoodsRatesByFilter(
                  @Valid @RequestBody RatingRecalcFilter filters
    ){

        return rateService.getGoodRatesByFilter(filters);
    }



    // recalculation rating--------------------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/recalculation")
    public StringResponse recalculateRating(
    ){
        return recalculationService.personRequest(null);
    }

    @GetMapping("/recalculation/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public StringResponse recalculateRatingById(@PathVariable("id") Long id){
        if (id==null || id <=0){
            throw new NotCorrectInput("id must be > 0");
        }
        return recalculationService.personRequest(id);
    }




}
