package com.jose.membershiprules.store;

import com.jose.membershiprules.domain.RuleResultStatus;

import java.math.BigDecimal;

public record StoreDiscountResponse(
        RuleResultStatus status,
        String productName,
        BigDecimal basePrice,
        int discountPercentage,
        BigDecimal finalPrice
) {
}
