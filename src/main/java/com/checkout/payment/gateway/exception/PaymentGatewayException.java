package com.checkout.payment.gateway.exception;

public class PaymentGatewayException extends RuntimeException {
  private final int statusCode;

  public PaymentGatewayException(String message, int statusCode) {
    super(message);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
