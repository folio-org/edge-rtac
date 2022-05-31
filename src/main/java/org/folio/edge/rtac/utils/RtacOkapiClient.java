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
import lombok.AllArgsConstructor;
import lombok.Value;

public class RtacOkapiClient extends OkapiClient {
  private static final Logger logger = LogManager.getLogger(RtacOkapiClient.class);

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

    post(okapiURL + "/rtac-batch", tenant, requestBody,
      combineHeadersWithDefaults(headers), promise::complete, promise::fail);

    return promise.future()
      .map(Response::new)
      .map(new ResponseInterpreter()::interpretResponse)
      .onFailure(RtacOkapiClient::logError)
      .toCompletionStage().toCompletableFuture();
  }

  private static void logError(Throwable t) {
    logger.error("Exception when calling mod-rtac: {}", t.getMessage());
  }

  @Value
  @AllArgsConstructor
  static class Response {
    int statusCode;
    String body;

    public Response(HttpResponse<Buffer> response) {
      this(response.statusCode(), response.body().toString());
    }
  }

  static class ResponseInterpreter {
    String interpretResponse(Response response) {
      if (response.getStatusCode() == 200) {
        logger.info("Successfully retrieved title info from mod-rtac: ({}) {}",
          response.getStatusCode(), response.getBody());

        return response.getBody();
      } else {
        logger.error("Failed to get title info from mod-rtac: ({}) {}",
            response.getStatusCode(), response.getBody());

        // Failure is converted to empty response
        return new JsonObject().encode();
      }
    }
  }
}
