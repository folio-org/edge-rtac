package org.folio.edge.rtac;

import static org.apache.http.HttpStatus.SC_NOT_ACCEPTABLE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.folio.edge.core.Constants.APPLICATION_JSON;
import static org.folio.edge.core.Constants.APPLICATION_XML;
import static org.folio.edge.core.Constants.TEXT_PLAIN;
import static org.folio.edge.rtac.utils.RtacUtils.composeMimeTypes;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import java.io.IOException;
import org.apache.http.HttpHeaders;
import org.folio.edge.rtac.model.Instances;
import org.folio.edge.rtac.utils.RtacMimeTypeEnum;
import org.junit.Test;

public class RtacHandlerTest extends MainVerticleTest {

  @Test
  public void shouldRespondWithXMLWhenClientDoesNotStateAPreference() throws IOException {
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
  public void shouldRespondWithXMLWhenClientSpecifiedXMLType() throws IOException {
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
  public void shouldRespondWithJSONWhenClientSpecifiedJSONType() throws IOException {
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
  public void shouldRespondWithXMLWhenClientSpecifiedBothOfXMLAndJSONTypes() throws IOException {
    final var queryString = prepareQueryFor(apiKey, titleId);
    final var expectedRecords = prepareRecordsFor(titleId);

    // Make get request with XML type content
    final var resp = RestAssured
        .given()
        .accept(
            composeMimeTypes(RtacMimeTypeEnum.APPLICATION_XML, RtacMimeTypeEnum.APPLICATION_JSON))
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
  public void shouldRespondWithSupportedTypeWhenClientSpecifiedBothSupportedAndUnsupportedTypes() throws IOException {
    final var queryString = prepareQueryFor(apiKey, titleId);
    final var expectedRecordsJson = prepareRecordsFor(titleId).toJson();

    // Make get request with XML type content
    final var resp = RestAssured
        .given()
        .accept(composeMimeTypes(RtacMimeTypeEnum.APPLICATION_JSON.toString(), TEXT_PLAIN))
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
  public void shouldRespondUnsupportedMediaTypeWhenClientStateWrongType()
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
}