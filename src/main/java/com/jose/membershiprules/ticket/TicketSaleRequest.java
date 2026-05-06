package com.jose.membershiprules.ticket;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TicketSaleRequest(
        @NotBlank
        String memberNumber,

        @NotBlank
        String accessCode,

        @NotNull
        LocalDate requestDate,

        @Min(0)
        int remainingTickets
) {
}
