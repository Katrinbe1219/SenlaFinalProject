package org.example.core.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.creating.CategoryCreateDto;
import org.example.core.dto.getting.categories.CategoryGetDto;
import org.example.core.dto.patching.CategoryPatchDto;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.services.dictionaries.CategoryService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {SecurityConfiguration.class,
        CategoryController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class CategoryControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    CategoryService categoryService;

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
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void getAllIfAuthorized() throws Exception {
        mockMvc.perform(
                get("/categories")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("negative")
    void getAllIfUnauthorized() throws Exception {
        mockMvc.perform(
                get("/categories")
        ).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }


    private static Stream<Arguments> provideForgetAll(){
        return Stream.of(
                Arguments.of("size", "0"),
                Arguments.of("page", "-1"),
                Arguments.of("ids", "[]"),
                Arguments.of("sort", "20")
        );
    }

    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    @ParameterizedTest
    @MethodSource("provideForgetAll")
    void getAllIfAuthorizedWithInvalidSizeParam(String param, String value) throws Exception {
        mockMvc.perform(
                        get("/categories")
                                .param(param, value)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .exists());
    }


    @Tag("negative")
    @Test
    @WithMockCustomUser(role = "MAX_USER")
    void createCategoryIfRoleIsProhibited() throws Exception {
        CategoryCreateDto dto  = new CategoryCreateDto();
        dto.setName("name");

        String json = mapper.writeValueAsString(dto);
        mockMvc.perform(
                post("/categories")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Tag("negative")
    @Test
    @WithMockCustomUser(role = "ADMIN")
    void createCategoryIfRoleAllowedWithInvalidDto() throws Exception {
        CategoryCreateDto dto  = new CategoryCreateDto();
        dto.setParentId(-1L);
        String json = mapper.writeValueAsString(dto);
        mockMvc.perform(
                        post("/categories")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(2)));
    }

    @Tag("positive")
    @Test
    @WithMockCustomUser(role = "ADMIN")
    void createCategoryIfRoleAllowedWithValidDto() throws Exception {
        CategoryCreateDto dto  = new CategoryCreateDto();
        dto.setParentId(2L);
        dto.setName("name");
        String json = mapper.writeValueAsString(dto);
        when(categoryService.createCategory(any(CategoryCreateDto.class)))
                .thenReturn(new CategoryGetDto());

        mockMvc.perform(
                        post("/categories")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )

                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void deleteCategoryIfRoleAllowedWithValidVariable() throws Exception {
        mockMvc.perform(
                delete("/categories/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Category deleted"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void deleteCategoryIfRoleAllowedWithInvalidVariable() throws Exception {
        mockMvc.perform(
                        delete("/categories/0")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void deleteCategoryIfRoleProhibited() throws Exception {
        mockMvc.perform(
                        delete("/categories/0")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void editCategoryIfRoleAllowedWithValidDto() throws Exception {
        CategoryPatchDto dto = new CategoryPatchDto();
        dto.setName("name");
        dto.setId(1L);

        String json = mapper.writeValueAsString(dto);
        mockMvc.perform(
                patch("/categories")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Category updated"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void editCategoryIfRoleAllowedWithInvalidDto() throws Exception {
        CategoryPatchDto dto = new CategoryPatchDto();
        dto.setName("");
        dto.setId(0L);

        String json = mapper.writeValueAsString(dto);
        mockMvc.perform(
                        patch("/categories")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors",hasSize(2)));
    }


    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void editCategoryIfRoleProhibited() throws Exception {
        CategoryPatchDto dto = new CategoryPatchDto();
        dto.setName("name");
        dto.setId(1L);

        String json = mapper.writeValueAsString(dto);
        mockMvc.perform(
                        patch("/categories")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message")
                        .value("Access Denied"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role="ANALYST")
    void getCategoryIfAuthorizedAndValidVariable() throws Exception {
        CategoryGetDto dto = new CategoryGetDto();
        dto.setId(1L);
        when(categoryService.getById(anyLong())).thenReturn(dto);

        mockMvc.perform(
                get("/categories/1")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());

    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role="ANALYST")
    void getCategoryIfAuthorizedAndInvalidVariable() throws Exception {
        CategoryGetDto dto = new CategoryGetDto();
        dto.setId(1L);
        when(categoryService.getById(anyLong())).thenReturn(dto);

        mockMvc.perform(
                        get("/categories/0")
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));

    }

    @Test
    @Tag("negative")
    void getCategoryIfUnauthorized() throws Exception {
        mockMvc.perform(
                        get("/categories/1")
                ).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());

    }


}
