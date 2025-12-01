package org.folio.edge.rtac.service;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.edge.rtac.client.RtacClient;
import org.folio.edge.rtac.exception.RtacSoftErrorException;
import org.folio.edge.rtac.utils.ObjectMapperUtils;
import org.folio.rtac.domain.dto.BatchHoldingsResponse;
import org.folio.rtac.domain.dto.InstanceHoldings;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class RtacService {

  private static final String PARAM_INSTANCE_IDS = "instanceIds";
  private static final String PARAM_FULL_PERIODICALS = "fullPeriodicals";

  private final RtacClient rtacClient;
  private final ObjectMapperUtils objectMapperUtils;

  public InstanceHoldings getInstanceRtac(String instanceId, Boolean fullPeriodicals) {
    final String modRtacResponse = fetchRtacData(List.of(instanceId), fullPeriodicals);
    return transformSingleResponse(modRtacResponse, instanceId);
  }

  public BatchHoldingsResponse getBatchRtac(String instanceIds, Boolean fullPeriodicals) {
    final List<String> ids = parseInstanceIds(instanceIds);
    final String modRtacResponse = fetchRtacData(ids, fullPeriodicals);
    return transformBatchResponse(modRtacResponse);
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

  private String fetchRtacData(List<String> ids, Boolean fullPeriodicals) {
    final String requestBody = buildRequestBody(ids, fullPeriodicals);
    final String response = rtacClient.rtac(requestBody);
    log.debug("mod-rtac response: {}", response);
    return response;
  }

  private String buildRequestBody(List<String> ids, Boolean fullPeriodicals) {
    final Map<String, Object> body = new HashMap<>();
    body.put(PARAM_INSTANCE_IDS, ids);
    if (fullPeriodicals != null) {
      body.put(PARAM_FULL_PERIODICALS, fullPeriodicals);
    }
    return objectMapperUtils.writeAsString(body);
  }

  private InstanceHoldings transformSingleResponse(String modRtacResponse, String instanceId) {
    var batchHoldingsResponse = objectMapperUtils.readValue(modRtacResponse, BatchHoldingsResponse.class);

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