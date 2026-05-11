package org.example.core.controllers.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.kafka.DiscountMessage;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        SendNotificationController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class SendNotificationControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

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
    @WithMockCustomUser(role = "MODERATOR")
    void sendDiscountNotificationIfAuthorizedWithValidDto() throws Exception {
        DiscountMessage dto = new DiscountMessage();
        dto.setTopic("Spring Sale");
        dto.setShopId(1L);
        dto.setCategoryId(2L);
        dto.setMessage("YAAYAYA");

        mockMvc.perform(post("/moderator/notification/discount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message was send"));
    }



    @Test
    @Tag("negative")
    void sendDiscountNotificationIfUnauthorized() throws Exception {
        DiscountMessage dto = new DiscountMessage();
        dto.setMessage("message");
        dto.setTopic("TOPCI");

        mockMvc.perform(post("/moderator/notification/discount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void sendDiscountNotificationIfWrongRole() throws Exception {
        DiscountMessage dto = new DiscountMessage();
        dto.setMessage("message");
        dto.setTopic("TOPCI");


        mockMvc.perform(post("/moderator/notification/discount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void sendDiscountNotificationWithInvalidDto() throws Exception {
        DiscountMessage dto = new DiscountMessage();
        dto.setShopId(-1L);
        dto.setTagId(-2L);
        dto.setCategoryId(0L);

        mockMvc.perform(post("/moderator/notification/discount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors",hasSize(6)));
    }
}
