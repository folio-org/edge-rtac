package org.folio.edge.rtac.model;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JacksonXmlRootElement(localName = "holding")
@JsonDeserialize(builder = Holding.Builder.class)
public final class Holding {
  public final String id;
  public final String callNumber;
  public final String location;
  public final String status;
  public final String dueDate;
  public final String tempLocation;
  public final String volume;

  private Holding(String id, String callNumber, String location, String status, String dueDate, String tempLocation, String volume) {
    this.id = id;
    this.callNumber = callNumber;
    this.location = location;
    this.status = status;
    this.dueDate = dueDate;
    this.tempLocation = tempLocation;
    this.volume = volume;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String id;
    private String callNumber;
    private String location;
    private String status;
    private String dueDate;
    private String tempLocation;
    private String volume;

    @JsonProperty("id")
    public Builder id(String id) {
      this.id = id;
      return this;
    }

    @JsonProperty("callNumber")
    public Builder callNumber(String callNumber) {
      this.callNumber = callNumber;
      return this;
    }

    @JsonProperty("location")
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    @JsonProperty("status")
    public Builder status(String status) {
      this.status = status;
      return this;
    }

    @JsonProperty("dueDate")
    public Builder dueDate(String dueDate) {
      this.dueDate = dueDate;
      return this;
    }

    @JsonProperty("tempLocation")
    public Builder tempLocation(String tempLocation) {
      this.tempLocation = tempLocation;
      return this;
    }

    @JsonProperty("volume")
    public Builder volume(String volume) {
      this.volume = volume;
      return this;
    }

    public Holding build() {
      return new Holding(id, callNumber, location, status, dueDate, tempLocation, volume);
    }
  }

  @Override
  @Generated("Eclipse")
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
  @Generated("Eclipse")
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Holding)) {
      return false;
    }
    Holding other = (Holding) obj;
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
