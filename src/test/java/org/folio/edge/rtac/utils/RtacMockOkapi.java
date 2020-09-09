package org.folio.edge.rtac.utils;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.folio.edge.core.Constants.APPLICATION_JSON;
import static org.folio.edge.core.Constants.TEXT_PLAIN;
import static org.folio.edge.core.Constants.X_OKAPI_TOKEN;

import java.util.List;

import org.apache.log4j.Logger;
import org.folio.edge.core.utils.test.MockOkapi;
import org.folio.edge.rtac.model.Holding;
import org.folio.edge.rtac.model.Holdings;
import org.folio.edge.rtac.model.Instances;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class RtacMockOkapi extends MockOkapi {

  private static final Logger logger = Logger.getLogger(RtacMockOkapi.class);

  public static final String titleId_notFound = "0c8e8ac5-6bcc-461e-a8d3-4b55a96addc9";

  public RtacMockOkapi(int port, List<String> knownTenants) {
    super(port, knownTenants);
  }

  @Override
  public Router defineRoutes() {
    Router router = super.defineRoutes();
    router.route(HttpMethod.GET, "/rtac/:titleid").handler(this::modRtacHandler);
    return router;
  }

  public void modRtacHandler(RoutingContext ctx) {
    String titleId = ctx.request().getParam("titleid");
    String token = ctx.request().getHeader(X_OKAPI_TOKEN);

    if (token == null || !token.equals(MOCK_TOKEN)) {
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

  public static String getHoldingsJson(String titleId) {
    Holding h = Holding.builder()
      .id("99712686103569")
      .callNumber("PS3552.E796 D44x 1975")
      .location("LC General Collection Millersville University Library")
      .status("Item in place")
      .tempLocation("")
      .dueDate("")
      .volume("v.5:no.2-6")
      .build();

    Instances holdings = new Instances();
    holdings.getHoldings().add(new Holdings(asList(h)));

    String ret = null;
    try {
      ret = holdings.toJson();
    } catch (JsonProcessingException e) {
      logger.warn("Failed to generate holdings JSON", e);
    }
    return ret;
  }
}
