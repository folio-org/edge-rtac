package org.folio.edge.rtac;

import static org.folio.edge.core.Constants.APPLICATION_XML;
import static org.folio.edge.core.Constants.SYS_LOG_LEVEL;
import static org.folio.edge.core.Constants.SYS_OKAPI_URL;
import static org.folio.edge.core.Constants.SYS_PORT;
import static org.folio.edge.core.Constants.SYS_REQUEST_TIMEOUT_MS;
import static org.folio.edge.core.Constants.SYS_SECURE_STORE_PROP_FILE;
import static org.folio.edge.core.Constants.TEXT_PLAIN;
import static org.folio.edge.core.utils.test.MockOkapi.X_DURATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;
import org.folio.edge.core.utils.ApiKeyUtils;
import org.folio.edge.core.utils.test.TestUtils;
import org.folio.edge.rtac.model.Holdings;
import org.folio.edge.rtac.utils.RtacMockOkapi;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

  private static final Logger logger = Logger.getLogger(MainVerticleTest.class);

  private static final String titleId = "0c8e8ac5-6bcc-461e-a8d3-4b55a96addc8";
  private static final String apiKey = "Z1luMHVGdjNMZl9kaWt1X2Rpa3U=";
  private static final String badApiKey = "ZnMwMDAwMDAwMA==0000";
  private static final String unknownTenantApiKey = "Z1luMHVGdjNMZl9ib2d1c19ib2d1cw==";

  private static final long requestTimeoutMs = 3000L;

  private static Vertx vertx;
  private static RtacMockOkapi mockOkapi;

  @BeforeClass
  public static void setUpOnce(TestContext context) throws Exception {
    int okapiPort = TestUtils.getPort();
    int serverPort = TestUtils.getPort();

    List<String> knownTenants = new ArrayList<>();
    knownTenants.add(ApiKeyUtils.parseApiKey(apiKey).tenantId);

    mockOkapi = spy(new RtacMockOkapi(okapiPort, knownTenants));
    mockOkapi.start(context);

    vertx = Vertx.vertx();

    System.setProperty(SYS_PORT, String.valueOf(serverPort));
    System.setProperty(SYS_OKAPI_URL, "http://localhost:" + okapiPort);
    System.setProperty(SYS_SECURE_STORE_PROP_FILE, "src/main/resources/ephemeral.properties");
    System.setProperty(SYS_LOG_LEVEL, "DEBUG");
    System.setProperty(SYS_REQUEST_TIMEOUT_MS, String.valueOf(requestTimeoutMs));

    final DeploymentOptions opt = new DeploymentOptions();
    vertx.deployVerticle(MainVerticle.class.getName(), opt, context.asyncAssertSuccess());

    RestAssured.baseURI = "http://localhost:" + serverPort;
    RestAssured.port = serverPort;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @AfterClass
  public static void tearDownOnce(TestContext context) {
    logger.info("Shutting down server");
    final Async async = context.async();
    vertx.close(res -> {
      if (res.failed()) {
        logger.error("Failed to shut down edge-rtac server", res.cause());
        fail(res.cause().getMessage());
      } else {
        logger.info("Successfully shut down edge-rtac server");
      }

      logger.info("Shutting down mock Okapi");
      mockOkapi.close();
      async.complete();
    });
  }

  @Test
  public void testAdminHealth(TestContext context) {
    logger.info("=== Test the health check endpoint ===");

    final Response resp = RestAssured
      .get("/admin/health")
      .then()
      .contentType(TEXT_PLAIN)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, TEXT_PLAIN)
      .extract()
      .response();

    assertEquals("\"OK\"", resp.body().asString());
  }

  @Test
  public void testRtacUnknownApiKey(TestContext context) throws Exception {
    logger.info("=== Test request with unknown apiKey (tenant) ===");

    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s&apikey=%s", titleId, unknownTenantApiKey))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    String expected = new Holdings().toXml();
    String actual = resp.body().asString();
    assertEquals(expected, actual);
  }

  @Test
  public void testRtacBadApiKey(TestContext context) throws Exception {
    logger.info("=== Test request with malformed apiKey ===");

    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s&apikey=%s", titleId, badApiKey))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    String expected = new Holdings().toXml();
    String actual = resp.body().asString();

    assertEquals(expected, actual);
  }

  @Test
  public void testRtacTitleFound(TestContext context) throws Exception {
    logger.info("=== Test request where title is found ===");

    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s&apikey=%s", titleId, apiKey))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .extract()
      .response();

    Holdings expected = Holdings.fromJson(RtacMockOkapi.getHoldingsJson(titleId));
    Holdings actual = Holdings.fromXml(resp.body().asString());
    assertEquals(expected, actual);
  }

  @Test
  public void testRtacTitleNotFound(TestContext context) throws Exception {
    logger.info("=== Test request where title isn't found ===");

    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s&apikey=%s",
          RtacMockOkapi.titleId_notFound, apiKey))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    Holdings expected = new Holdings();
    Holdings actual = Holdings.fromXml(resp.body().asString());
    assertEquals(expected, actual);
  }

  @Test
  public void testRtacNoApiKey(TestContext context) throws Exception {
    logger.info("=== Test request with no apiKey ===");

    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s", RtacMockOkapi.titleId_notFound))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    Holdings expected = new Holdings();
    Holdings actual = Holdings.fromXml(resp.body().asString());
    assertEquals(expected, actual);
  }

  @Test
  public void testRtacNoId(TestContext context) throws Exception {
    logger.info("=== Test request with no mms_id ===");

    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?apikey=%s", apiKey))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    Holdings expected = new Holdings();
    Holdings actual = Holdings.fromXml(resp.body().asString());
    assertEquals(expected, actual);
  }

  @Test
  public void testRtacNoQueryArgs(TestContext context) throws Exception {
    logger.info("=== Test request with no query args ===");

    final Response resp = RestAssured
      .get("/prod/rtac/folioRTAC")
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    Holdings expected = new Holdings();
    Holdings actual = Holdings.fromXml(resp.body().asString());
    assertEquals(expected, actual);
  }

  @Test
  public void testRtacEmptyQueryArgs(TestContext context) throws Exception {
    logger.info("=== Test request with empty query args ===");

    final Response resp = RestAssured
      .get("/prod/rtac/folioRTAC?mms_id=&apikey=")
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    Holdings expected = new Holdings();
    Holdings actual = Holdings.fromXml(resp.body().asString());
    assertEquals(expected, actual);
  }

  @Test
  public void testCachedToken(TestContext context) throws Exception {
    logger.info("=== Test the tokens are cached and reused ===");

    Holdings expected = Holdings.fromJson(RtacMockOkapi.getHoldingsJson(titleId));
    int iters = 5;

    for (int i = 0; i < iters; i++) {
      final Response resp = RestAssured
        .get(String.format("/prod/rtac/folioRTAC?mms_id=%s&apikey=%s", titleId, apiKey))
        .then()
        .contentType(APPLICATION_XML)
        .statusCode(200)
        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
        .extract()
        .response();

      assertEquals(expected, Holdings.fromXml(resp.body().asString()));
    }

    verify(mockOkapi).loginHandler(any());
    verify(mockOkapi, atLeast(iters)).modRtacHandler(any());
  }

  @Test
  public void testRequestTimeout(TestContext context) throws Exception {
    logger.info("=== Test request timeout ===");

    final Response resp = RestAssured
      .with()
      .header(X_DURATION, requestTimeoutMs * 2)
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s&apikey=%s", titleId, apiKey))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .extract()
      .response();

    Holdings expected = new Holdings();
    Holdings actual = Holdings.fromXml(resp.body().asString());
    assertEquals(expected, actual);
  }
}
