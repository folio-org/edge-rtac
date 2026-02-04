package org.folio.edge.rtac.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.folio.edge.rtac.client.RtacCacheClient;
import org.folio.rtac.domain.dto.RtacRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RtacCacheServiceTest {

  private static final String INSTANCE_ID_1 = "inst-1";
  private static final String INSTANCE_ID_2 = "inst-2";
  private static final String QUERY_TITLE_FOO = "title==foo";
  private static final Boolean AVAILABLE_TRUE = true;
  private static final Integer LIMIT_10 = 10;
  private static final Integer OFFSET_0 = 0;
  private static final Integer LIMIT_5 = 5;
  private static final Integer OFFSET_2 = 2;
  private static final String SORT_BY_LOCATION = "locationName,asc";
  private static final String SORT_BY_STATUS = "status,desc";

  @InjectMocks
  private RtacCacheService rtacCacheService;

  @Mock
  private RtacCacheClient rtacCacheClient;


  @Test
  void searchRtacCacheHoldings_shouldDelegateToClient_andReturnResult() {
    // given
    when(rtacCacheClient.searchRtacCacheHoldings(INSTANCE_ID_1, QUERY_TITLE_FOO, AVAILABLE_TRUE, LIMIT_10, OFFSET_0, SORT_BY_LOCATION))
      .thenReturn("search-response");

    // when
    String result = rtacCacheService.searchRtacCacheHoldings(INSTANCE_ID_1, QUERY_TITLE_FOO, AVAILABLE_TRUE, LIMIT_10, OFFSET_0, SORT_BY_LOCATION);

    // then
    assertThat(result).isNotNull();
    assertSearchDelegation(INSTANCE_ID_1, QUERY_TITLE_FOO, AVAILABLE_TRUE, LIMIT_10, OFFSET_0, SORT_BY_LOCATION);
  }

  @Test
  void getRtacCacheHoldingsById_shouldDelegateToClient_andReturnResult() {
    // given
    when(rtacCacheClient.rtacCacheById(INSTANCE_ID_2, LIMIT_5, OFFSET_2, SORT_BY_STATUS)).thenReturn("single-response");

    // when
    String result = rtacCacheService.getRtacCacheHoldingsById(INSTANCE_ID_2, LIMIT_5, OFFSET_2, SORT_BY_STATUS);

    // then
    assertThat(result).isNotNull();
    assertByIdDelegation(INSTANCE_ID_2, LIMIT_5, OFFSET_2, SORT_BY_STATUS);
  }

  @Test
  void getRtacCacheBatchHoldings_shouldDelegateToClient_andReturnResult() {
    // given
    RtacRequest request = new RtacRequest();
    when(rtacCacheClient.rtacCacheBatch(request)).thenReturn("batch-response");

    // when
    String result = rtacCacheService.getRtacCacheBatchHoldings(request);

    // then
    assertThat(result).isNotNull();
    assertBatchDelegation(request);
  }

  private void assertSearchDelegation(String instanceId, String query, Boolean available, Integer limit, Integer offset, String sort) {
    ArgumentCaptor<String> instanceCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Boolean> availableCaptor = ArgumentCaptor.forClass(Boolean.class);
    ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Integer> offsetCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<String> sortCaptor = ArgumentCaptor.forClass(String.class);

    verify(rtacCacheClient).searchRtacCacheHoldings(
        instanceCaptor.capture(),
        queryCaptor.capture(),
        availableCaptor.capture(),
        limitCaptor.capture(),
        offsetCaptor.capture(),
        sortCaptor.capture()
    );

    assertThat(instanceCaptor.getValue()).isEqualTo(instanceId);
    assertThat(queryCaptor.getValue()).isEqualTo(query);
    assertThat(availableCaptor.getValue()).isEqualTo(available);
    assertThat(limitCaptor.getValue()).isEqualTo(limit);
    assertThat(offsetCaptor.getValue()).isEqualTo(offset);
    assertThat(sortCaptor.getValue()).isEqualTo(sort);
  }

  private void assertByIdDelegation(String instanceId, Integer limit, Integer offset, String sort) {
    ArgumentCaptor<String> instanceCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Integer> offsetCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<String> sortCaptor = ArgumentCaptor.forClass(String.class);

    verify(rtacCacheClient).rtacCacheById(instanceCaptor.capture(), limitCaptor.capture(), offsetCaptor.capture(), sortCaptor.capture());

    assertThat(instanceCaptor.getValue()).isEqualTo(instanceId);
    assertThat(limitCaptor.getValue()).isEqualTo(limit);
    assertThat(offsetCaptor.getValue()).isEqualTo(offset);
    assertThat(sortCaptor.getValue()).isEqualTo(sort);
  }

  private void assertBatchDelegation(RtacRequest request) {
    ArgumentCaptor<RtacRequest> requestCaptor = ArgumentCaptor.forClass(RtacRequest.class);
    verify(rtacCacheClient).rtacCacheBatch(requestCaptor.capture());
    assertThat(requestCaptor.getValue()).isSameAs(request);
  }
}
