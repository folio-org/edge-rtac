package org.folio.edge.rtac.config;

import lombok.RequiredArgsConstructor;
import org.folio.edge.rtac.client.RtacCacheClient;
import org.folio.edge.rtac.client.RtacClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class HttpClientConfiguration {

  @Qualifier("edgeHttpServiceProxyFactory")
  private final HttpServiceProxyFactory httpServiceProxyFactory;

  @Bean
  public RtacClient rtacClient() {
    return httpServiceProxyFactory.createClient(RtacClient.class);
  }

  @Bean
  public RtacCacheClient rtacCacheClient() {
    return httpServiceProxyFactory.createClient(RtacCacheClient.class);
  }
}

