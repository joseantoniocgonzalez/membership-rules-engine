package com.jose.membershiprules.ticket.infrastructure;

import com.jose.membershiprules.ticket.TicketPurchaseRegistry;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTicketPurchaseRegistry implements TicketPurchaseRegistry {

    private final Set<String> purchases = ConcurrentHashMap.newKeySet();

    @Override
    public boolean hasPurchase(String memberNumber, String matchCode) {
        return purchases.contains(key(memberNumber, matchCode));
    }

    @Override
    public void recordPurchase(String memberNumber, String matchCode) {
        purchases.add(key(memberNumber, matchCode));
    }

    private String key(String memberNumber, String matchCode) {
        return memberNumber + "::" + matchCode;
    }
}
