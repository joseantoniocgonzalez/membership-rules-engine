package com.jose.membershiprules.store;

import com.jose.membershiprules.domain.MembershipType;
import com.jose.membershiprules.domain.RuleResultStatus;
import com.jose.membershiprules.member.Member;
import com.jose.membershiprules.member.MemberAccessValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StoreDiscountService {

    private final MemberAccessValidator memberAccessValidator;

    public StoreDiscountService(MemberAccessValidator memberAccessValidator) {
        this.memberAccessValidator = memberAccessValidator;
    }

    public StoreDiscountResponse calculateDiscount(StoreDiscountRequest request) {
        return memberAccessValidator.validate(request.memberNumber(), request.accessCode())
                .map(member -> buildDiscountResponse(request, member))
                .orElseGet(() -> invalidAccessResponse(request));
    }

    private StoreDiscountResponse buildDiscountResponse(StoreDiscountRequest request, Member member) {
        int discountPercentage = discountPercentageFor(member.membershipType());
        BigDecimal finalPrice = applyDiscount(request.basePrice(), discountPercentage);

        return new StoreDiscountResponse(
                RuleResultStatus.CONFIRMED,
                request.productName(),
                request.basePrice(),
                discountPercentage,
                finalPrice
        );
    }

    private int discountPercentageFor(MembershipType membershipType) {
        return switch (membershipType) {
            case SEASON_TICKET_HOLDER, PREMIUM_MEMBER -> 20;
            case STANDARD_MEMBER -> 10;
            case FREE_MEMBER -> 0;
        };
    }

    private BigDecimal applyDiscount(BigDecimal basePrice, int discountPercentage) {
        BigDecimal discountMultiplier = BigDecimal.valueOf(100 - discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return basePrice.multiply(discountMultiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private StoreDiscountResponse invalidAccessResponse(StoreDiscountRequest request) {
        return new StoreDiscountResponse(
                RuleResultStatus.INVALID_MEMBER_ACCESS,
                request.productName(),
                request.basePrice(),
                0,
                request.basePrice()
        );
    }
}
