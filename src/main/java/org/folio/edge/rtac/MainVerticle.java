package org.folio.edge.rtac;

import org.folio.edge.core.EdgeVerticle;
import org.folio.edge.core.utils.Mappers;
import org.folio.edge.rtac.utils.RtacOkapiClientFactory;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends EdgeVerticle {

  public MainVerticle() {
    super();
  }


  static {
    Mappers.xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  @Override
  public Router defineRoutes() {
    RtacOkapiClientFactory ocf = new RtacOkapiClientFactory(vertx, okapiURL, reqTimeoutMs);
    RtacHandler rtacHandler = new RtacHandler(secureStore, ocf);

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.route(HttpMethod.GET, "/admin/health").handler(this::handleHealthCheck);

    //Deprecated API
    router.route(HttpMethod.GET, "/prod/rtac/folioRTAC")
        .produces("application/xml").produces("text/xml").produces("application/json")
        .handler(ctx -> rtacHandler.handle(ctx, false));

    router.route(HttpMethod.GET, "/rtac/:instanceId")
        .produces("application/xml").produces("text/xml").produces("application/json")
        .handler(ctx -> rtacHandler.handle(ctx, false));

    router.route(HttpMethod.GET, "/rtac")
        .produces("application/xml").produces("text/xml").produces("application/json")
        .handler(ctx -> rtacHandler.handle(ctx, true));

    // Default router for any other mime types, mainly to allow processing of the "Unsupported media type"
    router.route().handler(ctx -> rtacHandler.handle(ctx, true));

    return router;
  }
}
