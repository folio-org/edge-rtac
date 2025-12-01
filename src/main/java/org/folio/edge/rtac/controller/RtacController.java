package org.folio.edge.rtac.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.edge.rtac.service.RtacService;
import org.folio.rtac.domain.dto.BatchHoldingsResponse;
import org.folio.rtac.domain.dto.InstanceHoldings;
import org.folio.rtac.rest.resource.RtacApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
public class RtacController implements RtacApi {

  private final RtacService rtacService;

  @Override
  public ResponseEntity<BatchHoldingsResponse> getBatchRtac(String instanceIds,
                                             Boolean fullPeriodicals,
                                             String lang,
                                             String authorization,
                                             String apiKey,
                                             String xOkapiTenant,
                                             String xOkapiUrl,
                                             String xOkapiToken) {
    var response = rtacService.getBatchRtac(instanceIds, fullPeriodicals);

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<InstanceHoldings> getInstanceRtac(String instanceId,
                                                Boolean fullPeriodicals,
                                                String lang,
                                                String authorization,
                                                String apiKey,
                                                String xOkapiTenant,
                                                String xOkapiUrl,
                                                String xOkapiToken) {
    var response = rtacService.getInstanceRtac(instanceId, fullPeriodicals);
    return ResponseEntity.ok(response);
  }
}
