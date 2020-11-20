package org.folio.edge.rtac;

import static org.apache.http.HttpStatus.SC_NOT_ACCEPTABLE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.folio.edge.core.Constants.APPLICATION_JSON;
import static org.folio.edge.core.Constants.APPLICATION_XML;
import static org.folio.edge.core.Constants.TEXT_PLAIN;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.rtac.model.Instances;
import org.folio.edge.rtac.utils.RtacMockOkapi;
import org.junit.Test;

public class RtacHandlerTest extends MainVerticleTest {

  private static final Logger logger = LogManager.getLogger(RtacHandlerTest.class);

  private String prepareQueryFor(String apiKey, String... instanceIds) {
    if (ArrayUtils.isEmpty(instanceIds)) {
      return String.format("/rtac?apikey=%s", apiKey);
    } else {
      String instancesAsString = Arrays.asList(instanceIds).stream()
          .collect(Collectors.joining(","));
      return String.format("/rtac?apikey=%s&instanceIds=%s", apiKey, instancesAsString);
    }
  }

  private Instances prepareRecordsFor(String... instanceIds) {
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
  public void checkNoAcceptHeadersSpecifiedSuccessfulResponse() throws IOException {
    logger.info("=== Test when No Accept headers specified returns response in XML by default ===");

    final var queryString = prepareQueryFor(apiKey, titleId);
    final var expectedRecords = prepareRecordsFor(titleId);

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
    final var xmlResponsePayload = Instances.fromXml(responsePayload);

    // Check valid Xml payload returned
    assertEquals(expectedRecords, xmlResponsePayload);
  }

  @Test
  public void checkXmlAcceptHeaderSpecifiedSuccessfulResponse() throws IOException {
    logger.info("=== Test when Xml Accept header specified returns response in Xml  ===");

    final var queryString = prepareQueryFor(apiKey, titleId);
    final var expectedRecords = prepareRecordsFor(titleId);

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

    final var responsePayload = resp.body().asString();
    final var xmlResponsePayload = Instances.fromXml(responsePayload);

    // Check valid Xml payload returned
    assertEquals(expectedRecords, xmlResponsePayload);
  }

  @Test
  public void checkJsonAcceptHeaderSpecifiedSuccessfulResponse() throws IOException {
    logger.info("=== Test Json Accept header specified returns response in Json ===");

    final var queryString = prepareQueryFor(apiKey, titleId);
    final var expectedRecordsJson = prepareRecordsFor(titleId).toJson();

    // Make get request with JSON type content
    final var resp = RestAssured
        .given()
        .accept(APPLICATION_JSON)
        .get(queryString)
        .then()
        .contentType(APPLICATION_JSON)
        .statusCode(SC_OK)
        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
        .extract()
        .response();

    final var responsePayload = resp.body().asString();

    // Check valid Json payload returned
    assertEquals(expectedRecordsJson, responsePayload);
  }

  @Test
  public void checkUnsupportedAcceptHeadersFailedResponse() throws JsonProcessingException {
    logger.info("=== Test wrong Accept headers passed returns HTTP 406 Not Acceptable ===");

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

}