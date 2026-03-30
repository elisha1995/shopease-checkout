package com.shopease.checkout.shipping;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.CartItemDto;
import com.shopease.checkout.dto.response.ShippingQuoteResponse;
import com.shopease.checkout.shipping.discount.ShippingDiscountChain;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ShippingServiceImpl implements ShippingService {

    private final Map<String, ShippingStrategy> strategies;
    private final ShippingDiscountChain discountChain;

    public ShippingServiceImpl(List<ShippingStrategy> strategyList, ShippingDiscountChain discountChain) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(ShippingStrategy::getKey, Function.identity()));
        this.discountChain = discountChain;
    }

    @Override
    public ShippingQuoteResponse calculate(String method, List<CartItemDto> items, MembershipTier tier) {
        var strategy = strategies.get(method.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown shipping method: " + method);
        }

        double baseCost = strategy.calculateBaseCost(items);
        var result = discountChain.applyDiscounts(baseCost, items, tier);

        return new ShippingQuoteResponse(
                strategy.getKey(),
                strategy.getLabel(),
                Math.round(baseCost * 100.0) / 100.0,
                result.finalCost(),
                result.appliedDiscounts()
        );
    }

    @Override
    public List<ShippingQuoteResponse> calculateAll(List<CartItemDto> items, MembershipTier tier) {
        return strategies.values().stream()
                .map(s -> calculate(s.getKey(), items, tier))
                .toList();
    }
}
