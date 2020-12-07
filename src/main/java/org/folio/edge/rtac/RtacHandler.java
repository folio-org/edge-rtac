package org.folio.edge.rtac;

import static io.vertx.core.http.HttpHeaders.ACCEPT;
import static java.util.stream.Collectors.toList;
import static org.folio.edge.core.Constants.APPLICATION_JSON;
import static org.folio.edge.core.Constants.APPLICATION_XML;
import static org.folio.edge.rtac.utils.RtacUtils.SEPARATOR_COMMA;
import static org.folio.edge.rtac.utils.RtacUtils.checkSupportedAcceptHeaders;
import static org.folio.edge.rtac.utils.RtacUtils.hasRequestAnyOfAcceptTypes;
import static org.folio.edge.rtac.utils.RtacUtils.isXmlRequest;
import static org.folio.edge.rtac.utils.RtacUtils.returnEmptyResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.core.Handler;
import org.folio.edge.core.security.SecureStore;
import org.folio.edge.core.utils.Mappers;
import org.folio.edge.rtac.model.Holdings;
import org.folio.edge.rtac.model.Instances;
import org.folio.edge.rtac.utils.RtacMimeTypeEnum;
import org.folio.edge.rtac.utils.RtacOkapiClient;
import org.folio.edge.rtac.utils.RtacOkapiClientFactory;

public class RtacHandler extends Handler {

  private static final Logger logger = LogManager.getLogger(RtacHandler.class);

  private static final String PARAM_FULL_PERIODICALS = "fullPeriodicals";
  private static final String PARAM_TITLE_ID = "mms_id";
  private static final String PARAM_INSTANCE_ID = "instanceId";
  private static final String PARAM_INSTANCE_IDS = "instanceIds";

  public RtacHandler(SecureStore secureStore, RtacOkapiClientFactory ocf) {
    super(secureStore, ocf);
  }

  protected void handle(RoutingContext ctx, boolean isBatch) {
    final var request = ctx.request();
    logger.info("Request: {} \n params: {}", request.uri(), request.params());

    // Check if there were supported "Accept" headers passed
    if (!checkSupportedAcceptHeaders(request)) {
      notAcceptable(ctx, "Unsupported media type: " + request.getHeader(ACCEPT));
      return;
    }

    super.handleCommon(ctx,
        new String[]{},
        new String[]{PARAM_TITLE_ID, PARAM_INSTANCE_ID, PARAM_INSTANCE_IDS, PARAM_FULL_PERIODICALS},
        (client, params) -> {

          RtacOkapiClient rtacClient = new RtacOkapiClient(client);
          String instanceIds;
          try {
            List<String> ids = getListOfIds(isBatch, params);
            if (CollectionUtils.isEmpty(ids)) {
              badRequest(ctx, "Invalid instance id" + params.toString());
              return;
            }

            var rtacParams = new HashMap<>();
            rtacParams.put(PARAM_INSTANCE_IDS, ids);
            rtacParams
                .put(PARAM_FULL_PERIODICALS, Boolean.valueOf(params.get(PARAM_FULL_PERIODICALS)));
            instanceIds = Mappers.jsonMapper.writeValueAsString(rtacParams);
          } catch (JsonProcessingException e) {
            logger.error("Exception during serialization in mod-rtac", e);
            returnEmptyResponse(ctx);
            return;
          }

          // Remember original Accept headers
          final var rtacAcceptMimeType = getRtacMimeType(request);

          // Determine if it's  need to mock original Accept header with json-type, which is used internally
          final boolean needUpdateRtacMimeType = hasRequestAnyOfAcceptTypes(request,
              RtacMimeTypeEnum.APPLICATION_XML, RtacMimeTypeEnum.TEXT_XML, RtacMimeTypeEnum.ALL)
              || !rtacAcceptMimeType.stream().findFirst().orElse(StringUtils.EMPTY)
              .equalsIgnoreCase(APPLICATION_JSON);

          // Update Accept header with internally accepted value
          if (needUpdateRtacMimeType) {
            updateRequestWithMimeType(request, APPLICATION_JSON);
          }

          rtacClient.rtac(instanceIds, request.headers())
              .thenAcceptAsync(body -> {
                try {
                  logger.debug("rtac response: {}", body);

                  // Restore original Accept headers
                  if (needUpdateRtacMimeType) {
                    updateRequestWithMimeType(request, rtacAcceptMimeType.toArray(new String[0]));
                  }

                  String returningContent = body;
                  /// Determine whether the response should be returned in XML format
                  final var isXmlResponse = isXmlRequest(request);
                  if (isXmlResponse) {
                    final var instances = Instances.fromJson(body);
                    if (isBatch) {
                      returningContent = instances.toXml();
                    } else {
                      var holdings = instances.getHoldings().isEmpty() ? new Holdings()
                          : instances.getHoldings().get(0);
                      returningContent = holdings.toXml();
                    }
                  }


                  logger.info("Converted Response: \n {}", returningContent);
                  ctx.response()
                      .setStatusCode(200)
                      .putHeader(HttpHeaders.CONTENT_TYPE,
                          isXmlResponse ? APPLICATION_XML : APPLICATION_JSON)
                      .end(returningContent);

                } catch (IOException e) {
                  logger.error("Exception translating JSON -> XML: {}", e.getMessage(), e);
                  returnEmptyResponse(ctx);
                }
              })
              .exceptionally(t -> {
                logger.error("Exception calling mod-rtac", t);
                returnEmptyResponse(ctx);
                return null;
              });
        });
  }

  /**
   * Returns mime type from Accept header of Rtac request
   *
   * @param request
   * @return
   */
  protected List<String> getRtacMimeType(HttpServerRequest request) {
    return request.headers().getAll(ACCEPT);
  }

  /**
   * Updates mime type of Accept header from Rtac request
   *
   * @param request
   * @param acceptHeader
   */
  protected void updateRequestWithMimeType(HttpServerRequest request, String... acceptHeader) {
    request.headers().remove(ACCEPT);
    if (ArrayUtils.isNotEmpty(acceptHeader)) {
      Arrays.stream(acceptHeader).filter(StringUtils::isNotBlank).forEach(header -> {
        logger.info("Accept header will be updated with: " + acceptHeader);
        request.headers().add(ACCEPT, header);
      });
    }
  }

  private List<String> getListOfIds(boolean isBatch, Map<String, String> params) {
    List<String> ids;
    try {
      if (isBatch) {
        ids = Arrays.stream(params.get(PARAM_INSTANCE_IDS)
            .split(SEPARATOR_COMMA)).filter(Objects::nonNull)
            .map(String::trim).collect(toList());
      } else {
        var id = params.get(PARAM_TITLE_ID);
        if (StringUtils.isEmpty(id)) {
          id = params.get(PARAM_INSTANCE_ID);
        }
        ids = List.of(id);
      }
    } catch (Exception e) {
      ids = Collections.emptyList();
    }
    return ids;
  }

  @Override
  protected void invalidApiKey(RoutingContext ctx, String msg) {
    logger.error("Invalid API key: " + msg);
    returnEmptyResponse(ctx);
  }

  @Override
  protected void accessDenied(RoutingContext ctx, String body) {
    logger.error("Access denied: " + body);
    returnEmptyResponse(ctx);
  }

  @Override
  protected void badRequest(RoutingContext ctx, String body) {
    logger.error("Bad request: " + body);
    returnEmptyResponse(ctx);
  }
}
