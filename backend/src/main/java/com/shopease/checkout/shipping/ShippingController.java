package com.shopease.checkout.shipping;

import com.shopease.checkout.dto.request.ShippingCalculateRequest;
import com.shopease.checkout.entity.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipping")
@Tag(name = "Shipping")
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculate shipping cost", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> calculate(@Valid @RequestBody ShippingCalculateRequest request,
                                       Authentication authentication) {
        var user = (UserEntity) authentication.getPrincipal();
        var tier = user.getTier();

        if (request.method() != null) {
            return ResponseEntity.ok(shippingService.calculate(request.method(), request.items(), tier));
        }
        return ResponseEntity.ok(shippingService.calculateAll(request.items(), tier));
    }
}
