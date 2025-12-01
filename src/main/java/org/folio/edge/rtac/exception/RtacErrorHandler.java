package org.folio.edge.rtac.exception;

import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.folio.rtac.domain.dto.Error;
import org.folio.rtac.domain.dto.HoldingsError;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
public class RtacErrorHandler {

  @ExceptionHandler(RtacSoftErrorException.class)
  public ResponseEntity<HoldingsError> handleRtacSoftErrorException(RtacSoftErrorException exception) {
    log.error("Rtac soft error, code: {}, message: {}", exception.getCode(), exception.getMessage());
    HoldingsError holdingsError = new HoldingsError();
    holdingsError.setCode(exception.getCode());
    holdingsError.setMessage(exception.getMessage());
    return ResponseEntity.ok(holdingsError);
  }

  @ExceptionHandler(FeignException.class)
  public ResponseEntity<String> handleFeignException(FeignException exception) {
    String properErrorMessage = exception.contentUTF8();
    log.error("Error occurred during service chain call, {}", properErrorMessage);
    return ResponseEntity.status(exception.status())
      .contentType(MediaType.APPLICATION_JSON)
      .body(properErrorMessage);
  }

  @ExceptionHandler({HttpMessageConversionException.class, ConstraintViolationException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Error handleExceptionWithBadRequestStatus(RuntimeException exception) {
    log.error("Not valid request cause, {}", exception.getMessage());
    return buildErrorResponse(HttpStatus.BAD_REQUEST.value(), exception);
  }

  private Error buildErrorResponse(int status, RuntimeException exception) {
    log.debug(exception.getMessage(), exception);
    Error errorResponse = new Error();
    errorResponse.setCode(status);
    errorResponse.setErrorMessage(exception.getMessage());
    return errorResponse;
  }
}