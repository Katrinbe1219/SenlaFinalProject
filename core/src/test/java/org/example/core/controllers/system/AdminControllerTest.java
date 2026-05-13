package org.example.core.controllers.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.auth.RegisterDto;
import org.example.core.dto.getting.RefreshTokenDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.getting.users.UserFullDto;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.hibernate.base_settings.filters.RefreshTokenFilter;
import org.example.core.hibernate.base_settings.filters.users.UserAdvancedFilter;
import org.example.core.models.types.RoleTypes;
import org.example.core.services.auth.RefreshTokenService;
import org.example.core.services.objects.UserService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        AdminController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class AdminControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private static final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

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
    @WithMockCustomUser(role = "ADMIN")
    void getUsersIfAdminWithValidFilters() throws Exception {
        UserAdvancedFilter filters = new UserAdvancedFilter();
        filters.setLocked(true);
        filters.setStartCreatedAt(LocalDate.of(2024,1,2));
        filters.setUpdatedAt(LocalDate.of(2024,1,2));


        List<UserFullDto> expected = List.of(new UserFullDto(), new UserFullDto());
        when(userService.getAllUsers(any(UserAdvancedFilter.class), anyBoolean())).thenReturn(expected);

        mockMvc.perform(get("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void getUsersIfAdminWithInvalidFilters() throws Exception {
        UserAdvancedFilter filters = new UserAdvancedFilter();
        filters.setStartCreatedAt(LocalDate.of(2024,5,2));
        filters.setEndCreatedAt(LocalDate.of(2024,1,2));

        filters.setStartUpdatedAt(LocalDate.of(2024,5,2));
        filters.setEndUpdatedAt(LocalDate.of(2024,1,2));

        List<UserFullDto> expected = List.of(new UserFullDto(), new UserFullDto());
        when(userService.getAllUsers(any(UserAdvancedFilter.class), anyBoolean())).thenReturn(expected);

        mockMvc.perform(get("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(2)));
    }

    @Test
    @Tag("negative")
    void getUsersIfUnauthorized() throws Exception {
        UserAdvancedFilter filters = new UserAdvancedFilter();
        mockMvc.perform(get("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void getUsersIfRoleProhibited() throws Exception {
        UserAdvancedFilter filters = new UserAdvancedFilter();
        mockMvc.perform(get("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isForbidden());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void postAnalystIfAdminWithValidDto() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setLogin("analyst");
        dto.setUsername("username");
        dto.setPassword("pass123");
        dto.setEmail("s@mail.ru");

        UserFullDto response = new UserFullDto();
        response.setId(1L);
        response.setLogin("analyst");
        when(userService.createUser(eq(RoleTypes.ANALYST), any(RegisterDto.class))).thenReturn(response);

        mockMvc.perform(post("/admin/users/analyst")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))

        .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.login").value("analyst"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void postAnalystIfAdminWithInvalidDto() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setEmail("asd");
        mockMvc.perform(post("/admin/users/analyst")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(4)));
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void postUserWithMinUserType() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setLogin("min_user");
        dto.setPassword("pass");
        dto.setUsername("username");
        dto.setEmail("m@mail.ru");
        UserFullDto response = new UserFullDto();
        response.setId(2L);
        when(userService.createUser(eq(RoleTypes.MIN_USER), any(RegisterDto.class))).thenReturn(response);

        mockMvc.perform(post("/admin/users/user")
                        .param("type", "min_user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void postUserWithMaxUserType() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setLogin("m_user");
        dto.setPassword("pass");
        dto.setUsername("username");
        dto.setEmail("m@mail.com");
        UserFullDto response = new UserFullDto();
        response.setId(3L);
        when(userService.createUser(eq(RoleTypes.MAX_USER), any(RegisterDto.class))).thenReturn(response);

        mockMvc.perform(post("/admin/users/user")
                        .param("type", "max_user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void postUserWithInvalidType() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setLogin("m_user");
        dto.setPassword("pass");
        dto.setUsername("username");
        dto.setEmail("m@mail.com");
        mockMvc.perform(post("/admin/users/user")
                        .param("type", "super_user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Either min_user or max_user role"));
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void postModerator() throws Exception {
        RegisterDto dto = new RegisterDto();
        dto.setLogin("mod");
        dto.setPassword("pass");
        dto.setUsername("mod");
        dto.setEmail("m@mail.com");
        UserFullDto response = new UserFullDto();
        response.setId(4L);
        when(userService.createUser(eq(RoleTypes.MODERATOR), any(RegisterDto.class))).thenReturn(response);

        mockMvc.perform(post("/admin/users/moderator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void getUserByIdIfAdminWithValidId() throws Exception {
        UserFullDto dto = new UserFullDto();
        dto.setId(5L);
        dto.setLogin("test");
        when(userService.getUserById(5L)).thenReturn(dto);

        mockMvc.perform(get("/admin/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.login").value("test"));
    }

    private static Stream<Arguments> provideInvalidIds() {
        return Stream.of(
                Arguments.of("0"),
                Arguments.of("-5")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidIds")
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void getUserByIdWithInvalidId(String id) throws Exception {
        mockMvc.perform(get("/admin/users/" + id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Id must be > 0"));
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void deleteUserByIdSuccess() throws Exception {
        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User is successfully deleted"));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidIds")
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void deleteUserByIdInvalidId(String id) throws Exception {
        mockMvc.perform(delete("/admin/users/" + id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Id must be > 0"));
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void patchRoleSuccess() throws Exception {
        mockMvc.perform(patch("/admin/users/1/role")
                        .param("role", "max_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role has been updated successfully"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void patchRoleWithInvalidRoleString() throws Exception {
        mockMvc.perform(patch("/admin/users/1/role")
                        .param("role", "smth"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void getSessionsWithValidFilters() throws Exception {
        RefreshTokenFilter filter = new RefreshTokenFilter();
        List<RefreshTokenDto> tokens = List.of(new RefreshTokenDto(), new RefreshTokenDto());
        when(refreshTokenService.getAllByFilters(any(RefreshTokenFilter.class))).thenReturn(tokens);

        mockMvc.perform(get("/admin/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void getSessionsWithInvalidFilters() throws Exception {
        RefreshTokenFilter filter = new RefreshTokenFilter();
        filter.setSize(0);
        filter.setEndLastUsedAt(LocalDate.of(2023,1,1));
        filter.setStartLastUsedAt(LocalDate.of(2025,1,1));

        filter.setCreatedAt(LocalDate.of(2026,1,1));
        filter.setStartCreatedAt(LocalDate.of(2027,1,1));
        filter.setEndCreatedAt(LocalDate.of(2028,1,1));

        filter.setUserId(2L);
        filter.setUserIds(List.of(1L));

        List<RefreshTokenDto> tokens = List.of(new RefreshTokenDto(), new RefreshTokenDto());
        when(refreshTokenService.getAllByFilters(any(RefreshTokenFilter.class))).thenReturn(tokens);

        mockMvc.perform(get("/admin/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filter)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(4)));
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void getSessionsByUserIdValid() throws Exception {
        List<RefreshTokenDto> tokens = List.of(new RefreshTokenDto());
        when(refreshTokenService.getTokensByUserId(10L)).thenReturn(tokens);

        mockMvc.perform(get("/admin/sessions/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidIds")
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void getSessionsByUserIdInvalidId(String id) throws Exception {
        mockMvc.perform(get("/admin/sessions/" + id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Id must be > 0"));
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "ADMIN")
    void deleteSessionsByUserIdValid() throws Exception {
        mockMvc.perform(delete("/admin/sessions/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sessions has been deleted successfully"));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidIds")
    @Tag("negative")
    @WithMockCustomUser(role = "ADMIN")
    void deleteSessionsByUserIdInvalidId(String id) throws Exception {
        mockMvc.perform(delete("/admin/sessions/" + id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Id must be > 0"));
    }
}
