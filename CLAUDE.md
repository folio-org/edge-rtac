# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`edge-rtac` is a FOLIO edge module (Spring Boot / Java 21) that bridges 3rd-party discovery services (notably EBSCO's RTAC used by EDS) to FOLIO's `mod-rtac`. It deliberately mimics a legacy RTAC integration's endpoint names, params, and data formats, so API shapes here are constrained by backward compatibility, not free design — check `README.md` and the OpenAPI spec before changing request/response shapes. Note the intentional quirk: failures generally still return HTTP 200 with an empty/soft-error payload rather than an error status (see `RtacSoftErrorException` / `RtacErrorHandler`).

## Build & test commands

- Build: `mvn clean install`
- Run all tests: `mvn test` (surefire is configured to include `**/*IT.java`, `**/*Test.java`, `**/*Tests.java`)
- Run a single test class: `mvn test -Dtest=RtacServiceTest`
- Run a single test method: `mvn test -Dtest=RtacServiceTest#getInstanceRtac_shouldReturnHoldings`
- Run just the integration tests: `mvn test -Dtest=*IT`
- Run the app locally: `mvn spring-boot:run` (requires `okapi_url`, see System Properties in `README.md`)

The `openapi-generator-maven-plugin` regenerates DTOs/API interfaces from `src/main/resources/swagger.api/edge-rtac.yaml` during `generate-sources`/`generate-resources` — a plain `mvn compile`/`mvn test` will trigger it, no separate step needed. Generated sources land in `target/generated-sources`.

## Architecture

Layering is strict and thin — controller → service → HTTP client (interface-based, no impl classes written by hand):

- **API contract**: `src/main/resources/swagger.api/edge-rtac.yaml` (+ `schemas/*.json`) is the source of truth. `RtacApi`, `RtacCacheApi`, and DTOs (`org.folio.rtac.rest.resource.*`, `org.folio.rtac.domain.dto.*`) are generated from it — don't hand-edit generated code; change the YAML/schemas instead.
- **Controllers** (`controller/`): `RtacController` implements generated `RtacApi` (`/rtac`, `/rtac/{instanceId}`) — the "live" holdings lookup path. `RtacCacheController` implements `RtacCacheApi` (`/rtac-cache/...`) — a separate, cache-backed lookup path with search/pagination. `TenantController` implements the FOLIO module-lifecycle Tenant API (no-op here). Controllers do no logic beyond delegating to a service.
- **Services** (`service/`): `RtacService` calls FOLIO for live holdings, parses the comma-separated `instanceIds` param, builds a `RtacBatchRequest`, and for the single-instance endpoint unwraps the batch response into one `InstanceHoldings` (synthesizing an empty holding if none found, or throwing `RtacSoftErrorException` if mod-rtac reported an error). `RtacCacheService` is a thin pass-through to `RtacCacheClient` returning raw JSON strings (`JsonNode.toString()`), unlike `RtacService` which returns typed DTOs.
- **Clients** (`client/`): `RtacClient` and `RtacCacheClient` are declarative `@HttpExchange` interfaces (Spring 6 HTTP Interface Client), not Feign/RestTemplate. They're turned into beans via `HttpServiceProxyFactory` in `config/HttpClientConfiguration.java`, using the `edgeHttpServiceProxyFactory` bean supplied by `edge-common-spring` (which handles Okapi URL/tenant/token wiring — see `folio.exchange.enabled: false` and `folio.client.okapiUrl` in `application.yml`).
- **Errors**: `RtacErrorHandler` (`@RestControllerAdvice`) centralizes exception→HTTP mapping. `RtacSoftErrorException` → HTTP 200 with a `HoldingsError` body (the "soft failure" RTAC quirk). Downstream HTTP errors (`HttpStatusCodeException`) are passed through with their original status and body. Validation/message-conversion errors → 400.
- **`ObjectMapperUtils`**: wraps the app's Jackson `ObjectMapper` (note: `tools.jackson` package — this project is on Jackson 3, not `com.fasterxml.jackson`).

Depends on `edge-common-spring` and `folio-spring-base` for cross-cutting edge-module concerns (auth, tenant handling, HTTP client wiring, secure store). Most of the "plumbing" (JWT/API-key auth, Okapi header propagation, tenant validation) lives in those libraries, not in this repo — if something around auth/headers looks missing here, check there first.

## Testing conventions

- Unit tests (`service/*Test.java`, `utils/*Test.java`) test services/utils in isolation.
- Integration tests (`controller/*IT.java`) extend `BaseIntegrationTests`, which boots the full Spring context (`@SpringBootTest` + `@AutoConfigureMockMvc`) against a WireMock server standing in for Okapi/mod-rtac. `TestConstants` and `TestUtil` hold shared fixtures; `RtacResultMatchers` has custom MockMvc assertions.
- `TenantControllerTest` is a plain `Test` (not `IT`) despite hitting a controller — check `RtacCacheControllerIT`/`RtacControllerIT` for the WireMock-backed pattern when adding new endpoint tests.

## Configuration notes

- `application.yml`'s `spring.application.name: edge-courses` is stale copy-paste from another edge module — be aware of it but there's no indication it should be "fixed" incidentally.
- Secure store, TLS, and other runtime configuration is documented in `README.md`'s System Properties / env variable tables — consult it rather than guessing property names.
