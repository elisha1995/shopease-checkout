package com.shopease.checkout.payment;

import com.shopease.checkout.common.model.Currency;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@ConfigurationProperties(prefix = "currency")
public class CurrencyConversionServiceImpl implements CurrencyConversionService {

    private Map<String, Double> rates;

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }

    @Override
    public double getRate(Currency currency) {
        return rates.getOrDefault(currency.name(), 1.0);
    }

    @Override
    public double convertFromUsd(double amountInUsd, Currency target) {
        if (target == Currency.USD) return round(amountInUsd);
        return round(amountInUsd * getRate(target));
    }

    @Override
    public double convertToUsd(double amount, Currency source) {
        if (source == Currency.USD) return round(amount);
        return round(amount / getRate(source));
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