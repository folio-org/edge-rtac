package org.folio.edge.rtac.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class ObjectMapperUtils {

  private final ObjectMapper objectMapper;

  public String writeAsString(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize object to JSON", e);
    }
  }

  public <T> T readValue(String modRtacResponse, Class<T> type) {
    try {
      return objectMapper.readValue(modRtacResponse, type);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to deserialize JSON to object", e);
    }
  }
}
