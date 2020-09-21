package org.folio.edge.rtac.utils;

import static org.folio.edge.core.Constants.APPLICATION_JSON;
import static org.folio.edge.core.Constants.TEXT_PLAIN;
import static org.folio.edge.core.Constants.X_OKAPI_TOKEN;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.folio.edge.core.utils.test.MockOkapi;
import org.folio.edge.rtac.model.Error;
import org.folio.edge.rtac.model.Holding;
import org.folio.edge.rtac.model.Holdings;
import org.folio.edge.rtac.model.Instances;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.SneakyThrows;

public class RtacMockOkapi extends MockOkapi {

  private static final Logger logger = Logger.getLogger(RtacMockOkapi.class);

  public static final String titleId_notFound = "0c8e8ac5-6bcc-461e-a8d3-4b55a96addc9";
  public static final String titleId_Error = "69640328-788e-43fc-9c3c-af39e243f3b8";
  public static final String titleId_InvalidResponse = "0c8e8ac5-6bcc-461e-a8d3-4b55a96add10";

  public RtacMockOkapi(int port, List<String> knownTenants) {
    super(port, knownTenants);
  }

  @Override
  public Router defineRoutes() {
    Router router = super.defineRoutes();
    router.route(HttpMethod.POST, "/rtac/batch").handler(this::modRtacHandler);
    return router;
  }

  @SneakyThrows
  public void modRtacHandler(RoutingContext ctx) {
    final var payload = ctx.getBody().toJsonObject();
    final var instanceIds = payload.getJsonArray("instanceIds");
    String token = ctx.request().getHeader(X_OKAPI_TOKEN);

    if (token == null || !token.equals(MOCK_TOKEN)) {
      ctx.response()
        .setStatusCode(403)
        .putHeader(HttpHeaders.CONTENT_TYPE, TEXT_PLAIN)
        .end("Access requires permission: rtac.holdings.item.get");
    } else {
      var holdings = new ArrayList<Holdings>();
      var errors = new ArrayList<Error>();
      for (Object instanceId : instanceIds) {
        if (instanceId.equals(titleId_notFound)) {
          continue;
        }
        if (instanceId.equals(titleId_Error)) {
          errors.add(new Error().withCode("404")
            .withMessage(String.format("Instance %s can not be retrieved", instanceId)));
        } else{
          final var h = getHoldings(instanceId.toString());
          holdings.add(h);
        }
      }

      var instances = new Instances();
      instances.setHoldings(holdings);
      if (!errors.isEmpty()) {
        instances.setErrors(errors);
      }

      if (instanceIds.size() == 1){
        if (instanceIds.getString(0).equals(titleId_notFound)){
          ctx.response()
            .setStatusCode(404)
            .putHeader(HttpHeaders.CONTENT_TYPE, TEXT_PLAIN)
            .end("rtac not found");
        } else if (instanceIds.getString(0).equals(titleId_InvalidResponse)){
          ctx.response()
            .setStatusCode(200)
            .end("invalid response");
        } else{
          ctx.response()
            .setStatusCode(200)
            .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
            .end(instances.toJson());
        }
      } else {
        ctx.response()
          .setStatusCode(200)
          .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
          .end(instances.toJson());
      }
    }
  }

  public static Holdings getHoldings(String titleId) {
    Holding h = Holding.builder()
      .id("99712686103569")
      .callNumber("PS3552.E796 D44x 1975")
      .location("LC General Collection Millersville University Library")
      .status("Item in place")
      .tempLocation("")
      .dueDate("")
      .volume("v.5:no.2-6")
      .build();

    final var holdings = new Holdings();
    holdings.setInstanceId(titleId);
    holdings.setHoldings(List.of(h));

    return holdings;
  }
}
