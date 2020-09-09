package org.folio.edge.rtac.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.folio.edge.core.utils.Mappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "holdings")
@Builder
@Data
public final class RtacHoldings {

  @JsonProperty("holdings")
  @JacksonXmlProperty(localName = "holding")
  @JacksonXmlElementWrapper(useWrapping = false)
  public final List<RtacHolding> holdingRecords = new ArrayList<>();;




  public String toXml() throws JsonProcessingException {
    return Mappers.XML_PROLOG + Mappers.xmlMapper.writeValueAsString(this);
  }

  public String toJson() throws JsonProcessingException {
    return Mappers.jsonMapper.writeValueAsString(this);
  }

  public static RtacHoldings fromJson(String json) throws IOException {
    return Mappers.jsonMapper.readValue(json, RtacHoldings.class);
  }

  public static RtacHoldings fromXml(String xml) throws IOException {
    return Mappers.xmlMapper.readValue(xml, RtacHoldings.class);
  }
}
