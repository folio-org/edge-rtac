package org.folio.edge.rtac;

import static java.util.stream.Collectors.toList;
import static org.folio.edge.core.Constants.APPLICATION_XML;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.core.Handler;
import org.folio.edge.core.security.SecureStore;
import org.folio.edge.core.utils.Mappers;
import org.folio.edge.rtac.model.Holdings;
import org.folio.edge.rtac.model.Instances;
import org.folio.edge.rtac.utils.RtacOkapiClient;
import org.folio.edge.rtac.utils.RtacOkapiClientFactory;

import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class RtacHandler extends Handler {

  private static final Logger logger = LogManager.getLogger(RtacHandler.class);

  private static final String FALLBACK_EMPTY_RESPONSE = Mappers.XML_PROLOG + "\n<holdings/>";

  private static final String PARAM_FULL_PERIODICALS = "fullPeriodicals";
  private static final String PARAM_TITLE_ID = "mms_id";
  private static final String PARAM_INSTANCE_ID = "instanceId";
  private static final String PARAM_INSTANCE_IDS = "instanceIds";

  public RtacHandler(SecureStore secureStore, RtacOkapiClientFactory ocf) {
    super(secureStore, ocf);
  }

  protected void handle(RoutingContext ctx, boolean isBatch) {
    final var request = ctx.request();
    logger.info("Request: {} \n params: {}", request.uri(), request.params() );
    super.handleCommon(ctx,
      new String[]{},
      new String[]{PARAM_TITLE_ID, PARAM_INSTANCE_ID, PARAM_INSTANCE_IDS, PARAM_FULL_PERIODICALS },
      (client, params) -> {

        RtacOkapiClient rtacClient = new RtacOkapiClient(client);
        String instanceIds;
        try {
          List<String> ids = getListOfIds(isBatch, params);
          if (CollectionUtils.isNullOrEmpty(ids)) {
            badRequest(ctx, "Invalid instance id" + params.toString());
            return;
          }

          var rtacParams = new HashMap<>();
          rtacParams.put(PARAM_INSTANCE_IDS, ids);
          rtacParams.put(PARAM_FULL_PERIODICALS, Boolean.valueOf(params.get(PARAM_FULL_PERIODICALS)));
          instanceIds = Mappers.jsonMapper.writeValueAsString(rtacParams);
        } catch (JsonProcessingException e) {
          logger.error("Exception during serialization in mod-rtac", e);
          returnEmptyResponse(ctx);
          return;
        }
        rtacClient.rtac(instanceIds, request.headers())
          .thenAcceptAsync(body -> {
            try {
              logger.debug("rtac response: {}", body);
              String xml;
              final var instances = Instances.fromJson(body);
              if (isBatch) {
                xml = instances.toXml();
              } else {
                var holdings = instances.getHoldings().isEmpty() ? new Holdings() : instances.getHoldings().get(0);
                xml = holdings.toXml();
              }
              logger.info("Converted Response: \n {}", xml);
              ctx.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
                .end(xml);
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

  private List<String> getListOfIds(boolean isBatch, Map<String, String> params) {
    List<String> ids;
    try {
      if (isBatch) {
        ids = Arrays.stream(params.get(PARAM_INSTANCE_IDS)
          .split(",")).filter(Objects::nonNull)
          .map(String::trim).collect(toList());
      } else {
        var id = params.get(PARAM_TITLE_ID);
        if (StringUtils.isNullOrEmpty(id)) {
          id = params.get(PARAM_INSTANCE_ID);
        }
        ids = List.of(id);
      }
    } catch (Exception e) {
      ids = Collections.emptyList();
    }
    return ids;
  }

  private void returnEmptyResponse(RoutingContext ctx) {
    // NOTE: We always return a 200 even if holdings is empty here
    // because that's what the API we're trying to mimic does...
    // Yes, even if the response from mod-rtac is non-200!
    String xml = null;
    try {
      xml = new Instances().toXml();
    } catch (JsonProcessingException e) {
      // OK, we'll do it ourselves then
      xml = FALLBACK_EMPTY_RESPONSE;
    }
    ctx.response()
      .setStatusCode(200)
      .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .end(xml);
  }

  @Override
  protected void invalidApiKey(RoutingContext ctx, String msg) {
    logger.error(msg);
    returnEmptyResponse(ctx);
  }

  @Override
  protected void accessDenied(RoutingContext ctx, String body) {
    logger.error(body);
    returnEmptyResponse(ctx);
  }

  @Override
  protected void badRequest(RoutingContext ctx, String body) {
    logger.error(body);
    returnEmptyResponse(ctx);
  }
}
