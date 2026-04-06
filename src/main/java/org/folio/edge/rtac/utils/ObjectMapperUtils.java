package org.folio.edge.rtac.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ObjectMapperUtils {

  private final ObjectMapper objectMapper;

  public String writeAsString(Object object) {
      return objectMapper.writeValueAsString(object);
  }

  public JsonNode readTree(String content) {
    return objectMapper.readTree(content);
  }

  public <T> T readValue(String modRtacResponse, Class<T> type) {
      return objectMapper.readValue(modRtacResponse, type);
  }
}
