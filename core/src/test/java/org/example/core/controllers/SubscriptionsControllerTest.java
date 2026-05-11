package org.example.core.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;

import org.example.core.dto.getting.subscriptions.AvailabilitySubGetDto;
import org.example.core.dto.getting.subscriptions.PriceSubGetDto;
import org.example.core.exceptions.GlobalExceptionHandler;

import org.example.core.hibernate.base_settings.filters.subscriptions.AvailabilitySubFilter;
import org.example.core.hibernate.base_settings.filters.subscriptions.PriceSubFilter;
import org.example.core.services.documents.subscriptions.AvailabilitySubService;
import org.example.core.services.documents.subscriptions.PriceSubService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        SubscriptionsController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class SubscriptionsControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PriceSubService priceSubService;

    @Autowired
    private AvailabilitySubService availabilitySubService;

    private static final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

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
    @WithMockCustomUser(role = "ADMIN")
    void getAvailabilityIfAllowedRole() throws Exception {
        AvailabilitySubFilter filter = new AvailabilitySubFilter();
        List<AvailabilitySubGetDto> expected = List.of(new AvailabilitySubGetDto(), new AvailabilitySubGetDto());
        when(availabilitySubService.findAll(any(AvailabilitySubFilter.class))).thenReturn(expected);

        mockMvc.perform(get("/subscriptions/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }



    @Test
    @Tag("negative")
    void getAvailabilityIfUnauthorized() throws Exception {
        AvailabilitySubFilter filter = new AvailabilitySubFilter();
        mockMvc.perform(get("/subscriptions/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filter)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getAvailabilityIfRoleProhibited() throws Exception {
        AvailabilitySubFilter filter = new AvailabilitySubFilter();
        mockMvc.perform(get("/subscriptions/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filter)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void getAvailabilityWithInvalidBody() throws Exception {
        AvailabilitySubFilter filter = new AvailabilitySubFilter();
        filter.setCurDate(LocalDate.of(2025,12,12));
        filter.setStartDate(LocalDate.of(2024,12,1));
        filter.setGoodIds(List.of());
        filter.setPage(-10);
        String json = mapper.writeValueAsString(filter);
        mockMvc.perform(get("/subscriptions/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getPriceIfRoleAllowed() throws Exception {
        PriceSubFilter filter = new PriceSubFilter();
        List<PriceSubGetDto> expected = List.of(new PriceSubGetDto(), new PriceSubGetDto());
        when(priceSubService.findAll(any(PriceSubFilter.class))).thenReturn(expected);

        mockMvc.perform(get("/subscriptions/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }



    @Test
    @Tag("negative")
    void getPriceIfUnauthorized() throws Exception {
        PriceSubFilter filter = new PriceSubFilter();
        String json = mapper.writeValueAsString(filter);
        mockMvc.perform(get("/subscriptions/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getPriceIfRoleProhibited() throws Exception {
        PriceSubFilter filter = new PriceSubFilter();
        String json = mapper.writeValueAsString(filter);
        mockMvc.perform(get("/subscriptions/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void getPriceWithInvalidBody() throws Exception {
        PriceSubFilter filter = new PriceSubFilter();

        filter.setMaxPrice(BigDecimal.valueOf(12));
        filter.setMinPrice(BigDecimal.valueOf(15));
        filter.setCurPrice(BigDecimal.valueOf(-15));

        String json = mapper.writeValueAsString(filter);
        mockMvc.perform(get("/subscriptions/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(2)));
    }
}