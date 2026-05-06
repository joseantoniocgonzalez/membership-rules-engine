package com.jose.membershiprules.ticket;

import com.jose.membershiprules.domain.RuleResultStatus;

public record TicketSaleResponse(
        RuleResultStatus status,
        String matchCode,
        String matchName,
        String memberNumber,
        String message
) {
}
