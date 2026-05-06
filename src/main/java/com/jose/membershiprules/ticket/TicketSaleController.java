package com.jose.membershiprules.ticket;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketSaleController {

    private final TicketSaleService ticketSaleService;

    public TicketSaleController(TicketSaleService ticketSaleService) {
        this.ticketSaleService = ticketSaleService;
    }

    @PostMapping("/api/tickets/normal-match/purchases")
    public TicketSaleResponse requestTicket(@Valid @RequestBody TicketSaleRequest request) {
        return ticketSaleService.requestTicket(request);
    }
}
