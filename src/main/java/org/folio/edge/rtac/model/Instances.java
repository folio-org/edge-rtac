package org.folio.edge.rtac.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import org.folio.edge.core.utils.Mappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;


@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "instances")
public final class Instances {

  @JsonProperty("holdings")
  @JacksonXmlProperty(localName = "holding")
  public final List<Holdings> holdings = new ArrayList<>();

  public List<Holdings> getHoldings() {
    return holdings;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + holdings.hashCode();
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
    Instances other = (Instances) obj;
    return holdings.equals(other.holdings);
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



  }

