package org.example.core.controllers.prices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.core.annotations.WithMockCustomUser;
import org.example.core.config.TestSecurityConfig;
import org.example.core.config.TestServicesConfig;
import org.example.core.configuration.SecurityConfiguration;
import org.example.core.dto.creating.PriceCreateDto;
import org.example.core.dto.getting.StringResponse;
import org.example.core.dto.getting.prices.PriceGetResultForModerator;
import org.example.core.exceptions.GlobalExceptionHandler;
import org.example.core.hibernate.base_settings.filters.prices.PriceFilter;
import org.example.core.services.documents.prices.PriceService;
import org.example.core.services.documents.prices.data.OptionForUpload;
import org.example.core.services.upload.ImportFileCsvService;
import org.example.core.services.upload.ImportFileXlsxService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {
        SecurityConfiguration.class,
        PriceForModeratorController.class,
        GlobalExceptionHandler.class,
        TestServicesConfig.class,
        TestSecurityConfig.class
})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class PriceForModeratorControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PriceService priceService;

    @Autowired
    private ImportFileCsvService importFileCsvService;

    @Autowired
    private ImportFileXlsxService importFileXlsxService;

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

    private static PriceCreateDto getValidPriceCreateDto(){
        PriceCreateDto dto = new PriceCreateDto();
        dto.setGoodId(1L);
        dto.setShopId(1L);
        dto.setPrice(BigDecimal.valueOf(100));
        return dto;

    }
    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void createPriceIfValidDto() throws Exception {
        PriceCreateDto dto = getValidPriceCreateDto();
        PriceGetResultForModerator response = new PriceGetResultForModerator();
        response.setId(10L);
        when(priceService.createPrice(any(PriceCreateDto.class))).thenReturn(response);

        mockMvc.perform(post("/moderator/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void createPriceIfInvalidDto() throws Exception {
        PriceCreateDto dto = new PriceCreateDto();
        dto.setGoodId(-1L);
        mockMvc.perform(post("/moderator/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    @Tag("negative")
    void createPriceIfUnauthorized() throws Exception {
        PriceCreateDto dto = getValidPriceCreateDto();

        mockMvc.perform(post("/moderator/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MIN_USER")
    void createPriceIfRoleProhibited() throws Exception {
        PriceCreateDto dto = getValidPriceCreateDto();

        mockMvc.perform(post("/moderator/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access Denied"));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void updatePriceIfValidDto() throws Exception {
        PriceCreateDto dto = getValidPriceCreateDto();

        PriceGetResultForModerator response = new PriceGetResultForModerator();
        response.setId(10L);
        when(priceService.updatePrice(any(PriceCreateDto.class))).thenReturn(response);

        mockMvc.perform(patch("/moderator/prices/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void updatePriceIfInvalidDto() throws Exception {
        PriceCreateDto invalidDto = new PriceCreateDto();
        mockMvc.perform(patch("/moderator/prices/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void uploadFileIfValidCsv() throws Exception {
        PriceCreateDto priceCreate = getValidPriceCreateDto();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                "goodId,shopId,price\n1,1,100".getBytes()
        );
        List<PriceCreateDto> dtos = List.of(priceCreate);
        when(importFileCsvService.importPrices(any())).thenReturn(dtos);
        doNothing().when(priceService).saveAll(anyList(), any(OptionForUpload.class), anyBoolean());

        mockMvc.perform(multipart("/moderator/prices/upload")
                        .file(file)
                        .param("option", "stop")
                        .param("send", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Everything was uploaded"));
    }

    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void uploadFileIfValidXlsx() throws Exception {
        PriceCreateDto priceCreate = getValidPriceCreateDto();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "fake content".getBytes()
        );
        when(importFileXlsxService.importPrices(any())).thenReturn(List.of(priceCreate));
        doNothing().when(priceService).saveAll(anyList(), any(OptionForUpload.class), anyBoolean());

        mockMvc.perform(multipart("/moderator/prices/upload")
                        .file(file)
                        .param("option", "skip")
                        .param("send", "false"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void uploadFileIfOptionSkipAndSendTrue() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "data".getBytes());
        mockMvc.perform(multipart("/moderator/prices/upload")
                        .file(file)
                        .param("option", "skip")
                        .param("send", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Option skip can not be applied with parameter send = true"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void uploadFileWhenFileEmpty() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);
        mockMvc.perform(multipart("/moderator/prices/upload")
                        .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File is empty!"));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void uploadFileIfInvalidExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file",
                "test.txt",
                "text/plain",
                "data".getBytes());
        mockMvc.perform(multipart("/moderator/prices/upload")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File is wrong! It is either csv or xls or xlsx"));
    }

    @Test
    @Tag("negative")
    void uploadFileIfUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "data".getBytes());
        mockMvc.perform(multipart("/moderator/prices/upload")
                        .file(file))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void getPricesIfValidFilters() throws Exception {
        PriceFilter filters = new PriceFilter();
        List<PriceGetResultForModerator> expected = List.of(new PriceGetResultForModerator());
        when(priceService.getByFilters(any(PriceFilter.class))).thenReturn(expected);

        mockMvc.perform(get("/moderator/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void getPricesIfInvalidFilters() throws Exception {
        PriceFilter filters = new PriceFilter();
        filters.setCurPrice(BigDecimal.valueOf(-1));
        filters.setShopIds(List.of());

        mockMvc.perform(get("/moderator/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(filters)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors", hasSize(2)));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void deletePriceByGoodAndShopIfValid() throws Exception {
        doNothing().when(priceService).deletePriceByGoodAndShop(1L, 2L);
        mockMvc.perform(delete("/moderator/prices")
                        .param("goodId", "1")
                        .param("shopId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Price was deleted successfully"));
    }

    private static Stream<Arguments> provideInvalidDeleteParams() {
        return Stream.of(
                Arguments.of("goodId", "0", "shopId", "1", "goodId must be >0 "),
                Arguments.of("goodId", "-1", "shopId", "1", "goodId must be >0 "),
                Arguments.of("goodId", "1", "shopId", "0", "shopId must be >0 "),
                Arguments.of("goodId", "1", "shopId", "-5", "shopId must be >0 "),
                Arguments.of("goodId", null, "shopId", "1", "Required request parameter 'goodId' for method parameter type Long is not present"),
                Arguments.of("goodId", "1", "shopId", null, "Required request parameter 'shopId' for method parameter type Long is not present")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidDeleteParams")
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void deletePriceByGoodAndShopIfInvalidParams(String goodParam, String goodVal, String shopParam, String shopVal, String expectedMessage) throws Exception {
        var request = delete("/moderator/prices");
        if (goodParam != null) request.param(goodParam, goodVal);
        if (shopParam != null) request.param(shopParam, shopVal);
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void deletePriceByIdIfValid() throws Exception {
        mockMvc.perform(delete("/moderator/prices/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Price was deleted successfully"));
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
    @WithMockCustomUser(role = "MODERATOR")
    void deletePriceByIdWithInvalidId(String id) throws Exception {
        mockMvc.perform(delete("/moderator/prices/" + id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("id must be >0 "));
    }


    @Test
    @Tag("positive")
    @WithMockCustomUser(role = "MODERATOR")
    void getPriceByIdIfValid() throws Exception {
        PriceGetResultForModerator dto = new PriceGetResultForModerator();
        dto.setId(5L);
        when(priceService.getByIdForModerator(5L)).thenReturn(dto);

        mockMvc.perform(get("/moderator/prices/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidIds")
    @Tag("negative")
    @WithMockCustomUser(role = "MODERATOR")
    void getPriceByIdIfInvalidId(String id) throws Exception {
        mockMvc.perform(get("/moderator/prices/" + id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("id must be >0 "));
    }

    @Test
    @Tag("negative")
    void getPriceByIdIfUnauthorized() throws Exception {
        mockMvc.perform(get("/moderator/prices/1"))
                .andExpect(status().isUnauthorized());
    }
}
