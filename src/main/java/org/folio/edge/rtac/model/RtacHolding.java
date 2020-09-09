package org.folio.edge.rtac.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JacksonXmlRootElement(localName = "holding")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = RtacHolding.RtacHoldingBuilder.class)
@Builder
public class RtacHolding {

  @JsonProperty("id")
  public final String id;
  @JsonProperty("callNumber")
  public final String callNumber;
  @JsonProperty("location")
  public final String location;
  @JsonProperty("status")
  public final String status;
  @JsonProperty("dueDate")
  public final String dueDate;
  @JsonProperty("tempLocation")
  public final String tempLocation;
  @JsonProperty("volume")
  public final String volume;
  @JsonProperty("temporaryLoanType")
  private final String temporaryLoanType;
  @JsonProperty("permanentLoanType")
  private final String permanentLoanType;

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
    result = prime * result + ((volume == null) ? 0 : volume.hashCode());
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
    if (!(obj instanceof RtacHolding)) {
      return false;
    }
    RtacHolding other = (RtacHolding) obj;
    if (callNumber == null) {
      if (other.callNumber != null) {
        return false;
      }
    } else if (!callNumber.equals(other.callNumber)) {
      return false;
    }
    if (dueDate == null) {
      if (other.dueDate != null) {
        return false;
      }
    } else if (!dueDate.equals(other.dueDate)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (location == null) {
      if (other.location != null) {
        return false;
      }
    } else if (!location.equals(other.location)) {
      return false;
    }
    if (status == null) {
      if (other.status != null) {
        return false;
      }
    } else if (!status.equals(other.status)) {
      return false;
    }
    if (tempLocation == null) {
      if (other.tempLocation != null) {
        return false;
      }
    } else if (!tempLocation.equals(other.tempLocation)) {
      return false;
    }
    if (volume == null) {
      if (other.volume != null) {
        return false;
      }
    } else if (!volume.equals(other.volume)) {
      return false;
    }
    return true;
  }
}
