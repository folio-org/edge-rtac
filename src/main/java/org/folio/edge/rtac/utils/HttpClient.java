package org.folio.edge.rtac.utils;

import java.util.Map;

import org.apache.log4j.Logger;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;

import static org.folio.edge.rtac.Constants.*;

public class HttpClient {
  private static final Logger logger = Logger.getLogger(HttpClient.class);

  public static final long DEFAULT_REQUEST_TIMEOUT = 3 * 1000; // ms

  private final Vertx vertx;

  public HttpClient(Vertx vertx) {
    this.vertx = vertx;
  }

  public void post(String url, String tenant, String payload, Handler<HttpClientResponse> responseHandler) {
    post(url, tenant, payload, null, responseHandler);
  }

  public void post(String url, String tenant, String payload, Map<String, String> headers,
      Handler<HttpClientResponse> responseHandler) {
    io.vertx.core.http.HttpClient httpClient = null;

    try {
      httpClient = vertx.createHttpClient();

      if (logger.isTraceEnabled())
        logger.trace("POST " + url + " Request: " + payload);

      final HttpClientRequest request = httpClient.postAbs(url);

      // safe to assume application/json. I *think* Caller can still
      // override
      request.putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
        .putHeader(HttpHeaders.ACCEPT.toString(), "application/json, text/plain")
        .putHeader("X-Okapi-Tenant", tenant);

      if (headers != null) {
        request.headers().addAll(headers);
      }

      request.handler(responseHandler);

      request.setTimeout(DEFAULT_REQUEST_TIMEOUT)
        .end(payload);
    } finally {
      if (httpClient != null)
        httpClient.close();
    }
  }

  public void get(String url, String tenant, Handler<HttpClientResponse> responseHandler) {
    get(url, tenant, null, responseHandler);
  }

  public void get(String url, String tenant, Map<String, String> headers,
      Handler<HttpClientResponse> responseHandler) {
    io.vertx.core.http.HttpClient httpClient = null;

    try {
      httpClient = vertx.createHttpClient();

      logger.info("GET " + url + " tenant: " + tenant + " token: " + headers.get(X_OKAPI_TOKEN));

      final HttpClientRequest request = httpClient.getAbs(url);

      request.putHeader(HttpHeaders.ACCEPT.toString(), "application/json, text/plain")
        .putHeader(X_OKAPI_TENANT, tenant);

      if (headers != null) {
        request.headers().addAll(headers);
      }

      request.handler(responseHandler)
        .setTimeout(DEFAULT_REQUEST_TIMEOUT)
        .end();
    } finally {
      if (httpClient != null)
        httpClient.close();
    }
  }
}
