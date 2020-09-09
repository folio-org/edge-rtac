package org.folio.edge.rtac.model;

import java.util.ArrayList;
import java.util.List;

import org.folio.edge.core.utils.Mappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "holdings")
@Builder
@Data
public final class Holdings {

  @JsonProperty("holdings")
  @JacksonXmlProperty(localName = "holding")
  public List<Holding> holdingRecords;

  public Holdings() {
    holdingRecords = new ArrayList<>();
  }

  public Holdings(List<Holding> holdingRecords) {
    this.holdingRecords = holdingRecords;
  }

  public Holdings(List<Holding> holdingRecords, String instanceId) {
    this.holdingRecords = holdingRecords;
    this.instanceId = instanceId;
  }

  @JsonProperty("instanceId")
  @JacksonXmlProperty(localName = "instanceId")
  public String instanceId = null;

  public String toXml() throws JsonProcessingException {
    return Mappers.XML_PROLOG + Mappers.xmlMapper.writeValueAsString(this);
  }
}
