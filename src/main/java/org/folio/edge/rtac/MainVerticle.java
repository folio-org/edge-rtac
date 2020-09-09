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


    router.route(HttpMethod.GET, "/prod/rtac/folioRTAC")
      .handler(ctx -> rtacHandler.handle(ctx, false));

    router.route(HttpMethod.GET, "/rtac")
      .handler(ctx -> rtacHandler.handle(ctx, true));
    return router;
  }
}
