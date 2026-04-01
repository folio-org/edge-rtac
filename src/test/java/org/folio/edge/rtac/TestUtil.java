package org.folio.edge.rtac;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import tools.jackson.databind.ObjectMapper;

@Log4j2
public class TestUtil {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static String readFileContentFromResources(String path) {
    try {
      ClassLoader classLoader = TestUtil.class.getClassLoader();
      URL url = classLoader.getResource(path);
      return IOUtils.toString(url, StandardCharsets.UTF_8);
    } catch (IOException e) {
      log.error(e.getMessage());
      throw new IllegalStateException(e);
    }
  }

}
