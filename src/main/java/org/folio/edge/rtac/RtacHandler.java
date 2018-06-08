package org.folio.edge.rtac;

import static org.folio.edge.core.Constants.APPLICATION_XML;
import static org.folio.edge.core.Constants.PARAM_API_KEY;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;
import org.folio.edge.core.InstitutionalUserHelper;
import org.folio.edge.core.InstitutionalUserHelper.MalformedApiKeyException;
import org.folio.edge.core.model.ClientInfo;
import org.folio.edge.core.security.SecureStore;
import org.folio.edge.core.utils.Mappers;
import org.folio.edge.rtac.model.Holdings;
import org.folio.edge.rtac.utils.RtacOkapiClient;
import org.folio.edge.rtac.utils.RtacOkapiClientFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class RtacHandler {

  private static final Logger logger = Logger.getLogger(RtacHandler.class);

  private static final String FALLBACK_EMPTY_RESPONSE = Mappers.XML_PROLOG + "\n<holdings/>";

  public static final String PARAM_TITLE_ID = "mms_id";

  private final RtacOkapiClientFactory ocf;
  private final InstitutionalUserHelper iuHelper;

  public RtacHandler(SecureStore secureStore, RtacOkapiClientFactory ocf) {
    this.ocf = ocf;
    iuHelper = new InstitutionalUserHelper(secureStore);
  }

  protected void handle(RoutingContext ctx) {

    String key = ctx.request().getParam(PARAM_API_KEY);
    String id = ctx.request().getParam(PARAM_TITLE_ID);

    if (id == null || id.isEmpty() || key == null || key.isEmpty()) {
      returnEmptyResponse(ctx);
    } else {
      ClientInfo clientInfo;
      try {
        clientInfo = InstitutionalUserHelper.parseApiKey(key);
      } catch (MalformedApiKeyException e) {
        returnEmptyResponse(ctx);
        return;
      }

      final RtacOkapiClient client = ocf.getRtacOkapiClient(clientInfo.tenantId);

      // get token via cache or logging in
      CompletableFuture<String> tokenFuture = iuHelper.getToken(client, 
          clientInfo.clientId, 
          clientInfo.tenantId,
          clientInfo.username);
      if (tokenFuture.isCompletedExceptionally()) {
        returnEmptyResponse(ctx);
      } else {
        tokenFuture.thenAcceptAsync(token -> {
          client.setToken(token);
          // call mod-rtac
          client.rtac(id, ctx.request().headers()).thenAcceptAsync(body -> {
            String xml = null;
            try {
              xml = Holdings.fromJson(body).toXml();
              logger.info("Converted Response: \n" + xml);
              ctx.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
                .end(xml);
            } catch (IOException e) {
              logger.error("Exception translating JSON -> XML: " + e.getMessage());
              returnEmptyResponse(ctx);
            }
          }).exceptionally(t -> {
            logger.error("Exception calling mod-rtac", t);
            returnEmptyResponse(ctx);
            return null;
          });
        });
      }
    }
  }

  private void returnEmptyResponse(RoutingContext ctx) {
    // NOTE: We always return a 200 even if holdings is empty here
    // because that's what the API we're trying to mimic does...
    // Yes, even if the response from mod-rtac is non-200!
    String xml = null;
    try {
      xml = new Holdings().toXml();
    } catch (JsonProcessingException e) {
      // OK, we'll doing ourselves then
      xml = FALLBACK_EMPTY_RESPONSE;
    }
    ctx.response()
      .setStatusCode(200)
      .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
      .end(xml);
  }

}
