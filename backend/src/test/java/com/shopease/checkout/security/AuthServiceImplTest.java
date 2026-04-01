package com.shopease.checkout.security;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.LoginRequest;
import com.shopease.checkout.dto.request.RegisterRequest;
import com.shopease.checkout.entity.UserEntity;
import com.shopease.checkout.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private ApplicationEventPublisher eventPublisher;
    private AuthServiceImpl authService;

    private static void setId(Object entity, UUID id) {
        try {
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
    }

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtService, eventPublisher);
    }

    @Test
    void registerCreatesUserAndReturnsToken() {
        var request = new RegisterRequest("Kwame Asante", "kwame@test.com", "password",
                "+233240000000", MembershipTier.GOLD);

        when(userRepository.existsByEmail("kwame@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded_hash");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(
                invocation -> {
            var entity = invocation.getArgument(0, UserEntity.class);
            setId(entity, UUID.randomUUID());
            return entity;
        });
        when(jwtService.generateToken(any(), eq("kwame@test.com"),
                eq("GOLD"))).thenReturn("jwt-token");

        var response = authService.register(request);

        assertEquals("jwt-token", response.token());
        assertEquals("Kwame Asante", response.fullName());
        assertEquals("kwame@test.com", response.email());
        assertEquals("GOLD", response.tier());
        verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void registerThrowsForDuplicateEmail() {
        var request = new RegisterRequest("Ama", "ama@test.com", "pass", null, null);
        when(userRepository.existsByEmail("ama@test.com")).thenReturn(true);

        var ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));
        assertEquals("Email already registered", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerPublishesUserRegisteredEvent() {
        var request = new RegisterRequest("Kofi", "kofi@test.com", "pass123", null,
                null);

        when(userRepository.existsByEmail("kofi@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hash");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            var entity = invocation.getArgument(0, UserEntity.class);
            setId(entity, UUID.randomUUID());
            return entity;
        });
        when(jwtService.generateToken(any(), any(), any())).thenReturn("token");

        authService.register(request);

        var captor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals("Kofi", captor.getValue().userName());
        assertEquals("kofi@test.com", captor.getValue().userEmail());
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        var entity = new UserEntity();
        entity.setEmail("kwame@test.com");
        entity.setFullName("Kwame");
        entity.setPasswordHash("encoded");
        entity.setTier(MembershipTier.STANDARD);
        entity.setPhone(null);
        setId(entity, UUID.randomUUID());

        when(userRepository.findByEmail("kwame@test.com")).thenReturn(
                Optional.of(entity));
        when(passwordEncoder.matches("correct_pass", "encoded"))
                .thenReturn(true);
        when(jwtService.generateToken(any(), eq("kwame@test.com"),
                eq("STANDARD"))).thenReturn("login-token");

        var request = new LoginRequest("kwame@test.com", "correct_pass");
        var response = authService.login(request);

        assertEquals("login-token", response.token());
        assertEquals("Kwame", response.fullName());
    }

    @Test
    void loginThrowsForWrongPassword() {
        var entity = new UserEntity();
        entity.setEmail("kwame@test.com");
        entity.setPasswordHash("encoded");

        when(userRepository.findByEmail("kwame@test.com")).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("wrong_pass", "encoded")).thenReturn(false);

        var request = new LoginRequest("kwame@test.com", "wrong_pass");
        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void loginThrowsForNonExistentEmail() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        var request = new LoginRequest("nobody@test.com", "pass");
        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void getProfileReturnsUserProfile() {
        var entity = new UserEntity();
        entity.setFullName("Kwame");
        entity.setEmail("kwame@test.com");
        entity.setPhone("+233240000000");
        entity.setTier(MembershipTier.GOLD);
        setId(entity, UUID.randomUUID());

        var profile = authService.getProfile(entity);

        assertEquals("Kwame", profile.fullName());
        assertEquals("kwame@test.com", profile.email());
        assertEquals("GOLD", profile.tier());
    }
}
