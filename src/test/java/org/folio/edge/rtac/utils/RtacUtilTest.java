package org.folio.edge.rtac.utils;

import static io.vertx.core.http.HttpHeaders.ACCEPT;
import static org.folio.edge.core.Constants.APPLICATION_JSON;
import static org.folio.edge.core.Constants.APPLICATION_XML;
import static org.folio.edge.core.Constants.TEXT_PLAIN;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.RoutingContext;
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
  private static RoutingContext context;
  @Mock
  private static HttpServerRequest rtacRequest;
  private static MultiMap rtacHeaders;

  @BeforeClass
  public static void beforeClass() {
    context = mock(RoutingContext.class);
    rtacRequest = mock(HttpServerRequest.class);
    rtacHeaders = new CaseInsensitiveHeaders();

    // Define behaviour of mock object
    when(context.request()).thenReturn(rtacRequest);
    when(rtacRequest.headers()).thenReturn(rtacHeaders);
  }

  @After
  public void afterMethod() {
    rtacHeaders.clear();
  }

  @Test
  public void shouldTreatRequestAsSupportedWhenClientDoesNotStateAPreference() {
    // No headers, validation of request should pass
    assertTrue(RtacUtils.checkSupportedAcceptHeaders(context));
  }

  @Test
  public void shouldTreatRequestAsSupportedWhenClientSpecifiedWildcardAllType() {
    rtacHeaders.add(String.valueOf(ACCEPT), RtacUtils.ALL_WILDCARD);
    when(context.getAcceptableContentType()).thenReturn(APPLICATION_XML);

    assertTrue(RtacUtils.checkSupportedAcceptHeaders(context));
  }

  @Test
  public void shouldTreatRequestAsSupportedWhenClientSpecifiedXmlType() {
    rtacHeaders.add(String.valueOf(ACCEPT), APPLICATION_XML);
    when(context.getAcceptableContentType()).thenReturn(APPLICATION_XML);

    assertTrue(RtacUtils.checkSupportedAcceptHeaders(context));
  }

  @Test
  public void shouldTreatRequestAsSupportedWhenClientSpecifiedJsonType() {
    rtacHeaders.add(String.valueOf(ACCEPT), APPLICATION_JSON);
    when(context.getAcceptableContentType()).thenReturn(APPLICATION_JSON);

    assertTrue(RtacUtils.checkSupportedAcceptHeaders(context));
  }

  @Test
  public void shouldTreatRequestAsUnsupportedWhenClientSpecifiedUnsupportedType() {
    rtacHeaders.add(String.valueOf(ACCEPT), TEXT_PLAIN);
    when(context.getAcceptableContentType()).thenReturn(null);

    assertFalse(RtacUtils.checkSupportedAcceptHeaders(context));
  }

  @Test
  public void shouldTreatRequestAsAcceptingXMLWhenClientDoesNotStateAPreference() {
    // No headers, request identified as XML
    assertTrue(RtacUtils.isXmlRequest(context));
  }

  @Test
  public void shouldTreatRequestAsAcceptingXMLWhenClientSpecifiedWildcardAllType() {
    rtacHeaders.add(String.valueOf(ACCEPT), RtacUtils.ALL_WILDCARD);
    when(context.getAcceptableContentType()).thenReturn(APPLICATION_XML);

    // No headers, request identified as XML
    assertTrue(RtacUtils.isXmlRequest(context));
  }

  @Test
  public void XmlIsPreferredWhenClientAcceptsBothXmlAndJson() {
    rtacHeaders.add(String.valueOf(ACCEPT),
        (List<String>) Arrays.asList(APPLICATION_XML, APPLICATION_JSON));
    when(context.getAcceptableContentType()).thenReturn(APPLICATION_XML);

    assertTrue(RtacUtils.isXmlRequest(context));
  }

}