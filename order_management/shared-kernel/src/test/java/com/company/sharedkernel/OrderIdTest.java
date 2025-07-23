package com.company.sharedkernel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderId Value Object Tests")
class OrderIdTest {

    @Test
    @DisplayName("Should generate unique OrderId")
    void shouldGenerateUniqueOrderId() {
        OrderId orderId1 = OrderId.generate();
        OrderId orderId2 = OrderId.generate();

        assertNotNull(orderId1);
        assertNotNull(orderId2);
        assertNotEquals(orderId1, orderId2);
        assertNotEquals(orderId1.getValue(), orderId2.getValue());
    }

    @Test
    @DisplayName("Should create OrderId from UUID")
    void shouldCreateOrderIdFromUUID() {
        UUID uuid = UUID.randomUUID();
        OrderId orderId = OrderId.of((UUID) uuid);

        assertNotNull(orderId);
        assertEquals(uuid, orderId.getValue());
    }

    @Test
    @DisplayName("Should throw exception for null UUID")
    void shouldThrowExceptionForNullUUID() {
        assertThrows(IllegalArgumentException.class, () -> OrderId.of((UUID) null));
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        UUID uuid = UUID.randomUUID();
        OrderId orderId1 = OrderId.of((UUID) uuid);
        OrderId orderId2 = OrderId.of((UUID) uuid);
        OrderId orderId3 = OrderId.of((UUID) UUID.randomUUID());

        assertEquals(orderId1, orderId2);
        assertEquals(orderId1.hashCode(), orderId2.hashCode());
        assertNotEquals(orderId1, orderId3);
        assertNotEquals(orderId1.hashCode(), orderId3.hashCode());
    }

    @Test
    @DisplayName("Should have consistent toString")
    void shouldHaveConsistentToString() {
        UUID uuid = UUID.randomUUID();
        OrderId orderId = OrderId.of((UUID) uuid);

        String toString = orderId.toString();
        assertNotNull(toString);
        assertTrue(toString.contains(uuid.toString()));
    }
}