package org.folio.edge.rtac;

public class Constants {
  
  private Constants() {

  }

  // System Properties
  public static final String SYS_SECURE_STORE_PROP_FILE = "secure_store_props";
  public static final String SYS_SECURE_STORE_TYPE = "secure_store";
  public static final String SYS_OKAPI_URL = "okapi_url";
  public static final String SYS_PORT = "port";
  public static final String SYS_TOKEN_CACHE_TTL_MS = "token_cache_ttl_ms";
  public static final String SYS_TOKEN_CACHE_CAPACITY = "token_cache_capacity";

  // Property names
  public static final String PROP_SECURE_STORE_TYPE = "secureStore.type";

  // Defaults
  public static final String DEFAULT_SECURE_STORE_TYPE = "ephemeral";
  public static final String DEFAULT_PORT = "8081";
  public static final String DEFAULT_TOKEN_CACHE_TTL_MS = String.valueOf(60 * 60 * 1000);
  public static final String DEFAULT_TOKEN_CACHE_CAPACITY = "100";

  // Headers
  public static final String X_OKAPI_TENANT = "x-okapi-tenant";
  public static final String X_OKAPI_TOKEN = "x-okapi-token";

  // Param names
  public static final String PARAM_API_KEY = "apikey";
  public static final String PARAM_TITLE_ID = "mms_id";
}
