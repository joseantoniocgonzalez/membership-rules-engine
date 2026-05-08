# Decision Table

This document summarizes the main decision rules implemented in Membership Rules Engine.

The goal is to make the business logic easy to review, test and maintain.

## 1. Member access validation

All member-based operations must validate both `memberNumber` and `accessCode`.

| Condition | Result |
| --- | --- |
| `memberNumber` does not exist | `INVALID_MEMBER_ACCESS` |
| `memberNumber` exists but `accessCode` does not match | `INVALID_MEMBER_ACCESS` |
| `memberNumber` exists and `accessCode` matches | Continue to the business rule |

This rule applies to:

- store discounts;
- normal match ticket purchases;
- Promotion Final ticket requests;
- Promotion Final ticket cancellations.

## 2. Store discount decision table

Store discounts apply only to the official club store.

| Membership type | Active required | Discount | Status |
| --- | --- | ---: | --- |
| `SEASON_TICKET_HOLDER` | No specific active rule for store discount | 20% | `CONFIRMED` |
| `PREMIUM_MEMBER` | No specific active rule for store discount | 20% | `CONFIRMED` |
| `STANDARD_MEMBER` | No specific active rule for store discount | 10% | `CONFIRMED` |
| `FREE_MEMBER` | No specific active rule for store discount | 0% | `CONFIRMED` |
| Invalid credentials | Not applicable | 0% | `INVALID_MEMBER_ACCESS` |

Example:

| Input | Value |
| --- | --- |
| Product | Home Shirt |
| Base price | £75 |
| Member number | BHFC-2045 |
| Access code | 482910 |
| Membership type | `PREMIUM_MEMBER` |

| Output | Value |
| --- | --- |
| Status | `CONFIRMED` |
| Discount | 20% |
| Final price | £60 |

## 3. Normal match ticket sale decision table

Normal home match:

```text
Bristol Harbour FC vs Northampton Cobblers FC
```

Sale window dates used by the backend:

| Membership type | Sale window opens |
| --- | --- |
| `PREMIUM_MEMBER` | 2026-07-01 |
| `STANDARD_MEMBER` | 2026-07-08 |
| `FREE_MEMBER` | 2026-07-15 |

Decision table:

| Condition | Status |
| --- | --- |
| Invalid `memberNumber` or `accessCode` | `INVALID_MEMBER_ACCESS` |
| Member is inactive | `REJECTED` |
| Member is `SEASON_TICKET_HOLDER` | `ALREADY_INCLUDED` |
| Member has already purchased a ticket for the match | `DUPLICATE_REQUEST` |
| Member sale window is not open yet | `NOT_YET_AVAILABLE` |
| No tickets remain | `SOLD_OUT` |
| Member is eligible, sale window is open and tickets remain | `CONFIRMED` |

## 4. Promotion Final eligibility decision table

Promotion Final:

```text
Bristol Harbour FC vs Birmingham Forge FC
Promotion Final
Wembley Stadium, London
```

Bristol Harbour FC ticket allocation:

```text
35,000 tickets
```

Decision table:

| Condition | Status |
| --- | --- |
| Invalid `memberNumber` or `accessCode` | `INVALID_MEMBER_ACCESS` |
| Member is inactive | `REJECTED` |
| Member is not `SEASON_TICKET_HOLDER` | `NOT_ELIGIBLE` |
| Member has already requested a final ticket | `DUPLICATE_REQUEST` |
| Active Season Ticket Holder is inside the allocation after seniority sorting | `CONFIRMED` |
| Active Season Ticket Holder is outside the allocation after seniority sorting | `WAITING_LIST` |

Seniority is based on `memberSince`.

A lower or higher member number does not decide priority by itself. The allocation is based on how long the member has held the season ticket.

## 5. Promotion Final allocation by seniority

The system stores valid final ticket applications and rebalances the allocation by seniority.

Example with a reduced test allocation of 3 tickets:

| Member number | Member since | Result |
| --- | --- | --- |
| BHFC-12000 | 2014-05-10 | `CONFIRMED` |
| BHFC-1001 | 2015-06-01 | `CONFIRMED` |
| BHFC-18050 | 2016-09-03 | `CONFIRMED` |
| BHFC-27000 | 2017-03-22 | `WAITING_LIST` |
| BHFC-36035 | 2018-08-01 | `WAITING_LIST` |

The production rule uses the real allocation of 35,000 tickets. Tests use a smaller allocation to verify the same rule clearly and efficiently.

## 6. Promotion Final cancellation decision table

A confirmed final ticket can be cancelled by validating the same member credentials.

| Condition | Status |
| --- | --- |
| Invalid `memberNumber` or `accessCode` | `INVALID_MEMBER_ACCESS` |
| Member does not have a confirmed final ticket | `REJECTED` |
| Member has a confirmed final ticket | `CANCELLED` |

After a confirmed ticket is cancelled:

| Waiting list state | Result |
| --- | --- |
| Waiting list has eligible members | First waiting list member by seniority is promoted to `CONFIRMED` |
| Waiting list is empty | No member is promoted |

The system must never exceed the ticket allocation.
