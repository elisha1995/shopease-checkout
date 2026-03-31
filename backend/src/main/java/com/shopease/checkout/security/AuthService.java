package com.shopease.checkout.security;

import com.shopease.checkout.dto.request.LoginRequest;
import com.shopease.checkout.dto.request.RegisterRequest;
import com.shopease.checkout.dto.response.AuthResponse;
import com.shopease.checkout.dto.response.UserProfileResponse;
import com.shopease.checkout.entity.UserEntity;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserProfileResponse getProfile(UserEntity authenticatedUser);
}
