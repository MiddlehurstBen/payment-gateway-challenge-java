package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(EventProcessingException.class)
  public ResponseEntity<ErrorResponse> handleException(EventProcessingException ex) {
    LOG.error("Exception happened", ex);
    
    // Check if it's a bank service unavailable error (503)
    if (ex.getMessage() != null && ex.getMessage().contains("Bank service unavailable")) {
      return new ResponseEntity<>(new ErrorResponse(ex.getMessage()),
          HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    // Check if it's a validation failure (400)
    if (ex.getMessage() != null && 
        (ex.getMessage().contains("Validation failed") ||
         ex.getMessage().contains("Bank processing failed"))) {
      return new ResponseEntity<>(new ErrorResponse(ex.getMessage()),
          HttpStatus.BAD_REQUEST);
    }
    
    // Not found (404)
    return new ResponseEntity<>(new ErrorResponse("Page not found"),
        HttpStatus.NOT_FOUND);
  }
  
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    LOG.error("Unexpected exception", ex);
    return new ResponseEntity<>(new ErrorResponse("An unexpected error occurred"),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
