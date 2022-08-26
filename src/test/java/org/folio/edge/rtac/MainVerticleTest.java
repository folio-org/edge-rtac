package org.folio.edge.rtac;

import static org.folio.edge.core.Constants.APPLICATION_XML;
import static org.folio.edge.core.Constants.SYS_LOG_LEVEL;
import static org.folio.edge.core.Constants.SYS_OKAPI_URL;
import static org.folio.edge.core.Constants.SYS_PORT;
import static org.folio.edge.core.Constants.SYS_REQUEST_TIMEOUT_MS;
import static org.folio.edge.core.Constants.SYS_SECURE_STORE_PROP_FILE;
import static org.folio.edge.core.Constants.TEXT_PLAIN;
import static org.folio.edge.core.utils.test.MockOkapi.X_DURATION;
import static org.folio.edge.core.Constants.APPLICATION_JSON;

import static org.apache.http.HttpStatus.SC_NOT_ACCEPTABLE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.folio.edge.rtac.utils.RtacUtils.composeMimeTypes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.core.utils.ApiKeyUtils;
import org.folio.edge.core.utils.test.TestUtils;
import org.folio.edge.rtac.model.Holding;
import org.folio.edge.rtac.model.Holdings;
import org.folio.edge.rtac.model.Instances;
import org.folio.edge.rtac.utils.RtacMockOkapi;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import lombok.SneakyThrows;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

  private static final Logger logger = LogManager.getLogger(MainVerticleTest.class);

  protected static final String titleId = "0c8e8ac5-6bcc-461e-a8d3-4b55a96addc8";
  protected static final String apiKey = ApiKeyUtils.generateApiKey(10, "diku", "diku");
  private static final String badApiKey = apiKey + "0000";
  private static final String unknownTenantApiKey = ApiKeyUtils.generateApiKey(10, "bogus", "diku");

  private static final int requestTimeoutMs = 3000;

  private static Vertx vertx;
  protected static RtacMockOkapi mockOkapi;

  @BeforeClass
  public static void setUpOnce(TestContext context) throws Exception {
    int okapiPort = TestUtils.getPort();
    int serverPort = TestUtils.getPort();

    List<String> knownTenants = new ArrayList<>();
    knownTenants.add(ApiKeyUtils.parseApiKey(apiKey).tenantId);

    mockOkapi = spy(new RtacMockOkapi(okapiPort, knownTenants));
    mockOkapi.start().onComplete(context.asyncAssertSuccess());

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
    mockOkapi.close().onComplete(context.asyncAssertSuccess());
  }

  protected String prepareQueryFor(String apiKey, String... instanceIds) {
    if (ArrayUtils.isEmpty(instanceIds)) {
      return String.format("/rtac?apikey=%s", apiKey);
    } else {
      String instancesAsString = Arrays.asList(instanceIds).stream()
          .collect(Collectors.joining(","));
      return String.format("/rtac?apikey=%s&instanceIds=%s", apiKey, instancesAsString);
    }
  }

  protected Instances prepareRecordsFor(String... instanceIds) {
    if (ArrayUtils.isEmpty(instanceIds)) {
      throw new IllegalArgumentException("No instances specified");

    } else {
      final var holdings = Arrays.asList(instanceIds).stream().map(RtacMockOkapi::getHoldings)
          .collect(Collectors.toList());
      final var instanceHoldingRecords = new Instances();
      instanceHoldingRecords.setHoldings(holdings);
      return instanceHoldingRecords;
    }
  }

  @Test
  public void testAdminHealthShouldSucceed(TestContext context) {
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

  // unsuccessful login attempts result in a 200 OK status with empty elements
  // in the message body

  @Test
  public void failsWhenUnknownAPIKeyProvided(TestContext context) throws Exception {
    logger.info("=== Test request with unknown apiKey (tenant) ===");

    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s&apikey=%s", titleId, unknownTenantApiKey))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    expectEmptyResponseOnFailure(resp);
  }

  @Test
  public void failsWhenBadApiKeyProvided(TestContext context) throws Exception {
    logger.info("=== Test request with malformed apiKey ===");

    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s&apikey=%s", titleId, badApiKey))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    expectEmptyResponseOnFailure(resp);
  }

  @Test
  public void testRtacTitleFound(TestContext context) throws Exception {
    logger.info("=== Test request where title is found ===");

    final Response resp = RestAssured
      .given()
      .accept(APPLICATION_JSON)
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s&apikey=%s", titleId, apiKey))
      .then()
      .contentType(APPLICATION_JSON)
      .statusCode(200)
      .extract()
      .response();

    JsonObject result =  new JsonObject(resp.body().asString());

    assertEquals("0c8e8ac5-6bcc-461e-a8d3-4b55a96addc8", result.getString("instanceId"));

    JsonObject holdingRecord = result.getJsonArray("holdings").getJsonObject(0);

    assertEquals("99712686103569", holdingRecord.getString("id"));
    assertEquals("PS3552.E796 D44x 1975", holdingRecord.getString("callNumber"));
    assertEquals("Item in place", holdingRecord.getString("status"));
    assertEquals("v.5:no.2-6", holdingRecord.getString("volume"));
  }

  // Unsuccessful searches result in a 200 OK status with an empty element in the
  // response body

  @Test
  public void emptyResponseWhenTitleNotFound(TestContext context) throws Exception {
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

    expectEmptyResponseOnFailure(resp);
  }

  @Test
  public void testRtacBatching(TestContext context) throws Exception {
    logger.info("=== Test rtac batching ===");

    final var titleId2 = UUID.randomUUID().toString();
    final var queryString = String.format("/rtac?apikey=%s&instanceIds=%s,%s",
      apiKey, titleId, titleId2);

    final Response resp = RestAssured
      .given()
      .accept(APPLICATION_JSON)
      .get(queryString)
      .then()
      .contentType(APPLICATION_JSON)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
      .extract()
      .response();

    JsonArray result =  new JsonObject(resp.body().asString())
      .getJsonArray("holdings");

    JsonObject first = result.getJsonObject(0);
    JsonObject second = result.getJsonObject(1);

    assertEquals(titleId, first.getString("instanceId"));
    assertEquals(titleId2, second.getString("instanceId"));

    JsonObject firstHoldings = first.getJsonArray("holdings").getJsonObject(0);
    JsonObject secondHoldings = second.getJsonArray("holdings").getJsonObject(0);

    assertEquals("99712686103569", firstHoldings.getString("id"));
    assertEquals("99712686103569", secondHoldings.getString("id"));
    assertEquals("PS3552.E796 D44x 1975", firstHoldings.getString("callNumber"));
    assertEquals("PS3552.E796 D44x 1975", secondHoldings.getString("callNumber"));
    assertEquals("Item in place", firstHoldings.getString("status"));
    assertEquals("Item in place", secondHoldings.getString("status"));
    assertEquals("v.5:no.2-6", firstHoldings.getString("volume"));
    assertEquals("v.5:no.2-6", secondHoldings.getString("volume"));
  }

  @Test
  public void shouldProvideNotFoundErrorsWhenTitleNotFound(TestContext context) throws Exception {

    final var queryString = String.format("/rtac?apikey=%s&instanceIds=%s,%s",
      apiKey, titleId, RtacMockOkapi.titleId_Error);

    final Response resp = RestAssured
      .given()
      .accept(APPLICATION_JSON)
      .get(queryString)
      .then()
      .contentType(APPLICATION_JSON)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
      .extract()
      .response();

    String expectedError = "Instance 69640328-788e-43fc-9c3c-af39e243f3b8 can not be retrieved";

    JsonObject result =  new JsonObject(resp.body().asString());
    JsonObject errors = result.getJsonArray("errors").getJsonObject(0);

    assertEquals(expectedError, errors.getString("message"));
    assertEquals("404", errors.getString("code"));
  }

  @Test
  public void emptyResponseWhenNoRtacApiKeyProvided(TestContext context) throws Exception {
    logger.info("=== Test request with no apiKey ===");

    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s", RtacMockOkapi.titleId_notFound))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    expectEmptyResponseOnFailure(resp);
  }

  @Test
  public void emptyResponseWhenNoIdProvided(TestContext context) throws Exception {
    logger.info("=== Test request with no mms_id ===");

    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?apikey=%s", apiKey))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    expectEmptyResponseOnFailure(resp);
  }

  @Test
  public void emptyResponseWhenNoQueryArgsProvided(TestContext context) throws Exception {
    logger.info("=== Test request with no query args ===");

    final Response resp = RestAssured
      .get("/prod/rtac/folioRTAC")
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    expectEmptyResponseOnFailure(resp);
  }

  @Test
  public void emptyResponseWhenEmptyQueryArgsProvided(TestContext context) throws Exception {
    logger.info("=== Test request with empty query args ===");

    final Response resp = RestAssured
      .get("/prod/rtac/folioRTAC?mms_id=&apikey=")
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .extract()
      .response();

    expectEmptyResponseOnFailure(resp);
  }

  @Test
  public void cachedTokensShouldBeReused(TestContext context) throws Exception {
    logger.info("=== Test the tokens are cached and reused ===");

    int iters = 5;

    for (int i = 0; i < iters; i++) {
      final Response resp = RestAssured
        .given()
        .accept(APPLICATION_JSON)
        .get(String.format("/prod/rtac/folioRTAC?mms_id=%s&apikey=%s", titleId, apiKey))
        .then()
        .contentType(APPLICATION_JSON)
        .statusCode(200)
        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .extract()
        .response();

      JsonObject result = new JsonObject(resp.body().asString());

      assertEquals(titleId, result.getString("instanceId"));

      JsonObject holdings = result.getJsonArray("holdings").getJsonObject(0);

      assertEquals("PS3552.E796 D44x 1975", holdings.getString("callNumber"));
      assertEquals("99712686103569", holdings.getString("id"));
      assertEquals("Item in place", holdings.getString("status"));
      assertEquals("v.5:no.2-6", holdings.getString("volume"));
    }

    verify(mockOkapi).loginHandler(any());
    verify(mockOkapi, atLeast(iters)).modRtacHandler(any());
  }

  @Test
  public void emptyResponseWhenRequestTimeout(TestContext context) throws Exception {
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

    expectEmptyResponseOnFailure(resp);
  }

  @Test
  @SneakyThrows
  public void emptyResponseWhenRtacResponseIsInvalid() {
    final Response resp = RestAssured
      .get(String.format("/prod/rtac/folioRTAC?mms_id=%s&apikey=%s", RtacMockOkapi.titleId_InvalidResponse, apiKey))
      .then()
      .contentType(APPLICATION_XML)
      .statusCode(200)
      .extract()
      .response();

    expectEmptyResponseOnFailure(resp);
  }

  @Test
  @SneakyThrows
  public void responseShouldIncludeInstanceIdWhenTitleNotFoundAndReturnsJson() {
    final Response resp = RestAssured
      .given()
      .accept(APPLICATION_JSON)
      .get(String.format("/rtac/%s?apikey=%s", RtacMockOkapi.titleId_notFound, apiKey))
      .then()
      .contentType(APPLICATION_JSON)
      .statusCode(SC_OK)
      .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
      .extract()
      .response();

    final JsonObject actual = new JsonObject(resp.body().asString());

    assertEquals("0c8e8ac5-6bcc-461e-a8d3-4b55a96addc9", actual.getString("instanceId"));
  }

  @Test
  public void shouldRespondWithXMLWhenClientDoesNotStateAPreference() throws IOException {
    final var queryString = prepareQueryFor(apiKey, titleId);

    // Make get request with XML type content
    final var resp = RestAssured
        .get(queryString)
        .then()
        .contentType(APPLICATION_XML)
        .statusCode(SC_OK)
        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
        .extract()
        .response();

    final var responsePayload = resp.body().asString();
    Holdings holdings = Instances.fromXml(responsePayload).getHoldings().get(0);

    assertEquals("0c8e8ac5-6bcc-461e-a8d3-4b55a96addc8", holdings.getInstanceId());

    Holding holding = holdings.getHoldings().get(0);

    assertEquals("99712686103569", holding.id);
    assertEquals("PS3552.E796 D44x 1975", holding.callNumber);
    assertEquals("Item in place", holding.status);
    assertEquals("v.5:no.2-6", holding.volume);
  }

  @Test
  public void shouldRespondWithXMLWhenClientAcceptsOnlyXML() throws IOException {
    final var queryString = prepareQueryFor(apiKey, titleId);

    // Make get request with XML type content
    final var resp = RestAssured
        .given()
        .accept(APPLICATION_XML)
        .get(queryString)
        .then()
        .contentType(APPLICATION_XML)
        .statusCode(SC_OK)
        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
        .extract()
        .response();

    final String responsePayload = resp.body().asString();

    // Check valid Xml payload returned
    assertTrue(isValidXml(responsePayload));
  }

  @Test
  public void shouldRespondWithXMLWhenClientAcceptsBothXMLAndJSON() throws IOException {
    final var queryString = prepareQueryFor(apiKey, titleId);

    // Make get request with XML type content
    final var resp = RestAssured
        .given()
        .accept(
            composeMimeTypes(APPLICATION_XML, APPLICATION_JSON))
        .get(queryString)
        .then()
        .contentType(APPLICATION_XML)
        .statusCode(SC_OK)
        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
        .extract()
        .response();

    final var responsePayload = resp.body().asString();

    // Check valid Xml payload returned
    assertTrue(isValidXml(responsePayload));
  }

  @Test
  public void shouldRespondWithSupportedTypeWhenClientAcceptsBothSupportedAndUnsupportedTypes()
      throws IOException {
    final var queryString = prepareQueryFor(apiKey, titleId);

    // Make get request with XML type content
    final var resp = RestAssured
        .given()
        .accept(composeMimeTypes(APPLICATION_JSON, TEXT_PLAIN))
        .get(queryString)
        .then()
        .contentType(APPLICATION_JSON)
        .statusCode(SC_OK)
        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .extract()
        .response();

    final var responsePayload = resp.body().asString();

    // Check valid Json payload returned
    assertTrue(isValidJson(responsePayload));
  }

  @Test
  public void shouldRespondWithUnsupportedMediaTypeWhenClientOnlyAcceptsUnsupportedType()
      throws JsonProcessingException {
    final var queryString = prepareQueryFor(apiKey, titleId);

    // Make get request with XML type content
    final var resp = RestAssured
        .given()
        .accept(TEXT_PLAIN)
        .get(queryString)
        .then()
        .statusCode(SC_NOT_ACCEPTABLE)
        .extract()
        .response();

    // Check not supported content 406 status code returned
    assertEquals(SC_NOT_ACCEPTABLE, resp.getStatusCode());
  }

  private boolean isValidJson(String json) {
    try {
      new JsonObject(json);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private boolean isValidXml(String xml) {
    try {
      Instances.fromXml(xml);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @SneakyThrows
  private void expectEmptyResponseOnFailure(Response response) {
    // Failures are mapped to empty successful responses due to compatibility with legacy systems
    final var instances = Instances.fromXml(response.body().asString());

    assertEquals(0, instances.getHoldings().size());
  }

}
