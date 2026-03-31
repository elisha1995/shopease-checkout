package com.shopease.checkout.shipping;

import com.shopease.checkout.dto.request.ShippingCalculateRequest;
import com.shopease.checkout.entity.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipping")
@Tag(name = "Shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate shipping cost", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Object> calculate(@Valid @RequestBody ShippingCalculateRequest request,
                                       Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserEntity user)) {
            return ResponseEntity.status(401).build();
        }

        if (request.method() != null) {
            return ResponseEntity.ok(shippingService.calculate(request.method(), request.items(), user.getTier()));
        }
        return ResponseEntity.ok(shippingService.calculateAll(request.items(), user.getTier()));
    }
}
