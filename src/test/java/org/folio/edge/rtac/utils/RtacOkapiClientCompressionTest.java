package org.folio.edge.rtac.utils;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.core.utils.test.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class RtacOkapiClientCompressionTest {
  private static final Logger logger = LogManager.getLogger(RtacOkapiClientCompressionTest.class);

  private final Vertx vertx = Vertx.vertx();

  private final String titleId = UUID.randomUUID().toString();
  private static final String tenant = "diku";
  private static final int reqTimeout = 3000;

  private RtacOkapiClient client;

  @Before
  public void setUp(TestContext context) throws Exception {
    int okapiPort = TestUtils.getPort();

    final HttpServer server = vertx.createHttpServer(
        new HttpServerOptions().setCompressionSupported(true));

    server.requestHandler(req -> {
      req.response()
        .setStatusCode(200)
        .putHeader("content-type", "application/json")
        .end("{\"test\":\"1234\"}");
    });

    final Async async = context.async();
    server.listen(okapiPort, "localhost", ar -> {
      context.assertTrue(ar.succeeded());
      async.complete();
    });

    client = new RtacOkapiClientFactory(vertx,
        "http://localhost:" + okapiPort, reqTimeout).getRtacOkapiClient(tenant);
  }

  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  /**
   * This test will ensure that if compression is requested via the "Accept-Encoding" header and
   * the server responds with compressed data, then the edge-common Okapi client will be able to
   * decompress the data before it is available for use by the handlers. Test failure here
   * indicates that compression is not working via edge-common.
   *
   * @param context test context
   * @throws Exception unexpected failure
   */
  @Test
  public void testCompression(TestContext context) throws Exception {
    logger.info("=== Test Compression ===");

    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("Accept-Encoding", "gzip");
    Async async = context.async();
    client.rtac(titleId, headers).thenAccept(body -> {
      logger.info("mod-rtac response body: " + body);
      context.assertEquals("{\"test\":\"1234\"}", body.toString());
      async.complete();
    }).exceptionally(t -> {
      context.fail(t);
      return null;
    });
  }
}
