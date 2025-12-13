package com.checkout.payment.gateway.exception;

public class BadRequestException extends PaymentGatewayException {
  public BadRequestException(String message) {
    super(message, 400);
  }

}
