package com.shopease.checkout.payment;

import com.shopease.checkout.common.model.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyConversionServiceImplTest {

    private CurrencyConversionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CurrencyConversionServiceImpl();
        service.setRates(Map.of(
                "USD", 1.0,
                "GHS", 15.0,
                "EUR", 0.92,
                "GBP", 0.79
        ));
    }

    @Test
    void convertFromUsdReturnsAmountForUsd() {
        assertEquals(100.0, service.convertFromUsd(
                100.0, Currency.USD));
    }

    @Test
    void convertFromUsdAppliesRate() {
        assertEquals(1500.0, service.convertFromUsd(100.0,
                Currency.GHS));
    }

    @Test
    void convertToUsdReturnsAmountForUsd() {
        assertEquals(100.0, service.convertToUsd(100.0,
                Currency.USD));
    }

    @Test
    void convertToUsdDividesByRate() {
        double result = service.convertToUsd(1500.0, Currency.GHS);
        assertEquals(100.0, result);
    }

    @Test
    void convertBetweenSameCurrencyReturnsRoundedAmount() {
        assertEquals(49.99, service.convert(49.99, Currency.GHS, Currency.GHS));
    }

    @Test
    void convertBetweenDifferentCurrencies() {
        // 100 GHS → USD → EUR: 100 / 15.0 = 6.67 USD (rounded) * 0.92 = 6.14 EUR
        double result = service.convert(100.0, Currency.GHS, Currency.EUR);
        assertEquals(6.14, result);
    }

    @Test
    void getRateReturnsConfiguredRate() {
        assertEquals(15.0, service.getRate(Currency.GHS));
        assertEquals(0.92, service.getRate(Currency.EUR));
    }

    @Test
    void getRateDefaultsToOneForUnknownCurrency() {
        // Set rates without a specific currency
        service.setRates(Map.of("USD", 1.0));
        assertEquals(1.0, service.getRate(Currency.GHS));
    }

    @Test
    void resultIsRoundedToTwoDecimalPlaces() {
        // 33.33 USD * 15.0 = 499.95 GHS
        double result = service.convertFromUsd(33.33, Currency.GHS);
        assertEquals(499.95, result);
    }
}
