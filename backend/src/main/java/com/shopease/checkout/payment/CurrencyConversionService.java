package com.shopease.checkout.payment;

import com.shopease.checkout.common.model.Currency;

/** SRP: single responsibility — currency math only. */
public interface CurrencyConversionService {
    double convertFromUsd(double amountInUsd, Currency target);
    double convertToUsd(double amount, Currency source);
    double convert(double amount, Currency from, Currency to);
}
