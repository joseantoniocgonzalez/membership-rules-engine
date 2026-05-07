package com.jose.membershiprules.finalallocation;

public record PromotionFinal(
        String finalCode,
        String homeTeam,
        String awayTeam,
        String competitionName,
        String venue,
        String city,
        int ticketAllocation
) {

    public static PromotionFinal wembleyPromotionFinal() {
        return new PromotionFinal(
                "PROMOTION-FINAL-2026",
                "Bristol Harbour FC",
                "Birmingham Forge FC",
                "Promotion Final",
                "Wembley Stadium",
                "London",
                35_000
        );
    }

    public String displayName() {
        return homeTeam + " vs " + awayTeam;
    }

    public String venueDisplayName() {
        return venue + ", " + city;
    }
}
