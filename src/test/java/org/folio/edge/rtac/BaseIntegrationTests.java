package org.folio.edge.rtac;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.edge.rtac.config.RtacClientRequestInterceptor;
import org.folio.edgecommonspring.client.EdgeFeignClientProperties;
import org.folio.edgecommonspring.client.EnrichUrlClient;
import org.folio.spring.integration.XOkapiHeaders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@Log4j2
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource("classpath:application-test.yml")
@Import({RtacClientRequestInterceptor.class})
@AutoConfigureMockMvc
@AutoConfigureObservability
public abstract class BaseIntegrationTests {

  protected static final WireMockServer WIRE_MOCK = new WireMockServer(
      WireMockSpring.options()
          .dynamicPort()
          .extensions(new ResponseTemplateTransformer(false)));
  private static final String TEST_API_KEY = "eyJzIjoiQlBhb2ZORm5jSzY0NzdEdWJ4RGgiLCJ0IjoidGVzdCIsInUiOiJ0ZXN0X2FkbWluIn0=";
  private static final String OKAPI_URL = "okapiUrl";

  @BeforeAll
  static void beforeAll(@Autowired EnrichUrlClient enrichUrlClient,
      @Autowired RtacClientRequestInterceptor interceptor, @Autowired EdgeFeignClientProperties properties) {
    WIRE_MOCK.start();
    var url = WIRE_MOCK.baseUrl();
    ReflectionTestUtils.setField(enrichUrlClient, OKAPI_URL, url);
    ReflectionTestUtils.setField(interceptor, OKAPI_URL, url);
    ReflectionTestUtils.setField(properties, OKAPI_URL, url);
  }

  @AfterAll
  static void afterAll() {
    WIRE_MOCK.stop();
  }

  protected static ResultActions doGet(MockMvc mockMvc, String url, MediaType acceptType) throws Exception {
    return mockMvc.perform(get(url)
        .headers(defaultHeaders())
        .accept(acceptType));
  }

  @SneakyThrows
  protected static ResultActions doPost(MockMvc mockMvc, String url, String body) {
    return mockMvc.perform(post(url)
        .content(body)
        .headers(defaultHeaders()));
  }

  protected static ResultActions doGetWithParam(MockMvc mockMvc, String url, String paramName, String paramValue)
      throws Exception {
    return mockMvc.perform(get(url)
        .param(paramName, paramValue)
        .headers(defaultHeaders()));
  }

  protected static ResultActions doGetWithParam(MockMvc mockMvc, String url, String paramName, String paramValue,
      MediaType acceptType) throws Exception {
    return mockMvc.perform(get(url)
        .param(paramName, paramValue)
        .headers(defaultHeaders())
        .accept(acceptType));
  }

  private static HttpHeaders defaultHeaders() {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(APPLICATION_JSON);
    httpHeaders.put(XOkapiHeaders.TENANT, List.of(TestConstants.TEST_TENANT));
    httpHeaders.put(XOkapiHeaders.URL, List.of(WIRE_MOCK.baseUrl()));
    httpHeaders.put(XOkapiHeaders.AUTHORIZATION, List.of(TEST_API_KEY));
    return httpHeaders;
  }
}