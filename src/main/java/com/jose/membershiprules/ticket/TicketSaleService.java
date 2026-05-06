package com.jose.membershiprules.ticket;

import com.jose.membershiprules.domain.MembershipType;
import com.jose.membershiprules.domain.RuleResultStatus;
import com.jose.membershiprules.member.Member;
import com.jose.membershiprules.member.MemberAccessValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TicketSaleService {

    private static final LocalDate PREMIUM_WINDOW_START = LocalDate.of(2026, 7, 1);
    private static final LocalDate STANDARD_WINDOW_START = LocalDate.of(2026, 7, 8);
    private static final LocalDate FREE_WINDOW_START = LocalDate.of(2026, 7, 15);

    private final MemberAccessValidator memberAccessValidator;
    private final TicketPurchaseRegistry ticketPurchaseRegistry;

    public TicketSaleService(
            MemberAccessValidator memberAccessValidator,
            TicketPurchaseRegistry ticketPurchaseRegistry
    ) {
        this.memberAccessValidator = memberAccessValidator;
        this.ticketPurchaseRegistry = ticketPurchaseRegistry;
    }

    public TicketSaleResponse requestTicket(TicketSaleRequest request) {
        NormalMatch match = NormalMatch.bristolHarbourVsNorthamptonCobblers();

        return memberAccessValidator.validate(request.memberNumber(), request.accessCode())
                .map(member -> evaluateTicketRequest(request, match, member))
                .orElseGet(() -> response(
                        RuleResultStatus.INVALID_MEMBER_ACCESS,
                        match,
                        request.memberNumber(),
                        "The member number or access code is invalid."
                ));
    }

    private TicketSaleResponse evaluateTicketRequest(
            TicketSaleRequest request,
            NormalMatch match,
            Member member
    ) {
        if (!member.active()) {
            return response(
                    RuleResultStatus.REJECTED,
                    match,
                    member.memberNumber(),
                    "Inactive members cannot purchase match tickets."
            );
        }

        if (member.membershipType() == MembershipType.SEASON_TICKET_HOLDER) {
            return response(
                    RuleResultStatus.ALREADY_INCLUDED,
                    match,
                    member.memberNumber(),
                    "Season Ticket Holders already have access included for this home match."
            );
        }

        if (ticketPurchaseRegistry.hasPurchase(member.memberNumber(), match.matchCode())) {
            return response(
                    RuleResultStatus.DUPLICATE_REQUEST,
                    match,
                    member.memberNumber(),
                    "This member has already purchased a ticket for this match."
            );
        }

        if (request.requestDate().isBefore(windowStartFor(member.membershipType()))) {
            return response(
                    RuleResultStatus.NOT_YET_AVAILABLE,
                    match,
                    member.memberNumber(),
                    "The ticket sale window is not open for this membership type."
            );
        }

        if (request.remainingTickets() <= 0) {
            return response(
                    RuleResultStatus.SOLD_OUT,
                    match,
                    member.memberNumber(),
                    "No tickets are available for this match."
            );
        }

        ticketPurchaseRegistry.recordPurchase(member.memberNumber(), match.matchCode());

        return response(
                RuleResultStatus.CONFIRMED,
                match,
                member.memberNumber(),
                "Ticket purchase confirmed."
        );
    }

    private LocalDate windowStartFor(MembershipType membershipType) {
        return switch (membershipType) {
            case PREMIUM_MEMBER -> PREMIUM_WINDOW_START;
            case STANDARD_MEMBER -> STANDARD_WINDOW_START;
            case FREE_MEMBER -> FREE_WINDOW_START;
            case SEASON_TICKET_HOLDER -> LocalDate.MIN;
        };
    }

    private TicketSaleResponse response(
            RuleResultStatus status,
            NormalMatch match,
            String memberNumber,
            String message
    ) {
        return new TicketSaleResponse(
                status,
                match.matchCode(),
                match.displayName(),
                memberNumber,
                message
        );
    }
}
