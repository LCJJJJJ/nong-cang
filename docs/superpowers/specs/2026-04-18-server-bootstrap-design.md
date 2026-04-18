# Server Bootstrap Design

**Date:** 2026-04-18

**Goal:** Initialize a backend project under `server/` for Ubuntu using JDK 17 and Spring Boot 3.

**Context**

- The repository is currently empty aside from `.git`.
- The machine already has `OpenJDK 17.0.18` and `Maven 3.9.9`.
- The user confirmed a minimal backend scaffold using Maven with `Spring Web`, `Validation`, and `Actuator`.

**Chosen Approach**

- Use the official Spring Initializr service at `start.spring.io`.
- Generate a Maven project targeting Java 17.
- Pin the project to a Spring Boot 3 release line.
- Create the project directly in `server/`.

**Project Shape**

- Group: `com.nongcang`
- Artifact: `server`
- Package: `com.nongcang.server`
- Packaging: `jar`
- Dependencies: `web`, `validation`, `actuator`

**Why This Approach**

- It matches the official Spring initialization flow.
- It avoids hand-maintaining the initial Maven wrapper and plugin setup.
- It fits the current machine because Java 17 and Maven are already available.

**Verification**

- Confirm generated files exist under `server/`.
- Run `./mvnw test` in `server/`.
