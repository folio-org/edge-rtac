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
@JacksonXmlRootElement(localName = "instances")
public final class Instances {

  @JsonProperty("holdings")
  @JacksonXmlProperty(localName = "holdings")
  @JacksonXmlElementWrapper(useWrapping = false)
  private List<Holdings> holdings = new ArrayList<>();

  @JsonProperty("holdings")
  public List<Holdings> getHoldings() {
    return holdings;
  }

  @JsonProperty("holdings")
  public void setHoldings(List<Holdings> holdings) {
    this.holdings = holdings;
  }

  public String toXml() throws JsonProcessingException {
    return Mappers.XML_PROLOG + Mappers.xmlMapper.writeValueAsString(this);
  }

  public String toJson() throws JsonProcessingException {
    return Mappers.jsonMapper.writeValueAsString(this);
  }

  public static Instances fromJson(String json) throws IOException {
    return Mappers.jsonMapper.readValue(json, Instances.class);
  }

  public static Instances fromXml(String xml) throws IOException {
    return Mappers.xmlMapper.readValue(xml, Instances.class);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Instances instances = (Instances) o;
    return Objects.equals(holdings, instances.holdings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(holdings);
  }
}

