---
name: java-spring-interview
description: Interview the user before implementing, refactoring, or designing important Java/Spring Boot changes to confirm goals, scope, assumptions, and validation.
---

# Java Spring Interview

Use this skill before implementing, refactoring, or designing important changes in Java, Spring Boot, or the Spring ecosystem.

The purpose is to confirm mutual understanding, clarify ambiguity, catch wrong assumptions, and agree on the intended outcome before touching code or proposing a solution.

User stories are not required unless the user explicitly asks for them.

## When To Use

Use this skill for non-trivial Java/Spring work, including changes involving:

- Spring Boot application behavior
- Spring MVC controllers and REST APIs
- Spring Data JPA, Hibernate, entities, repositories, migrations, and transactions
- Spring Security, authentication, authorization, JWT, and method security
- Validation, DTOs, mappers, and API contracts
- Configuration properties, profiles, and externalized configuration
- REST clients, OpenFeign, service discovery, Eureka, and Spring Cloud Gateway
- Kafka producers, consumers, event contracts, and message flow
- Testing, integration tests, Testcontainers, Docker, and local runtime behavior
- Application architecture, module boundaries, service responsibilities, and refactoring

Do not use this skill for trivial edits where intent is already obvious, such as fixing a typo, renaming a local variable, adding a simple log line, or running a requested command.

## Interview Style

Keep the interview practical and focused. Ask only the most important questions needed to remove ambiguity for the current task.

Do not ask a huge checklist every time. Pick the relevant parts only.

Prefer a few targeted questions over broad discovery. If the user already provided enough context, ask no questions and move to the summary.

Do not continue interviewing forever. Once the understanding is clear, summarize and proceed according to the agreed direction.

## Question Areas

Ask about relevant parts only:

- What the user wants to change
- Why the user wants to change it
- Current behavior
- Desired behavior
- Affected classes, modules, endpoints, services, repositories, entities, tests, or configuration files
- Constraints from the existing codebase
- What should stay unchanged
- Edge cases
- Expected validation or tests
- Whether the change should be minimal or whether refactoring is acceptable

Ask about related technologies only when relevant to the current task, such as Spring MVC, Spring Data JPA, Hibernate, Spring Security, validation, transactions, configuration properties, REST clients, OpenFeign, Eureka, Spring Cloud Gateway, Kafka, testing, Testcontainers, Docker, and application architecture.

## Output Before Work

When enough context is available, summarize before touching code or proposing the solution:

- Confirmed goal
- Agreed scope
- Out of scope
- Assumptions
- Implementation direction
- Validation strategy
- Remaining open questions, if any

If important ambiguity remains, ask the smallest useful set of questions first. Do not include unrelated checklist items.

## Proceeding

After the summary, continue with implementation, refactoring, or design only if the user has provided enough information or explicitly approves the assumptions.

For simple tasks, keep the process lightweight: one or two questions, or a short summary with assumptions, is enough.
