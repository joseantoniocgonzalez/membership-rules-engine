package com.jose.membershiprules.member;

import com.jose.membershiprules.domain.MembershipType;

public record Member(
        String memberNumber,
        String accessCode,
        MembershipType membershipType,
        boolean active
) {
}
