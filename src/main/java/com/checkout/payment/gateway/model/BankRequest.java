package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BankRequest {
  @JsonProperty(value = "card_number", required = true)
  private String cardNumber;
  @JsonProperty(value = "expiry_date", required = true)
  private String expiryDate;
  @JsonProperty(value = "currency", required = true)
  private String currency;
  @JsonProperty(value = "amount", required = true)
  private Integer amount;
  @JsonProperty(value = "cvv", required = true)
  private String cvv;
}
