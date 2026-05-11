package org.example.core.controllers.prices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.getting.goods.GoodAnalyseForShopDto;
import org.example.core.dto.getting.goods.GoodPriceInShop;
import org.example.core.dto.getting.prices.PriceInTime;
import org.example.core.dto.getting.statistics.CartStatisticRequest;
import org.example.core.dto.getting.statistics.DistrictStatisticDto;
import org.example.core.dto.getting.statistics.categories.CategoryStatDto;
import org.example.core.dto.getting.statistics.shops.ShopCartDto;
import org.example.core.dto.getting.statistics.shops.ShopStatByCategoryDto;
import org.example.core.dto.getting.statistics.shops.ShopStatisticDto;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.hibernate.base_settings.filters.goods.GoodPriceInShopsFilter;
import org.example.core.hibernate.base_settings.filters.prices.DistrictStatisticFilter;
import org.example.core.hibernate.base_settings.filters.prices.PriceInTimeFilter;
import org.example.core.hibernate.base_settings.filters.prices.ShopStatByCategoryFilter;
import org.example.core.services.documents.prices.PriceAnalyzeService;
import org.example.core.services.graphics.GraphicalAnalyseService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        PriceForAnalyzeController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class PriceForAnalyzeControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    PriceAnalyzeService priceService;

    @Autowired
    GraphicalAnalyseService graphicalAnalyseService;

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
    void getGoodsByShopIdIfValidParams() throws Exception {
        when(priceService.getGoodsByShop(anyString(), anyLong(), anyInt()))
                .thenReturn(List.of(new GoodAnalyseForShopDto()));

        mockMvc.perform(get("/analyst/prices/shops/1/goods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private static Stream<Arguments> invalidParamsForgetGoodsByShopId(){
        return Stream.of(
                Arguments.of("count", "0", "Request Param count must be > 0"),
                Arguments.of("count", "-1", "Request Param count must be > 0"),
                Arguments.of("type", "hjk", "Request param type  must be min or max"),
                Arguments.of("type", "MINIMAL", "Request param type  must be min or max")
        );
    }

    private static Stream<Arguments> invalidShopIdForgetGoodsByShopId(){
        return Stream.of(
                Arguments.of( "0"),
                Arguments.of( "-1")
        );
    }

    @ParameterizedTest
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    @MethodSource("invalidShopIdForgetGoodsByShopId")
    void getGoodsByShopIdIfInvalidId(String value) throws Exception {
        mockMvc.perform(get("/analyst/prices/shops/" + value + "/goods"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("path variable id must be > 0"));
    }


    @ParameterizedTest
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    @MethodSource("invalidParamsForgetGoodsByShopId")
    void getGoodsByShopIdIfInvalidParams(String param, String value, String er) throws Exception {
        mockMvc.perform(get("/analyst/prices/shops/1/goods")
                        .param(param, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value(er));
    }




    @Test
    @Tag("negative")
    void getGoodsByShopIdIfUnauthorized() throws Exception {
        mockMvc.perform(get("/analyst/prices/shops/1/goods"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void getGoodsByShopIdIfRoleProhibited() throws Exception {
        when(priceService.getGoodsByShop(anyString(), anyLong(), anyInt()))
                .thenReturn(List.of(new GoodAnalyseForShopDto()));

        mockMvc.perform(get("/analyst/prices/shops/1/goods"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }



    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getGoodPricesInShopsIfValidBody() throws Exception {
        GoodPriceInShopsFilter filters = new GoodPriceInShopsFilter();
        filters.setGoodId(11L);

        when(priceService.getGoodPricesInShops(any()))
                .thenReturn(List.of(new GoodPriceInShop()));

        mockMvc.perform(get("/analyst/prices/shops/good/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getGoodPricesInShopsIfNothingFound() throws Exception {
        GoodPriceInShopsFilter filters = new GoodPriceInShopsFilter();
       filters.setShopIds(List.of(1L,2L));
       filters.setGoodId(1L);

        when(priceService.getGoodPricesInShops(any())).thenReturn(List.of());

        mockMvc.perform(get("/analyst/prices/shops/good/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message")
                        .value("No prices were found to generate graph"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getGoodPricesInShopsIfInvalidBody() throws Exception {
        GoodPriceInShopsFilter filters = new GoodPriceInShopsFilter();
        filters.setShopIds(List.of());

        mockMvc.perform(get("/analyst/prices/shops/good/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(2)));
    }

    @Test
    @Tag("negative")
    void getGoodPricesInShopsIfUnauthorized() throws Exception {
        GoodPriceInShopsFilter filters = new GoodPriceInShopsFilter();
        filters.setGoodId(1L);
        mockMvc.perform(get("/analyst/prices/shops/good/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MAX_USER")
    void getGoodPricesInShopsIfRoleProhibited() throws Exception {
        GoodPriceInShopsFilter filters = new GoodPriceInShopsFilter();
        filters.setGoodId(1L);
        mockMvc.perform(get("/analyst/prices/shops/good/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isForbidden());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getGoodsByShopIdInTimeIfValidBody() throws Exception {
        PriceInTimeFilter filters = new PriceInTimeFilter();
        filters.setShopId(1L);
        filters.setGoodId(1L);
        filters.setStartDate(LocalDate.of(2024,1,1));
        filters.setEndDate(LocalDate.of(2024,1,2));

        when(priceService.getGoodPriceInTime(any()))
                .thenReturn(List.of(new PriceInTime()));

        mockMvc.perform(get("/analyst/prices/shops/good-in-time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getGoodsByShopIdInTimeIfNothingFound() throws Exception {
        PriceInTimeFilter filters = new PriceInTimeFilter();
        filters.setShopId(1L);
        filters.setGoodId(1L);
        filters.setStartDate(LocalDate.of(2024,1,1));
        filters.setEndDate(LocalDate.of(2024,1,2));


        when(priceService.getGoodPriceInTime(any())).thenReturn(List.of());

        mockMvc.perform(get("/analyst/prices/shops/good-in-time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message")
                        .value("No prices were found to generate graph"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void getGoodsByShopIdInTimeIfRoleProhibited() throws Exception {
        PriceInTimeFilter filters = new PriceInTimeFilter();
        filters.setShopId(1L);
        filters.setGoodId(1L);
        filters.setStartDate(LocalDate.of(2024,1,1));
        filters.setEndDate(LocalDate.of(2024,1,2));


        when(priceService.getGoodPriceInTime(any())).thenReturn(List.of());

        mockMvc.perform(get("/analyst/prices/shops/good-in-time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
     }

    @Test
    @Tag("negative")
    void getGoodsByShopIdInTimeIfUnauthorized() throws Exception {
        PriceInTimeFilter filters = new PriceInTimeFilter();
        filters.setShopId(1L);
        filters.setGoodId(1L);
        filters.setStartDate(LocalDate.of(2024,1,1));
        filters.setEndDate(LocalDate.of(2024,1,2));


        when(priceService.getGoodPriceInTime(any())).thenReturn(List.of());

        mockMvc.perform(get("/analyst/prices/shops/good-in-time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getGoodsByShopIdInTimeIfInvalidBody() throws Exception {
        PriceInTimeFilter filters = new PriceInTimeFilter();
        filters.setShopId(-1L);
        filters.setStartDate(LocalDate.of(2024,1,1));
        filters.setEndDate(LocalDate.of(2022,1,2));

        mockMvc.perform(get("/analyst/prices/shops/good-in-time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getShopsStatsByMainCategoriesIfValidBody() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();
        filters.setShopIds(List.of(1L,2L));

        when(priceService.getShopsStatByMainCategories(any()))
                .thenReturn(List.of(new ShopStatByCategoryDto()));

        mockMvc.perform(get("/analyst/prices/categories/main")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getShopsStatsByMainCategoriesIfInvalidBody() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();
        filters.setCategoryIds(List.of());
        filters.setShopIds(List.of());
        filters.setStartDate(LocalDate.of(2024,1,1));
        filters.setEndDate(LocalDate.of(2022,1,2));

        mockMvc.perform(get("/analyst/prices/categories/main")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    @Tag("negative")
    void getShopsStatsByMainCategoriesIfUnauthorized() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();

        mockMvc.perform(get("/analyst/prices/categories/main")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getShopsStatsByMainCategoriesIfRoleProhibited() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();

        mockMvc.perform(get("/analyst/prices/categories/main")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isForbidden());
    }


    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getShopStatsByMainShopIdGraphIfShopIdsIsNull() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();

        mockMvc.perform(get("/analyst/prices/categories/main/shop/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("shopIds length must be 1"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getShopsStatsByMainShopIdGraphIfShopIdsMoreThanOne() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();
        filters.setShopIds(List.of(1L, 2L));

        mockMvc.perform(get("/analyst/prices/categories/main/shop/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("shopIds length must be 1"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getShopsStatsByMainShopIdGraphIfNothingFound() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();
        filters.setShopIds(List.of(1L));

        when(priceService.getShopsStatByMainCategories(any())).thenReturn(List.of());

        mockMvc.perform(get("/analyst/prices/categories/main/shop/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getShopsStatsByMainShopIdGraphIfRoleProhibited() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();
        filters.setShopIds(List.of(1L));


        mockMvc.perform(get("/analyst/prices/categories/main/shop/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    void getShopsStatsByMainShopIdGraphIfUnauthorized() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();
        filters.setShopIds(List.of(1L));


        mockMvc.perform(get("/analyst/prices/categories/main/shop/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getShopsStatsBySubCategoriesIfValidBody() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();
        filters.setStartDate(LocalDate.of(2024,1,1));
        filters.setEndDate(LocalDate.of(2024,1,2));

        when(priceService.getShopsStatBySubCategories(any()))
                .thenReturn(List.of(new ShopStatByCategoryDto()));

        mockMvc.perform(get("/analyst/prices/categories/sub")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getShopsStatsBySubCategoriesIfInvalidBody() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();
        filters.setStartDate(LocalDate.of(2024,1,1));
        filters.setEndDate(LocalDate.of(2023,1,2));

        mockMvc.perform(get("/analyst/prices/categories/sub")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(1)));
    }


    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getShopStatsBySubCategoriesGraphIfShopIdsIsNull() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();

        mockMvc.perform(get("/analyst/prices/categories/sub/shop/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("ShopIds length must be 1"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getShopStatsBySubCategoriesGraphIfShopIdsMoreThanOne() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();
        filters.setShopIds(List.of(1L, 2L));

        mockMvc.perform(get("/analyst/prices/categories/sub/shop/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("ShopIds length must be 1"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getShopStatsBySubCategoriesGraphIfNothingFound() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();
        filters.setShopIds(List.of(1L));

        when(priceService.getShopsStatBySubCategories(any())).thenReturn(List.of());

        mockMvc.perform(get("/analyst/prices/categories/sub/shop/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("No prices were found to generate graph"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getShopStatsBySubCategoriesGraphIfRoleProhibited() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();
        filters.setShopIds(List.of(1L));

        mockMvc.perform(get("/analyst/prices/categories/sub/shop/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getShopStatsBySubCategoriesGraphIfUnauthorized() throws Exception {
        ShopStatByCategoryFilter filters = new ShopStatByCategoryFilter();
        filters.setShopIds(List.of(1L));

        mockMvc.perform(get("/analyst/prices/categories/sub/shop/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getShopStatsByDistrictsIfValidBody() throws Exception {
        DistrictStatisticFilter filters = new DistrictStatisticFilter();
        filters.setCategoryIds(List.of(1L));
        filters.setStartDate(LocalDate.of(2024,1,1));
        filters.setEndDate(LocalDate.of(2024,1,2));

        when(priceService.getShopsStatByDistricts(any()))
                .thenReturn(List.of(new DistrictStatisticDto()));

        mockMvc.perform(get("/analyst/prices/shops/districts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getShopStatsByDistrictsIfInvalidBody() throws Exception {
        DistrictStatisticFilter filters = new DistrictStatisticFilter();
        filters.setCategoryIds(List.of(1L));
        filters.setTagIds(List.of(1L));
        filters.setGoodIds(List.of(1L));
        filters.setStartDate(LocalDate.of(2024,1,1));
        filters.setEndDate(LocalDate.of(2022,1,2));

        mockMvc.perform(get("/analyst/prices/shops/districts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                        .andExpect(jsonPath("$.errors", hasSize(4)));
    }

    @Test
    @Tag("negative")
    void getShopStatsByDistrictsIfUnauthorized() throws Exception {
        DistrictStatisticFilter filters = new DistrictStatisticFilter();

        mockMvc.perform(get("/analyst/prices/shops/districts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getShopStatsByDistrictsIfRoleProhibited() throws Exception {
        DistrictStatisticFilter filters = new DistrictStatisticFilter();


        mockMvc.perform(get("/analyst/prices/shops/districts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getDistrictGraphIfDistrictIdsIsNull() throws Exception {
        DistrictStatisticFilter filters = new DistrictStatisticFilter();

        mockMvc.perform(get("/analyst/prices/shops/district/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("categoryIds length must be 1"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getDistrictGraphIfDistrictIdsMoreThanOne() throws Exception {
        DistrictStatisticFilter filters = new DistrictStatisticFilter();
        filters.setDistrictIds(List.of(1L, 2L));

        mockMvc.perform(get("/analyst/prices/shops/district/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("categoryIds length must be 1"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getDistrictGraphIfNothingFound() throws Exception {
        DistrictStatisticFilter filters = new DistrictStatisticFilter();
        filters.setDistrictIds(List.of(1L));

        when(priceService.getShopsStatByDistricts(any())).thenReturn(List.of());

        mockMvc.perform(get("/analyst/prices/shops/district/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("No prices were found to generate graph"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getDistrictGraphIfRoleProhibited() throws Exception {
        DistrictStatisticFilter filters = new DistrictStatisticFilter();
        filters.setDistrictIds(List.of(1L));


        mockMvc.perform(get("/analyst/prices/shops/district/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    void getDistrictGraphIfUnauthorized() throws Exception {
        DistrictStatisticFilter filters = new DistrictStatisticFilter();
        filters.setDistrictIds(List.of(1L));


        mockMvc.perform(get("/analyst/prices/shops/district/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getCategoryDistrictsGraphIfCategoryIdsIsNull() throws Exception {
        DistrictStatisticFilter filters = new DistrictStatisticFilter();

        mockMvc.perform(get("/analyst/prices/shops/districts/category/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("categoryIds length must be 1"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getCategoryDistrictsGraphIfCategoryIdsMoreThanOne() throws Exception {
        DistrictStatisticFilter filters = new DistrictStatisticFilter();
        filters.setCategoryIds(List.of(1L, 2L));

        mockMvc.perform(get("/analyst/prices/shops/districts/category/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("categoryIds length must be 1"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getCategoryDistrictsGraphIfNothingFound() throws Exception {
        DistrictStatisticFilter filters = new DistrictStatisticFilter();
        filters.setCategoryIds(List.of(1L));

        when(priceService.getShopsStatByDistricts(any())).thenReturn(List.of());

        mockMvc.perform(get("/analyst/prices/shops/districts/category/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void getCategoryDistrictsGraphIfRoleProhibited() throws Exception {
        DistrictStatisticFilter filters = new DistrictStatisticFilter();
        filters.setCategoryIds(List.of(1L));


        mockMvc.perform(get("/analyst/prices/shops/districts/category/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    void getCategoryDistrictsGraphIfUnauthorized() throws Exception {
        DistrictStatisticFilter filters = new DistrictStatisticFilter();
        filters.setCategoryIds(List.of(1L));


        mockMvc.perform(get("/analyst/prices/shops/districts/category/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getShopStatisticsIfValidId() throws Exception {
        when(priceService.getShopStatistics(anyLong()))
                .thenReturn(new ShopStatisticDto());

        mockMvc.perform(get("/analyst/prices/shops/1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getShopStatisticsIfInvalidId() throws Exception {
        mockMvc.perform(get("/analyst/prices/shops/0/statistics"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("path variable shop id  must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getShopStatisticsIfNegativeId() throws Exception {
        mockMvc.perform(get("/analyst/prices/shops/-1/statistics"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("path variable shop id  must be > 0"));
    }

    @Test
    @Tag("negative")
    void getShopStatisticsIfUnauthorized() throws Exception {
        mockMvc.perform(get("/analyst/prices/shops/1/statistics"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /analyst/prices/shops/cart ──────────────────────────

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void compareCartInShopsIfValidBody() throws Exception {
        CartStatisticRequest request = new CartStatisticRequest();
        request.setShopIds(List.of(1L));
        request.setGoodIds(List.of(2L,3L));

        when(priceService.compareCartByShops(any()))
                .thenReturn(List.of(new ShopCartDto()));

        mockMvc.perform(get("/analyst/prices/shops/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void compareCartInShopsIfInvalidBody() throws Exception {
        CartStatisticRequest request = new CartStatisticRequest();

        mockMvc.perform(get("/analyst/prices/shops/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(2)));
    }

    @Test
    @Tag("negative")
    void compareCartInShopsIfUnauthorized() throws Exception {
        CartStatisticRequest request = new CartStatisticRequest();
        request.setShopIds(List.of(1L));
        request.setGoodIds(List.of(2L,3L));
        mockMvc.perform(get("/analyst/prices/shops/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /analyst/prices/shops/cart/graph ────────────────────

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void compareCartInShopsGraphIfValidBody() throws Exception {
        CartStatisticRequest request = new CartStatisticRequest();
        request.setShopIds(List.of(1L));
        request.setGoodIds(List.of(2L,3L));

        when(priceService.compareCartByShops(any()))
                .thenReturn(List.of(new ShopCartDto()));

        mockMvc.perform(get("/analyst/prices/shops/cart/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void compareCartInShopsGraphIfNothingFound() throws Exception {
        CartStatisticRequest request = new CartStatisticRequest();
        request.setShopIds(List.of(1L));
        request.setGoodIds(List.of(2L,3L));

        when(priceService.compareCartByShops(any())).thenReturn(List.of());

        mockMvc.perform(get("/analyst/prices/shops/cart/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void compareCartInShopsGraphIfInvalidBody() throws Exception {
        CartStatisticRequest request = new CartStatisticRequest();
        request.setShopIds(List.of());
        request.setGoodIds(List.of());

        mockMvc.perform(get("/analyst/prices/shops/cart/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(2)));
    }

    @Test
    @Tag("negative")
    void compareCartInShopsGraphIfUnauthorized() throws Exception {
        CartStatisticRequest request = new CartStatisticRequest();
        request.setShopIds(List.of(1L));
        request.setGoodIds(List.of(2L,3L));

        mockMvc.perform(get("/analyst/prices/shops/cart/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
