# edge-rtac

Copyright (C) 2018 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Introduction

Edge API to interface w/ FOLIO for 3rd party discovery services to determine holdings availability.

## Overview

Coming Soon!

## Configuration

Configuration information is specified in two forms:  
1. System Properties - General configuration
1. Properties File - Configuration specific to the desired secure store

### System Properties

Proprety              | Default     | Description
--------------------- | ----------- | -------------
`port`                | `8081`      | Server port to listen on
`okapi_url`           | *required*  | Where to find OKAPI (URL)
`secure_store`        | `Ephemeral` | Type of secure store to use.  Valid: `Ephemeral`, `AwsSsm`, `Vault`
`secure_store_props`  | `NA`        | Path to a properties file specifying secure store configuration
`token_cache_ttl_ms`  | `3600000`   | How long to cache JWTs, in milliseconds (ms)
`token_cache_capacity`| `100`       | Max token cache size

### Secure Stores

Three secure stores currently implemented for safe retreival of encrypted credentials:

* **EphemeralStore** - Only intended for _development purposes_.  Credentials are defind in plain text in a specified properties file.  See `src/main/resources/ephemeral.properties`
* **AwsParamStore** - Retreives credentials from Amazon Web Services Systems Manager (AWS SSM), more specifically the Parameter Store, where they're stored encrypted using a KMS key.  See `src.main/resources/aws_ss.properties`
* **VaultStore** - Retreives credentials from a Vault (http://vaultproject.io).  This was added as a more generic alternative for those not using AWS.  See `src/main/resources/vault.properties`

## Additional information

### Issue tracker

See project [FOLIO](https://issues.folio.org/browse/FOLIO)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### Other documentation

Other [modules](https://dev.folio.org/source-code/#server-side) are described,
with further FOLIO Developer documentation at [dev.folio.org](https://dev.folio.org/)

