package org.example.core.controllers;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.creating.ShopCreateDto;
import org.example.core.dto.getting.statistics.shops.ShopGetDto;
import org.example.core.dto.patching.ShopPatchDto;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.services.objects.ShopService;
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
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        ShopController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class ShopControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    ShopService shopService;

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
    void getShopsIfAuthorized() throws Exception {
        when(shopService.findAll(any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/shops")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("positive")
    void getShopsIfUnauthorized() throws Exception {
        when(shopService.findAll(any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/shops")
                ).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    private static Stream<Arguments> provideForGetShops() {
        return Stream.of(
                Arguments.of("size", "0"),
                Arguments.of("size", "-1"),
                Arguments.of("page", "-1"),
                Arguments.of("sort", "-10"),
                Arguments.of("districtIds", "[]")
        );
    }

    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    @ParameterizedTest
    @MethodSource("provideForGetShops")
    void getShopsWithInvalidParams(String param, String value) throws Exception {
        mockMvc.perform(
                        get("/shops")
                                .param(param, value)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MIN_USER")
    void getShopByIdIfAuthorizedWithValidId() throws Exception {
        ShopGetDto dto = new ShopGetDto();
        dto.setId(1L);
        when(shopService.findById(anyLong())).thenReturn(dto);

        mockMvc.perform(
                        get("/shops/1")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getShopByIdWithInvalidId() throws Exception {
        mockMvc.perform(
                        get("/shops/0")
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getShopByIdWithNegativeId() throws Exception {
        mockMvc.perform(
                        get("/shops/-1")
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void createShopIfRoleAllowedWithValidDto() throws Exception {
        ShopCreateDto createDto = new ShopCreateDto();
        createDto.setName("Shop One");
        createDto.setAddress("Street 1");
        createDto.setDistrictId(1L);

        ShopGetDto responseDto = new ShopGetDto();
        responseDto.setId(1L);
        responseDto.setName("Shop One");

        when(shopService.create(any(ShopCreateDto.class))).thenReturn(responseDto);

        String json = mapper.writeValueAsString(createDto);

        mockMvc.perform(
                        post("/shops")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Shop One"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void createShopIfRoleAllowedWithInvalidDto() throws Exception {
        ShopCreateDto createDto = new ShopCreateDto(); // пустой dto — нарушает @Valid
        String json = mapper.writeValueAsString(createDto);

        mockMvc.perform(
                        post("/shops")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void createShopIfRoleProhibited() throws Exception {
        ShopCreateDto createDto = new ShopCreateDto();
        createDto.setName("Shop One");
        createDto.setAddress("Street 1");
        createDto.setDistrictId(1L);

        String json = mapper.writeValueAsString(createDto);

        mockMvc.perform(
                        post("/shops")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void createShopIfUnauthorized() throws Exception {
        ShopCreateDto createDto = new ShopCreateDto();
        createDto.setName("Shop One");
        createDto.setAddress("Street 1");
        createDto.setDistrictId(1L);

        String json = mapper.writeValueAsString(createDto);

        mockMvc.perform(
                        post("/shops")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void deleteShopIfRoleAllowedWithValidId() throws Exception {
        mockMvc.perform(
                        delete("/shops/1")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Shop deleted successfully"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void deleteShopIfRoleAllowedWithInvalidId() throws Exception {
        mockMvc.perform(
                        delete("/shops/0")
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void deleteShopIfRoleProhibited() throws Exception {
        mockMvc.perform(
                        delete("/shops/1")
                ).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void deleteShopIfUnauthorized() throws Exception {
        mockMvc.perform(
                        delete("/shops/1")
                ).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void patchShopIfRoleAllowedWithValidDto() throws Exception {
        ShopPatchDto patchDto = new ShopPatchDto();
        patchDto.setId(1L);
        patchDto.setName("Updated Name");

        String json = mapper.writeValueAsString(patchDto);

        mockMvc.perform(
                        patch("/shops")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Shop was updated successfully"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void patchShopIfRoleAllowedWithAllNullFields() throws Exception {
        ShopPatchDto patchDto = new ShopPatchDto();
        patchDto.setId(1L);
        // name, address, districtId — все null

        String json = mapper.writeValueAsString(patchDto);

        mockMvc.perform(
                        patch("/shops")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Anything must be given"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void patchShopIfRoleAllowedWithInvalidDto() throws Exception {
        ShopPatchDto patchDto = new ShopPatchDto();
        // пустой dto — нарушает @Valid

        String json = mapper.writeValueAsString(patchDto);

        mockMvc.perform(
                        patch("/shops")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void patchShopIfRoleProhibited() throws Exception {
        ShopPatchDto patchDto = new ShopPatchDto();
        patchDto.setId(1L);
        patchDto.setName("Updated Name");

        String json = mapper.writeValueAsString(patchDto);

        mockMvc.perform(
                        patch("/shops")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void patchShopIfUnauthorized() throws Exception {
        ShopPatchDto patchDto = new ShopPatchDto();
        patchDto.setId(1L);
        patchDto.setName("Updated Name");

        String json = mapper.writeValueAsString(patchDto);

        mockMvc.perform(
                        patch("/shops")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }
}
