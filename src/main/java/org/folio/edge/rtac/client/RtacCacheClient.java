package org.folio.edge.rtac.client;

import org.folio.edge.rtac.config.RtacClientConfig;
import org.folio.rtac.domain.dto.RtacRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "rtac-cache", configuration = RtacClientConfig.class)
public interface RtacCacheClient {

  @GetMapping(value = "/rtac-cache/search/{instanceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  String searchRtacCacheHoldings(@PathVariable("instanceId") String instanceId,
                                 @RequestParam String query,
                                 @RequestParam(required = false) Boolean available,
                                 @RequestParam Integer limit,
                                 @RequestParam Integer offset);

  @GetMapping(value = "/rtac-cache/{instanceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  String rtacCacheById(@PathVariable String instanceId, @RequestParam Integer limit, @RequestParam Integer offset);

  @PostMapping(value = "/rtac-cache/batch", consumes = MediaType.APPLICATION_JSON_VALUE)
  String rtacCacheBatch(@RequestBody RtacRequest rtacRequest);

}
