package org.folio.edge.rtac.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.folio.edge.rtac.TestUtil;
import org.folio.edge.rtac.client.RtacClient;
import org.folio.edge.rtac.exception.RtacSoftErrorException;
import org.folio.edge.rtac.utils.ObjectMapperUtils;
import org.folio.rtac.domain.dto.BatchHoldingsResponse;
import org.folio.rtac.domain.dto.InstanceHoldings;
import org.folio.rtac.domain.dto.RtacBatchRequest;
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
    var singleHoldingResponse = TestUtil.readFileContentFromResources("__files/rtac/single-holding-response.json");
    var batchHoldingsResponse = objectMapperUtils.readValue(singleHoldingResponse, BatchHoldingsResponse.class);
    when(rtacClient.rtac(any(RtacBatchRequest.class))).thenReturn(batchHoldingsResponse);

    InstanceHoldings result = rtacService.getInstanceRtac(INSTANCE_ID, null);

    assertThat(result).isNotNull();
    assertThat(result.getInstanceId()).isEqualTo(INSTANCE_ID);
    assertThat(result.getHoldings()).isNotEmpty();
  }

  @Test
  void getInstanceRtac_shouldReturnInstanceHoldingsWithFullPeriodicals() {
    var singleHoldingResponse = TestUtil.readFileContentFromResources("__files/rtac/single-holding-response.json");
    var batchHoldingsResponse = objectMapperUtils.readValue(singleHoldingResponse, BatchHoldingsResponse.class);
    when(rtacClient.rtac(any(RtacBatchRequest.class))).thenReturn(batchHoldingsResponse);
    ArgumentCaptor<RtacBatchRequest> requestCaptor = ArgumentCaptor.forClass(RtacBatchRequest.class);

    InstanceHoldings result = rtacService.getInstanceRtac(INSTANCE_ID, true);

    verify(rtacClient).rtac(requestCaptor.capture());
    var requestBody = requestCaptor.getValue();
    assertThat(result).isNotNull();
    assertThat(requestBody.getInstanceIds()).contains(INSTANCE_ID);
    assertThat(requestBody.getFullPeriodicals()).isTrue();
  }

  @Test
  void getInstanceRtac_shouldReturnEmptyHoldings_whenResponseHasNoHoldings() {
    var emptyHoldingsResponse = TestUtil.readFileContentFromResources("__files/rtac/empty-holdings-response.json");
    var batchHoldingsResponse = objectMapperUtils.readValue(emptyHoldingsResponse, BatchHoldingsResponse.class);
    when(rtacClient.rtac(any(RtacBatchRequest.class))).thenReturn(batchHoldingsResponse);

    InstanceHoldings result = rtacService.getInstanceRtac(INSTANCE_ID, null);

    assertThat(result.getInstanceId()).isEqualTo(INSTANCE_ID);
    assertThat(result.getHoldings()).isNullOrEmpty();
  }

  @Test
  void getInstanceRtac_shouldThrowException_whenResponseHasErrors() {
    var errorResponse = TestUtil.readFileContentFromResources("__files/rtac/mod-rtac-error.json");
    var batchHoldingsResponse = objectMapperUtils.readValue(errorResponse, BatchHoldingsResponse.class);
    when(rtacClient.rtac(any(RtacBatchRequest.class))).thenReturn(batchHoldingsResponse);

    assertThatThrownBy(() -> rtacService.getInstanceRtac(INSTANCE_ID, null))
        .isInstanceOf(RtacSoftErrorException.class)
        .hasFieldOrPropertyWithValue("code", "404")
        .hasMessageContaining("Instance not found");
  }

  @Test
  void getBatchRtac_shouldReturnBatchHoldingsResponse() {
    var batchHoldingsResponseString = TestUtil.readFileContentFromResources("__files/rtac/batch-holdings-response.json");
    var batchHoldingsResponse = objectMapperUtils.readValue(batchHoldingsResponseString, BatchHoldingsResponse.class);
    when(rtacClient.rtac(any(RtacBatchRequest.class))).thenReturn(batchHoldingsResponse);

    var instanceIds = INSTANCE_ID_1 + "," + INSTANCE_ID_2;
    BatchHoldingsResponse result = rtacService.getBatchRtac(instanceIds, null);

    assertThat(result.getHoldings()).hasSize(2);
    assertThat(result.getHoldings())
        .extracting(InstanceHoldings::getInstanceId)
        .containsExactly(INSTANCE_ID_1, INSTANCE_ID_2);
  }

  @Test
  void getBatchRtac_shouldTrimAndFilterInstanceIds() {
    var batchHoldingsResponseString = TestUtil.readFileContentFromResources("__files/rtac/batch-holdings-response.json");
    var batchHoldingsResponse = objectMapperUtils.readValue(batchHoldingsResponseString, BatchHoldingsResponse.class);
    when(rtacClient.rtac(any(RtacBatchRequest.class))).thenReturn(batchHoldingsResponse);
    ArgumentCaptor<RtacBatchRequest> requestCaptor = ArgumentCaptor.forClass(RtacBatchRequest.class);
    var instanceIds = "  " + INSTANCE_ID_1 + " , " + INSTANCE_ID_2 + "  , , ";

    rtacService.getBatchRtac(instanceIds, false);

    verify(rtacClient).rtac(requestCaptor.capture());
    var requestBody = requestCaptor.getValue();
    assertThat(requestBody.getInstanceIds()).contains(INSTANCE_ID_1);
    assertThat(requestBody.getInstanceIds()).contains(INSTANCE_ID_2);
    assertThat(requestBody.getFullPeriodicals()).isFalse();
  }

  @Test
  void getBatchRtac_shouldIncludeFullPeriodicalsWhenProvided() {
    var batchHoldingsResponseString = TestUtil.readFileContentFromResources("__files/rtac/batch-holdings-response.json");
    var batchHoldingsResponse = objectMapperUtils.readValue(batchHoldingsResponseString, BatchHoldingsResponse.class);
    when(rtacClient.rtac(any(RtacBatchRequest.class))).thenReturn(batchHoldingsResponse);
    ArgumentCaptor<RtacBatchRequest> requestCaptor = ArgumentCaptor.forClass(RtacBatchRequest.class);

    rtacService.getBatchRtac(INSTANCE_ID_1, true);

    verify(rtacClient).rtac(requestCaptor.capture());
    var requestBody = requestCaptor.getValue();
    assertThat(requestBody.getFullPeriodicals()).isTrue();
  }

  @Test
  void getBatchRtac_shouldNotIncludeFullPeriodicalsWhenNull() {
    var batchHoldingsResponseString = TestUtil.readFileContentFromResources("__files/rtac/batch-holdings-response.json");
    var batchHoldingsResponse = objectMapperUtils.readValue(batchHoldingsResponseString, BatchHoldingsResponse.class);
    when(rtacClient.rtac(any(RtacBatchRequest.class))).thenReturn(batchHoldingsResponse);
    ArgumentCaptor<RtacBatchRequest> requestCaptor = ArgumentCaptor.forClass(RtacBatchRequest.class);

    rtacService.getBatchRtac(INSTANCE_ID_1, null);

    verify(rtacClient).rtac(requestCaptor.capture());
    var requestBody = requestCaptor.getValue();
    assertThat(requestBody.getFullPeriodicals()).isNull();
  }
}
