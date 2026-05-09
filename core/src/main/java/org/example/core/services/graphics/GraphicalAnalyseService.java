package org.example.core.services.graphics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.goods.GoodPriceInShop;
import org.example.core.dto.getting.prices.PriceInTime;
import org.example.core.dto.getting.rates.RateInTimeDto;
import org.example.core.dto.getting.statistics.DistrictStatisticDto;
import org.example.core.dto.getting.statistics.categories.CategoryStatDto;
import org.example.core.dto.getting.statistics.shops.ShopCartDto;
import org.example.core.hibernate.base_settings.filters.prices.PriceInTimeFilter;
import org.example.core.hibernate.base_settings.filters.rates.RatesFilter;
import org.example.core.utils.DateTimeUtils;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CancellationException;

@Service
public class GraphicalAnalyseService {
    private  static final Logger logger = LogManager.getLogger(GraphicalAnalyseService.class);
    private GraphicService graphicService;
    public GraphicalAnalyseService(GraphicService graphicService) {
        this.graphicService = graphicService;
    }

    // bar x:shops y:price
    public void generateBarForGoodPriceInShops(
            OutputStream outputStream,
            String title, String xLabel,
            String yLabel,
            List<GoodPriceInShop> goodsWithAnalyze
    ) throws Exception {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (GoodPriceInShop good : goodsWithAnalyze) {
            dataset.addValue(good.getPrice(), yLabel,  good.getShopId());
        }


        graphicService.generateBar(outputStream, title, xLabel, yLabel, dataset);
    }

    // done
    public void generateImeSeriesForGoodRateInTime(
            OutputStream outputStream,
            String title, String xLabel,
            String yLabel,
            List<RateInTimeDto> rates,
            RatesFilter filters
    ){

        try{
            TimeSeriesCollection dataset = new TimeSeriesCollection();
            TimeSeries series = new TimeSeries(title);

            int index = 0;

            LocalDate currentDate = filters.getStartDate();
            if (currentDate.isBefore(
                    DateTimeUtils.toLocalDate(rates.get(0).getCreatedAt())
            )){
                while(!currentDate.equals(
                        DateTimeUtils.toLocalDate(rates.get(0).getCreatedAt())
                )){
                    series.add(new Second(Date.valueOf(currentDate)), 0d);
                    currentDate = currentDate.plusDays(1);
                }
            }

            while(!currentDate.isAfter(filters.getEndDate())){
                if (index >= rates.size()){
                    series.add(new Second(Date.valueOf(currentDate)), rates.get(index-1).getRate());
                    currentDate = currentDate.plusDays(1);
                    continue;
                }

                if (checkDate(rates,currentDate,  index)){
                    series.add(new Second(Date.valueOf(currentDate)), rates.get(index).getRate());
                    currentDate = currentDate.plusDays(1);
                }else{
                    index ++;
                }
            }

            dataset.addSeries(series);
            graphicService.generateTimeSeries(outputStream, title, xLabel, yLabel,
                    dataset,0,5,1d);
        }catch (Exception e){
            logger.error("GraphicalAnalyseService generateImeSeriesForGoodRateInTime ", e.getMessage());
            throw new CancellationException(e.getMessage());
        }

    }

    //bar x: shops y:price - done
    public void generateBarForCartPriceInShops(
            OutputStream out, String title, String xLabel,
            String yLabel,
            List<ShopCartDto> info

    )  {

        try{
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (ShopCartDto shopCart : info) {
                dataset.addValue(shopCart.getTotalPrice(), yLabel, shopCart.getShopName() + " " + shopCart.getShopId());
            }

            graphicService.generateBar(out, title, xLabel, yLabel, dataset);
        }catch (Exception e){
            logger.error("GraphicalAnalyseService generateBarForCartPriceInShops ", e.getMessage());
            throw new CancellationException(e.getMessage());
        }


    }

