
package org.folio.edge.rtac.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * An error
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "message",
    "type",
    "code",
    "parameters"
})
public class Error {

    /**
     * Error message text
     * (Required)
     *
     */
    @JsonProperty("message")
    @JsonPropertyDescription("Error message text")
    private String message;
    /**
     * Error message type
     *
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Error message type")
    private String type;
    /**
     * Error message code
     *
     */
    @JsonProperty("code")
    @JsonPropertyDescription("Error message code")
    private String code;

    /**
     * Error message text
     * (Required)
     *
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * Error message text
     * (Required)
     *
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
     * Error message type
     *
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * Error message type
     *
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    public Error withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Error message code
     *
     */
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    /**
     * Error message code
     *
     */
    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    public Error withCode(String code) {
        this.code = code;
        return this;
    }
}
