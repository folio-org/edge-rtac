package org.folio.edge.rtac.security;

import java.util.Properties;

public abstract class SecureStore {

  protected Properties properties;

  protected SecureStore(Properties properties) {
    this.properties = properties;
  }

  public abstract String get(String tenant, String username);

}
