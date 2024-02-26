package org.folio.edge.rtac.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JacksonXmlRootElement(localName = "holding")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = Holding.HoldingBuilder.class)
@Builder
public final class Holding {
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
  @JsonProperty("holdingsCopyNumber")
  public final String holdingsCopyNumber;
  @JsonProperty("itemCopyNumber")
  public final String itemCopyNumber;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Holding holding = (Holding) o;
    return Objects.equals(id, holding.id) &&
      Objects.equals(callNumber, holding.callNumber) &&
      Objects.equals(location, holding.location) &&
      Objects.equals(status, holding.status) &&
      Objects.equals(dueDate, holding.dueDate) &&
      Objects.equals(tempLocation, holding.tempLocation) &&
      Objects.equals(volume, holding.volume) &&
      Objects.equals(temporaryLoanType, holding.temporaryLoanType) &&
      Objects.equals(permanentLoanType, holding.permanentLoanType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, callNumber, location, status, dueDate, tempLocation, volume, temporaryLoanType, permanentLoanType);
  }
}
