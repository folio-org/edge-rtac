package org.folio.edge.rtac;

import static org.folio.edge.rtac.TestConstants.EMPTY_INSTANCE_ID;
import static org.folio.edge.rtac.TestConstants.INSTANCE_ID;
import static org.folio.edge.rtac.TestConstants.BATCH_INSTANCE_1;
import static org.folio.edge.rtac.TestConstants.BATCH_INSTANCE_2;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import lombok.experimental.UtilityClass;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * Utility class containing result matchers for RTAC controller integration tests.
 * Provides matchers for both JSON and XML response formats.
 */
@UtilityClass
public class RtacResultMatchers {


  /**
   * Returns a result matcher for validating instance holdings response in JSON format.
   * Verifies instanceId, holdings array size, and detailed properties of each holding
   * including id, callNumber, location, status, materialType, library, notes, and holdingsStatements.
   *
   * @return ResultMatcher for JSON instance holdings validation
   */
  public static ResultMatcher matchInstanceHoldingsJson() {
    return result -> {
      jsonPath("$.instanceId", is(INSTANCE_ID)).match(result);
      jsonPath("$.holdings", hasSize(2)).match(result);
      // First holding
      jsonPath("$.holdings[0].id", is("holding-1")).match(result);
      jsonPath("$.holdings[0].callNumber", is("QA76.73.C15 C33 2019")).match(result);
      jsonPath("$.holdings[0].location", is("Main Library")).match(result);
      jsonPath("$.holdings[0].locationCode", is("ML")).match(result);
      jsonPath("$.holdings[0].locationId", is("location-id-1")).match(result);
      jsonPath("$.holdings[0].status", is("Available")).match(result);
      jsonPath("$.holdings[0].volume", is("Vol. 1")).match(result);
      jsonPath("$.holdings[0].barcode", is("12345678")).match(result);
      jsonPath("$.holdings[0].suppressFromDiscovery", is(false)).match(result);
      jsonPath("$.holdings[0].totalHoldRequests", is(0)).match(result);
      jsonPath("$.holdings[0].materialType.id", is("material-type-1")).match(result);
      jsonPath("$.holdings[0].materialType.name", is("Book")).match(result);
      jsonPath("$.holdings[0].library.name", is("Central Library")).match(result);
      jsonPath("$.holdings[0].notes", hasSize(1)).match(result);
      jsonPath("$.holdings[0].notes[0].note", is("Test note")).match(result);
      jsonPath("$.holdings[0].notes[0].holdingsNoteTypeName", is("General note")).match(result);
      jsonPath("$.holdings[0].holdingsStatements", hasSize(1)).match(result);
      jsonPath("$.holdings[0].holdingsStatements[0].statement", is("Holdings statement 1")).match(result);
      // Second holding
      jsonPath("$.holdings[1].id", is("holding-2")).match(result);
      jsonPath("$.holdings[1].callNumber", is("QA76.73.C15 C33 2019 Copy 2")).match(result);
      jsonPath("$.holdings[1].location", is("Main Library")).match(result);
      jsonPath("$.holdings[1].locationCode", is("ML")).match(result);
      jsonPath("$.holdings[1].locationId", is("location-id-2")).match(result);
      jsonPath("$.holdings[1].status", is("Checked out")).match(result);
      jsonPath("$.holdings[1].dueDate", is("2025-12-31T23:59:59Z")).match(result);
      jsonPath("$.holdings[1].volume", is("Vol. 2")).match(result);
      jsonPath("$.holdings[1].barcode", is("87654321")).match(result);
      jsonPath("$.holdings[1].suppressFromDiscovery", is(false)).match(result);
      jsonPath("$.holdings[1].totalHoldRequests", is(2)).match(result);
      jsonPath("$.holdings[1].materialType.id", is("material-type-2")).match(result);
      jsonPath("$.holdings[1].materialType.name", is("Periodical")).match(result);
      jsonPath("$.holdings[1].library.name", is("Branch Library")).match(result);
    };
  }

