package com.jose.membershiprules.finalallocation;

import com.jose.membershiprules.domain.RuleResultStatus;

import java.time.LocalDate;

public record FinalTicketApplication(
        String memberNumber,
        LocalDate memberSince,
        RuleResultStatus status
) {
}
