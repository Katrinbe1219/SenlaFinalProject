package org.example.core.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;

import org.example.core.dto.TagDto;
import org.example.core.exceptions.DoesNoeExist;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.services.dictionaries.TagService;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        TagController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class TagControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    TagService tagService;

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
    void getAllTagsIfUnauthorized() throws Exception {
        mockMvc.perform(get("/tags"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MIN_USER")
    void getAllTagsIfAuthorized() throws Exception {
        TagDto dto = new TagDto();
        dto.setId(1L);
        dto.setName("test");
        when(tagService.getAllTags(eq(10), eq(0), any(), isNull()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("test"));
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void getAllTagsIfAuthorizedValidParams() throws Exception {
        when(tagService.getAllTags(eq(5), eq(2), any(), anyList()))
                .thenReturn(List.of());

        mockMvc.perform(get("/tags")
                        .param("count", "5")
                        .param("page", "2")
                        .param("sort", "2")
                        .param("ids", "1", "2", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private static Stream<Arguments> provideForGetTagsNegative() {
        return Stream.of(
                Arguments.of("count", "0", "Count must be greater than 0"),
                Arguments.of("count", "-5", "Count must be greater than 0"),
                Arguments.of("page", "-1", "Page must be >= 0"),
                Arguments.of("page", "-100", "Page must be >= 0"),
                Arguments.of("ids", "", "ids length must be > 0")   // пустой список ids
        );
    }

    @ParameterizedTest
    @MethodSource("provideForGetTagsNegative")
    @Tag("negative")
    @WithMockCustomUser(role = "MAX_USER")
    void getAllTagsIfAuthorizedWithInvalidParams(String param, String value, String expectedMessage) throws Exception {
        mockMvc.perform(get("/tags")
                        .param(param, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }



    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MAX_USER")
    void getTagByIdSuccess() throws Exception {
        TagDto dto = new TagDto();
        dto.setId(10L);
        dto.setName("existing");
        when(tagService.getTagById(10L)).thenReturn(dto);

        mockMvc.perform(get("/tags/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("existing"));
    }



    @Test
    @Tag("negative")
    void getTagByIdIfUnauthorized() throws Exception {
        when(tagService.getTagById(99L)).thenThrow(new RuntimeException("Tag not found"));
        mockMvc.perform(get("/tags/99"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MAX_USER")
    void getTagByIdIfVariableIsInvalid() throws Exception {
        mockMvc.perform(get("/tags/-99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));;
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void addTagIfRoleAllowedWIthValidBody() throws Exception {
        Map<String, String> body = Map.of("name", "NewTag");
        TagDto response = new TagDto();
        response.setId(5L);
        response.setName("NewTag");
        when(tagService.createTag("NewTag")).thenReturn(response);

        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("NewTag"));
    }


    @Test
    @Tag("negative")
    void addTagIfUnauthorized() throws Exception {
        Map<String, String> body = Map.of("name", "Any");
        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void addTagIfRoleProhibited() throws Exception {
        Map<String, String> body = Map.of("name", "Any");
        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void addTagIfMissingName() throws Exception {
        Map<String, String> body = Map.of();
        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Name must be given"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void addTagIfNullName() throws Exception {
        Map<String, String> body = Map.of("name",  "");
        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Name must be given"));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void deleteTagIfSuccess() throws Exception {
        doNothing().when(tagService).deleteTag(1L);
        mockMvc.perform(delete("/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tag deleted"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void deleteTagIfNotFound() throws Exception {
        doThrow(new DoesNoeExist("test")).when(tagService).deleteTag(999L);
        mockMvc.perform(delete("/tags/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void deleteTagIfVariableIsInvalid() throws Exception {

        mockMvc.perform(delete("/tags/-999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));;
    }

    @Test
    @Tag("negative")
    void deleteTagIfUnauthorized() throws Exception {
        mockMvc.perform(delete("/tags/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void deleteTagIfRoleProhibited() throws Exception {
        mockMvc.perform(delete("/tags/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void editTagIfSuccess() throws Exception {
        Map<String, String> body = Map.of("name", "UpdatedName");
        doNothing().when(tagService).editTag(any(TagDto.class));

        mockMvc.perform(patch("/tags/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tag updated"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void editTagIfEmptyName() throws Exception {
        Map<String, String> body = Map.of("name", "");
        mockMvc.perform(patch("/tags/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name must be given"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void editTagIfMissingName() throws Exception {
        Map<String, String> body = Map.of();
        mockMvc.perform(patch("/tags/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name must be given"));
    }

    @Test
    @Tag("negative")
    void editTagIfUnauthorized() throws Exception {
        Map<String, String> body = Map.of("name", "New");
        mockMvc.perform(patch("/tags/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void editTagIfRoleProhibited() throws Exception {
        Map<String, String> body = Map.of("name", "New");
        mockMvc.perform(patch("/tags/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }
}
