# edge-rtac

Copyright (C) 2018 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Introduction

Edge API to interface w/ FOLIO for 3rd party discovery services to determine holdings availability.

## Overview

The purpose of this edge API is to bridge the gap between 3rd party discovery services and FOLIO.  More specifically, the initial implementation was built with EBSCO's Real Time Availability Check (RTAC) service in mind.  RTAC is used by the EBSCO Discovery Service (EDS) to obtain up-to-date holdings information (status, call number, location, due date, etc.).  

In order to reduce the amount of integration work on the discovery side, the API implemented here mimics an existing RTAC integration.  This includes the parameter names, data formats, API endpoint names, among other things.  There are certain quirks about the API being mimiced that may seem unintuitive, such as failures of any sort still result in 200 response, but with an empty `<holdings/>` XML payload.  Eventually all this will be documented.

## Security

Since the RTAC configuration for EDS provides an API key, which we use here to identify a FOLIO tenant.  For now, the API key is simply a url safe base64 encoding of the tenantId.  The Edge API decodes this API key and retreives credentials for an "institutional user" from the configured secure storage.  Additional information can be found in the "Secure Stores" section below.

### Insitutional Users

The idea here is that a FOLIO user is created for each tenant for the purposes of RTAC and eventually other APIs.  The credentials are stored in one of the secure stores and retreived as needed by the edge API.

The Edge API does not create users, or write credentials.  Those need to be provisioned manually or by some other process.  The current secure stores expect credentials to be stored in a way that adheres to naming conventions.  See the various secure store sections below for specifics.

Currently the institutional username is the same as the tenantId, but this is subject to change.

### Secure Stores

Three secure stores currently implemented for safe retreival of encrypted credentials:

#### EphemeralStore ####

Only intended for _development purposes_.  Credentials are defind in plain text in a specified properties file.  See `src/main/resources/ephemeral.properties`

#### AwsParamStore ####

Retreives credentials from Amazon Web Services Systems Manager (AWS SSM), more specifically the Parameter Store, where they're stored encrypted using a KMS key.  See `src.main/resources/aws_ss.properties`

**Key:** `<tenantId>_<username>` 

E.g. Key=`diku_diku`

#### VaultStore ####

Retreives credentials from a Vault (http://vaultproject.io).  This was added as a more generic alternative for those not using AWS.  See `src/main/resources/vault.properties`

**Key:** `secrets/<tenantId>` 
**Field:** `<username>`

E.g. Key=`secrets/diku`, Field=`diku`

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

## Additional information

### Issue tracker

See project [FOLIO](https://issues.folio.org/browse/FOLIO)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### Other documentation

Other [modules](https://dev.folio.org/source-code/#server-side) are described,
with further FOLIO Developer documentation at [dev.folio.org](https://dev.folio.org/)

