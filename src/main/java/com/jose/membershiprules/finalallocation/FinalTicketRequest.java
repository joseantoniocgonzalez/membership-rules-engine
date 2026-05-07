package com.jose.membershiprules.finalallocation;

import jakarta.validation.constraints.NotBlank;

public record FinalTicketRequest(
        @NotBlank
        String memberNumber,

        @NotBlank
        String accessCode
) {
}
