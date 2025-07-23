package com.company.sharedkernel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Money Value Object Tests")
class MoneyTest {

    private final Currency USD = Currency.getInstance("USD");
    private final Currency EUR = Currency.getInstance("EUR");

    @Test
    @DisplayName("Should create money with positive amount")
    void shouldCreateMoneyWithPositiveAmount() {
        Money money = Money.of(new BigDecimal("100.50"), USD);

        assertEquals(new BigDecimal("100.50"), money.getAmount());
        assertEquals(USD, money.getCurrency());
    }

    @Test
    @DisplayName("Should create zero money")
    void shouldCreateZeroMoney() {
        Money money = Money.zero(USD);

        assertTrue(money.isZero());
        assertEquals(USD, money.getCurrency());
    }

    @Test
    @DisplayName("Should add money with same currency")
    void shouldAddMoneyWithSameCurrency() {
        Money money1 = Money.of(new BigDecimal("100.00"), USD);
        Money money2 = Money.of(new BigDecimal("50.00"), USD);

        Money result = money1.add(money2);

        assertEquals(new BigDecimal("150.00"), result.getAmount());
        assertEquals(USD, result.getCurrency());
    }

    @Test
    @DisplayName("Should throw exception when adding different currencies")
    void shouldThrowExceptionWhenAddingDifferentCurrencies() {
        Money usdMoney = Money.of(new BigDecimal("100.00"), USD);
        Money eurMoney = Money.of(new BigDecimal("50.00"), EUR);

        assertThrows(IllegalArgumentException.class, () -> usdMoney.add(eurMoney));
    }

    @Test
    @DisplayName("Should multiply money by factor")
    void shouldMultiplyMoneyByFactor() {
        Money money = Money.of(new BigDecimal("100.00"), USD);

        Money result = money.multiply(new BigDecimal("2.5"));

        assertEquals(new BigDecimal("250.000"), result.getAmount());
    }

    @Test
    @DisplayName("Should identify positive money")
    void shouldIdentifyPositiveMoney() {
        Money money = Money.of(new BigDecimal("0.01"), USD);

        assertTrue(money.isPositive());
    }

    @Test
    @DisplayName("Should throw exception for null amount")
    void shouldThrowExceptionForNullAmount() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(null, USD));
    }

    @Test
    @DisplayName("Should throw exception for null currency")
    void shouldThrowExceptionForNullCurrency() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(BigDecimal.TEN, null));
    }

    @Test
    @DisplayName("Should support equality and hash code")
    void shouldSupportEqualityAndHashCode() {
        Money money1 = Money.of(new BigDecimal("100.00"), USD);
        Money money2 = Money.of(new BigDecimal("100.00"), USD);
        Money money3 = Money.of(new BigDecimal("200.00"), USD);

        assertEquals(money1, money2);
        assertEquals(money1.hashCode(), money2.hashCode());
        assertNotEquals(money1, money3);
    }

    @Test
    @DisplayName("Should create USD money using convenience method")
    void shouldCreateUsdMoneyUsingConvenienceMethod() {
        Money money = Money.usd(new BigDecimal("99.99"));

        assertEquals(new BigDecimal("99.99"), money.getAmount());
        assertEquals(USD, money.getCurrency());
    }

    @Test
    @DisplayName("Should subtract money with same currency")
    void shouldSubtractMoneyWithSameCurrency() {
        Money money1 = Money.of(new BigDecimal("100.00"), USD);
        Money money2 = Money.of(new BigDecimal("30.00"), USD);

        Money result = money1.subtract(money2);

        assertEquals(new BigDecimal("70.00"), result.getAmount());
        assertEquals(USD, result.getCurrency());
    }

}