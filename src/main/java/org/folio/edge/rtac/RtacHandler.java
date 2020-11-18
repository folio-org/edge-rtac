package org.folio.edge.rtac;

import static io.vertx.core.http.HttpHeaders.ACCEPT;
import static java.util.stream.Collectors.toList;
import static org.folio.edge.core.Constants.APPLICATION_XML;

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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.core.Handler;
import org.folio.edge.core.security.SecureStore;
import org.folio.edge.core.utils.Mappers;
import org.folio.edge.rtac.model.Holdings;
import org.folio.edge.rtac.model.Instances;
import org.folio.edge.rtac.utils.RtacOkapiClient;
import org.folio.edge.rtac.utils.RtacOkapiClientFactory;

public class RtacHandler extends Handler {

  private static final Logger logger = LogManager.getLogger(RtacHandler.class);

  private static final String FALLBACK_EMPTY_RESPONSE = Mappers.XML_PROLOG + "\n<holdings/>";

  private static final String PARAM_FULL_PERIODICALS = "fullPeriodicals";
  private static final String PARAM_TITLE_ID = "mms_id";
  private static final String PARAM_INSTANCE_ID = "instanceId";
  private static final String PARAM_INSTANCE_IDS = "instanceIds";

  private static final String SUPPORTED_MIME_TYPE_PATTERN = "(text|\\*)\\s*/\\s*(xml|\\*)|(application|\\*)\\s*/\\s*(xml|json|\\*)";
  private static final String XML_RELATED_MIME_TYPE_PATTERN = "(text|\\*)\\s*/\\s*(xml|\\*)|(application|\\*)\\s*/\\s*(xml|\\*)";
  private static final String JSON_RELATED_MIME_TYPE_PATTERN = "(application)\\s*/\\s*(json)";
  private static final String JSON_MIME_TYPE = "application/json";

  public RtacHandler(SecureStore secureStore, RtacOkapiClientFactory ocf) {
    super(secureStore, ocf);
  }

