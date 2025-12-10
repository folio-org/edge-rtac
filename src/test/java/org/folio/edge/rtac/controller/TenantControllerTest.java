package org.folio.edge.rtac.controller;

import static org.folio.edge.rtac.TestConstants.POST_TENANT_REQUEST;
import static org.folio.edge.rtac.TestConstants.TENANT_URL;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.edge.rtac.BaseIntegrationTests;
import org.folio.edge.rtac.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class TenantControllerTest extends BaseIntegrationTests {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void postTenant_shouldReturnHttpStatusOk() throws Exception {
    String requestBody = TestUtil.readFileContentFromResources(POST_TENANT_REQUEST);

    doPost(mockMvc, TENANT_URL, requestBody)
      .andExpect(status().isNoContent());
  }
}