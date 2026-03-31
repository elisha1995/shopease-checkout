package com.shopease.checkout.order;

import com.shopease.checkout.dto.request.CheckoutRequest;
import com.shopease.checkout.dto.response.CheckoutResponse;
import com.shopease.checkout.dto.response.OrderResponse;
import com.shopease.checkout.dto.response.ProductResponse;
import com.shopease.checkout.entity.UserEntity;
import com.shopease.checkout.mapper.ProductMapper;
import com.shopease.checkout.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Orders & Products")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ProductRepository productRepository;

    @PostMapping("/orders/checkout")
    @Operation(summary = "Checkout", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request,
                                                     Authentication authentication) {
        var user = (UserEntity) authentication.getPrincipal();
        var result = orderService.checkout(user, request);
        return result.success() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/orders/{orderNumber}")
    @Operation(summary = "Get order details", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderNumber) {
        return orderService.findByOrderNumber(orderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/orders")
    @Operation(summary = "Get current user's orders", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication authentication) {
        var user = (UserEntity) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.findByUser(user));
    }

    @GetMapping("/products")
    @Operation(summary = "List active products")
    public List<ProductResponse> getProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(ProductMapper::toResponse)
                .toList();
    }
}