  /**
   * Returns a result matcher for validating instance holdings with fullPeriodicals parameter in JSON format.
   * Verifies a subset of holding properties including id, callNumber, location, status, and dueDate.
   *
   * @return ResultMatcher for JSON instance holdings with full periodicals validation
   */
  public static ResultMatcher matchInstanceHoldingsWithFullPeriodicalsJson() {
    return result -> {
      jsonPath("$.instanceId", is(INSTANCE_ID)).match(result);
      jsonPath("$.holdings", hasSize(2)).match(result);
      // First holding
      jsonPath("$.holdings[0].id", is("holding-1")).match(result);
      jsonPath("$.holdings[0].callNumber", is("QA76.73.C15 C33 2019")).match(result);
      jsonPath("$.holdings[0].location", is("Main Library")).match(result);
      jsonPath("$.holdings[0].status", is("Available")).match(result);
      // Second holding
      jsonPath("$.holdings[1].id", is("holding-2")).match(result);
      jsonPath("$.holdings[1].callNumber", is("QA76.73.C15 C33 2019 Copy 2")).match(result);
      jsonPath("$.holdings[1].status", is("Checked out")).match(result);
      jsonPath("$.holdings[1].dueDate", is("2025-12-31T23:59:59Z")).match(result);
    };
  }

  /**
   * Returns a result matcher for validating batch holdings response in JSON format.
   * Verifies multiple instances with their respective holdings including all detailed properties.
   *
   * @return ResultMatcher for JSON batch holdings validation
   */
  public static ResultMatcher matchBatchHoldingsJson() {
    return result -> {
      jsonPath("$.holdings", hasSize(2)).match(result);
      // First instance
      jsonPath("$.holdings[0].instanceId", is(BATCH_INSTANCE_1)).match(result);
      jsonPath("$.holdings[0].holdings", hasSize(1)).match(result);
      jsonPath("$.holdings[0].holdings[0].id", is("holding-1")).match(result);
      jsonPath("$.holdings[0].holdings[0].callNumber", is("QA76.73.C15 C33 2019")).match(result);
      jsonPath("$.holdings[0].holdings[0].location", is("Main Library")).match(result);
      jsonPath("$.holdings[0].holdings[0].locationCode", is("ML")).match(result);
      jsonPath("$.holdings[0].holdings[0].locationId", is("location-id-1")).match(result);
      jsonPath("$.holdings[0].holdings[0].status", is("Available")).match(result);
      jsonPath("$.holdings[0].holdings[0].volume", is("Vol. 1")).match(result);
      jsonPath("$.holdings[0].holdings[0].barcode", is("12345678")).match(result);
      jsonPath("$.holdings[0].holdings[0].suppressFromDiscovery", is(false)).match(result);
      jsonPath("$.holdings[0].holdings[0].totalHoldRequests", is(0)).match(result);
      jsonPath("$.holdings[0].holdings[0].materialType.id", is("material-type-1")).match(result);
      jsonPath("$.holdings[0].holdings[0].materialType.name", is("Book")).match(result);
      jsonPath("$.holdings[0].holdings[0].library.name", is("Central Library")).match(result);
      // Second instance
      jsonPath("$.holdings[1].instanceId", is(BATCH_INSTANCE_2)).match(result);
      jsonPath("$.holdings[1].holdings", hasSize(1)).match(result);
      jsonPath("$.holdings[1].holdings[0].id", is("holding-2")).match(result);
      jsonPath("$.holdings[1].holdings[0].callNumber", is("PR6052.R326 A6 2001")).match(result);
      jsonPath("$.holdings[1].holdings[0].location", is("Science Library")).match(result);
      jsonPath("$.holdings[1].holdings[0].locationCode", is("SL")).match(result);
      jsonPath("$.holdings[1].holdings[0].locationId", is("location-id-2")).match(result);
      jsonPath("$.holdings[1].holdings[0].status", is("Available")).match(result);
      jsonPath("$.holdings[1].holdings[0].volume", is("Vol. 1")).match(result);
      jsonPath("$.holdings[1].holdings[0].barcode", is("11223344")).match(result);
      jsonPath("$.holdings[1].holdings[0].suppressFromDiscovery", is(false)).match(result);
      jsonPath("$.holdings[1].holdings[0].totalHoldRequests", is(1)).match(result);
      jsonPath("$.holdings[1].holdings[0].materialType.id", is("material-type-2")).match(result);
      jsonPath("$.holdings[1].holdings[0].materialType.name", is("Book")).match(result);
      jsonPath("$.holdings[1].holdings[0].library.name", is("Science Complex")).match(result);
    };
  }

