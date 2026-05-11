package org.example.core.controllers.favourites;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.controllers.goods.GoodControllerAdvanced;
import org.example.core.dto.getting.favourites.FavouriteGetForUserDto;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.services.documents.FavouriteService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        FavouriteForUserController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class FavouriteForUserControllerTest {
    @Autowired
    WebApplicationContext context;
    private static ObjectMapper mapper = new ObjectMapper();

    MockMvc mockMvc;

    @Autowired
    FavouriteService favouriteService;

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
    @WithMockCustomUser(role="MAX_USER")
    void findAllIfRoleAllowed() throws Exception {
        FavouriteGetForUserDto dto = new FavouriteGetForUserDto();
        dto.setGoodId(1L);
        dto.setGoodName("name");

        when(favouriteService.getAllForUser(any()))
                .thenReturn(List.of(dto));
        mockMvc.perform(
                get("/favourites")
        )
                .andExpect(status().isOk())

                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].goodId").value(1L));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role="ANALYST")
    void findAllIfRoleProhibited() throws Exception {
        FavouriteGetForUserDto dto = new FavouriteGetForUserDto();
        dto.setGoodId(1L);
        dto.setGoodName("name");

        when(favouriteService.getAllForUser(anyString()))
                .thenReturn(List.of(dto));
        mockMvc.perform(
                        get("/favourites")
                ).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void findAllIfUnauthorized() throws Exception {
        FavouriteGetForUserDto dto = new FavouriteGetForUserDto();
        dto.setGoodId(1L);
        dto.setGoodName("name");

        when(favouriteService.getAllForUser(anyString()))
                .thenReturn(List.of(dto));
        mockMvc.perform(
                        get("/favourites")
                ).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }
}
