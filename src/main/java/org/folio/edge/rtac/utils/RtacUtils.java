

package org.folio.edge.rtac.utils;

import static io.vertx.core.http.HttpHeaders.ACCEPT;
import static org.folio.edge.core.Constants.APPLICATION_JSON;
import static org.folio.edge.core.Constants.APPLICATION_XML;
import static org.folio.edge.core.Constants.TEXT_XML;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.folio.edge.core.utils.Mappers;
import org.folio.edge.rtac.model.Instances;

public final class RtacUtils {
  public static final String FALLBACK_EMPTY_XML_RESPONSE = Mappers.XML_PROLOG + "\n<holdings/>";
  public static final String FALLBACK_EMPTY_JSON_RESPONSE = (new JsonObject()
      .put("holdings", new JsonObject())).toString();
  public static final String SEPARATOR_COMMA = ",";
  public static final String ALL_WILDCARD = "*/*";

  private RtacUtils() { }

  private static String getPayload(boolean defaultXml) throws JsonProcessingException {
    return (defaultXml) ? new Instances().toXml() : new Instances().toJson();
  }

  private static String getEmptyPayload(boolean defaultXml) {
    return (defaultXml) ? FALLBACK_EMPTY_XML_RESPONSE : FALLBACK_EMPTY_JSON_RESPONSE;
  }

  private static String getContentType(boolean defaultXml) {
    return (defaultXml) ? APPLICATION_XML : APPLICATION_JSON;
  }

  public static boolean hasAcceptHeader(HttpServerRequest request) {
    return StringUtils.isNotBlank(
        request.headers().getAll(ACCEPT)
            .stream().map(requestType -> requestType.split(SEPARATOR_COMMA))
            .flatMap(requestTypeSplitted -> Arrays.stream(requestTypeSplitted)).map(String::trim)
            .collect(Collectors.joining()));
  }

  public static String getAcceptableContentType(RoutingContext ctx) {
    final var acceptableContentType = Optional.ofNullable(ctx.getAcceptableContentType())
        .orElse(StringUtils.EMPTY);

    if (StringUtils.isNotBlank(acceptableContentType)) {
      final var normalizedAcceptableContentType =
          acceptableContentType.equalsIgnoreCase(TEXT_XML) ? APPLICATION_XML
              : acceptableContentType;
      return normalizedAcceptableContentType;
    }

    return acceptableContentType;
  }

  /**
   * Check if there is accept header which lead to returning XML content (by business cases
   * described in the: https://issues.folio.org/browse/EDGRTAC-16)
   *
   * @param ctx
   * @return
   */
  public static boolean isXmlRequest(RoutingContext ctx) {
    return !hasAcceptHeader(ctx.request())
        || getAcceptableContentType(ctx).equalsIgnoreCase(APPLICATION_XML);
  }

  public static boolean isJsonRequest(RoutingContext ctx) {
    return getAcceptableContentType(ctx).equalsIgnoreCase(APPLICATION_JSON);
  }

  public static void returnEmptyResponse(RoutingContext ctx) {
    // NOTE: We always return a 200 even if holdings is empty here
    // because that's what the API we're trying to mimic does...
    // Yes, even if the response from mod-rtac is non-200!
    final var isXmlResponse = isXmlRequest(ctx);
    String responsePayload = null;
    try {
      responsePayload = getPayload(isXmlResponse);
    } catch (JsonProcessingException e) {
      // OK, we'll do it ourselves then
      responsePayload = getEmptyPayload(isXmlResponse);
    }
    ctx.response()
        .setStatusCode(200)
        .putHeader(HttpHeaders.CONTENT_TYPE, getContentType(isXmlResponse))
        .end(responsePayload);
  }

  /**
   * EDGE-RTAC supports application/xml or application/json  and all its derivatives in Accept
   * header. Empty Accept header implies any MIME type is accepted, same as Accept:
   *//*
   *
   * Valid examples: text/xml, text/*, *//*, *\xml, application/json, application/*, *//*, *\json
   * NOT Valid examples: application/xml, application/*, test/json
   *
   * @param request - http request to the module
   * @return - true if accept headers are supported
   */
  public static boolean checkSupportedAcceptHeaders(RoutingContext ctx) {
    return StringUtils.isBlank(getAcceptableContentType(ctx))
        && CollectionUtils.isEmpty(ctx.request().headers().getAll(ACCEPT))
        || isXmlRequest(ctx) || isJsonRequest(ctx);
  }

  public static String composeMimeTypes(String... mimeTypes) {
    String result = StringUtils.EMPTY;
    if (ArrayUtils.isNotEmpty(mimeTypes)) {
      result = Stream.of(mimeTypes).map(String::trim).filter(StringUtils::isNotBlank)
          .collect(Collectors.joining(SEPARATOR_COMMA));
    }
    return result;
  }


}
