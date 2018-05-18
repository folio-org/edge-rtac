package org.folio.edge.rtac.utils;

import static org.folio.edge.rtac.Constants.APPLICATION_JSON;
import static org.folio.edge.rtac.Constants.TEXT_PLAIN;
import static org.folio.edge.rtac.Constants.X_OKAPI_TENANT;
import static org.folio.edge.rtac.Constants.X_OKAPI_TOKEN;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;

public class OkapiClient {

  private static final Logger logger = Logger.getLogger(OkapiClient.class);

  public static final long DEFAULT_REQUEST_TIMEOUT = 3 * 1000L; // ms

  private final String okapiURL;
  private final HttpClient client;
  private final String tenant;

  protected final Map<String, String> defaultHeaders = new HashMap<>();

  public OkapiClient(Vertx vertx, String okapiURL, String tenant) {
    this.okapiURL = okapiURL;
    this.tenant = tenant;
    this.client = vertx.createHttpClient(new HttpClientOptions().setKeepAlive(false));
  }

  public CompletableFuture<String> login(String username, String password) {
    CompletableFuture<String> future = new CompletableFuture<>();

    JsonObject payload = new JsonObject();
    payload.put("username", username);
    payload.put("password", password == null ? "" : password);

    post(
        okapiURL + "/authn/login",
        tenant,
        payload.encode(),
        response -> response.bodyHandler(body -> {
          try {
            if (response.statusCode() == 201) {
              logger.info("Successfully logged into FOLIO");
              String token = response.getHeader(X_OKAPI_TOKEN);
              setToken(token);
              future.complete(token);
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
    return future;
  }

  public CompletableFuture<String> rtac(String titleId) {
    CompletableFuture<String> future = new CompletableFuture<>();
    get(
        okapiURL + "/rtac/" + titleId,
        tenant,
        defaultHeaders, response -> response.bodyHandler(body -> {
          try {
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
          } catch (Exception e) {
            logger.error("Exception calling mod-rtac: " + e.getMessage());
            future.complete("{}");
          }
        }));
    return future;
  }

  public CompletableFuture<Boolean> healthy() {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    get(
        okapiURL + "/_/proxy/health",
        tenant, response -> response.bodyHandler(body -> {
          try {
            int status = response.statusCode();
            if (status == 200) {
              future.complete(true);
            } else {
              logger.error(String.format("OKAPI is unhealthy! status: %s body: %s", status, body.toString()));
              future.complete(false);
            }
          } catch (Exception e) {
            logger.error("Exception checking OKAPI's health: " + e.getMessage());
            future.complete(false);
          }
        }));
    return future;
  }

  public void setToken(String token) {
    defaultHeaders.put(X_OKAPI_TOKEN, token == null ? "" : token);
  }

  public void post(String url, String tenant, String payload, Handler<HttpClientResponse> responseHandler) {
    post(url, tenant, payload, null, responseHandler);
  }

  public void post(String url, String tenant, String payload, Map<String, String> headers,
      Handler<HttpClientResponse> responseHandler) {

    if (logger.isTraceEnabled())
      logger.trace("POST " + url + " Request: " + payload);

    final HttpClientRequest request = client.postAbs(url);

    // safe to assume application/json. I *think* Caller can still
    // override
    request.putHeader(HttpHeaders.CONTENT_TYPE.toString(), APPLICATION_JSON)
      .putHeader(HttpHeaders.ACCEPT.toString(), String.format("%s, %s", APPLICATION_JSON, TEXT_PLAIN))
      .putHeader(X_OKAPI_TENANT, tenant);

    if (headers != null) {
      request.headers().addAll(headers);
    }

    request.handler(responseHandler);

    request.setTimeout(DEFAULT_REQUEST_TIMEOUT)
      .end(payload);
  }

  public void get(String url, String tenant, Handler<HttpClientResponse> responseHandler) {
    get(url, tenant, null, responseHandler);
  }

  public void get(String url, String tenant, Map<String, String> headers,
      Handler<HttpClientResponse> responseHandler) {

    logger.info(String.format("GET %s tenant: %s token: %s", url, tenant,
        (headers != null) ? headers.get(X_OKAPI_TOKEN) : ""));

    final HttpClientRequest request = client.getAbs(url);

    request.putHeader(HttpHeaders.ACCEPT.toString(), String.format("%s, %s", APPLICATION_JSON, TEXT_PLAIN))
      .putHeader(X_OKAPI_TENANT, tenant);

    if (headers != null) {
      request.headers().addAll(headers);
    }

    request.handler(responseHandler)
      .setTimeout(DEFAULT_REQUEST_TIMEOUT)
      .end();
  }

  public void close() {
    client.close();
  }
}
