package com.jose.membershiprules;

import com.jose.membershiprules.domain.RuleResultStatus;
import com.jose.membershiprules.finalallocation.FinalTicketAllocationService;
import com.jose.membershiprules.finalallocation.FinalTicketRequest;
import com.jose.membershiprules.finalallocation.FinalTicketResponse;
import com.jose.membershiprules.store.StoreDiscountRequest;
import com.jose.membershiprules.store.StoreDiscountResponse;
import com.jose.membershiprules.store.StoreDiscountService;
import com.jose.membershiprules.ticket.TicketSaleRequest;
import com.jose.membershiprules.ticket.TicketSaleResponse;
import com.jose.membershiprules.ticket.TicketSaleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BusinessRulesIntegrationTest {

    @Autowired
    private StoreDiscountService storeDiscountService;

    @Autowired
    private TicketSaleService ticketSaleService;

    @Autowired
    private FinalTicketAllocationService finalTicketAllocationService;

    @Test
    void shouldApplyStoreDiscountUsingMemberLoadedFromPostgreSql() {
        StoreDiscountResponse response = storeDiscountService.calculateDiscount(
                new StoreDiscountRequest(
                        "BHFC-2045",
                        "482910",
                        "Home Shirt",
                        new BigDecimal("75.00")
                )
        );

        assertThat(response.status()).isEqualTo(RuleResultStatus.CONFIRMED);
        assertThat(response.discountPercentage()).isEqualTo(20);
        assertThat(response.finalPrice()).isEqualByComparingTo("60.00");
    }

    @Test
    void shouldApplyTicketSaleWindowUsingMemberLoadedFromPostgreSql() {
        TicketSaleResponse response = ticketSaleService.requestTicket(
                new TicketSaleRequest(
                        "BHFC-3107",
                        "739204",
                        LocalDate.of(2026, 7, 2),
                        100
                )
        );

        assertThat(response.status()).isEqualTo(RuleResultStatus.NOT_YET_AVAILABLE);
        assertThat(response.memberNumber()).isEqualTo("BHFC-3107");
    }

    @Test
    void shouldApplyPromotionFinalEligibilityUsingMemberLoadedFromPostgreSql() {
        FinalTicketResponse response = finalTicketAllocationService.requestFinalTicket(
                new FinalTicketRequest(
                        "BHFC-2045",
                        "482910"
                )
        );

        assertThat(response.status()).isEqualTo(RuleResultStatus.NOT_ELIGIBLE);
        assertThat(response.memberNumber()).isEqualTo("BHFC-2045");
        assertThat(response.venue()).isEqualTo("Wembley Stadium, London");
    }
}
