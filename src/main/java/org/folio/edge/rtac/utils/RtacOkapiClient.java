package org.folio.edge.rtac.utils;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.core.utils.OkapiClient;

import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;

public class RtacOkapiClient extends OkapiClient {

  private static final Logger logger = LogManager.getLogger(RtacOkapiClient.class);
  private static final String RTAC_API_URI = "/rtac-batch";

  public RtacOkapiClient(OkapiClient client) {
    super(client);
  }
 
  protected RtacOkapiClient(Vertx vertx, String okapiURL, String tenant, int timeout) {
    super(vertx, okapiURL, tenant, timeout);
  }

  public CompletableFuture<String> rtac(String titleId) {
    return rtac(titleId, null);
  }

  public CompletableFuture<String> rtac(String requestBody, MultiMap headers) {
    final Promise<HttpResponse<Buffer>> promise = Promise.promise();

    post(okapiURL + RTAC_API_URI, tenant, requestBody,
      combineHeadersWithDefaults(headers), promise::complete, promise::fail);

    return promise.future()
      .map(RtacOkapiClient::interpretResponse)
      .onFailure(RtacOkapiClient::logError)
      .toCompletionStage().toCompletableFuture();
  }

  private static void logError(Throwable t) {
    logger.error("Exception when calling mod-rtac: {}", t.getMessage());
  }

  private static String interpretResponse(HttpResponse<Buffer> response) {
    int statusCode = response.statusCode();
    String responseBody = response.body().toString();

    if (statusCode == 200) {
      logger.info("Successfully retrieved title info from mod-rtac: ({}) {}",
        statusCode, responseBody);

      return responseBody;
    } else {
      logger.error(
          "Failed to get title info from mod-rtac: ({}) {}",
          response.statusCode(), responseBody);

      // Failure is converted to empty response
      return new JsonObject().encode();
    }
  }
}
