package org.folio.edge.rtac;

import static org.folio.edge.rtac.Constants.APPLICATION_XML;
import static org.folio.edge.rtac.Constants.SYS_LOG_LEVEL;
import static org.folio.edge.rtac.Constants.SYS_OKAPI_URL;
import static org.folio.edge.rtac.Constants.SYS_PORT;
import static org.folio.edge.rtac.Constants.SYS_SECURE_STORE_PROP_FILE;
import static org.folio.edge.rtac.Constants.TEXT_PLAIN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;
import org.folio.edge.rtac.model.Holdings;
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
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

  private static final Logger logger = Logger.getLogger(MainVerticleTest.class);

  private static final String titleId = "0c8e8ac5-6bcc-461e-a8d3-4b55a96addc8";
  private static final String apiKey = "ZGlrdQ==";
  private static final String badApiKey = "ZnMwMDAwMDAwMA==0000";
  private static final String unknownTenantApiKey = "Ym9ndXN0ZW5hbnQ=";

  private static Vertx vertx;
  private static MockOkapi mockOkapi;

  // ** setUp/tearDown **//

  @BeforeClass
  public static void setUpOnce(TestContext context) throws Exception {
    int okapiPort = TestUtils.getPort();
    int serverPort = TestUtils.getPort();

    List<String> knownTenants = new ArrayList<>();
    knownTenants.add(new String(Base64.getUrlDecoder().decode(apiKey)));

    mockOkapi = spy(new MockOkapi(okapiPort, knownTenants));// spy(new
    // MockOkapi(okapiPort));
    mockOkapi.start(context);

    vertx = Vertx.vertx();

    System.setProperty(SYS_PORT, String.valueOf(serverPort));
    System.setProperty(SYS_OKAPI_URL, "http://localhost:" + okapiPort);
    System.setProperty(SYS_SECURE_STORE_PROP_FILE, "src/main/resources/ephemeral.properties");
    System.setProperty(SYS_LOG_LEVEL, "DEBUG");

    final DeploymentOptions opt = new DeploymentOptions();
    vertx.deployVerticle(MainVerticle.class.getName(), opt, context.asyncAssertSuccess());

    RestAssured.baseURI = "http://localhost:" + serverPort;
    RestAssured.port = serverPort;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @AfterClass
  public static void tearDownOnce(TestContext context) {
    logger.info("Shutting down server");
    vertx.close(context.asyncAssertSuccess());

    logger.info("Shutting down mock Okapi");
    mockOkapi.close();
  }

  // ** Test cases **//

  @Test
  public void testAdminHealth(TestContext context) {
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
    final Async async = context.async();

    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s&apikey=%s", titleId, apiKey))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    Holdings expected = Holdings.fromJson(MockOkapi.getHoldingsJson(titleId));
    Holdings actual = Holdings.fromXml(resp.body().asString());
    assertEquals(expected, actual);

    async.complete();
  }

  @Test
  public void testRtacTitleNotFound(TestContext context) throws Exception {
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
}
