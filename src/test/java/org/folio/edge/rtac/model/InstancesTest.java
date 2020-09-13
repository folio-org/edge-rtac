package org.folio.edge.rtac.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

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

public class InstancesTest {

  private static final Logger logger = Logger.getLogger(InstancesTest.class);

  private static final String holdingsXSD = "ramls/batch-holdings.xsd";
  private Validator validator;

  private Instances instances;
  private final String INSTANCE_ID = "INSTANCE_ID";

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

    instances = new Instances();

    final var holdings = new Holdings();
    holdings.setInstanceId(INSTANCE_ID);
    holdings.setHoldings(List.of(h1, h2));
    instances.setHoldings(List.of(holdings));

    SchemaFactory schemaFactory = SchemaFactory
      .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = schemaFactory.newSchema(new File(holdingsXSD));
    validator = schema.newValidator();

  }

  @Test
  public void testToFromJson() throws IOException {
    String json = instances.toJson();
    logger.info("JSON: " + json);

    var fromJson = Instances.fromJson(json);
    assertEquals(instances, fromJson);
  }

  @Test
  public void testToFromXml() throws IOException {
    String xml = instances.toXml();
    logger.info("XML: " + xml);

    Source source = new StreamSource(new StringReader(xml));
    try {
      validator.validate(source);
    } catch (SAXException e) {
      fail("XML validation failed: " + e.getMessage());
    }

    var fromXml = Instances.fromXml(xml);
    assertEquals(instances, fromXml);
  }

  @Test
  public void testJsonToXml() throws IOException {
    String json = instances.toJson();
    var fromJson = Instances.fromJson(json);
    String xml = fromJson.toXml();
    var fromXml = Instances.fromXml(xml);

    logger.info(json);
    logger.info(xml);

    assertEquals(instances, fromJson);
    assertEquals(instances, fromXml);
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

  @Test
  public void testInstancesEquals(){
    assertNotEquals(instances, new Object());
  }

  @Test
  public void testInstancesHashCode(){
    final var i2 = new Instances();
    assertNotEquals(instances.hashCode(), i2.hashCode());
  }

  @Test
  public void testHoldingsEquals(){
    final var h1 = new Holdings();
    h1.setInstanceId("test");
    assertNotEquals(instances.getHoldings().get(0), h1);
  }
}
