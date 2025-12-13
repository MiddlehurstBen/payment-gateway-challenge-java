package com.checkout.payment.gateway.model;

import lombok.Data;

@Data
public class BankResponse {
  private boolean authorized;
  private int httpStatusCode;
}
