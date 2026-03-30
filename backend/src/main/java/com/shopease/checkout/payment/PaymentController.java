package com.shopease.checkout.payment;

import com.shopease.checkout.common.model.Currency;
import com.shopease.checkout.dto.response.CurrencyResponse;
import com.shopease.checkout.dto.response.PaymentMethodResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/payment")
@Tag(name = "Payment")
public class PaymentController {

    private final PaymentProcessorFactory factory;

    public PaymentController(PaymentProcessorFactory factory) {
        this.factory = factory;
    }

    @GetMapping("/methods")
    @Operation(summary = "List available payment methods")
    public List<PaymentMethodResponse> methods() {
        return factory.availableMethods();
    }

    @GetMapping("/currencies")
    @Operation(summary = "List supported currencies and exchange rates")
    public List<CurrencyResponse> currencies() {
        return Arrays.stream(Currency.values())
                .map(c -> new CurrencyResponse(c.name(), c.getRateToUsd()))
                .toList();
    }
}
