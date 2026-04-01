package com.shopease.checkout.mapper;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.RegisterRequest;
import com.shopease.checkout.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toEntitySetsAllFields() {
        var request = new RegisterRequest("Kwame Asante", "kwame@test.com",
                "pass123", "+233240000000", MembershipTier.GOLD);
        var entity = UserMapper.toEntity(request, "encoded_hash");

        assertEquals("Kwame Asante", entity.getFullName());
        assertEquals("kwame@test.com", entity.getEmail());
        assertEquals("encoded_hash", entity.getPasswordHash());
        assertEquals("+233240000000", entity.getPhone());
        assertEquals(MembershipTier.GOLD, entity.getTier());
    }

    @Test
    void toEntityDefaultsToStandardTierWhenNull() {
        var request = new RegisterRequest("Ama Serwaa", "ama@test.com", "pass123",
                null, null);
        var entity = UserMapper.toEntity(request, "hash");

        assertEquals(MembershipTier.STANDARD, entity.getTier());
        assertNull(entity.getPhone());
    }

    @Test
    void toAuthResponseMapsCorrectly() {
        var entity = new UserEntity();
        entity.setEmail("kwame@test.com");
        entity.setFullName("Kwame Asante");
        entity.setTier(MembershipTier.GOLD);
        entity.setPhone("+233240000000");
        // Simulate a persisted entity with ID
        try {
            var idField = UserEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, UUID.fromString("019d0000-0000-7000-8000-000000000001"));
        } catch (Exception _) {
            fail("Could not set ID field");
        }

        var response = UserMapper.toAuthResponse(entity, "jwt-token-123");

        assertEquals("jwt-token-123", response.token());
        assertEquals("019d0000-0000-7000-8000-000000000001", response.userId());
        assertEquals("Kwame Asante", response.fullName());
        assertEquals("kwame@test.com", response.email());
        assertEquals("GOLD", response.tier());
        assertEquals("+233240000000", response.phone());
    }

    @Test
    void toProfileResponseMapsCorrectly() {
        var entity = new UserEntity();
        entity.setFullName("Ama Serwaa");
        entity.setEmail("ama@test.com");
        entity.setPhone("+233500000000");
        entity.setTier(MembershipTier.STANDARD);
        try {
            var idField = UserEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, UUID.fromString("019d0000-0000-7000-8000-000000000002"));
        } catch (Exception _) {
            fail("Could not set ID field");
        }

        var response = UserMapper.toProfileResponse(entity);

        assertEquals("019d0000-0000-7000-8000-000000000002", response.id());
        assertEquals("Ama Serwaa", response.fullName());
        assertEquals("ama@test.com", response.email());
        assertEquals("+233500000000", response.phone());
        assertEquals("STANDARD", response.tier());
    }
}
