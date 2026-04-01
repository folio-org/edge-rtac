package org.folio.edge.rtac.client;

import org.folio.rtac.domain.dto.RtacRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import tools.jackson.databind.JsonNode;

@HttpExchange(contentType = "application/json")
public interface RtacCacheClient {

  @GetExchange("rtac-cache/search/{instanceId}")
  JsonNode searchRtacCacheHoldings(@PathVariable("instanceId") String instanceId,
                                 @RequestParam String query,
                                 @RequestParam(required = false) Boolean available,
                                 @RequestParam Integer limit,
                                 @RequestParam Integer offset,
                                 @RequestParam(required = false) String sort);

  @GetExchange("rtac-cache/{instanceId}")
  JsonNode rtacCacheById(@PathVariable String instanceId,
      @RequestParam Integer limit, @RequestParam Integer offset,
      @RequestParam(required = false) String sort);

  @PostExchange("rtac-cache/batch")
  JsonNode rtacCacheBatch(@RequestBody RtacRequest rtacRequest);

}
