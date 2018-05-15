package org.folio.edge.rtac;

import static org.folio.edge.rtac.Constants.DEFAULT_TOKEN_CACHE_CAPACITY;
import static org.folio.edge.rtac.Constants.DEFAULT_TOKEN_CACHE_TTL_MS;
import static org.folio.edge.rtac.Constants.PARAM_API_KEY;
import static org.folio.edge.rtac.Constants.PARAM_TITLE_ID;
import static org.folio.edge.rtac.Constants.SYS_TOKEN_CACHE_CAPACITY;
import static org.folio.edge.rtac.Constants.SYS_TOKEN_CACHE_TTL_MS;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;
import org.folio.edge.rtac.cache.TokenCache;
import org.folio.edge.rtac.cache.TokenCache.TokenCacheBuilder;
import org.folio.edge.rtac.model.Holdings;
import org.folio.edge.rtac.security.SecureStore;
import org.folio.edge.rtac.utils.Mappers;
import org.folio.edge.rtac.utils.OkapiClient;
import org.folio.edge.rtac.utils.OkapiClientFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class RtacHandler {

  private static final Logger logger = Logger.getLogger(RtacHandler.class);

  private final SecureStore secureStore;
  private final OkapiClientFactory ocf;
  private final TokenCache<String> tokenCache;

  public RtacHandler(SecureStore secureStore, OkapiClientFactory ocf) {
    this.secureStore = secureStore;
    this.ocf = ocf;

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

  protected void rtacHandler(RoutingContext ctx) {

    String key = ctx.request().getParam(PARAM_API_KEY);
    String id = ctx.request().getParam(PARAM_TITLE_ID);

    if (id == null || id.isEmpty() || key == null || key.isEmpty()) {
      // NOTE: We always return a 200 even if holdings is empty here
      // because that's what the API we're trying to mimic does...
      // Yes, even if the response from mod-rtac is non-200!
      String xml = null;
      try {
        xml = new Holdings().toXml();
      } catch (JsonProcessingException e) {
        // OK, we'll doing ourselves then
        xml = Mappers.PROLOG + "\n<holdings/>";
      }
      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, "application/xml")
        .end(xml);
    } else {
      String tenant = new String(Base64.getUrlDecoder().decode(key));

      logger.info(String.format("API Key: %s, Tenant: %s", key, tenant));

      OkapiClient client = ocf.getOkapiClient(tenant);

      String user = tenant;
      String password = secureStore.get(tenant, user);

      // login
      getToken(client, tenant, user, password).thenRun(() -> {
        // call mod-rtac
        client.rtac(id).thenAcceptAsync(body -> {
          String xml = null;
          try {
            logger.info("Original Response: \n" + body);
            xml = Holdings.fromJson(body).toXml();
            logger.info("Converted Response: \n" + xml);
          } catch (Exception e) {
            xml = Mappers.PROLOG + "\n<holdings/>";
            logger.error("Exception translating JSON -> XML: " + e.getMessage());
          }

          // NOTE: Again, we return a 200 here because that's what the
          // API
          // we're trying to mimic does
          ctx.response()
            .setStatusCode(200)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/xml")
            .end(xml);
        });
      });
    }
  }

  private CompletableFuture<String> getToken(OkapiClient client, String tenant, String username, String password) {
    CompletableFuture<String> future = new CompletableFuture<>();

    String token = tokenCache.get(tenant, username);
    if (token != null) {
      future.complete(token);
    } else {
      client.login(username, password).thenAccept(t -> {
        tokenCache.put(tenant, username, t);
        future.complete(t);
      });
    }

    return future;
  }
}
