package org.folio.edge.rtac;

import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class RtacHandlerTest {

  @ParameterizedTest
  @CsvSource(textBlock = """
      '', ''
      ?apikey=q, ''
      /foo/bar?apikey=q, /foo/bar
      http://127.0.0.1, http://127.0.0.1
      https://example.com/?, https://example.com/
      https://example.com/foo/bar/?x=a&y=b, https://example.com/foo/bar/
      https://example.com?x=a?y=b, https://example.com
      """)
  void logUri(String uri, String expected) {
    var request = mock(HttpServerRequest.class);
    when(request.uri()).thenReturn(uri);
    when(request.params()).thenReturn(MultiMap.caseInsensitiveMultiMap());
    var logger = mock(Logger.class);
    RtacHandler.log(logger, request);
    verify(logger).info(anyString(), eq(expected), any());
  }

  static Stream<Arguments> logParams() {
    return Stream.of(
        of(Map.of(), List.of("[]")),
        of(Map.of("a", "x", "b", "y"), List.of("[a=x, b=y]", "[b=y, a=x]")),
        of(Map.of("apikey", "foo"), List.of("[apikey=...]")),
        of(Map.of("apikey", "foo", "a", "x", "apiKey", "bar"), List.of(
            "[a=x, apiKey=..., apikey=...]",
            "[a=x, apikey=..., apiKey=...]",
            "[apiKey=..., a=x, apikey=...]",
            "[apikey=..., a=x, apiKey=...]",
            "[apiKey=..., apikey=..., a=x]",
            "[apikey=..., apiKey=..., a=x]"
            ))
        );
  }

  @ParameterizedTest
  @MethodSource
  void logParams(Map<String,String> params, List<String> expected) {
    var request = mock(HttpServerRequest.class);
    when(request.uri()).thenReturn("");
    when(request.params()).thenReturn(MultiMap.caseInsensitiveMultiMap().addAll(params));
    var logger = mock(Logger.class);
    RtacHandler.log(logger, request);
    verify(logger).info(anyString(), any(), argThat(actual -> expected.contains(actual.toString())));
  }

}
