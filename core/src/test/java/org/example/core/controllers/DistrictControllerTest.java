package org.example.core.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.DistrictDto;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.services.dictionaries.DistrictService;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        DistrictController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class DistrictControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    DistrictService districtService;

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
    void getAllIfAuthorized() throws Exception {

        mockMvc.perform(
                        get("/districts")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("negative")
    void getAllIfUnauthorized() throws Exception {
        mockMvc.perform(
                        get("/districts")
                ).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    private static Stream<Arguments> provideForGetAll() {
        return Stream.of(
                Arguments.of("size", "0"),
                Arguments.of("size", "-1"),
                Arguments.of("page", "-1"),
                Arguments.of("sort", "-10") ,
                Arguments.of("ids", "[]")
        );
    }

    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    @ParameterizedTest
    @MethodSource("provideForGetAll")
    void getAllIfAuthorizedWithInvalidParams(String param, String value) throws Exception {
        mockMvc.perform(
                        get("/districts")
                                .param(param, value)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MIN_USER")
    void getByIdIfAuthorized() throws Exception {
        DistrictDto dto = new DistrictDto(1L, "Central");
        when(districtService.getById(anyLong())).thenReturn(dto);

        mockMvc.perform(
                        get("/districts/1")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Central"));
    }

    @Test
    @Tag("negative")
    void getByIdIfUnauthorized() throws Exception {
        mockMvc.perform(
                        get("/districts/1")
                ).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void deleteIfRoleAllowedWithValidId() throws Exception {
        mockMvc.perform(
                        delete("/districts/1")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("District deleted"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void deleteIfRoleAllowedWithInvalidId() throws Exception {
        mockMvc.perform(
                        delete("/districts/0")
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void deleteIfRoleProhibited() throws Exception {
        mockMvc.perform(
                        delete("/districts/1")
                ).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void deleteIfUnauthorized() throws Exception {
        mockMvc.perform(
                        delete("/districts/1")
                ).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void updateIfRoleAllowedWithValidBody() throws Exception {
        String json = mapper.writeValueAsString(Map.of("name", "New Name"));

        mockMvc.perform(
                        patch("/districts/1")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("District updated"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void updateIfRoleAllowedWithBlankName() throws Exception {
        String json = mapper.writeValueAsString(Map.of("name", "  "));

        mockMvc.perform(
                        patch("/districts/1")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Name must be given"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void updateIfRoleAllowedWithoutNameField() throws Exception {
        String json = mapper.writeValueAsString(Map.of("other", "value"));

        mockMvc.perform(
                        patch("/districts/1")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Name must be given"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void updateIfRoleProhibited() throws Exception {
        String json = mapper.writeValueAsString(Map.of("name", "New Name"));

        mockMvc.perform(
                        patch("/districts/1")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void createIfRoleAllowedWithValidBody() throws Exception {
        DistrictDto dto = new DistrictDto(1L, "Central");
        when(districtService.createDistrict(anyString())).thenReturn(dto);

        String json = mapper.writeValueAsString(Map.of("name", "Central"));

        mockMvc.perform(
                        post("/districts")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Central"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void createIfRoleAllowedWithBlankName() throws Exception {
        String json = mapper.writeValueAsString(Map.of("name", ""));

        mockMvc.perform(
                        post("/districts")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Name must be given"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void createIfRoleAllowedWithoutNameField() throws Exception {
        String json = mapper.writeValueAsString(Map.of("other", "value"));

        mockMvc.perform(
                        post("/districts")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Name must be given"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void createIfRoleProhibited() throws Exception {
        String json = mapper.writeValueAsString(Map.of("name", "Central"));

        mockMvc.perform(
                        post("/districts")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void createIfUnauthorized() throws Exception {
        String json = mapper.writeValueAsString(Map.of("name", "Central"));

        mockMvc.perform(
                        post("/districts")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }
}
