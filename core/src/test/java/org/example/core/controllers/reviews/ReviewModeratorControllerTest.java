package org.example.core.controllers.reviews;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;

import org.example.core.dto.getting.reviews.ReviewFullDto;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.hibernate.base_settings.filters.reviews.ReviewAdvancedFilters;
import org.example.core.services.documents.reviews.ReviewAdvancedService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        ReviewModeratorController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class ReviewModeratorControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ReviewAdvancedService reviewService;

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

    // ==================== GET /moderator/reviews/{id} ====================

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void getReviewByIdIfModeratorWithValidId() throws Exception {
        ReviewFullDto dto = new ReviewFullDto();
        dto.setId(10L);
        dto.setReview("Great product");
        when(reviewService.getReviewById(10L)).thenReturn(dto);

        mockMvc.perform(get("/moderator/reviews/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.review").value("Great product"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void getReviewByIdIfModeratorWithInvalidId() throws Exception {
        mockMvc.perform(get("/moderator/reviews/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }



    @Test
    @Tag("negative")
    void getReviewByIdIfUnauthorized() throws Exception {
        mockMvc.perform(get("/moderator/reviews/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getReviewByIdIfRoleProhibited() throws Exception {
        mockMvc.perform(get("/moderator/reviews/1"))
                .andExpect(status().isForbidden());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void blockReviewByIdIfModeratorWithValidId() throws Exception {

        mockMvc.perform(patch("/moderator/reviews/1/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review blocked"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void blockReviewByIdIfModeratorWithInvalidId() throws Exception {
        mockMvc.perform(patch("/moderator/reviews/-10/block"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }


    @Test
    @Tag("negative")
    void blockReviewByIdIfUnauthorized() throws Exception {
        mockMvc.perform(patch("/moderator/reviews/1/block"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void blockReviewByIdIfRoleProhibited() throws Exception {
        mockMvc.perform(patch("/moderator/reviews/1/block"))
                .andExpect(status().isForbidden());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void unblockReviewByIdIfModeratorWithValidId() throws Exception {
        mockMvc.perform(delete("/moderator/reviews/1/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review unblocked"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void unblockReviewByIdIfModeratorWithInvalidId() throws Exception {
        mockMvc.perform(delete("/moderator/reviews/0/block"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }


    @Test
    @Tag("negative")
    void unblockReviewByIdIfUnauthorized() throws Exception {
        mockMvc.perform(delete("/moderator/reviews/1/block"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void unblockReviewByIdIfRoleProhibited() throws Exception {
        mockMvc.perform(delete("/moderator/reviews/1/block"))
                .andExpect(status().isForbidden());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void getAllReviewsIfModeratorWithValidFilters() throws Exception {
        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();
        List<ReviewFullDto> expected = List.of(new ReviewFullDto(), new ReviewFullDto());
        when(reviewService.getByFilters(any(ReviewAdvancedFilters.class))).thenReturn(expected);

        mockMvc.perform(get("/moderator/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @Tag("negative")
    void getAllReviewsIfUnauthorized() throws Exception {
        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();
        mockMvc.perform(get("/moderator/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getAllReviewsIfRoleProhibited() throws Exception {
        ReviewAdvancedFilters filters = new ReviewAdvancedFilters();
        mockMvc.perform(get("/moderator/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isForbidden());
    }

     @Test
     @Tag("negative")
     @WithMockCustomUser(role = "MODERATOR")
     void getAllReviewsWithInvalidFilters() throws Exception {
         ReviewAdvancedFilters filters = new ReviewAdvancedFilters();
        filters.setPage(-1);
        filters.setCreatedAt(LocalDate.of(2024,12,13));
        filters.setStartDate(LocalDate.of(2024,12,13));
         mockMvc.perform(
                 get("/moderator/reviews")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(mapper.writeValueAsString(filters))
                 )
         .
                 andExpect(status().isBadRequest())
                 .andExpect(jsonPath("$.errors").exists())
                 .andExpect(jsonPath("$.errors", hasSize(2)));
     }
}
