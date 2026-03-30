package com.shopease.checkout.payment;

import org.junit.jupiter.api.Test;
import com.shopease.checkout.common.model.Currency;
import static org.junit.jupiter.api.Assertions.*;

class CurrencyConversionServiceTest {

    private final CurrencyConversionService service = new CurrencyConversionServiceImpl();

    @Test void usdToUsd_noConversion()  { assertEquals(100.0, service.convertFromUsd(100.0, Currency.USD)); }
    @Test void usdToGhs()               { assertEquals(1550.0, service.convertFromUsd(100.0, Currency.GHS)); }
    @Test void usdToEur()               { assertEquals(92.0, service.convertFromUsd(100.0, Currency.EUR)); }
    @Test void ghsToUsd()               { assertEquals(10.0, service.convertToUsd(155.0, Currency.GHS)); }
    @Test void eurToUsd()               { assertEquals(100.0, service.convertToUsd(92.0, Currency.EUR)); }
    @Test void ghsToEur_crossConvert()  { assertEquals(9.2, service.convert(155.0, Currency.GHS, Currency.EUR), 0.01); }
    @Test void sameCurrency_noConvert()  { assertEquals(42.50, service.convert(42.50, Currency.GHS, Currency.GHS)); }

    @Test void shouldRoundToTwoDecimals() {
        assertEquals(51.62, service.convertFromUsd(3.33, Currency.GHS), 0.001);
    }
}
