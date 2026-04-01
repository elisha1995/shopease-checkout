package com.shopease.checkout.security;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.entity.UserEntity;
import com.shopease.checkout.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtService jwtService;
    private UserRepository userRepository;
    private JwtAuthFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userRepository = mock(UserRepository.class);
        filter = new JwtAuthFilter(jwtService, userRepository);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void setsAuthenticationForValidToken() throws Exception {
        var userId = UUID.randomUUID();
        var user = new UserEntity();
        user.setEmail("kwame@test.com");
        user.setTier(MembershipTier.GOLD);
        try {
            var idField = UserEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, userId);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }

        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtService.isValid("valid-token")).thenReturn(true);
        when(jwtService.getUserId("valid-token")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(user, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_USER".equals(a.getAuthority())));
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> "TIER_GOLD".equals(a.getAuthority())));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doesNotSetAuthenticationWhenNoHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doesNotSetAuthenticationForNonBearerHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doesNotSetAuthenticationForInvalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtService.isValid("invalid-token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void doesNotSetAuthenticationWhenUserNotFound() throws Exception {
        var userId = UUID.randomUUID();
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtService.isValid("valid-token")).thenReturn(true);
        when(jwtService.getUserId("valid-token")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void alwaysContinuesFilterChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad");
        when(jwtService.isValid("bad")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
