package org.folio.edge.rtac.utils;

import static io.vertx.core.http.HttpHeaders.ACCEPT;
import static org.folio.edge.core.Constants.TEXT_PLAIN;
import static org.folio.edge.rtac.utils.RtacUtils.ALL_WILDCARD;
import static org.folio.edge.rtac.utils.RtacUtils.APPLICATION_JSON;
import static org.folio.edge.rtac.utils.RtacUtils.APPLICATION_XML;
import static org.folio.edge.rtac.utils.RtacUtils.SUPPORTED_TYPES;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(VertxUnitRunner.class)
public class RtacUtilTest {

  @Mock
  private static HttpServerRequest rtacRequest;
  private static MultiMap rtacHeaders;

  private static List<String> supportedAcceptHeaders = Arrays
      .asList(SUPPORTED_TYPES);

  private static List<String> unsupportedAcceptHeaders = Arrays.asList(TEXT_PLAIN);

  private static List<String> xmlAndJsonAcceptHeaders = Arrays
      .asList(APPLICATION_XML, APPLICATION_JSON);

  private static List<String> wildcardAllAcceptHeaders = Arrays
      .asList(ALL_WILDCARD);

  @BeforeClass
  public static void beforeClass() {
    rtacHeaders = new CaseInsensitiveHeaders();
    rtacRequest = mock(HttpServerRequest.class);

    // Define behaviour of mock object
    when(rtacRequest.headers()).thenReturn(rtacHeaders);
  }

  @After
  public void afterMethod() {
    rtacHeaders.clear();
  }

  @Test
  public void shouldTreatRequestAsSupportedWhenClientDoesNotStateAPreference() {
    // No headers, validation of request should pass
    assertTrue(RtacUtils.checkSupportedAcceptHeaders(rtacRequest));
  }

  @Test
  public void shouldTreatRequestAsXMLWhenClientDoesNotStateAPreference() {
    // No headers, request identified as XML
    assertTrue(RtacUtils.isXmlRequest(rtacRequest));
  }

  @Test
  public void shouldTreatRequestAsSupportedWhenClientSpecifiedWildcardAllType() {
    rtacHeaders.add(String.valueOf(ACCEPT), wildcardAllAcceptHeaders);

    assertTrue(RtacUtils.checkSupportedAcceptHeaders(rtacRequest));
  }

  @Test
  public void shouldTreatRequestAsXMLWhenClientSpecifiedWildcardAllType() {
    rtacHeaders.add(String.valueOf(ACCEPT), wildcardAllAcceptHeaders);

    assertTrue(RtacUtils.isXmlRequest(rtacRequest));
  }

  @Test
  public void shouldTreatRequestAsSupportedWhenClientSpecifiedSupportedType() {
    rtacHeaders.add(String.valueOf(ACCEPT), supportedAcceptHeaders);

    assertTrue(RtacUtils.checkSupportedAcceptHeaders(rtacRequest));
  }

  @Test
  public void shouldTreatRequestAsUnsupportedWhenClientSpecifiedUnsupportedType() {
    rtacHeaders.add(String.valueOf(ACCEPT), unsupportedAcceptHeaders);

    assertFalse(RtacUtils.checkSupportedAcceptHeaders(rtacRequest));
  }

  @Test
  public void XmlIsPreferredWhenClientAcceptsBothXmlAndJson() {
    rtacHeaders.add(String.valueOf(ACCEPT), xmlAndJsonAcceptHeaders);

    assertTrue(RtacUtils.isXmlRequest(rtacRequest));
  }
}