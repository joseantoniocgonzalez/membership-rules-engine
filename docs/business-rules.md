# Business Rules

Membership Rules Engine models membership-based rules for Bristol Harbour FC, a fictional English football club.

The backend focuses on three main areas:

- official store discounts;
- normal home match ticket sale windows;
- Promotion Final ticket allocation at Wembley Stadium.

All operations that depend on membership type must validate both `memberNumber` and `accessCode` before applying any business rule.

## Member access validation

A member operation is valid only when:

- the `memberNumber` exists;
- the `accessCode` matches the stored member record.

If either value is invalid, the system returns:

```text
INVALID_MEMBER_ACCESS
```

This validation is not a full authentication system. The project does not use Spring Security, JWT, sessions or login flows in this version.

## Membership types

The system supports four membership types:

- `SEASON_TICKET_HOLDER`
- `PREMIUM_MEMBER`
- `STANDARD_MEMBER`
- `FREE_MEMBER`

A member can also be active or inactive. Inactive members can be rejected even if their membership type would normally be eligible.

## Store discounts

Store discounts apply only to the official club store.

Discount rules:

| Membership type | Discount |
| --- | ---: |
| `SEASON_TICKET_HOLDER` | 20% |
| `PREMIUM_MEMBER` | 20% |
| `STANDARD_MEMBER` | 10% |
| `FREE_MEMBER` | 0% |

Example:

| Field | Value |
| --- | --- |
| Product | Home Shirt |
| Base price | £75 |
| Member number | BHFC-2045 |
| Access code | 482910 |
| Membership type | `PREMIUM_MEMBER` |
| Discount | 20% |
| Final price | £60 |

Invalid credentials return `INVALID_MEMBER_ACCESS` and no discount is applied.

## Normal match ticket sale windows

Normal home match:

```text
Bristol Harbour FC vs Northampton Cobblers FC
```

Sale windows:

| Membership type | Rule |
| --- | --- |
| `SEASON_TICKET_HOLDER` | Access already included |
| `PREMIUM_MEMBER` | Can buy from the first sale window |
| `STANDARD_MEMBER` | Can buy from a later sale window |
| `FREE_MEMBER` | Can buy only in the final sale window if tickets remain |

Possible outcomes:

| Status | Meaning |
| --- | --- |
| `ALREADY_INCLUDED` | Season Ticket Holders already have access for the home match |
| `CONFIRMED` | Ticket purchase is accepted |
| `NOT_YET_AVAILABLE` | The sale window is not open for that membership type |
| `SOLD_OUT` | No tickets remain |
| `DUPLICATE_REQUEST` | The member has already purchased a ticket for the match |
| `REJECTED` | The member is inactive |
| `INVALID_MEMBER_ACCESS` | Member credentials are invalid |

## Promotion Final ticket allocation

Final:

```text
Bristol Harbour FC vs Birmingham Forge FC
Promotion Final
Wembley Stadium, London
```

Bristol Harbour FC receives:

```text
35,000 tickets
```

Eligibility rule:

```text
Only active Season Ticket Holders are eligible for the Promotion Final ticket allocation.
```

Allocation rule:

```text
Tickets are assigned by season ticket seniority until the allocation is filled.
```

Seniority is based on:

```text
memberSince
```

If there are more valid applications than available tickets, remaining eligible members are placed on the waiting list.

Possible outcomes:

| Status | Meaning |
| --- | --- |
| `CONFIRMED` | The member receives a ticket |
| `WAITING_LIST` | The member is eligible but outside the current allocation |
| `NOT_ELIGIBLE` | The member is not a Season Ticket Holder |
| `REJECTED` | The member is inactive |
| `DUPLICATE_REQUEST` | The member has already requested a final ticket |
| `INVALID_MEMBER_ACCESS` | Member credentials are invalid |

## Waiting list promotion

If a confirmed Season Ticket Holder cancels their Promotion Final ticket:

- the confirmed ticket is marked as `CANCELLED`;
- the first eligible member on the waiting list is promoted to `CONFIRMED`;
- the system must never exceed the ticket allocation.

The waiting list promotion follows the same seniority rule used for the original allocation.
