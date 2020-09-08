package org.folio.edge.rtac.model;

import java.util.ArrayList;
import java.util.List;

import org.folio.edge.core.utils.Mappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "holdings")
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((holdingRecords == null) ? 0 : holdingRecords.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Holdings other = (Holdings) obj;
    if (holdingRecords == null) {
      if (other.holdingRecords != null) {
        return false;
      }
    } else if (!holdingRecords.equals(other.holdingRecords)) {
      return false;
    }
    return true;
  }

  public String toXml() throws JsonProcessingException {
    return Mappers.XML_PROLOG + Mappers.xmlMapper.writeValueAsString(this);
  }
}
