package org.folio.edge.rtac.model;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import nl.jqno.equalsverifier.EqualsVerifier;

public class HoldingsTest {

  private static final Logger logger = Logger.getLogger(HoldingsTest.class);

  private static final String holdingsXSD = "ramls/holdings.xsd";
  private Validator validator;

  private Instances holdings;

  @Before
  public void setUp() throws Exception {
    Holding h1 = Holding.builder()
      .id("99712686103569")
      .callNumber("PS3552.E796 D44x 1975")
      .location("LC General Collection Millersville University Library")
      .status("Item in place")
      .dueDate("")
      .volume("v.10:no.2")
      .build();

    Holding h2 = Holding.builder()
      .id("99712686103569")
      .callNumber("PS3552.E796 D44x 1975")
      .location("LC General Collection Millersville University Library")
      .status("Item in place")
      .dueDate("2018-04-23 12:00:00")
      .build();

    holdings = new Instances();

    holdings.getHoldings().add(new Holdings(asList(h1,h2)));

    SchemaFactory schemaFactory = SchemaFactory
      .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = schemaFactory.newSchema(new File(holdingsXSD));
    validator = schema.newValidator();

  }

  @Test
  public void testEqualsContract() {
    EqualsVerifier.forClass(Holding.class).verify();
    EqualsVerifier.forClass(Instances.class).verify();
  }

  @Test
  public void testToFromJson() throws IOException {
    String json = holdings.toJson();
    logger.info("JSON: " + json);

    Instances fromJson = Instances.fromJson(json);
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

    Instances fromXml = Instances.fromXml(xml);
    assertEquals(holdings, fromXml);
  }

  @Test
  public void testJsonToXml() throws IOException {
    String json = holdings.toJson();
    Instances fromJson = Instances.fromJson(json);
    String xml = fromJson.toXml();
    Instances fromXml = Instances.fromXml(xml);

    logger.info(json);
    logger.info(xml);

    assertEquals(holdings, fromJson);
    assertEquals(holdings, fromXml);
  }

  @Test
  public void testEmpty() throws IOException {
    String xml = new Instances().toXml();
    logger.info("XML: " + xml);

    Source source = new StreamSource(new StringReader(xml));
    try {
      validator.validate(source);
    } catch (SAXException e) {
      fail("XML validation failed: " + e.getMessage());
    }
  }
}
