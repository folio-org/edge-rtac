# 2.2.1 2021-12-15
* Upgrade to Log4J 2.16.0. (CVE-2021-44228) (EDGRTAC-52)

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
