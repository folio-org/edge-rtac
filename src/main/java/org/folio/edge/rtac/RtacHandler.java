package org.folio.edge.rtac;

import static org.folio.edge.rtac.Constants.PARAM_API_KEY;
import static org.folio.edge.rtac.Constants.PARAM_TITLE_ID;

import java.util.Base64;

import org.apache.log4j.Logger;
import org.folio.edge.rtac.model.Holdings;
import org.folio.edge.rtac.security.SecureStore;
import org.folio.edge.rtac.utils.Mappers;
import org.folio.edge.rtac.utils.OkapiClient;
import org.folio.edge.rtac.utils.OkapiClientFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class RtacHandler {

  private static final Logger logger = Logger.getLogger(RtacHandler.class);

  private final SecureStore secureStore;
  private final OkapiClientFactory ocf;

  public RtacHandler(SecureStore secureStore, OkapiClientFactory ocf) {
    this.secureStore = secureStore;
    this.ocf = ocf;
  }

  protected void rtacHandler(RoutingContext ctx) {

    String key = ctx.request().getParam(PARAM_API_KEY);
    String id = ctx.request().getParam(PARAM_TITLE_ID);

    if (id == null || id.isEmpty() || key == null || key.isEmpty()) {
      // NOTE: We always return a 200 even if holdings is empty here
      // because that's what the API we're trying to mimic does...
      // Yes, even if the response from mod-rtac is non-200!
      String xml = null;
      try {
        xml = new Holdings().toXml();
      } catch (JsonProcessingException e) {
        // OK, we'll doing ourselves then
        xml = Mappers.prolog + "\n<holdings/>";
      }
      ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, "application/xml")
        .end(xml);
    } else {
      String tenant = new String(Base64.getUrlDecoder().decode(key));

      logger.info(String.format("API Key: %s, Tenant: %s", key, tenant));

      OkapiClient client = ocf.getOkapiClient(tenant);

      String user = tenant;
      String password = secureStore.get(tenant, user);

      // login
      client.getToken(user, password).thenRun(() -> {
        // call mod-rtac
        client.rtac(id).thenAcceptAsync(body -> {
          String xml = null;
          try {
            xml = Holdings.fromJson(body).toXml();
            logger.info("Converted Response: \n" + xml);
          } catch (Exception e) {
            xml = Mappers.prolog + "\n<holdings/>";
            logger.error("Exception translating JSON -> XML: " + e.getMessage());
          }

          // NOTE: Again, we return a 200 here because that's what the
          // API
          // we're trying to mimic does
          ctx.response()
            .setStatusCode(200)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/xml")
            .end(xml);
        });
      });
    }
  }
}
