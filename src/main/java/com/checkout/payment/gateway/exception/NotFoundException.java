package com.checkout.payment.gateway.exception;

public class NotFoundException extends PaymentGatewayException {
  public NotFoundException(String message) {
    super(message, 404);
  }

}
