package org.folio.edge.rtac;

import static org.folio.edge.rtac.Constants.SYS_OKAPI_URL;
import static org.folio.edge.rtac.Constants.SYS_PORT;
import static org.folio.edge.rtac.Constants.SYS_SECURE_STORE_PROP_FILE;
import static org.junit.Assert.assertEquals;

import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;
import org.folio.edge.rtac.model.Holdings;
import org.folio.edge.rtac.utils.MockOkapi;
import org.folio.edge.rtac.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

  private static final Logger logger = Logger.getLogger(MainVerticleTest.class);

  private final String titleId = "0c8e8ac5-6bcc-461e-a8d3-4b55a96addc8";
  private final String apiKey = "ZnMwMDAwMDAwMA==";

  private Vertx vertx;
  private MockOkapi mockOkapi;

  private int okapiPort = TestUtils.getPort();
  private int serverPort = TestUtils.getPort();

  // ** setUp/tearDown **//

  @Before
  public void setUp(TestContext context) throws Exception {

    mockOkapi = new MockOkapi(okapiPort);
    mockOkapi.start(context);
    vertx = Vertx.vertx();

    final JsonObject conf = new JsonObject();
    conf.put(SYS_PORT, String.valueOf(serverPort));
    conf.put(SYS_OKAPI_URL, "http://localhost:" + okapiPort);
    conf.put(SYS_SECURE_STORE_PROP_FILE, "src/main/resources/ephemeral.properties");

    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    vertx.deployVerticle(MainVerticle.class.getName(), opt, context.asyncAssertSuccess());

    RestAssured.baseURI = "http://localhost:" + serverPort;
    RestAssured.port = serverPort;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @After
  public void tearDown(TestContext context) {
    logger.info("Closing Vertx");
    vertx.close(context.asyncAssertSuccess());
  }

  // ** Test cases **//

  @Test
  public void testAdminHealth(TestContext context) {
    final Async async = context.async();

    final Response resp = RestAssured
      .get("/admin/health")
      .then()
      .contentType(ContentType.TEXT)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, "text/plain")
      .extract()
      .response();

    assertEquals("\"OK\"", resp.body().asString());

    async.complete();
  }

  @Test
  public void testRtacTitleFound(TestContext context) throws Exception {
    final Async async = context.async();

    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s&apikey=%s", titleId, apiKey))
      .then()
      .contentType(ContentType.XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, "application/xml")
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
      .contentType(ContentType.XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, "application/xml")
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
      .contentType(ContentType.XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, "application/xml")
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
      .contentType(ContentType.XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, "application/xml")
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
      .contentType(ContentType.XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, "application/xml")
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
      .contentType(ContentType.XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, "application/xml")
      .extract()
      .response();

    Holdings expected = new Holdings();
    Holdings actual = Holdings.fromXml(resp.body().asString());
    assertEquals(expected, actual);

    async.complete();
  }
}
