package org.example.core.controllers.goods;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.creating.ReviewCreateDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.getting.goods.GoodGetForUserDto;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.hibernate.base_settings.filters.goods.GoodFilter;
import org.example.core.hibernate.base_settings.filters.reviews.ReviewForUserFilters;
import org.example.core.services.documents.FavouriteService;
import org.example.core.services.documents.reviews.ReviewForUserService;
import org.example.core.services.objects.GoodService;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

@ContextConfiguration(classes = {SecurityConfiguration.class,
        GoodController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class GoodControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    GoodService goodService;
    @Autowired
    FavouriteService favouriteService;
    @Autowired
    ReviewForUserService reviewService;

    private static ObjectMapper mapper = new ObjectMapper();

    MockMvc mockMvc;

    @BeforeAll
    static void setUpObjectMapper(){
        mapper.registerModule(new JavaTimeModule());
    }
    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity()).build();
    }

    @Test
    @WithMockCustomUser(role = "MAX_USER")
    void findAllIfAllowedRole() throws Exception {
        GoodFilter filters = new GoodFilter();
        String json = mapper.writeValueAsString(filters);
        when(goodService.findAllForUser(any()))
                .thenReturn(List.of(new GoodGetForUserDto()));

        mockMvc.perform(get("/goods")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(content().contentType(APPLICATION_JSON))
                // TODO inspect
                .andExpect(jsonPath("$").isArray()) // чисто массив
                .andExpect(status().isOk());
    }

    @Test
    void findAllIfUnauthorized() throws Exception {
        GoodFilter filters = new GoodFilter();
        String json = mapper.writeValueAsString(filters);
        when(goodService.findAllForUser(any()))
                .thenReturn(List.of(new GoodGetForUserDto()));

        mockMvc.perform(get("/goods")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void findAllIfProhibitedRole() throws Exception {
        GoodFilter filters = new GoodFilter();
        String json = mapper.writeValueAsString(filters);
        mockMvc.perform(get("/goods")
                        .contentType(APPLICATION_JSON)
                        .content(json))

                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(role = "MIN_USER")
    void findAllIfAllowedRoleAndProhibitedFilter() throws Exception {
        GoodFilter filters = new GoodFilter();
        filters.setStartCreatedAt(LocalDate.of(2026,1,1));
        filters.setEndCreatedAt(LocalDate.of(2026,1,2));
        String json = mapper.writeValueAsString(filters);

        mockMvc.perform(get("/goods")
                        .contentType(APPLICATION_JSON)
                        .content(json))

                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser(role="ADMIN")
    @Tag("negative")
    void findByIdIfProhibitedRole() throws Exception {
        mockMvc.perform(
                get("/goods/1")
        ).andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(role="MIN_USER")
    @Tag("positive")
    void findByIdIfAllowedRole() throws Exception {
        when(goodService.findForUserById(anyLong()))
                .thenReturn(new GoodGetForUserDto());

        mockMvc.perform(
                get("/goods/1")
        ).andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockCustomUser(role="MIN_USER")
    @Tag("negative")
    void findByIdIfInvalidPathVariable() throws Exception {
        when(goodService.findForUserById(anyLong()))
                .thenReturn(new GoodGetForUserDto());

        mockMvc.perform(
                        get("/goods/-1")
                ).andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockCustomUser(role="ANALYST")
    @Tag("negative")
    void addReviewIfProhibitedRole() throws Exception {
        ReviewCreateDto dto = new ReviewCreateDto();
        dto.setReview("excellent");
        dto.setRate(1);
        String json = mapper.writeValueAsString(dto);
        mockMvc.perform(
                post("/goods/1/review")
                        .content(json)
                        .contentType(APPLICATION_JSON)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(role="MAX_USER")
    @Tag("positive")
    void addReviewIfAllowedRole() throws Exception {
        ReviewCreateDto dto = new ReviewCreateDto();
        dto.setReview("excellent");
        dto.setRate(1);
        String json = mapper.writeValueAsString(dto);

        when(reviewService.createReview(any(), anyString(), anyLong())).thenReturn(null);

        mockMvc.perform(
                post("/goods/1/review")
                        .content(json)
                        .contentType(APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(
                        jsonPath("$.message")
                                .value("Review was added"));
    }

    @Test
    @WithMockCustomUser(role="MAX_USER")
    @Tag("negative")
    void addReviewIfAllowedRoleAndNotCorrectDto() throws Exception {
        ReviewCreateDto dto = new ReviewCreateDto();
        dto.setReview("");

        String json = mapper.writeValueAsString(dto);
        when(reviewService.createReview(any(), anyString(), anyLong())).thenReturn(null);

        mockMvc.perform(
                        post("/goods/1/review")
                                .content(json)
                                .contentType(APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(2)))
                ;
    }

    @Test
    @WithMockCustomUser(role = "MIN_USER")
    @Tag("negative")
    void getReviewsByGoodIdIfRoleAllowedWithoutGoodId() throws Exception {
        ReviewForUserFilters filters = new ReviewForUserFilters();
        String json = mapper.writeValueAsString(filters);
        mockMvc.perform(
                get("/goods/reviews")
                        .contentType(APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.errors[0]").value(containsString("goodId can not be null")));
    }

    @Test
    @WithMockCustomUser(role = "MIN_USER")
    @Tag("positive")
    void getReviewsByGoodIdIfRoleAllowed() throws Exception {
        ReviewForUserFilters filters = new ReviewForUserFilters();
        filters.setGoodId(1L);
        String json = mapper.writeValueAsString(filters);
        mockMvc.perform(
                        get("/goods/reviews")
                                .contentType(APPLICATION_JSON)
                                .content(json)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockCustomUser(role = "ANALYST")
    @Tag("negative")
    void getReviewsByGoodIdIfRoleProhibited() throws Exception {
        ReviewForUserFilters filters = new ReviewForUserFilters();
        filters.setGoodId(1L);
        String json = mapper.writeValueAsString(filters);
        mockMvc.perform(
                        get("/goods/reviews")
                                .contentType(APPLICATION_JSON)
                                .content(json)
                ).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Access Denied")))
                .andExpect(jsonPath("$").isMap());
    }

    @Test
    @WithMockCustomUser(role = "MAX_USER")
    @Tag("positive")
    void favouriteGoodIfRoleAllowed() throws Exception {
        mockMvc.perform(
                post("/goods/1/favourite")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        containsString("Favourite was added")
                ));
    }

    @Test
    @WithMockCustomUser(role = "MODERATOR")
    @Tag("negative")
    void favouriteGoodIfRoleProhibited() throws Exception {
        mockMvc.perform(
                        post("/goods/1/favourite")
                ).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(
                        containsString("Access Denied")
                ));
    }

    @Test
    @WithMockCustomUser(role = "MIN_USER")
    @Tag("negative")
    void favouriteGoodIfRoleAllowedAndGoodIdInvalid() throws Exception {
        mockMvc.perform(
                        post("/goods/-1/favourite")
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        containsString("goodId must be > 0")
                ));
    }

    @Test
    @WithMockCustomUser(role = "MAX_USER")
    @Tag("positive")
    void unFavouriteGoodIfRoleAllowed() throws Exception {
        mockMvc.perform(
                        delete("/goods/1/favourite")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        containsString("Favourite was removed")
                ));
    }

    @Test
    @WithMockCustomUser(role = "MODERATOR")
    @Tag("negative")
    void unFavouriteGoodIfRoleProhibited() throws Exception {
        mockMvc.perform(
                        delete("/goods/1/favourite")
                ).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(
                        containsString("Access Denied")
                ));
    }

    @Test
    @WithMockCustomUser(role = "MIN_USER")
    @Tag("negative")
    void unFavouriteGoodIfRoleAllowedAndGoodIdInvalid() throws Exception {
        mockMvc.perform(
                        delete("/goods/-1/favourite")
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        containsString("goodId must be > 0")
                ));
    }

}


