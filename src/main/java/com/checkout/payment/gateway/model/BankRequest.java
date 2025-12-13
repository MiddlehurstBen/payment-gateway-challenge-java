package com.checkout.payment.gateway.model;

import lombok.Data;

@Data
public class BankRequest {
  private String cardNumber;
  private String expiryDate;
  private String currencyCode;
  private Integer amount;
  private String cvv;
}
