package com.jose.membershiprules.member;

import com.jose.membershiprules.domain.MembershipType;

import java.time.LocalDate;

public record Member(
        String memberNumber,
        String accessCode,
        MembershipType membershipType,
        boolean active,
        LocalDate memberSince
) {
}
