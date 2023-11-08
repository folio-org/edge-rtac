package org.folio.edge.rtac.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.EqualsAndHashCode;

/**
 * third-level location unit
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "name",
  "code"
})
@EqualsAndHashCode
public class Library {

  /**
   * name of the location (Required)
   */
  @JsonProperty("name")
  @JsonPropertyDescription("name of the location")
  private String name;
  /**
   * distinct code for the location (Required)
   */
  @JsonProperty("code")
  @JsonPropertyDescription("distinct code for the location")
  private String code;

  /**
   * name of the location (Required)
   */
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  /**
   * name of the location (Required)
   */
  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public Library withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * distinct code for the location (Required)
   */
  @JsonProperty("code")
  public String getCode() {
    return code;
  }

  /**
   * distinct code for the location (Required)
   */
  @JsonProperty("code")
  public void setCode(String code) {
    this.code = code;
  }

  public Library withCode(String code) {
    this.code = code;
    return this;
  }

}
