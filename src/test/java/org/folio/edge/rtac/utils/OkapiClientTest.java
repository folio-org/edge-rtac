package org.folio.edge.rtac.utils;

import static org.folio.edge.rtac.Constants.X_OKAPI_TOKEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class OkapiClientTest {

  private static final Logger logger = Logger.getLogger(OkapiClientTest.class);

  private final String mockToken = MockOkapi.mockToken;
  private final String titleId = "0c8e8ac5-6bcc-461e-a8d3-4b55a96addc8";

  private Vertx vertx;
  private OkapiClient client;
  private MockOkapi mockOkapi;

  // ** setUp/tearDown **//

  @Before
  public void setUp(TestContext context) throws Exception {
    int okapiPort = TestUtils.getPort();

    mockOkapi = new MockOkapi(okapiPort);
    mockOkapi.start(context);
    vertx = Vertx.vertx();

    client = new OkapiClientFactory(vertx, "http://localhost:" + okapiPort).getOkapiClient("testtenant");
  }

  @After
  public void tearDown(TestContext context) {
    logger.info("Closing Vertx");
    vertx.close(context.asyncAssertSuccess());
  }

  // ** Test cases **//

  @Test
  public void testLogin(TestContext context) {
    Async async = context.async();
    client.getToken("admin", "password").thenRun(() -> {
      logger.info(client.defaultHeaders.get(X_OKAPI_TOKEN));

      // Ensure that the client's default headers now contain the
      // x-okapi-token for use in subsequent okapi calls
      assertEquals(mockToken, client.defaultHeaders.get(X_OKAPI_TOKEN));
      async.complete();
    });
  }

  @Test
  public void testRtac(TestContext context) {
    Async async = context.async();
    client.getToken("admin", "password").thenAccept(v -> {
      // Redundant - also checked in testLogin(...), but can't hurt
      assertEquals(mockToken, client.defaultHeaders.get(X_OKAPI_TOKEN));

      client.rtac(titleId).thenAccept(body -> {
        logger.info("mod-rtac response body: " + body);
        assertEquals(body, MockOkapi.getHoldingsJson(titleId));
        async.complete();
      });
    });
  }

  @Test
  public void testHealthy(TestContext context) {
    Async async = context.async();
    client.healthy().thenAccept(isHealthy -> {
      assertTrue(isHealthy);
      async.complete();
    });
  }
}
