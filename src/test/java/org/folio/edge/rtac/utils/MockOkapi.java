package org.folio.edge.rtac.utils;

import static org.folio.edge.rtac.Constants.X_OKAPI_TENANT;
import static org.folio.edge.rtac.Constants.X_OKAPI_TOKEN;

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
import io.vertx.ext.web.handler.BodyHandler;

public class MockOkapi {

  private static final Logger logger = Logger.getLogger(MockOkapi.class);

  public static final String mockToken = "mynameisyonyonsonicomefromwisconsoniworkatalumbermillthereallthepeopleimeetasiwalkdownthestreetaskhowinthehelldidyougethereisaymynameisyonyonsonicomefromwisconson";
  public static final String titleId_notFound = "0c8e8ac5-6bcc-461e-a8d3-4b55a96addc9";

  public final int okapiPort;
  public final Vertx vertx;

  public MockOkapi(int port) {
    okapiPort = port;
    vertx = Vertx.vertx();
  }

  public void start(TestContext context) {

    // Setup Mock Okapi...
    Router router = Router.router(vertx);
    HttpServer server = vertx.createHttpServer();

    router.route().handler(BodyHandler.create());
    router.route(HttpMethod.POST, "/authn/login").handler(ctx -> {
      JsonObject body = ctx.getBodyAsJson();

      int status;
      String resp = null;
      if (ctx.request().getHeader(X_OKAPI_TENANT) == null) {
        status = 400;
        resp = "Unable to process request Tenant must be set";
      } else if (!body.containsKey("username") || !body.containsKey("password")) {
        status = 400;
        resp = "Json content error";
      } else if (ctx.request().getHeader(HttpHeaders.CONTENT_TYPE) == null ||
          !ctx.request().getHeader(HttpHeaders.CONTENT_TYPE).equals("application/json")) {
        status = 400;
        resp = "Content-type header must be [\"application/json\"]";
      } else {
        status = 201;
        resp = body.toString();
      }

      ctx.response()
        .setStatusCode(status)
        .putHeader(X_OKAPI_TOKEN, mockToken)
        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .end(resp);
    });
    router.route(HttpMethod.GET, "/rtac/:titleid").handler(ctx -> {
      String titleId = ctx.request().getParam("titleid");
      String token = ctx.request().getHeader(X_OKAPI_TOKEN);
      if (token == null || !token.equals(mockToken)) {
        ctx.response()
          .setStatusCode(403)
          .end("Access requires permission: rtac.holdings.item.get");
      } else if (titleId.equals(titleId_notFound)) {
        // Magic titleID signifying we want to mock a "not found"
        // response.
        ctx.response()
          .setStatusCode(404)
          .end("rtac not found");
      } else {
        ctx.response()
          .setStatusCode(200)
          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .end(getHoldingsJson(titleId));
      }
    });

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
    Holding h = new Holding();
    h.id = titleId;
    h.callNumber = "PS3552.E796 D44x 1975";
    h.location = "LC General Collection Millersville University Library";
    h.status = "Item in place";
    h.tempLocation = "";
    h.dueDate = "";

    Holdings holdings = new Holdings();
    holdings.holdings.add(h);

    String ret = null;
    try {
      ret = holdings.toJson();
    } catch (JsonProcessingException e) {
      logger.warn("Failed to generate holdings JSON", e);
    }
    return ret;
  }
}
