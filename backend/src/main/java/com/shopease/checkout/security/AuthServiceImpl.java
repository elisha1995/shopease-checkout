package com.shopease.checkout.security;

import com.shopease.checkout.dto.request.LoginRequest;
import com.shopease.checkout.dto.request.RegisterRequest;
import com.shopease.checkout.dto.response.AuthResponse;
import com.shopease.checkout.dto.response.UserProfileResponse;
import com.shopease.checkout.entity.UserEntity;
import com.shopease.checkout.mapper.UserMapper;
import com.shopease.checkout.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        var entity = UserMapper.toEntity(request, passwordEncoder.encode(request.password()));
        entity = userRepository.save(entity);

        // Publish event to trigger welcome email (Observer pattern)
        eventPublisher.publishEvent(new UserRegisteredEvent(entity.getFullName(), entity.getEmail()));

        var token = jwtService.generateToken(entity.getId(), entity.getEmail(), entity.getTier().name());
        return UserMapper.toAuthResponse(entity, token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.email())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        var token = jwtService.generateToken(user.getId(), user.getEmail(), user.getTier().name());
        return UserMapper.toAuthResponse(user, token);
    }

    @Override
    public UserProfileResponse getProfile(UserEntity authenticatedUser) {
        return UserMapper.toProfileResponse(authenticatedUser);
    }
}
