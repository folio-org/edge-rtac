package org.folio.edge.rtac.utils;

import static org.folio.edge.rtac.Constants.DEFAULT_TOKEN_CACHE_CAPACITY;
import static org.folio.edge.rtac.Constants.DEFAULT_TOKEN_CACHE_TTL_MS;
import static org.folio.edge.rtac.Constants.SYS_TOKEN_CACHE_CAPACITY;
import static org.folio.edge.rtac.Constants.SYS_TOKEN_CACHE_TTL_MS;
import static org.folio.edge.rtac.Constants.X_OKAPI_TOKEN;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;
import org.folio.edge.rtac.cache.TokenCache;
import org.folio.edge.rtac.cache.TokenCache.TokenCacheBuilder;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class OkapiClient {

  private static final Logger logger = Logger.getLogger(OkapiClient.class);

  private final String okapiURL;
  private final HttpClient httpClient;
  private final String tenant;
  private final TokenCache<String> tokenCache;

  protected final Map<String, String> defaultHeaders = new HashMap<>();

  public OkapiClient(Vertx vertx, String okapiURL, String tenant) {
    this.okapiURL = okapiURL;
    this.tenant = tenant;
    httpClient = new HttpClient(vertx);

    final String tokenCacheTtlMs = System.getProperty(SYS_TOKEN_CACHE_TTL_MS,
        DEFAULT_TOKEN_CACHE_TTL_MS);
    final long cacheTtlMs = Long.parseLong(tokenCacheTtlMs);
    logger.info("Using token cache TTL (ms): " + tokenCacheTtlMs);

    final String tokenCacheCapacity = System.getProperty(SYS_TOKEN_CACHE_CAPACITY,
        DEFAULT_TOKEN_CACHE_CAPACITY);
    final int cacheCapacity = Integer.parseInt(tokenCacheCapacity);
    logger.info("Using token cache capacity: " + tokenCacheCapacity);

    tokenCache = new TokenCacheBuilder<String>()
      .withCapacity(cacheCapacity)
      .withTTL(cacheTtlMs)
      .build();
  }

  public CompletableFuture<Void> getToken(String username, String password) {
    CompletableFuture<Void> future = new CompletableFuture<>();

    String token = tokenCache.get(tenant, username);
    if (token != null) {
      setToken(token);
      future.complete(null);
    } else {
      JsonObject payload = new JsonObject();
      payload.put("username", username);
      payload.put("password", password);

      httpClient.post(
          okapiURL + "/authn/login",
          tenant,
          payload.encode(),
          response -> response.bodyHandler(body -> {

            try {
              if (response.statusCode() == 201) {
                logger.info("Successfully logged into FOLIO");
                setToken(response.getHeader(X_OKAPI_TOKEN));
                tokenCache.put(tenant, username, token);
                future.complete(null);
              } else {
                logger.warn(String.format(
                    "Failed to log into FOLIO: (%s) %s",
                    response.statusCode(),
                    body.toString()));
                future.complete(null);
              }
            } catch (Exception e) {
              logger.warn("Exception during login: " + e.getMessage());
              future.completeExceptionally(e);
            }
          }));
    }
    return future;
  }

  public CompletableFuture<String> rtac(String titleId) {
    CompletableFuture<String> future = new CompletableFuture<>();
    httpClient.get(
        okapiURL + "/rtac/" + titleId,
        tenant,
        defaultHeaders, response -> response.bodyHandler(body -> {
          try {
            if (response.statusCode() == 200) {
              logger.info(String.format(
                  "Successfully retrieved title info from mod-rtac: (%s) %s",
                  response.statusCode(),
                  body.toString()));
              future.complete(body.toString());
            } else {
              String err = String.format(
                  "Failed to get title info from mod-rtac: (%s) %s",
                  response.statusCode(),
                  body.toString());
              logger.error(err);
              future.complete("{}");
            }
          } catch (Exception e) {
            logger.error("Exception calling mod-rtac: " + e.getMessage());
            future.complete("{}");
          }
        }));
    return future;
  }

  private void setToken(String token) {
    defaultHeaders.put(X_OKAPI_TOKEN, token);
  }
}
