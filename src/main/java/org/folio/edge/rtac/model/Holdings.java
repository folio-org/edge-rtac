package org.folio.edge.rtac.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.folio.edge.rtac.utils.Mappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "holdings")
public class Holdings {

  @JacksonXmlProperty(localName = "holding")
  @JacksonXmlElementWrapper(useWrapping = false)
  public List<Holding> holdings;

  public Holdings() {
    this.holdings = new ArrayList<Holding>();
  }

  @JacksonXmlRootElement(localName = "holding")
  public static class Holding {
    public String id;
    public String callNumber;
    public String location;
    public String status;
    public String dueDate;
    public String tempLocation;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((callNumber == null) ? 0 : callNumber.hashCode());
      result = prime * result + ((dueDate == null) ? 0 : dueDate.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((location == null) ? 0 : location.hashCode());
      result = prime * result + ((status == null) ? 0 : status.hashCode());
      result = prime * result + ((tempLocation == null) ? 0 : tempLocation.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Holding other = (Holding) obj;
      if (callNumber == null) {
        if (other.callNumber != null)
          return false;
      } else if (!callNumber.equals(other.callNumber))
        return false;
      if (dueDate == null) {
        if (other.dueDate != null)
          return false;
      } else if (!dueDate.equals(other.dueDate))
        return false;
      if (id == null) {
        if (other.id != null)
          return false;
      } else if (!id.equals(other.id))
        return false;
      if (location == null) {
        if (other.location != null)
          return false;
      } else if (!location.equals(other.location))
        return false;
      if (status == null) {
        if (other.status != null)
          return false;
      } else if (!status.equals(other.status))
        return false;
      if (tempLocation == null) {
        if (other.tempLocation != null)
          return false;
      } else if (!tempLocation.equals(other.tempLocation))
        return false;
      return true;
    }
  }

  public String toXml() throws JsonProcessingException {
    return Mappers.prolog + Mappers.xmlMapper.writeValueAsString(this);
  }

  public String toJson() throws JsonProcessingException {
    return Mappers.jsonMapper.writeValueAsString(this);
  }

  public static Holdings fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
    return Mappers.jsonMapper.readValue(json, Holdings.class);
  }

  public static Holdings fromXml(String xml) throws JsonParseException, JsonMappingException, IOException {
    return Mappers.xmlMapper.readValue(xml, Holdings.class);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((holdings == null) ? 0 : holdings.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Holdings other = (Holdings) obj;
    if (holdings == null) {
      if (other.holdings != null)
        return false;
    } else if (!holdings.equals(other.holdings))
      return false;
    return true;
  }
}
