package org.folio.edge.rtac.exception;

import lombok.Getter;

/**
 * Exception representing a soft error from mod-rtac.
 * This exception is handled by RtacErrorHandler to return a HoldingsError response
 * with HTTP 200 status, meaning the error is conveyed in the response body
 * without failing the HTTP request.
 */
@Getter
public class RtacSoftErrorException extends RuntimeException {

  private final String code;

  public RtacSoftErrorException(String code, String message) {
    super(message);
    this.code = code;
  }
}