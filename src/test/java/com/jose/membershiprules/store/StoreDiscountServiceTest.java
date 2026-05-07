package com.jose.membershiprules.store;

import com.jose.membershiprules.domain.MembershipType;
import com.jose.membershiprules.domain.RuleResultStatus;
import com.jose.membershiprules.member.Member;
import com.jose.membershiprules.member.MemberAccessValidator;
import com.jose.membershiprules.member.MemberDirectory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class StoreDiscountServiceTest {

    private StoreDiscountService storeDiscountService;

    @BeforeEach
    void setUp() {
        Map<String, Member> members = Map.of(
                "BHFC-1001", new Member("BHFC-1001", "111111", MembershipType.SEASON_TICKET_HOLDER, true, java.time.LocalDate.of(2015, 6, 1)),
                "BHFC-2045", new Member("BHFC-2045", "482910", MembershipType.PREMIUM_MEMBER, true, java.time.LocalDate.of(2021, 7, 14)),
                "BHFC-3107", new Member("BHFC-3107", "739204", MembershipType.STANDARD_MEMBER, true, java.time.LocalDate.of(2023, 2, 10)),
                "BHFC-4880", new Member("BHFC-4880", "105377", MembershipType.FREE_MEMBER, true, java.time.LocalDate.of(2024, 9, 3))
        );

        MemberDirectory memberDirectory = memberNumber -> Optional.ofNullable(members.get(memberNumber));
        MemberAccessValidator memberAccessValidator = new MemberAccessValidator(memberDirectory);

        storeDiscountService = new StoreDiscountService(memberAccessValidator);
    }

    @ParameterizedTest
    @CsvSource({
            "BHFC-1001,111111,20,60.00",
            "BHFC-2045,482910,20,60.00",
            "BHFC-3107,739204,10,67.50",
            "BHFC-4880,105377,0,75.00"
    })
    void shouldCalculateStoreDiscountByMembershipType(
            String memberNumber,
            String accessCode,
            int expectedDiscountPercentage,
            String expectedFinalPrice
    ) {
        StoreDiscountRequest request = new StoreDiscountRequest(
                memberNumber,
                accessCode,
                "Home Shirt",
                new BigDecimal("75.00")
        );

        StoreDiscountResponse response = storeDiscountService.calculateDiscount(request);

        assertThat(response.status()).isEqualTo(RuleResultStatus.CONFIRMED);
        assertThat(response.productName()).isEqualTo("Home Shirt");
        assertThat(response.basePrice()).isEqualByComparingTo("75.00");
        assertThat(response.discountPercentage()).isEqualTo(expectedDiscountPercentage);
        assertThat(response.finalPrice()).isEqualByComparingTo(expectedFinalPrice);
    }

    @Test
    void shouldRejectDiscountWhenAccessCodeIsInvalid() {
        StoreDiscountRequest request = new StoreDiscountRequest(
                "BHFC-2045",
                "000000",
                "Home Shirt",
                new BigDecimal("75.00")
        );

        StoreDiscountResponse response = storeDiscountService.calculateDiscount(request);

        assertThat(response.status()).isEqualTo(RuleResultStatus.INVALID_MEMBER_ACCESS);
        assertThat(response.discountPercentage()).isZero();
        assertThat(response.finalPrice()).isEqualByComparingTo("75.00");
    }

    @Test
    void shouldRejectDiscountWhenMemberNumberDoesNotExist() {
        StoreDiscountRequest request = new StoreDiscountRequest(
                "BHFC-9999",
                "482910",
                "Home Shirt",
                new BigDecimal("75.00")
        );

        StoreDiscountResponse response = storeDiscountService.calculateDiscount(request);

        assertThat(response.status()).isEqualTo(RuleResultStatus.INVALID_MEMBER_ACCESS);
        assertThat(response.discountPercentage()).isZero();
        assertThat(response.finalPrice()).isEqualByComparingTo("75.00");
    }
}
