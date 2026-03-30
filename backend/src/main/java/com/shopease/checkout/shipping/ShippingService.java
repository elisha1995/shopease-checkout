package com.shopease.checkout.shipping;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.CartItemDto;
import com.shopease.checkout.dto.response.ShippingQuoteResponse;
import java.util.List;

public interface ShippingService {

    ShippingQuoteResponse calculate(String method, List<CartItemDto> items, MembershipTier tier);

    List<ShippingQuoteResponse> calculateAll(List<CartItemDto> items, MembershipTier tier);
}
