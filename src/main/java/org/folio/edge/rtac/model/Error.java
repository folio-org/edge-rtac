
package org.folio.edge.rtac.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * An error
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "message",
  "code",
})
public class Error {

  /**
   * Error message text
   * (Required)
   */
  @JsonProperty("message")
  @JsonPropertyDescription("Error message text")
  private String message;
  /**
   * Error message code
   */
  @JsonProperty("code")
  @JsonPropertyDescription("Error message code")
  private String code;

  /**
   * Error message text
   * (Required)
   */
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  /**
   * Error message text
   * (Required)
   */
  @JsonProperty("message")
  public void setMessage(String message) {
    this.message = message;
  }

  public Error withMessage(String message) {
    this.message = message;
    return this;
  }

  /**
   * Error message code
   */
  @JsonProperty("code")
  public String getCode() {
    return code;
  }

  /**
   * Error message code
   */
  @JsonProperty("code")
  public void setCode(String code) {
    this.code = code;
  }

  public Error withCode(String code) {
    this.code = code;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Error error = (Error) o;
    return Objects.equals(message, error.message) &&
      Objects.equals(code, error.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message, code);
  }
}
