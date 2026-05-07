package com.jose.membershiprules.finalallocation;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FinalTicketAllocationController {

    private final FinalTicketAllocationService finalTicketAllocationService;

    public FinalTicketAllocationController(FinalTicketAllocationService finalTicketAllocationService) {
        this.finalTicketAllocationService = finalTicketAllocationService;
    }

    @PostMapping("/api/final-tickets/requests")
    public FinalTicketResponse requestFinalTicket(@Valid @RequestBody FinalTicketRequest request) {
        return finalTicketAllocationService.requestFinalTicket(request);
    }

    @PostMapping("/api/final-tickets/cancellations")
    public FinalTicketCancellationResponse cancelFinalTicket(@Valid @RequestBody FinalTicketRequest request) {
        return finalTicketAllocationService.cancelFinalTicket(request);
    }
}
