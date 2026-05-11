package org.example.core.controllers.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.getting.goods.GoodGetFullDto;
import org.example.core.dto.getting.reviews.ReviewFullDto;
import org.example.core.dto.getting.statistics.shops.ShopGetDto;
import org.example.core.dto.export.PriceHistoryByGoodAndShop;
import org.example.core.dto.export.ShopsCurrentPricesDto;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.hibernate.base_settings.filters.ModeratorRecalcFilter;
import org.example.core.hibernate.base_settings.filters.goods.GoodFilter;
import org.example.core.hibernate.base_settings.filters.rates.RatingRecalcFilter;
import org.example.core.hibernate.base_settings.filters.reviews.ReviewAdvancedFilters;
import org.example.core.hibernate.base_settings.service_dto.RateExportDto;
import org.example.core.hibernate.base_settings.sorting_types.ReviewSortTypes;
import org.example.core.services.dictionaries.CategoryService;
import org.example.core.services.dictionaries.TagService;
import org.example.core.services.documents.ModeratorRecalcService;
import org.example.core.services.documents.RateService;
import org.example.core.services.documents.prices.PriceExportService;
import org.example.core.services.documents.reviews.ReviewAdvancedService;
import org.example.core.services.export.ExportCsvService;
import org.example.core.services.export.ExportXlsxService;
import org.example.core.services.objects.GoodService;
import org.example.core.services.objects.ShopService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        ExportController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class ExportControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    PriceExportService priceService;

    @Autowired
    ReviewAdvancedService reviewService;

    @Autowired
    RateService rateService;

    @Autowired
    GoodService goodService;

    @Autowired
    ExportCsvService csvService;

    @Autowired
    ExportXlsxService xlsxService;

    @Autowired
    ModeratorRecalcService moderatorRecalcService;

    private static ObjectMapper mapper = new ObjectMapper();

    MockMvc mockMvc;

    @BeforeAll
    static void setUpObjectMapper() {
        mapper.registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getPricesIfValidParamsAsCsv() throws Exception {
        when(priceService.getShopsCurrentPrices(any()))
                .thenReturn(List.of(new ShopsCurrentPricesDto()));
        when(csvService.getShopsCurrentPrices(any(), any()))
                .thenReturn("csv content".getBytes());

        mockMvc.perform(get("/export/prices")
                        .param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"report.csv\""))
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getPricesIfValidParamsAsXlsx() throws Exception {
        when(priceService.getShopsCurrentPrices(any()))
                .thenReturn(List.of(new ShopsCurrentPricesDto()));
        when(xlsxService.generateReportForCurrentShopsPrices(any(), any(), any(), any()))
                .thenReturn("xlsx content".getBytes());

        mockMvc.perform(get("/export/prices")
                        .param("format", "xlsx"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"report.xlsx\""));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getPricesIfNothingFound() throws Exception {
        when(priceService.getShopsCurrentPrices(any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/export/prices")
                        .param("format", "csv"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getPricesIfFormatInvalid() throws Exception {
        when(priceService.getShopsCurrentPrices(any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/export/prices")
                        .param("format", "br"))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value(
                        containsString("ExportFormat Unknown include value:")
                ));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getPricesIfIncludesInvalid() throws Exception {
        when(priceService.getShopsCurrentPrices(any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/export/prices")
                        .param("include", "cr"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value(
                        containsString("ShopsCurrentPricesIncludeTypes Unknown include value:")
                ));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getPriceHistoryIfValidParams() throws Exception {
        when(priceService.getPriceHistoryByGoodId(anyLong(), anyLong()))
                .thenReturn(List.of(new PriceHistoryByGoodAndShop()));
        when(csvService.getPriceHistoryByGoodId(any()))
                .thenReturn("csv".getBytes());

        mockMvc.perform(get("/export/prices/history/1")
                        .param("shopId", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"report.csv\""))
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getPriceHistoryIfInvalidGoodId() throws Exception {
        mockMvc.perform(get("/export/prices/history/0")
                        .param("shopId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message")
                        .value("Path variable id and request param shopId must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getPriceHistoryIfInvalidShopId() throws Exception {
        mockMvc.perform(get("/export/prices/history/1")
                        .param("shopId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message")
                        .value("Path variable id and request param shopId must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getPriceHistoryIfNothingFound() throws Exception {
        when(priceService.getPriceHistoryByGoodId(anyLong(), anyLong()))
                .thenReturn(List.of());

        mockMvc.perform(get("/export/prices/history/1")
                        .param("shopId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getReviewsHistoryIfValidBodyAsCsv() throws Exception {
        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();
        filters.setSortType(ReviewSortTypes.DESC);

        when(reviewService.getByFilters(any()))
                .thenReturn(List.of(new ReviewFullDto()));
        when(csvService.getReviewsHistory(any()))
                .thenReturn("csv".getBytes());

        mockMvc.perform(get("/export/reviews/history")
                        .param("format", "csv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"report.csv\""))
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getReviewsHistoryIfCsvWithInclude() throws Exception {
        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();

        when(reviewService.getByFilters(any()))
                .thenReturn(List.of(new ReviewFullDto()));

        mockMvc.perform(get("/export/reviews/history")
                        .param("format", "csv")
                        .param("include", "MODERATORS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message")
                        .value("Csv can not be applied with include"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getReviewsHistoryIfInvalidBody() throws Exception {
        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();
        filters.setBlockedBy(-1L);
        filters.setGoodId(0L);
        filters.setRate(-10);
        filters.setPage(-10);
        filters.setSize(0);
        mockMvc.perform(get("/export/reviews/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(5)));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getReviewsHistoryIfFormatInvalid() throws Exception {
        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();
        mockMvc.perform(get("/export/reviews/history")
                        .param("format", "re")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value(
                        containsString("ExportFormat Unknown include value:")
                ));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getReviewsHistoryIfNothingFound() throws Exception {
        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();
        when(reviewService.getByFilters(any())).thenReturn(List.of());

        mockMvc.perform(get("/export/reviews/history")
                        .param("format", "csv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getReviewsHistoryByGoodIdIfValidBody() throws Exception {
        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();
        filters.setGoodId(1L);

        when(reviewService.getByFilters(any()))
                .thenReturn(List.of(new ReviewFullDto()));
        when(csvService.getReviewsHistoryByGoodId(any()))
                .thenReturn("csv".getBytes());

        mockMvc.perform(get("/export/reviews/history/good")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"report.csv\""));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getReviewsHistoryByGoodIdIfGoodIdIsNull() throws Exception {
        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();
        when(reviewService.getByFilters(any()))
                .thenReturn(List.of(new ReviewFullDto()));

        mockMvc.perform(get("/export/reviews/history/good")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Good id must be given"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getReviewsHistoryByGoodIdIfNothingFound() throws Exception {
        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();
       filters.setGoodId(14L);
       filters.setRate(1);

        when(reviewService.getByFilters(any())).thenReturn(List.of());

        mockMvc.perform(get("/export/reviews/history/good")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getRecalculationsHistoryIfValidBodyAsCsv() throws Exception {
        RatingRecalcFilter filters = new RatingRecalcFilter();

        when(rateService.getRatesExportByFilter(any()))
                .thenReturn(List.of(new RateExportDto()));
        when(csvService.getRecalculations(any()))
                .thenReturn("csv".getBytes());

        mockMvc.perform(get("/export/recalculations/history")
                        .param("format", "csv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"recalculations.csv\""))
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getRecalculationsHistoryIfNothingFound() throws Exception {
        RatingRecalcFilter filters = new RatingRecalcFilter();
        when(rateService.getRatesExportByFilter(any())).thenReturn(List.of());

        mockMvc.perform(get("/export/recalculations/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getRecalculationsHistoryIfInvalidBody() throws Exception {
        RatingRecalcFilter filters = new RatingRecalcFilter();
        filters.setStartDate(LocalDate.of(2027,2,1));
        filters.setEndDate(LocalDate.of(2025,2,2));

        mockMvc.perform(get("/export/recalculations/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]").value(
                        containsString("startDate must be <= endDate")
                ));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getRecalculationsHistoryIfFormatInvalid() throws Exception {
        RatingRecalcFilter filters = new RatingRecalcFilter();
        mockMvc.perform(get("/export/recalculations/history")
                        .param("format", "ra")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value(
                        containsString("ExportFormat Unknown include value:")
                ));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getRecalculationsHistoryIfIncludeInvalid() throws Exception {
        RatingRecalcFilter filters = new RatingRecalcFilter();
        mockMvc.perform(get("/export/recalculations/history")
                        .param("include", "ra")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value(
                        containsString("RecalculationInclude Unknown include value: ")
                ));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getAllInfoGoodsIfValidBodyAsCsv() throws Exception {
        GoodFilter filters = new GoodFilter();
        filters.setCurRating(2d);
        when(goodService.findAllForAnalyst(any()))
                .thenReturn(List.of(new GoodGetFullDto()));
        when(csvService.getAllGoods(any()))
                .thenReturn("csv".getBytes());

        mockMvc.perform(get("/export/goods")
                        .param("format", "csv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"all_goods.csv\""))
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getAllInfoGoodsIfNothingFound() throws Exception {
        GoodFilter filters = new GoodFilter();
        when(goodService.findAllForAnalyst(any())).thenReturn(List.of());

        mockMvc.perform(get("/export/goods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getAllInfoGoodsIfInvalidBody() throws Exception {
        GoodFilter filters = new GoodFilter();
        filters.setTagIds(List.of());
        filters.setCategoryIds(List.of());

        mockMvc.perform(get("/export/goods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors",hasSize(2)));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getAllInfoGoodsIfFormatInvalid() throws Exception {
        GoodFilter filters = new GoodFilter();
        mockMvc.perform(get("/export/goods")
                        .param("format", "qe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value(
                        containsString("ExportFormat Unknown include value:")
                ));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getAllInfoGoodsIfIncludeInvalid() throws Exception {
        GoodFilter filters = new GoodFilter();
        mockMvc.perform(get("/export/goods")
                        .param("include", "qe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value(
                        containsString(" AllGoodsInclude Unknown include value:")
                ));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getModeratorRecalcIfValidBodyAsCsv() throws Exception {
        ModeratorRecalcFilter filters = new ModeratorRecalcFilter();

        when(moderatorRecalcService.findAllFullVersion(any()))
                .thenReturn(List.of());
        when(csvService.getModeratorRecalc(any()))
                .thenReturn("csv".getBytes());

        mockMvc.perform(get("/export/moderators-recalculations")
                        .param("format", "csv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"moderators-recalculations.csv\""))
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getModeratorRecalcIfInvalidBody() throws Exception {
        ModeratorRecalcFilter filters = new ModeratorRecalcFilter();
        filters.setGoodId(-1L);
        filters.setModeratorId(-1L);
        filters.setModeratorIds(List.of());
        mockMvc.perform(get("/export/moderators-recalculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(4)));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getModeratorRecalcIfFormatInvalid() throws Exception {
        ModeratorRecalcFilter filters = new ModeratorRecalcFilter();
        mockMvc.perform(get("/export/moderators-recalculations")
                        .param("format", "re")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value(
                        containsString("ExportFormat Unknown include value:")
                ));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getModeratorRecalcIfIncludeInvalid() throws Exception {
        ModeratorRecalcFilter filters = new ModeratorRecalcFilter();
        mockMvc.perform(get("/export/moderators-recalculations")
                        .param("include", "re")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value(
                        containsString("ModeratorRecalcInclude Unknown include value:")
                ));
    }
}
