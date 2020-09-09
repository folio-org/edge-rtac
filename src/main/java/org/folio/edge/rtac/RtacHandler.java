package org.folio.edge.rtac;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.folio.edge.core.Handler;
import org.folio.edge.core.security.SecureStore;
import org.folio.edge.core.utils.Mappers;
import org.folio.edge.rtac.model.Holdings;
import org.folio.edge.rtac.model.Instances;
import org.folio.edge.rtac.utils.RtacOkapiClient;
import org.folio.edge.rtac.utils.RtacOkapiClientFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import static java.util.stream.Collectors.toList;
import static org.folio.edge.core.Constants.APPLICATION_XML;

public class RtacHandler extends Handler {

  private static final Logger logger = Logger.getLogger(RtacHandler.class);

  private static final String FALLBACK_EMPTY_RESPONSE = Mappers.XML_PROLOG + "\n<holdings/>";

  private static final String PARAM_FULL_PERIODICALS = "fullPeriodicals";

  public static final String PARAM_TITLE_ID = "mms_id";

  public RtacHandler(SecureStore secureStore, RtacOkapiClientFactory ocf) {
    super(secureStore, ocf);
  }

  protected void handle(RoutingContext ctx, boolean isBatch) {
    super.handleCommon(ctx,
      new String[]{PARAM_TITLE_ID},
      new String[]{PARAM_FULL_PERIODICALS},
      (client, params) -> {
        RtacOkapiClient rtacClient = new RtacOkapiClient(client);
        String instanceIds;
        try {
          Map<String, Object> inventoryParams = new HashMap<>();

          List<String> ids = Arrays.stream(params.get(PARAM_TITLE_ID)
            .split(",")).filter(Objects::nonNull)
            .map(String::trim).collect(toList());

          inventoryParams.put("instanceIds", ids);
          inventoryParams.put(PARAM_FULL_PERIODICALS, Boolean.valueOf(params.get(PARAM_FULL_PERIODICALS)));
          instanceIds = Mappers.jsonMapper
            .writeValueAsString(inventoryParams);
        } catch (JsonProcessingException e) {
          logger.error("Exception during serialization in mod-rtac", e);
          returnEmptyResponse(ctx);
          return;
        }
        rtacClient.rtac(instanceIds, ctx.request().headers())
          .thenAcceptAsync(body -> {
            try {
              String xml;
              final Instances holdings = Instances.fromJson(body);
              if (isBatch) {
                xml = holdings.toXml();
              } else {
                Holdings holding = !holdings.getHoldings().isEmpty() ? holdings.getHoldings().get(0) : new Holdings();
                holding.setInstanceId(null);
                xml = holding.toXml();
              }
              logger.info("Converted Response: \n" + xml);
              ctx.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
                .end(xml);
            } catch (IOException e) {
              logger.error("Exception translating JSON -> XML: " + e.getMessage(), e);
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
    returnEmptyResponse(ctx);
  }

  @Override
  protected void accessDenied(RoutingContext ctx, String body) {
    returnEmptyResponse(ctx);
  }

  @Override
  protected void badRequest(RoutingContext ctx, String body) {
    returnEmptyResponse(ctx);
  }
}