    // time series x:date y:price - done
    public void generateTimeSeriesForGoodPriceInShopTime(
            OutputStream outputStream,
            String title, String xLabel,
            String yLabel,
            List<PriceInTime> prices,
            PriceInTimeFilter filters
    )  {
        try{
            TimeSeriesCollection dataset = new TimeSeriesCollection();
            TimeSeries series = new TimeSeries(title);


            LocalDate currentDate = filters.getStartDate();
            int index = 0;
            LocalDate lastDate = filters.getEndDate()!=null ? filters.getEndDate() : LocalDate.now();

            if (currentDate.isBefore(DateTimeUtils.toLocalDate(prices.get(0).getValidFrom()))){
                while(!currentDate.equals(DateTimeUtils.toLocalDate(prices.get(0).getValidFrom()))){
                    series.add(new Second(Date.valueOf(currentDate)), 0d);
                    currentDate = currentDate.plusDays(1);
                };
            }

            while(!currentDate.isAfter(lastDate)){
                if (index >= prices.size()) {
                    series.add(new Second(Date.valueOf(currentDate)), prices.get(index-1).getPrice());
                    currentDate = currentDate.plusDays(1);
                    continue;
                }

                if (checkDate(currentDate, prices, index)){
                    series.add(
                            new Second(Date.valueOf(currentDate)), prices.get(index).getPrice());
                    currentDate = currentDate.plusDays(1);
                }else{
                    index++;
                }
            }

            dataset.addSeries(series);
            graphicService.generateTimeSeries(
                    outputStream, title, xLabel, yLabel, dataset,
                    null,null,null
            );
        }catch (Exception e){
            logger.error("GraphicalAnalyseService generateTimeSeriesForGoodPriceInShopTime ", e.getMessage());
            throw new CancellationException(e.getMessage());
        }



    }

    private boolean checkDate(
            List<RateInTimeDto> prices,
            LocalDate currentDate,
            int index
    ){
        try{
            LocalDate validFrom = DateTimeUtils.toLocalDate(prices.get(index).getCreatedAt());

            LocalDate validTo = prices.get(index).getCreatedAt() != null ?
                    DateTimeUtils.toLocalDate(prices.get(index).getCreatedAt())
                    : LocalDate.now();

            return !currentDate.isBefore(validFrom) && !currentDate.isAfter(validTo);
        }catch (Exception e){
            logger.error("GraphicalAnalyseService checkDate ", e.getMessage());
            throw new CancellationException(e.getMessage());
        }

    }

    private boolean checkDate(
            LocalDate currentDate,
            List<PriceInTime> prices,
            int index
    ){
        try{
            LocalDate validFrom = DateTimeUtils.toLocalDate(prices.get(index).getValidFrom());

            LocalDate validTo = prices.get(index).getValidTo() != null ?
                    DateTimeUtils.toLocalDate(prices.get(index).getValidTo())
                    : LocalDate.now();

            return !currentDate.isBefore(validFrom) && !currentDate.isAfter(validTo);
        }catch (Exception e){
            logger.error("GraphicalAnalyseService checkDate ", e.getMessage());
            throw new CancellationException(e.getMessage());
        }

    }

    // bar x: category y:rate
    public void generateBarForAverageCategoriesRate(
            OutputStream out, String title, String xAxisLabel,
            String yAxisLabel,
            List<CategoryStatDto> info
    )  {
        try{
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (CategoryStatDto category : info) {
                dataset.addValue(category.getAvgPrice(), yAxisLabel, category.getCategoryId());
            }

            graphicService.generateBar(out, title, xAxisLabel, yAxisLabel, dataset);
        }catch (Exception e){
            logger.error("GraphicalAnalyseService generateBarForAverageCategoriesRate ", e.getMessage());
            throw new CancellationException(e.getMessage());
        }


    }

    public void generateBarForAverageCategoriesInDistrict(
            OutputStream out, String title, String xAxisLabel,
            String yAxisLabel,
            List<DistrictStatisticDto> info
    ) {
        try{
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (DistrictStatisticDto category : info) {
                dataset.addValue(category.getAvgPrice(), yAxisLabel, category.getCategoryId());
            }

            graphicService.generateBar(out, title, xAxisLabel, yAxisLabel, dataset);
        }catch (Exception e){
            logger.error("GraphicalAnalyseService generateBarForAverageCategoriesInDistrict ", e.getMessage());
            throw new CancellationException(e.getMessage());
        }


    }

    public void generateBarForAverageCategoryInDistricts(
            OutputStream out, String title, String xAxisLabel,
            String yAxisLabel,
            List<DistrictStatisticDto> info
    )  {
        try{
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (DistrictStatisticDto category : info) {
                dataset.addValue(category.getAvgPrice(), yAxisLabel, category.getDistrictId());
            }

            graphicService.generateBar(out, title, xAxisLabel, yAxisLabel, dataset);
        }catch (Exception e){
            logger.error("GraphicalAnalyseService generateBarForAverageCategoryInDistricts ", e.getMessage());
            throw new CancellationException(e.getMessage());
        }



    }



}
