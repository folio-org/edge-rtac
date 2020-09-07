package org.folio.edge.rtac;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.folio.edge.core.Handler;
import org.folio.edge.core.security.SecureStore;
import org.folio.edge.core.utils.Mappers;
import org.folio.edge.rtac.model.Holdings;
import org.folio.edge.rtac.utils.RtacOkapiClient;
import org.folio.edge.rtac.utils.RtacOkapiClientFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import static java.util.Collections.singletonMap;
import static org.folio.edge.core.Constants.APPLICATION_XML;

public class RtacHandler extends Handler {

  private static final Logger logger = Logger.getLogger(RtacHandler.class);

  private static final String FALLBACK_EMPTY_RESPONSE = Mappers.XML_PROLOG + "\n<holdings/>";

  public static final String PARAM_TITLE_ID = "mms_id";

  public RtacHandler(SecureStore secureStore, RtacOkapiClientFactory ocf) {
    super(secureStore, ocf);
  }

  protected void handle(RoutingContext ctx) {
    super.handleCommon(ctx,
      new String[]{PARAM_TITLE_ID},
      new String[]{},
      (client, params) -> {
        RtacOkapiClient rtacClient = new RtacOkapiClient(client);
        String instanceIds;
        try {
          instanceIds = Mappers.jsonMapper
            .writeValueAsString(singletonMap("instanceIds",
              params.get(PARAM_TITLE_ID)));
        } catch (JsonProcessingException e) {
          logger.error("Exception during serialization in mod-rtac", e);
          returnEmptyResponse(ctx);
          return;
        }
        rtacClient.rtac(instanceIds, ctx.request().headers())
          .thenAcceptAsync(body -> {
            try {
              String xml = Holdings.fromJson(body).toXml();
              logger.info("Converted Response: \n" + xml);
              ctx.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
                .end(xml);
            } catch (IOException e) {
              logger.error("Exception translating JSON -> XML: " + e.getMessage());
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
      xml = new Holdings().toXml();
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
