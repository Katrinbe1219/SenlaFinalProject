package org.example.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.core.exceptions.NonHibernateException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtCheckFilterTest {
    @Mock
    ObjectMapper mapper;

    @Mock
    JwtService jwtService;

    @Mock
    UserDetailsService userDetailsService;
    @InjectMocks
    //  основном коде не было кастомного конструктора, так что подойдет
    JwtCheckFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Tag("positive")
    @DisplayName("doFilterInternalIfTokenExists")
    void doFilterInternalIfTokenExists() throws ServletException, IOException {
        Claims claims = mock(Claims.class);
        UserDetails userDetails = mock(UserDetails.class);

        Collection auths = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(userDetails.getAuthorities()).thenReturn(auths);

        when(userDetailsService.loadUserByUsername(eq("username")))
                .thenReturn(userDetails);
        when(jwtService.parseToken(anyString())).thenReturn(claims);
        when(claims.getSubject()).thenReturn("username");
        when(request.getHeader(eq("Authorization")))
                .thenReturn("Bearer token-check");

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Assertions.assertNotNull(auth);
        Assertions.assertEquals(userDetails, auth.getPrincipal());
        verify(filterChain).doFilter(request, response);

    }


    @Test
    @Tag("negative")
    @DisplayName("doFilterInternalIfTokenDoesNotExists")
    void doFilterInternalIfTokenDoesNotExists() throws ServletException, IOException {
        when(request.getHeader(eq("Authorization")))
                .thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);
        verifyNoInteractions(jwtService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @Tag("negative")
    @DisplayName("doFilterInternalIfRepositoryFailed")
    void doFilterInternalIfRepositoryFailed() throws ServletException, IOException {
        when(request.getHeader(eq("Authorization")))
                .thenReturn("Bearer token-check");
        when(jwtService.parseToken(eq("token-check")))
                .thenThrow(new NonHibernateException("problem"));

        PrintWriter pw = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(pw);

        filter.doFilterInternal(request, response, filterChain);
        verifyNoInteractions(filterChain);
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }

    @Test
    @Tag("negative")
    @DisplayName("doFilterInternalIfRepositoryFailed")
    void doFilterInternalIfTokenIsInvalid() throws ServletException, IOException {
        when(request.getHeader(eq("Authorization")))
                .thenReturn("Bearer token-check");
        when(jwtService.parseToken(eq("token-check")))
                .thenThrow(new MalformedJwtException("problem"));

        PrintWriter pw = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(pw);

        filter.doFilterInternal(request, response, filterChain);
        verifyNoInteractions(filterChain);
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @Tag("negative")
    @DisplayName("doFilterInternalIfTokenHasNullUsernameNoAuthentication")
    void doFilterInternalIfTokenHasNullUsernameNoAuthentication() throws Exception {
        Claims claims = mock(Claims.class);

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token");
        when(jwtService.parseToken("token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);

    }

    @ParameterizedTest
    @ValueSource(strings = {"/login", "/logout", "/refresh"})
    @DisplayName("shouldNotFilterSkippedUrls")
    void shouldNotFilterSkippedUrls(String url){
        when(request.getRequestURI()).thenReturn(url);
        Assertions.assertTrue(filter.shouldNotFilter(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/prices", "/shops", "/units"})
    @DisplayName("shouldNotFilterNotSkippedUrls")
    void shouldNotFilterNotSkippedUrls(String url){
        when(request.getRequestURI()).thenReturn(url);
        Assertions.assertFalse(filter.shouldNotFilter(request));
    }
}
