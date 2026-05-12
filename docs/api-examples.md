# API Examples

This document shows practical `curl` examples for testing the main API endpoints in Membership Rules Engine.

The examples use the fictional demo members loaded by Flyway in `V2__insert_demo_members.sql`.

## Base URL

When the application is running locally:

```text
http://localhost:8080
```

## Demo member records

| Member number | Access code | Type | Active | Useful for |
| --- | --- | --- | --- | --- |
| `BHFC-12000` | `120000` | `SEASON_TICKET_HOLDER` | Yes | Promotion Final confirmed example |
| `BHFC-1001` | `111111` | `SEASON_TICKET_HOLDER` | Yes | Normal match already included |
| `BHFC-18050` | `180500` | `SEASON_TICKET_HOLDER` | Yes | Promotion Final seniority example |
| `BHFC-27000` | `270000` | `SEASON_TICKET_HOLDER` | Yes | Waiting list sample |
| `BHFC-36035` | `360350` | `SEASON_TICKET_HOLDER` | Yes | Waiting list / high member number example |
| `BHFC-1900` | `190000` | `SEASON_TICKET_HOLDER` | No | Inactive member rejection |
| `BHFC-2045` | `482910` | `PREMIUM_MEMBER` | Yes | 20% store discount / final not eligible |
| `BHFC-3107` | `739204` | `STANDARD_MEMBER` | Yes | 10% discount / ticket window example |
| `BHFC-4880` | `105377` | `FREE_MEMBER` | Yes | 0% discount / final sale window example |

## 1. Store discount: Premium Member

Endpoint:

```text
POST /api/store/discounts
```

Request:

```bash
curl -s -X POST http://localhost:8080/api/store/discounts \
  -H "Content-Type: application/json" \
  -d '{
    "memberNumber": "BHFC-2045",
    "accessCode": "482910",
    "productName": "Home Shirt",
    "basePrice": 75
  }'
```

Expected result:

```json
{
  "status": "CONFIRMED",
  "productName": "Home Shirt",
  "basePrice": 75,
  "discountPercentage": 20,
  "finalPrice": 60.00
}
```

## 2. Store discount: invalid access code

Request:

```bash
curl -s -X POST http://localhost:8080/api/store/discounts \
  -H "Content-Type: application/json" \
  -d '{
    "memberNumber": "BHFC-2045",
    "accessCode": "000000",
    "productName": "Home Shirt",
    "basePrice": 75
  }'
```

Expected result:

```json
{
  "status": "INVALID_MEMBER_ACCESS",
  "productName": "Home Shirt",
  "basePrice": 75,
  "discountPercentage": 0,
  "finalPrice": 75
}
```

## 3. Normal match ticket: Season Ticket Holder

Endpoint:

```text
POST /api/tickets/normal-match/purchases
```

Request:

```bash
curl -s -X POST http://localhost:8080/api/tickets/normal-match/purchases \
  -H "Content-Type: application/json" \
  -d '{
    "memberNumber": "BHFC-1001",
    "accessCode": "111111",
    "requestDate": "2026-07-02",
    "remainingTickets": 100
  }'
```

Expected status:

```text
ALREADY_INCLUDED
```

Season Ticket Holders already have access included for this normal home match.

## 4. Normal match ticket: Standard Member before sale window

Request:

```bash
curl -s -X POST http://localhost:8080/api/tickets/normal-match/purchases \
  -H "Content-Type: application/json" \
  -d '{
    "memberNumber": "BHFC-3107",
    "accessCode": "739204",
    "requestDate": "2026-07-02",
    "remainingTickets": 100
  }'
```

Expected status:

```text
NOT_YET_AVAILABLE
```

The Standard Member sale window is not open yet.

## 5. Normal match ticket: Premium Member confirmed

Request:

```bash
curl -s -X POST http://localhost:8080/api/tickets/normal-match/purchases \
  -H "Content-Type: application/json" \
  -d '{
    "memberNumber": "BHFC-2045",
    "accessCode": "482910",
    "requestDate": "2026-07-02",
    "remainingTickets": 100
  }'
```

Expected status:

```text
CONFIRMED
```

Premium Members can buy from the first sale window.

## 6. Promotion Final: confirmed Season Ticket Holder

Endpoint:

```text
POST /api/final-tickets/requests
```

Request:

```bash
curl -s -X POST http://localhost:8080/api/final-tickets/requests \
  -H "Content-Type: application/json" \
  -d '{
    "memberNumber": "BHFC-12000",
    "accessCode": "120000"
  }'
```

Expected status:

```text
CONFIRMED
```

Active Season Ticket Holders are eligible for the Promotion Final allocation.

## 7. Promotion Final: Premium Member not eligible

Request:

```bash
curl -s -X POST http://localhost:8080/api/final-tickets/requests \
  -H "Content-Type: application/json" \
  -d '{
    "memberNumber": "BHFC-2045",
    "accessCode": "482910"
  }'
```

Expected status:

```text
NOT_ELIGIBLE
```

Only active Season Ticket Holders are eligible for the Promotion Final allocation.

## 8. Promotion Final: inactive Season Ticket Holder rejected

Request:

```bash
curl -s -X POST http://localhost:8080/api/final-tickets/requests \
  -H "Content-Type: application/json" \
  -d '{
    "memberNumber": "BHFC-1900",
    "accessCode": "190000"
  }'
```

Expected status:

```text
REJECTED
```

Inactive members cannot request a Promotion Final ticket.

## 9. Promotion Final: duplicate request

Run the same confirmed final ticket request twice:

```bash
curl -s -X POST http://localhost:8080/api/final-tickets/requests \
  -H "Content-Type: application/json" \
  -d '{
    "memberNumber": "BHFC-12000",
    "accessCode": "120000"
  }'

curl -s -X POST http://localhost:8080/api/final-tickets/requests \
  -H "Content-Type: application/json" \
  -d '{
    "memberNumber": "BHFC-12000",
    "accessCode": "120000"
  }'
```

Expected status on the second request:

```text
DUPLICATE_REQUEST
```

## 10. Promotion Final: cancellation

Endpoint:

```text
POST /api/final-tickets/cancellations
```

Request:

```bash
curl -s -X POST http://localhost:8080/api/final-tickets/cancellations \
  -H "Content-Type: application/json" \
  -d '{
    "memberNumber": "BHFC-12000",
    "accessCode": "120000"
  }'
```

Expected status:

```text
CANCELLED
```

If there is a waiting list member, the system promotes the first eligible member by seniority.

## Notes

- These examples are intended for local API testing.
- The GitHub Pages demo is static and does not call the backend directly.
- The automated tests remain the main verification mechanism for the business rules.
