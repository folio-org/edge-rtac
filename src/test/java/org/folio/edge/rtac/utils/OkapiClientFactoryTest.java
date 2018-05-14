package org.folio.edge.rtac.utils;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import io.vertx.core.Vertx;

public class OkapiClientFactoryTest {

  private OkapiClientFactory ocf;

  @Before
  public void setUp() throws Exception {

    Vertx vertx = Vertx.vertx();
    ocf = new OkapiClientFactory(vertx, "http://mocked.okapi:9130");
  }

  @Test
  public void testGetOkapiClient() {
    OkapiClient client = ocf.getOkapiClient("tenant");
    assertNotNull(client);
  }

}
