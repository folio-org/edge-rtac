package org.folio.edge.rtac.controller;

import static org.folio.edge.rtac.TestConstants.INSTANCE_UUID;
import static org.folio.edge.rtac.TestConstants.NON_EXISTENT_INSTANCE_UUID;
import static org.folio.edge.rtac.TestConstants.QUERY_PARAM;
import static org.folio.edge.rtac.TestConstants.QUERY_PARAM_VALUE;
import static org.folio.edge.rtac.TestConstants.RTAC_CACHE_REQUEST_NON_EXISTENT_PATH;
import static org.folio.edge.rtac.TestConstants.RTAC_CACHE_REQUEST_PATH;
import static org.folio.edge.rtac.TestConstants.RTAC_CACHE_SEARCH_URL;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.edge.rtac.BaseIntegrationTests;
import org.folio.edge.rtac.TestConstants;
import org.folio.edge.rtac.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class RtacCacheControllerIT extends BaseIntegrationTests {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void searchInstanceRtacCache_shouldReturnCachedHoldings() throws Exception {
    doGetWithParam(mockMvc, RTAC_CACHE_SEARCH_URL + INSTANCE_UUID, QUERY_PARAM, QUERY_PARAM_VALUE)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.instanceId", equalTo(INSTANCE_UUID)))
      .andExpect(jsonPath("$.holdings", notNullValue()))
      .andExpect(jsonPath("$.totalRecords", equalTo(2)));
  }

  @Test
  void searchInstanceRtacCache_shouldReturnNotFound_whenInstanceDoesNotExist() throws Exception {
    doGetWithParam(mockMvc, RTAC_CACHE_SEARCH_URL + NON_EXISTENT_INSTANCE_UUID, QUERY_PARAM, QUERY_PARAM_VALUE)
      .andExpect(status().isNotFound());
  }

  @Test
  void getRtacCacheHoldingsById_shouldReturnCachedHoldings() throws Exception {
    doGet(mockMvc, TestConstants.RTAC_CACHE_BY_ID_URL + INSTANCE_UUID, MediaType.APPLICATION_JSON)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.instanceId", equalTo(INSTANCE_UUID)))
      .andExpect(jsonPath("$.holdings", notNullValue()))
      .andExpect(jsonPath("$.totalRecords", equalTo(2)));
  }

  @Test
  void getRtacCacheHoldingsById_shouldReturnNotFound_whenInstanceDoesNotExist() throws Exception {
    doGet(mockMvc, TestConstants.RTAC_CACHE_BY_ID_URL + NON_EXISTENT_INSTANCE_UUID, MediaType.APPLICATION_JSON)
      .andExpect(status().isNotFound());
  }

  @Test
  void postRtacCacheBatchHoldings_shouldReturnBatchCachedHoldings() throws Exception {
    var requestBody = TestUtil.readFileContentFromResources(RTAC_CACHE_REQUEST_PATH);
    doPost(mockMvc, TestConstants.RTAC_CACHE_BATCH_URL, requestBody)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.holdings[0].instanceId", equalTo(INSTANCE_UUID)))
      .andExpect(jsonPath("$.holdings[0].hasVolumes", equalTo(true)));
  }

  @Test
  void postRtacCacheBatchHoldings_shouldReturnNotFound_whenInstancesDoNotExist() throws Exception {
    var requestBody = TestUtil.readFileContentFromResources(RTAC_CACHE_REQUEST_NON_EXISTENT_PATH);
    doPost(mockMvc, TestConstants.RTAC_CACHE_BATCH_URL, requestBody)
      .andExpect(status().isNotFound());
  }
}
