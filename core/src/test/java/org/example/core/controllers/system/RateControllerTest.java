package org.example.core.controllers.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.getting.rates.RateFullDto;
import org.example.core.dto.getting.rates.RateValidGoodDto;
import org.example.core.dto.getting.rates.RateWithGoodNameDto;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.hibernate.base_settings.filters.rates.RatesFilter;
import org.example.core.hibernate.base_settings.filters.rates.RatingRecalcFilter;
import org.example.core.models.types.RatingTriggerType;
import org.example.core.services.RecalculationService;
import org.example.core.services.documents.RateService;
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
        RateController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class RateControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    RateService rateService;

    @Autowired
    RecalculationService recalculationService;

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
    @WithMockCustomUser(role = "MODERATOR")
    void getTopRatesIfAuthorizedWithValidParams() throws Exception {
        when(rateService.getTopRatesAmongAll(anyInt(), anyBoolean()))
                .thenReturn(List.of(new RateWithGoodNameDto()));

        mockMvc.perform(get("/rates/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("negative")
    void getTopRatesIfUnauthorized() throws Exception {
        when(rateService.getTopRatesAmongAll(anyInt(), anyBoolean()))
                .thenReturn(List.of());

        mockMvc.perform(get("/rates/top"))
                .andExpect(status().isUnauthorized());
    }

    private static Stream<Arguments> provideForGetTop() {
        return Stream.of(
                Arguments.of("num", "0"),
                Arguments.of("num", "-1")
        );
    }

    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    @ParameterizedTest
    @MethodSource("provideForGetTop")
    void getTopRatesWithInvalidParams(String param, String value) throws Exception {
        mockMvc.perform(get("/rates/top")
                        .param(param, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("num request param must be  > 0"));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getTopRatesAmongGoodIfValidParams() throws Exception {
        when(rateService.getTopRatesAmongProduct(anyInt(), anyLong()))
                .thenReturn(List.of(new RateValidGoodDto()));

        mockMvc.perform(get("/rates/goods/1/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private static Stream<Arguments> provideForGetTopRates() {
        return Stream.of(
                Arguments.of( "0"),
                Arguments.of("-1")
        );
    }

    @ParameterizedTest
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    @MethodSource("provideForGetTopRates")
    void getTopRatesAmongGoodIfInvalidId( String value) throws Exception {
        mockMvc.perform(get("/rates/goods/" +value+ "/top"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }



    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getTopRatesAmongGoodIfInvalidCount() throws Exception {
        mockMvc.perform(get("/rates/goods/1/top")
                        .param("count", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Count must be > 0"));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void getGoodRatesByFilterIfValidBody() throws Exception {
        RatingRecalcFilter filters = new RatingRecalcFilter();
        filters.setGoodId(1L);
        filters.setEndDate(LocalDate.of(2024,1,1));

        when(rateService.getGoodRatesByFilter(any()))
                .thenReturn(List.of(new RateFullDto()));

        mockMvc.perform(get("/rates/good")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void getGoodRatesByFilterIfGoodIdIsNull() throws Exception {
        RatingRecalcFilter filters = new RatingRecalcFilter();
        mockMvc.perform(get("/rates/good")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("goodId must be given"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void getGoodRatesByFilterIfCategoryIdGiven() throws Exception {
        RatingRecalcFilter filters = new RatingRecalcFilter();
        filters.setGoodId(1L);
        filters.setCategoryId(1L);

        mockMvc.perform(get("/rates/good")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors[0]")
                        .value("Either goodId or categoryId + categoryIds"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void getGoodRatesByFilterIfTagIdGiven() throws Exception {
        RatingRecalcFilter filters = new RatingRecalcFilter();
        filters.setGoodId(1L);
        filters.setTagId(1L);

        mockMvc.perform(get("/rates/good")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("tag can not be given"));
    }


    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void getGoodRatesByFilterIfInvalidBody() throws Exception {
        RatingRecalcFilter filters = new RatingRecalcFilter();
        filters.setGoodId(1L);
        filters.setEndDate(LocalDate.of(2024,1,1));
        filters.setStartDate(LocalDate.of(2026,1,2));
        filters.setCurRate(-1d);
        mockMvc.perform(get("/rates/good")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(2)));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void getAllGoodsRatesByFilterIfValidBody() throws Exception {
        RatingRecalcFilter filters = new RatingRecalcFilter();
        filters.setTriggerType(RatingTriggerType.ADMIN);
        when(rateService.getGoodRatesByFilter(any()))
                .thenReturn(List.of(new RateFullDto()));

        mockMvc.perform(get("/rates/goods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getAllGoodsRatesByFilterIfInvalidBody() throws Exception {
        RatingRecalcFilter filters = new RatingRecalcFilter();
        filters.setTagId(-1L);
        filters.setTagIds(List.of());
        mockMvc.perform(get("/rates/goods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors",hasSize(3)));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getGoodRatesInTimeIfValidBody() throws Exception {
        RatesFilter filters = new RatesFilter();
        filters.setGoodId(1L);
        filters.setEndDate(LocalDate.of(2024,1,1));
        filters.setStartDate(LocalDate.of(2022,1,2));
        when(rateService.getGoodRateInTime(any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/rates/good/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getGoodRatesInTimeIfInvalidBody() throws Exception {
        RatesFilter filters = new RatesFilter();


        mockMvc.perform(get("/rates/good/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void recalculateRatingIfRoleAllowed() throws Exception {
        when(recalculationService.personRequest(any(), any()))
                .thenReturn(new StringResponse("Recalculation started"));

        mockMvc.perform(get("/rates/recalculation/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }



    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void recalculateRatingIfRoleProhibited() throws Exception {
        mockMvc.perform(get("/rates/recalculation"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void recalculateRatingIfUnauthorized() throws Exception {
        mockMvc.perform(get("/rates/recalculation"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void recalculateRatingByIdIfRoleAllowedWithValidId() throws Exception {
        when(recalculationService.personRequest(anyLong(), any()))
                .thenReturn(new StringResponse("Recalculation started"));

        mockMvc.perform(get("/rates/recalculation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void recalculateRatingByIdIfRoleAllowedWithInvalidId() throws Exception {
        mockMvc.perform(get("/rates/recalculation/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void recalculateRatingByIdIfRoleAllowedWithNegativeId() throws Exception {
        mockMvc.perform(get("/rates/recalculation/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void recalculateRatingByIdIfRoleProhibited() throws Exception {
        mockMvc.perform(get("/rates/recalculation/1"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("negative")
    void recalculateRatingByIdIfUnauthorized() throws Exception {
        mockMvc.perform(get("/rates/recalculation/1"))
                .andExpect(status().isUnauthorized());
    }
}
