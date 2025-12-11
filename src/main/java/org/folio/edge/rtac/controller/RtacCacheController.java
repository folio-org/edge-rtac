package org.folio.edge.rtac.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.edge.rtac.service.RtacCacheService;
import org.folio.rtac.domain.dto.RtacRequest;
import org.folio.rtac.rest.resource.RtacCacheApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
public class RtacCacheController implements RtacCacheApi {

  private final RtacCacheService rtacCacheService;

  @Override
  public ResponseEntity<String> searchRtacCacheHoldings(String instanceId,
                                                        String query,
                                                        Boolean available,
                                                        Integer limit,
                                                        Integer offset,
                                                        String authorization,
                                                        String apiKey) {
    var holdings = rtacCacheService.searchRtacCacheHoldings(instanceId, query, available, limit, offset);
    return ResponseEntity.ok(holdings);
  }

  @Override
  public ResponseEntity<String> getRtacCacheHoldingsById(String instanceId,
                                                         Integer limit,
                                                         Integer offset,
                                                         String authorization,
                                                         String apiKey) {
    var holdings = rtacCacheService.getRtacCacheHoldingsById(instanceId, limit, offset);
    return ResponseEntity.ok(holdings);
  }


  @Override
  public ResponseEntity<String> postRtacCacheBatchHoldings(RtacRequest rtacRequest) {
    var holdingsBatch = rtacCacheService.getRtacCacheBatchHoldings(rtacRequest);
    return ResponseEntity.ok(holdingsBatch);
  }
}
