package org.folio.edge.rtac.utils;

import static org.folio.edge.rtac.Constants.X_OKAPI_TOKEN;
import static org.folio.edge.rtac.utils.MockOkapi.X_DURATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class OkapiClientTest {

  private static final Logger logger = Logger.getLogger(OkapiClientTest.class);

  private final String mockToken = MockOkapi.mockToken;
  private final String titleId = "0c8e8ac5-6bcc-461e-a8d3-4b55a96addc8";
  private static final String tenant = "diku";
  private static final long reqTimeout = 3000L;

  private Vertx vertx;
  private OkapiClient client;
  private MockOkapi mockOkapi;

  // ** setUp/tearDown **//

  @Before
  public void setUp(TestContext context) throws Exception {
    int okapiPort = TestUtils.getPort();

    List<String> knownTenants = new ArrayList<>();
    knownTenants.add(tenant);

    mockOkapi = new MockOkapi(okapiPort, knownTenants);
    mockOkapi.start(context);
    vertx = Vertx.vertx();

    client = new OkapiClientFactory(vertx, "http://localhost:" + okapiPort, reqTimeout).getOkapiClient(tenant);
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
    // client.getToken("admin", "password").thenRun(() -> {
    client.login("admin", "password").thenRun(() -> {
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
    // client.getToken("admin", "password").thenAccept(v -> {
    client.login("admin", "password").thenAccept(v -> {
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

  @Test
  public void testLoginNoPassword(TestContext context) {
    Async async = context.async();
    CompletableFuture<String> future = client.login("admin", null);
    future.thenAcceptAsync(token -> {
      assertNull(token);
    });
    async.complete();
  }

  @Test
  public void testLoginTimeout(TestContext context) throws Exception {
    Async async = context.async();
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.set(X_DURATION, String.valueOf(reqTimeout * 2));
    CompletableFuture<String> future = client.login("admin", null, headers);
    try {
      future.get();
      fail("Expected a TimeoutException to be thrown");
    } catch (Exception e) {
      assertEquals(TimeoutException.class, e.getCause().getClass());
    }
    async.complete();
  }

  @Test
  public void testRtacTimeout(TestContext context) throws Exception {
    Async async = context.async();
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.set(X_DURATION, String.valueOf(reqTimeout * 2));
    CompletableFuture<String> future = client.rtac(titleId, headers);
    try {
      future.get();
      fail("Expected a TimeoutException to be thrown");
    } catch (Exception e) {
      assertEquals(TimeoutException.class, e.getCause().getClass());
    }
    async.complete();
  }
}
