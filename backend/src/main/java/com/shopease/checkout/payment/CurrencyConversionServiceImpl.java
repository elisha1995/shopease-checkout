package com.shopease.checkout.payment;

import com.shopease.checkout.common.model.Currency;
import org.springframework.stereotype.Service;

@Service
public class CurrencyConversionServiceImpl implements CurrencyConversionService {

    @Override
    public double convertFromUsd(double amountInUsd, Currency target) {
        return switch (target) {
            case USD -> round(amountInUsd);
            default  -> round(amountInUsd * target.getRateToUsd());
        };
    }

    @Override
    public double convertToUsd(double amount, Currency source) {
        return switch (source) {
            case USD -> round(amount);
            default  -> round(amount / source.getRateToUsd());
        };
    }

    @Override
    public double convert(double amount, Currency from, Currency to) {
        if (from == to) return round(amount);
        return convertFromUsd(convertToUsd(amount, from), to);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