  /**
   * Returns a result matcher for validating empty holdings response in JSON format.
   * Verifies the instanceId matches the expected empty instance ID.
   *
   * @return ResultMatcher for JSON empty holdings validation
   */
  public static ResultMatcher matchEmptyHoldingsJson() {
    return jsonPath("$.instanceId", is(EMPTY_INSTANCE_ID));
  }

  // ========== XML Result Matchers ==========

  /**
   * Returns a result matcher for validating instance holdings response in XML format.
   * Verifies instanceId, holdings count, and detailed properties of each holding
   * including id, callNumber, location, status, materialType, library, notes, and holdingsStatements.
   *
   * @return ResultMatcher for XML instance holdings validation
   */
  public static ResultMatcher matchInstanceHoldingsXml() {
    return result -> {
      xpath("/holdings/instanceId").string(INSTANCE_ID).match(result);
      xpath("count(/holdings/holding)").number(2.0).match(result);
      // First holding
      xpath("/holdings/holding[1]/id").string("holding-1").match(result);
      xpath("/holdings/holding[1]/callNumber").string("QA76.73.C15 C33 2019").match(result);
      xpath("/holdings/holding[1]/location").string("Main Library").match(result);
      xpath("/holdings/holding[1]/locationCode").string("ML").match(result);
      xpath("/holdings/holding[1]/locationId").string("location-id-1").match(result);
      xpath("/holdings/holding[1]/status").string("Available").match(result);
      xpath("/holdings/holding[1]/volume").string("Vol. 1").match(result);
      xpath("/holdings/holding[1]/barcode").string("12345678").match(result);
      xpath("/holdings/holding[1]/suppressFromDiscovery").string("false").match(result);
      xpath("/holdings/holding[1]/totalHoldRequests").string("0").match(result);
      xpath("/holdings/holding[1]/materialType/id").string("material-type-1").match(result);
      xpath("/holdings/holding[1]/materialType/name").string("Book").match(result);
      xpath("/holdings/holding[1]/library/name").string("Central Library").match(result);
      xpath("count(/holdings/holding[1]/notes)").number(1.0).match(result);
      xpath("/holdings/holding[1]/notes[1]/note").string("Test note").match(result);
      xpath("/holdings/holding[1]/notes[1]/holdingsNoteTypeName").string("General note").match(result);
      xpath("count(/holdings/holding[1]/holdingsStatements)").number(1.0).match(result);
      xpath("/holdings/holding[1]/holdingsStatements[1]/statement").string("Holdings statement 1").match(result);
      // Second holding
      xpath("/holdings/holding[2]/id").string("holding-2").match(result);
      xpath("/holdings/holding[2]/callNumber").string("QA76.73.C15 C33 2019 Copy 2").match(result);
      xpath("/holdings/holding[2]/location").string("Main Library").match(result);
      xpath("/holdings/holding[2]/locationCode").string("ML").match(result);
      xpath("/holdings/holding[2]/locationId").string("location-id-2").match(result);
      xpath("/holdings/holding[2]/status").string("Checked out").match(result);
      xpath("/holdings/holding[2]/dueDate").string("2025-12-31T23:59:59Z").match(result);
      xpath("/holdings/holding[2]/volume").string("Vol. 2").match(result);
      xpath("/holdings/holding[2]/barcode").string("87654321").match(result);
      xpath("/holdings/holding[2]/suppressFromDiscovery").string("false").match(result);
      xpath("/holdings/holding[2]/totalHoldRequests").string("2").match(result);
      xpath("/holdings/holding[2]/materialType/id").string("material-type-2").match(result);
      xpath("/holdings/holding[2]/materialType/name").string("Periodical").match(result);
      xpath("/holdings/holding[2]/library/name").string("Branch Library").match(result);
    };
  }

  /**
   * Returns a result matcher for validating instance holdings with fullPeriodicals parameter in XML format.
   * Verifies a subset of holding properties including id, callNumber, location, status, and dueDate.
   *
   * @return ResultMatcher for XML instance holdings with full periodicals validation
   */
  public static ResultMatcher matchInstanceHoldingsWithFullPeriodicalsXml() {
    return result -> {
      xpath("/holdings/instanceId").string(INSTANCE_ID).match(result);
      xpath("count(/holdings/holding)").number(2.0).match(result);
      // First holding
      xpath("/holdings/holding[1]/id").string("holding-1").match(result);
      xpath("/holdings/holding[1]/callNumber").string("QA76.73.C15 C33 2019").match(result);
      xpath("/holdings/holding[1]/location").string("Main Library").match(result);
      xpath("/holdings/holding[1]/status").string("Available").match(result);
      // Second holding
      xpath("/holdings/holding[2]/id").string("holding-2").match(result);
      xpath("/holdings/holding[2]/callNumber").string("QA76.73.C15 C33 2019 Copy 2").match(result);
      xpath("/holdings/holding[2]/status").string("Checked out").match(result);
      xpath("/holdings/holding[2]/dueDate").string("2025-12-31T23:59:59Z").match(result);
    };
  }

