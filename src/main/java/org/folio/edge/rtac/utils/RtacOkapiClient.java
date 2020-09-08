package org.folio.edge.rtac.utils;

import java.util.concurrent.CompletableFuture;

import org.folio.edge.core.utils.OkapiClient;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

public class RtacOkapiClient extends OkapiClient {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public RtacOkapiClient(OkapiClient client) {
    super(client);
  }

  protected RtacOkapiClient(Vertx vertx, String okapiURL, String tenant, long timeout) {
    super(vertx, okapiURL, tenant, timeout);
  }

  public CompletableFuture<String> rtac(String titleId) {
    return rtac(titleId, null);
  }

  public CompletableFuture<String> rtac(String instanceId, MultiMap headers) {
    VertxCompletableFuture<String> future = new VertxCompletableFuture<>(vertx);

    post(
        okapiURL + "/rtac/batch",
        tenant,
        createRequestBody(instanceId),
        combineHeadersWithDefaults(headers),
        response -> response.bodyHandler(body -> {
          int statusCode = response.statusCode();
          if (statusCode == 200) {
            String responseBody = body.toString();
            logger.info(String.format(
                "Successfully retrieved title info from mod-rtac: (%s) %s",
                statusCode,
                responseBody));
            future.complete(responseBody);
          } else {
            String err = String.format(
                "Failed to get title info from mod-rtac: (%s) %s",
                response.statusCode(),
                body.toString());
            logger.error(err);
            future.complete("{}");
          }
        }),
        t -> {
          logger.error("Exception: " + t.getMessage());
          future.completeExceptionally(t);
        });
    return future;
  }

  private String createRequestBody(String instanceIds) {
    final var payload = new JsonObject();
    payload.put("instanceIds", new JsonArray().add(instanceIds));
    logger.debug("request body to rtac {}", payload.toString());
    return payload.toString();
  }
}
