package org.example.core.services.graphics;

import io.jsonwebtoken.lang.Assert;
import org.example.core.dto.getting.goods.GoodPriceInShop;
import org.example.core.dto.getting.prices.PriceInTime;
import org.example.core.dto.getting.rates.RateInTimeDto;
import org.example.core.dto.getting.statistics.DistrictStatisticDto;
import org.example.core.dto.getting.statistics.categories.CategoryStatDto;
import org.example.core.dto.getting.statistics.shops.ShopCartDto;
import org.example.core.hibernate.base_settings.filters.prices.PriceInTimeFilter;
import org.example.core.hibernate.base_settings.filters.rates.RatesFilter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeriesCollection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class GraphicalAnalyseServiceTest {
    @Mock
    GraphicService graphicService;

    @InjectMocks
    GraphicalAnalyseService service;

    @Test
    @DisplayName("generateBarForGoodPriceInShopsDefault")
    void generateBarForGoodPriceInShopsDefault() throws Exception {
        GoodPriceInShop dto = new GoodPriceInShop();
        dto.setPrice(16d);
        dto.setShopId(1l);

        GoodPriceInShop dto1 = new GoodPriceInShop();
        dto1.setPrice(13d);
        dto1.setShopId(2l);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.generateBarForGoodPriceInShops(
                out, "title", "X", "Y", List.of(dto, dto1)
        );

        ArgumentCaptor<DefaultCategoryDataset> captor = ArgumentCaptor.forClass(DefaultCategoryDataset.class);


        verify(graphicService).generateBar(
                eq(out), eq("title"), eq("X"), eq("Y"),
                captor.capture()
        );

        DefaultCategoryDataset capture = captor.getValue();
        Assertions.assertEquals(13d, capture.getValue("Y",2l));
        Assertions.assertEquals(16d, capture.getValue("Y",1l));



    }

    @Test
    @DisplayName("generateImeSeriesForGoodRateInTimeDefault")
    void generateImeSeriesForGoodRateInTimeDefault() throws Exception{
        OutputStream out = new ByteArrayOutputStream();
        RateInTimeDto dto = new RateInTimeDto();
        dto.setRate(2d);
        dto.setCreatedAt(Instant.now());

        RateInTimeDto dto1 = new RateInTimeDto();
        dto1.setRate(3d);
        dto1.setCreatedAt(Instant.now().minus(1, ChronoUnit.DAYS));

        RatesFilter filters = new RatesFilter();
        filters.setStartDate(LocalDate.now().minusDays(2));
        filters.setEndDate(LocalDate.now().plusDays(1));


        service.generateImeSeriesForGoodRateInTime(
                out, "Title", "X", "Y", List.of(dto, dto1), filters
        );

        ArgumentCaptor<TimeSeriesCollection> captor = ArgumentCaptor.forClass(TimeSeriesCollection.class);
        verify(graphicService).generateTimeSeries(
                eq(out), eq("Title"), eq("X"), eq("Y"),
                captor.capture(), eq(0),eq(5),eq(1d)
        );

        TimeSeriesCollection capture = captor.getValue();
        Assertions.assertEquals(new Second(Date.valueOf(LocalDate.now().minusDays(2))), capture.getSeries(0).getTimePeriod(0));
        Assertions.assertEquals(0d, capture.getSeries(0).getValue(0));
        Assertions.assertEquals(3d, capture.getSeries(0).getValue(3));


    }

    @Test
    @DisplayName("generateBarForCartPriceInShopsDefault")
    void generateBarForCartPriceInShopsDefault() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        ShopCartDto dto = new ShopCartDto();
        dto.setShopId(1l);
        dto.setShopName("shop1");
        dto.setTotalPrice(BigDecimal.valueOf(123));
        dto.setOutOfStockGoods(null);

        ShopCartDto dto1 = new ShopCartDto();
        dto1.setShopId(2l);
        dto1.setShopName("shop2");
        dto1.setTotalPrice(BigDecimal.valueOf(456));
        dto1.setOutOfStockGoods("milk");

        service.generateBarForCartPriceInShops(
                out, "Title", "X", "Y",
                List.of(dto, dto1)
        );

        ArgumentCaptor<DefaultCategoryDataset> captor = ArgumentCaptor.forClass(DefaultCategoryDataset.class);
        verify(graphicService).generateBar(
                eq(out), eq("Title"), eq("X"), eq("Y"), captor.capture()
        );

        DefaultCategoryDataset capture = captor.getValue();
        Assertions.assertEquals(dto1.getTotalPrice(), capture.getValue("Y",dto1.getShopName() + " " + dto1.getShopId()));
        Assertions.assertEquals(dto.getTotalPrice(), capture.getValue("Y",dto.getShopName() + " " + dto.getShopId()));



    }

    @Test
    @DisplayName("generateTimeSeriesForGoodPriceInShopTimeDefault")
    void generateTimeSeriesForGoodPriceInShopTimeDefault() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        PriceInTime dto = new PriceInTime();
        dto.setPrice(12d);
        dto.setValidFrom(Instant.now().minus(7, ChronoUnit.DAYS));
        dto.setValidTo(Instant.now().minus(5, ChronoUnit.DAYS));

        PriceInTime dto1 = new PriceInTime();
        dto1.setPrice(3d);
        dto1.setValidFrom(Instant.now().minus(4, ChronoUnit.DAYS));
        dto1.setValidTo(Instant.now().minus(2, ChronoUnit.DAYS));

        PriceInTime dto2 = new PriceInTime();
        dto2.setPrice(4d);
        dto2.setValidFrom(Instant.now().minus(1, ChronoUnit.DAYS));
        dto2.setValidTo(Instant.now());

        PriceInTimeFilter filters = new PriceInTimeFilter();
        filters.setStartDate(LocalDate.now().minusDays(6));
        filters.setEndDate(LocalDate.now());


        service.generateTimeSeriesForGoodPriceInShopTime(
                out, "Title", "X", "Y",
                List.of(dto, dto1, dto2),
                filters
        );

        ArgumentCaptor<TimeSeriesCollection> captor = ArgumentCaptor.forClass(TimeSeriesCollection.class);
        verify(graphicService).generateTimeSeries(
                eq(out), eq("Title"), eq("X"), eq("Y"),
                captor.capture(), eq(null), eq(null), eq(null)
        );

        TimeSeriesCollection capture = captor.getValue();
        Assertions.assertEquals(new Second(Date.valueOf(filters.getStartDate())), capture.getSeries(0).getTimePeriod(0));
        Assertions.assertEquals(new Second(Date.valueOf(filters.getStartDate().plusDays(1))), capture.getSeries(0).getTimePeriod(1));

        Assertions.assertEquals(dto.getPrice(), capture.getSeries(0).getValue(0));
       Assertions.assertEquals(3d, capture.getSeries(0).getValue(2));
        Assertions.assertEquals(4d, capture.getSeries(0).getValue(5));






    }

    @Test
    @DisplayName("generateBarForAverageCategoriesRateDefault")
    void generateBarForAverageCategoriesRateDefault() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        CategoryStatDto dto = new CategoryStatDto();
        dto.setCategoryId(1l);
        dto.setCategoryName("cat1");
        dto.setAvgPrice(BigDecimal.valueOf(150));
        dto.setMinPrice(BigDecimal.valueOf(100));
        dto.setMaxPrice(BigDecimal.valueOf(200));
        dto.setCountGoods(2l);

        CategoryStatDto dto1 = new CategoryStatDto();
        dto1.setCategoryId(2l);
        dto1.setCategoryName("cat2");
        dto1.setAvgPrice(BigDecimal.valueOf(165));
        dto1.setMinPrice(BigDecimal.valueOf(110));
        dto1.setMaxPrice(BigDecimal.valueOf(220));
        dto1.setCountGoods(2l);

        service.generateBarForAverageCategoriesRate(
                out, "Title", "X", "Y",
                List.of(dto, dto1)
        );

        ArgumentCaptor<DefaultCategoryDataset> captor = ArgumentCaptor.forClass(DefaultCategoryDataset.class);
        verify(graphicService).generateBar(
                eq(out), eq("Title"), eq("X"), eq("Y"), captor.capture()
        );
        DefaultCategoryDataset capture = captor.getValue();
        Assertions.assertEquals(dto1.getAvgPrice(), capture.getValue("Y",dto1.getCategoryId()));
        Assertions.assertEquals(dto.getAvgPrice(), capture.getValue("Y",dto.getCategoryId()));


    }

    @Test
    @DisplayName("generateBarForAverageCategoriesInDistrictDefault")
    void generateBarForAverageCategoriesInDistrictDefault() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        DistrictStatisticDto dto = new DistrictStatisticDto();
        dto.setCategoryId(1l);
        dto.setAvgPrice(BigDecimal.valueOf(150));

        DistrictStatisticDto dto1 = new DistrictStatisticDto();
        dto1.setCategoryId(2l);
        dto1.setAvgPrice(BigDecimal.valueOf(165));

        DistrictStatisticDto dto2 = new DistrictStatisticDto();
        dto2.setCategoryId(3l);
        dto2.setAvgPrice(BigDecimal.valueOf(110));

        service.generateBarForAverageCategoriesInDistrict(
          out, "Title", "X", "Y",
          List.of(dto, dto1, dto2)
        );

        ArgumentCaptor<DefaultCategoryDataset> captor = ArgumentCaptor.forClass(DefaultCategoryDataset.class);

        verify(graphicService).generateBar(
                eq(out), eq("Title"), eq("X"),
                eq("Y"), captor.capture()
        );
        DefaultCategoryDataset capture = captor.getValue();
        Assertions.assertEquals(dto1.getAvgPrice(), capture.getValue("Y",dto1.getCategoryId()));
        Assertions.assertEquals(dto.getAvgPrice(), capture.getValue("Y",dto.getCategoryId()));
        Assertions.assertEquals(dto2.getAvgPrice(), capture.getValue("Y",dto2.getCategoryId()));
    }

    @Test
    @DisplayName("")
    void generateBarForAverageCategoryInDistricts() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        DistrictStatisticDto dto = new DistrictStatisticDto();
        dto.setAvgPrice(BigDecimal.valueOf(150));
        dto.setDistrictId(1l);

        DistrictStatisticDto dto1 = new DistrictStatisticDto();
        dto1.setDistrictId(2l);
        dto1.setAvgPrice(BigDecimal.valueOf(165));

        DistrictStatisticDto dto2 = new DistrictStatisticDto();
        dto2.setDistrictId(3l);
        dto2.setAvgPrice(BigDecimal.valueOf(110));

        service.generateBarForAverageCategoryInDistricts(
                out, "Title", "X", "Y",
                List.of(dto,dto1,dto2)
        );

        ArgumentCaptor<DefaultCategoryDataset> captor = ArgumentCaptor.forClass(DefaultCategoryDataset.class);
        verify(graphicService).generateBar(
                eq(out), eq("Title"), eq("X"), eq("Y"),
                captor.capture()
        );

        DefaultCategoryDataset capture = captor.getValue();
        Assertions.assertEquals(dto1.getAvgPrice(), capture.getValue("Y",dto1.getDistrictId()));
        Assertions.assertEquals(dto.getAvgPrice(), capture.getValue("Y",dto.getDistrictId()));
        Assertions.assertEquals(dto2.getAvgPrice(), capture.getValue("Y",dto2.getDistrictId()));

    }
}
