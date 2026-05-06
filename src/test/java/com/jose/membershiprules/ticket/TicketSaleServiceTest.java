package com.jose.membershiprules.ticket;

import com.jose.membershiprules.domain.MembershipType;
import com.jose.membershiprules.domain.RuleResultStatus;
import com.jose.membershiprules.member.Member;
import com.jose.membershiprules.member.MemberAccessValidator;
import com.jose.membershiprules.member.MemberDirectory;
import com.jose.membershiprules.ticket.infrastructure.InMemoryTicketPurchaseRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TicketSaleServiceTest {

    private TicketSaleService ticketSaleService;

    @BeforeEach
    void setUp() {
        Map<String, Member> members = Map.of(
                "BHFC-1001", new Member("BHFC-1001", "111111", MembershipType.SEASON_TICKET_HOLDER, true),
                "BHFC-2045", new Member("BHFC-2045", "482910", MembershipType.PREMIUM_MEMBER, true),
                "BHFC-3107", new Member("BHFC-3107", "739204", MembershipType.STANDARD_MEMBER, true),
                "BHFC-4880", new Member("BHFC-4880", "105377", MembershipType.FREE_MEMBER, true),
                "BHFC-9001", new Member("BHFC-9001", "900100", MembershipType.PREMIUM_MEMBER, false)
        );

        MemberDirectory memberDirectory = memberNumber -> Optional.ofNullable(members.get(memberNumber));
        MemberAccessValidator memberAccessValidator = new MemberAccessValidator(memberDirectory);
        TicketPurchaseRegistry ticketPurchaseRegistry = new InMemoryTicketPurchaseRegistry();

        ticketSaleService = new TicketSaleService(memberAccessValidator, ticketPurchaseRegistry);
    }

    @Test
    void shouldConfirmPremiumMemberPurchaseWhenWindowIsOpen() {
        TicketSaleRequest request = request("BHFC-2045", "482910", LocalDate.of(2026, 7, 2), 100);

        TicketSaleResponse response = ticketSaleService.requestTicket(request);

        assertThat(response.status()).isEqualTo(RuleResultStatus.CONFIRMED);
        assertThat(response.matchCode()).isEqualTo("BHFC-NCFC-2026");
        assertThat(response.matchName()).isEqualTo("Bristol Harbour FC vs Northampton Cobblers FC");
        assertThat(response.memberNumber()).isEqualTo("BHFC-2045");
    }

    @Test
    void shouldReturnAlreadyIncludedForSeasonTicketHolder() {
        TicketSaleRequest request = request("BHFC-1001", "111111", LocalDate.of(2026, 7, 2), 100);

        TicketSaleResponse response = ticketSaleService.requestTicket(request);

        assertThat(response.status()).isEqualTo(RuleResultStatus.ALREADY_INCLUDED);
        assertThat(response.message()).isEqualTo(
                "Season Ticket Holders already have access included for this home match."
        );
    }

    @Test
    void shouldRejectStandardMemberBeforeSaleWindowOpens() {
        TicketSaleRequest request = request("BHFC-3107", "739204", LocalDate.of(2026, 7, 2), 100);

        TicketSaleResponse response = ticketSaleService.requestTicket(request);

        assertThat(response.status()).isEqualTo(RuleResultStatus.NOT_YET_AVAILABLE);
        assertThat(response.message()).isEqualTo(
                "The ticket sale window is not open for this membership type."
        );
    }

    @Test
    void shouldRejectFreeMemberWhenTicketsAreSoldOut() {
        TicketSaleRequest request = request("BHFC-4880", "105377", LocalDate.of(2026, 7, 16), 0);

        TicketSaleResponse response = ticketSaleService.requestTicket(request);

        assertThat(response.status()).isEqualTo(RuleResultStatus.SOLD_OUT);
        assertThat(response.message()).isEqualTo("No tickets are available for this match.");
    }

    @Test
    void shouldRejectDuplicatePurchaseForSameMemberAndMatch() {
        TicketSaleRequest request = request("BHFC-2045", "482910", LocalDate.of(2026, 7, 2), 100);

        TicketSaleResponse firstResponse = ticketSaleService.requestTicket(request);
        TicketSaleResponse secondResponse = ticketSaleService.requestTicket(request);

        assertThat(firstResponse.status()).isEqualTo(RuleResultStatus.CONFIRMED);
        assertThat(secondResponse.status()).isEqualTo(RuleResultStatus.DUPLICATE_REQUEST);
        assertThat(secondResponse.message()).isEqualTo(
                "This member has already purchased a ticket for this match."
        );
    }

    @Test
    void shouldRejectInactiveMember() {
        TicketSaleRequest request = request("BHFC-9001", "900100", LocalDate.of(2026, 7, 2), 100);

        TicketSaleResponse response = ticketSaleService.requestTicket(request);

        assertThat(response.status()).isEqualTo(RuleResultStatus.REJECTED);
        assertThat(response.message()).isEqualTo("Inactive members cannot purchase match tickets.");
    }

    @Test
    void shouldRejectTicketRequestWhenAccessCodeIsInvalid() {
        TicketSaleRequest request = request("BHFC-2045", "000000", LocalDate.of(2026, 7, 2), 100);

        TicketSaleResponse response = ticketSaleService.requestTicket(request);

        assertThat(response.status()).isEqualTo(RuleResultStatus.INVALID_MEMBER_ACCESS);
        assertThat(response.message()).isEqualTo("The member number or access code is invalid.");
    }

    private TicketSaleRequest request(
            String memberNumber,
            String accessCode,
            LocalDate requestDate,
            int remainingTickets
    ) {
        return new TicketSaleRequest(memberNumber, accessCode, requestDate, remainingTickets);
    }
}
