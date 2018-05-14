package org.folio.edge.rtac;

import static org.folio.edge.rtac.Constants.*;
import static org.folio.edge.rtac.Constants.DEFAULT_SECURE_STORE_TYPE;
import static org.folio.edge.rtac.Constants.PROP_SECURE_STORE_TYPE;
import static org.folio.edge.rtac.Constants.SYS_OKAPI_URL;
import static org.folio.edge.rtac.Constants.SYS_SECURE_STORE_PROP_FILE;
import static org.folio.edge.rtac.Constants.SYS_SECURE_STORE_TYPE;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.folio.edge.rtac.security.SecureStore;
import org.folio.edge.rtac.security.SecureStoreFactory;
import org.folio.edge.rtac.utils.OkapiClientFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {

  // Private Members
  private static final Logger logger = Logger.getLogger(MainVerticle.class);
  private SecureStore secureStore;

  @Override
  public void start(Future<Void> future) {
    Router router = Router.router(vertx);
    HttpServer server = vertx.createHttpServer();

    // Get properties from context too, for unit tests purposes.
    final String portStr = System.getProperty(SYS_PORT, context.config().getString(SYS_PORT, DEFAULT_PORT));
    final int port = Integer.parseInt(portStr);
    logger.info("Using port: " + port);

    final String okapiURL = System.getProperty(SYS_OKAPI_URL, context.config().getString(SYS_OKAPI_URL));
    logger.info("Using okapiURL: " + okapiURL);

    final String secureStorePropFile = System.getProperty(SYS_SECURE_STORE_PROP_FILE,
        context.config().getString(SYS_SECURE_STORE_PROP_FILE));

    initializeSecureStore(secureStorePropFile);

    OkapiClientFactory ocf = new OkapiClientFactory(vertx, okapiURL);

    RtacHandler rtacHandler = new RtacHandler(secureStore, ocf);

    router.route().handler(BodyHandler.create());
    router.route(HttpMethod.GET, "/admin/health").handler(this::healthCheckHandler);
    router.route(HttpMethod.GET, "/prod/rtac/folioRTAC").handler(rtacHandler::rtacHandler);

    server.requestHandler(router::accept).listen(port, result -> {
      if (result.succeeded()) {
        future.complete();
      } else {
        future.fail(result.cause());
      }
    });
  }

  protected void initializeSecureStore(String secureStorePropFile) {
    Properties secureStoreProps = new Properties();

    if (secureStorePropFile != null) {
      // TODO add support for s3://bucket/file.properties
      try {
        secureStoreProps.load(new FileInputStream(secureStorePropFile));
        logger.info("Successfully loaded properties from: " + secureStorePropFile);
      } catch (Exception e) {
        logger.warn("Failed to load secure store properties.", e);
      }
    } else {
      logger.warn("No secure store properties file specified.  Using defaults");
    }

    // Order of precedence: system property, properties file, default
    String type = System.getProperty(SYS_SECURE_STORE_TYPE,
        secureStoreProps.getProperty(PROP_SECURE_STORE_TYPE, DEFAULT_SECURE_STORE_TYPE));

    secureStore = SecureStoreFactory.getSecureStore(type, secureStoreProps);
  }

  protected void healthCheckHandler(RoutingContext ctx) {
    ctx.response()
      .setStatusCode(200)
      .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
      .end("\"OK\"");
  }
}
