package org.folio.edge.rtac;

import static org.folio.edge.rtac.Constants.APPLICATION_XML;
import static org.folio.edge.rtac.Constants.PARAM_API_KEY;
import static org.folio.edge.rtac.Constants.PARAM_TITLE_ID;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;
import org.folio.edge.rtac.cache.TokenCache;
import org.folio.edge.rtac.cache.TokenCache.NotInitializedException;
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

  private static final String FALLBACK_EMPTY_RESPONSE = Mappers.PROLOG + "\n<holdings/>";

  private final SecureStore secureStore;
  private final OkapiClientFactory ocf;

  public RtacHandler(SecureStore secureStore, OkapiClientFactory ocf) {
    this.secureStore = secureStore;
    this.ocf = ocf;
  }

  protected void rtacHandler(RoutingContext ctx) {

    String key = ctx.request().getParam(PARAM_API_KEY);
    String id = ctx.request().getParam(PARAM_TITLE_ID);

    if (id == null || id.isEmpty() || key == null || key.isEmpty()) {
      returnEmptyResponse(ctx);
    } else {
      String tenant = getTenant(key);
      if (tenant == null) {
        returnEmptyResponse(ctx);
        return;
      }

      final OkapiClient client = ocf.getOkapiClient(tenant);

      // get token via cache or logging in
      CompletableFuture<String> tokenFuture = getToken(client, tenant, tenant);
      if (tokenFuture.isCompletedExceptionally()) {
        returnEmptyResponse(ctx);
      } else {
        tokenFuture.thenAcceptAsync(token -> {
          client.setToken(token);
          // call mod-rtac
          client.rtac(id, ctx.request().headers()).thenAcceptAsync(body -> {
            String xml = null;
            try {
              xml = Holdings.fromJson(body).toXml();
              logger.info("Converted Response: \n" + xml);
              ctx.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
                .end(xml);
            } catch (IOException e) {
              logger.error("Exception translating JSON -> XML: " + e.getMessage());
              returnEmptyResponse(ctx);
            }
          }).exceptionally(t -> {
            logger.error("Exception calling mod-rtac", t);
            returnEmptyResponse(ctx);
            return null;
          });
        });
      }
    }
  }

  private void returnEmptyResponse(RoutingContext ctx) {
    // NOTE: We always return a 200 even if holdings is empty here
    // because that's what the API we're trying to mimic does...
    // Yes, even if the response from mod-rtac is non-200!
    String xml = null;
    try {
      xml = new Holdings().toXml();
    } catch (JsonProcessingException e) {
      // OK, we'll doing ourselves then
      xml = FALLBACK_EMPTY_RESPONSE;
    }
    ctx.response()
      .setStatusCode(200)
      .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .end(xml);
  }

  private String getTenant(String apiKey) {
    String tenant = null;
    try {
      tenant = new String(Base64.getUrlDecoder().decode(apiKey.getBytes()));
      logger.info(String.format("API Key: %s, Tenant: %s", apiKey, tenant));

    } catch (Exception e) {
      logger.error(String.format("Failed to parse API Key %s", apiKey), e);
    }
    return tenant;
  }

  private CompletableFuture<String> getToken(OkapiClient client, String tenant, String username) {
    CompletableFuture<String> future = new CompletableFuture<>();

    String token = null;
    try {
      TokenCache cache = TokenCache.getInstance();
      token = cache.get(tenant, username);
    } catch (NotInitializedException e) {
      logger.warn("Failed to access TokenCache", e);
    }

    if (token != null) {
      logger.info("Using cached token");
      future.complete(token);
    } else {
      String password = secureStore.get(tenant, username);

      CompletableFuture<String> loginFuture = client.login(username, password);

      if (loginFuture.isCompletedExceptionally()) {
        try {
          loginFuture.get();
        } catch (Exception e) {
          logger.error("Login Failed", e);
          future.completeExceptionally(e);
        }
      } else {
        loginFuture.thenAccept(t -> {
          try {
            TokenCache.getInstance().put(tenant, username, t);
          } catch (NotInitializedException e) {
            logger.warn("Failed to cache token", e);
          }
          future.complete(t);
        });
      }
    }

    return future;
  }
}
