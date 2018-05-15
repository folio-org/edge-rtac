package org.folio.edge.rtac.model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.folio.edge.rtac.model.Holdings.Holding;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class HoldingsTest {

  private static final Logger logger = Logger.getLogger(HoldingsTest.class);

  private static final String holdingsXSD = "ramls/holdings.xsd";
  private Validator validator;

  private Holdings holdings;

  @Before
  public void setUp() throws Exception {
    Holding h1 = new Holding();
    h1.setId("99712686103569");
    h1.setCallNumber("PS3552.E796 D44x 1975");
    h1.setLocation("LC General Collection Millersville University Library");
    h1.setStatus("Item in place");
    h1.setTempLocation("");
    h1.setDueDate("");

    Holding h2 = new Holding();
    h2.setId("99712686103569");
    h2.setCallNumber("PS3552.E796 D44x 1975");
    h2.setLocation("LC General Collection Millersville University Library");
    h2.setStatus("Item in place");
    h2.setTempLocation("");
    h2.setDueDate("2018-04-23 12:00:00");

    holdings = new Holdings();
    holdings.holdingRecords.add(h1);
    holdings.holdingRecords.add(h2);

    SchemaFactory schemaFactory = SchemaFactory
      .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = schemaFactory.newSchema(new File(holdingsXSD));
    validator = schema.newValidator();

  }

  @Test
  public void testToFromJson() throws IOException {
    String json = holdings.toJson();
    logger.info("JSON: " + json);

    Holdings fromJson = Holdings.fromJson(json);
    assertEquals(holdings, fromJson);
  }

  @Test
  public void testToFromXml() throws IOException {
    String xml = holdings.toXml();
    logger.info("XML: " + xml);

    Source source = new StreamSource(new StringReader(xml));
    try {
      validator.validate(source);
    } catch (SAXException e) {
      fail("XML validation failed: " + e.getMessage());
    }

    Holdings fromXml = Holdings.fromXml(xml);
    assertEquals(holdings, fromXml);
  }

  @Test
  public void testEmpty() throws IOException {
    String xml = new Holdings().toXml();
    logger.info("XML: " + xml);

    Source source = new StreamSource(new StringReader(xml));
    try {
      validator.validate(source);
    } catch (SAXException e) {
      fail("XML validation failed: " + e.getMessage());
    }
  }
}
