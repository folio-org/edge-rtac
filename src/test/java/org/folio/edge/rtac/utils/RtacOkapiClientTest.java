package org.folio.edge.rtac.utils;

import static java.util.stream.Collectors.toList;
import static org.folio.edge.core.utils.test.MockOkapi.MOCK_TOKEN;
import static org.folio.edge.core.utils.test.MockOkapi.X_DURATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.core.utils.Mappers;
import org.folio.edge.core.utils.test.TestUtils;
import org.folio.edge.rtac.model.Instances;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import lombok.SneakyThrows;

@RunWith(VertxUnitRunner.class)
public class RtacOkapiClientTest {

  private static final Logger logger = LogManager.getLogger(RtacOkapiClientTest.class);

  private final String titleId = "0c8e8ac5-6bcc-461e-a8d3-4b55a96addc8";
  private static final String tenant = "diku";
  private static final long reqTimeout = 3000L;

  private RtacOkapiClient client;
  private RtacMockOkapi mockOkapi;

  @Before
  public void setUp(TestContext context) throws Exception {
    int okapiPort = TestUtils.getPort();

    List<String> knownTenants = new ArrayList<>();
    knownTenants.add(tenant);

    mockOkapi = new RtacMockOkapi(okapiPort, knownTenants);
    mockOkapi.start(context);

    client = new RtacOkapiClientFactory(Vertx.vertx(), "http://localhost:" + okapiPort, reqTimeout)
      .getRtacOkapiClient(tenant);
  }

  @After
  public void tearDown(TestContext context) {
    mockOkapi.close(context);
  }

  @Test
  public void testRtac(TestContext context) {
    logger.info("=== Test successful RTAC request ===");

    Async async = context.async();
    client.login("admin", "password").thenAcceptAsync(v -> {
      assertEquals(MOCK_TOKEN, client.getToken());

      client.rtac(buildRtacRequest(titleId)).thenAcceptAsync(body -> {
        logger.info("mod-rtac response body: " + body);
        try {
          final var instances = Instances.fromJson(body);
          final var actual = instances.getHoldings().get(0);
          if (RtacMockOkapi.getHoldings(titleId).equals(actual)){
            async.complete();
          } else{
            context.fail("RTAC response body is not as expected!");
          }
        } catch (IOException e) {
          context.fail(e);
        }
      }).exceptionally(t->{
        context.fail(t);
        return null;
      });
    });
  }

  @SneakyThrows
  private String buildRtacRequest(String input) {
    Map<String, Object> rtacParams = new HashMap<>();

    List<String> ids = Arrays.stream(input
      .split(",")).filter(Objects::nonNull)
      .map(String::trim).collect(toList());

    rtacParams.put("instanceIds", ids);
    var instanceIds = Mappers.jsonMapper
      .writeValueAsString(rtacParams);
    return instanceIds;
  }

  @Test
  public void testRtacTimeout(TestContext context) throws Exception {
    logger.info("=== Test RTAC timeout ===");

    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.set(X_DURATION, String.valueOf(reqTimeout * 2));
    CompletableFuture<String> future = client.rtac(titleId, headers);
    try {
      future.get();
      fail("Expected a TimeoutException to be thrown");
    } catch (Exception e) {
      assertEquals(TimeoutException.class, e.getCause().getClass());
    }
  }
}
