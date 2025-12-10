package org.folio.edge.rtac.config;

import static org.folio.edge.rtac.TestConstants.OKAPI_URL;
import static org.folio.edge.rtac.TestConstants.TEST_TENANT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.folio.edge.api.utils.exception.AuthorizationException;
import org.folio.edgecommonspring.client.EdgeFeignClientProperties;
import org.folio.edgecommonspring.domain.entity.ConnectionSystemParameters;
import org.folio.edgecommonspring.security.SecurityManagerService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.model.UserToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class RtacClientRequestInterceptorTest {

  private static final String API_KEY = "eyJzIjoiQlBhb2ZORm5jSzY0NzdEdWJ4RGgiLCJ0IjoidGVzdCIsInUiOiJ0ZXN0X2FkbWluIn0=";
  private static final String TOKEN_VALUE = "tokenValue";

  @Spy
  @InjectMocks
  private RtacClientRequestInterceptor interceptor;

  @Mock
  private FolioExecutionContext context;

  @Mock
  private SecurityManagerService securityManagerService;

  @Mock
  private ConnectionSystemParameters connectionSystemParameters;

  @Mock
  private HttpServletRequest servletRequest;

  @Mock
  private EdgeFeignClientProperties properties;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(interceptor, "okapiUrl", OKAPI_URL);
  }

  @Test
  void interceptorShouldInterceptRequestTemplate() {
    setupTests();
    var requestTemplate = new RequestTemplate();
    var headers = new HashMap<String, Collection<String>>();
    headers.put("authorization", List.of(API_KEY));
    when(context.getAllHeaders()).thenReturn(headers);

    interceptor.apply(requestTemplate);

    var requestTemplateHeaders = requestTemplate.headers();
    var tenant = ((List<String>) requestTemplateHeaders.get(XOkapiHeaders.TENANT)).getFirst();
    var token = ((List<String>) requestTemplateHeaders.get(XOkapiHeaders.TOKEN)).getFirst();
    assertEquals(OKAPI_URL, requestTemplate.path());
    assertEquals(TEST_TENANT, tenant);
    assertEquals(TOKEN_VALUE, token);
  }

  @Test
  void interceptorShouldInterceptRequestTemplate_whenApiKeyIsRequestParam() {
    setupTests();
    var requestTemplate = new RequestTemplate();
    var servletRequestAttributes = new ServletRequestAttributes(servletRequest);
    doReturn(servletRequestAttributes).when(interceptor).getServletRequestAttributes();
    when(context.getAllHeaders()).thenReturn(Collections.emptyMap());
    when(servletRequest.getParameter("apiKey")).thenReturn(API_KEY);

    interceptor.apply(requestTemplate);

    var headers = requestTemplate.headers();
    var tenant = ((List<String>) headers.get(XOkapiHeaders.TENANT)).getFirst();
    var token = ((List<String>) headers.get(XOkapiHeaders.TOKEN)).getFirst();
    assertEquals(OKAPI_URL, requestTemplate.path());
    assertEquals(TEST_TENANT, tenant);
    assertEquals(TOKEN_VALUE, token);
  }

  @Test
  void getServletRequestAttributes_shouldThrowException_whenNoApiKey() {
    doReturn(null).when(interceptor).getServletRequestAttributes();
    assertThrows(AuthorizationException.class, () -> interceptor.getServletRequest(), "No apikey provided");
  }

  private void setupTests() {
    when(properties.getOkapiUrl()).thenReturn(OKAPI_URL);
    when(securityManagerService.getParamsWithToken(API_KEY)).thenReturn(connectionSystemParameters);
    when(connectionSystemParameters.getTenantId()).thenReturn(TEST_TENANT);
    when(connectionSystemParameters.getOkapiToken()).thenReturn(new UserToken(TOKEN_VALUE, null));
  }
}