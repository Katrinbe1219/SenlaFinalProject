package org.example.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.example.core.exceptions.NonHibernateException;
import org.example.core.services.auth.RefreshTokenService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtRefreshFilterTest {

    @Mock
    RefreshTokenService refreshTokenService;
    @Mock
    DeviceInfoExtractor deviceInfoExtractor;

    private ObjectMapper mapper = new ObjectMapper();
    private PrintWriter pw;
    private StringWriter sw;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    @Mock
    FilterChain chain;

    JwtRefreshFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtRefreshFilter(
                refreshTokenService,
                deviceInfoExtractor,
                mapper
        );

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @Tag("negative")
    @DisplayName("doFilterInternalIfRequestIsNotRefresh")
    void doFilterInternalIfRequestIsNotRefresh() throws ServletException, IOException {
        request.setRequestURI("/login");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        verifyNoInteractions(refreshTokenService, deviceInfoExtractor);
    }

    @Test
    @Tag("negative")
    @DisplayName("doFilterInternalIfRequestIsNotPost")
    void doFilterInternalIfRequestIsNotPost() throws ServletException, IOException {
        request.setMethod("GET");
        request.setRequestURI("/refresh");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        verifyNoInteractions(refreshTokenService, deviceInfoExtractor);
    }

    @Test
    @Tag("negative")
    @DisplayName("doFilterInternalIfNonHibernateException")
    void doFilterInternalIfNonHibernateException() throws ServletException, IOException {
        RefreshRequest refreshRequest = new RefreshRequest("token");
        String requestBody = mapper.writeValueAsString(refreshRequest);

        request.setMethod("POST");
       request.setRequestURI("/refresh");
       request.setContent(
               requestBody.getBytes()
       );

        when(deviceInfoExtractor.extract(request)).thenReturn("device-infod");
        when(refreshTokenService.rotate(anyString(), anyString()))
                .thenThrow(new NonHibernateException("problem"));


        filter.doFilter(request, response, chain);
        verify(chain, never()).doFilter(request, response);
        Assertions.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        Assertions.assertTrue(response.getContentAsString().contains("problem"));
    }


    @Test
    @Tag("negative")
    @DisplayName("doFilterInternalIfSuccessful")
    void doFilterInternalIfSuccessful() throws ServletException, IOException {
        RefreshRequest refreshRequest = new RefreshRequest("token");
        String requestBody = mapper.writeValueAsString(refreshRequest);

        request.setMethod("POST");
        request.setRequestURI("/refresh");
        request.setContent(
                requestBody.getBytes()
        );

        when(deviceInfoExtractor.extract(request)).thenReturn("device-infod");
        when(refreshTokenService.rotate(anyString(), anyString()))
                .thenReturn(new TokenPair("access-token", "refresh-token"));


        filter.doFilter(request, response, chain);
        verify(chain, never()).doFilter(request, response);
        Assertions.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assertions.assertTrue(response.getContentAsString().contains("access-token"));
    }

}
