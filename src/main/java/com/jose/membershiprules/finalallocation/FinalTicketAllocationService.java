package com.jose.membershiprules.finalallocation;

import com.jose.membershiprules.domain.MembershipType;
import com.jose.membershiprules.domain.RuleResultStatus;
import com.jose.membershiprules.member.Member;
import com.jose.membershiprules.member.MemberAccessValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FinalTicketAllocationService {

    private final MemberAccessValidator memberAccessValidator;
    private final FinalTicketAllocationRegistry allocationRegistry;
    private final int ticketAllocation;

    public FinalTicketAllocationService(
            MemberAccessValidator memberAccessValidator,
            FinalTicketAllocationRegistry allocationRegistry,
            @Value("${membership.final.ticket-allocation:35000}") int ticketAllocation
    ) {
        this.memberAccessValidator = memberAccessValidator;
        this.allocationRegistry = allocationRegistry;
        this.ticketAllocation = ticketAllocation;
    }

    public FinalTicketResponse requestFinalTicket(FinalTicketRequest request) {
        PromotionFinal promotionFinal = PromotionFinal.wembleyPromotionFinal();

        return memberAccessValidator.validate(request.memberNumber(), request.accessCode())
                .map(member -> evaluateFinalTicketRequest(promotionFinal, member))
                .orElseGet(() -> response(
                        RuleResultStatus.INVALID_MEMBER_ACCESS,
                        promotionFinal,
                        request.memberNumber(),
                        "The member number or access code is invalid."
                ));
    }

    public FinalTicketCancellationResponse cancelFinalTicket(FinalTicketRequest request) {
        PromotionFinal promotionFinal = PromotionFinal.wembleyPromotionFinal();

        return memberAccessValidator.validate(request.memberNumber(), request.accessCode())
                .map(member -> evaluateFinalTicketCancellation(promotionFinal, member))
                .orElseGet(() -> cancellationResponse(
                        RuleResultStatus.INVALID_MEMBER_ACCESS,
                        promotionFinal,
                        request.memberNumber(),
                        null,
                        "The member number or access code is invalid."
                ));
    }

    private FinalTicketResponse evaluateFinalTicketRequest(PromotionFinal promotionFinal, Member member) {
        if (!member.active()) {
            return response(
                    RuleResultStatus.REJECTED,
                    promotionFinal,
                    member.memberNumber(),
                    "Inactive members cannot request a Promotion Final ticket."
            );
        }

        if (member.membershipType() != MembershipType.SEASON_TICKET_HOLDER) {
            return response(
                    RuleResultStatus.NOT_ELIGIBLE,
                    promotionFinal,
                    member.memberNumber(),
                    "Only active Season Ticket Holders are eligible for the Promotion Final allocation."
            );
        }

        if (allocationRegistry.hasApplication(member.memberNumber())) {
            return response(
                    RuleResultStatus.DUPLICATE_REQUEST,
                    promotionFinal,
                    member.memberNumber(),
                    "This member has already requested a Promotion Final ticket."
            );
        }

        allocationRegistry.save(new FinalTicketApplication(
                member.memberNumber(),
                member.memberSince(),
                RuleResultStatus.WAITING_LIST
        ));

        rebalanceAllocationBySeniority();

        RuleResultStatus status = allocationRegistry.findByMemberNumber(member.memberNumber())
                .map(FinalTicketApplication::status)
                .orElse(RuleResultStatus.WAITING_LIST);

        return response(
                status,
                promotionFinal,
                member.memberNumber(),
                messageFor(status)
        );
    }

    private FinalTicketCancellationResponse evaluateFinalTicketCancellation(
            PromotionFinal promotionFinal,
            Member member
    ) {
        Optional<FinalTicketApplication> application = allocationRegistry.findByMemberNumber(member.memberNumber());

        if (application.isEmpty() || application.get().status() != RuleResultStatus.CONFIRMED) {
            return cancellationResponse(
                    RuleResultStatus.REJECTED,
                    promotionFinal,
                    member.memberNumber(),
                    null,
                    "Only confirmed Promotion Final tickets can be cancelled."
            );
        }

        allocationRegistry.cancelConfirmedTicket(member.memberNumber());
        Optional<FinalTicketApplication> promotedApplication = allocationRegistry.promoteNextWaitingListMember();
        String promotedMemberNumber = promotedApplication
                .map(FinalTicketApplication::memberNumber)
                .orElse(null);

        return cancellationResponse(
                RuleResultStatus.CANCELLED,
                promotionFinal,
                member.memberNumber(),
                promotedMemberNumber,
                cancellationMessage(promotedMemberNumber)
        );
    }

    private void rebalanceAllocationBySeniority() {
        var activeApplications = allocationRegistry.findAll().stream()
                .filter(application -> application.status() != RuleResultStatus.CANCELLED)
                .sorted(java.util.Comparator
                        .comparing(FinalTicketApplication::memberSince)
                        .thenComparing(FinalTicketApplication::memberNumber))
                .toList();

        for (int index = 0; index < activeApplications.size(); index++) {
            FinalTicketApplication application = activeApplications.get(index);
            RuleResultStatus status = index < ticketAllocation
                    ? RuleResultStatus.CONFIRMED
                    : RuleResultStatus.WAITING_LIST;

            allocationRegistry.save(new FinalTicketApplication(
                    application.memberNumber(),
                    application.memberSince(),
                    status
            ));
        }
    }

    private String messageFor(RuleResultStatus status) {
        return switch (status) {
            case CONFIRMED -> "Promotion Final ticket confirmed.";
            case WAITING_LIST -> "The allocation is full. The member has been placed on the waiting list.";
            default -> "Promotion Final ticket request processed.";
        };
    }

    private String cancellationMessage(String promotedMemberNumber) {
        if (promotedMemberNumber == null) {
            return "Promotion Final ticket cancelled.";
        }

        return "Promotion Final ticket cancelled. "
                + promotedMemberNumber
                + " has been promoted from the waiting list.";
    }

    private FinalTicketResponse response(
            RuleResultStatus status,
            PromotionFinal promotionFinal,
            String memberNumber,
            String message
    ) {
        return new FinalTicketResponse(
                status,
                promotionFinal.finalCode(),
                promotionFinal.displayName(),
                promotionFinal.venueDisplayName(),
                memberNumber,
                message
        );
    }

    private FinalTicketCancellationResponse cancellationResponse(
            RuleResultStatus status,
            PromotionFinal promotionFinal,
            String memberNumber,
            String promotedMemberNumber,
            String message
    ) {
        return new FinalTicketCancellationResponse(
                status,
                promotionFinal.finalCode(),
                promotionFinal.displayName(),
                promotionFinal.venueDisplayName(),
                memberNumber,
                promotedMemberNumber,
                message
        );
    }
}
