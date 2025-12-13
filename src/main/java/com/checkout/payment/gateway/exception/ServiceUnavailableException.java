package com.checkout.payment.gateway.exception;

public class ServiceUnavailableException extends PaymentGatewayException {

  public ServiceUnavailableException(String message) {
    super(message, 503);
  }
}
