package org.folio.edge.rtac;

import static org.folio.edge.rtac.Constants.APPLICATION_XML;
import static org.folio.edge.rtac.Constants.SYS_LOG_LEVEL;
import static org.folio.edge.rtac.Constants.SYS_OKAPI_URL;
import static org.folio.edge.rtac.Constants.SYS_PORT;
import static org.folio.edge.rtac.Constants.SYS_REQUEST_TIMEOUT_MS;
import static org.folio.edge.rtac.Constants.SYS_SECURE_STORE_PROP_FILE;
import static org.folio.edge.rtac.Constants.TEXT_PLAIN;
import static org.folio.edge.rtac.utils.MockOkapi.X_DURATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;
import org.folio.edge.rtac.model.Holdings;
import org.folio.edge.rtac.security.EphemeralStore;
import org.folio.edge.rtac.security.SecureStore;
import org.folio.edge.rtac.utils.MockOkapi;
import org.folio.edge.rtac.utils.TestUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

  private static final Logger logger = Logger.getLogger(MainVerticleTest.class);

  private static final String titleId = "0c8e8ac5-6bcc-461e-a8d3-4b55a96addc8";
  private static final String apiKey = "ZGlrdQ==";
  private static final String badApiKey = "ZnMwMDAwMDAwMA==0000";
  private static final String unknownTenantApiKey = "Ym9ndXN0ZW5hbnQ=";

  private static final long requestTimeoutMs = 3000L;

  private static Vertx vertx;
  private static MockOkapi mockOkapi;

  // ** setUp/tearDown **//

  @BeforeClass
  public static void setUpOnce(TestContext context) throws Exception {
    int okapiPort = TestUtils.getPort();
    int serverPort = TestUtils.getPort();

    List<String> knownTenants = new ArrayList<>();
    knownTenants.add(new String(Base64.getUrlDecoder().decode(apiKey)));

    mockOkapi = spy(new MockOkapi(okapiPort, knownTenants));
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
    vertx.close(res -> {
      if (res.failed()) {
        logger.error("Failed to shut down edge-rtac server", res.cause());
        fail(res.cause().getMessage());
      } else {
        logger.info("Successfully shut down edge-rtac server");
      }

      logger.info("Shutting down mock Okapi");
      mockOkapi.close();
    });
  }

  // ** Test cases **//

  @Test
  public void testAdminHealth(TestContext context) {
    logger.info("=== Test the health check endpoint ===");
    final Async async = context.async();

    final Response resp = RestAssured
      .get("/admin/health")
      .then()
      .contentType(TEXT_PLAIN)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, TEXT_PLAIN)
      .extract()
      .response();

    assertEquals("\"OK\"", resp.body().asString());

    async.complete();
  }

  @Test
  public void testRtacUnknownApiKey(TestContext context) throws Exception {
    logger.info("=== Test request with unknown apiKey (tenant) ===");
    final Async async = context.async();

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

    async.complete();
  }

  @Test
  public void testRtacBadApiKey(TestContext context) throws Exception {
    logger.info("=== Test request with malformed apiKey ===");
    final Async async = context.async();

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

    logger.info(actual);
    assertEquals(expected, actual);

    async.complete();
  }

  @Test
  public void testRtacTitleFound(TestContext context) throws Exception {
    logger.info("=== Test request where title is found ===");
    final Async async = context.async();

    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s&apikey=%s", titleId, apiKey))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .extract()
      .response();

    Holdings expected = Holdings.fromJson(MockOkapi.getHoldingsJson(titleId));
    Holdings actual = Holdings.fromXml(resp.body().asString());
    assertEquals(expected, actual);

    async.complete();
  }

  @Test
  public void testRtacTitleNotFound(TestContext context) throws Exception {
    logger.info("=== Test request where title isn't found ===");
    final Async async = context.async();

    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s&apikey=%s",
          MockOkapi.titleId_notFound, apiKey))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    Holdings expected = new Holdings();
    Holdings actual = Holdings.fromXml(resp.body().asString());
    assertEquals(expected, actual);

    async.complete();
  }

  @Test
  public void testRtacNoApiKey(TestContext context) throws Exception {
    logger.info("=== Test request with no apiKey ===");
    final Async async = context.async();

    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s", MockOkapi.titleId_notFound))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    Holdings expected = new Holdings();
    Holdings actual = Holdings.fromXml(resp.body().asString());
    assertEquals(expected, actual);

    async.complete();
  }

  @Test
  public void testRtacNoId(TestContext context) throws Exception {
    logger.info("=== Test request with no mms_id ===");
    final Async async = context.async();

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

    async.complete();
  }

  @Test
  public void testRtacNoQueryArgs(TestContext context) throws Exception {
    logger.info("=== Test request with no query args ===");
    final Async async = context.async();

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

    async.complete();
  }

  @Test
  public void testRtacEmptyQueryArgs(TestContext context) throws Exception {
    logger.info("=== Test request with empty query args ===");
    final Async async = context.async();

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

    async.complete();
  }

  @Test
  public void testCachedToken(TestContext context) throws Exception {
    logger.info("=== Test the tokens are cached and reused ===");
    final Async async = context.async();

    Holdings expected = Holdings.fromJson(MockOkapi.getHoldingsJson(titleId));
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

    async.complete();
  }

  @Test
  public void testInitializeSecureStoreHttp(TestContext context) throws Exception {
    logger.info("=== Test initialize secure store from Http link ===");
    final Async testAsync = context.async();

    MainVerticle verticle = new MainVerticle();

    // Setup Simple server to host properties file.
    int port = TestUtils.getPort();

    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.route(HttpMethod.GET, "/path/to/ephemeral.properties").handler(ctx -> {

      String body = "";
      try {
        InputStream in = new FileInputStream("src/main/resources/ephemeral.properties");
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
          String line;
          while ((line = reader.readLine()) != null) {
            resultStringBuilder.append(line).append("\n");
          }
        }
        body = resultStringBuilder.toString();
      } catch (Exception e) {

      }
      ctx.response()
        .setStatusCode(200)
        .end(body);
    });

    final Async async = context.async();
    server.requestHandler(router::accept).listen(port, result -> {
      if (result.failed()) {
        logger.warn(result.cause());
      }
      context.assertTrue(result.succeeded());
      async.complete();
    });

    SecureStore fromHttp = verticle.initializeSecureStore("http://localhost:" + port + "/path/to/ephemeral.properties");
    assertNotNull(fromHttp);
    assertEquals(EphemeralStore.class, fromHttp.getClass());

    server.close(res -> {
      if (res.failed()) {
        logger.error("Failed to shut down properties file server", res.cause());
        fail(res.cause().getMessage());
      } else {
        logger.info("Successfully shut down properties file server");
      }
    });

    testAsync.complete();
  }

  @Test
  public void testInitializeSecureStoreLocal(TestContext context) throws Exception {
    logger.info("=== Test initialize secure store from local file ===");
    final Async testAsync = context.async();

    MainVerticle verticle = new MainVerticle();

    SecureStore fromLocal = verticle.initializeSecureStore("src/main/resources/ephemeral.properties");
    assertNotNull(fromLocal);
    assertEquals(EphemeralStore.class, fromLocal.getClass());

    testAsync.complete();
  }

  @Test
  public void testRequestTimeout(TestContext context) throws Exception {
    logger.info("=== Test request timeout ===");
    final Async testAsync = context.async();

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

    testAsync.complete();
  }
}
