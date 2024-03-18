package org.folio.edge.rtac.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.EqualsAndHashCode;

/**
 * A material type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "id",
  "name"
})
@EqualsAndHashCode
public class MaterialType {

  @JsonProperty("id")
  private String id;
  /**
   * label for the material type (Required)
   */
  @JsonProperty("name")
  @JsonPropertyDescription("label for the material type")
  private String name;

  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  public MaterialType withId(String id) {
    this.id = id;
    return this;
  }

  /**
   * label for the material type (Required)
   */
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  /**
   * label for the material type (Required)
   */
  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public MaterialType withName(String name) {
    this.name = name;
    return this;
  }

}
