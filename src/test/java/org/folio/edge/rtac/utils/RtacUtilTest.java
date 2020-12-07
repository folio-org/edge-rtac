package org.folio.edge.rtac.utils;

import static io.vertx.core.http.HttpHeaders.ACCEPT;
import static org.folio.edge.core.Constants.TEXT_PLAIN;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.util.Arrays;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(VertxUnitRunner.class)
public class RtacUtilTest {

  @Mock
  private static HttpServerRequest rtacRequest;
  @Mock
  private static MultiMap rtacHeaders;

  private static List<String> supportedAcceptHeaders = Arrays
      .asList(RtacMimeTypeEnum.getAllTypesAsString());
  private static List<String> unsupportedAcceptHeaders = Arrays.asList(TEXT_PLAIN);

  @BeforeClass
  public static void beforeClassRtacUtils() {
    // Creates mock of RTAC request nd headers
    rtacRequest = mock(HttpServerRequest.class);
    rtacHeaders = mock(MultiMap.class);

    // Define mock object's behavior
    when(rtacRequest.headers()).thenReturn(rtacHeaders);
  }

  @Test
  public void shouldSupportedTypePassedWhenClientSpecifiedSupportedType() {
    when(rtacHeaders.contains(ACCEPT)).thenReturn(true);
    when(rtacHeaders.getAll(ACCEPT)).thenReturn(supportedAcceptHeaders);
    assertTrue(RtacUtils.checkSupportedAcceptHeaders(rtacRequest));
  }

  @Test
  public void shouldCheckingForSupportedTypePassedWhenClientDoesNotStateAPreference() {
    when(rtacHeaders.contains(ACCEPT)).thenReturn(false);
    assertTrue(RtacUtils.checkSupportedAcceptHeaders(rtacRequest));
  }

  @Test
  public void shouldCheckingForSupportedTypeFailedWhenClientSpecifiedUnsupportedType() {
    when(rtacHeaders.contains(ACCEPT)).thenReturn(true);
    when(rtacHeaders.getAll(ACCEPT)).thenReturn(unsupportedAcceptHeaders);
    assertFalse(RtacUtils.checkSupportedAcceptHeaders(rtacRequest));
  }

  @Test
  public void shouldCheckingForXMLTypePassedWhenClientDoesNotStateAPreference() {
    when(rtacHeaders.contains(ACCEPT)).thenReturn(false);
    assertTrue(RtacUtils.isXmlRequest(rtacRequest));
  }

  @Test
  public void shouldCheckingForXMLTypePassedWhenClientSpecifiedSupportedType() {
    when(rtacHeaders.contains(ACCEPT)).thenReturn(true);
    when(rtacHeaders.getAll(ACCEPT)).thenReturn(supportedAcceptHeaders);
    assertTrue(RtacUtils.isXmlRequest(rtacRequest));
  }

  @Test
  public void shouldCheckingForXMLTypeFailedWhenClientSpecifiedUnsupportedType() {
    when(rtacHeaders.contains(ACCEPT)).thenReturn(true);
    when(rtacHeaders.getAll(ACCEPT)).thenReturn(unsupportedAcceptHeaders);
    assertFalse(RtacUtils.isXmlRequest(rtacRequest));
  }
}