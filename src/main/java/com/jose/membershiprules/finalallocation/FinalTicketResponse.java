package com.jose.membershiprules.finalallocation;

import com.jose.membershiprules.domain.RuleResultStatus;

public record FinalTicketResponse(
        RuleResultStatus status,
        String finalCode,
        String finalName,
        String venue,
        String memberNumber,
        String message
) {
}
