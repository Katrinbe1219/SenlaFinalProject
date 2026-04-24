package org.example.application.controllers.system;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.example.application.dto.getting.StringResponse;
import org.example.application.dto.getting.rates.RateFullDto;
import org.example.application.dto.getting.rates.RateInTimeDto;
import org.example.application.dto.getting.rates.RateValidGoodDto;
import org.example.application.dto.getting.rates.RateWithGoodNameDto;
import org.example.application.exceptions.DoesNoeExist;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.hibernate.base_settings.filters.rates.RatesFilter;
import org.example.application.hibernate.base_settings.filters.rates.RatingRecalcFilter;
import org.example.application.services.RecalculationService;
import org.example.application.services.documents.RateService;
import org.example.application.services.graphics.GraphicalAnalyseService;
import org.springframework.http.MediaType;
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
        return rateService.getTopRatesAmongAll(count, withSuspicious);
    }

    @GetMapping("/goods/{id}/top")
    public List<RateValidGoodDto> getTopValidRatesAmongGood(
            @PathVariable("id") Long id,
            @RequestParam(value = "count", defaultValue = "10") int count
    ){
        if (count <=0){
            throw new NotCorrectInput("Count must be greater than 0");
        }
        return rateService.getTopRatesAmongProduct(count, id);
    }

    @GetMapping("/goods/{id}")
    public List<RateFullDto> getGoodRatesByFilter(@PathVariable("id") Long id,
                                                  @RequestBody RatingRecalcFilter filters
  ){
        if (filters.getCategoryId() != null || filters.getCategoryIds() != null){
            throw new NotCorrectInput("Категория не может быть введена");
        }
        if (filters.getGoodsIds()!= null){
            throw new NotCorrectInput("Множество продуктов не может быть указано");
        }

        if (filters.getGoodId() != null && !filters.getGoodId().equals(id)){
            throw new  NotCorrectInput("Продукт не может указываться в body");
        }



        filters.setGoodId(id);
        return rateService.getGoodRatesByFilter(filters);
    }

    @GetMapping(value = "/goods/{id}/graph", produces = MediaType.IMAGE_PNG_VALUE)
    public void getGoodRatesInTime(HttpServletResponse response,
                                   @RequestBody RatesFilter filters,
                                   @PathVariable("id") Long id) throws Exception{
        if (filters.getFirstDate() == null || filters.getLastDate() == null ){
            throw new NotCorrectInput("firstDate and lastDate must be given");
        }
        filters.setGoodId(id);
        List<RateInTimeDto> rates = rateService.getGoodRateInTime(filters);
        if (rates.isEmpty()){
            throw new DoesNoeExist("Rates are empty, no image can be generated");
        }
        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"goods_rates.png\"");

        graphicalService.generateImeSeriesForGoodRateInTime(
                response.getOutputStream(), "Good " + id + " Rate", "Date", "Rate", rates,
                filters
        );
        response.getOutputStream().flush();

    }

    @GetMapping("/goods")
    public List<RateFullDto> getAllGoodsRatesByFilter(
                  @RequestBody RatingRecalcFilter filters
    ){
        if (filters.getGoodId() != null && filters.getGoodsIds() != null){
            throw new NotCorrectInput("Either one good id or several");
        }

        if (filters.getGoodId()!= null && (filters.getCategoryId()!= null || filters.getCategoryIds() != null)){
            throw new NotCorrectInput("For one good no category can be given");
        }

        return rateService.getGoodRatesByFilter(filters);
    }



    // recalculation rating--------------------------------------
    // TODO only for admin
    @GetMapping("/recalculation")
    public StringResponse recalculateRating(

    ){
        return recalculationService.moderatorRequest(null);
    }

    @GetMapping("/recalculation/{id}")
    public StringResponse recalculateRatingById(@PathVariable("id") Long id){
        return recalculationService.moderatorRequest(id);
    }




}
