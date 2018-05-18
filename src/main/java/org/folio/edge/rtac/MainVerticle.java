package org.folio.edge.rtac;

import static org.folio.edge.rtac.Constants.DEFAULT_LOG_LEVEL;
import static org.folio.edge.rtac.Constants.DEFAULT_NULL_TOKEN_CACHE_TTL_MS;
import static org.folio.edge.rtac.Constants.DEFAULT_PORT;
import static org.folio.edge.rtac.Constants.DEFAULT_SECURE_STORE_TYPE;
import static org.folio.edge.rtac.Constants.DEFAULT_TOKEN_CACHE_CAPACITY;
import static org.folio.edge.rtac.Constants.DEFAULT_TOKEN_CACHE_TTL_MS;
import static org.folio.edge.rtac.Constants.PROP_SECURE_STORE_TYPE;
import static org.folio.edge.rtac.Constants.SYS_LOG_LEVEL;
import static org.folio.edge.rtac.Constants.SYS_NULL_TOKEN_CACHE_TTL_MS;
import static org.folio.edge.rtac.Constants.SYS_OKAPI_URL;
import static org.folio.edge.rtac.Constants.SYS_PORT;
import static org.folio.edge.rtac.Constants.SYS_SECURE_STORE_PROP_FILE;
import static org.folio.edge.rtac.Constants.SYS_SECURE_STORE_TYPE;
import static org.folio.edge.rtac.Constants.SYS_TOKEN_CACHE_CAPACITY;
import static org.folio.edge.rtac.Constants.SYS_TOKEN_CACHE_TTL_MS;
import static org.folio.edge.rtac.Constants.TEXT_PLAIN;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.folio.edge.rtac.cache.TokenCache;
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

  private static final Logger logger = Logger.getLogger(MainVerticle.class);

  @Override
  public void start(Future<Void> future) {
    final String logLvl = System.getProperty(SYS_LOG_LEVEL, DEFAULT_LOG_LEVEL);
    Logger.getRootLogger().setLevel(Level.toLevel(logLvl));
    logger.info("Using log level: " + logLvl);

    final String portStr = System.getProperty(SYS_PORT, DEFAULT_PORT);
    final int port = Integer.parseInt(portStr);
    logger.info("Using port: " + port);

    final String okapiURL = System.getProperty(SYS_OKAPI_URL);
    logger.info("Using okapi URL: " + okapiURL);

    final String tokenCacheTtlMs = System.getProperty(SYS_TOKEN_CACHE_TTL_MS);
    final long cacheTtlMs = tokenCacheTtlMs != null ? Long.parseLong(tokenCacheTtlMs) : DEFAULT_TOKEN_CACHE_TTL_MS;
    logger.info("Using token cache TTL (ms): " + tokenCacheTtlMs);

    final String nullTokenCacheTtlMs = System.getProperty(SYS_NULL_TOKEN_CACHE_TTL_MS);
    final long failureCacheTtlMs = nullTokenCacheTtlMs != null ? Long.parseLong(nullTokenCacheTtlMs)
        : DEFAULT_NULL_TOKEN_CACHE_TTL_MS;
    logger.info("Using token cache TTL (ms): " + failureCacheTtlMs);

    final String tokenCacheCapacity = System.getProperty(SYS_TOKEN_CACHE_CAPACITY);
    final int cacheCapacity = tokenCacheCapacity != null ? Integer.parseInt(tokenCacheCapacity)
        : DEFAULT_TOKEN_CACHE_CAPACITY;
    logger.info("Using token cache capacity: " + tokenCacheCapacity);

    // initialize the TokenCache
    TokenCache.initialize(cacheTtlMs, failureCacheTtlMs, cacheCapacity);

    final String secureStorePropFile = System.getProperty(SYS_SECURE_STORE_PROP_FILE);
    SecureStore secureStore = initializeSecureStore(secureStorePropFile);

    OkapiClientFactory ocf = new OkapiClientFactory(vertx, okapiURL);
    RtacHandler rtacHandler = new RtacHandler(secureStore, ocf);

    Router router = Router.router(vertx);
    HttpServer server = vertx.createHttpServer();
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

  protected SecureStore initializeSecureStore(String secureStorePropFile) {
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

    return SecureStoreFactory.getSecureStore(type, secureStoreProps);
  }

  protected void healthCheckHandler(RoutingContext ctx) {
    ctx.response()
      .setStatusCode(200)
      .putHeader(HttpHeaders.CONTENT_TYPE, TEXT_PLAIN)
      .end("\"OK\"");
  }
}
