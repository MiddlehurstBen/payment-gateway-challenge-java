package com.checkout.payment.gateway.exception;

public class InternalServerException extends PaymentGatewayException {
  public InternalServerException(String message) {
    super(message, 500);
  }

}
