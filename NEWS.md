# v2.9.1 2025.03.17

* Update to edge-rtac Java 21 [EDGRTAC-100](https://folio-org.atlassian.net/browse/EDGRTAC-100)
* Sunflower 2025 R1 - Migrate AWS SDK for Java from 1.x to 2.x [EDGRTAC-97](https://folio-org.atlassian.net/browse/EDGRTAC-97)

# v2.8.0 2024.10.14

* Enhance single rtac response ([EDGRTAC-93](https://folio-org.atlassian.net/browse/EDGRTAC-93))
* Update Vert.x to v4.5.10 version ([EDGRTAC-96](https://folio-org.atlassian.net/browse/EDGRTAC-96))

# v2.7.3 2024.07.03

* [EDGRTAC-89](https://folio-org.atlassian.net/browse/EDGRTAC-89) edge-common 4.7.1: AwsParamStore to support FIPS-approved crypto modules

# v2.7.2 2024.05.29

* [EDGRTAC-88](https://folio-org.atlassian.net/browse/EDGRTAC-88) Vert.x 4.5.7 fixing netty-codec-http form POST OOM CVE-2024-29025
* Remove -SNAPSHOT from edge-common:4.7.0-SNAPSHOT
* [EDGRTAC-87](https://folio-org.atlassian.net/browse/EDGRTAC-87) aws-java-sdk-ssm 1.12.729 removing ion-java CVE-2024-21634
* [EDGRTAC-86](https://folio-org.atlassian.net/browse/EDGRTAC-86) Enhance HTTP Endpoint Security with TLS and FIPS-140-2 Compliant Cryptography 

# 2.7.1 2024-03-22

* US1238243: update edge-common version to 4.6.0 ([MODRTAC-109](https://folio-org.atlassian.net/browse/MODRTAC-109))

# 2.7.0 2024-03-20

* Upgrade edge-rtac Vert.x 4.5.4 ([MODRTAC-109](https://folio-org.atlassian.net/browse/MODRTAC-109))
* Add holdingsCopyNumber and ItemsCopyNumber to rtac response ([EDGRTAC-81](https://folio-org.atlassian.net/browse/EDGRTAC-81))

# 2.6.2 2023-12-07

* EDGRTAC-82: Upgrade edge-common to get refresh token rotation (RTR) ([EDGRTAC-82](https://issues.folio.org/browse/EDGRTAC-82))

# 2.6.1 2023-11-02

* Upgrade to Vert.x 4.4.6, Netty 4.1.100.Final (CVE-2023-44487) ([EDGRTAC-79](https://issues.folio.org/browse/EDGRTAC-79))

# 2.6.0 2022-10-19

* Upgrade to Log4J 2.18.0 (CVE-2021-44832) ([EDGRTAC-68](https://issues.folio.org/browse/EDGRTAC-68))
* Upgrade to edge-common 4.4.1 ([EDGRTAC-68](https://issues.folio.org/browse/EDGRTAC-68))
* Upgrade to vertx 4.3.3 ([EDGRTAC-68](https://issues.folio.org/browse/EDGRTAC-68))

# 2.5.0 2022-06-15

* Remove no longer supported vert.x completable future dependency. ([EDGRTAC-60](https://issues.folio.org/browse/EDGRTAC-60)

# 2.4.0 2022-02-23

* Upgrade to Log4J 2.17.0. (CVE-2021-44228, CVE-2021-45105) ([EDGRTAC-50](https://issues.folio.org/browse/EDGRTAC-50), [EDGRTAC-55](https://issues.folio.org/browse/EDGRTAC-55))

# 2.3.0 2021-10-05

* Upgrade to vert.x 4.x ([EDGRTAC-37](https://issues.folio.org/browse/EDGRTAC-37))
* Instance ID number is now included in responses in which holdings data could not be found or reported ([EDGRTAC-43](https://issues.folio.org/browse/EDGRTAC-43))

# 2.2.0 2021-03-16

* Respects the `accept` header provided by the client ([EDGRTAC-16](https://issues.folio.org/browse/EDGRTAC-16))

## 2.1.1 2020-11-02

Full [Changelog](https://github.com/folio-org/edge-rtac/compare/v2.1.0...v2.1.1)

 * [EDGRTAC-33](https://issues.folio.org/browse/EDGRTAC-33): Memory leak of ~200mb for /rtac?instanceIds=id1,id2,idn API

## 2.1.0 2020-10-14

Full [Changelog](https://github.com/folio-org/edge-rtac/compare/v2.0.2...v2.1.0)

 * [EDGRERTAC-26](https://issues.folio.org/browse/EDGRTAC-26): Fix security vulnerability reported in log4j 1.2
 * [EDGRERTAC-29](https://issues.folio.org/browse/EDGRTAC-29): REST batching support
 * [EDGRERTAC-30](https://issues.folio.org/browse/EDGRTAC-30): Update to java 11
 
## 2.0.2 2020-06-11

Full [Changelog](https://github.com/folio-org/edge-rtac/compare/v2.0.1...v2.0.2)

 * [EDGRERTAC-23](https://issues.folio.org/browse/EDGRTAC-23): Add 7.0 as acceptable login interface version
 * [FOLIO-2358](https://issues.folio.org/browse/FOLIO-2358): Use JVM features (UseContainerSupport, MaxRAMPercentage) to manage container memory
 * [FOLIO-2235](https://issues.folio.org/browse/FOLIO-2235): Add LaunchDescriptor settings to each backend non-core module repository

## 2.0.1 2019-07-24

Full [Changelog](https://github.com/folio-org/edge-rtac/compare/v2.0.0...v2.0.1)

 * [EDGRERTAC-11](https://issues.folio.org/browse/EDGRTAC-11): Provide "volume" in RTAC response
 * [EDGRERTAC-12](https://issues.folio.org/browse/EDGRTAC-12): Add `6.0` to the `login` interface
 * [EDGRERTAC-13](https://issues.folio.org/browse/EDGRTAC-13): Support HTTP compression

## 2.0.0 2019-02-15

Full [Changelog](https://github.com/folio-org/edge-rtac/compare/v1.2.4...v2.0.0)

 * [EDGRERTAC-8](https://issues.folio.org/browse/EDGRTAC-8): Add missing required interface from module descriptor
 * [EDGRERTAC-7](https://issues.folio.org/browse/EDGRTAC-7): Upgrade to edge-common-2.0.0
 * [EDGRERTAC-3](https://issues.folio.org/browse/EDGRTAC-3): Update to RAML 1.0

## 1.2.4 2018-12-05
 * [EDGRERTAC-4](https://issues.folio.org/browse/EDGRTAC-4): Updated the README
   EDGRTAC JIRA project link
 * [EDGRESOLV-5](https://issues.folio.org/browse/EDGRTAC-5): Updated the
   edge-common dependency to v1.0.0

## 0.0.1 2018-05-14
 * Initial Commit
