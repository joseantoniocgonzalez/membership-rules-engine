package com.jose.membershiprules.finalallocation;

import com.jose.membershiprules.domain.MembershipType;
import com.jose.membershiprules.domain.RuleResultStatus;
import com.jose.membershiprules.finalallocation.infrastructure.InMemoryFinalTicketAllocationRegistry;
import com.jose.membershiprules.member.Member;
import com.jose.membershiprules.member.MemberAccessValidator;
import com.jose.membershiprules.member.MemberDirectory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FinalTicketAllocationServiceTest {

    private FinalTicketAllocationService finalTicketAllocationService;
    private FinalTicketAllocationRegistry allocationRegistry;

    @BeforeEach
    void setUp() {
        Map<String, Member> members = Map.ofEntries(
                Map.entry("BHFC-12000", new Member(
                        "BHFC-12000", "120000", MembershipType.SEASON_TICKET_HOLDER, true,
                        LocalDate.of(2014, 5, 10)
                )),
                Map.entry("BHFC-1001", new Member(
                        "BHFC-1001", "111111", MembershipType.SEASON_TICKET_HOLDER, true,
                        LocalDate.of(2015, 6, 1)
                )),
                Map.entry("BHFC-18050", new Member(
                        "BHFC-18050", "180500", MembershipType.SEASON_TICKET_HOLDER, true,
                        LocalDate.of(2016, 9, 3)
                )),
                Map.entry("BHFC-27000", new Member(
                        "BHFC-27000", "270000", MembershipType.SEASON_TICKET_HOLDER, true,
                        LocalDate.of(2017, 3, 22)
                )),
                Map.entry("BHFC-36035", new Member(
                        "BHFC-36035", "360350", MembershipType.SEASON_TICKET_HOLDER, true,
                        LocalDate.of(2018, 8, 1)
                )),
                Map.entry("BHFC-1900", new Member(
                        "BHFC-1900", "190000", MembershipType.SEASON_TICKET_HOLDER, false,
                        LocalDate.of(2013, 4, 12)
                )),
                Map.entry("BHFC-2045", new Member(
                        "BHFC-2045", "482910", MembershipType.PREMIUM_MEMBER, true,
                        LocalDate.of(2021, 7, 14)
                )),
                Map.entry("BHFC-3107", new Member(
                        "BHFC-3107", "739204", MembershipType.STANDARD_MEMBER, true,
                        LocalDate.of(2023, 2, 10)
                )),
                Map.entry("BHFC-4880", new Member(
                        "BHFC-4880", "105377", MembershipType.FREE_MEMBER, true,
                        LocalDate.of(2024, 9, 3)
                ))
        );

        MemberDirectory memberDirectory = memberNumber -> Optional.ofNullable(members.get(memberNumber));
        MemberAccessValidator memberAccessValidator = new MemberAccessValidator(memberDirectory);
        allocationRegistry = new InMemoryFinalTicketAllocationRegistry();

        finalTicketAllocationService = new FinalTicketAllocationService(
                memberAccessValidator,
                allocationRegistry,
                3
        );
    }

    @Test
    void shouldAssignConfirmedAndWaitingListBySeasonTicketSeniority() {
        finalTicketAllocationService.requestFinalTicket(request("BHFC-36035", "360350"));
        finalTicketAllocationService.requestFinalTicket(request("BHFC-27000", "270000"));
        finalTicketAllocationService.requestFinalTicket(request("BHFC-18050", "180500"));
        finalTicketAllocationService.requestFinalTicket(request("BHFC-1001", "111111"));
        finalTicketAllocationService.requestFinalTicket(request("BHFC-12000", "120000"));

        assertThat(statusFor("BHFC-12000")).isEqualTo(RuleResultStatus.CONFIRMED);
        assertThat(statusFor("BHFC-1001")).isEqualTo(RuleResultStatus.CONFIRMED);
        assertThat(statusFor("BHFC-18050")).isEqualTo(RuleResultStatus.CONFIRMED);
        assertThat(statusFor("BHFC-27000")).isEqualTo(RuleResultStatus.WAITING_LIST);
        assertThat(statusFor("BHFC-36035")).isEqualTo(RuleResultStatus.WAITING_LIST);
    }

    @ParameterizedTest
    @CsvSource({
            "BHFC-2045,482910",
            "BHFC-3107,739204",
            "BHFC-4880,105377"
    })
    void shouldRejectNonSeasonTicketMembersAsNotEligible(String memberNumber, String accessCode) {
        FinalTicketResponse response = finalTicketAllocationService.requestFinalTicket(
                request(memberNumber, accessCode)
        );

        assertThat(response.status()).isEqualTo(RuleResultStatus.NOT_ELIGIBLE);
    }

    @Test
    void shouldRejectInactiveSeasonTicketHolder() {
        FinalTicketResponse response = finalTicketAllocationService.requestFinalTicket(
                request("BHFC-1900", "190000")
        );

        assertThat(response.status()).isEqualTo(RuleResultStatus.REJECTED);
    }

    @Test
    void shouldRejectDuplicateFinalTicketRequest() {
        FinalTicketRequest request = request("BHFC-12000", "120000");

        FinalTicketResponse firstResponse = finalTicketAllocationService.requestFinalTicket(request);
        FinalTicketResponse secondResponse = finalTicketAllocationService.requestFinalTicket(request);

        assertThat(firstResponse.status()).isEqualTo(RuleResultStatus.CONFIRMED);
        assertThat(secondResponse.status()).isEqualTo(RuleResultStatus.DUPLICATE_REQUEST);
    }

    @Test
    void shouldRejectFinalTicketRequestWhenAccessCodeIsInvalid() {
        FinalTicketResponse response = finalTicketAllocationService.requestFinalTicket(
                request("BHFC-12000", "000000")
        );

        assertThat(response.status()).isEqualTo(RuleResultStatus.INVALID_MEMBER_ACCESS);
    }

    @Test
    void shouldCancelConfirmedTicketAndPromoteFirstWaitingListMember() {
        finalTicketAllocationService.requestFinalTicket(request("BHFC-12000", "120000"));
        finalTicketAllocationService.requestFinalTicket(request("BHFC-1001", "111111"));
        finalTicketAllocationService.requestFinalTicket(request("BHFC-18050", "180500"));
        finalTicketAllocationService.requestFinalTicket(request("BHFC-27000", "270000"));

        assertThat(statusFor("BHFC-27000")).isEqualTo(RuleResultStatus.WAITING_LIST);

        FinalTicketCancellationResponse response = finalTicketAllocationService.cancelFinalTicket(
                request("BHFC-12000", "120000")
        );

        assertThat(response.status()).isEqualTo(RuleResultStatus.CANCELLED);
        assertThat(response.memberNumber()).isEqualTo("BHFC-12000");
        assertThat(response.promotedMemberNumber()).isEqualTo("BHFC-27000");
        assertThat(statusFor("BHFC-12000")).isEqualTo(RuleResultStatus.CANCELLED);
        assertThat(statusFor("BHFC-27000")).isEqualTo(RuleResultStatus.CONFIRMED);
    }

    @Test
    void shouldRejectCancellationWhenAccessCodeIsInvalid() {
        finalTicketAllocationService.requestFinalTicket(request("BHFC-12000", "120000"));

        FinalTicketCancellationResponse response = finalTicketAllocationService.cancelFinalTicket(
                request("BHFC-12000", "000000")
        );

        assertThat(response.status()).isEqualTo(RuleResultStatus.INVALID_MEMBER_ACCESS);
        assertThat(statusFor("BHFC-12000")).isEqualTo(RuleResultStatus.CONFIRMED);
    }

    private FinalTicketRequest request(String memberNumber, String accessCode) {
        return new FinalTicketRequest(memberNumber, accessCode);
    }

    private RuleResultStatus statusFor(String memberNumber) {
        return allocationRegistry.findByMemberNumber(memberNumber)
                .map(FinalTicketApplication::status)
                .orElseThrow();
    }
}
