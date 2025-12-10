package org.folio.edge.rtac.client;

import org.folio.edge.rtac.config.RtacClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "rtac", configuration = RtacClientConfig.class)
public interface RtacClient {

  @PostMapping(value = "/rtac-batch", consumes = "application/json")
  String rtac(@RequestBody String requestBody);
}
