package org.example.core.controllers.prices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.creating.PriceSubCreateDto;
import org.example.core.dto.getting.prices.PriceComparisonRequest;
import org.example.core.dto.getting.prices.PriceGetDtoForUser;

import org.example.core.dto.getting.subscriptions.AvailabilitySubGetDto;
import org.example.core.dto.getting.subscriptions.PriceSubGetDto;
import org.example.core.exceptions.GlobalExceptionHandler;

import org.example.core.models.User;
import org.example.core.services.documents.prices.PriceService;

import org.example.core.services.documents.subscriptions.AvailabilitySubService;
import org.example.core.services.documents.subscriptions.PriceSubService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        PriceForUserController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class PriceForUserControllerTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mockMvc;
    @Autowired
    PriceService priceService;
    @Autowired
    PriceSubService priceSubService;
    @Autowired
    AvailabilitySubService availabilitySubService;

    @Autowired
    WebApplicationContext context;

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
    @WithMockCustomUser(role = "MIN_USER")
    void getPricesWithValidParams() throws Exception {

        List<PriceGetDtoForUser> expected = List.of(new PriceGetDtoForUser(), new PriceGetDtoForUser());
        when(priceService.getAllForUser(1L, 2L)).thenReturn(expected);

        mockMvc.perform(get("/prices")
                        .param("goodId", "1")
                        .param("shopId", "2"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private static Stream<Arguments> provideInvalidGetParams() {
        return Stream.of(
                Arguments.of("goodId", "0", "shopId", "1", "goodId must be > 0"),
                Arguments.of("goodId", "-5", "shopId", "1", "goodId must be > 0"),
                Arguments.of("goodId", "1", "shopId", "0", "shopId must be > 0"),
                Arguments.of("goodId", "1", "shopId", "-3", "shopId must be > 0")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidGetParams")
    @Tag("negative")
    @WithMockCustomUser(role = "MAX_USER")
    void getPricesWithInvalidParams(String goodParam, String goodVal, String shopParam, String shopVal, String expectedMessage) throws Exception {
        mockMvc.perform(get("/prices")
                        .param(goodParam, goodVal)
                        .param(shopParam, shopVal))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getPricesWithoutParams() throws Exception {
        mockMvc.perform(get("/prices"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getPricesIfRoleProhibited() throws Exception {
        mockMvc.perform(get("/prices")
                        .param("shopId", "1")
                        .param("goodId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Tag("negative")
    void getPricesIfUnauthorized() throws Exception {
        mockMvc.perform(get("/prices")
                        .param("shopId", "1")
                        .param("goodId", "1"))

                .andExpect(status().isUnauthorized());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MAX_USER")
    void getComparisonIfMaxUserValidRequest() throws Exception {
        PriceComparisonRequest request = new PriceComparisonRequest();
        request.setGoodId(1L);
        request.setShopIds(List.of(1L, 2L, 3L, 4L)); // max 4 для max user

        List<PriceGetDtoForUser> expected = List.of(new PriceGetDtoForUser());
        when(priceService.getComparison(any(PriceComparisonRequest.class))).thenReturn(expected);

        mockMvc.perform(get("/prices/comparison")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MIN_USER")
    void getComparisonIfMinUserValidRequest() throws Exception {
        PriceComparisonRequest request = new PriceComparisonRequest();
        request.setGoodId(1L);
        request.setShopIds(List.of(1L, 2L)); // max 2 для min user

        when(priceService.getComparison(any(PriceComparisonRequest.class))).thenReturn(List.of());

        mockMvc.perform(get("/prices/comparison")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void getComparisonIfAdminWithAnyShopIds() throws Exception {
        PriceComparisonRequest request = new PriceComparisonRequest();
        request.setGoodId(1L);
        request.setShopIds(List.of(1L, 2L, 3L, 4L, 5L)); // сотрудникам без лимит
        when(priceService.getComparison(any())).thenReturn(List.of());

        mockMvc.perform(get("/prices/comparison")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MAX_USER")
    void getComparisonIfMaxUserExceedsMaxShops() throws Exception {
        PriceComparisonRequest request = new PriceComparisonRequest();
        request.setGoodId(1L);
        request.setShopIds(List.of(1L, 2L, 3L, 4L, 5L));

        mockMvc.perform(get("/prices/comparison")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not allowed to compare this amount of shops. Maximum: 4"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getComparisonIfMinUserExceedsMaxShops() throws Exception {
        PriceComparisonRequest request = new PriceComparisonRequest();
        request.setGoodId(1L);
        request.setShopIds(List.of(1L, 2L, 3L));

        mockMvc.perform(get("/prices/comparison")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You are not allowed to compare this amount of shops. Maximum: 2"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getComparisonIfMinUserWithNullShopIds() throws Exception {
        PriceComparisonRequest request = new PriceComparisonRequest();
        request.setGoodId(1L);
        request.setShopIds(null);

        mockMvc.perform(get("/prices/comparison")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not allowed to compare in all shops"));
    }

    @Test
    @Tag("negative")
    void getComparisonIfUnauthorized() throws Exception {
        PriceComparisonRequest request = new PriceComparisonRequest();
        request.setGoodId(1L);
        mockMvc.perform(get("/prices/comparison")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MAX_USER")
    void getComparisonIfInvalidBody() throws Exception {
        PriceComparisonRequest invalidRequest = new PriceComparisonRequest();
        mockMvc.perform(get("/prices/comparison")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(1)));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MAX_USER")
    void createPriceSubscriptionIfMaxUserWithValidDto() throws Exception {
        PriceSubCreateDto dto = new PriceSubCreateDto();
        dto.setGoodId(1L);
        dto.setShopId(2L);
        dto.setPrice(BigDecimal.valueOf(90.4));

        PriceSubGetDto response = new PriceSubGetDto();
        response.setId(10L);
        when(priceSubService.createSubscription(any(PriceSubCreateDto.class), any(User.class))).thenReturn(response);

        mockMvc.perform(post("/prices/subscribe/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MAX_USER")
    void createPriceSubscriptionIfInvalidDto() throws Exception {
        PriceSubCreateDto dto = new PriceSubCreateDto();
        dto.setGoodId(-1L);
        dto.setShopId(-2L);
        mockMvc.perform(post("/prices/subscribe/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    @Tag("negative")
    void createPriceSubscriptionIfUnauthorized() throws Exception {
        PriceSubCreateDto dto = new PriceSubCreateDto();
        dto.setGoodId(1L);
        dto.setShopId(2L);
        dto.setPrice(BigDecimal.valueOf(90.4));
        mockMvc.perform(post("/prices/subscribe/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void createPriceSubscriptionIfRoleProhibited() throws Exception {
        PriceSubCreateDto dto = new PriceSubCreateDto();
        dto.setGoodId(1L);
        dto.setShopId(2L);
        dto.setPrice(BigDecimal.valueOf(90.4));
        mockMvc.perform(post("/prices/subscribe/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MAX_USER")
    void createAvailabilitySubscriptionIfValidParams() throws Exception {
        AvailabilitySubGetDto response = new AvailabilitySubGetDto();
        response.setId(5L);
        when(availabilitySubService.createSubscription(any(User.class), eq(1L), eq(2L))).thenReturn(response);

        mockMvc.perform(post("/prices/subscribe/availability")
                        .param("goodId", "1")
                        .param("shopId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    private static Stream<Arguments> provideInvalidAvailabilityParams() {
        return Stream.of(
                Arguments.of("goodId", "0", "shopId", "1", "goodId must be > 0"),
                Arguments.of("goodId", "-1", "shopId", "1", "goodId must be > 0"),
                Arguments.of("goodId", "1", "shopId", "0", "shopId must be > 0"),
                Arguments.of("goodId", "1", "shopId", "-5", "shopId must be > 0")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidAvailabilityParams")
    @Tag("negative")
    @WithMockCustomUser(role = "MAX_USER")
    void createAvailabilitySubscriptionIfInvalidParams(String goodParam, String goodVal, String shopParam, String shopVal, String expectedMessage) throws Exception {
        mockMvc.perform(post("/prices/subscribe/availability")
                        .param(goodParam, goodVal)
                        .param(shopParam, shopVal))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @Test
    @Tag("negative")
    void createAvailabilitySubscriptionIfUnauthorized() throws Exception {
        mockMvc.perform(post("/prices/subscribe/availability")
                        .param("goodId", "1")
                        .param("shopId", "2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void createAvailabilitySubscriptionIfRoleProhibited() throws Exception {
        mockMvc.perform(post("/prices/subscribe/availability")
                        .param("goodId", "1")
                        .param("shopId", "2"))
                .andExpect(status().isForbidden());
    }
}
