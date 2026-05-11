package org.example.core.controllers.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.creating.ManyModeratorLogCreateDto;
import org.example.core.dto.creating.ModeratorLogCreateDto;
import org.example.core.dto.getting.ModeratorRecalcDto;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.hibernate.base_settings.filters.ModeratorRecalcFilter;
import org.example.core.models.types.ModeratorVerdict;
import org.example.core.services.documents.ModeratorRecalcService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        ModeratorRecalculationController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class ModeratorRecalculationControllerTest {
    @Autowired
    WebApplicationContext context;

    @Autowired
    ModeratorRecalcService service;
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
    @WithMockCustomUser(role = "MODERATOR")
    void findAllIfValidDto() throws Exception {
        ModeratorRecalcDto dto = new ModeratorRecalcDto();
        dto.setId(1L);
        ModeratorRecalcDto dto1 = new ModeratorRecalcDto();
        dto1.setId(2L);

        ModeratorRecalcFilter filters = new ModeratorRecalcFilter();
        when(service.findAllFullVersion(any())).thenReturn(
                List.of(dto, dto1)
        );
        mockMvc.perform(
                get("/moderator/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void findAllIfInvalidDto() throws Exception {
        ModeratorRecalcFilter filters = new ModeratorRecalcFilter();
        filters.setGoodId(-1L);
        filters.setGoodIds(List.of());
        filters.setPage(-1);
        filters.setStartDate(LocalDate.of(2027,1,1));
        filters.setEndDate(LocalDate.of(2024,1,2));
        mockMvc.perform(
                get("/moderator/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters))
        ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(5)));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void findAllIfRoleProhibited() throws Exception {
        ModeratorRecalcFilter filters = new ModeratorRecalcFilter();

        mockMvc.perform(
                get("/moderator/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters))
        ).andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ANALYST")
    void changeStatusIfRoleProhibited() throws Exception {
        ManyModeratorLogCreateDto filters = new ManyModeratorLogCreateDto();

        ModeratorLogCreateDto dto = new ModeratorLogCreateDto();
        dto.setComment("comment");
        dto.setVerdict(ModeratorVerdict.SUSPICIOUS);
        dto.setGoodId(1L);

        filters.setVerdicts(List.of(dto));
        mockMvc.perform(
                        get("/moderator/check/change-status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(filters))
                ).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void changeStatusIfInvalidDto() throws Exception {
        ManyModeratorLogCreateDto filters = new ManyModeratorLogCreateDto();

        mockMvc.perform(
                        patch("/moderator/check/change-status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(filters))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(1)));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void changeStatusForManyIfValidDto() throws Exception {
        ManyModeratorLogCreateDto filters = new ManyModeratorLogCreateDto();

        ModeratorLogCreateDto dto = new ModeratorLogCreateDto();
        dto.setComment("comment");
        dto.setVerdict(ModeratorVerdict.SUSPICIOUS);
        dto.setGoodId(1L);

        filters.setVerdicts(List.of(dto));


        mockMvc.perform(
                patch("/moderator/check/change-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters))

        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Goods' statuses were changed"));
    }

}
