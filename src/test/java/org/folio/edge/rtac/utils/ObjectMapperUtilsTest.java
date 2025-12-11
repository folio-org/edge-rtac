package org.folio.edge.rtac.utils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ObjectMapperUtilsTest {

  private static final String JSON_BODY = "{\"field\":\"value\"}";
  private static final String WRONG_JSON_BODY = "{\"field\":value}";
  private static final String FIELD_VALUE = "value";

  @Spy
  private final ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks
  private ObjectMapperUtils objectMapperUtils;

  @Test
  void writeAsString_shouldSerializeObject() {
    Map<String, String> testMap = new HashMap<>();
    testMap.put("field", FIELD_VALUE);

    String result = objectMapperUtils.writeAsString(testMap);

    assertNotNull(result);
    assertEquals(JSON_BODY, result);
  }

  @Test
  void readValue_shouldDeserializeJson() {
    JsonSample jsonSample = objectMapperUtils.readValue(JSON_BODY, JsonSample.class);

    assertNotNull(jsonSample);
    assertEquals(FIELD_VALUE, jsonSample.getField());
  }

  @Test
  void readValue_shouldThrowException_whenJsonBodyIsWrong() {
    assertThatThrownBy(() -> objectMapperUtils.readValue(WRONG_JSON_BODY, JsonSample.class))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("500 INTERNAL_SERVER_ERROR");
  }

  @Setter
  @Getter
  static class JsonSample {
    private String field;
  }
}