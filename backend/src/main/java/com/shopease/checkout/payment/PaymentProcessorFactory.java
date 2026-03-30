package com.shopease.checkout.payment;

import com.shopease.checkout.dto.response.PaymentMethodResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * FACTORY PATTERN: Encapsulates the decision of which PaymentProcessor to create.
 * The controller never knows about concrete adapter classes.
 */
@Component
public class PaymentProcessorFactory {

    private final Map<String, PaymentProcessor> processors;

    public PaymentProcessorFactory(List<PaymentProcessor> processorList) {
        this.processors = processorList.stream()
                .collect(Collectors.toMap(PaymentProcessor::getKey, Function.identity()));
    }

    public PaymentProcessor create(String method) {
        var processor = processors.get(method.toUpperCase());
        if (processor == null) {
            throw new IllegalArgumentException(
                    "Unknown payment method: '%s'. Available: %s".formatted(method, processors.keySet())
            );
        }
        return processor;
    }

    public List<PaymentMethodResponse> availableMethods() {
        return processors.values().stream()
                .map(p -> new PaymentMethodResponse(p.getKey(), p.getDisplayName()))
                .toList();
    }
}
