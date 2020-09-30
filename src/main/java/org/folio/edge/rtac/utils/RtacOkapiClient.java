package org.folio.edge.rtac.utils;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.core.utils.OkapiClient;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

public class RtacOkapiClient extends OkapiClient {

  private static final Logger logger = LogManager.getLogger(RtacOkapiClient.class);
  private static final String RTAC_API_URI = "/rtac/batch";

  public RtacOkapiClient(OkapiClient client) {
    super(client);
  }

  protected RtacOkapiClient(Vertx vertx, String okapiURL, String tenant, long timeout) {
    super(vertx, okapiURL, tenant, timeout);
  }

  public CompletableFuture<String> rtac(String titleId) {
    return rtac(titleId, null);
  }

  public CompletableFuture<String> rtac(String requestBody, MultiMap headers) {
    VertxCompletableFuture<String> future = new VertxCompletableFuture<>(vertx);

    post(
        okapiURL + RTAC_API_URI,
        tenant,
        requestBody,
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
}
