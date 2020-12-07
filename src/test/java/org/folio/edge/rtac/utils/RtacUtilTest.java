package org.folio.edge.rtac.utils;

import static io.vertx.core.http.HttpHeaders.ACCEPT;
import static org.folio.edge.core.Constants.TEXT_PLAIN;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.unit.TestContext;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.rtac.MainVerticleTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;


public class RtacUtilTest extends MainVerticleTest {
  private static final Logger logger = LogManager.getLogger(RtacUtilTest.class);

  @Mock
  private static HttpServerRequest rtacRequest;
  @Mock
  private static MultiMap rtacHeaders;

  private static List<String> validAcceptHeader = Arrays
      .asList(RtacMimeTypeEnum.getAllTypesAsString());
  private static List<String> wrongAcceptHeader = Arrays.asList(TEXT_PLAIN);

  @BeforeClass
  public static void beforeClassRtacUtils(TestContext context) {
    // Creates mock of RTAC request nd headers
    rtacRequest = mock(HttpServerRequest.class);
    rtacHeaders = mock(MultiMap.class);

    // Define mock object's behavior
    when(rtacRequest.headers()).thenReturn(rtacHeaders);
  }

  @Test
  public void checkSupportedAcceptHeadersSuccessful(TestContext context) {
    when(rtacHeaders.contains(ACCEPT)).thenReturn(true);
    when(rtacHeaders.getAll(ACCEPT)).thenReturn(validAcceptHeader);
    assertTrue(RtacUtils.checkSupportedAcceptHeaders(rtacRequest));
  }

  @Test
  public void checkSupportedAcceptHeadersNoAcceptHeaderSuccessful(TestContext context) {
    when(rtacHeaders.contains(ACCEPT)).thenReturn(false);
    assertTrue(RtacUtils.checkSupportedAcceptHeaders(rtacRequest));
  }

  @Test
  public void checkSupportedAcceptHeadersFailed(TestContext context) {
    when(rtacHeaders.contains(ACCEPT)).thenReturn(true);
    when(rtacHeaders.getAll(ACCEPT)).thenReturn(wrongAcceptHeader);
    assertFalse(RtacUtils.checkSupportedAcceptHeaders(rtacRequest));
  }

  @Test
  public void checkIsXmlRequestSuccessful(TestContext context) {
    when(rtacHeaders.contains(ACCEPT)).thenReturn(true);
    when(rtacHeaders.getAll(ACCEPT)).thenReturn(wrongAcceptHeader);
  }

  @Test
  public void checkIsXmlRequestNoAcceptHeaderSuccessful(TestContext context) {
    when(rtacHeaders.contains(ACCEPT)).thenReturn(false);
    assertTrue(RtacUtils.isXmlRequest(rtacRequest));
  }

  @Test
  public void checkIsXmlRequestFailed(TestContext context) {
    when(rtacHeaders.contains(ACCEPT)).thenReturn(true);
    when(rtacHeaders.getAll(ACCEPT)).thenReturn(wrongAcceptHeader);
    assertFalse(RtacUtils.isXmlRequest(rtacRequest));
  }
}