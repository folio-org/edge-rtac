
package org.folio.edge.rtac.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.folio.edge.core.utils.Mappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;


/**
 * Batch holdings response
 *
 */
@JacksonXmlRootElement(localName = "holdingsBatch")
public class RtacHoldingsBatch {

    /**
     * Real Time Availability Check (RTAC) holding details
     *
     */
    @JsonProperty("records")
    @JsonPropertyDescription("Real Time Availability Check (RTAC) holding details")
    private List<RtacHoldings> records = new ArrayList<>();

    /**
     * Real Time Availability Check (RTAC) holding details
     *
     */
    @JsonProperty("records")
    public List<RtacHoldings> getRecords() {
        return records;
    }

    /**
     * Real Time Availability Check (RTAC) holding details
     *
     */
    @JsonProperty("records")
    public void setRecords(List<RtacHoldings> records) {
        this.records = records;
    }

    public RtacHoldingsBatch withRecords(List<RtacHoldings> records) {
        this.records = records;
        return this;
    }


  public String toXml(boolean batch) throws JsonProcessingException {
    final Object value = batch ? this : records.get(0);
    return Mappers.XML_PROLOG + Mappers.xmlMapper.writeValueAsString(value);
  }

  public static RtacHoldingsBatch fromJson(String json) throws IOException {
    return Mappers.jsonMapper.readValue(json, RtacHoldingsBatch.class);
  }

}
