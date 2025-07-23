package com.company.customerorder.domain.model;

import com.company.sharedkernel.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderItem Value Object Tests")
class OrderItemTest {

    @Test
    @DisplayName("Should create valid OrderItem")
    void shouldCreateValidOrderItem() {
        Money price = Money.of(new BigDecimal("50.00"), Currency.getInstance("USD"));
        
        OrderItem item = OrderItem.of(
            "PROD-001",
            "Test Product",
            2,
            price
        );

        assertNotNull(item);
        assertEquals("PROD-001", item.getProductCode());
        assertEquals("Test Product", item.getDescription());
        assertEquals(2, item.getQuantity());
        assertEquals(price, item.getUnitPrice());
        assertEquals(Money.of(new BigDecimal("100.00"), Currency.getInstance("USD")), item.getTotalPrice());
    }

    @Test
    @DisplayName("Should throw exception for null product code")
    void shouldThrowExceptionForNullProductCode() {
        Money price = Money.of(BigDecimal.TEN, Currency.getInstance("USD"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            OrderItem.of(null, "Product", 1, price));
    }

    @Test
    @DisplayName("Should throw exception for empty product code")
    void shouldThrowExceptionForEmptyProductCode() {
        Money price = Money.of(BigDecimal.TEN, Currency.getInstance("USD"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            OrderItem.of("", "Product", 1, price));
    }

    @Test
    @DisplayName("Should throw exception for null description")
    void shouldThrowExceptionForNullDescription() {
        Money price = Money.of(BigDecimal.TEN, Currency.getInstance("USD"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            OrderItem.of("PROD-001", null, 1, price));
    }

    @Test
    @DisplayName("Should throw exception for zero quantity")
    void shouldThrowExceptionForZeroQuantity() {
        Money price = Money.of(BigDecimal.TEN, Currency.getInstance("USD"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            OrderItem.of("PROD-001", "Product", 0, price));
    }

    @Test
    @DisplayName("Should throw exception for negative quantity")
    void shouldThrowExceptionForNegativeQuantity() {
        Money price = Money.of(BigDecimal.TEN, Currency.getInstance("USD"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            OrderItem.of("PROD-001", "Product", -1, price));
    }

    @Test
    @DisplayName("Should throw exception for null price")
    void shouldThrowExceptionForNullPrice() {
        assertThrows(IllegalArgumentException.class, () -> 
            OrderItem.of("PROD-001", "Product", 1, null));
    }

    @Test
    @DisplayName("Should calculate total price correctly")
    void shouldCalculateTotalPriceCorrectly() {
        Money unitPrice = Money.of(new BigDecimal("25.50"), Currency.getInstance("USD"));
        OrderItem item = OrderItem.of("PROD-001", "Product", 3, unitPrice);

        Money expectedTotal = Money.of(new BigDecimal("76.50"), Currency.getInstance("USD"));
        assertEquals(expectedTotal, item.getTotalPrice());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        Money price = Money.of(BigDecimal.TEN, Currency.getInstance("USD"));
        
        OrderItem item1 = OrderItem.of("PROD-001", "Product", 1, price);
        OrderItem item2 = OrderItem.of("PROD-001", "Product", 1, price);
        OrderItem item3 = OrderItem.of("PROD-002", "Product", 1, price);

        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
    }
}