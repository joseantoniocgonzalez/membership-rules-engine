# Membership Rules Engine

Membership Rules Engine is a Java and Spring Boot backend project that models membership-based business rules for a fictional English football club: Bristol Harbour FC.

The project focuses on rule validation, ticket eligibility, store discounts, ticket sale windows and Promotion Final ticket allocation.

## Business context

Bristol Harbour FC manages different membership types with different rights and restrictions:

- Season Ticket Holders
- Premium Members
- Standard Members
- Free Members

The system models rules for:

- official club store discounts;
- normal match ticket sale windows;
- included access for Season Ticket Holders;
- Promotion Final ticket allocation at Wembley Stadium;
- waiting list promotion after cancellations.

The Promotion Final is played between Bristol Harbour FC and Birmingham Forge FC at Wembley Stadium, London. Bristol Harbour FC receives an allocation of 35,000 tickets.

## Technical focus

This project is designed to demonstrate:

- backend development with Java and Spring Boot;
- business rule modelling;
- REST API design;
- validation of member credentials;
- unit and parameterized testing;
- integration testing with PostgreSQL and Testcontainers;
- database migrations with Flyway;
- CI execution with GitHub Actions;
- technical documentation for a portfolio project.

## Stack

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Flyway
- Spring Data JPA
- JUnit 5
- Testcontainers
- JaCoCo
- GitHub Actions

## Rule summary

Every member operation that depends on membership type must validate both:

- memberNumber
- accessCode

If the member number does not exist or the access code does not match, the operation returns INVALID_MEMBER_ACCESS.

This validation is intentionally kept separate from full authentication. The project does not use Spring Security, JWT or session-based login for this version.

## Running the project checks

Run the test suite with:

    ./mvnw test

## Project identity

All club names, rivals, products and membership scenarios are fictional.

Wembley Stadium is used as a real-world venue to give the Promotion Final a realistic context.
