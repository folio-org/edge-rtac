package org.folio.edge.rtac;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.edgecommonspring.client.EdgeClientProperties;
import org.folio.spring.integration.XOkapiHeaders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@Log4j2
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseIntegrationTests {


  protected static final WireMockServer WIRE_MOCK = new WireMockServer(
    options()
      .dynamicPort()
      .templatingEnabled(true)
      .globalTemplating(false));
  private static final String TEST_API_KEY = "eyJzIjoiQlBhb2ZORm5jSzY0NzdEdWJ4RGgiLCJ0IjoidGVzdCIsInUiOiJ0ZXN0X2FkbWluIn0=";
  private static final String OKAPI_URL = "okapiUrl";

  @Autowired
  protected MockMvc mockMvc;

  @BeforeAll
  static void beforeAll(@Autowired EdgeClientProperties edgeClientProperties) {
    WIRE_MOCK.start();
    ReflectionTestUtils.setField(edgeClientProperties, OKAPI_URL, WIRE_MOCK.baseUrl());
    log.info("Wire mock started");
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

  protected static ResultActions doGet(MockMvc mockMvc, String url) throws Exception {
    return mockMvc.perform(get(url)
      .headers(defaultHeaders()));
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

  protected static ResultActions doGetWithParams(MockMvc mockMvc, String url, Map<String, String> params) throws Exception {
    var request = get(url).headers(defaultHeaders());
    params.forEach(request::param);
    return mockMvc.perform(request);
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
