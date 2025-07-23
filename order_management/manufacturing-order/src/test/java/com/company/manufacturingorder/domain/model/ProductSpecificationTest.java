package com.company.manufacturingorder.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductSpecification Value Object Tests")
class ProductSpecificationTest {

    @Test
    @DisplayName("Should create valid ProductSpecification")
    void shouldCreateValidProductSpecification() {
        ProductSpecification spec = ProductSpecification.of(
            "PROD-001",
            "Test Product",
            10,
            "Standard specifications"
        );

        assertNotNull(spec);
        assertEquals("PROD-001", spec.getProductCode());
        assertEquals("Test Product", spec.getDescription());
        assertEquals(10, spec.getQuantity());
        assertEquals("Standard specifications", spec.getSpecifications());
    }

    @Test
    @DisplayName("Should throw exception for null product code")
    void shouldThrowExceptionForNullProductCode() {
        assertThrows(IllegalArgumentException.class, () -> 
            ProductSpecification.of(null, "Description", 10, "Specs"));
    }

    @Test
    @DisplayName("Should throw exception for empty product code")
    void shouldThrowExceptionForEmptyProductCode() {
        assertThrows(IllegalArgumentException.class, () -> 
            ProductSpecification.of("", "Description", 10, "Specs"));
    }

    @Test
    @DisplayName("Should throw exception for null description")
    void shouldThrowExceptionForNullDescription() {
        assertThrows(IllegalArgumentException.class, () -> 
            ProductSpecification.of("PROD-001", null, 10, "Specs"));
    }

    @Test
    @DisplayName("Should throw exception for negative quantity")
    void shouldThrowExceptionForNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> 
            ProductSpecification.of("PROD-001", "Description", -1, "Specs"));
    }

    @Test
    @DisplayName("Should throw exception for zero quantity")
    void shouldThrowExceptionForZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> 
            ProductSpecification.of("PROD-001", "Description", 0, "Specs"));
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        ProductSpecification spec1 = ProductSpecification.of("PROD-001", "Description", 10, "Specs");
        ProductSpecification spec2 = ProductSpecification.of("PROD-001", "Description", 10, "Specs");
        ProductSpecification spec3 = ProductSpecification.of("PROD-002", "Description", 10, "Specs");

        assertEquals(spec1, spec2);
        assertNotEquals(spec1, spec3);
    }
}