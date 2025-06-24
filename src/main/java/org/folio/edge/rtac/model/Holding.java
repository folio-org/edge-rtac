package org.folio.edge.rtac.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import java.util.Objects;
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
  @JsonProperty("locationCode")
  public final String locationCode;
  @JsonProperty("locationId")
  public final String locationId;
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
  @JsonProperty("barcode")
  private final String barcode;
  @JsonProperty("suppressFromDiscovery")
  private final Boolean suppressFromDiscovery;
  @JsonProperty("totalHoldRequests")
  private final Integer totalHoldRequests;
  @JsonProperty("materialType")
  private final MaterialType materialType;
  @JsonProperty("library")
  private final Library library;
  @JsonProperty("notes")
  private final List<HoldingsNote> notes;
  @JsonProperty("holdingsStatements")
  private final List<HoldingsStatement> holdingsStatements;
  @JsonProperty("holdingsStatementsForIndexes")
  private final List<HoldingsStatement> holdingsStatementsForIndexes;
  @JsonProperty("holdingsStatementsForSupplements")
  private final List<HoldingsStatement> holdingsStatementsForSupplements;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Holding holding = (Holding) o;
    return Objects.equals(id, holding.id) && Objects.equals(callNumber,
      holding.callNumber) && Objects.equals(location, holding.location)
      && Objects.equals(locationCode, holding.locationCode) && Objects.equals(
      locationId, holding.locationId) && Objects.equals(status, holding.status)
      && Objects.equals(dueDate, holding.dueDate) && Objects.equals(
      tempLocation, holding.tempLocation) && Objects.equals(volume, holding.volume)
      && Objects.equals(temporaryLoanType, holding.temporaryLoanType)
      && Objects.equals(permanentLoanType, holding.permanentLoanType)
      && Objects.equals(barcode, holding.barcode) && Objects.equals(
      suppressFromDiscovery, holding.suppressFromDiscovery) && Objects.equals(
      totalHoldRequests, holding.totalHoldRequests) && Objects.equals(materialType,
      holding.materialType) && Objects.equals(library, holding.library)
      && Objects.equals(holdingsStatements, holding.holdingsStatements)
      && Objects.equals(holdingsStatementsForIndexes,
      holding.holdingsStatementsForIndexes) && Objects.equals(
      holdingsStatementsForSupplements, holding.holdingsStatementsForSupplements);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, callNumber, location, locationCode, locationId, status, dueDate,
      tempLocation, volume, temporaryLoanType, permanentLoanType, barcode, suppressFromDiscovery,
      totalHoldRequests, materialType, library, holdingsStatements, holdingsStatementsForIndexes,
      holdingsStatementsForSupplements);
  }
}
