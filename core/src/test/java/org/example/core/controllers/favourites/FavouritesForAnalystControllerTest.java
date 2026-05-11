package org.example.core.controllers.favourites;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.getting.favourites.FavouriteCountByGoodDto;
import org.example.core.dto.getting.favourites.FavouriteFullDto;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.hibernate.base_settings.filters.FavouritesAnalystFilters;
import org.example.core.services.documents.FavouriteService;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        FavouritesForAnalystController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class FavouritesForAnalystControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    FavouriteService favouriteService;

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
    void findAllIfRoleAllowedWithValidBody() throws Exception {
        FavouritesAnalystFilters filters = new FavouritesAnalystFilters();
        filters.setGoodIds(List.of(1L,2L));

        when(favouriteService.findAllForAnalyst(any()))
                .thenReturn(List.of(new FavouriteFullDto()));

        mockMvc.perform(get("/analyst/favourites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void findAllIfRoleAllowedWithInvalidBody() throws Exception {
        FavouritesAnalystFilters filters = new FavouritesAnalystFilters();
        filters.setGoodIds(List.of());
        filters.setCategoryIds(List.of());
        filters.setTagIds(List.of());

        mockMvc.perform(get("/analyst/favourites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void findAllIfRoleProhibited() throws Exception {
        FavouritesAnalystFilters filters = new FavouritesAnalystFilters();

        mockMvc.perform(get("/analyst/favourites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void findAllIfUnauthorized() throws Exception {
        FavouritesAnalystFilters filters = new FavouritesAnalystFilters();

        mockMvc.perform(get("/analyst/favourites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getCountByAllGoodsIfRoleAllowedWithValidBody() throws Exception {
        FavouritesAnalystFilters filters = new FavouritesAnalystFilters();

        when(favouriteService.countAllByGoodId(any()))
                .thenReturn(List.of(new FavouriteCountByGoodDto()));

        mockMvc.perform(get("/analyst/favourites/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getCountByAllGoodsIfRoleAllowedWithInvalidBody() throws Exception {
        FavouritesAnalystFilters filters = new FavouritesAnalystFilters();
       filters.setGoodIds(List.of());

        mockMvc.perform(get("/analyst/favourites/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(1)));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getCountByAllGoodsIfRoleProhibited() throws Exception {
        FavouritesAnalystFilters filters = new FavouritesAnalystFilters();

        mockMvc.perform(get("/analyst/favourites/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void getCountByAllGoodsIfUnauthorized() throws Exception {
        FavouritesAnalystFilters filters = new FavouritesAnalystFilters();

        mockMvc.perform(get("/analyst/favourites/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void getCountByGoodIdIfRoleAllowedWithValidId() throws Exception {
        FavouriteCountByGoodDto dto = new FavouriteCountByGoodDto();
        when(favouriteService.countOneByGoodId(anyLong())).thenReturn(dto);

        mockMvc.perform(get("/analyst/favourites/count/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getCountByGoodIdIfRoleAllowedWithInvalidId() throws Exception {
        mockMvc.perform(get("/analyst/favourites/count/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void getCountByGoodIdIfRoleAllowedWithNegativeId() throws Exception {
        mockMvc.perform(get("/analyst/favourites/count/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getCountByGoodIdIfRoleProhibited() throws Exception {
        mockMvc.perform(get("/analyst/favourites/count/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void getCountByGoodIdIfUnauthorized() throws Exception {
        mockMvc.perform(get("/analyst/favourites/count/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }
}
