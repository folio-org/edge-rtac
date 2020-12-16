package org.folio.edge.rtac.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.folio.edge.core.utils.Mappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "holdings")
public final class Holdings {

  @JsonProperty("instanceId")
  @JacksonXmlProperty(localName = "instanceId")
  private String instanceId;

  @JsonProperty("holdings")
  @JacksonXmlProperty(localName = "holding")
  @JacksonXmlElementWrapper(useWrapping = false)
  private List<Holding> holdings = new ArrayList<>();

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public void setHoldings(List<Holding> holdings) {
    this.holdings = holdings;
  }

  public String toXml() throws JsonProcessingException {
    return Mappers.XML_PROLOG + Mappers.xmlMapper.writeValueAsString(this);
  }

  public String toJson() throws JsonProcessingException {
    return Mappers.jsonMapper.writeValueAsString(this);
  }

  public static Holdings fromXml(String xml) throws IOException {
    return Mappers.xmlMapper.readValue(xml, Holdings.class);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Holdings holdings1 = (Holdings) o;

    return holdings.equals(holdings1.holdings) &&
      Objects.equals(instanceId, holdings1.instanceId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(holdings, instanceId);
  }
}
