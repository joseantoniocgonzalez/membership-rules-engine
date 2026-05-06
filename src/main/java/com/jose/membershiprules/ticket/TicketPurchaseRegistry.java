package com.jose.membershiprules.ticket;

public interface TicketPurchaseRegistry {

    boolean hasPurchase(String memberNumber, String matchCode);

    void recordPurchase(String memberNumber, String matchCode);
}
