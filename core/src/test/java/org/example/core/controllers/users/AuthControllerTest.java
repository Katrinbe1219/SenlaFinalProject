package org.example.core.controllers.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.controllers.CategoryController;
import org.example.core.dto.auth.RegisterDto;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.security.DeviceInfoExtractor;
import org.example.core.security.TokenPair;
import org.example.core.services.auth.AuthService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ContextConfiguration(classes = {SecurityConfiguration.class,
        AuthController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class AuthControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    AuthService authService;

    @Autowired
    DeviceInfoExtractor deviceInfoExtractor;

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
    void registerIfNotAuthorizedValidDto() throws Exception {

        RegisterDto dto = new RegisterDto();
        dto.setUsername("username");
        dto.setLogin("login");
        dto.setPassword("password");

        when(authService.register(any(), any()))
                .thenReturn(new TokenPair("access-token", "refresh-token"));
        mockMvc.perform(
                post("/register")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @Tag("negative")
    void registerIfNotAuthorizedInvalidDto() throws Exception {

        RegisterDto dto = new RegisterDto();


        when(authService.register(any(), any()))
                .thenReturn(new TokenPair("access-token", "refresh-token"));
        mockMvc.perform(
                        post("/register")
                                .content(mapper.writeValueAsString(dto))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role="MODERATOR")
    void registerIfAuthorized() throws Exception {

        RegisterDto dto = new RegisterDto();
        dto.setUsername("username");
        dto.setLogin("login");
        dto.setPassword("password");

        when(authService.register(any(), any()))
                .thenReturn(new TokenPair("access-token", "refresh-token"));
        mockMvc.perform(
                        post("/register")
                                .content(mapper.writeValueAsString(dto))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }
}
