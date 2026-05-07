package com.jose.membershiprules.finalallocation.infrastructure;

import com.jose.membershiprules.domain.RuleResultStatus;
import com.jose.membershiprules.finalallocation.FinalTicketAllocationRegistry;
import com.jose.membershiprules.finalallocation.FinalTicketApplication;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryFinalTicketAllocationRegistry implements FinalTicketAllocationRegistry {

    private final Map<String, FinalTicketApplication> applications = new ConcurrentHashMap<>();

    @Override
    public boolean hasApplication(String memberNumber) {
        return applications.containsKey(memberNumber);
    }

    @Override
    public void save(FinalTicketApplication application) {
        applications.put(application.memberNumber(), application);
    }

    @Override
    public Optional<FinalTicketApplication> findByMemberNumber(String memberNumber) {
        return Optional.ofNullable(applications.get(memberNumber));
    }

    @Override
    public Collection<FinalTicketApplication> findAll() {
        return applications.values();
    }

    @Override
    public Optional<FinalTicketApplication> promoteNextWaitingListMember() {
        Optional<FinalTicketApplication> nextWaitingListApplication = applications.values().stream()
                .filter(application -> application.status() == RuleResultStatus.WAITING_LIST)
                .min(Comparator
                        .comparing(FinalTicketApplication::memberSince)
                        .thenComparing(FinalTicketApplication::memberNumber));

        nextWaitingListApplication.ifPresent(application -> save(new FinalTicketApplication(
                application.memberNumber(),
                application.memberSince(),
                RuleResultStatus.CONFIRMED
        )));

        return nextWaitingListApplication.map(application -> new FinalTicketApplication(
                application.memberNumber(),
                application.memberSince(),
                RuleResultStatus.CONFIRMED
        ));
    }

    @Override
    public void cancelConfirmedTicket(String memberNumber) {
        findByMemberNumber(memberNumber)
                .filter(application -> application.status() == RuleResultStatus.CONFIRMED)
                .ifPresent(application -> save(new FinalTicketApplication(
                        application.memberNumber(),
                        application.memberSince(),
                        RuleResultStatus.CANCELLED
                )));
    }
}
