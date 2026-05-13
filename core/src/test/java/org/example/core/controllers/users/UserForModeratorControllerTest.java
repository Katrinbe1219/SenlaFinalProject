package org.example.core.controllers.users;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.getting.users.UserFullDto;
import org.example.core.exceptions.GlobalExceptionHandler;

import org.example.core.hibernate.base_settings.filters.users.UserAdvancedFilter;
import org.example.core.models.types.RoleTypes;
import org.example.core.services.objects.UserService;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        UserForModeratorController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class UserForModeratorControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    UserService userService;

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
    void upgradeIfRoleAllowedWithValidId() throws Exception {
        mockMvc.perform(patch("/moderator/users/upgrade/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Upgraded successfully"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void upgradeIfRoleAllowedWithInvalidId() throws Exception {
        mockMvc.perform(patch("/moderator/users/upgrade/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("User Id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void upgradeIfRoleAllowedWithNegativeId() throws Exception {
        mockMvc.perform(patch("/moderator/users/upgrade/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("User Id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void upgradeIfRoleProhibited() throws Exception {
        mockMvc.perform(patch("/moderator/users/upgrade/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void upgradeIfUnauthorized() throws Exception {
        mockMvc.perform(patch("/moderator/users/upgrade/1"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void deUpgradeIfRoleAllowedWithValidId() throws Exception {
        mockMvc.perform(delete("/moderator/users/upgrade/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Deleted successfully"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void deUpgradeIfRoleAllowedWithInvalidId() throws Exception {
        mockMvc.perform(delete("/moderator/users/upgrade/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("User Id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void deUpgradeIfRoleProhibited() throws Exception {
        mockMvc.perform(delete("/moderator/users/upgrade/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void deUpgradeIfUnauthorized() throws Exception {
        mockMvc.perform(delete("/moderator/users/upgrade/1"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void lockUserIfRoleAllowedWithValidId() throws Exception {
        mockMvc.perform(patch("/moderator/users/lock/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Locked successfully"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void lockUserIfRoleAllowedWithInvalidId() throws Exception {
        mockMvc.perform(patch("/moderator/users/lock/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("User Id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void lockUserIfRoleProhibited() throws Exception {
        mockMvc.perform(patch("/moderator/users/lock/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void lockUserIfUnauthorized() throws Exception {
        mockMvc.perform(patch("/moderator/users/lock/1"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void unlockUserIfRoleAllowedWithValidId() throws Exception {
        mockMvc.perform(delete("/moderator/users/lock/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Unlocked successfully"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void unlockUserIfRoleAllowedWithInvalidId() throws Exception {
        mockMvc.perform(delete("/moderator/users/lock/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("User Id must be > 0"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void unlockUserIfRoleProhibited() throws Exception {
        mockMvc.perform(delete("/moderator/users/lock/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void unlockUserIfUnauthorized() throws Exception {
        mockMvc.perform(delete("/moderator/users/lock/1"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void getAllUsersIfRoleAllowedWithValidBody() throws Exception {
        UserAdvancedFilter filters = new UserAdvancedFilter();
        filters.setRoleType(RoleTypes.MIN_USER);

        when(userService.getAllUsers(any(), anyBoolean())).thenReturn(List.of());

        mockMvc.perform(get("/moderator/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void getAllUsersIfRoleAllowedWithMaxUserFilter() throws Exception {
        UserAdvancedFilter filters = new UserAdvancedFilter();
        filters.setRoleType(RoleTypes.MAX_USER);

        when(userService.getAllUsers(any(), anyBoolean())).thenReturn(List.of(new UserFullDto()));

        mockMvc.perform(get("/moderator/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void getAllUsersIfRoleAllowedWithProhibitedRoleFilter() throws Exception {
        UserAdvancedFilter filters = new UserAdvancedFilter();
        filters.setRoleType(RoleTypes.ADMIN);

        mockMvc.perform(get("/moderator/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message")
                        .value("Your are not allowed to get these users"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void getAllUsersIfRoleAllowedWithInvalidBody() throws Exception {
        UserAdvancedFilter filters = new UserAdvancedFilter();
        filters.setLocked(true);
        filters.setNonLocked(true);

        filters.setStartCreatedAt(LocalDate.of(2027,1,2));
        filters.setEndCreatedAt(LocalDate.of(2025,1,2));

        mockMvc.perform(get("/moderator/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(2)));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getAllUsersIfRoleProhibited() throws Exception {
        UserAdvancedFilter filters = new UserAdvancedFilter();
        filters.setRoleType(RoleTypes.MIN_USER);

        mockMvc.perform(get("/moderator/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }

    @Test
    @Tag("negative")
    void getAllUsersIfUnauthorized() throws Exception {
        UserAdvancedFilter filters = new UserAdvancedFilter();
        filters.setRoleType(RoleTypes.MIN_USER);

        mockMvc.perform(get("/moderator/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isUnauthorized());
    }
}
