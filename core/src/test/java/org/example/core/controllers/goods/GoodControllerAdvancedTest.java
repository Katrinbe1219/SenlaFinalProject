package org.example.core.controllers.goods;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.creating.GoodCreateDto;
import org.example.core.dto.creating.ModeratorLogCreateDto;
import org.example.core.dto.getting.goods.GoodGetFullDto;
import org.example.core.dto.getting.goods.GoodIdDto;
import org.example.core.dto.patching.GoodPatchDto;
import org.example.core.exceptions.GlobalExceptionHandler;

import org.example.core.hibernate.base_settings.filters.goods.GoodFilter;
import org.example.core.models.types.ModeratorVerdict;
import org.example.core.services.documents.ModeratorRecalcService;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        GoodControllerAdvanced.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class GoodControllerAdvancedTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    GoodService goodService;

    @Autowired
    ModeratorRecalcService logService;

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
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void findAllIfRoleAllowedWithInvalidBody() throws Exception {
        GoodFilter filter = new GoodFilter();
        filter.setTagIds(List.of());

        filter.setEndCreatedAt(LocalDate.of(2025,2,2));
        filter.setCurCreatedAt(LocalDate.of(2025,2,2));

        filter.setCurRating(-19D);

        when(goodService.findAllForAnalyst(any())).thenReturn(List.of());

        mockMvc.perform(get("/goods/advanced")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filter)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void findAllIfRoleAllowedWithValidBody() throws Exception {
        GoodFilter filter = new GoodFilter();
        filter.setTagIds(List.of(1L));

        filter.setEndCreatedAt(LocalDate.of(2025,2,2));
        filter.setStartCreatedAt(LocalDate.of(2025,1,2));

        mockMvc.perform(get("/goods/advanced")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void findAllIfRoleProhibited() throws Exception {
        GoodFilter filter = new GoodFilter();

        mockMvc.perform(get("/goods/advanced")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filter)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void findAllIfUnauthorized() throws Exception {
        GoodFilter filter = new GoodFilter();

        mockMvc.perform(get("/goods/advanced")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filter)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ANALYST")
    void findByIdIfRoleAllowedWithValidId() throws Exception {
        GoodGetFullDto dto = new GoodGetFullDto();
        when(goodService.getFullById(anyLong())).thenReturn(dto);

        mockMvc.perform(get("/goods/advanced/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void findByIdIfRoleAllowedWithInvalidId() throws Exception {
        mockMvc.perform(get("/goods/advanced/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void findByIdIfRoleProhibited() throws Exception {
        mockMvc.perform(get("/goods/advanced/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void findByIdIfUnauthorized() throws Exception {
        mockMvc.perform(get("/goods/advanced/1"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void createGoodIfRoleAllowedWithValidDto() throws Exception {
        GoodCreateDto dto = new GoodCreateDto();
        dto.setName("name");
        dto.setCategoryId(1L);
        dto.setUnitId(2L);
        dto.setTagIds(List.of(1L));
        dto.setDescription("description ");
        when(goodService.createGood(any())).thenReturn(new GoodIdDto());

        mockMvc.perform(post("/goods/advanced")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void createGoodIfRoleAllowedWithInvalidDto() throws Exception {
        GoodCreateDto dto = new GoodCreateDto();

        dto.setCategoryId(0L);

        dto.setTagIds(List.of());
        dto.setDescription("");

        mockMvc.perform(post("/goods/advanced")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(5)));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void createGoodIfRoleProhibited() throws Exception {
        GoodCreateDto dto = new GoodCreateDto();
        dto.setName("name");
        dto.setCategoryId(1L);
        dto.setUnitId(2L);

        mockMvc.perform(post("/goods/advanced")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void createGoodIfUnauthorized() throws Exception {
        GoodCreateDto dto = new GoodCreateDto();
        dto.setName("name");
        dto.setCategoryId(1L);
        dto.setUnitId(2L);

        mockMvc.perform(post("/goods/advanced")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void deleteIfRoleAllowedWithValidId() throws Exception {
        mockMvc.perform(delete("/goods/advanced/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Good was deleted"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void deleteIfRoleAllowedWithInvalidId() throws Exception {
        mockMvc.perform(delete("/goods/advanced/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void deleteIfRoleProhibited() throws Exception {
        mockMvc.perform(delete("/goods/advanced/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void deleteIfUnauthorized() throws Exception {
        mockMvc.perform(delete("/goods/advanced/1"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void patchIfRoleAllowedWithValidDto() throws Exception {
        GoodPatchDto dto = new GoodPatchDto();
        dto.setId(1L);
        dto.setName("only letters");
        dto.setCategoryId(2L);

        mockMvc.perform(patch("/goods/advanced/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Good was updated"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void patchIfRoleAllowedWithInvalidDto() throws Exception {
        GoodPatchDto dto = new GoodPatchDto();

        dto.setName("123#$");
        dto.setCategoryId(0L);
        dto.setUnitId(-1L);

        mockMvc.perform(patch("/goods/advanced/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void patchIfRoleAllowedWithInvalidId() throws Exception {
        GoodPatchDto dto = new GoodPatchDto();
        dto.setId(1L);
        dto.setName("only letters");

        mockMvc.perform(patch("/goods/advanced/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void patchIfRoleProhibited() throws Exception {
        GoodPatchDto dto = new GoodPatchDto();
        dto.setId(1L);
        dto.setName("only letters");

        mockMvc.perform(patch("/goods/advanced/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void patchIfUnauthorized() throws Exception {
        GoodPatchDto dto = new GoodPatchDto();
        dto.setId(1L);
        dto.setName("only letters");

        mockMvc.perform(patch("/goods/advanced/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }



    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void blockIfRoleAllowedWithInvalidId() throws Exception {
        ModeratorLogCreateDto dto = new ModeratorLogCreateDto();
        dto.setComment("comment");

        mockMvc.perform(patch("/goods/advanced/0/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void blockIfRoleProhibited() throws Exception {
        ModeratorLogCreateDto dto = new ModeratorLogCreateDto();
        dto.setVerdict(ModeratorVerdict.RECALCULATED);
        dto.setComment("comment");
        mockMvc.perform(patch("/goods/advanced/1/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void unblockIfRoleAllowedWithValidDto() throws Exception {
        ModeratorLogCreateDto dto = new ModeratorLogCreateDto();
        dto.setComment("comment");
        dto.setVerdict(ModeratorVerdict.RECALCULATED);
        mockMvc.perform(delete("/goods/advanced/1/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Good was unblocked"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void unblockIfRoleAllowedWithInvalidDto() throws Exception {
        ModeratorLogCreateDto dto = new ModeratorLogCreateDto();


        mockMvc.perform(delete("/goods/advanced/1/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void unblockIfRoleAllowedWithInvalidId() throws Exception {
        ModeratorLogCreateDto dto = new ModeratorLogCreateDto();
        dto.setComment("comment");

        mockMvc.perform(delete("/goods/advanced/0/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void unblockIfRoleProhibited() throws Exception {
        ModeratorLogCreateDto dto = new ModeratorLogCreateDto();
        dto.setComment("comment");

        mockMvc.perform(delete("/goods/advanced/1/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void unblockIfUnauthorized() throws Exception {
        ModeratorLogCreateDto dto = new ModeratorLogCreateDto();
        dto.setComment("comment");

        mockMvc.perform(delete("/goods/advanced/1/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
