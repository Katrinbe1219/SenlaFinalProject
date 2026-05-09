package org.example.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.core.models.User;
import org.example.core.services.auth.RefreshTokenService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomLoginFilterTest {
    @Mock
    AuthenticationManager authManager;

    private ObjectMapper mapper = new ObjectMapper();
    @Mock
    JwtService jwtService;
    @Mock
    RefreshTokenService refreshTokenService;
    @Mock
    DeviceInfoExtractor deviceInfoExtractor;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;

    // сть супер класс,
    // значит через обычный MOck не получится, рефлексия не даст super()
    CustomLoginFilter filter;

    @BeforeEach
    void setUp(){
        filter = new CustomLoginFilter(
                authManager, jwtService,
                refreshTokenService, deviceInfoExtractor, mapper
        );
    }

    @Test
    @Tag("positive")
    @DisplayName("onSuccessfulAuthenticationIfTokenPairIsReturned")
    void onSuccessfulAuthenticationIfTokenPairIsReturned() throws Exception {
        Authentication auth = mock(Authentication.class);
        User user = new User();

        when(jwtService.generateToken(eq(auth)))
                .thenReturn("access-token");
        when(deviceInfoExtractor.extract(eq(request)))
                .thenReturn("Chrom device-info");
        when(auth.getPrincipal()).thenReturn(user);
        when(refreshTokenService.createToken(eq(user), anyString()))
                .thenReturn("refresh-token");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        filter.onSuccessfulAuthentication(request, response, auth);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("application/json");

        String res = sw.toString();
        Assertions.assertTrue(res.contains("access-token"));
        Assertions.assertTrue(res.contains("refresh-token"));

    }

    @Test
    @Tag("negative")
    @DisplayName("onUnsuccessfulAuthenticationIfUserIsLocked")
    void onUnsuccessfulAuthenticationIfUserIsLocked() throws Exception {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        filter.onUnsuccessfulAuthentication(
                request,response, new LockedException("Locked")
        );
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String res = sw.toString();
        Assertions.assertTrue(res.contains("Your account is locked"));
    }

    @Test
    @Tag("negative")
    @DisplayName("onUnsuccessfulAuthenticationIfUserIsNotLocked")
    void onUnsuccessfulAuthenticationIfUserIsNotLocked() throws Exception {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        filter.onUnsuccessfulAuthentication(
                request,response, new BadCredentialsException("credentials")
        );
        verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String res = sw.toString();
        Assertions.assertTrue(res.contains("Bad credentials"));
    }
}