  protected void handle(RoutingContext ctx, boolean isBatch) {
    final var request = ctx.request();
    logger.info("Request: {} \n params: {}", request.uri(), request.params() );

    // Check if there were supported "Accept" headers passed
    if (!hasSupportedAcceptHeaders(request)) {
      notAcceptable(ctx, "Unsupported media type" + request.headers());
      return;
    }

    super.handleCommon(ctx,
      new String[] {},
      new String[] { PARAM_TITLE_ID, PARAM_INSTANCE_ID, PARAM_INSTANCE_IDS, PARAM_FULL_PERIODICALS },
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
          rtacParams.put(PARAM_FULL_PERIODICALS, Boolean.valueOf(params.get(PARAM_FULL_PERIODICALS)));
          instanceIds = Mappers.jsonMapper.writeValueAsString(rtacParams);
        } catch (JsonProcessingException e) {
          logger.error("Exception during serialization in mod-rtac", e);
          returnEmptyResponse(ctx);
          return;
        }

        final boolean hasAcceptHeader = hasNonEmptyAcceptHeader(request);
        // Remember original Accept headers
        final List<String> originAcceptHeaders = request.headers().getAll(ACCEPT);
        final boolean isAcceptHeadersUpdated;

        // Replace xml-related accept header by JSON ones, to make a call to RTAC (it supports JSON by default)
        if (hasRelatedAcceptHeaders(request, XML_RELATED_MIME_TYPE_PATTERN, false)) {
          updateAcceptHeadersWith(request, XML_RELATED_MIME_TYPE_PATTERN, JSON_MIME_TYPE);
          isAcceptHeadersUpdated = true;
        } else if (!hasAcceptHeader) {
          updateAcceptHeadersWith(request, JSON_MIME_TYPE);
          isAcceptHeadersUpdated = true;
        } else {
          isAcceptHeadersUpdated = false;
        }

        rtacClient.rtac(instanceIds, request.headers())
          .thenAcceptAsync(body -> {
            try {
              logger.debug("rtac response: {}", body);

              // Return back origin Accept headers if any
              if (isAcceptHeadersUpdated) {
                updateAcceptHeadersWith(request, originAcceptHeaders);
              }

              String returningContent;
              if (hasXmlRelatedAcceptHeaders(request)) {
                final var instances = Instances.fromJson(body);
                if (isBatch) {
                  returningContent = instances.toXml();
                } else {
                  var holdings = instances.getHoldings().isEmpty() ? new Holdings()
                      : instances.getHoldings().get(0);
                  returningContent = holdings.toXml();
                }
              } else if (hasJsonRelatedAcceptHeaders(request)) {
                // RTAC module returns content in JSON format,
                // so it will be used for case when there is no need to return in XML
                returningContent = body;
              } else {
                returningContent = StringUtils.EMPTY;
                logger.error("Returning content was not determined correctly, please check conditions.");
                returnEmptyResponse(ctx);
              }

              logger.info("Converted Response: \n {}", returningContent);
              ctx.response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_XML)
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
   * EDGE-RTAC supports application/xml or application/json  and all its derivatives in Accept header.
   * Empty Accept header implies any MIME type is accepted, same as Accept: *//*
   *
   * Valid examples: text/xml, text/*, *//*, *\xml, application/json, application/*, *//*, *\json
   * NOT Valid examples: application/xml, application/*, test/json
   *
   * @param request - http request to the module
   * @return - true if accept headers are supported
   */
  private boolean hasSupportedAcceptHeaders(HttpServerRequest request) {
    if (hasNonEmptyAcceptHeader(request)) {
      return ifAnyStringMatch(request.headers().getAll(ACCEPT), SUPPORTED_MIME_TYPE_PATTERN);
    } else {
      return true;
    }
  }

  private void updateAcceptHeadersWith(HttpServerRequest request, String findMimeTypePattern,
      String replaceWithMimeType) {
    String updatedHeaders = request.headers().getAll(ACCEPT).stream()
        .map(header -> header.split(","))
        .flatMap(array -> Arrays.stream(array))
        .map(entry -> entry.trim().matches(findMimeTypePattern) ? replaceWithMimeType : entry)
        .distinct()
        .collect(Collectors.joining(", "));

    updateAcceptHeadersWith(request, updatedHeaders);
  }

  private void updateAcceptHeadersWith(HttpServerRequest request, List<String> acceptHeaders) {
    updateAcceptHeadersWith(request, acceptHeaders.stream().toArray(String[]::new));
  }

  private void updateAcceptHeadersWith(HttpServerRequest request, String... acceptHeaders) {
    if (acceptHeaders == null) {
      return;
    }
    List<String> headerList = Arrays.asList(acceptHeaders);
    request.headers().remove(ACCEPT);
    headerList.forEach(c -> request.headers().add(ACCEPT, c));
  }

  private boolean ifAnyStringMatch(List<String> checkedList, String matchPattern) {
    final Pattern pattern = Pattern.compile(matchPattern);
    return checkedList
        .stream()
        .anyMatch(h -> pattern.matcher(h).find());
  }

  private boolean hasNonEmptyAcceptHeader(HttpServerRequest request) {
    return (request.headers().contains(ACCEPT) && StringUtils.isNotBlank(
        request.headers().getAll(ACCEPT).stream().map(String::trim).collect(Collectors.joining())));
  }

  /**
   * Check for particular Accept headers
   * @param request
   * @return
   */
  private boolean hasRelatedAcceptHeaders(HttpServerRequest request, String headersPattern, boolean allowEmptyAccept) {
    if (hasNonEmptyAcceptHeader(request)) {
      return ifAnyStringMatch(request.headers().getAll(ACCEPT), headersPattern);
    } else {
      // For XML related accept headers there is allowed empty/no Accept header at all.
      // So we just return passed value if empty/no Accept header allowed, like for XML-related mime-type checking.
      return allowEmptyAccept;
    }
  }

  /**
   * Check if there is accept header which lead to returning XML content
   * (by business cases described in the: https://issues.folio.org/browse/EDGRTAC-16)
   *
   * @param request
   * @return
   */
  private boolean hasXmlRelatedAcceptHeaders(HttpServerRequest request) {
    if (hasNonEmptyAcceptHeader(request)) {
      return hasRelatedAcceptHeaders(request, XML_RELATED_MIME_TYPE_PATTERN, true);
    } else {
      return true;
    }
  }

  /**
   * Check if there is accept header which lead to returning JSON content
   * (by business cases described in the: https://issues.folio.org/browse/EDGRTAC-16)
   *
   * @param request
   * @return
   */
  private boolean hasJsonRelatedAcceptHeaders(HttpServerRequest request) {
    return hasRelatedAcceptHeaders(request, JSON_RELATED_MIME_TYPE_PATTERN, false);
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
