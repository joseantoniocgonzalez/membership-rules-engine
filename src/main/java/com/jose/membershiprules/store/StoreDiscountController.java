package com.jose.membershiprules.store;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StoreDiscountController {

    private final StoreDiscountService storeDiscountService;

    public StoreDiscountController(StoreDiscountService storeDiscountService) {
        this.storeDiscountService = storeDiscountService;
    }

    @PostMapping("/api/store/discounts")
    public StoreDiscountResponse calculateDiscount(@Valid @RequestBody StoreDiscountRequest request) {
        return storeDiscountService.calculateDiscount(request);
    }
}
