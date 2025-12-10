package org.folio.edge.rtac.controller;

import static org.folio.edge.rtac.TestConstants.RTAC_BATCH_URL;
import static org.folio.edge.rtac.TestConstants.RTAC_INSTANCE_URL;
import static org.folio.edge.rtac.RtacResultMatchers.matchBatchHoldingsJson;
import static org.folio.edge.rtac.RtacResultMatchers.matchBatchHoldingsXml;
import static org.folio.edge.rtac.RtacResultMatchers.matchEmptyHoldingsJson;
import static org.folio.edge.rtac.RtacResultMatchers.matchEmptyHoldingsXml;
import static org.folio.edge.rtac.RtacResultMatchers.matchInstanceHoldingsJson;
import static org.folio.edge.rtac.RtacResultMatchers.matchInstanceHoldingsWithFullPeriodicalsJson;
import static org.folio.edge.rtac.RtacResultMatchers.matchInstanceHoldingsWithFullPeriodicalsXml;
import static org.folio.edge.rtac.RtacResultMatchers.matchInstanceHoldingsXml;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_XML;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import org.folio.edge.rtac.BaseIntegrationTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

class RtacControllerIT extends BaseIntegrationTests {

  private static final String INSTANCE_UUID_1 = "83034b0a-bf71-4495-b642-2e998f721e5d";
  private static final String INSTANCE_UUID_2 = "67227d94-7333-4d22-98a0-718b49d36595";
  private static final String LANG_PARAM_NAME = "lang";
  private static final String LANG_PARAM_INVALID_VALUE = "111111";
  private static final String EMPTY_INSTANCE_ID = "00000000-0000-0000-0000-000000000000";

  @Autowired
  private MockMvc mockMvc;

  @ParameterizedTest
  @MethodSource("instanceHoldingsProvider")
  void getInstanceRtac_shouldReturnHoldings(MediaType acceptType, ResultMatcher matcher) throws Exception {
    doGet(mockMvc, RTAC_INSTANCE_URL, acceptType)
        .andExpect(matcher);
  }

  @ParameterizedTest
  @MethodSource("instanceHoldingsWithFullPeriodicalsProvider")
  void getInstanceRtac_shouldReturnHoldingsWithFullPeriodicals(MediaType acceptType, ResultMatcher matcher)
      throws Exception {
    doGetWithParam(mockMvc, RTAC_INSTANCE_URL, "fullPeriodicals", "true", acceptType)
        .andExpect(matcher);
  }

  @ParameterizedTest
  @MethodSource("batchHoldingsProvider")
  void getBatchRtac_shouldReturnBatchHoldings(MediaType acceptType, ResultMatcher matcher) throws Exception {
    String instanceIds = INSTANCE_UUID_1 + "," + INSTANCE_UUID_2;
    doGetWithParam(mockMvc, RTAC_BATCH_URL, "instanceIds", instanceIds, acceptType)
        .andExpect(matcher);
  }

  @ParameterizedTest
  @ValueSource(strings = {RTAC_BATCH_URL, RTAC_INSTANCE_URL})
  void getRtac_shouldReturnBadRequest_whenLangParamInvalid(String endpoint) throws Exception {
    doGetWithParam(mockMvc, endpoint, LANG_PARAM_NAME, LANG_PARAM_INVALID_VALUE)
        .andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @MethodSource("emptyHoldingsProvider")
  void getInstanceRtac_shouldReturnEmptyHoldings_whenNoHoldingsFound(MediaType acceptType, ResultMatcher matcher)
      throws Exception {
    String emptyInstanceUrl = "/rtac/" + EMPTY_INSTANCE_ID;
    doGet(mockMvc, emptyInstanceUrl, acceptType)
        .andExpect(matcher);
  }

  private static Stream<Arguments> instanceHoldingsProvider() {
    return Stream.of(
        Arguments.of(APPLICATION_JSON, matchInstanceHoldingsJson()),
        Arguments.of(APPLICATION_XML, matchInstanceHoldingsXml()),
        Arguments.of(TEXT_XML, matchInstanceHoldingsXml())
    );
  }

  private static Stream<Arguments> instanceHoldingsWithFullPeriodicalsProvider() {
    return Stream.of(
        Arguments.of(APPLICATION_JSON, matchInstanceHoldingsWithFullPeriodicalsJson()),
        Arguments.of(APPLICATION_XML, matchInstanceHoldingsWithFullPeriodicalsXml()),
        Arguments.of(TEXT_XML, matchInstanceHoldingsWithFullPeriodicalsXml())
    );
  }

  private static Stream<Arguments> batchHoldingsProvider() {
    return Stream.of(
        Arguments.of(APPLICATION_JSON, matchBatchHoldingsJson()),
        Arguments.of(APPLICATION_XML, matchBatchHoldingsXml()),
        Arguments.of(TEXT_XML, matchBatchHoldingsXml())
    );
  }

  private static Stream<Arguments> emptyHoldingsProvider() {
    return Stream.of(
        Arguments.of(APPLICATION_JSON, matchEmptyHoldingsJson()),
        Arguments.of(APPLICATION_XML, matchEmptyHoldingsXml()),
        Arguments.of(TEXT_XML, matchEmptyHoldingsXml())
    );
  }
}