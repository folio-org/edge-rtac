package org.folio.edge.rtac.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.edge.rtac.client.RtacCacheClient;
import org.folio.rtac.domain.dto.RtacRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class RtacCacheService {

  private final RtacCacheClient rtacCacheClient;

  public String searchRtacCacheHoldings(String instanceId, String query, Boolean available,
    Integer limit, Integer offset) {
    return rtacCacheClient.searchRtacCacheHoldings(instanceId, query, available, limit, offset);
  }

  public String getRtacCacheHoldingsById(String instanceId, Integer limit, Integer offset) {
    return rtacCacheClient.rtacCacheById(instanceId, limit, offset);
  }

  public String getRtacCacheBatchHoldings(RtacRequest rtacRequest) {
    return rtacCacheClient.rtacCacheBatch(rtacRequest);
  }
}
