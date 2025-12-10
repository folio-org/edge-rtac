package org.folio.edge.rtac.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.folio.edge.rtac.TestUtil;
import org.folio.edge.rtac.client.RtacClient;
import org.folio.edge.rtac.exception.RtacSoftErrorException;
import org.folio.edge.rtac.utils.ObjectMapperUtils;
import org.folio.rtac.domain.dto.BatchHoldingsResponse;
import org.folio.rtac.domain.dto.InstanceHoldings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RtacServiceTest {

  private static final String INSTANCE_ID = "69640328-788e-43fc-9c3c-af39e243f3b7";
  private static final String INSTANCE_ID_1 = "83034b0a-bf71-4495-b642-2e998f721e5d";
  private static final String INSTANCE_ID_2 = "67227d94-7333-4d22-98a0-718b49d36595";

  @Mock
  private RtacClient rtacClient;

  private ObjectMapperUtils objectMapperUtils;
  private RtacService rtacService;

  @BeforeEach
  void setUp() {
    objectMapperUtils = new ObjectMapperUtils(TestUtil.OBJECT_MAPPER);
    rtacService = new RtacService(rtacClient, objectMapperUtils);
  }

  @Test
  void getInstanceRtac_shouldReturnInstanceHoldings() {
    String singleHoldingResponse = TestUtil.readFileContentFromResources("__files/rtac/single_holding_response.json");
    when(rtacClient.rtac(anyString())).thenReturn(singleHoldingResponse);

    InstanceHoldings result = rtacService.getInstanceRtac(INSTANCE_ID, null);

    assertThat(result).isNotNull();
    assertThat(result.getInstanceId()).isEqualTo(INSTANCE_ID);
    assertThat(result.getHoldings()).isNotEmpty();
  }

  @Test
  void getInstanceRtac_shouldReturnInstanceHoldingsWithFullPeriodicals() {
    String singleHoldingResponse = TestUtil.readFileContentFromResources("__files/rtac/single_holding_response.json");
    when(rtacClient.rtac(anyString())).thenReturn(singleHoldingResponse);

    ArgumentCaptor<String> requestCaptor = ArgumentCaptor.forClass(String.class);

    InstanceHoldings result = rtacService.getInstanceRtac(INSTANCE_ID, true);

    verify(rtacClient).rtac(requestCaptor.capture());
    String requestBody = requestCaptor.getValue();

    assertThat(result).isNotNull();
    assertThat(requestBody).contains("fullPeriodicals");
    assertThat(requestBody).contains("true");
  }

  @Test
  void getInstanceRtac_shouldReturnEmptyHoldings_whenResponseHasNoHoldings() {
    String emptyHoldingsResponse = TestUtil.readFileContentFromResources("__files/rtac/empty_holdings_response.json");
    when(rtacClient.rtac(anyString())).thenReturn(emptyHoldingsResponse);

    InstanceHoldings result = rtacService.getInstanceRtac(INSTANCE_ID, null);

    assertThat(result.getInstanceId()).isEqualTo(INSTANCE_ID);
    assertThat(result.getHoldings()).isNullOrEmpty();
  }

  @Test
  void getInstanceRtac_shouldThrowException_whenResponseHasErrors() {
    String errorResponse = TestUtil.readFileContentFromResources("__files/rtac/mod_rtac_error.json");
    when(rtacClient.rtac(anyString())).thenReturn(errorResponse);

    assertThatThrownBy(() -> rtacService.getInstanceRtac(INSTANCE_ID, null))
        .isInstanceOf(RtacSoftErrorException.class)
        .hasFieldOrPropertyWithValue("code", "404")
        .hasMessageContaining("Instance not found");
  }

  @Test
  void getBatchRtac_shouldReturnBatchHoldingsResponse() {
    String batchHoldingsResponse = TestUtil.readFileContentFromResources("__files/rtac/batch_holdings_response.json");
    when(rtacClient.rtac(anyString())).thenReturn(batchHoldingsResponse);

    String instanceIds = INSTANCE_ID_1 + "," + INSTANCE_ID_2;
    BatchHoldingsResponse result = rtacService.getBatchRtac(instanceIds, null);

    assertThat(result.getHoldings()).hasSize(2);
    assertThat(result.getHoldings())
        .extracting(InstanceHoldings::getInstanceId)
        .containsExactly(INSTANCE_ID_1, INSTANCE_ID_2);
  }

  @Test
  void getBatchRtac_shouldTrimAndFilterInstanceIds() {
    String batchHoldingsResponse = TestUtil.readFileContentFromResources("__files/rtac/batch_holdings_response.json");
    when(rtacClient.rtac(anyString())).thenReturn(batchHoldingsResponse);

    ArgumentCaptor<String> requestCaptor = ArgumentCaptor.forClass(String.class);

    String instanceIds = "  " + INSTANCE_ID_1 + " , " + INSTANCE_ID_2 + "  , , ";
    rtacService.getBatchRtac(instanceIds, false);

    verify(rtacClient).rtac(requestCaptor.capture());
    String requestBody = requestCaptor.getValue();

    assertThat(requestBody).contains(INSTANCE_ID_1);
    assertThat(requestBody).contains(INSTANCE_ID_2);
    assertThat(requestBody).contains("fullPeriodicals");
    assertThat(requestBody).contains("false");
  }

  @Test
  void getBatchRtac_shouldIncludeFullPeriodicalsWhenProvided() {
    String batchHoldingsResponse = TestUtil.readFileContentFromResources("__files/rtac/batch_holdings_response.json");
    when(rtacClient.rtac(anyString())).thenReturn(batchHoldingsResponse);

    ArgumentCaptor<String> requestCaptor = ArgumentCaptor.forClass(String.class);

    rtacService.getBatchRtac(INSTANCE_ID_1, true);

    verify(rtacClient).rtac(requestCaptor.capture());
    String requestBody = requestCaptor.getValue();

    assertThat(requestBody).contains("fullPeriodicals");
    assertThat(requestBody).contains("true");
  }

  @Test
  void getBatchRtac_shouldNotIncludeFullPeriodicalsWhenNull() {
    String batchHoldingsResponse = TestUtil.readFileContentFromResources("__files/rtac/batch_holdings_response.json");
    when(rtacClient.rtac(anyString())).thenReturn(batchHoldingsResponse);

    ArgumentCaptor<String> requestCaptor = ArgumentCaptor.forClass(String.class);

    rtacService.getBatchRtac(INSTANCE_ID_1, null);

    verify(rtacClient).rtac(requestCaptor.capture());
    String requestBody = requestCaptor.getValue();

    assertThat(requestBody).doesNotContain("fullPeriodicals");
  }
}