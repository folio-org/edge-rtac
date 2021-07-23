package org.folio.edge.rtac.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import io.vertx.core.Vertx;

public class RtacOkapiClientFactoryTest {

  private static final int reqTimeout = 5000;

  private RtacOkapiClientFactory ocf;

  @Before
  public void setUp() throws Exception {

    Vertx vertx = Vertx.vertx();
    ocf = new RtacOkapiClientFactory(vertx, "http://mocked.okapi:9130", reqTimeout);
  }

  @Test
  public void testGetOkapiClient() {
    RtacOkapiClient client = ocf.getRtacOkapiClient("tenant");
    assertNotNull(client);
    assertEquals(reqTimeout, client.reqTimeout);
  }

}
