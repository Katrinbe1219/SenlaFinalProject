package org.example.core.controllers.users;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.getting.ProfileDto;
import org.example.core.dto.patching.UpdateUserPasswordDto;
import org.example.core.dto.patching.UserDefaultPatchDto;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.mapping.users.ProfileDtoMapper;
import org.example.core.security.DeviceInfoExtractor;
import org.example.core.security.TokenPair;

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

import java.time.Clock;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        ProfileController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class ProfileControllerTest {
    @Autowired
    ProfileDtoMapper profileDtoMapper;

    @Autowired
    WebApplicationContext context;

    @Autowired
    UserService userService;

    @Autowired
    DeviceInfoExtractor extractor;

    @Autowired
    Clock clock;

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
    void getProfileIfAuthenticated() throws Exception {
        ProfileDto dto = new ProfileDto();
        when(profileDtoMapper.toDto(any()))
                .thenReturn(dto);
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Tag("negative")
    void getProfileIfUnauthorized() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MIN_USER", daysFromLastUpdate = 4)
    void updateProfileIfValidDtoAndCanUpdate() throws Exception {
        // дефолтные значения фабрики: login="login", email="email"

        UserDefaultPatchDto dto = new UserDefaultPatchDto();
        dto.setNewLogin("new_login");
        dto.setNewEmail("n@mail.com");

        when(userService.patchDefault(any(), any(), any())).thenReturn(null);

        mockMvc.perform(patch("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Everything updated successfully"));
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MIN_USER", daysFromLastUpdate = 4)
    void updateProfileIfLoginChangedReturnsNewTokenPair() throws Exception {
        UserDefaultPatchDto dto = new UserDefaultPatchDto();
        dto.setNewLogin("new_login"); // смена логина > новые токены

        when(userService.patchDefault(any(), any(), any()))
                .thenReturn(new TokenPair("new.access", "new.refresh"));

        mockMvc.perform(patch("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.access"))
                .andExpect(jsonPath("$.refreshToken").value("new.refresh"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER") // daysFromLastUpdate = 1 по дефолту
    void updateProfileIfCanNotUpdate() throws Exception {
        UserDefaultPatchDto dto = new UserDefaultPatchDto();
        dto.setNewLogin("new_login");

        mockMvc.perform(patch("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message")
                        .value("You can not update profile, 3 days did not past from last update"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER", daysFromLastUpdate = 4)
    void updateProfileIfSameLogin() throws Exception {
        UserDefaultPatchDto dto = new UserDefaultPatchDto();
        dto.setNewLogin("login"); //def

        mockMvc.perform(patch("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Login can not be the same"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(username = "minU", role = "MIN_USER", daysFromLastUpdate = 4)
    void updateProfileIfSameUsername() throws Exception {
        UserDefaultPatchDto dto = new UserDefaultPatchDto();
        dto.setNewUsername("minU"); // def

        mockMvc.perform(patch("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Username can not be the same"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER", daysFromLastUpdate = 4)
    void updateProfileIfSameEmail() throws Exception {
        UserDefaultPatchDto dto = new UserDefaultPatchDto();
        dto.setNewEmail("sal@mail.ru"); // def

        mockMvc.perform(patch("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Email can not be the same"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER", daysFromLastUpdate = 4)
    void updateProfileIfInvalidDto() throws Exception {
        UserDefaultPatchDto dto = new UserDefaultPatchDto();
        dto.setNewUsername("");
        dto.setNewLogin("");
        dto.setNewEmail("asd");

        mockMvc.perform(patch("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    @Tag("negative")
    void updateProfileIfUnauthorized() throws Exception {
        UserDefaultPatchDto dto = new UserDefaultPatchDto();
        dto.setNewLogin("new_login");

        mockMvc.perform(patch("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MIN_USER", daysFromLastUpdate = 4)
    void updatePasswordIfValidDto() throws Exception {
        UpdateUserPasswordDto dto = new UpdateUserPasswordDto();
        dto.setOldPassword("old_password");
        dto.setNewPassword("new_password");

        mockMvc.perform(patch("/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Updated successfully"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER") // 1 день — нельзя обновлять; по дефолту
    void updatePasswordIfCanNotUpdate() throws Exception {
        UpdateUserPasswordDto dto = new UpdateUserPasswordDto();
        dto.setOldPassword("old_password");
        dto.setNewPassword("new_password");

        mockMvc.perform(patch("/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message")
                        .value("You can not update profile, 3 days did not past from last update"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER", daysFromLastUpdate = 4)
    void updatePasswordIfSamePasswords() throws Exception {
        UpdateUserPasswordDto dto = new UpdateUserPasswordDto();
        dto.setOldPassword("same_password");
        dto.setNewPassword("same_password");

        mockMvc.perform(patch("/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message")
                        .value("Given passwords can not be the same"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER", daysFromLastUpdate = 4)
    void updatePasswordIfInvalidDto() throws Exception {
        UpdateUserPasswordDto dto = new UpdateUserPasswordDto();
        dto.setNewPassword("");

        mockMvc.perform(patch("/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(2)));
    }

    @Test
    @Tag("negative")
    void updatePasswordIfUnauthorized() throws Exception {
        UpdateUserPasswordDto dto = new UpdateUserPasswordDto();
        dto.setOldPassword("old_password");
        dto.setNewPassword("new_password");

        mockMvc.perform(patch("/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
