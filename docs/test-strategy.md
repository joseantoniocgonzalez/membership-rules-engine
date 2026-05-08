# Test Strategy

This document explains the testing approach used in Membership Rules Engine.

The project focuses on business rules, validation, persistence and integration between backend components. The test strategy is designed to verify the rules from different levels without relying only on manual endpoint checks.

## Goals

The test suite aims to verify that:

- membership credentials are validated before applying business rules;
- store discounts are calculated correctly;
- normal match ticket sale windows are enforced;
- Promotion Final allocation follows eligibility and seniority rules;
- waiting list promotion works after a cancellation;
- member data can be loaded from PostgreSQL using JPA;
- Flyway migrations prepare the database schema and demo data;
- business services work with data loaded from PostgreSQL in integration tests.

## Test levels

## 1. Unit tests

Unit tests focus on business logic in isolation.

They verify the behavior of services such as:

- `StoreDiscountService`
- `TicketSaleService`
- `FinalTicketAllocationService`

These tests use controlled in-memory data so that each rule can be tested directly.

Examples:

| Rule | Expected result |
| --- | --- |
| Premium Member store discount | 20% discount |
| Standard Member before sale window | `NOT_YET_AVAILABLE` |
| Season Ticket Holder for normal home match | `ALREADY_INCLUDED` |
| Premium Member requesting Promotion Final ticket | `NOT_ELIGIBLE` |
| Inactive Season Ticket Holder requesting Promotion Final ticket | `REJECTED` |
| Duplicate final ticket request | `DUPLICATE_REQUEST` |

## 2. Parameterized tests

Parameterized tests are used when the same rule must be checked against several inputs.

Examples:

- store discounts by membership type;
- non-Season Ticket Holder members rejected from the Promotion Final allocation.

This avoids repeating almost identical test methods and makes decision rules easier to review.

## 3. Integration tests

Integration tests verify that Spring Boot, JPA, Flyway and PostgreSQL work together.

The project uses Testcontainers to start a real PostgreSQL database during the test run.

Integration tests verify that:

- Flyway creates the `members` table;
- Flyway inserts demo members;
- `JpaMemberDirectory` reads members from PostgreSQL;
- business services apply rules using members loaded from PostgreSQL.

Examples:

| Test | Purpose |
| --- | --- |
| `MemberDirectoryIntegrationTest` | Verifies member lookup from PostgreSQL |
| `BusinessRulesIntegrationTest` | Verifies business rules using PostgreSQL-loaded members |

## 4. Manual endpoint checks

Manual checks with `curl` were used during development to validate endpoints while the backend evolved.

Examples:

```bash
POST /api/store/discounts
POST /api/tickets/normal-match/purchases
POST /api/final-tickets/requests
POST /api/final-tickets/cancellations
```

Manual checks are useful during development, but the final confidence comes from the automated test suite.

## Current automated coverage

The project currently includes automated tests for:

- store discount rules;
- member access validation;
- normal match ticket sale windows;
- duplicate normal match ticket purchases;
- Promotion Final eligibility;
- Promotion Final allocation by seniority;
- waiting list behavior;
- cancellation and automatic waiting list promotion;
- PostgreSQL persistence through JPA;
- Flyway database migrations;
- business rules using Testcontainers-backed data.

## Testcontainers

Testcontainers is used to run PostgreSQL in tests without requiring a manually configured local database.

This gives the project a more realistic integration test setup because the application talks to a real PostgreSQL instance instead of a mock or an in-memory database.

## Flyway

Flyway is used to manage database migrations.

Current migrations:

| Migration | Purpose |
| --- | --- |
| `V1__create_members_table.sql` | Creates the `members` table |
| `V2__insert_demo_members.sql` | Inserts fictional demo members used by the rules |

## JaCoCo

JaCoCo is used to generate a test coverage report during the Maven `verify` phase.

The report is generated at:

```text
target/site/jacoco/index.html
```

Coverage is not treated as the only quality indicator. The main goal is to combine meaningful business-rule tests with measurable coverage.

## Continuous Integration

GitHub Actions runs the project verification automatically on:

- pushes to `main`;
- pull requests targeting `main`.

The CI workflow runs:

```bash
./mvnw --batch-mode verify
```

This means the pipeline executes the test suite and generates the JaCoCo coverage report.

## Quality approach

The project prioritizes:

- clear business rules;
- readable tests;
- explicit expected statuses;
- integration tests with real PostgreSQL;
- repeatable builds with Maven;
- automated verification through GitHub Actions.

The goal is not only to make the backend work, but to make the rules easy to understand, verify and extend.
