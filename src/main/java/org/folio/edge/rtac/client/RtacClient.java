package org.folio.edge.rtac.client;

import org.folio.edge.rtac.config.RtacClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "rtac", configuration = RtacClientConfig.class)
public interface RtacClient {

  @PostMapping(value = "/rtac-batch", consumes = MediaType.APPLICATION_JSON_VALUE)
  String rtac(@RequestBody String requestBody);
}
