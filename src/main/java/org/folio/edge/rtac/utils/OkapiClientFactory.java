package org.folio.edge.rtac.utils;

import io.vertx.core.Vertx;

public class OkapiClientFactory {

  public final String okapiURL;
  public final Vertx vertx;

  public OkapiClientFactory(Vertx vertx, String okapiURL) {
    this.vertx = vertx;
    this.okapiURL = okapiURL;
  }

  public OkapiClient getOkapiClient(String tenant) {
    return new OkapiClient(vertx, okapiURL, tenant);
  }
}