  /**
   * Returns a result matcher for validating batch holdings response in XML format.
   * Verifies multiple instances with their respective holdings including all detailed properties.
   *
   * @return ResultMatcher for XML batch holdings validation
   */
  public static ResultMatcher matchBatchHoldingsXml() {
    return result -> {
      xpath("count(/instances/holdings)").number(2.0).match(result);
      // First instance
      xpath("/instances/holdings[1]/instanceId").string(BATCH_INSTANCE_1).match(result);
      xpath("count(/instances/holdings[1]/holding)").number(1.0).match(result);
      xpath("/instances/holdings[1]/holding[1]/id").string("holding-1").match(result);
      xpath("/instances/holdings[1]/holding[1]/callNumber").string("QA76.73.C15 C33 2019").match(result);
      xpath("/instances/holdings[1]/holding[1]/location").string("Main Library").match(result);
      xpath("/instances/holdings[1]/holding[1]/locationCode").string("ML").match(result);
      xpath("/instances/holdings[1]/holding[1]/locationId").string("location-id-1").match(result);
      xpath("/instances/holdings[1]/holding[1]/status").string("Available").match(result);
      xpath("/instances/holdings[1]/holding[1]/volume").string("Vol. 1").match(result);
      xpath("/instances/holdings[1]/holding[1]/barcode").string("12345678").match(result);
      xpath("/instances/holdings[1]/holding[1]/suppressFromDiscovery").string("false").match(result);
      xpath("/instances/holdings[1]/holding[1]/totalHoldRequests").string("0").match(result);
      xpath("/instances/holdings[1]/holding[1]/materialType/id").string("material-type-1").match(result);
      xpath("/instances/holdings[1]/holding[1]/materialType/name").string("Book").match(result);
      xpath("/instances/holdings[1]/holding[1]/library/name").string("Central Library").match(result);
      // Second instance
      xpath("/instances/holdings[2]/instanceId").string(BATCH_INSTANCE_2).match(result);
      xpath("count(/instances/holdings[2]/holding)").number(1.0).match(result);
      xpath("/instances/holdings[2]/holding[1]/id").string("holding-2").match(result);
      xpath("/instances/holdings[2]/holding[1]/callNumber").string("PR6052.R326 A6 2001").match(result);
      xpath("/instances/holdings[2]/holding[1]/location").string("Science Library").match(result);
      xpath("/instances/holdings[2]/holding[1]/locationCode").string("SL").match(result);
      xpath("/instances/holdings[2]/holding[1]/locationId").string("location-id-2").match(result);
      xpath("/instances/holdings[2]/holding[1]/status").string("Available").match(result);
      xpath("/instances/holdings[2]/holding[1]/volume").string("Vol. 1").match(result);
      xpath("/instances/holdings[2]/holding[1]/barcode").string("11223344").match(result);
      xpath("/instances/holdings[2]/holding[1]/suppressFromDiscovery").string("false").match(result);
      xpath("/instances/holdings[2]/holding[1]/totalHoldRequests").string("1").match(result);
      xpath("/instances/holdings[2]/holding[1]/materialType/id").string("material-type-2").match(result);
      xpath("/instances/holdings[2]/holding[1]/materialType/name").string("Book").match(result);
      xpath("/instances/holdings[2]/holding[1]/library/name").string("Science Complex").match(result);
    };
  }

  /**
   * Returns a result matcher for validating empty holdings response in XML format.
   * Verifies the instanceId matches the expected empty instance ID.
   *
   * @return ResultMatcher for XML empty holdings validation
   */
  public static ResultMatcher matchEmptyHoldingsXml() {
    return result -> xpath("/holdings/instanceId").string(EMPTY_INSTANCE_ID).match(result);
  }
}