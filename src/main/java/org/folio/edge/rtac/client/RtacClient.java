package org.folio.edge.rtac.client;

import org.folio.rtac.domain.dto.BatchHoldingsResponse;
import org.folio.rtac.domain.dto.RtacBatchRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(contentType = "application/json")
public interface RtacClient {

  @PostExchange("rtac-batch")
  BatchHoldingsResponse rtac(@RequestBody RtacBatchRequest requestBody);

}
