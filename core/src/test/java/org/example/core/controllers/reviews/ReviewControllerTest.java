package org.example.core.controllers.reviews;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.getting.reviews.ReviewDto;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.services.documents.reviews.ReviewForUserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        ReviewController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class ReviewControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    ReviewForUserService reviewService;

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
    @WithMockCustomUser(role = "MIN_USER")
    void getReviewsByUserIfAuthorizedWithValidParams() throws Exception {
        when(reviewService.getByUserId(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(new ReviewDto()));

        mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("negative")
    void getReviewsByUserIfUnauthorized() throws Exception {
        mockMvc.perform(get("/reviews"))
                .andExpect(status().isUnauthorized());
    }

    private static Stream<Arguments> provideForGetReviews() {
        return Stream.of(
                Arguments.of("count", "0"),
                Arguments.of("count", "-1"),
                Arguments.of("page", "-1")
        );
    }

    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    @ParameterizedTest
    @MethodSource("provideForGetReviews")
    void getReviewsByUserIfAuthorizedWithInvalidParams(String param, String value) throws Exception {
        mockMvc.perform(get("/reviews")
                        .param(param, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MIN_USER")
    void getReviewByUserAndGoodIfAuthorizedWithValidId() throws Exception {
        ReviewDto dto = new ReviewDto();
        when(reviewService.getByUserAndGood(anyString(), anyLong()))
                .thenReturn(dto);

        mockMvc.perform(get("/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getReviewByUserAndGoodIfAuthorizedWithInvalidId() throws Exception {
        mockMvc.perform(get("/reviews/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be >0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getReviewByUserAndGoodIfAuthorizedWithNegativeId() throws Exception {
        mockMvc.perform(get("/reviews/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be >0"));
    }

    @Test
    @Tag("negative")
    void getReviewByUserAndGoodIfUnauthorized() throws Exception {
        mockMvc.perform(get("/reviews/1"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MIN_USER")
    void deleteReviewIfAuthorizedWithValidId() throws Exception {
        mockMvc.perform(delete("/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Review was deleted"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void deleteReviewIfAuthorizedWithInvalidId() throws Exception {
        mockMvc.perform(delete("/reviews/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be >0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void deleteReviewIfAuthorizedWithNegativeId() throws Exception {
        mockMvc.perform(delete("/reviews/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be >0"));
    }

    @Test
    @Tag("negative")
    void deleteReviewIfUnauthorized() throws Exception {
        mockMvc.perform(delete("/reviews/1"))
                .andExpect(status().isUnauthorized());
    }
}
