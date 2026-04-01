package org.folio.edge.rtac.service;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.edge.rtac.client.RtacClient;
import org.folio.edge.rtac.exception.RtacSoftErrorException;
import org.folio.edge.rtac.utils.ObjectMapperUtils;
import org.folio.rtac.domain.dto.BatchHoldingsResponse;
import org.folio.rtac.domain.dto.InstanceHoldings;
import org.folio.rtac.domain.dto.RtacBatchRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class RtacService {

  private final RtacClient rtacClient;
  private final ObjectMapperUtils objectMapperUtils;

  public InstanceHoldings getInstanceRtac(String instanceId, Boolean fullPeriodicals) {
    final var batchHoldingsResponse = fetchRtacData(List.of(instanceId), fullPeriodicals);
    return transformSingleResponse(batchHoldingsResponse, instanceId);
  }

  public BatchHoldingsResponse getBatchRtac(String instanceIds, Boolean fullPeriodicals) {
    final var ids = parseInstanceIds(instanceIds);
    return fetchRtacData(ids, fullPeriodicals);
  }

  private List<String> parseInstanceIds(String instanceIds) {
    if (StringUtils.isBlank(instanceIds)) {
      return List.of();
    }
    return Arrays.stream(instanceIds.split(","))
        .map(String::trim)
        .filter(StringUtils::isNotBlank)
        .toList();
  }

  private BatchHoldingsResponse fetchRtacData(List<String> ids, Boolean fullPeriodicals) {
    final var rtacBatchRequest = buildRtacBatchRequest(ids, fullPeriodicals);
    final var response = rtacClient.rtac(rtacBatchRequest);
    log.debug("mod-rtac response: {}", response);
    return response;
  }

  private RtacBatchRequest buildRtacBatchRequest(List<String> ids, Boolean fullPeriodicals) {
    var requestBody = new RtacBatchRequest();
    requestBody.setInstanceIds(ids);
    if (fullPeriodicals != null) {
      requestBody.setFullPeriodicals(fullPeriodicals);
    }
    return requestBody;
  }

  private InstanceHoldings transformSingleResponse(BatchHoldingsResponse batchHoldingsResponse, String instanceId) {
    if (hasErrors(batchHoldingsResponse)) {
      var error = batchHoldingsResponse.getErrors().getFirst();
      throw new RtacSoftErrorException(error.getCode(), error.getMessage());
    }
    return extractOrCreateEmptyHolding(batchHoldingsResponse, instanceId);
  }

  private BatchHoldingsResponse transformBatchResponse(String modRtacResponse) {
    return objectMapperUtils.readValue(modRtacResponse, BatchHoldingsResponse.class);
  }

  private InstanceHoldings extractOrCreateEmptyHolding(BatchHoldingsResponse instances, String instanceId) {
    if (isNotEmpty(instances.getHoldings())) {
      return instances.getHoldings().getFirst();
    }

    final InstanceHoldings emptyHoldings = new InstanceHoldings();
    emptyHoldings.setInstanceId(instanceId);
    return emptyHoldings;
  }

  private boolean hasErrors(BatchHoldingsResponse instances) {
    return isNotEmpty(instances.getErrors());
  }
}
