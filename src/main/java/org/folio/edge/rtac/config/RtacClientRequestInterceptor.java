package org.folio.edge.rtac.config;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.TOKEN;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.edge.api.utils.exception.AuthorizationException;
import org.folio.edgecommonspring.client.EdgeFeignClientProperties;
import org.folio.edgecommonspring.security.SecurityManagerService;
import org.folio.spring.FolioExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Log4j2
@RequiredArgsConstructor
public class RtacClientRequestInterceptor implements RequestInterceptor {

  public static final String AUTHORIZATION = "authorization";
  private final EdgeFeignClientProperties properties;
  private final SecurityManagerService securityManagerService;
  private final FolioExecutionContext context;

  @Deprecated
  @Value("${okapi_url:NO_VALUE}")
  private String okapiUrl;

  @Override
  public void apply(RequestTemplate template) {
    String okapiUrlToUse = properties.getOkapiUrl();
    if (isBlank(okapiUrlToUse)) {
      log.warn("deprecated property okapi_url is used. Please use folio.client.okapiUrl instead.");
      okapiUrlToUse = okapiUrl;
    }
    template.target(okapiUrlToUse);

    String apiKey = getApiKey();
    var paramsWithToken = securityManagerService.getParamsWithToken(apiKey);

    template.header(TENANT, paramsWithToken.getTenantId());
    template.header(TOKEN, paramsWithToken.getOkapiToken().accessToken());

    log.debug("RTAC client request to {} for tenant {}", okapiUrlToUse, paramsWithToken.getTenantId());
  }

  private String getApiKey() {
    var authorizationList = (List<String>) context.getAllHeaders().get(AUTHORIZATION);
    if (Objects.nonNull(authorizationList)) {
      return authorizationList.getFirst();
    }
    var request = getServletRequest();
    return request.getParameter("apiKey");
  }

  HttpServletRequest getServletRequest() {
    ServletRequestAttributes requestAttributes = getServletRequestAttributes();
    if (Objects.nonNull(requestAttributes)) {
      return requestAttributes.getRequest();
    }
    throw new AuthorizationException("No apikey provided");
  }

  ServletRequestAttributes getServletRequestAttributes() {
    return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
  }
}
