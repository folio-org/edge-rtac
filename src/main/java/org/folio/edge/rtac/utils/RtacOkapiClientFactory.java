package org.folio.edge.rtac.utils;

import org.folio.edge.core.utils.OkapiClientFactory;

import io.vertx.core.Vertx;

public class RtacOkapiClientFactory extends OkapiClientFactory {

  public RtacOkapiClientFactory(Vertx vertx, String okapiURL, long reqTimeoutMs) {
    super(vertx, okapiURL, reqTimeoutMs);
  }

  public RtacOkapiClient getRtacOkapiClient(String tenant) {
    return new RtacOkapiClient(vertx, okapiURL, tenant, reqTimeoutMs);
  }
}
