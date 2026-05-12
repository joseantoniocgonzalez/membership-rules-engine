# Membership Rules Engine

Membership Rules Engine is a Java and Spring Boot backend project that models membership-based business rules for a fictional English football club: Bristol Harbour FC.

The project focuses on member validation, store discounts, normal match ticket sale windows and Promotion Final ticket allocation.

## Live demo

[![Live demo](https://img.shields.io/badge/Live%20demo-GitHub%20Pages-0f4c81?style=for-the-badge)](https://joseantoniocgonzalez.github.io/membership-rules-engine/)
[![CI](https://img.shields.io/github/actions/workflow/status/joseantoniocgonzalez/membership-rules-engine/ci.yml?branch=main&style=for-the-badge&label=CI)](https://github.com/joseantoniocgonzalez/membership-rules-engine/actions)
[![Java](https://img.shields.io/badge/Java-21-d7a84b?style=for-the-badge)](#stack)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-backend-081826?style=for-the-badge)](#stack)

A static GitHub Pages demo is available here:

**https://joseantoniocgonzalez.github.io/membership-rules-engine/**

The demo is a visual companion to the backend. It allows visitors to understand the main rules without running the Spring Boot application locally:

- store discounts by membership type;
- normal match ticket sale windows;
- Promotion Final allocation and waiting list behavior;
- demo member records for manual testing.

## Quick links

| Resource | Link |
| --- | --- |
| Live demo | https://joseantoniocgonzalez.github.io/membership-rules-engine/ |
| Business rules | `docs/business-rules.md` |
| Decision table | `docs/decision-table.md` |
| Test strategy | `docs/test-strategy.md` |
| API examples | `docs/api-examples.md` |
| GitHub Actions workflow | `.github/workflows/ci.yml` |

## Highlights

- REST API for membership-based business rules.
- PostgreSQL persistence with Flyway migrations.
- Unit, parameterized and integration tests.
- Testcontainers-backed PostgreSQL integration tests.
- GitHub Actions CI pipeline running `./mvnw --batch-mode verify`.
- JaCoCo coverage report generated during Maven `verify`.
- Static GitHub Pages demo for non-technical review.

## Business context

Bristol Harbour FC manages different membership types with different rights and restrictions:

- Season Ticket Holders
- Premium Members
- Standard Members
- Free Members

The backend models rules for:

- official club store discounts;
- normal home match ticket sale windows;
- included access for Season Ticket Holders;
- Promotion Final ticket allocation at Wembley Stadium;
- waiting list promotion after cancellations.

The Promotion Final is played between Bristol Harbour FC and Birmingham Forge FC at Wembley Stadium, London. Bristol Harbour FC receives an allocation of 35,000 tickets.

## Technical focus

This project demonstrates:

- backend development with Java and Spring Boot;
- REST API design;
- business rule modelling;
- member credential validation;
- PostgreSQL persistence with JPA;
- database migrations with Flyway;
- unit and parameterized testing with JUnit 5;
- integration testing with Testcontainers;
- CI execution with GitHub Actions;
- test coverage reporting with JaCoCo;
- technical documentation for a portfolio project.

## Stack

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Flyway
- Spring Data JPA
- JUnit 5
- AssertJ
- Testcontainers
- JaCoCo
- GitHub Actions

## Main rules

Every member operation that depends on membership type must validate both:

- `memberNumber`
- `accessCode`

If the member number does not exist or the access code does not match, the operation returns:

```text
INVALID_MEMBER_ACCESS
```

This validation is intentionally kept separate from full authentication. The project does not use Spring Security, JWT or session-based login in this version.

## Implemented features

### Store discounts

Store discounts apply only to the official club store.

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

Endpoint:

```text
POST /api/store/discounts
```

### Normal match ticket sale windows

Normal home match:

```text
Bristol Harbour FC vs Northampton Cobblers FC
```

Implemented outcomes:

- `ALREADY_INCLUDED`
- `CONFIRMED`
- `NOT_YET_AVAILABLE`
- `SOLD_OUT`
- `DUPLICATE_REQUEST`
- `REJECTED`
- `INVALID_MEMBER_ACCESS`

Endpoint:

```text
POST /api/tickets/normal-match/purchases
```

### Promotion Final ticket allocation

Final:

```text
Bristol Harbour FC vs Birmingham Forge FC
Promotion Final
Wembley Stadium, London
```

Rules:

- only active Season Ticket Holders are eligible;
- tickets are assigned by season ticket seniority;
- seniority is based on `memberSince`;
- the allocation is limited to 35,000 tickets;
- eligible members outside the allocation are placed on the waiting list;
- duplicate requests are rejected;
- inactive members are rejected;
- cancelled confirmed tickets can promote the first waiting list member by seniority.

Implemented outcomes:

- `CONFIRMED`
- `WAITING_LIST`
- `REJECTED`
- `NOT_ELIGIBLE`
- `DUPLICATE_REQUEST`
- `CANCELLED`
- `INVALID_MEMBER_ACCESS`

Endpoints:

```text
POST /api/final-tickets/requests
POST /api/final-tickets/cancellations
```

## Persistence

The project uses PostgreSQL with Flyway migrations.

Current migrations:

| Migration | Purpose |
| --- | --- |
| `V1__create_members_table.sql` | Creates the `members` table |
| `V2__insert_demo_members.sql` | Inserts fictional demo members |

Member data is loaded through Spring Data JPA using:

- `MemberEntity`
- `MemberJpaRepository`
- `JpaMemberDirectory`

## Testing

The project includes unit, parameterized and integration tests.

Current test areas:

- store discount rules;
- member credential validation;
- normal match ticket sale windows;
- duplicate ticket purchase prevention;
- Promotion Final eligibility;
- Promotion Final allocation by seniority;
- waiting list promotion after cancellation;
- JPA member lookup from PostgreSQL;
- business rules using PostgreSQL-backed data.

Integration tests use Testcontainers to run a real PostgreSQL database during the test suite.

Run tests:

```bash
./mvnw test
```

Run full verification with JaCoCo coverage:

```bash
./mvnw verify
```

The JaCoCo report is generated at:

```text
target/site/jacoco/index.html
```

## Continuous Integration

GitHub Actions runs the verification pipeline on:

- pushes to `main`;
- pull requests targeting `main`.

The CI workflow executes:

```bash
./mvnw --batch-mode verify
```

This runs the automated test suite and generates the JaCoCo coverage report.

## Documentation

Additional technical documentation is available in the `docs/` directory:

- `docs/business-rules.md`
- `docs/decision-table.md`
- `docs/test-strategy.md`
- `docs/api-examples.md`

## Project structure

```text
src/main/java/com/jose/membershiprules
├── domain
├── finalallocation
├── member
├── store
└── ticket

src/main/resources/db/migration
├── V1__create_members_table.sql
└── V2__insert_demo_members.sql

src/test/java/com/jose/membershiprules
├── BusinessRulesIntegrationTest.java
├── MemberDirectoryIntegrationTest.java
├── finalallocation
├── store
└── ticket

docs
├── assets
│   ├── app.js
│   ├── bhfc-crest.png
│   ├── bhfc-home-shirt.png
│   └── styles.css
├── api-examples.md
├── business-rules.md
├── decision-table.md
├── index.html
└── test-strategy.md
```

## Project identity

All club names, rivals, products, members and membership scenarios are fictional.

Wembley Stadium is used as a real-world venue to give the Promotion Final a realistic context.
