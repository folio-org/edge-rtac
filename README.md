# edge-rtac

Copyright (C) 2018-2023 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Introduction

Edge API to interface with FOLIO for 3rd party discovery services to determine holdings availability.

## Overview

The purpose of this edge API is to bridge the gap between 3rd party discovery services and FOLIO.  More specifically, the initial implementation was built with EBSCO's Real Time Availability Check (RTAC) service in mind.  RTAC is used by the EBSCO Discovery Service (EDS) to obtain up-to-date holdings information (status, call number, location, due date, etc.).

In order to reduce the amount of integration work on the discovery side, the API implemented here mimics an existing RTAC integration.  This includes the parameter names, data formats, API endpoint names, among other things.  There are certain quirks about the API being mimicked that may seem unintuitive, such as failures of any sort still result in 200 response, but with an empty `<holdings/>` XML payload.  Eventually all this will be documented.

## Security

See [edge-common](https://github.com/folio-org/edge-common) for a description of the security model.

## Requires Permissions

Institutional users should be granted the following permission in order to use this edge API:
- `rtac.all`

## Configuration

* See [edge-common](https://github.com/folio-org/edge-common) for a description of how configuration works.
* See [FOLIO-2835](https://issues.folio.org/browse/FOLIO-2835): Following the 2.1.0 release, the nginx proxy for edge modules in the environments needs to be able to route to two different paths on the edge-rtac module:
```
/prod/rtac/folioRTAC
/rtac
```

### System Properties

| Property               | Default     | Description                                                             |
|------------------------|-------------|-------------------------------------------------------------------------|
| `port`                 | `8081`      | Server port to listen on                                                |
| `okapi_url`            | *required*  | Where to find Okapi (URL)                                               |
| `request_timeout_ms`   | `30000`     | Request Timeout                                                         |
| `ssl_enabled`          | `false`     | Set whether SSL/TLS is enabled for Vertx Http Server                    |
| `keystore_type`        | `NA`        | Set the key store type                                                  |
| `keystore_provider`    | `NA`        | Set the provider name of the key store                                  |
| `keystore_path`        | `NA`        | Set the path to the key store                                           |
| `keystore_password`    | `NA`        | Set the password for the key store                                      |
| `key_alias`            | `NA`        | Optional identifier that points to a specific key within the key store  |
| `key_alias_password`   | `NA`        | Optional param that points to a password of `key_alias` if it protected |
| `log_level`            | `INFO`      | Log4j Log Level                                                         |
| `token_cache_capacity` | `100`       | Max token cache size                                                    |
| `token_cache_ttl_ms`   | `100`       | How long to cache JWTs, in milliseconds (ms)                            |
| `secure_store`         | `Ephemeral` | Type of secure store to use.  Valid: `Ephemeral`, `AwsSsm`, `Vault`     |
| `secure_store_props`   | `NA`        | Path to a properties file specifying secure store configuration         |

### System Properties for TLS configuration for Http server
To configure Transport Layer Security (TLS) for HTTP server in edge module, the following configuration parameters should be used.
Parameters marked as Required are required only in case when ssl_enabled is set to true.

| Property                          | Default           | Description                                                                                 |
|-----------------------------------|-------------------|---------------------------------------------------------------------------------------------|
| `http-server.ssl_enabled`         | `false`           | Set whether SSL/TLS is enabled for Vertx Http Server                                        |
| `http-server.keystore_type`       | `NA`              | (Required). Set the type of the keystore. Common types include `JKS`, `PKCS12`, and `BCFKS` |
| `http-server.keystore_provider`   | `NA`              | Set the provider name of the key store                                                      |
| `http-server.keystore_path`       | `NA`              | (Required). Set the location of the keystore file in the local file system                  |
| `http-server.keystore_password`   | `NA`              | (Required). Set the password for the keystore                                               |
| `http-server.key_alias`           | `NA`              | Set the alias of the key within the keystore.                                               |
| `http-server.key_alias_password`  | `NA`              | Point to a password of `key_alias` if it is protected                                       |

### System Properties for TLS configuration for Web Client
To configure Transport Layer Security (TLS) for Web clients in the edge module, you can use the following configuration parameters.
Truststore parameters for configuring Web clients are optional even when ssl_enabled = true.
If truststore parameters need to be populated - truststore_type, truststore_path, truststore_password - are required.

| Property                          | Default           | Description                                                                     |
|-----------------------------------|-------------------|---------------------------------------------------------------------------------|
| `web-client.ssl_enabled`          | `false`           | Set whether SSL/TLS is enabled for Vertx Http Server                            |
| `web-client.truststore_type`      | `NA`              | Set the type of the keystore. Common types include `JKS`, `PKCS12`, and `BCFKS` |
| `web-client.truststore_provider`  | `NA`              | Set the provider name of the key store                                          |
| `web-client.truststore_path`      | `NA`              | Set the location of the keystore file in the local file system                  |
| `web-client.truststore_password`  | `NA`              | Set the password for the keystore                                               |
| `web-client.key_alias`            | `NA`              | Set the alias of the key within the keystore.                                   |
| `web-client.key_alias_password`   | `NA`              | Point to a password of `key_alias` if it is protected                           |


## Additional information

### Issue tracker

See project [EDGRTAC](https://issues.folio.org/browse/EDGRTAC)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### Other documentation

Other [modules](https://dev.folio.org/source-code/#server-side) are described,
with further FOLIO Developer documentation at [dev.folio.org](https://dev.folio.org/)

