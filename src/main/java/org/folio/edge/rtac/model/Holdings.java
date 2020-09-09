package org.folio.edge.rtac.model;

import java.util.ArrayList;
import java.util.List;

import org.folio.edge.core.utils.Mappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "holdings")
@Builder
@Setter
public final class Holdings {

  @JsonProperty("holdings")
  @JacksonXmlProperty(localName = "holding")
  @JacksonXmlElementWrapper(useWrapping =false)
  private List<Holding> holdings;

  public Holdings() {
    holdings = new ArrayList<>();
  }

  public Holdings(List<Holding> holdings) {
    this.holdings = holdings;
  }

  public Holdings(List<Holding> holdings, String instanceId) {
    this.holdings = holdings;
    this.instanceId = instanceId;
  }

  @JsonProperty("instanceId")
  @JacksonXmlProperty(localName = "instanceId")
  public String instanceId = null;

  public String toXml() throws JsonProcessingException {
    return Mappers.XML_PROLOG + Mappers.xmlMapper.writeValueAsString(this);
  }
}
