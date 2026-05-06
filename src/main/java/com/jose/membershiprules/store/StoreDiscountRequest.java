package com.jose.membershiprules.store;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StoreDiscountRequest(
        @NotBlank
        String memberNumber,

        @NotBlank
        String accessCode,

        @NotBlank
        String productName,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal basePrice
) {
}
