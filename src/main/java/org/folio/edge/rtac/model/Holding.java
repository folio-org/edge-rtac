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

}
