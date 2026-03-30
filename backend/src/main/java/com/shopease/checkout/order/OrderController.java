package com.shopease.checkout.order;

import com.shopease.checkout.dto.request.CheckoutRequest;
import com.shopease.checkout.dto.response.CheckoutResponse;
import com.shopease.checkout.dto.response.OrderResponse;
import com.shopease.checkout.dto.response.ProductResponse;
import com.shopease.checkout.dto.response.UserProfileResponse;
import com.shopease.checkout.entity.UserEntity;
import com.shopease.checkout.mapper.ProductMapper;
import com.shopease.checkout.mapper.UserMapper;
import com.shopease.checkout.repository.ProductRepository;
import com.shopease.checkout.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Orders & Products")
public class OrderController {

    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService,
                           ProductRepository productRepository,
                           UserRepository userRepository) {
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/orders/checkout")
    @Operation(summary = "Checkout", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request,
                                                     Authentication authentication) {
        var user = (UserEntity) authentication.getPrincipal();
        var result = orderService.checkout(user, request);
        return result.success() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Get order details", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String id) {
        return orderService.findById(id)
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

    @GetMapping("/users")
    @Operation(summary = "List demo users (for account switching)")
    public List<UserProfileResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toProfileResponse)
                .toList();
    }
}
