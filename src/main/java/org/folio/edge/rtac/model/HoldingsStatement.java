package org.folio.edge.rtac.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "statement",
  "note"
})
@EqualsAndHashCode(of = {"statement", "note"})
public class HoldingsStatement {

  /**
   * Name of the holdings note type (Required)
   */
  @JsonProperty("statement")
  @JsonPropertyDescription("Name of the holdings note type")
  private String statement;
  /**
   * Text content of the note (Required)
   */
  @JsonProperty("note")
  @JsonPropertyDescription("Text content of the note")
  private String note;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  /**
   * Name of the holdings note type (Required)
   */
  @JsonProperty("statement")
  public String getStatement() {
    return statement;
  }

  /**
   * Name of the holdings note type (Required)
   */
  @JsonProperty("statement")
  public void setStatement(String statement) {
    this.statement = statement;
  }

  public HoldingsStatement withStatement(String statement) {
    this.statement = statement;
    return this;
  }

  /**
   * Text content of the note (Required)
   */
  @JsonProperty("note")
  public String getNote() {
    return note;
  }

  /**
   * Text content of the note (Required)
   */
  @JsonProperty("note")
  public void setNote(String note) {
    this.note = note;
  }

  public HoldingsStatement withNote(String note) {
    this.note = note;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  public HoldingsStatement withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

}
