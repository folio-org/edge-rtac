package org.folio.edge.rtac.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

@Configuration
public class MapperConfig {

  @Bean
  public XmlMapper xmlMapper() {
    XmlMapper mapper = new XmlMapper();
    mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    return mapper;
  }

  @Bean
  @Primary
  public ObjectMapper jsonObjectMapper() {
    var mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    return mapper;
  }

  @Bean
  public HttpMessageConverters customConverters() {
    MappingJackson2HttpMessageConverter jsonConverter =
        new MappingJackson2HttpMessageConverter(jsonObjectMapper());

    MappingJackson2XmlHttpMessageConverter xmlConverter =
        new MappingJackson2XmlHttpMessageConverter(xmlMapper());

    return new HttpMessageConverters(jsonConverter, xmlConverter);
  }
}