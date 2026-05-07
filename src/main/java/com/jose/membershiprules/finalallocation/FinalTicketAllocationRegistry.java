package com.jose.membershiprules.finalallocation;

import java.util.Collection;
import java.util.Optional;

public interface FinalTicketAllocationRegistry {

    boolean hasApplication(String memberNumber);

    void save(FinalTicketApplication application);

    Optional<FinalTicketApplication> findByMemberNumber(String memberNumber);

    Collection<FinalTicketApplication> findAll();

    Optional<FinalTicketApplication> promoteNextWaitingListMember();

    void cancelConfirmedTicket(String memberNumber);
}
