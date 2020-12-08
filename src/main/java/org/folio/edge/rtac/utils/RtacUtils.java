package org.folio.edge.rtac.utils;

import static io.vertx.core.http.HttpHeaders.ACCEPT;
import static org.folio.edge.core.Constants.APPLICATION_JSON;
import static org.folio.edge.core.Constants.APPLICATION_XML;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
  private boolean result;

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
    return (request.headers().contains(ACCEPT) && StringUtils.isNotBlank(
        getAcceptHeaderList(request).stream().collect(Collectors.joining())));
  }

  public static boolean hasRequestAnyOfAcceptTypes(HttpServerRequest request,
      RtacMimeTypeEnum... types) {
    boolean result = false;

    if (ArrayUtils.isNotEmpty(types)) {
      var normalizedAcceptHeaders = getAcceptHeaderList(request);

      result = normalizedAcceptHeaders.stream()
          .anyMatch(reqMimeArrayEntry -> Stream.of(types)
              .anyMatch(mimeType -> mimeType.toString().equalsIgnoreCase(reqMimeArrayEntry)));
    }
    return result;
  }

  /**
   * Check if there is accept header which lead to returning XML content (by business cases
   * described in the: https://issues.folio.org/browse/EDGRTAC-16)
   *
   * @param request
   * @return
   */
  public static boolean isXmlRequest(HttpServerRequest request) {
    if (hasAcceptHeader(request)) {
      return hasRequestAnyOfAcceptTypes(request, RtacMimeTypeEnum.APPLICATION_XML,
          RtacMimeTypeEnum.TEXT_XML, RtacMimeTypeEnum.ALL);
    } else {
      // For XML related accept headers there is allowed empty/no Accept header at all.
      // So we just return passed value if empty/no Accept header allowed, like for XML-related mime-type checking.
      return true;
    }
  }

  public static List<String> getAcceptHeaderList(HttpServerRequest request) {
    List<String> mimeTypes = request.headers().getAll(ACCEPT);
    if (CollectionUtils.isEmpty(mimeTypes)) {
      return Collections.emptyList();
    }

    return mimeTypes.stream()
        .map(reqMime -> reqMime.split(SEPARATOR_COMMA))
        .flatMap(reqMimeArray -> Arrays.stream(reqMimeArray))
        .map(String::trim).collect(Collectors.toList());
  }

  public static void returnEmptyResponse(RoutingContext ctx) {
    // NOTE: We always return a 200 even if holdings is empty here
    // because that's what the API we're trying to mimic does...
    // Yes, even if the response from mod-rtac is non-200!
    final var isXmlResponse = isXmlRequest(ctx.request());
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
  public static boolean checkSupportedAcceptHeaders(HttpServerRequest request) {
    if (!hasAcceptHeader(request)) {
      return true;
    } else {
      return hasRequestAnyOfAcceptTypes(request, RtacMimeTypeEnum.getAllTypes());
    }
  }

  public static String composeMimeTypes(String... mimeTypes) {
    String result = StringUtils.EMPTY;
    if (ArrayUtils.isNotEmpty(mimeTypes)) {
      result = Stream.of(mimeTypes).map(String::trim).collect(Collectors.joining(SEPARATOR_COMMA));
    }
    return result;
  }

  public static String composeMimeTypes(RtacMimeTypeEnum... mimeTypes) {
    return composeMimeTypes(
        Arrays.stream(Optional.ofNullable(mimeTypes).orElse(new RtacMimeTypeEnum[0]))
            .map(RtacMimeTypeEnum::toString).toArray(String[]::new));
  }

}
