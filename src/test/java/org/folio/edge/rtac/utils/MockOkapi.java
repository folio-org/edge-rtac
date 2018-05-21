package org.folio.edge.rtac.utils;

import static org.folio.edge.rtac.Constants.APPLICATION_JSON;
import static org.folio.edge.rtac.Constants.TEXT_PLAIN;
import static org.folio.edge.rtac.Constants.X_OKAPI_TENANT;
import static org.folio.edge.rtac.Constants.X_OKAPI_TOKEN;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.folio.edge.rtac.model.Holdings;
import org.folio.edge.rtac.model.Holdings.Holding;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MockOkapi {

  private static final Logger logger = Logger.getLogger(MockOkapi.class);

  public static final String mockToken = "mynameisyonyonsonicomefromwisconsoniworkatalumbermillthereallthepeopleimeetasiwalkdownthestreetaskhowinthehelldidyougethereisaymynameisyonyonsonicomefromwisconson";
  public static final String titleId_notFound = "0c8e8ac5-6bcc-461e-a8d3-4b55a96addc9";

  public final int okapiPort;
  private final Vertx vertx;
  private final List<String> knownTenants;

  public MockOkapi(int port, List<String> knownTenants) {
    okapiPort = port;
    vertx = Vertx.vertx();
    this.knownTenants = knownTenants == null ? new ArrayList<>() : knownTenants;
  }

  public void close() {
    vertx.close(res -> {
      if(res.failed()) {
        logger.error("Failed to shut down mock OKAPI server", res.cause());
        fail(res.cause().getMessage());
      } else {
        logger.info("Successfully shut down mock OKAPI server");
      }
    });
  }

  public void start(TestContext context) {

    // Setup Mock Okapi...
    Router router = Router.router(vertx);
    HttpServer server = vertx.createHttpServer();

    router.route().handler(BodyHandler.create());
    router.route(HttpMethod.GET, "/_/proxy/health").handler(this::healthCheckHandler);
    router.route(HttpMethod.POST, "/authn/login").handler(this::loginHandler);
    router.route(HttpMethod.GET, "/rtac/:titleid").handler(this::modRtacHandler);

    final Async async = context.async();
    server.requestHandler(router::accept).listen(okapiPort, result -> {
      if (result.failed()) {
        logger.warn(result.cause());
      }
      context.assertTrue(result.succeeded());
      async.complete();
    });
  }

  public static String getHoldingsJson(String titleId) {
    Holding h = Holding.builder()
      .id("99712686103569")
      .callNumber("PS3552.E796 D44x 1975")
      .location("LC General Collection Millersville University Library")
      .status("Item in place")
      .tempLocation("")
      .dueDate("")
      .build();

    Holdings holdings = new Holdings();
    holdings.holdingRecords.add(h);

    String ret = null;
    try {
      ret = holdings.toJson();
    } catch (JsonProcessingException e) {
      logger.warn("Failed to generate holdings JSON", e);
    }
    return ret;
  }

  public void healthCheckHandler(RoutingContext ctx) {
    ctx.response()
      .setStatusCode(200)
      .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
      .end("[ ]");
  }

  public void loginHandler(RoutingContext ctx) {
    JsonObject body = null;
    try {
      body = ctx.getBodyAsJson();
    } catch (Exception e) {

    }

    String tenant = ctx.request().getHeader(X_OKAPI_TENANT);

    String contentType = TEXT_PLAIN;
    int status;
    String resp = null;
    if (tenant == null) {
      status = 400;
      resp = "Unable to process request Tenant must be set";
    } else if (body == null || !body.containsKey("username") || !body.containsKey("password")) {
      status = 400;
      resp = "Json content error";
    } else if (ctx.request().getHeader(HttpHeaders.CONTENT_TYPE) == null ||
        !ctx.request().getHeader(HttpHeaders.CONTENT_TYPE).equals(APPLICATION_JSON)) {
      status = 400;
      resp = String.format("Content-type header must be [\"%s\"]", APPLICATION_JSON);
    } else if (!knownTenants.contains(tenant)) {
      status = 400;
      resp = String.format("no such tenant %s", tenant);
    } else {
      status = 201;
      resp = body.toString();
      contentType = APPLICATION_JSON;
      ctx.response().putHeader(X_OKAPI_TOKEN, mockToken);
    }

    ctx.response()
      .setStatusCode(status)
      .putHeader(HttpHeaders.CONTENT_TYPE, contentType)
      .end(resp);
  }

  public void modRtacHandler(RoutingContext ctx) {
    String titleId = ctx.request().getParam("titleid");
    String token = ctx.request().getHeader(X_OKAPI_TOKEN);
    if (token == null || !token.equals(mockToken)) {
      ctx.response()
        .setStatusCode(403)
        .putHeader(HttpHeaders.CONTENT_TYPE, TEXT_PLAIN)
        .end("Access requires permission: rtac.holdings.item.get");
    } else if (titleId.equals(titleId_notFound)) {
      // Magic titleID signifying we want to mock a "not found"
      // response.
      ctx.response()
        .setStatusCode(404)
        .putHeader(HttpHeaders.CONTENT_TYPE, TEXT_PLAIN)
        .end("rtac not found");
    } else {
      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .end(getHoldingsJson(titleId));
    }
  }
}
