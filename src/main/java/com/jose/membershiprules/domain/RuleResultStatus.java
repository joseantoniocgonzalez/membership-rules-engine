package com.jose.membershiprules.domain;

public enum RuleResultStatus {
    CONFIRMED,
    WAITING_LIST,
    REJECTED,
    SOLD_OUT,
    ALREADY_INCLUDED,
    NOT_YET_AVAILABLE,
    NOT_ELIGIBLE,
    DUPLICATE_REQUEST,
    CANCELLED,
    INVALID_MEMBER_ACCESS
}
