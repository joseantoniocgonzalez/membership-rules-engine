package com.jose.membershiprules.ticket;

import java.time.LocalDate;

public record NormalMatch(
        String matchCode,
        String homeTeam,
        String awayTeam,
        LocalDate matchDate
) {

    public static NormalMatch bristolHarbourVsNorthamptonCobblers() {
        return new NormalMatch(
                "BHFC-NCFC-2026",
                "Bristol Harbour FC",
                "Northampton Cobblers FC",
                LocalDate.of(2026, 8, 15)
        );
    }

    public String displayName() {
        return homeTeam + " vs " + awayTeam;
    }
}
